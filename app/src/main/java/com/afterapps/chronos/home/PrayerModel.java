package com.afterapps.chronos.home;

/*
 * Created by mahmoud on 8/12/17.
 */

import com.afterapps.chronos.api.Responses.TimingsResponse;
import com.afterapps.chronos.api.ServiceGenerator;
import com.afterapps.chronos.api.TimingsService;
import com.afterapps.chronos.beans.Location;
import com.afterapps.chronos.beans.Prayer;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

class PrayerModel {

    interface PrayerCallback {
        void onLocationError();

        void onPrayersReady(List<Prayer> upcomingPrayersDetached);

        void onConnectionError();

        void onLocalError();
    }

    private final PrayerCallback mPrayerCallback;

    PrayerModel(PrayerCallback prayerCallback) {
        mPrayerCallback = prayerCallback;
    }

    void getPrayers(int method, int school, int latitudeMethod) {
        Realm realm = Realm.getDefaultInstance();
        Location location = realm.where(Location.class)
                .equalTo("selected", true)
                .findFirst();
        if (location == null) {
            mPrayerCallback.onLocationError();
            realm.close();
            return;
        }
        String timeZoneId = location.getTimezoneId();
        String signature = timeZoneId + method + school + latitudeMethod;
        long currentTimestamp = new Date().getTime();
        RealmResults<Prayer> upcomingPrayers = realm.where(Prayer.class)
                .equalTo("signature", signature)
                .greaterThan("timestamp", currentTimestamp)
                .findAllSorted("timestamp", Sort.ASCENDING);
        if (upcomingPrayers.size() == 0) {
            Location locationDetached = realm.copyFromRealm(location);
            fetchPrayers(method, school, latitudeMethod, locationDetached);
            realm.close();
            return;
        }
        List<Prayer> upcomingPrayersDetached = realm.copyFromRealm(upcomingPrayers);
        mPrayerCallback.onPrayersReady(upcomingPrayersDetached);
        realm.close();
    }

    private void fetchPrayers(final int method, final int school, final int latitudeMethod, final Location locationDetached) {
        Calendar currentTimeCal = Calendar.getInstance();
        int currentMonth = currentTimeCal.get(Calendar.MONTH);
        int currentYear = currentTimeCal.get(Calendar.YEAR);
        TimingsService timingsService = ServiceGenerator.createTimingsService();
        Call<TimingsResponse> timingsCall = timingsService.getTimings(
                locationDetached.getLatitude(),
                locationDetached.getLongitude(),
                locationDetached.getTimezoneId(),
                currentMonth,
                currentYear,
                method,
                school,
                latitudeMethod
        );
        timingsCall.enqueue(new Callback<TimingsResponse>() {
            @Override
            public void onResponse(Call<TimingsResponse> call, Response<TimingsResponse> response) {
                if (isResponseValid(response)) {
                    storePrayers(response.body(), method, school, latitudeMethod, locationDetached);
                } else {
                    mPrayerCallback.onConnectionError();
                }
            }

            @Override
            public void onFailure(Call<TimingsResponse> call, Throwable t) {
                mPrayerCallback.onConnectionError();
            }
        });
    }

    private void storePrayers(TimingsResponse timingsResponse,
                              int method,
                              int school,
                              int latitudeMethod,
                              Location locationDetached) {
        try {
            final List<Prayer> prayers = timingsResponse.getPrayers(method,
                    school,
                    latitudeMethod,
                    locationDetached);
            Realm realm = Realm.getDefaultInstance();
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    RealmResults<Prayer> oldPrayers = realm.where(Prayer.class).findAll();
                    oldPrayers.deleteAllFromRealm();
                    List<Prayer> prayersDetached =
                            realm.copyFromRealm(realm.copyToRealmOrUpdate(prayers));
                    mPrayerCallback.onPrayersReady(prayersDetached);
                }
            });
        } catch (IllegalAccessException e) {
            mPrayerCallback.onLocalError();
        }
    }

    @SuppressWarnings("ConstantConditions")
    private boolean isResponseValid(Response<TimingsResponse> response) {
        return response.isSuccessful() &&
                response.body() != null &&
                response.body().getStatus().equals("OK") &&
                response.body().getCode() == 200;
    }
}

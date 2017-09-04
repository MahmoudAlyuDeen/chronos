package com.afterapps.chronos.home;

/*
 * Created by mahmoud on 8/12/17.
 */

import android.support.annotation.NonNull;

import com.afterapps.chronos.api.Responses.TimingsResponse;
import com.afterapps.chronos.api.ServiceGenerator;
import com.afterapps.chronos.api.TimingsService;
import com.afterapps.chronos.beans.Location;
import com.afterapps.chronos.beans.Prayer;

import java.util.Calendar;
import java.util.List;

import io.realm.Realm;
import io.realm.Sort;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

class PrayerModel {

    private boolean isFetching;

    interface PrayerCallback {
        void onLocationError();

        void onPrayersReady(List<Prayer> prayersDetached);

        void onConnectionError();

        void onLogicError();
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
        Location locationDetached = realm.copyFromRealm(location);
        realm.close();
        String timeZoneId = locationDetached.getTimezoneId();
        String signature = timeZoneId + method + school + latitudeMethod;
        List<Prayer> prayersDetached = getPrayers(signature);
        if (prayersDetached.size() < 6) {
            isFetching = true;
            fetchPrayers(method, school, latitudeMethod, locationDetached, true);
            fetchPrayers(method, school, latitudeMethod, locationDetached, false);
        } else {
            mPrayerCallback.onPrayersReady(prayersDetached);
        }
    }

    private List<Prayer> getPrayers(String signature) {
        Realm realm = Realm.getDefaultInstance();
        List<Prayer> prayersDetached = realm.copyFromRealm(realm.where(Prayer.class)
                .equalTo("signature", signature)
                .findAllSorted("timestamp", Sort.ASCENDING));
        realm.close();
        return prayersDetached;
    }

    private void fetchPrayers(final int method,
                              final int school,
                              final int latitudeMethod,
                              final Location locationDetached,
                              final boolean prefetch) {
        final Calendar currentTimeCal = Calendar.getInstance();
        final int currentMonth = currentTimeCal.get(Calendar.MONTH) + 1;
        final int currentYear = currentTimeCal.get(Calendar.YEAR);
        final TimingsService timingsService = ServiceGenerator.createTimingsService();
        Call<TimingsResponse> timingsCall = timingsService.getTimings(
                locationDetached.getLatitude(),
                locationDetached.getLongitude(),
                locationDetached.getTimezoneId(),
                prefetch ? currentMonth == 11 ? currentMonth : currentMonth + 1 : currentMonth,
                prefetch && currentMonth == 11 ? currentYear + 1 : currentYear,
                method,
                school,
                latitudeMethod
        );
        timingsCall.enqueue(new Callback<TimingsResponse>() {
            @Override
            public void onResponse(@NonNull Call<TimingsResponse> call, @NonNull Response<TimingsResponse> response) {
                if (isResponseValid(response)) {
                    storePrayers(response.body(), method, school, latitudeMethod, locationDetached);
                } else {
                    mPrayerCallback.onConnectionError();
                }
            }

            @Override
            public void onFailure(@NonNull Call<TimingsResponse> call, @NonNull Throwable t) {
                mPrayerCallback.onConnectionError();
            }
        });
    }

    private void storePrayers(final TimingsResponse timingsResponse,
                              final int method,
                              final int school,
                              final int latitudeMethod,
                              final Location locationDetached) {
        try {
            final List<Prayer> prayers = timingsResponse.getPrayers(method,
                    school,
                    latitudeMethod,
                    locationDetached);
            Realm realm = Realm.getDefaultInstance();
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    realm.copyToRealmOrUpdate(prayers);
                    String timeZoneId = locationDetached.getTimezoneId();
                    String signature = timeZoneId + method + school + latitudeMethod;
                    List<Prayer> prayersDetached = getPrayers(signature);

                    if (isFetching) {
                        isFetching = false;
                    } else {
                        if (prayersDetached.size() == 0) {
                            mPrayerCallback.onLogicError();
                            return;
                        }
                        mPrayerCallback.onPrayersReady(prayersDetached);
                    }
                }
            });
        } catch (IllegalAccessException e) {
            mPrayerCallback.onLogicError();
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
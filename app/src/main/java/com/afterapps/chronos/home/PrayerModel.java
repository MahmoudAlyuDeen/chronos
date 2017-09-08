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

import static com.afterapps.chronos.Constants.FETCH_THRESHOLD;
import static com.afterapps.chronos.Constants.PREFETCH_THRESHOLD;

public class PrayerModel {

    private boolean shouldWaitForConcurrentResponse;

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

    void getPrayers(final String method, final String school, final String latitudeMethod) {
        final Realm realm = Realm.getDefaultInstance();
        final Location location = realm.where(Location.class)
                .equalTo("selected", true)
                .findFirst();
        if (location == null) {
            mPrayerCallback.onLocationError();
            realm.close();
            return;
        }
        final Location locationDetached = realm.copyFromRealm(location);
        realm.close();
        final String timeZoneId = locationDetached.getTimezoneId();
        final String signature = timeZoneId + method + school + latitudeMethod;
        final List<Prayer> prayersDetached = getStoredPrayers(signature);
        if (prayersDetached.size() < FETCH_THRESHOLD) {
            shouldWaitForConcurrentResponse = true;
            fetchPrayers(method, school, latitudeMethod, locationDetached, true);
            fetchPrayers(method, school, latitudeMethod, locationDetached, false);
        } else {
            if (prayersDetached.size() < PREFETCH_THRESHOLD) {
                fetchPrayers(method, school, latitudeMethod, locationDetached, true);
            }
            mPrayerCallback.onPrayersReady(prayersDetached);
        }
    }

    public static List<Prayer> getStoredPrayers(String signature) {
        final Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        final long midnightTimestamp = calendar.getTimeInMillis();
        final Realm realm = Realm.getDefaultInstance();
        final List<Prayer> prayersDetached = realm.copyFromRealm(realm.where(Prayer.class)
                .equalTo("signature", signature)
                .greaterThan("timestamp", midnightTimestamp)
                .findAllSorted("timestamp", Sort.ASCENDING));
        realm.close();
        return prayersDetached;
    }

    private void fetchPrayers(final String method,
                              final String school,
                              final String latitudeMethod,
                              final Location locationDetached,
                              final boolean prefetch) {
        Call<TimingsResponse> timingsCall = getTimingsCall(method, school, latitudeMethod, locationDetached, Calendar.getInstance(), prefetch);
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

    public static Call<TimingsResponse> getTimingsCall(final String method,
                                                       final String school,
                                                       final String latitudeMethod,
                                                       final Location locationDetached,
                                                       final Calendar currentTimeCal,
                                                       final boolean prefetch) {
        final int currentMonth = currentTimeCal.get(Calendar.MONTH) + 1;
        final int currentYear = currentTimeCal.get(Calendar.YEAR);
        final TimingsService timingsService = ServiceGenerator.createTimingsService();
        return timingsService.getTimings(
                locationDetached.getLatitude(),
                locationDetached.getLongitude(),
                locationDetached.getTimezoneId(),
                prefetch ? currentMonth == 11 ? currentMonth : currentMonth + 1 : currentMonth,
                prefetch && currentMonth == 11 ? currentYear + 1 : currentYear,
                method,
                school,
                latitudeMethod
        );
    }

    private void storePrayers(final TimingsResponse timingsResponse,
                              final String method,
                              final String school,
                              final String latitudeMethod,
                              final Location locationDetached) {
        try {
            final List<Prayer> prayers = timingsResponse.getPrayers(method,
                    school,
                    latitudeMethod,
                    locationDetached);
            final Realm realm = Realm.getDefaultInstance();
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    realm.copyToRealmOrUpdate(prayers);
                    final String timeZoneId = locationDetached.getTimezoneId();
                    final String signature = timeZoneId + method + school + latitudeMethod;
                    final List<Prayer> prayersDetached = getStoredPrayers(signature);

                    if (shouldWaitForConcurrentResponse) {
                        shouldWaitForConcurrentResponse = false;
                    } else {
                        if (prayersDetached.size() == 0) {
                            mPrayerCallback.onLogicError();
                            return;
                        }
                        mPrayerCallback.onPrayersReady(prayersDetached);
                    }
                }
            });
            realm.close();
        } catch (IllegalAccessException e) {
            mPrayerCallback.onLogicError();
        }
    }

    @SuppressWarnings("ConstantConditions")
    public static boolean isResponseValid(Response<TimingsResponse> response) {
        return response.isSuccessful() &&
                response.body() != null &&
                response.body().getStatus().equals("OK") &&
                response.body().getCode() == 200;
    }
}
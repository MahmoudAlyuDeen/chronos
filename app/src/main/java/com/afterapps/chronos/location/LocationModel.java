package com.afterapps.chronos.location;

/*
 * Created by mahmoudalyudeen on 7/24/17.
 */

import android.support.annotation.NonNull;

import com.afterapps.chronos.api.Responses.ReverseGeoLocResponse;
import com.afterapps.chronos.api.ReverseGeoLocService;
import com.afterapps.chronos.api.ServiceGenerator;
import com.afterapps.chronos.beans.Location;

import java.util.Date;

import javax.net.ssl.SSLHandshakeException;

import io.realm.Realm;
import io.realm.RealmResults;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.afterapps.chronos.Constants.GOOGLE_REVERSE_GEO_LOC_API_KEY;

class LocationModel {

    interface LocationCallBack {

        void onLocationHandled();

        void onReverseGeolocationError();

        void onConnectionError();
    }

    private final LocationCallBack mLocationCallBack;
    private Call<ReverseGeoLocResponse> mReverseGeoLocCall;

    LocationModel(LocationCallBack locationCallBack) {
        mLocationCallBack = locationCallBack;
    }

    void onLocationSelected(String timezoneId) {
        unSelectOldLocations();
        selectLocation(timezoneId);
        mLocationCallBack.onLocationHandled();
    }

    void onLocationDetected(final android.location.Location geoLocation) {
        final ReverseGeoLocService service =
                ServiceGenerator.createGeoLocService();
        if (mReverseGeoLocCall != null) {
            mReverseGeoLocCall.cancel();
        }
        mReverseGeoLocCall = service.getReverseGeo(GOOGLE_REVERSE_GEO_LOC_API_KEY,
                Long.toString(new Date().getTime() / 1000),
                getCoordinates(geoLocation));

        mReverseGeoLocCall.enqueue(new Callback<ReverseGeoLocResponse>() {
            @SuppressWarnings("ConstantConditions")
            @Override
            public void onResponse(@NonNull Call<ReverseGeoLocResponse> call,
                                   @NonNull Response<ReverseGeoLocResponse> response) {
                if (isResponseValid(response)) {
                    unSelectOldLocations();
                    createLocation(geoLocation, response.body());
                    selectLocation(response.body().getTimeZoneId());
                    mLocationCallBack.onLocationHandled();
                } else {
                    mLocationCallBack.onConnectionError();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ReverseGeoLocResponse> call,
                                  @NonNull Throwable t) {
                if (t instanceof SSLHandshakeException) {
                    mLocationCallBack.onReverseGeolocationError();
                } else {
                    mLocationCallBack.onConnectionError();
                }
            }
        });
    }

    private void selectLocation(final String timeZoneId) {
        final Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                final Location location = realm.where(Location.class)
                        .equalTo("timezoneId", timeZoneId).findFirst();
                if (location != null) {
                    location.setSelected(true);
                }
                realm.close();
            }
        });
    }

    private void createLocation(final android.location.Location geoLocation,
                                final ReverseGeoLocResponse reverseGeoLocResponse) {
        final Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                final Location location = realm.where(Location.class)
                        .equalTo("timezoneId", reverseGeoLocResponse.getTimeZoneId()).findFirst();
                if (location == null) {
                    final Location newLocation = new Location(geoLocation, reverseGeoLocResponse);
                    realm.copyToRealmOrUpdate(newLocation);
                }
                realm.close();
            }
        });
    }

    private void unSelectOldLocations() {
        final Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                final RealmResults<Location> locations = realm.where(Location.class).findAll();
                for (Location oldLocation : locations) {
                    oldLocation.setSelected(false);
                }
                realm.close();
            }
        });
    }

    private String getCoordinates(android.location.Location geoLocation) {
        return geoLocation.getLatitude() + "," + geoLocation.getLongitude();
    }

    @SuppressWarnings("ConstantConditions")
    private boolean isResponseValid(Response<ReverseGeoLocResponse> response) {
        return response.isSuccessful() &&
                response.body() != null &&
                response.body().getStatus() != null &&
                response.body().getStatus().equals("OK") &&
                response.body().getTimeZoneId() != null;
    }
}

package com.afterapps.chronos;

/*
 * Created by mahmoudalyudeen on 6/7/17.
 */

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.hannesdorfmann.mosby3.mvp.MvpPresenter;
import com.hannesdorfmann.mosby3.mvp.MvpView;

import icepick.State;
import pub.devrel.easypermissions.EasyPermissions;

import static com.afterapps.chronos.Constants.LOCATION_REQUEST_TIMEOUT;

public abstract class BaseLocationActivity<V extends MvpView, P extends MvpPresenter<V>>
        extends BaseActivity<V, P>
        implements ResultCallback<LocationSettingsResult>,
        LocationListener,
        GoogleApiClient.ConnectionCallbacks {

    private static final int REQUEST_PERMISSION = 1;
    private static final int REQUEST_CHECK_SETTINGS = 2;

    @State
    protected boolean askedForLocationPermission;

    @State
    protected boolean isWaitingForLocation;

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private LocationCallback mLocationCallback;

    @NonNull
    @Override
    public abstract P createPresenter();

    protected void connectLocationClient() {
        if (mGoogleApiClient != null) {
            if (mGoogleApiClient.isConnected()) {
                mGoogleApiClient.disconnect();
            }
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        if (mFusedLocationProviderClient != null && mLocationCallback != null) {
            isWaitingForLocation = true;
            mFusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (isWaitingForLocation) {
            connectLocationClient();
            isWaitingForLocation = false;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .build();
    }

    protected void checkLocationPermissionAndRequestLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            if (askedForLocationPermission) {
                showAction();
            } else {
                askedForLocationPermission = true;
                final String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION};
                EasyPermissions.requestPermissions(this,
                        getString(R.string.rationale_location),
                        R.string.ok,
                        R.string.cancel,
                        REQUEST_PERMISSION,
                        perms);
            }
            return;
        }

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setExpirationDuration(LOCATION_REQUEST_TIMEOUT);
        mLocationRequest.setNumUpdates(1);

        final LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .setAlwaysShow(true)
                .addLocationRequest(mLocationRequest);
        final PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient,
                        builder.build());
        result.setResultCallback(BaseLocationActivity.this);
    }

    @Override
    public void onResult(@NonNull LocationSettingsResult result) {
        final Status status = result.getStatus();
        switch (status.getStatusCode()) {
            case LocationSettingsStatusCodes.SUCCESS:
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                                != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                mLocationCallback = new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        showContent();
                        onLocationChanged(locationResult.getLastLocation());
                    }
                };
                mFusedLocationProviderClient
                        .requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                showProgress();
                break;
            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                try {
                    status.startResolutionForResult(this, REQUEST_CHECK_SETTINGS);
                } catch (IntentSender.SendIntentException ignored) {
                }
                break;
            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                showLocationDetectionError();
                break;
        }
    }

    protected abstract void showLocationDetectionError();

    @Override
    public void onLocationChanged(Location location) {
        isWaitingForLocation = false;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        checkLocationPermissionAndRequestLocation();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        checkLocationPermissionAndRequestLocation();
                        break;
                    case Activity.RESULT_CANCELED:
                        onChangeSettingsDenied();
                        break;
                }
                break;
        }
    }

    protected abstract void onChangeSettingsDenied();

    @Override
    public void onConnectionSuspended(int i) {
    }
}
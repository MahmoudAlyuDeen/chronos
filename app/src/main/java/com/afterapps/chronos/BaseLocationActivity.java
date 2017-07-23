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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.hannesdorfmann.mosby3.mvp.MvpPresenter;
import com.hannesdorfmann.mosby3.mvp.MvpView;

import icepick.State;
import pub.devrel.easypermissions.EasyPermissions;

public abstract class BaseLocationActivity<V extends MvpView, P extends MvpPresenter<V>>
        extends BaseActivity<V, P>
        implements ResultCallback<LocationSettingsResult>,
        LocationListener, GoogleApiClient.ConnectionCallbacks {

    private static final int REQUEST_PERMISSION = 1;
    private static final int REQUEST_CHECK_SETTINGS = 2;

    @State
    boolean askedForLocationPermission;

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    @NonNull
    @Override
    public abstract P createPresenter();

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
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
                String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION,
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

        LocationServices.getFusedLocationProviderClient(this)
                .getLastLocation()
                .addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            onLocationChanged(task.getResult());
                        } else {
                            mLocationRequest = LocationRequest.create();
                            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                            mLocationRequest.setExpirationDuration(60000);
                            mLocationRequest.setInterval(10000);
                            mLocationRequest.setFastestInterval(500);
                            mLocationRequest.setNumUpdates(1);

                            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                                    .setAlwaysShow(true)
                                    .addLocationRequest(mLocationRequest);
                            PendingResult<LocationSettingsResult> result =
                                    LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient,
                                            builder.build());
                            result.setResultCallback(BaseLocationActivity.this);
                        }
                    }
                });
    }

    @Override
    public void onResult(@NonNull LocationSettingsResult result) {
        final Status status = result.getStatus();
        switch (status.getStatusCode()) {
            case LocationSettingsStatusCodes.SUCCESS:
                checkLocationPermissionAndRequestLocation();
                break;
            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                try {
                    status.startResolutionForResult(this, REQUEST_CHECK_SETTINGS);
                } catch (IntentSender.SendIntentException ignored) {
                }
                break;
            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                showError();
                break;
        }
    }

    @Override
    public abstract void onLocationChanged(Location location);

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
                        // All required changes were successfully made
                        checkLocationPermissionAndRequestLocation();
                        break;
                    case Activity.RESULT_CANCELED:
                        // The user was asked to change settings, but chose not to
                        showAction();
                        break;
                }
                break;
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
    }
}
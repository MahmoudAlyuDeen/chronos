package com.afterapps.chronos.location;

import android.location.Location;

import com.hannesdorfmann.mosby3.mvp.MvpBasePresenter;

/*
 * Created by mahmoudalyudeen on 4/19/17.
 */

class LocationPresenter extends MvpBasePresenter<LocationView> implements LocationModel.LocationCallBack {

    private LocationModel mLocationModel;

    void onLocationDetected(Location geoLocation) {
        if (mLocationModel == null) {
            mLocationModel = new LocationModel(this);
        }
        mLocationModel.onLocationDetected(geoLocation);
    }

    @Override
    public void onLocationHandled() {
        if (isViewAttached() && getView() != null) {
            getView().onLocationHandled();
        }
    }

    @Override
    public void onLocationError() {
        if (isViewAttached() && getView() != null) {
            getView().onLocationError();
        }
    }

    void onLocationSelected(String timezoneId) {
        if (mLocationModel == null) {
            mLocationModel = new LocationModel(this);
        }
        mLocationModel.onLocationSelected(timezoneId);
    }
}

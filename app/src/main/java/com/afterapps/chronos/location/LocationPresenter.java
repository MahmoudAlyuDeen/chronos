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
        if (isViewAttached() && getView() != null) {
            getView().showProgress();
        }
    }

    @Override
    public void onLocationHandled() {
        if (isViewAttached() && getView() != null) {
            getView().onLocationHandled();
            getView().showContent();
        }
    }

    @Override
    public void onReverseGeolocationError() {
        if (isViewAttached() && getView() != null) {
            getView().onReverseGeolocationError();
            getView().showContent();
        }
    }

    @Override
    public void onConnectionError() {
        if (isViewAttached() && getView() != null) {
            getView().showConnectionError();
            getView().showContent();
        }
    }

    void onLocationSelected(String timezoneId) {
        if (mLocationModel == null) {
            mLocationModel = new LocationModel(this);
        }
        mLocationModel.onLocationSelected(timezoneId);
    }
}

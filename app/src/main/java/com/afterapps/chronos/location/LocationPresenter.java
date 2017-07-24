package com.afterapps.chronos.location;

import android.location.Location;

import com.hannesdorfmann.mosby3.mvp.MvpBasePresenter;

/*
 * Created by mahmoudalyudeen on 4/19/17.
 */

class LocationPresenter extends MvpBasePresenter<LocationView> implements LocationModel.LocationCallBack {

    private LocationModel mLocationModel;

    void onLocationAvailable(Location geoLocation) {
        if (mLocationModel == null) {
            mLocationModel = new LocationModel(this);
        }
        mLocationModel.handleLocation(geoLocation);
    }

    @Override
    public void onLocationHandled() {

    }

    @Override
    public void onLocationError() {

    }
}

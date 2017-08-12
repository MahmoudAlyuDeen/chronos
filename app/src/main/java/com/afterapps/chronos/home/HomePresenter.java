package com.afterapps.chronos.home;

import com.afterapps.chronos.beans.Prayer;
import com.hannesdorfmann.mosby3.mvp.MvpBasePresenter;

import java.util.List;

/*
 * Created by mahmoudalyudeen on 4/19/17.
 */

class HomePresenter extends MvpBasePresenter<HomeView>
        implements PrayerModel.PrayerCallback {

    private PrayerModel mPrayerModel;

    void getPrayers(int method, int school, int latitudeMethod) {
        if (mPrayerModel == null ){
            mPrayerModel = new PrayerModel(this);
        }
        mPrayerModel.getPrayers(method, school, latitudeMethod);
        if (isViewAttached() && getView() != null) {
            getView().showProgress();
        }
    }

    @Override
    public void onLocationError() {
        if (isViewAttached() && getView() != null) {
            getView().showEmpty();
        }
    }

    @Override
    public void onPrayersReady(List<Prayer> upcomingPrayersDetached) {
        if (isViewAttached() && getView() != null) {
            getView().showContent();
        }
    }

    @Override
    public void onConnectionError() {
        if (isViewAttached() && getView() != null) {
            getView().showAction();
        }
    }

    @Override
    public void onLocalError() {
        if (isViewAttached() && getView() != null) {
            getView().showError();
        }
    }
}

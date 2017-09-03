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
        if (mPrayerModel == null) {
            mPrayerModel = new PrayerModel(this);
        }
        if (isViewAttached() && getView() != null) {
            getView().showProgress();
        }
        mPrayerModel.getPrayers(method, school, latitudeMethod);
    }

    @Override
    public void onPrayersReady(List<Prayer> upcomingPrayersDetached) {
        if (isViewAttached() && getView() != null) {
            getView().onPrayersReady(upcomingPrayersDetached);
            getView().showContent();
        }
    }

    @Override
    public void onLocationError() {
        if (isViewAttached() && getView() != null) {
            getView().showEmpty();
        }
    }

    @Override
    public void onConnectionError() {
        if (isViewAttached() && getView() != null) {
            getView().showAction();
        }
    }

    @Override
    public void onLogicError() {
        if (isViewAttached() && getView() != null) {
            getView().showError();
        }
    }
}
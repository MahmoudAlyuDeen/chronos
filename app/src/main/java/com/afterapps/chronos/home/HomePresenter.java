package com.afterapps.chronos.home;

import com.afterapps.chronos.beans.Prayer;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.hannesdorfmann.mosby3.mvp.MvpBasePresenter;

import java.util.List;

/*
 * Created by mahmoudalyudeen on 4/19/17.
 */

class HomePresenter extends MvpBasePresenter<HomeView>
        implements PrayerModel.PrayerCallback {

    private PrayerModel mPrayerModel;
    private List<Prayer> mUpcomingPrayers;

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

    List<Prayer> getUpcomingPrayers(List<Prayer> prayerList, final long currentTimestamp) {
        List<Prayer> upcomingPrayers = Lists.newArrayList(Iterables.filter(prayerList, new Predicate<Prayer>() {
            @Override
            public boolean apply(Prayer prayer) {
                return prayer.getTimestamp() > currentTimestamp;
            }
        }));
        if (mUpcomingPrayers == null || mUpcomingPrayers.size() != upcomingPrayers.size()) {
            this.mUpcomingPrayers = upcomingPrayers;
            return upcomingPrayers.size() >= 6 ? upcomingPrayers.subList(0, 6) : upcomingPrayers;
        } else {
            return null;
        }
    }
}
package com.afterapps.chronos.home;

import com.afterapps.chronos.beans.Prayer;
import com.afterapps.chronos.job.PrayersJob;
import com.evernote.android.job.JobManager;
import com.hannesdorfmann.mosby3.mvp.MvpBasePresenter;

import java.util.List;

/*
 * Created by mahmoudalyudeen on 4/19/17.
 */

class HomePresenter extends MvpBasePresenter<HomeView>
        implements PrayerModel.PrayerCallback {

    private PrayerModel mPrayerModel;

    void getPrayers(final String method, final String school, final String latitudeMethod, final String uid) {
        if (mPrayerModel == null) {
            mPrayerModel = new PrayerModel(this, uid);
        }
        if (isViewAttached() && getView() != null) {
            getView().showProgress();
        }
        mPrayerModel.getPrayers(method, school, latitudeMethod);
    }

    @Override
    public void onPrayersReady(final List<Prayer> prayersDetached) {
        JobManager.instance().cancelAllForTag(PrayersJob.TAG);
        if (isViewAttached() && getView() != null) {
            getView().onPrayersReady(prayersDetached);
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
package com.afterapps.chronos.home;

import com.afterapps.chronos.beans.Prayer;
import com.hannesdorfmann.mosby3.mvp.MvpView;

import java.util.List;

/*
 * Created by mahmoudalyudeen on 4/19/17.
 */

interface HomeView extends MvpView {

    void showProgress();

    void showAction();

    void showError();

    void showEmpty();

    void showContent();

    void onPrayersReady(List<Prayer> upcomingPrayersDetached);
}

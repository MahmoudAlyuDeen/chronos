package com.afterapps.chronos.home;

import com.hannesdorfmann.mosby3.mvp.MvpView;

/*
 * Created by mahmoudalyudeen on 4/19/17.
 */

interface HomeView extends MvpView {

    void showProgress();

    void showAction();

    void showError();

    void showEmpty();

    void showContent();
}

package com.afterapps.chronos.location;

import com.hannesdorfmann.mosby3.mvp.MvpView;

/*
 * Created by mahmoudalyudeen on 4/19/17.
 */

interface LocationView extends MvpView {

    void onLocationHandled();

    void onReverseGeolocationError();

    void showConnectionError();

    void showProgress();

    void showContent();
}

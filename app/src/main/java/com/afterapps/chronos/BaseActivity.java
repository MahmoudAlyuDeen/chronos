package com.afterapps.chronos;

import android.os.Bundle;

import com.google.firebase.crash.FirebaseCrash;
import com.hannesdorfmann.mosby3.mvp.MvpActivity;
import com.hannesdorfmann.mosby3.mvp.MvpPresenter;
import com.hannesdorfmann.mosby3.mvp.MvpView;

import icepick.Icepick;
import icepick.State;

import static com.afterapps.chronos.Constants.VIEW_STATE_ACTION;
import static com.afterapps.chronos.Constants.VIEW_STATE_CONTENT;
import static com.afterapps.chronos.Constants.VIEW_STATE_EMPTY;
import static com.afterapps.chronos.Constants.VIEW_STATE_ERROR;
import static com.afterapps.chronos.Constants.VIEW_STATE_IDLE;
import static com.afterapps.chronos.Constants.VIEW_STATE_PROGRESS;

@SuppressWarnings({"unused", "EmptyMethod"})
public abstract class BaseActivity<V extends MvpView, P extends MvpPresenter<V>>
        extends MvpActivity<V, P>
        implements MvpView {

    @State
    protected int viewState;
    @State
    protected boolean userInteracted;

    protected boolean landscape;
    protected boolean arabic;

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Icepick.saveInstanceState(this, outState);
    }

    public void showProgress() {
        viewState = VIEW_STATE_PROGRESS;
        displayViewState();
    }

    public void showError() {
        reportCrash();
        viewState = VIEW_STATE_ERROR;
        displayViewState();
    }

    public void showEmpty() {
        viewState = VIEW_STATE_EMPTY;
        displayViewState();
    }

    public void showContent() {
        viewState = VIEW_STATE_CONTENT;
        displayViewState();
    }

    public void showAction() {
        viewState = VIEW_STATE_ACTION;
        displayViewState();
    }

    protected void reportCrash() {
        FirebaseCrash.log("Logic error occurred");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Icepick.restoreInstanceState(this, savedInstanceState);
        retainInstance = true;
        landscape = getResources().getBoolean(R.bool.landscape);
        arabic = getResources().getBoolean(R.bool.arabic);
        if (savedInstanceState == null) {
            viewState = VIEW_STATE_IDLE;
            userInteracted = false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        displayViewState();
    }

    protected void displayViewState() {
    }

    @Override
    public boolean onSupportNavigateUp() {
        if (userInteracted) {
            showDataLossConfirmation();
        } else {
            finish();
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if (userInteracted) {
            showDataLossConfirmation();
        } else {
            finish();
        }
    }

    private void showDataLossConfirmation() {
    }

    protected void discardData() {
    }
}

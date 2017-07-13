package com.afterapps.chronos;

import android.os.Bundle;
import android.widget.Toast;

import com.hannesdorfmann.mosby3.mvp.MvpActivity;
import com.hannesdorfmann.mosby3.mvp.MvpPresenter;
import com.hannesdorfmann.mosby3.mvp.MvpView;

import icepick.Icepick;
import icepick.State;

@SuppressWarnings("unused")
public abstract class BaseActivity<V extends MvpView, P extends MvpPresenter<V>> extends MvpActivity<V, P> implements MvpView {

    @State
    protected boolean isLoading;
    @State
    protected boolean userInteracted;

    protected boolean landscape;

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Icepick.saveInstanceState(this, outState);
    }

    public void showProgress() {
        isLoading = true;
        displayLoadingState();
    }

    public void hideProgress() {
        isLoading = false;
        displayLoadingState();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Icepick.restoreInstanceState(this, savedInstanceState);
        retainInstance = true;
        landscape = getResources().getBoolean(R.bool.landscape);
        if (savedInstanceState == null) {
            isLoading = false;
            userInteracted = false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        displayLoadingState();
    }

    protected void displayLoadingState() {
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

    protected void discardData() {
    }

    public void showConnectionError() {
        Toast.makeText(this, R.string.error_connection, Toast.LENGTH_SHORT).show();
    }

    public void showLoadingError() {
        Toast.makeText(this, R.string.error_loading, Toast.LENGTH_SHORT).show();
    }

    //todo: uncomment and configure when implementing authentication
    public void showAuthorizationError() {
//        new MaterialDialog.Builder(this)
//                .title(R.string.dialog_auth_title)
//                .content(R.string.dialog_auth_message)
//                .positiveText(R.string.dialog_auth_positive_button)
//                .theme(Resources.Theme.DARK)
//                .cancelable(false)
//                .onPositive(new MaterialDialog.SingleButtonCallback() {
//                    @Override
//                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
//                        ((Application) getApplication()).performSignOut(BaseActivity.this);
//                    }
//                })
//                .autoDismiss(true)
//                .show();
    }

    protected void showDataLossConfirmation() {
//        new MaterialDialog.Builder(this)
//                .title(R.string.dialog_date_loss_title)
//                .content(R.string.dialog_date_loss_message)
//                .positiveText(R.string.dialog_date_loss_positive_button)
//                .negativeText(R.string.dialog_date_loss_negative_button)
//                .theme(Theme.DARK)
//                .positiveColor(ContextCompat.getColor(this, R.color.colorRed))
//                .onPositive(new MaterialDialog.SingleButtonCallback() {
//                    @Override
//                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
//                        discardData();
//                    }
//                })
//                .onNegative(new MaterialDialog.SingleButtonCallback() {
//                    @Override
//                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
//                        dialog.dismiss();
//                    }
//                })
//                .show();
    }
}

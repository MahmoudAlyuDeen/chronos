package com.afterapps.chronos.home;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;

import com.afterapps.chronos.BaseActivity;
import com.afterapps.chronos.R;

import butterknife.ButterKnife;

public class HomeActivity
        extends BaseActivity<HomeView, HomePresenter>
        implements HomeView, SharedPreferences.OnSharedPreferenceChangeListener {

    private SharedPreferences mPref;

    @NonNull
    @Override
    public HomePresenter createPresenter() {
        return new HomePresenter();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mPref != null) {
            mPref.registerOnSharedPreferenceChangeListener(this);
            onSharedPreferenceChanged(mPref, "");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mPref != null) {
            mPref.unregisterOnSharedPreferenceChangeListener(this);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        mPref = PreferenceManager.getDefaultSharedPreferences(this);
        ButterKnife.bind(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (mPref == null) return;
        int method = mPref.getInt(getString(R.string.preference_key_method), 5);
        int school = mPref.getInt(getString(R.string.preference_key_school), 0);
        int latitudeMethod = mPref.getInt(getString(R.string.preference_key_latitude), 3);
        presenter.getPrayers(method, school, latitudeMethod);
    }

    @Override
    protected void displayViewState() {
        Log.d("@@@@", "displayViewState: " + viewState);
    }
}

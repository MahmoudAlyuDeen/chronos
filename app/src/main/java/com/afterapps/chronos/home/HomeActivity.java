package com.afterapps.chronos.home;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.afterapps.chronos.BaseActivity;
import com.afterapps.chronos.R;
import com.afterapps.chronos.beans.Prayer;
import com.afterapps.chronos.location.LocationActivity;

import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class HomeActivity
        extends BaseActivity<HomeView, HomePresenter>
        implements HomeView, SharedPreferences.OnSharedPreferenceChangeListener {

    @BindView(R.id.home_prayers_recycler)
    RecyclerView mHomePrayersRecycler;
    @BindView(R.id.home_toolbar)
    Toolbar mHomeToolbar;

    private SharedPreferences mPref;

    private List<Prayer> mPrayerList;

    private Handler mTickerHandler;
    private Runnable mTickerRunnable;

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
        stopTicking();
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
        setSupportActionBar(mHomeToolbar);
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

    @Override
    public void onPrayersReady(List<Prayer> upcomingPrayersDetached) {
        mPrayerList = upcomingPrayersDetached;
        startTicking();
    }

    void startTicking() {
        mTickerHandler = new Handler();
        mTickerRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    filterPrayers();
                } finally {
                    mTickerHandler.postDelayed(mTickerRunnable, 1000);
                }
            }
        };
        mTickerRunnable.run();
    }

    void stopTicking() {
        if (mTickerHandler != null && mTickerRunnable != null) {
            mTickerHandler.removeCallbacks(mTickerRunnable);
        }
    }

    private void filterPrayers() {
        List<Prayer> upcomingPrayers = presenter.getUpcomingPrayers(mPrayerList,
                new Date().getTime(),
                mHomePrayersRecycler.getAdapter() == null);
        if (upcomingPrayers != null) {
            displayPrayerSchedule(upcomingPrayers);
        }
    }

    private void displayPrayerSchedule(List<Prayer> upcomingPrayers) {
        PrayersAdapter adapter = new PrayersAdapter(this, upcomingPrayers, arabic);
        mHomePrayersRecycler.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_location:
                Intent location = new Intent(this, LocationActivity.class);
                startActivity(location);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}

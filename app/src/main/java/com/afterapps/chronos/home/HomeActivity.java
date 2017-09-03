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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afterapps.chronos.BaseActivity;
import com.afterapps.chronos.Constants;
import com.afterapps.chronos.R;
import com.afterapps.chronos.beans.Prayer;
import com.afterapps.chronos.location.LocationActivity;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.ButterKnife;

public class HomeActivity
        extends BaseActivity<HomeView, HomePresenter>
        implements HomeView, SharedPreferences.OnSharedPreferenceChangeListener {

    @BindView(R.id.home_prayers_recycler)
    RecyclerView mHomePrayersRecycler;
    @BindView(R.id.home_toolbar)
    Toolbar mHomeToolbar;
    @BindView(R.id.home_app_bar_timing_text_view)
    TextView mHomeAppBarTimingTextView;
    @BindView(R.id.home_app_bar_timing_subtitle_text_view)
    TextView mHomeAppBarTimingSubtitleTextView;
    @BindView(R.id.home_app_bar_text_parent)
    LinearLayout mHomeAppBarTextParent;
    @BindView(R.id.home_app_bar_icon_image_view)
    ImageView mHomeAppBarIconImageView;

    private SharedPreferences mPref;

    private List<Prayer> mPrayerList;

    private Handler mTickerHandler;
    private Runnable mTickerRunnable;

    private SimpleDateFormat timeFormat;

    private List<Prayer> mUpcomingPrayers;
    private Prayer mUpcomingPrayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        mPref = PreferenceManager.getDefaultSharedPreferences(this);
        timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.US);
        timeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        ButterKnife.bind(this);
        setSupportActionBar(mHomeToolbar);
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
        mUpcomingPrayer = null;
        if (mPref != null) {
            mPref.unregisterOnSharedPreferenceChangeListener(this);
        }
    }

    @NonNull
    @Override
    public HomePresenter createPresenter() {
        return new HomePresenter();
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
        final long currentTimestamp = new Date().getTime();
        final List<Prayer> allUpcomingPrayers =
                Lists.newArrayList(Iterables.filter(mPrayerList, new Predicate<Prayer>() {
                    @Override
                    public boolean apply(Prayer prayer) {
                        return prayer.getTimestamp() > currentTimestamp;
                    }
                }));
        List<Prayer> upcomingPrayers = allUpcomingPrayers.size() >= 6 ?
                allUpcomingPrayers.subList(0, 6) : allUpcomingPrayers;
        if (upcomingPrayers.isEmpty()) {
            showError();
            return;
        }
        if (mUpcomingPrayers == null || mUpcomingPrayers.size() != upcomingPrayers.size()) {
            mUpcomingPrayers = upcomingPrayers;
            displayPrayerSchedule();
        }
        if (mUpcomingPrayer == null || mUpcomingPrayer != upcomingPrayers.get(0)) {
            displayUpcomingLogo(upcomingPrayers.get(0), mUpcomingPrayer == null);
            mUpcomingPrayer = upcomingPrayers.get(0);
        }
        displayUpcomingPrayer();
    }

    private void displayUpcomingLogo(Prayer upcomingPrayer, boolean firstTime) {
        Log.d("@@@@", "displayUpcomingLogo, prayer: " + upcomingPrayer.getWhichPrayer());
        Log.d("@@@@", "displayUpcomingLogo, firstTime: " + firstTime);
    }

    @SuppressWarnings({"unchecked"})
    private void displayUpcomingPrayer() {
        final HashMap<String, String[]> prayerNames = Constants.PRAYER_NAMES;
        final String prayerTitle = prayerNames.get(mUpcomingPrayer.getWhichPrayer())[arabic ? 2 : 0];
        final long millisecondsUntilPrayer = mUpcomingPrayer.getTimestamp() - new Date().getTime();
        mHomeAppBarTimingTextView.setText(timeFormat.format(millisecondsUntilPrayer));
        mHomeAppBarTimingSubtitleTextView.setText(getString(R.string.up_next_subtitle, prayerTitle));
    }

    private void displayPrayerSchedule() {
        PrayersAdapter adapter = new PrayersAdapter(this, mUpcomingPrayers, arabic);
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

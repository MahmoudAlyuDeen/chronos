package com.afterapps.chronos.home;

import android.animation.Animator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afterapps.chronos.BaseActivity;
import com.afterapps.chronos.Constants;
import com.afterapps.chronos.R;
import com.afterapps.chronos.beans.Prayer;
import com.afterapps.chronos.location.LocationActivity;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.weather_icons_typeface_library.WeatherIcons;

import java.text.SimpleDateFormat;
import java.util.Calendar;
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
    @BindView(R.id.home_app_bar_logo_image_view)
    ImageView mHomeAppBarLogoImageView;
    @BindView(R.id.home_app_bar_mosque_image_view)
    ImageView mHomeAppBarMosqueImageView;

    private SharedPreferences mPref;

    private List<Prayer> mPrayerList;

    private Handler mTickerHandler;
    private Runnable mTickerRunnable;

    private SimpleDateFormat timeFormat;

    private List<Prayer> mUpcomingPrayers;
    private Prayer mUpcomingPrayer;
    private IconicsDrawable mUpcomingLogo;

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
        final int method = mPref.getInt(getString(R.string.preference_key_method), 5);
        final int school = mPref.getInt(getString(R.string.preference_key_school), 0);
        final int latitudeMethod = mPref.getInt(getString(R.string.preference_key_latitude), 3);
        presenter.getPrayers(method, school, latitudeMethod);
    }

    @Override
    protected void displayViewState() {
        Log.d("@@@@", "displayViewState: " + viewState);
    }

    @Override
    public void onPrayersReady(List<Prayer> prayersDetached) {
        mPrayerList = prayersDetached;
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
        if (allUpcomingPrayers.isEmpty()) {
            showError();
            return;
        }
        final Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        final long midnightTimestamp = calendar.getTimeInMillis();
        if (mUpcomingPrayer == null || mUpcomingPrayer != allUpcomingPrayers.get(0)
                || currentTimestamp == midnightTimestamp) {
            displayUpcomingLogo(mUpcomingPrayer == null, midnightTimestamp);
            mUpcomingPrayer = allUpcomingPrayers.get(0);
        }
        displayUpcomingPrayer();
        if (mUpcomingPrayers == null || mUpcomingPrayers.size() != allUpcomingPrayers.size()) {
            mUpcomingPrayers = allUpcomingPrayers;
            final List<Prayer> upcomingPrayersTillTomorrow = allUpcomingPrayers.size() >= 6 ?
                    allUpcomingPrayers.subList(0, 6) : allUpcomingPrayers;
            displayPrayerSchedule(upcomingPrayersTillTomorrow);
        }
    }

    private void displayUpcomingLogo(final boolean firstTime, final long midnightTimestamp) {
        IconicsDrawable upcomingLogo = getDayTimeLogo(midnightTimestamp);
        if (upcomingLogo == null) {
            return;
        }
        if (mUpcomingLogo != null && mUpcomingLogo.getIcon() == upcomingLogo.getIcon()) {
            return;
        }
        mUpcomingLogo = upcomingLogo;
        final int mosqueWidth = mHomeAppBarMosqueImageView.getWidth();
        final int mosqueHeight = mHomeAppBarMosqueImageView.getHeight();
        final int logoSize = mosqueWidth / 7;
        final int iconRightRightMargin = (int) (mosqueWidth / 3.5);

        if (mosqueHeight == 0) {
            mUpcomingLogo = null;
            mHomeAppBarMosqueImageView.getViewTreeObserver()
                    .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            displayUpcomingLogo(firstTime, midnightTimestamp);
                            mHomeAppBarLogoImageView.getViewTreeObserver()
                                    .removeOnGlobalLayoutListener(this);
                        }
                    });
            return;
        }

        final int displacement = mosqueHeight / 2;

        final RelativeLayout.LayoutParams layoutParams =
                new RelativeLayout.LayoutParams(logoSize, logoSize);
        layoutParams.setMargins(0, 0, iconRightRightMargin, 0);

        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);

        mHomeAppBarLogoImageView.setLayoutParams(layoutParams);

        if (firstTime) {
            mHomeAppBarLogoImageView.setImageDrawable(mUpcomingLogo);
            startRiseAnimation(displacement, mHomeAppBarLogoImageView);
        } else {
            startChainedSetRiseAnimation(displacement, mUpcomingLogo, mHomeAppBarLogoImageView);
        }
    }

    @SuppressWarnings({"unchecked"})
    private void displayUpcomingPrayer() {
        final HashMap<String, String[]> prayerNames = Constants.PRAYER_NAMES;
        final String prayerTitle = prayerNames.get(mUpcomingPrayer.getWhichPrayer())[arabic ? 2 : 0];
        final long millisecondsUntilPrayer = mUpcomingPrayer.getTimestamp() - new Date().getTime();
        mHomeAppBarTimingTextView.setText(timeFormat.format(millisecondsUntilPrayer));
        mHomeAppBarTimingSubtitleTextView.setText(getString(R.string.up_next_subtitle, prayerTitle));
    }

    private void displayPrayerSchedule(List<Prayer> upcomingPrayersTillTomorrow) {
        final PrayersAdapter adapter = new PrayersAdapter(this, upcomingPrayersTillTomorrow, arabic);
        mHomePrayersRecycler.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        final MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_location:
                final Intent location = new Intent(this, LocationActivity.class);
                startActivity(location);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Nullable
    private IconicsDrawable getDayTimeLogo(final long midnightTimestamp) {
        final long currentTimestamp = new Date().getTime();
        final long nextMidnightTimestamp = midnightTimestamp + 24 * 60 * 60 * 1000;
        final List<Prayer> todayPrayers =
                Lists.newArrayList(Iterables.filter(mPrayerList, new Predicate<Prayer>() {
                    @Override
                    public boolean apply(Prayer prayer) {
                        return prayer.getTimestamp() > midnightTimestamp &&
                                prayer.getTimestamp() < nextMidnightTimestamp;
                    }
                }));
        //todo: else executes!
        if (todayPrayers.size() < 6) {
            return null;
        } else {
            final long sunriseTimestamp = todayPrayers.get(1).getTimestamp();
            final long sunsetTimestamp = todayPrayers.get(4).getTimestamp();

            final IconicsDrawable dayTimeLogo = new IconicsDrawable(this).color(Color.WHITE);
            if (currentTimestamp < sunriseTimestamp) {
                dayTimeLogo.icon(WeatherIcons.Icon.wic_stars);
            } else if (currentTimestamp > sunsetTimestamp) {
                dayTimeLogo.icon(FontAwesome.Icon.faw_moon_o);
            } else {
                dayTimeLogo.icon(WeatherIcons.Icon.wic_day_sunny);
            }
            return dayTimeLogo;
        }
    }

    private void startChainedSetRiseAnimation(final int displacement,
                                              final IconicsDrawable newIcon,
                                              final ImageView animatingImageView) {
        animatingImageView.animate()
                .alpha(0)
                .translationYBy(displacement * 8)
                .setDuration(3000)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        animatingImageView.setImageDrawable(newIcon);
                        startRiseAnimation(displacement, animatingImageView);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
    }

    private void startRiseAnimation(final int displacement, final ImageView animatingImageView) {
        animatingImageView.setTranslationY(0);
        animatingImageView.setAlpha((float) 0);
        animatingImageView.animate()
                .alpha(1)
                .translationYBy(-displacement)
                .setDuration(1500)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        animation.cancel();
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
    }
}

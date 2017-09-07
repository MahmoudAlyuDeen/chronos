package com.afterapps.chronos.home;

import android.animation.Animator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afterapps.chronos.BaseActivity;
import com.afterapps.chronos.Constants;
import com.afterapps.chronos.R;
import com.afterapps.chronos.Utilities;
import com.afterapps.chronos.beans.Prayer;
import com.afterapps.chronos.job.PrayersJob;
import com.afterapps.chronos.location.LocationActivity;
import com.afterapps.chronos.preferences.SettingsActivity;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.mikepenz.iconics.IconicsDrawable;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import icepick.State;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.afterapps.chronos.Constants.FROM_ON_BOARDING;

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
    @BindView(R.id.home_empty_state_add_location_button)
    Button mHomeEmptyStateAddLocationButton;
    @BindView(R.id.home_empty_state_parent)
    NestedScrollView mHomeEmptyStateParent;
    @BindView(R.id.home_connection_error_notify_button)
    Button mHomeConnectionErrorNotifyButton;
    @BindView(R.id.home_connection_error_retry_button)
    Button mHomeConnectionErrorRetryButton;
    @BindView(R.id.home_connection_error_parent)
    NestedScrollView mHomeConnectionErrorParent;
    @BindView(R.id.home_logic_error_clear_button)
    Button mHomeLogicErrorClearButton;
    @BindView(R.id.home_logic_error_restart_button)
    Button mHomeLogicErrorRestartButton;
    @BindView(R.id.home_connection_error_prompt_parent)
    LinearLayout mHomeConnectionErrorPromptParent;
    @BindView(R.id.home_connection_error_notify_confirmation_text_view)
    TextView mHomeConnectionErrorNotifyConfirmationTextView;
    @BindView(R.id.home_logic_error_parent)
    NestedScrollView mHomeLogicErrorParent;

    private SharedPreferences mPref;

    private Handler mTickerHandler;
    private Runnable mTickerRunnable;

    private SimpleDateFormat timeFormat;

    private List<Prayer> mPrayerList;
    private List<Prayer> mUpcomingPrayers;

    private Prayer mUpcomingPrayer;
    private IconicsDrawable mUpcomingLogo;

    @State
    boolean mConnectionErrorNotifyConfirmed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        mPref = PreferenceManager.getDefaultSharedPreferences(this);
        timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.US);
        timeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        ButterKnife.bind(this);
        setSupportActionBar(mHomeToolbar);
        setTitle("");
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mPref != null) {
            mPref.registerOnSharedPreferenceChangeListener(this);
            onSharedPreferenceChanged(mPref, "");
        }
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopTicking();
        mUpcomingPrayer = null;
        mUpcomingPrayers = null;
        mPrayerList = null;
        mUpcomingLogo = null;
        if (mPref != null) {
            mPref.unregisterOnSharedPreferenceChangeListener(this);
        }
        mHomeAppBarLogoImageView.setAlpha(0f);
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(PrayersJob.PrayersFetchedEvent event) {
        //todo: hide notification
        onSharedPreferenceChanged(mPref, "");
    }

    @NonNull
    @Override
    public HomePresenter createPresenter() {
        return new HomePresenter();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (mPref == null) return;
        final String method = mPref.getString(getString(R.string.preference_key_method),
                getString(R.string.preference_default_method));
        final String school = mPref.getString(getString(R.string.preference_key_school),
                getString(R.string.preference_default_school));
        final String latitudeMethod = mPref.getString(getString(R.string.preference_key_latitude),
                getString(R.string.preference_default_latitude));
        presenter.getPrayers(method, school, latitudeMethod);
    }

    @Override
    protected void displayViewState() {
        mHomePrayersRecycler.setVisibility(GONE);
        mHomeAppBarTextParent.setVisibility(GONE);
        mHomeEmptyStateParent.setVisibility(GONE);
        mHomeConnectionErrorParent.setVisibility(GONE);
        mHomeConnectionErrorPromptParent.setVisibility(GONE);
        mHomeConnectionErrorNotifyConfirmationTextView.setVisibility(GONE);
        mHomeLogicErrorParent.setVisibility(GONE);
        switch (viewState) {
            case Constants.VIEW_STATE_PROGRESS:
                ProgressAdapter adapter = new ProgressAdapter();
                if (mHomePrayersRecycler.getAdapter() == null
                        || !(mHomePrayersRecycler.getAdapter() instanceof ProgressAdapter)) {
                    mHomePrayersRecycler.setAdapter(adapter);
                }
                mHomePrayersRecycler.setVisibility(VISIBLE);
                break;
            case Constants.VIEW_STATE_CONTENT:
                mHomePrayersRecycler.setVisibility(VISIBLE);
                mHomeAppBarTextParent.setVisibility(VISIBLE);
                break;
            case Constants.VIEW_STATE_EMPTY:
                mHomeEmptyStateParent.setVisibility(VISIBLE);
                break;
            case Constants.VIEW_STATE_ACTION:
                mHomeConnectionErrorParent.setVisibility(VISIBLE);
                if (mConnectionErrorNotifyConfirmed) {
                    mHomeConnectionErrorNotifyConfirmationTextView.setVisibility(VISIBLE);
                } else {
                    mHomeConnectionErrorPromptParent.setVisibility(VISIBLE);
                }
                break;
            case Constants.VIEW_STATE_ERROR:
                mHomeLogicErrorParent.setVisibility(VISIBLE);
                break;
        }
    }

    @Override
    public void onPrayersReady(List<Prayer> prayersDetached) {
        mPrayerList = prayersDetached;
        Utilities.updateHomeScreenWidget(this);
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
        final Calendar calendar = Calendar.getInstance();
        final long currentTimestamp = calendar.getTimeInMillis();
        final List<Prayer> allUpcomingPrayers =
                Lists.newArrayList(Iterables.filter(mPrayerList, new Predicate<Prayer>() {
                    @Override
                    public boolean apply(Prayer prayer) {
                        return prayer.getTimestamp() > currentTimestamp;
                    }
                }));
        if (allUpcomingPrayers.size() < 6) {
            onSharedPreferenceChanged(mPref, "");
            return;
        }
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
        IconicsDrawable upcomingLogo = Utilities.getDayTimeLogo(mPrayerList, midnightTimestamp, this);
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
        final int iconEndMargin = (int) (mosqueWidth / 3.5);

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
        layoutParams.setMargins(arabic ? iconEndMargin : 0,
                0,
                arabic ? 0 : iconEndMargin,
                0);

        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_END, RelativeLayout.TRUE);
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
            case R.id.action_settings:
                final Intent settings = new Intent(this, SettingsActivity.class);
                startActivity(settings);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @OnClick({R.id.home_empty_state_add_location_button,
            R.id.home_connection_error_notify_button,
            R.id.home_connection_error_retry_button,
            R.id.home_logic_error_clear_button,
            R.id.home_logic_error_restart_button})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.home_empty_state_add_location_button:
                final Intent location = new Intent(this, LocationActivity.class);
                location.putExtra(FROM_ON_BOARDING, true);
                startActivity(location);
                break;
            case R.id.home_connection_error_notify_button:
                PrayersJob.schedulePrayersJob();
                mConnectionErrorNotifyConfirmed = true;
                displayViewState();
                break;
            case R.id.home_connection_error_retry_button:
                onSharedPreferenceChanged(mPref, "");
                break;
            case R.id.home_logic_error_clear_button:
                //todo
                break;
            case R.id.home_logic_error_restart_button:
                //todo
                break;
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

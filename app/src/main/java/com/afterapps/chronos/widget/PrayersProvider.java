package com.afterapps.chronos.widget;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.afterapps.chronos.Constants;
import com.afterapps.chronos.R;
import com.afterapps.chronos.beans.Location;
import com.afterapps.chronos.beans.Prayer;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.weather_icons_typeface_library.WeatherIcons;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import io.realm.Realm;
import io.realm.Sort;

/*
 * Created by mahmoudalyudeen on 4/25/17.
 */

class PrayersProvider implements RemoteViewsService.RemoteViewsFactory {

    private Context mContext;
    private List<Prayer> mPrayerList;
    private final boolean mArabic;
    private final SimpleDateFormat timeFormat;

    @SuppressWarnings({"unused"})
    PrayersProvider(PrayersWidgetService stocksWidgetService, Intent intent) {
        mContext = stocksWidgetService;
        mArabic = mContext.getResources().getBoolean(R.bool.arabic);
        timeFormat = new SimpleDateFormat("hh:mm a", new Locale(mArabic ? "ar" : "en"));
        loadTodayPrayers();
    }

    @Override
    public void onCreate() {
    }

    private void loadTodayPrayers() {
        mPrayerList = new ArrayList<>(0);
        final Realm realm = Realm.getDefaultInstance();
        final SharedPreferences mPref = PreferenceManager.getDefaultSharedPreferences(mContext);
        final int method = mPref.getInt(mContext.getString(R.string.preference_key_method), 5);
        final int school = mPref.getInt(mContext.getString(R.string.preference_key_school), 0);
        final int latitudeMethod = mPref.getInt(mContext.getString(R.string.preference_key_latitude), 3);
        final Location location = realm.where(Location.class)
                .equalTo("selected", true)
                .findFirst();
        if (location == null) {
            realm.close();
            return;
        }
        final String signature = location.getTimezoneId() + method + school + latitudeMethod;
        List<Prayer> allPrayers = realm.copyFromRealm(realm.where(Prayer.class)
                .equalTo("signature", signature)
                .findAllSorted("timestamp", Sort.ASCENDING));
        realm.close();
        final Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        final long midnightTimestamp = calendar.getTimeInMillis();
        final long nextMidnightTimestamp = midnightTimestamp + 24 * 60 * 60 * 1000;
        mPrayerList = allPrayers;
        if (!allPrayers.isEmpty()) {
            mPrayerList = Lists.newArrayList(Iterables.filter(mPrayerList, new Predicate<Prayer>() {
                @Override
                public boolean apply(Prayer prayer) {
                    return prayer.getTimestamp() > midnightTimestamp &&
                            prayer.getTimestamp() < nextMidnightTimestamp;
                }
            }));
        }
    }

    @Override
    public void onDataSetChanged() {
        loadTodayPrayers();
    }

    @Override
    public void onDestroy() {
    }

    @Override
    public int getCount() {
        return mPrayerList.size();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        RemoteViews itemRemoteView = new RemoteViews(mContext.getPackageName(),
                R.layout.item_prayer);
        Prayer prayer = mPrayerList.get(position);
        if (prayer != null) {
            setPrayer(prayer, itemRemoteView);
        }
        return itemRemoteView;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @SuppressWarnings({"unchecked"})
    private void setPrayer(Prayer prayer, RemoteViews itemRemoteView) {
        final HashMap<String, String[]> prayerNames = Constants.PRAYER_NAMES;
        final String prayerTitle = prayerNames.get(prayer.getWhichPrayer())[mArabic ? 2 : 0];
        final String prayerSubtitle = prayerNames.get(prayer.getWhichPrayer())[mArabic ? 3 : 1];
        final String formattedTimestamp = timeFormat.format(prayer.getTimestamp());
        final IconicsDrawable prayerIcon = getPrayerIcon(prayer);

        itemRemoteView.setTextViewText(R.id.item_prayer_title_text_view, prayerTitle);
        itemRemoteView.setTextViewText(R.id.item_prayer_subtitle_text_view, prayerSubtitle);
        itemRemoteView.setTextViewText(R.id.item_prayer_timing_text_view, formattedTimestamp);
        itemRemoteView.setImageViewBitmap(R.id.item_prayer_logo_image_view, prayerIcon.toBitmap());
    }

    @NonNull
    private IconicsDrawable getPrayerIcon(Prayer prayer) {
        final IconicsDrawable prayerIcon =
                new IconicsDrawable(mContext)
                        .color(ContextCompat.getColor(mContext, R.color.colorTextSecondary));
        switch (prayer.getWhichPrayer()) {
            case "fajr":
                prayerIcon.icon(WeatherIcons.Icon.wic_stars);
                break;
            case "sunrise":
                prayerIcon.icon(WeatherIcons.Icon.wic_sunrise);
                break;
            case "dhuhr":
                prayerIcon.icon(WeatherIcons.Icon.wic_day_sunny);
                break;
            case "asr":
                prayerIcon.icon(WeatherIcons.Icon.wic_day_cloudy_high);
                break;
            case "maghrib":
                prayerIcon.icon(WeatherIcons.Icon.wic_sunset);
                break;
            case "isha":
                prayerIcon.icon(FontAwesome.Icon.faw_moon_o);
                break;
        }
        return prayerIcon;
    }
}
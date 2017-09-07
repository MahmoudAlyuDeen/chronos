package com.afterapps.chronos;

/*
 * Created by mahmoud on 9/7/17.
 */

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;

import com.afterapps.chronos.beans.Prayer;
import com.afterapps.chronos.widget.PrayersWidget;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.weather_icons_typeface_library.WeatherIcons;

import java.util.Date;
import java.util.List;

public class Utilities {
    private Utilities() {
    }

    public static void updateHomeScreenWidget(Context context) {
        Intent invalidateWidget = new Intent(context, PrayersWidget.class);
        invalidateWidget.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        context.sendBroadcast(invalidateWidget);
    }
    
    @Nullable
    public static IconicsDrawable getDayTimeLogo(final List<Prayer> prayerList,
                                                 final long midnightTimestamp,
                                                 final Context context) {
        final long currentTimestamp = new Date().getTime();
        final long nextMidnightTimestamp = midnightTimestamp + 24 * 60 * 60 * 1000;
        final List<Prayer> todayPrayers =
                Lists.newArrayList(Iterables.filter(prayerList, new Predicate<Prayer>() {
                    @Override
                    public boolean apply(Prayer prayer) {
                        return prayer.getTimestamp() > midnightTimestamp &&
                                prayer.getTimestamp() < nextMidnightTimestamp;
                    }
                }));
        if (todayPrayers.size() < 6) {
            return null;
        } else {
            final long sunriseTimestamp = todayPrayers.get(1).getTimestamp();
            final long sunsetTimestamp = todayPrayers.get(4).getTimestamp();

            final IconicsDrawable dayTimeLogo = new IconicsDrawable(context).color(Color.WHITE);
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

    @NonNull
    public static IconicsDrawable getPrayerIcon(Prayer prayer, Context context) {
        final IconicsDrawable prayerIcon =
                new IconicsDrawable(context)
                        .color(ContextCompat.getColor(context, R.color.colorTextSecondary));
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
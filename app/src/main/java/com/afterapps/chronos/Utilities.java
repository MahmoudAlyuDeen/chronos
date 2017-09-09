package com.afterapps.chronos;

/*
 * Created by mahmoud on 9/7/17.
 */

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;

import com.afterapps.chronos.beans.Prayer;
import com.afterapps.chronos.widget.PrayersWidget;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.weather_icons_typeface_library.WeatherIcons;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.afterapps.chronos.Constants.DISPLAY_THRESHOLD;
import static com.afterapps.chronos.Constants.WILL_NOTIFY_FLAG;

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
        if (todayPrayers.size() < DISPLAY_THRESHOLD) {
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
    public static IconicsDrawable getPrayerIcon(final Prayer prayer,
                                                final Context context,
                                                final int colorResId) {
        final IconicsDrawable prayerIcon =
                new IconicsDrawable(context)
                        .color(ContextCompat.getColor(context, colorResId));
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


    @NonNull
    public static ArrayList<Prayer> getPrayersForDay(final List<Prayer> prayerList,
                                                     final long midnightTimestamp,
                                                     final long nextMidnightTimestamp)
            throws IllegalStateException {
        try {
            return prayerList == null ? new ArrayList<Prayer>() : Lists.newArrayList(Iterables.filter(prayerList, new Predicate<Prayer>() {
                @Override
                public boolean apply(Prayer prayer) {
                    return prayer.getTimestamp() > midnightTimestamp &&
                            prayer.getTimestamp() < nextMidnightTimestamp;
                }
            }));
        } catch (Exception exception) {
            throw new IllegalStateException();
        }
    }

    @NonNull
    public static ArrayList<Prayer> getUpcomingPrayers(final List<Prayer> prayerList,
                                                       final long currentTimestamp)
            throws IllegalStateException {
        try {
            return prayerList == null ? new ArrayList<Prayer>() : Lists.newArrayList(Iterables.filter(prayerList, new Predicate<Prayer>() {
                @Override
                public boolean apply(Prayer prayer) {
                    return prayer.getTimestamp() > currentTimestamp;
                }
            }));
        } catch (Exception exception) {
            throw new IllegalStateException();
        }
    }

    public static void startChainedSetRiseAnimation(final int displacement,
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

    public static void startRiseAnimation(final int displacement, final ImageView animatingImageView) {
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


    @SuppressLint("ApplySharedPref")
    public static void setWillNotify(final Context context, final boolean willNotify) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(WILL_NOTIFY_FLAG, willNotify)
                .commit();
    }

    public static boolean getWillNotify(final Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(WILL_NOTIFY_FLAG, false);
    }
}
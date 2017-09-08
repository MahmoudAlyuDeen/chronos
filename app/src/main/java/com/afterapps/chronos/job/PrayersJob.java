package com.afterapps.chronos.job;

/*
 * Created by mahmoud on 9/6/17.
 */

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;

import com.afterapps.chronos.R;
import com.afterapps.chronos.api.Responses.TimingsResponse;
import com.afterapps.chronos.beans.Location;
import com.afterapps.chronos.beans.Prayer;
import com.afterapps.chronos.home.HomeActivity;
import com.afterapps.chronos.home.PrayerModel;
import com.evernote.android.job.Job;
import com.evernote.android.job.JobRequest;

import org.greenrobot.eventbus.EventBus;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import io.realm.Realm;
import retrofit2.Call;
import retrofit2.Response;

import static android.support.v4.app.NotificationCompat.PRIORITY_HIGH;
import static com.afterapps.chronos.Constants.NOTIFICATION_TAG_PRAYERS_READY;
import static com.afterapps.chronos.Utilities.updateHomeScreenWidget;
import static com.afterapps.chronos.home.PrayerModel.isResponseValid;

public class PrayersJob extends Job {

    public static final String TAG = "prayersJobTag";

    public static void schedulePrayersJob() {
        new JobRequest.Builder(TAG)
                .setExecutionWindow(5L, 1000L)
                .setBackoffCriteria(5_000L, JobRequest.BackoffPolicy.LINEAR)
                .setRequiredNetworkType(JobRequest.NetworkType.CONNECTED)
                .setRequirementsEnforced(true)
                .setUpdateCurrent(true)
                .setPersisted(true)
                .build()
                .schedule();
    }

    @Override
    @NonNull
    protected Result onRunJob(Params params) {
        final Realm realm = Realm.getDefaultInstance();
        final SharedPreferences mPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        final String method = mPref.getString(getContext().getString(R.string.preference_key_method),
                getContext().getString(R.string.preference_default_method));
        final String school = mPref.getString(getContext().getString(R.string.preference_key_school),
                getContext().getString(R.string.preference_default_school));
        final String latitudeMethod = mPref.getString(getContext().getString(R.string.preference_key_latitude),
                getContext().getString(R.string.preference_default_latitude));
        final Location location = realm.where(Location.class)
                .equalTo("selected", true)
                .findFirst();
        if (location == null) {
            realm.close();
            return Result.RESCHEDULE;
        }
        final String signature = location.getTimezoneId() + method + school + latitudeMethod;
        final Location locationDetached = realm.copyFromRealm(location);
        realm.close();
        final Result result = loadPrayers(signature) ? Result.SUCCESS :
                fetchPrayers(method, school, latitudeMethod, locationDetached) ?
                        Result.SUCCESS : Result.RESCHEDULE;
        if (result == Result.SUCCESS) {
            handleSuccess();
        }
        return result;
    }

    private void handleSuccess() {
        updateHomeScreenWidget(getContext());
        EventBus.getDefault().post(new PrayersFetchedEvent(showPrayersReadyNotification()));
    }

    private int showPrayersReadyNotification() {
        final Intent home = new Intent(getContext(), HomeActivity.class);
        home.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        home.setAction(Long.toString(new Date().getTime()));
        final Uri notificationTone = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        final PendingIntent homePendingIntent =
                PendingIntent.getActivity(getContext(), 0, home, PendingIntent.FLAG_CANCEL_CURRENT);

        final NotificationCompat.Builder messageNotificationBuilder =
                new NotificationCompat.Builder(getContext())
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle(getContext().getString(R.string.notification_title_prayers_ready))
                        .setContentText(getContext().getString(R.string.notification_body_prayers_ready))
                        .setPriority(PRIORITY_HIGH)
                        .setSound(notificationTone)
                        .setVibrate(new long[]{500})
                        .setAutoCancel(true)
                        .setContentIntent(homePendingIntent);

        final NotificationManager mNotificationManager =
                (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        final int notificationId = (int) new Date().getTime();
        mNotificationManager.notify(NOTIFICATION_TAG_PRAYERS_READY,
                notificationId,
                messageNotificationBuilder.build());
        return notificationId;
    }

    private boolean loadPrayers(String signature) {
        return PrayerModel.getStoredPrayers(signature).size() >= 6;
    }

    private boolean fetchPrayers(final String method,
                                 final String school,
                                 final String latitudeMethod,
                                 final Location locationDetached) {
        final Call<TimingsResponse> currentMonthTimingsCall = PrayerModel.getTimingsCall(method,
                school,
                latitudeMethod,
                locationDetached,
                Calendar.getInstance(),
                false);
        final Call<TimingsResponse> nextMonthTimingsCall = PrayerModel.getTimingsCall(method,
                school,
                latitudeMethod,
                locationDetached,
                Calendar.getInstance(),
                true);
        try {
            final Response<TimingsResponse> currentMonthResponse = currentMonthTimingsCall.execute();
            final Response<TimingsResponse> nextMonthResponse = nextMonthTimingsCall.execute();
            return !(!isResponseValid(currentMonthResponse) || !isResponseValid(nextMonthResponse)) &&
                    storePrayers(currentMonthResponse.body(),
                            nextMonthResponse.body(),
                            method,
                            school,
                            latitudeMethod,
                            locationDetached);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean storePrayers(
            final TimingsResponse currentMonthResponse,
            final TimingsResponse nextMonthResponse,
            final String method,
            final String school,
            final String latitudeMethod,
            final Location locationDetached) throws IllegalAccessException {
        final List<Prayer> currentMonthPrayers = currentMonthResponse.getPrayers(method,
                school,
                latitudeMethod,
                locationDetached);
        final List<Prayer> nextMonthPrayers = nextMonthResponse.getPrayers(method,
                school,
                latitudeMethod,
                locationDetached);
        final Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.copyToRealmOrUpdate(currentMonthPrayers);
                realm.copyToRealmOrUpdate(nextMonthPrayers);

            }
        });
        realm.close();
        return true;
    }

    public class PrayersFetchedEvent {
        private final int notificationId;

        public int getNotificationId() {
            return notificationId;
        }

        PrayersFetchedEvent(int notificationId) {
            this.notificationId = notificationId;
        }
    }
}
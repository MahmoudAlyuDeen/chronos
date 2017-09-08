package com.afterapps.chronos;

/*
 * Created by mahmoud on 1/9/17.
 */


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.preference.PreferenceManager;

import com.afterapps.chronos.home.HomeActivity;
import com.afterapps.chronos.job.TimingsJobCreator;
import com.evernote.android.job.JobManager;

import io.realm.Realm;
import io.realm.RealmConfiguration;

import static android.app.AlarmManager.RTC;
import static com.afterapps.chronos.Constants.APPLICATION_CRASHED_FLAG;

public class Application extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this);
        final RealmConfiguration config = new RealmConfiguration
                .Builder()
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.setDefaultConfiguration(config);

        JobManager.create(this).addJobCreator(new TimingsJobCreator());

        PreferenceManager.setDefaultValues(this, R.xml.settings, false);

        //todo: enable un-comment out this before release
//        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
//            @Override
//            public void uncaughtException(Thread thread, Throwable throwable) {
//                throwable.printStackTrace();
//                restartApplication(true);
//            }
//        });
    }

    public void clearDatabaseAndRestart() {
        final Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.deleteAll();
            }
        });
        realm.close();
        restartApplication(false);
    }

    public void restartApplication(final boolean isUncaughtException) {
        final Intent home = new Intent(getApplicationContext(), HomeActivity.class);
        if (isUncaughtException) {
            home.putExtra(APPLICATION_CRASHED_FLAG, true);
        }
        final PendingIntent homePending = PendingIntent.getActivity(getBaseContext(), 0, home, 0);
        AlarmManager mgr = (AlarmManager) getSystemService(ALARM_SERVICE);
        mgr.set(RTC, System.currentTimeMillis() + 100, homePending);
        System.exit(2);
    }
}

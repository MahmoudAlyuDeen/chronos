package com.afterapps.chronos.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.afterapps.chronos.R;


/*
 * Created by mahmoudalyudeen on 4/25/17.
 */

public class PrayersWidget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
//        scheduleUpdate();
        for (int appWidgetId : appWidgetIds) {
            updatePrayersWidget(context, appWidgetManager, appWidgetId);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

//    private void scheduleUpdate() {
//        new JobRequest.Builder(PrayersJob.TAG)
//                .setExecutionWindow(1, 1000)
//                .setPersisted(true)
//                .setRequiredNetworkType(JobRequest.NetworkType.CONNECTED)
//                .setBackoffCriteria(5000, JobRequest.BackoffPolicy.LINEAR)
//                .setUpdateCurrent(true)
//                .build()
//                .schedule();
//    }

    private void updatePrayersWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_prayers);
        views.setRemoteAdapter(R.id.widget_prayers_list_view, new Intent(context, PrayersWidgetService.class));
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }
}
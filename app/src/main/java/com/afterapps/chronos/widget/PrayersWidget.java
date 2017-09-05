package com.afterapps.chronos.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.afterapps.chronos.R;
import com.afterapps.chronos.home.HomeActivity;


/*
 * Created by mahmoudalyudeen on 4/25/17.
 */

public class PrayersWidget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        for (int appWidgetId : appWidgetIds) {
            updatePrayersWidget(context, appWidgetManager, appWidgetId);
        }
    }

    private void updatePrayersWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_prayers);
        views.setRemoteAdapter(R.id.widget_prayers_list_view, new Intent(context, PrayersWidgetService.class));
        views.setEmptyView(R.id.widget_prayers_list_view, R.id.widget_prayers_empty_text_view);
        views.setOnClickPendingIntent(R.id.widget_prayers_empty_text_view, PendingIntent.getActivity(context, 0, new Intent(context, HomeActivity.class), 0));
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (intent.getAction().equals(AppWidgetManager.ACTION_APPWIDGET_UPDATE)) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            ComponentName thisAppWidget = new ComponentName(context.getPackageName(), PrayersWidget.class.getName());
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_prayers_list_view);
        }
    }
}
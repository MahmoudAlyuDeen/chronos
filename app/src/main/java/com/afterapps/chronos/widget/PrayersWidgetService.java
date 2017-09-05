package com.afterapps.chronos.widget;
/*
 * Created by mahmoudalyudeen on 4/25/17.
 */

import android.content.Intent;
import android.widget.RemoteViewsService;

public class PrayersWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new PrayersProvider(this, intent);
    }
}
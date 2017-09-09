package com.afterapps.chronos;

/*
 * Created by mahmoud on 1/12/17.
 */

import java.util.HashMap;

@SuppressWarnings({"unused", "WeakerAccess"})
public class Constants {

    private Constants() {
    }

    public static final long LOCATION_REQUEST_TIMEOUT = 100000;
    
    public static final String WILL_NOTIFY_FLAG = "willNotifyFlag";

    public static final String APPLICATION_CRASHED_FLAG = "applicationCrashedFlag";

    public static final String PLAY_SERVICES_LINK = "https://play.google.com/store/apps/details?id=com.google.android.gms";

    public static final String FROM_ON_BOARDING = "locationOpenedFromEmptyState";

    public static final int FETCH_THRESHOLD = 12;
    public static final int DISPLAY_THRESHOLD = 6;
    public static final int PREFETCH_THRESHOLD = 60;
    public static final String NOTIFICATION_TAG_PRAYERS_READY = "notificationTagPrayersReady";

    public static String GOOGLE_REVERSE_GEO_LOC_API_KEY = "AIzaSyCUSRQAQCa5CmKPWe_VWdlArClGjQTXAZE";

    public static final String REVERSE_GEO_LOC_API_BASE_UEL = "https://maps.googleapis.com";
    public static final String PRAYER_TIMINGS_API_BASE_URL = "http://api.aladhan.com";

    public static final int VIEW_STATE_IDLE = 0;
    public static final int VIEW_STATE_PROGRESS = 1;
    public static final int VIEW_STATE_CONTENT = 2;
    public static final int VIEW_STATE_ERROR = 3;
    public static final int VIEW_STATE_ACTION = 4;
    public static final int VIEW_STATE_EMPTY = 5;

    public static final HashMap PRAYER_NAMES = new HashMap<String, String[]>() {{
        put("fajr", new String[]{"Fajr", "The dawn prayer", "الفجر", ""});
        put("sunrise", new String[]{"Shurouq", "Sunrise", "الشروق", ""});
        put("dhuhr", new String[]{"Dhuhr", "The noon prayer", "الظهر", ""});
        put("asr", new String[]{"Asr", "The afternoon prayer", "العصر", ""});
        put("maghrib", new String[]{"Maghrib", "The sunset prayer", "المغرب", ""});
        put("isha", new String[]{"Isha'a", "The night prayer", "العشاء", ""});
    }};
}

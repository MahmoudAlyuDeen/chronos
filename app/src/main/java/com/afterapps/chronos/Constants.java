package com.afterapps.chronos;

/*
 * Created by mahmoud on 1/12/17.
 */

@SuppressWarnings({"unused", "WeakerAccess"})
public class Constants {

    public static final String PLAY_SERVICES_LINK = "https://play.google.com/store/apps/details?id=com.google.android.gms";

    private Constants() {
    }

    public static String GOOGLE_REVERSE_GEO_LOC_API_KEY = "AIzaSyCUSRQAQCa5CmKPWe_VWdlArClGjQTXAZE";

    public static final String REVERSE_GEO_LOC_API_BASE_UEL = "https://maps.googleapis.com";
    public static final String PRAYER_TIMINGS_API_BASE_URL = "http://api.aladhan.com//";

    public static final int VIEW_STATE_IDLE = 0;
    public static final int VIEW_STATE_PROGRESS = 1;
    public static final int VIEW_STATE_CONTENT = 2;
    public static final int VIEW_STATE_ERROR = 3;
    public static final int VIEW_STATE_ACTION = 4;
    public static final int VIEW_STATE_EMPTY = 5;
}

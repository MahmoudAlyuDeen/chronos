package com.afterapps.chronos.api.Responses;

import com.afterapps.chronos.beans.Location;
import com.afterapps.chronos.beans.Prayer;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

//Generated("org.jsonschema2pojo")
@SuppressWarnings({"unused"})
public class TimingsResponse {

    @SerializedName("code")
    @Expose
    private Integer code;
    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("data")
    @Expose
    private List<Day> days;

    public Integer getCode() {
        return code;
    }

    public String getStatus() {
        return status;
    }

    List<Day> getDays() {
        return days;
    }

    public List<Prayer> getPrayers(String method, String school, String latitudeMethod, Location locationDetached)
            throws IllegalAccessException {
        List<Prayer> prayerList = new ArrayList<>(0);
        for (Day day : days) {
            final List<Prayer> timestampedPrayers = day.getTimings().getTimestampedPrayers(day.getDate().getTimestamp(),
                    method,
                    school,
                    latitudeMethod,
                    locationDetached);
            prayerList.addAll(timestampedPrayers);
        }
        return prayerList;
    }

    private class Day {

        @SerializedName("timings")
        @Expose
        private Timings timings;
        @SerializedName("date")
        @Expose
        private Date date;

        Timings getTimings() {
            return timings;
        }

        Date getDate() {
            return date;
        }

        private class Date {

            @SerializedName("readable")
            @Expose
            private String readable;
            @SerializedName("timestamp")
            @Expose
            private String timestamp;

            String getReadable() {
                return readable;
            }

            long getTimestamp() {
                return Long.valueOf(timestamp);
            }
        }

        private class Timings {

            @SerializedName("Fajr")
            @Expose
            private String fajr;
            @SerializedName("Sunrise")
            @Expose
            private String sunrise;
            @SerializedName("Dhuhr")
            @Expose
            private String dhuhr;
            @SerializedName("Asr")
            @Expose
            private String asr;
            @SerializedName("Maghrib")
            @Expose
            private String maghrib;
            @SerializedName("Isha")
            @Expose
            private String isha;

            public String getFajr() {
                return fajr;
            }

            public String getSunrise() {
                return sunrise;
            }

            public String getDhuhr() {
                return dhuhr;
            }

            public String getAsr() {
                return asr;
            }

            public String getMaghrib() {
                return maghrib;
            }

            public String getIsha() {
                return isha;
            }

            List<Prayer> getTimestampedPrayers(final long dayTimestamp,
                                               final String method,
                                               final String school,
                                               final String latitudeMethod,
                                               final Location locationDetached)
                    throws IllegalAccessException {
                final List<Prayer> prayerList = new ArrayList<>(0);
                final Field[] fields = Timings.class.getDeclaredFields();
                for (Field field : fields) {
                    String timing = (String) field.get(this);
                    if (timing == null) continue;
                    final String whichPrayer = field.getName();
                    final Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(dayTimestamp * 1000);
                    final int currentMonth = calendar.get(Calendar.MONTH);
                    final int currentYear = calendar.get(Calendar.YEAR);
                    final int currentDay = calendar.get(Calendar.DAY_OF_MONTH);
                    calendar.set(currentYear, currentMonth, currentDay,
                            Integer.valueOf(timing.substring(0, 2)),
                            Integer.valueOf(timing.substring(3, 5)));
                    final long timeInMillis = calendar.getTimeInMillis();
                    prayerList.add(
                            new Prayer(whichPrayer,
                                    timeInMillis,
                                    method,
                                    school,
                                    latitudeMethod,
                                    locationDetached.getTimezoneId()));
                }
                return prayerList;
            }
        }
    }
}
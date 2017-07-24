package com.afterapps.chronos.beans;

/*
 * Created by mahmoudalyudeen on 7/23/17.
 */

import com.afterapps.chronos.api.Responses.ReverseGeoLocResponse;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

@SuppressWarnings({"unused"})
public class Location extends RealmObject {

    @PrimaryKey
    private String timezoneId;
    private String displayName;
    private double longitude;
    private double latitude;
    private boolean selected;

    public Location() {
    }

    public Location(android.location.Location geoLocation, ReverseGeoLocResponse reverseGeoLocResponse) {
        final String timeZoneId = reverseGeoLocResponse.getTimeZoneId();
        timezoneId = reverseGeoLocResponse.getTimeZoneId();
        displayName = timeZoneId.substring(timeZoneId.indexOf("/") + 1);
        longitude = geoLocation.getLongitude();
        latitude = geoLocation.getLatitude();
        selected = true;
    }

    public String getTimezoneId() {
        return timezoneId;
    }

    public void setTimezoneId(String timezoneId) {
        this.timezoneId = timezoneId;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}

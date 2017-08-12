package com.afterapps.chronos.api.Responses;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

//Generated("org.jsonschema2pojo")
@SuppressWarnings({"unused"})
public class ReverseGeoLocResponse {

    @SerializedName("dstOffset")
    @Expose
    private Integer dstOffset;
    @SerializedName("rawOffset")
    @Expose
    private Integer rawOffset;
    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("timeZoneId")
    @Expose
    private String timeZoneId;
    @SerializedName("timeZoneName")
    @Expose
    private String timeZoneName;

    public Integer getDstOffset() {
        return dstOffset;
    }

    public Integer getRawOffset() {
        return rawOffset;
    }

    public String getStatus() {
        return status;
    }

    public String getTimeZoneId() {
        return timeZoneId;
    }

    public String getTimeZoneName() {
        return timeZoneName;
    }
}

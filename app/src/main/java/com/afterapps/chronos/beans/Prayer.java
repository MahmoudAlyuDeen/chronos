package com.afterapps.chronos.beans;

/*
 * Created by mahmoud on 8/12/17.
 */
@SuppressWarnings({"unused", "FieldCanBeLocal"})
public class Prayer
//        extends RealmObject
{

//    @PrimaryKey
    private String primaryKey;
    private long timestamp;
    private String whichPrayer;
    private String signature;

    public Prayer() {
    }

    public Prayer(final String whichPrayer,
                  final long timestamp,
                  final String method,
                  final String school,
                  final String latitudeMethod,
                  final String timezoneId) {
        this.signature = timezoneId + method + school + latitudeMethod;
        this.whichPrayer = whichPrayer;
        this.timestamp = timestamp;
        this.primaryKey = signature + whichPrayer + timestamp;
    }

    public String getPrimaryKey() {
        return primaryKey;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getWhichPrayer() {
        return whichPrayer;
    }

    public void setWhichPrayer(String whichPrayer) {
        this.whichPrayer = whichPrayer;
    }

    public String getSignature() {
        return signature;
    }
}

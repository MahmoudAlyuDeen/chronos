package com.afterapps.chronos.beans;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/*
 * Created by mahmoud on 8/12/17.
 */
@SuppressWarnings({"unused"})
public class Prayer extends RealmObject {

    @PrimaryKey
    private long timestamp;
    private String whichPrayer;
    private String signature;

    public Prayer() {
    }

    public Prayer(String whichPrayer,
                  long timestamp,
                  int method,
                  int school,
                  int latitudeMethod, Location locationDetached) {
        String timeZoneId = locationDetached.getTimezoneId();
        this.signature = timeZoneId + method + school + latitudeMethod;
        this.whichPrayer = whichPrayer;
        this.timestamp = timestamp;
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

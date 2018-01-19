package com.cinggl.cinggl.models;

/**
 * Created by J.EL on 1/9/2018.
 */

public class Timeline {
    String type;
    String uid;
    String pushId;
    long timeStamp;

    public Timeline() {

    }

    public Timeline(String type, String uid, String pushId, long timeStamp) {
        this.type = type;
        this.uid = uid;
        this.pushId = pushId;
        this.timeStamp = timeStamp;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getPushId() {
        return pushId;
    }

    public void setPushId(String pushId) {
        this.pushId = pushId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}

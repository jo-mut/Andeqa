package com.cinggl.cinggl.models;

/**
 * Created by J.EL on 1/4/2018.
 */

public class Message {
    String uid;
    String message;
    String pushId;
    long timeStamp;

    public Message() {
    }

    public Message(String message, String pushId, long timeStamp, String uid) {
        this.message = message;
        this.pushId = pushId;
        this.timeStamp = timeStamp;
        this.uid = uid;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPushId() {
        return pushId;
    }

    public void setPushId(String pushId) {
        this.pushId = pushId;
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
}

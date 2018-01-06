package com.cinggl.cinggl.models;

/**
 * Created by J.EL on 1/4/2018.
 */

public class MessagingUser {
    String uid;
    String message;
    long time;
    String pushId;

    public MessagingUser() {

    }

    public MessagingUser(String message, String pushId, long time, String uid) {
        this.message = message;
        this.pushId = pushId;
        this.time = time;
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

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}

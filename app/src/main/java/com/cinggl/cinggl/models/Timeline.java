package com.cinggl.cinggl.models;

/**
 * Created by J.EL on 1/9/2018.
 */

public class Timeline {
    String type;
    String uid;
    String pushId;

    public Timeline(String type, String uid, String pushId) {
        this.type = type;
        this.uid = uid;
        this.pushId = pushId;
    }

    public Timeline() {
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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
}

package com.cinggl.cinggl.models;

import org.parceler.Parcel;

/**
 * Created by J.EL on 8/16/2017.
 */

@Parcel
public class Ifair {
    String pushId;
    String uid;

    public Ifair() {
    }

    public Ifair(String pushId, String uid) {
        this.pushId = pushId;
        this.uid = uid;
    }

    public String getPushId() {
        return pushId;
    }

    public void setPushId(String pushId) {
        this.pushId = pushId;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}

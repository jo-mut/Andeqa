package com.cinggl.cinggl.models;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by J.EL on 6/24/2017.
 */

public class Like {
    String uid;
    String pushId;

    public Like(String uid, String pushId) {
        this.uid = uid;
        this.pushId = pushId;
    }

    public Like() {
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


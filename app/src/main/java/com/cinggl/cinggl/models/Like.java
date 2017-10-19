package com.cinggl.cinggl.models;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by J.EL on 6/24/2017.
 */

public class Like {
    String uid;
    String dateLiked;
    long timeStamp;
    String postKey;
    String pushId;


    public Like() {

    }

    public Like(String uid, String dateLiked, long timeStamp,
                String postKey, String pushId) {
        this.uid = uid;
        this.dateLiked = dateLiked;
        this.timeStamp = timeStamp;
        this.postKey = postKey;
        this.pushId = pushId;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getDateLiked() {
        return dateLiked;
    }

    public void setDateLiked(String dateLiked) {
        this.dateLiked = dateLiked;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getPostKey() {
        return postKey;
    }

    public void setPostKey(String postKey) {
        this.postKey = postKey;
    }

    public String getPushId() {
        return pushId;
    }

    public void setPushId(String pushId) {
        this.pushId = pushId;
    }
}


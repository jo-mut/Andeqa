package com.cinggl.cinggl.models;

/**
 * Created by J.EL on 1/9/2018.
 */

public class Timeline {
    String type;
    String uid;
    String pushId;
    String postId;
    long timeStamp;
    String status;

    public Timeline() {

    }

    public Timeline(String type, String uid, String pushId,
                    String postId, long timeStamp, String status) {
        this.type = type;
        this.uid = uid;
        this.pushId = pushId;
        this.postId = postId;
        this.timeStamp = timeStamp;
        this.status = status;
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

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

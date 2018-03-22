package com.andeqa.andeqa.models;

/**
 * Created by J.EL on 1/9/2018.
 */

public class Timeline {
    String type;
    String uid;
    String pushId;
    String postId;
    long time;
    String status;

    public Timeline() {

    }

    public Timeline(String type, String uid, String pushId,
                    String postId, long time, String status) {
        this.type = type;
        this.uid = uid;
        this.pushId = pushId;
        this.postId = postId;
        this.time = time;
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

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

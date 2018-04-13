package com.andeqa.andeqa.models;

/**
 * Created by J.EL on 1/9/2018.
 */

public class Timeline {
    String type;
    String userId;
    String postId;
    String activityId;
    long time;
    String status;

    public Timeline() {

    }

    public Timeline(String type, String userId, String postId,
                    String activityId, long time, String status) {
        this.type = type;
        this.userId = userId;
        this.postId = postId;
        this.activityId = activityId;
        this.time = time;
        this.status = status;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getActivityId() {
        return activityId;
    }

    public void setActivityId(String activityId) {
        this.activityId = activityId;
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

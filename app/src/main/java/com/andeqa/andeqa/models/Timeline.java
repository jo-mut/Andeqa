package com.andeqa.andeqa.models;

/**
 * Created by J.EL on 1/9/2018.
 */

public class Timeline {
    String type;
    String user_id;
    String post_id;
    String activity_id;
    long time;
    String status;

    public Timeline() {

    }

    public Timeline(String type, String user_id, String post_id,
                    String activity_id, long time, String status) {
        this.type = type;
        this.user_id = user_id;
        this.post_id = post_id;
        this.activity_id = activity_id;
        this.time = time;
        this.status = status;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getPost_id() {
        return post_id;
    }

    public void setPost_id(String post_id) {
        this.post_id = post_id;
    }

    public String getActivity_id() {
        return activity_id;
    }

    public void setActivity_id(String activity_id) {
        this.activity_id = activity_id;
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

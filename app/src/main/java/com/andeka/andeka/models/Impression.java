package com.andeka.andeka.models;

public class Impression {
    String user_id;
    String post_id;
    String impression_id;
    long time;

    public Impression() {
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

    public String getImpression_id() {
        return impression_id;
    }

    public void setImpression_id(String impression_id) {
        this.impression_id = impression_id;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}

package com.andeqa.andeqa.models;

public class Impression {
    String user_id;
    String post_id;
    String impression_id;
    String type;
    long compiled_duration;
    long recent_duration;
    long un_compiled_duration;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setImpression_id(String impression_id) {
        this.impression_id = impression_id;
    }

    public long getCompiled_duration() {
        return compiled_duration;
    }

    public void setCompiled_duration(long compiled_duration) {
        this.compiled_duration = compiled_duration;
    }

    public long getRecent_duration() {
        return recent_duration;
    }

    public void setRecent_duration(long recent_duration) {
        this.recent_duration = recent_duration;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getUn_compiled_duration() {
        return un_compiled_duration;
    }

    public void setUn_compiled_duration(long un_compiled_duration) {
        this.un_compiled_duration = un_compiled_duration;
    }
}



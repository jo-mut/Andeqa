package com.andeqa.andeqa.models;

/**
 * Created by J.EL on 11/10/2017.
 */

public class Relation {
    String following_id;
    String followed_id;
    String type;
    long time;


    public Relation() {

    }

    public Relation(String following_id) {
        this.following_id = following_id;
    }

    public String getFollowing_id() {
        return following_id;
    }

    public void setFollowing_id(String following_id) {
        this.following_id = following_id;
    }

    public String getFollowed_id() {
        return followed_id;
    }

    public void setFollowed_id(String followed_id) {
        this.followed_id = followed_id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}

package com.andeqa.andeqa.models;

/**
 * Created by J.EL on 6/24/2017.
 */

public class Like {
    String user_id;


    public Like(String user_id) {
        this.user_id = user_id;

    }

    public Like() {
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

}


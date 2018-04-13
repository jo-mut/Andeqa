package com.andeqa.andeqa.models;

/**
 * Created by J.EL on 6/24/2017.
 */

public class Like {
    String userId;


    public Like(String userId) {
        this.userId = userId;

    }

    public Like() {
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

}


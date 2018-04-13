package com.andeqa.andeqa.models;

/**
 * Created by J.EL on 11/10/2017.
 */

public class Relation {
    String userId;


    public Relation() {

    }

    public Relation(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}

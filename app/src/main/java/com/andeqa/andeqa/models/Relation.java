package com.andeqa.andeqa.models;

/**
 * Created by J.EL on 11/10/2017.
 */

public class Relation {
    String user_id;


    public Relation() {

    }

    public Relation(String user_id) {
        this.user_id = user_id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }
}

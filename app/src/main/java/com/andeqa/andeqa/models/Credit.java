package com.andeqa.andeqa.models;

/**
 * Created by J.EL on 10/25/2017.
 */

public class Credit {
    double amount;
    String user_id;
    String post_id;

    public Credit() {

    }

    public Credit(double amount, String user_id, String post_id) {
        this.amount = amount;
        this.user_id = user_id;
        this.post_id = post_id;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
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

}

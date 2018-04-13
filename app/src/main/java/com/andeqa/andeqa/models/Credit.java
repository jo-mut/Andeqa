package com.andeqa.andeqa.models;

/**
 * Created by J.EL on 10/25/2017.
 */

public class Credit {
    double amount;
    String userId;
    String postId;

    public Credit() {

    }

    public Credit(double amount, String userId, String postId) {
        this.amount = amount;
        this.userId = userId;
        this.postId = postId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
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

}

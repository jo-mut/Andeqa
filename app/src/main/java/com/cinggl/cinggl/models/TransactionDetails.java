package com.cinggl.cinggl.models;

/**
 * Created by J.EL on 8/24/2017.
 */

public class TransactionDetails {
    String uid;
    String pushId;
    String postId;
    String datePosted;
    double amount;
    double walletBalance;


    public TransactionDetails() {
    }

    public TransactionDetails(String uid, double balance,
                              double amount) {
        this.uid = uid;
        this.amount = amount;
    }

    public String getDatePosted() {
        return datePosted;
    }

    public void setDatePosted(String datePosted) {
        this.datePosted = datePosted;
    }

    public String getPushId() {
        return pushId;
    }

    public void setPushId(String pushId) {
        this.pushId = pushId;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public double getWalletBalance() {
        return walletBalance;
    }

    public void setWalletBalance(double walletBalance) {
        this.walletBalance = walletBalance;
    }
}

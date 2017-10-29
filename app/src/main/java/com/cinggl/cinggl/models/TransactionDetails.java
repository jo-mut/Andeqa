package com.cinggl.cinggl.models;

/**
 * Created by J.EL on 8/24/2017.
 */

public class TransactionDetails {
    String uid;
    String pushId;
    String postId;
    String date;
    String historyId;
    double amount;
    double walletBalance;


    public TransactionDetails() {
    }


    public TransactionDetails(String uid, String pushId, String postId, String date,
                              String historyId, double amount, double walletBalance) {
        this.uid = uid;
        this.pushId = pushId;
        this.postId = postId;
        this.date = date;
        this.historyId = historyId;
        this.amount = amount;
        this.walletBalance = walletBalance;
    }


    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
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

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getHistoryId() {
        return historyId;
    }

    public void setHistoryId(String historyId) {
        this.historyId = historyId;
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

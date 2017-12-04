package com.cinggl.cinggl.models;

/**
 * Created by J.EL on 10/25/2017.
 */

public class Credit {
    double amount;
    String uid;
    String pushId;

    public Credit() {

    }

    public Credit(double amount, String uid, String pushId) {
        this.amount = amount;
        this.uid = uid;
        this.pushId = pushId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
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

}

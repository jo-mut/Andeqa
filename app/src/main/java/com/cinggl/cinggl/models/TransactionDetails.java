package com.cinggl.cinggl.models;

/**
 * Created by J.EL on 8/24/2017.
 */

public class TransactionDetails {
    String uid;
    double amount;

    public TransactionDetails() {
    }

    public TransactionDetails(String uid, double amount) {
        this.uid = uid;
        this.amount = amount;
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

}

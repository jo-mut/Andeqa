package com.andeqa.andeqa.models;

/**
 * Created by J.EL on 8/24/2017.
 */

public class TransactionDetails {
    String uid;
    String pushId;
    String cingleId;
    String date;
    String type;
    double amount;
    double walletBalance;


    public TransactionDetails() {
    }


    public TransactionDetails(String uid, String pushId, String cingleId, String date,
                              String type, double amount, double walletBalance) {
        this.uid = uid;
        this.pushId = pushId;
        this.cingleId = cingleId;
        this.date = date;
        this.type = type;
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

    public String getCingleId() {
        return cingleId;
    }

    public void setCingleId(String cingleId) {
        this.cingleId = cingleId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

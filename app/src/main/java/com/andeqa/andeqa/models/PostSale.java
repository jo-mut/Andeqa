package com.andeqa.andeqa.models;

/**
 * Created by J.EL on 9/8/2017.
 */

public class PostSale {
    String pushId;
    String uid;
    String datePosted;
    long time;
    double randomNumber;
    double salePrice;
    long number;

    public PostSale() {

    }

    public PostSale(String pushId, String uid, String datePosted,
                    long time, double randomNumber, double
                              salePrice, long number) {
        this.pushId = pushId;
        this.uid = uid;
        this.datePosted = datePosted;
        this.time = time;
        this.randomNumber = randomNumber;
        this.salePrice = salePrice;
        this.number = number;
    }

    public String getPushId() {
        return pushId;
    }

    public void setPushId(String pushId) {
        this.pushId = pushId;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getDatePosted() {
        return datePosted;
    }

    public void setDatePosted(String datePosted) {
        this.datePosted = datePosted;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public double getRandomNumber() {
        return randomNumber;
    }

    public void setRandomNumber(double randomNumber) {
        this.randomNumber = randomNumber;
    }

    public double getSalePrice() {
        return salePrice;
    }

    public void setSalePrice(double salePrice) {
        this.salePrice = salePrice;
    }

    public long getNumber() {
        return number;
    }

    public void setNumber(long number) {
        this.number = number;
    }
}


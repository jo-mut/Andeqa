package com.cinggl.cinggl.models;

/**
 * Created by J.EL on 10/28/2017.
 */

public class CingleData {

    String uid;
    String pushId;
    long timeStamp;
    long number;
    double randomNumber;


    public CingleData() {

    }

    public CingleData(String uid, String pushId, long timeStamp,
                      long number, double randomNumber) {
        this.uid = uid;
        this.pushId = pushId;
        this.timeStamp = timeStamp;
        this.number = number;
        this.randomNumber = randomNumber;
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

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public long getNumber() {
        return number;
    }

    public void setNumber(long number) {
        this.number = number;
    }

    public double getRandomNumber() {
        return randomNumber;
    }

    public void setRandomNumber(double randomNumber) {
        this.randomNumber = randomNumber;
    }

}

package com.cinggl.cinggl.models;

/**
 * Created by J.EL on 10/28/2017.
 */

public class Post {

    long timeStamp;
    long number;
    double randomNumber;
    String title;
    String cingleImageUrl;
    String description;
    String pushId;
    String cingleIndex;
    String datePosted;
    String uid;
    double rate;
    double defaultRate;


    public Post() {

    }

    public Post(String cingleImageUrl, String cingleIndex,
                String datePosted, double defaultRate, String description,
                long number, String pushId, String pushId1, double randomNumber,
                double rate, long timeStamp, String title, String uid, String uid1) {
        this.cingleImageUrl = cingleImageUrl;
        this.cingleIndex = cingleIndex;
        this.datePosted = datePosted;
        this.defaultRate = defaultRate;
        this.description = description;
        this.number = number;
        this.pushId = pushId;
        this.pushId = pushId1;
        this.randomNumber = randomNumber;
        this.rate = rate;
        this.timeStamp = timeStamp;
        this.title = title;
        this.uid = uid;
        this.uid = uid1;
    }

    public String getCingleImageUrl() {
        return cingleImageUrl;
    }

    public void setCingleImageUrl(String cingleImageUrl) {
        this.cingleImageUrl = cingleImageUrl;
    }

    public String getCingleIndex() {
        return cingleIndex;
    }

    public void setCingleIndex(String cingleIndex) {
        this.cingleIndex = cingleIndex;
    }

    public String getDatePosted() {
        return datePosted;
    }

    public void setDatePosted(String datePosted) {
        this.datePosted = datePosted;
    }

    public double getDefaultRate() {
        return defaultRate;
    }

    public void setDefaultRate(double defaultRate) {
        this.defaultRate = defaultRate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getNumber() {
        return number;
    }

    public void setNumber(long number) {
        this.number = number;
    }

    public String getPushId() {
        return pushId;
    }

    public void setPushId(String pushId) {
        this.pushId = pushId;
    }

    public double getRandomNumber() {
        return randomNumber;
    }

    public void setRandomNumber(double randomNumber) {
        this.randomNumber = randomNumber;
    }

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}

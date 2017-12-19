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
    String creatorUid;
    double rate;
    double defaultRate;


    public Post() {

    }

    public Post(long timeStamp, long number, double randomNumber,
                String title, String cingleImageUrl,
                String description, String pushId, String cingleIndex,
                String datePosted, String uid, String creatorUid,
                double rate, double defaultRate) {
        this.timeStamp = timeStamp;
        this.number = number;
        this.randomNumber = randomNumber;
        this.title = title;
        this.cingleImageUrl = cingleImageUrl;
        this.description = description;
        this.pushId = pushId;
        this.cingleIndex = cingleIndex;
        this.datePosted = datePosted;
        this.uid = uid;
        this.creatorUid = creatorUid;
        this.rate = rate;
        this.defaultRate = defaultRate;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCingleImageUrl() {
        return cingleImageUrl;
    }

    public void setCingleImageUrl(String cingleImageUrl) {
        this.cingleImageUrl = cingleImageUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPushId() {
        return pushId;
    }

    public void setPushId(String pushId) {
        this.pushId = pushId;
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

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getCreatorUid() {
        return creatorUid;
    }

    public void setCreatorUid(String creatorUid) {
        this.creatorUid = creatorUid;
    }

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    public double getDefaultRate() {
        return defaultRate;
    }

    public void setDefaultRate(double defaultRate) {
        this.defaultRate = defaultRate;
    }
}

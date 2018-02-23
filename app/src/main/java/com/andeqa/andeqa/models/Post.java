package com.andeqa.andeqa.models;

/**
 * Created by J.EL on 10/28/2017.
 */

public class Post {

    long timeStamp;
    long number;
    double randomNumber;
    String title;
    String image;
    String description;
    String pushId;
    String cingleIndex;
    String datePosted;
    String uid;
    String creatorUid;
    String type;
    double rate;
    double defaultRate;


    public Post() {

    }

    public Post(String cingleIndex, String creatorUid,
                String datePosted, double defaultRate, String description,
                String image, long number, String pushId,
                double randomNumber, double rate, long timeStamp,
                String title, String type, String uid) {
        this.cingleIndex = cingleIndex;
        this.creatorUid = creatorUid;
        this.datePosted = datePosted;
        this.defaultRate = defaultRate;
        this.description = description;
        this.image = image;
        this.number = number;
        this.pushId = pushId;
        this.randomNumber = randomNumber;
        this.rate = rate;
        this.timeStamp = timeStamp;
        this.title = title;
        this.type = type;
        this.uid = uid;
    }

    public String getCingleIndex() {
        return cingleIndex;
    }

    public void setCingleIndex(String cingleIndex) {
        this.cingleIndex = cingleIndex;
    }

    public String getCreatorUid() {
        return creatorUid;
    }

    public void setCreatorUid(String creatorUid) {
        this.creatorUid = creatorUid;
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

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}

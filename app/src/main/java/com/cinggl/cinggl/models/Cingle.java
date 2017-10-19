package com.cinggl.cinggl.models;


import org.parceler.Parcel;

/**
 * Created by J.EL on 4/8/2017.
 */
@Parcel
public class Cingle {

    String title;
    String creator;
    String cingleImageUrl;
    String description;
    String moreDescription;
    String pushId;
    String cingleIndex;
    String uid;
    String datePosted;
    String cingleId;
    long timeStamp;
    long number;
    double randomNumber;
    double sensepoint;
    double rate;
    double defaultRate;


    public Cingle() {

    }

    public Cingle(String title, String creator, String cingleImageUrl, String description,
                  String moreDescription, String pushId, String cingleIndex,
                  String uid, String datePosted, String cingleId, long timeStamp,
                  long number, double randomNumber, double sensepoint, double rate,
                  double defaultRate) {
        this.title = title;
        this.creator = creator;
        this.cingleImageUrl = cingleImageUrl;
        this.description = description;
        this.moreDescription = moreDescription;
        this.pushId = pushId;
        this.cingleIndex = cingleIndex;
        this.uid = uid;
        this.datePosted = datePosted;
        this.cingleId = cingleId;
        this.timeStamp = timeStamp;
        this.number = number;
        this.randomNumber = randomNumber;
        this.sensepoint = sensepoint;
        this.rate = rate;
        this.defaultRate = defaultRate;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
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

    public String getMoreDescription() {
        return moreDescription;
    }

    public void setMoreDescription(String moreDescription) {
        this.moreDescription = moreDescription;
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

    public String getCingleId() {
        return cingleId;
    }

    public void setCingleId(String cingleId) {
        this.cingleId = cingleId;
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

    public double getSensepoint() {
        return sensepoint;
    }

    public void setSensepoint(double sensepoint) {
        this.sensepoint = sensepoint;
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

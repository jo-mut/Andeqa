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
    String profileImageUrl;
    String description;
    String moreDescription;
    String pushId;
    String cingleIndex;
    String uid;
    String datePosted;
    long timeStamp;
    long number;
    double randomNumber;
    double sensepoint;
    double rate;
    double defaultRate;


    public Cingle() {

    }

    public Cingle(String cingleImageUrl, String cingleIndex, String creator, String datePosted,
                  double defaultRate, String description, String moreDescription, long number,
                  String profileImageUrl, String pushId, double randomNumber, double rate,
                  double sensepoint, long timeStamp, String title, String uid) {
        this.cingleImageUrl = cingleImageUrl;
        this.cingleIndex = cingleIndex;
        this.creator = creator;
        this.datePosted = datePosted;
        this.defaultRate = defaultRate;
        this.description = description;
        this.moreDescription = moreDescription;
        this.number = number;
        this.profileImageUrl = profileImageUrl;
        this.pushId = pushId;
        this.randomNumber = randomNumber;
        this.rate = rate;
        this.sensepoint = sensepoint;
        this.timeStamp = timeStamp;
        this.title = title;
        this.uid = uid;
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

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
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

    public String getMoreDescription() {
        return moreDescription;
    }

    public void setMoreDescription(String moreDescription) {
        this.moreDescription = moreDescription;
    }

    public long getNumber() {
        return number;
    }

    public void setNumber(long number) {
        this.number = number;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
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

    public double getSensepoint() {
        return sensepoint;
    }

    public void setSensepoint(double sensepoint) {
        this.sensepoint = sensepoint;
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

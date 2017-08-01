package com.cinggl.cinggl.models;


import com.google.firebase.database.Exclude;

import org.parceler.Parcel;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by J.EL on 4/8/2017.
 */
@Parcel
public class Cingle {
    Cingle cingle;
    Cingulan cingulan;
    String title;
    String accountUserName;
    String cingleImageUrl;
    String profileImageUrl;
    String description;
    String moreDescription;
    String pushId;
    long timeStamp;
    long number;
    private String cingleIndex;
    double randomNumber;
    String uid;
    String datePosted;
    double sensepoint;

    public Cingle(){

    }

    public Cingle(double sensepoint, String datePosted,
                  String uid, String pushId,
                  String description, String profileImageUrl,
                  String cingleImageUrl, String accountUserName,
                  String title) {
        this.sensepoint = sensepoint;
        this.datePosted = datePosted;
        this.uid = uid;
        this.pushId = pushId;
        this.description = description;
        this.profileImageUrl = profileImageUrl;
        this.cingleImageUrl = cingleImageUrl;
        this.accountUserName = accountUserName;
        this.title = title;
    }

    public double getSensepoint() {
        return sensepoint;
    }

    public void setSensepoint(double sensepoint) {
        this.sensepoint = sensepoint;
    }

    public Cingle getCingle() {
        return cingle;
    }

    public void setCingle(Cingle cingle) {
        this.cingle = cingle;
    }

    public Cingulan getCingulan() {
        return cingulan;
    }

    public void setCingulan(Cingulan cingulan) {
        this.cingulan = cingulan;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAccountUserName() {
        return accountUserName;
    }

    public void setAccountUserName(String accountUserName) {
        this.accountUserName = accountUserName;
    }

    public String getCingleImageUrl() {
        return cingleImageUrl;
    }

    public void setCingleImageUrl(String cingleImageUrl) {
        this.cingleImageUrl = cingleImageUrl;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
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

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
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

    public long getNumber() {
        return number;
    }

    public void setNumber(long number) {
        this.number = number;
    }

    public String getCingleIndex() {
        return cingleIndex;
    }

    public void setCingleIndex(String cingleIndex) {
        this.cingleIndex = cingleIndex;
    }

    public double getRandomNumber() {
        return randomNumber;
    }

    public void setRandomNumber(double randomNumber) {
        this.randomNumber = randomNumber;
    }
}

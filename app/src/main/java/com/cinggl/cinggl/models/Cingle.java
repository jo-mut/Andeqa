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

    String title;
    String accountUserName;
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


    public Cingle(){

    }

    public Cingle(String title, String accountUserName, String cingleImageUrl,
                  String profileImageUrl, String description,
                  String moreDescription, String pushId,
                  String cingleIndex, String uid,
                  String datePosted, long timeStamp, long number,
                  double randomNumber, double sensepoint) {
        this.title = title;
        this.accountUserName = accountUserName;
        this.cingleImageUrl = cingleImageUrl;
        this.profileImageUrl = profileImageUrl;
        this.description = description;
        this.moreDescription = moreDescription;
        this.pushId = pushId;
        this.cingleIndex = cingleIndex;
        this.uid = uid;
        this.datePosted = datePosted;
        this.timeStamp = timeStamp;
        this.number = number;
        this.randomNumber = randomNumber;
        this.sensepoint = sensepoint;
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
}

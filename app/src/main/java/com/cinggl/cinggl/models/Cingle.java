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
    String timeStamp;
    String uid;
    String datePosted;
    double sensepoint;

    public Cingle(){

    }

    public Cingle(double sensepoint, String datePosted,
                  String uid, String timeStamp, String pushId,
                  String description, String profileImageUrl,
                  String cingleImageUrl, String accountUserName,
                  String title) {
        this.sensepoint = sensepoint;
        this.datePosted = datePosted;
        this.uid = uid;
        this.timeStamp = timeStamp;
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

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
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
}

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
    Cingulan cingulan;
    String title;
    int viewsCount;
    String accountUserName;
    String cingleImageUrl;
    String profileImageUrl;

    public int likesCount = 0;
    public Map<String, Boolean>likeByUser = new HashMap<>();
    int commentsCount;
    int cingleWorth;
    String description;
    String moreDescription;
    String pushId;
    String timeStamp;
    String uid;

    public Cingle(){

    }

    public Cingle(Cingulan cingulan, String cingleImageUrl,
                  String description, String title, String timeStamp) {
        this.cingleImageUrl = cingleImageUrl;
        this.description = description;
        this.title = title;
        this.cingulan = cingulan;
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

    public int getViewsCount() {
        return viewsCount;
    }

    public void setViewsCount(int viewsCount) {
        this.viewsCount = viewsCount;
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

    public int getLikesCount() {
        return likesCount;
    }

    public void setLikesCount(int likesCount) {
        this.likesCount = likesCount;
    }

    public int getCommentsCount() {
        return commentsCount;
    }

    public void setCommentsCount(int commentsCount) {
        this.commentsCount = commentsCount;
    }

    public int getCingleWorth() {
        return cingleWorth;
    }

    public void setCingleWorth(int cingleWorth) {
        this.cingleWorth = cingleWorth;
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

    @Exclude
    public Map<String, Object> toMap(){
        HashMap<String, Object> result = new HashMap<>();
        result.put("likeCount", likesCount);
        result.put("likeByUser", likeByUser);

        return result;
    }

}

package com.cinggl.cinggl.models;


import org.parceler.Parcel;

/**
 * Created by J.EL on 4/8/2017.
 */
@Parcel
public class Cingle {

    String title;
    int viewsCount;
    String accountUserName;
    String cingleImageUrl;

    int likesCount;
    int commentsCount;
    int cingleWorth;

    String description;
    String moreDescription;
    private String pushId;

    public Cingle(){

    }

    public Cingle(String cingleImageUrl,
                  String description, String title) {
        this.cingleImageUrl = cingleImageUrl;
        this.description = description;
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

    public int getCingleWorth() {
        return cingleWorth;
    }

    public void setCingleWorth(int cingleWorth) {
        this.cingleWorth = cingleWorth;
    }

    public int getCommentsCount() {
        return commentsCount;
    }

    public void setCommentsCount(int commentsCount) {
        this.commentsCount = commentsCount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getLikesCount() {
        return likesCount;
    }

    public void setLikesCount(int likesCount) {
        this.likesCount = likesCount;
    }

    public String getMoreDescription() {
        return moreDescription;
    }

    public void setMoreDescription(String moreDescription) {
        this.moreDescription = moreDescription;
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

    public String getPushId() {
        return pushId;
    }

    public void setPushId(String pushId) {
        this.pushId = pushId;
    }
}

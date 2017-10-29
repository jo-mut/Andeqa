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
    String pushId;
    String cingleIndex;
    String datePosted;
    String uid;
    double rate;
    double defaultRate;

    public Cingle() {

    }

    public Cingle(String title, String creator, String cingleImageUrl,
                  String description, String pushId, String cingleIndex,
                  String datePosted, String uid, double rate,
                  double defaultRate) {
        this.title = title;
        this.creator = creator;
        this.cingleImageUrl = cingleImageUrl;
        this.description = description;
        this.pushId = pushId;
        this.cingleIndex = cingleIndex;
        this.datePosted = datePosted;
        this.uid = uid;
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

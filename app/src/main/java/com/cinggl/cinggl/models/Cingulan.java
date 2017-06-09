package com.cinggl.cinggl.models;

import org.parceler.Parcel;

/**
 * Created by J.EL on 5/4/2017.
 */
@Parcel
public class Cingulan {
    private String profileImage;
    private String bio;
    private String username;
    private String timestamp;
    private String pushId;
    private String uid;

    public Cingulan(){
        //EMPTY CONSTRUCTOR REQUIRED
    }

    public Cingulan(String uid) {
        this.profileImage = profileImage;
        this.username = username;
        this.uid = uid;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getPushId() {
        return pushId;
    }

    public void setPushId(String pushId) {
        this.pushId = pushId;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}

package com.andeqa.andeqa.models;

/**
 * Created by J.EL on 5/4/2017.
 */

public class Cinggulan {
    String profileImage;
    String profileCover;
    String bio;
    String username;
    String pushId;
    String uid;
    String email;
    String firstName;
    String secondName;

    public Cinggulan(){
        //EMPTY CONSTRUCTOR REQUIRED
    }

    public Cinggulan(String profileImage, String bio,
                     String username, String profileCover,
                     String pushId, String uid, String email,
                     String firstName, String secondName) {
        this.profileImage = profileImage;
        this.bio = bio;
        this.username = username;
        this.pushId = pushId;
        this.uid = uid;
        this.email = email;
        this.profileCover = profileCover;
        this.firstName = firstName;
        this.secondName = secondName;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getProfileCover() {
        return profileCover;
    }

    public void setProfileCover(String profileCover) {
        this.profileCover = profileCover;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getSecondName() {
        return secondName;
    }

    public void setSecondName(String secondName) {
        this.secondName = secondName;
    }
}
package com.cinggl.cinggl.models;

/**
 * Created by J.EL on 5/4/2017.
 */

public class Cingulan {
    String firstNmae;
    String secondName;
    String profileImage;
    String bio;
    String username;
    private String pushId;

    public Cingulan(){
        //EMPTY CONSTRUCTOR REQUIRED
    }

    public Cingulan(String secondName, String profileImage,
                    String firstNmae, String bio, String username) {
        this.secondName = secondName;
        this.profileImage = profileImage;
        this.firstNmae = firstNmae;
        this.bio = bio;
        this.username = username;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getSecondName() {
        return secondName;
    }

    public void setSecondName(String secondName) {
        this.secondName = secondName;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getFirstNmae() {
        return firstNmae;
    }

    public void setFirstNmae(String firstNmae) {
        this.firstNmae = firstNmae;
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
}

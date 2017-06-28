package com.cinggl.cinggl.models;

/**
 * Created by J.EL on 6/24/2017.
 */

public class Like {
    String uid;
    String username;
    String profileImage;

    public Like() {
    }

    public Like(String uid, String username,
                String profileImage) {
        this.uid = uid;
        this.username = username;
        this.profileImage = profileImage;

    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

}

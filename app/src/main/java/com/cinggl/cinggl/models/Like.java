package com.cinggl.cinggl.models;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by J.EL on 6/24/2017.
 */

public class Like {
    String username;
    String profileImage;
    String uid;

    public Like() {
    }

    public Like(String profileImage, String uid, String username) {
        this.profileImage = profileImage;
        this.uid = uid;
        this.username = username;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
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
}

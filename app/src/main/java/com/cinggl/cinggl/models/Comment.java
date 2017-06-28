package com.cinggl.cinggl.models;

/**
 * Created by J.EL on 6/15/2017.
 */

public class Comment {
    public String uid;
    public String username;
    public String commentText;
    public String profileImage;

    public Comment() {
    }

    public Comment(String username, String uid, String commentText, String profileImage) {
        this.username = username;
        this.profileImage = profileImage;
        this.uid = uid;
        this.commentText = commentText;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getCommentText() {
        return commentText;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }
}

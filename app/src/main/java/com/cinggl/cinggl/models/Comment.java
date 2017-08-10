package com.cinggl.cinggl.models;

/**
 * Created by J.EL on 6/15/2017.
 */

public class Comment {
    String uid;
    String username;
    String commentText;

    public Comment() {
    }

    public Comment(String username, String uid, String commentText) {
        this.username = username;
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

}

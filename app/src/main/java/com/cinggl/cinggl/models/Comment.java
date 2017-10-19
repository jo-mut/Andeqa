package com.cinggl.cinggl.models;

/**
 * Created by J.EL on 6/15/2017.
 */

public class Comment {
    String uid;
    String username;
    String commentText;
    String pushId;
    String postKey;

    public Comment() {
    }

    public Comment(String uid, String username, String commentText,
                   String pushId, String postKey) {
        this.uid = uid;
        this.username = username;
        this.commentText = commentText;
        this.pushId = pushId;
        this.postKey = postKey;
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

    public String getCommentText() {
        return commentText;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    public String getPushId() {
        return pushId;
    }

    public void setPushId(String pushId) {
        this.pushId = pushId;
    }

    public String getPostKey() {
        return postKey;
    }

    public void setPostKey(String postKey) {
        this.postKey = postKey;
    }
}

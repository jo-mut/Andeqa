package com.cinggl.cinggl.models;

/**
 * Created by J.EL on 6/15/2017.
 */

public class Comment {
    String uid;
    String commentText;
    String pushId;
    String postId;

    public Comment() {
    }

    public Comment(String uid, String commentText, String pushId, String postId) {
        this.uid = uid;
        this.commentText = commentText;
        this.pushId = pushId;
        this.postId = postId;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
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

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }
}

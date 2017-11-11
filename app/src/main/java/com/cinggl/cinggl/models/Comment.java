package com.cinggl.cinggl.models;

/**
 * Created by J.EL on 6/15/2017.
 */

public class Comment {
    String uid;
    String commentText;
    String pushId;

    public Comment() {
    }

    public Comment(String uid, String commentText,
                   String pushId) {
        this.uid = uid;
        this.commentText = commentText;
        this.pushId = pushId;
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

}

package com.cinggl.cinggl.models;

/**
 * Created by J.EL on 1/4/2018.
 */

public class MessagingUser {
    String uid;
    String message;
    long time;
    String pushId;
    String roomId;

    public MessagingUser() {

    }

    public MessagingUser(String uid, String message, long time, String pushId, String roomId) {
        this.uid = uid;
        this.message = message;
        this.time = time;
        this.pushId = pushId;
        this.roomId = roomId;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getPushId() {
        return pushId;
    }

    public void setPushId(String pushId) {
        this.pushId = pushId;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }
}

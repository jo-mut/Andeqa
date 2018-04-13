package com.andeqa.andeqa.models;

/**
 * Created by J.EL on 1/4/2018.
 */

public class Message {

    String senderUid;
    String recepientUid;
    String message;
    String messageId;
    String type;
    String roomId;
    long time;

    public Message() {
    }

    public Message(String senderUid, String recepientUid, String message,
                   String messageId, String type, String roomId, long time) {
        this.senderUid = senderUid;
        this.recepientUid = recepientUid;
        this.message = message;
        this.messageId = messageId;
        this.type = type;
        this.roomId = roomId;
        this.time = time;

    }

    public String getSenderUid() {
        return senderUid;
    }

    public void setSenderUid(String senderUid) {
        this.senderUid = senderUid;
    }

    public String getRecepientUid() {
        return recepientUid;
    }

    public void setRecepientUid(String recepientUid) {
        this.recepientUid = recepientUid;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }


    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long timeStamp) {
        this.time = timeStamp;
    }
}

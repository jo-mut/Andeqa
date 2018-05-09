package com.andeqa.andeqa.models;

/**
 * Created by J.EL on 1/4/2018.
 */

public class Message {

    String sender_id;
    String recepient_id;
    String message;
    String message_id;
    String type;
    String room_id;
    long time;

    public Message() {
    }

    public Message(String sender_id, String recepient_id, String message,
                   String message_id, String type, String room_id, long time) {
        this.sender_id = sender_id;
        this.recepient_id = recepient_id;
        this.message = message;
        this.message_id = message_id;
        this.type = type;
        this.room_id = room_id;
        this.time = time;

    }

    public String getSender_id() {
        return sender_id;
    }

    public void setSender_id(String sender_id) {
        this.sender_id = sender_id;
    }

    public String getRecepient_id() {
        return recepient_id;
    }

    public void setRecepient_id(String recepient_id) {
        this.recepient_id = recepient_id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage_id() {
        return message_id;
    }

    public void setMessage_id(String message_id) {
        this.message_id = message_id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }


    public String getRoom_id() {
        return room_id;
    }

    public void setRoom_id(String room_id) {
        this.room_id = room_id;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long timeStamp) {
        this.time = timeStamp;
    }
}

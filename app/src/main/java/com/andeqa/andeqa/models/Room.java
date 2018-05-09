package com.andeqa.andeqa.models;

/**
 * Created by J.EL on 1/4/2018.
 */

public class Room {
    String receiver_id;
    String sender_id;
    String message;
    long time;
    String room_id;
    String sender_status;
    String receiver_status;

    public Room() {

    }


    public String getReceiver_id() {
        return receiver_id;
    }

    public void setReceiver_id(String receiver_id) {
        this.receiver_id = receiver_id;
    }

    public String getSender_id() {
        return sender_id;
    }

    public void setSender_id(String sender_id) {
        this.sender_id = sender_id;
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


    public String getRoom_id() {
        return room_id;
    }

    public void setRoom_id(String room_id) {
        this.room_id = room_id;
    }

    public String getSender_status() {
        return sender_status;
    }

    public void setSender_status(String sender_status) {
        this.sender_status = sender_status;
    }

    public String getReceiver_status() {
        return receiver_status;
    }

    public void setReceiver_status(String receiver_status) {
        this.receiver_status = receiver_status;
    }
}

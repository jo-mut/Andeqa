package com.andeqa.andeqa.models;

public class ChatPeriod extends Chat {

    private String time;

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    @Override
    public int getType() {
        return 0;
    }
}

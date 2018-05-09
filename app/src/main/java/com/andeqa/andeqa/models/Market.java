package com.andeqa.andeqa.models;

/**
 * Created by J.EL on 9/8/2017.
 */

public class Market {
    String post_id;
    String user_id;
    long time;
    double random_number;
    double sale_price;
    long number;

    public Market() {

    }


    public String getPost_id() {
        return post_id;
    }

    public void setPost_id(String post_id) {
        this.post_id = post_id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public double getRandom_number() {
        return random_number;
    }

    public void setRandom_number(double random_number) {
        this.random_number = random_number;
    }

    public double getSale_price() {
        return sale_price;
    }

    public void setSale_price(double sale_price) {
        this.sale_price = sale_price;
    }

    public long getNumber() {
        return number;
    }

    public void setNumber(long number) {
        this.number = number;
    }
}


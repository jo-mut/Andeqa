package com.andeqa.andeqa.models;

/**
 * Created by J.EL on 8/25/2017.
 */

public class Wallet {
    double balance;
    double deposited;
    double redeemed;
    String address;
    String user_id;
    long time;

    public Wallet() {
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public double getDeposited() {
        return deposited;
    }

    public void setDeposited(double deposited) {
        this.deposited = deposited;
    }

    public double getRedeemed() {
        return redeemed;
    }

    public void setRedeemed(double redeemed) {
        this.redeemed = redeemed;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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
}

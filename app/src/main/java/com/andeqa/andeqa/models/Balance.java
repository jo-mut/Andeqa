package com.andeqa.andeqa.models;

/**
 * Created by J.EL on 8/25/2017.
 */

public class Balance {
    double total_balance;
    double amount_deposited;
    double amount_redeemed;

    public Balance() {
    }

    public double getTotal_balance() {
        return total_balance;
    }

    public void setTotal_balance(double total_balance) {
        this.total_balance = total_balance;
    }

    public double getAmount_deposited() {
        return amount_deposited;
    }

    public void setAmount_deposited(double amount_deposited) {
        this.amount_deposited = amount_deposited;
    }

    public double getAmount_redeemed() {
        return amount_redeemed;
    }

    public void setAmount_redeemed(double amount_redeemed) {
        this.amount_redeemed = amount_redeemed;
    }
}

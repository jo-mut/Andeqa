package com.andeqa.andeqa.models;

/**
 * Created by J.EL on 8/25/2017.
 */

public class Balance {
    double totalBalance;
    double amountDeposited;
    double amountRedeemed;

    public Balance() {
    }

    public double getTotalBalance() {
        return totalBalance;
    }

    public void setTotalBalance(double totalBalance) {
        this.totalBalance = totalBalance;
    }

    public double getAmountDeposited() {
        return amountDeposited;
    }

    public void setAmountDeposited(double amountDeposited) {
        this.amountDeposited = amountDeposited;
    }

    public double getAmountRedeemed() {
        return amountRedeemed;
    }

    public void setAmountRedeemed(double amountRedeemed) {
        this.amountRedeemed = amountRedeemed;
    }
}

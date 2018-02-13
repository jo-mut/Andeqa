package com.andeqa.andeqa.models;

/**
 * Created by J.EL on 1/2/2018.
 */

public class TraceData {
    //Duration spent viewing the cingle
    private long viewDuartion;

    // the id for the viewed view
    private String viewId;

    //height of the viewed view in percentage
    private double percentageHeightVisible;

    //the cingles unique push id
    private String pushId;


    public TraceData() {

    }


    public long getViewDuartion() {
        return viewDuartion;
    }

    public void setViewDuartion(long viewDuartion) {
        this.viewDuartion = viewDuartion;
    }

    public String getViewId() {
        return viewId;
    }

    public void setViewId(String viewId) {
        this.viewId = viewId;
    }

    public double getPercentageHeightVisible() {
        return percentageHeightVisible;
    }

    public void setPercentageHeightVisible(double percentageHeightVisible) {
        this.percentageHeightVisible = percentageHeightVisible;
    }

    public String getPushId() {
        return pushId;
    }

    public void setPushId(String pushId) {
        this.pushId = pushId;
    }
}

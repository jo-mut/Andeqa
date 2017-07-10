package com.cinggl.cinggl.models;

/**
 * Created by J.EL on 7/4/2017.
 */

public class TraceData {
    //Duration spent viewing the cingle
    private long viewDuartion;

    // the id for the viewed view
    private String viewId;

    //height of the viewed view in percentage
    private double percentageHeightVisible;

    //number of times the cingle has gained impressions;
    private int impression;

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

    public int getImpression() {
        return impression;
    }

    public void setImpression(int impression) {
        this.impression = impression;
    }

    public String getPushId() {
        return pushId;
    }

    public void setPushId(String pushId) {
        this.pushId = pushId;
    }
}

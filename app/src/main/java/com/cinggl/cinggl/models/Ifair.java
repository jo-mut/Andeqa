package com.cinggl.cinggl.models;

import org.parceler.Parcel;

/**
 * Created by J.EL on 8/16/2017.
 */

@Parcel
public class Ifair {
    String image;
    String pushId;
    String title;
    String description;
    String creator;

    public Ifair() {
    }

    public Ifair(String image, String pushId, String title,
                 String description, String creator) {
        this.image = image;
        this.pushId = pushId;
        this.title = title;
        this.description = description;
        this.creator = creator;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getPushId() {
        return pushId;
    }

    public void setPushId(String pushId) {
        this.pushId = pushId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }
}

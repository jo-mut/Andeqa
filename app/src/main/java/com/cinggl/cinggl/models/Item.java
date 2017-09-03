package com.cinggl.cinggl.models;

/**
 * Created by J.EL on 8/30/2017.
 */

public class Item {

    public enum ItemType{
        LIKE_TYPE, COMMENT_TYPE, CINGLES_TYPE, FOLLOWERS_TYPE,
    }

    private String uid;
    private ItemType type;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public ItemType getType() {
        return type;
    }

    public void setType(ItemType type) {
        this.type = type;
    }
}

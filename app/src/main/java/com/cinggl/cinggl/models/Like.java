package com.cinggl.cinggl.models;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by J.EL on 6/24/2017.
 */

public class Like {
    String uid;



    public Like() {

    }

    public Like(String uid) {
        this.uid = uid;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}


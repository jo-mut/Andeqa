package com.cinggl.cinggl.ui;

import com.cinggl.cinggl.Constants;
import com.cinggl.cinggl.models.Cingulan;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by J.EL on 6/8/2017.
 */

public class FirebaseUtil {

    public static DatabaseReference getAppRef(){
        return FirebaseDatabase.getInstance().getReference();
    }

    public static String getCurrentUserId(){
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(firebaseUser != null){
            return firebaseUser.getUid();
        }
        return null;
    }

    public static Cingulan getCingulan() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        return new Cingulan(user.getUid());
    }

    public static DatabaseReference getCurrentUserReference(){
        String uid = getCurrentUserId();
        if(uid != null){
            return getAppRef().child("Cingulans").child(getCurrentUserId());
        }
        return null;
    }

    public static DatabaseReference getCinglesRef(){
        return getAppRef().child(Constants.FIREBASE_PUBLIC_CINGLES);
    }

    public static DatabaseReference getUsersRef(){
        return getAppRef().child("Cingulans");
    }

    public static DatabaseReference getLikesRef() {
        return getAppRef().child("likes");
    }

}

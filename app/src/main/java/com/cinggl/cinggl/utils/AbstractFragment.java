package com.cinggl.cinggl.utils;

import android.support.v4.app.Fragment;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

/**
 * Created by J.EL on 7/6/2017.
 */

public abstract class AbstractFragment extends Fragment{

    public AbstractFragment() {

    }

    public abstract Query getQuery(DatabaseReference databaseReference);
}

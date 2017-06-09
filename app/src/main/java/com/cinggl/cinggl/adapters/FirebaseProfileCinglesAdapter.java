package com.cinggl.cinggl.adapters;

import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.view.MotionEvent;
import android.view.View;

import com.cinggl.cinggl.models.Cingle;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

/**
 * Created by J.EL on 6/9/2017.
 */

public class FirebaseProfileCinglesAdapter extends FirebaseRecyclerAdapter
        <Cingle, FirebaseProfileCinglesViewHolder> {

    private DatabaseReference databaseReference;
    private Context mContext;

    public FirebaseProfileCinglesAdapter(Class<Cingle> modelClass, int modelLayout,
                                         Class<FirebaseProfileCinglesViewHolder> viewHolderClass,
                                         Query ref, DatabaseReference databaseReference,
                                         Context context) {
        super(modelClass, modelLayout, viewHolderClass, ref);
        databaseReference = ref.getRef();
        mContext = context;
    }

    @Override
    protected void populateViewHolder(final FirebaseProfileCinglesViewHolder viewHolder,
                                      Cingle model, int position) {
        viewHolder.bindProfileCingle(model);

    }
}

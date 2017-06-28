package com.cinggl.cinggl.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cinggl.cinggl.R;
import com.cinggl.cinggl.models.Like;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.nostra13.universalimageloader.utils.L;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by J.EL on 6/25/2017.
 */

public class LikesAdapter extends RecyclerView.Adapter<LikesViewHolder> {
    private Context mContext;
    private DatabaseReference mDatabaseReference;
    private ChildEventListener mChildEventListener;
    private static final String TAG = "LikesAdapter";

    private List<String> mLikeIds = new ArrayList<>();
    private List<Like> likes = new ArrayList<>();

    public LikesAdapter(final Context context, DatabaseReference reference) {
        mContext = context;
        mDatabaseReference = reference;

        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "onChildAdded:" + dataSnapshot.getKey());

                //add the new like to the list to be displayed
                Like like = dataSnapshot.getValue(Like.class);

                //update the RecyclerView;
                mLikeIds.add(dataSnapshot.getKey());
                likes.add(like);
                notifyItemInserted(likes.size() - 1);

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        reference.addChildEventListener(childEventListener);
        mChildEventListener = childEventListener;
    }

    public void cleanUpListener(){
        if(mChildEventListener != null){
            mDatabaseReference.removeEventListener(mChildEventListener);
        }
    }

    @Override
    public void onBindViewHolder(LikesViewHolder holder, int position) {
        Like like = likes.get(position);
        holder.bindLikes(like);
    }

    @Override
    public LikesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.likes_list_layout, parent, false);
        return new LikesViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return likes.size();
    }
}

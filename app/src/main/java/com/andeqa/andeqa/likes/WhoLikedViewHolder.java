package com.andeqa.andeqa.likes;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.andeqa.andeqa.R;
import com.google.firebase.firestore.DocumentSnapshot;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by J.EL on 8/17/2017.
 */

public class WhoLikedViewHolder extends RecyclerView.ViewHolder {
    View mView;
    Context mContext;
    public CircleImageView whoLikedImageView;

    public WhoLikedViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
        mContext = itemView.getContext();
        whoLikedImageView = (CircleImageView ) mView.findViewById(R.id.whoLikedImageView);

    }

    public void bindWhoLiked(final DocumentSnapshot documentSnapshot){

    }


}

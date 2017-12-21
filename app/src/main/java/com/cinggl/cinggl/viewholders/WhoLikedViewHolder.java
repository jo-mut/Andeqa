package com.cinggl.cinggl.viewholders;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.cinggl.cinggl.R;
import com.cinggl.cinggl.models.Like;
import com.google.firebase.firestore.DocumentSnapshot;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

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

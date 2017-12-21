package com.cinggl.cinggl.viewholders;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.cinggl.cinggl.R;
import com.cinggl.cinggl.models.Like;
import com.google.firebase.firestore.DocumentSnapshot;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by J.EL on 6/25/2017.
 */

public class LikesViewHolder extends RecyclerView.ViewHolder {

    View mView;
    Context mContext;
    public TextView usernameTextView;
    public CircleImageView profileImageView;
    public Button followButton;
    public TextView fullNameTextView;

    public LikesViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
        mContext = itemView.getContext();
        usernameTextView = (TextView)itemView.findViewById(R.id.usernameTextView);
        profileImageView = (CircleImageView) itemView.findViewById(R.id.creatorImageView);
        followButton = (Button) itemView.findViewById(R.id.followTextView);
        fullNameTextView = (TextView) itemView.findViewById(R.id.fullNameTextView);
    }

    public void bindLikes(final DocumentSnapshot documentSnapshot){
        profileImageView = (CircleImageView) mView.findViewById(R.id.creatorImageView);

    }

}

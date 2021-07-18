package com.andeka.andeka.people;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.andeka.andeka.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class PeopleRelationsViewHolder extends RecyclerView.ViewHolder {
    Context mContext;
    View mView;
    public ImageView mSendMessageImageView;
    public CircleImageView mProfileImageView;
    public Button mFollowButton;
    public TextView mFullNameTextView;
    public TextView mUsernameTextView;

    public PeopleRelationsViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
        mContext = itemView.getContext();
        mSendMessageImageView = (ImageView) mView.findViewById(R.id.sendMessageImageView);
        mProfileImageView = (CircleImageView) mView.findViewById(R.id.profileImageView);
        mFollowButton = (Button) mView.findViewById(R.id.followButton);
        mFullNameTextView = (TextView) mView.findViewById(R.id.fullNameTextView);
        mUsernameTextView = (TextView) mView.findViewById(R.id.usernameTextView);
    }
}

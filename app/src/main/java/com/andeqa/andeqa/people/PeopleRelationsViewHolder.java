package com.andeqa.andeqa.people;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.andeqa.andeqa.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class PeopleRelationsViewHolder extends RecyclerView.ViewHolder {
    View mView;
    Context mContext;
    public TextView fullNameTextView;
    public CircleImageView profileImageView;
    public Button followButton;
    public TextView usernameTextView;
    public ImageView sendMessageImageView;

    public PeopleRelationsViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
        mContext = itemView.getContext();
        fullNameTextView = (TextView) itemView.findViewById(R.id.fullNameTextView);
        profileImageView = (CircleImageView) itemView.findViewById(R.id.profileImageView);
        followButton = (Button) itemView.findViewById(R.id.followButton);
        usernameTextView = (TextView) itemView.findViewById(R.id.usernameTextView);
        sendMessageImageView = (ImageView) itemView.findViewById(R.id.sendMessageImageView);
    }
}



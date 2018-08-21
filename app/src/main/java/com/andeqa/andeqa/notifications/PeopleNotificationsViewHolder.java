package com.andeqa.andeqa.notifications;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.andeqa.andeqa.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class PeopleNotificationsViewHolder extends RecyclerView.ViewHolder {
    View mView;
    Context mContext;
    public CircleImageView profileImageView;
    public TextView usernameTextView;
    public RelativeLayout peopleRelativeLayout;
    public ImageView postImageView;
    public Button followButton;

    public PeopleNotificationsViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
        mContext = itemView.getContext();
        usernameTextView = (TextView)itemView.findViewById(R.id.usernameTextView);
        profileImageView = (CircleImageView) itemView.findViewById(R.id.profileImageView);
        peopleRelativeLayout = (RelativeLayout) itemView.findViewById(R.id.peopleRelativeLayout);
        followButton = (Button) itemView.findViewById(R.id.followButton);

    }
}

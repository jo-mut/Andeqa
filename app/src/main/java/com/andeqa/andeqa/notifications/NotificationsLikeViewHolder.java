package com.andeqa.andeqa.notifications;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.andeqa.andeqa.R;
import com.andeqa.andeqa.models.Timeline;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by J.EL on 1/18/2018.
 */

public class NotificationsLikeViewHolder extends RecyclerView.ViewHolder {


    View mView;
    Context mContext;
    public CircleImageView profileImageView;
    public TextView usernameTextView;
    public LinearLayout timelineLikeLinearLayout;
    public ImageView postImageView;

    public NotificationsLikeViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
        mContext = itemView.getContext();
        usernameTextView = (TextView)itemView.findViewById(R.id.usernameTextView);
        profileImageView = (CircleImageView) itemView.findViewById(R.id.profileImageView);
        timelineLikeLinearLayout = (LinearLayout) itemView.findViewById(R.id.notificationLinearLayout);
        postImageView = (ImageView) itemView.findViewById(R.id.postImageView);
    }

}

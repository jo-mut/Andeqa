package com.andeka.andeka.notifications;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.andeka.andeka.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class NotificationLikeViewHolder extends RecyclerView.ViewHolder {

    View mView;
    Context mContext;
    public TextView usernameTextView;
    public CircleImageView profileImageView;
    public LinearLayout timelineCommentLinearLayout;
    public ImageView postImageView;
    public TextView activityTextView;

    public NotificationLikeViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
        mContext = itemView.getContext();
        usernameTextView = (TextView)itemView.findViewById(R.id.usernameTextView);
        profileImageView = (CircleImageView) itemView.findViewById(R.id.profileImageView);
        timelineCommentLinearLayout = (LinearLayout) itemView.findViewById(R.id.notificationLinearLayout);
        postImageView = (ImageView) itemView.findViewById(R.id.postImageView);
        activityTextView = (TextView) itemView.findViewById(R.id.activityTextView);

    }

}

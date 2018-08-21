package com.andeqa.andeqa.message;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.andeqa.andeqa.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class RoomPhotoViewHolder extends RecyclerView.ViewHolder {
    View mView;
    Context mContext;
    public CircleImageView profileImageView;
    public TextView timeTextView;
    public TextView lastMessageTextView;
    public RelativeLayout roomRelativeLayout;
    public TextView usernameTextView;

    public RoomPhotoViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
        mContext = itemView.getContext();
        timeTextView = (TextView) mView.findViewById(R.id.timeTextView);
        profileImageView = (CircleImageView) mView.findViewById(R.id.profileImageView);
        lastMessageTextView = (TextView) mView.findViewById(R.id.lastMessageTextView);
        roomRelativeLayout = (RelativeLayout) mView.findViewById(R.id.messagingUserRelativeLayout);
        usernameTextView = (TextView) mView.findViewById(R.id.usernameTextView);

    }
}

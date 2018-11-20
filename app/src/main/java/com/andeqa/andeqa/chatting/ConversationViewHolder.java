package com.andeqa.andeqa.chatting;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.andeqa.andeqa.R;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by J.EL on 1/4/2018.
 */

public class ConversationViewHolder extends RecyclerView.ViewHolder {
    View mView;
    Context mContext;
    public CircleImageView profileImageView;
    public TextView usernameTextView;
    public TextView timeTextView;
    public TextView lastMessageTextView;
    public RelativeLayout roomRelativeLayout;

    public ConversationViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
        mContext = itemView.getContext();
        timeTextView = (TextView) mView.findViewById(R.id.statusTextView);
        usernameTextView = (TextView) mView.findViewById(R.id.usernameTextView);
        profileImageView = (CircleImageView) mView.findViewById(R.id.profileImageView);
        lastMessageTextView = (TextView) mView.findViewById(R.id.lastMessageTextView);
        roomRelativeLayout = (RelativeLayout) mView.findViewById(R.id.messagingUserRelativeLayout);

    }


}


package com.andeqa.andeqa.message;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.andeqa.andeqa.R;
import com.andeqa.andeqa.models.Room;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by J.EL on 1/4/2018.
 */

public class MessageViewHolder extends RecyclerView.ViewHolder {
    View mView;
    Context mContext;
    public CircleImageView profileImageView;
    public TextView usernameTextView;
    public TextView timeTextView;
    public TextView lastMessageTextView;
    public RelativeLayout roomRelativeLayout;
    public View statusView;



    public MessageViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
        mContext = itemView.getContext();
        timeTextView = (TextView) mView.findViewById(R.id.timeTextView);
        usernameTextView = (TextView) mView.findViewById(R.id.usernameTextView);
        profileImageView = (CircleImageView) mView.findViewById(R.id.profileImageView);
        lastMessageTextView = (TextView) mView.findViewById(R.id.lastMessageTextView);
        roomRelativeLayout = (RelativeLayout) mView.findViewById(R.id.messagingUserRelativeLayout);
        statusView = (View) mView.findViewById(R.id.statusView);

    }

    public void bindMessagingUser(final Room room){


    }
}


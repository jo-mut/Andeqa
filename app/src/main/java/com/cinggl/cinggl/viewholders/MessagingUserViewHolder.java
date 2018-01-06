package com.cinggl.cinggl.viewholders;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.cinggl.cinggl.R;
import com.cinggl.cinggl.models.Message;
import com.cinggl.cinggl.models.MessagingUser;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by J.EL on 1/4/2018.
 */

public class MessagingUserViewHolder extends RecyclerView.ViewHolder{

    View mView;
    Context mContext;
    public CircleImageView profileImageView;
    public TextView usernameTextView;
    public TextView timeTextView;
    public TextView lastMessageTextView;

    public MessagingUserViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
        mContext = itemView.getContext();
        timeTextView = (TextView) mView.findViewById(R.id.timeTextView);
        usernameTextView = (TextView) mView.findViewById(R.id.usernameTextView);
        profileImageView = (CircleImageView) mView.findViewById(R.id.profileImageView);
        lastMessageTextView = (TextView) mView.findViewById(R.id.lastMessageTextView);
    }

    public void bindMessagingUser(final MessagingUser messagingUser){

    }

}

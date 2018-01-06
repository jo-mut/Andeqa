package com.cinggl.cinggl.viewholders;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.cinggl.cinggl.R;
import com.cinggl.cinggl.models.Message;
import com.google.firebase.firestore.DocumentSnapshot;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.cinggl.cinggl.R.id.timeTextView;


/**
 * Created by J.EL on 1/4/2018.
 */

public class MessageViewHolder extends RecyclerView.ViewHolder {
    View mView;
    Context mContext;
    public TextView statusTextView;
    public TextView messageTextView;


    public MessageViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
        mContext = itemView.getContext();
        statusTextView = (TextView) mView.findViewById(R.id.statusTextView);
        messageTextView = (TextView) mView.findViewById(R.id.messageTextView);
    }

    public void bindMessage(final Message message){


    }
}


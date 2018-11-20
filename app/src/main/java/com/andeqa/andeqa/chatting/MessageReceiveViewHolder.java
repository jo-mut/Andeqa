package com.andeqa.andeqa.chatting;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.andeqa.andeqa.R;
import com.andeqa.andeqa.models.Message;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * Created by J.EL on 1/17/2018.
 */

public class MessageReceiveViewHolder extends RecyclerView.ViewHolder {

    View mView;
    Context mContext;
    public TextView messageTextView;
    public TextView timeTextView;
    public ImageView statusImageView;
    public TextView dateTextView;
    public TextView photoTextView;
    public ImageView photoImageView;
    public LinearLayout photoLinearLayout;
    public LinearLayout timeRelativeLayout;

    public MessageReceiveViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
        mContext = itemView.getContext();
        messageTextView = (TextView) mView.findViewById(R.id.messageTextView);
        timeTextView = (TextView) mView.findViewById(R.id.statusTextView);
        photoImageView = (ImageView) mView.findViewById(R.id.photoImageView);
        photoLinearLayout = (LinearLayout) mView.findViewById(R.id.photoLinearLayout);
        photoTextView = (TextView) mView.findViewById(R.id.messageTextView);
        timeRelativeLayout = (LinearLayout) mView.findViewById(R.id.timeRelativeLayout);
        dateTextView = (TextView) mView.findViewById(R.id.dateTextView);

    }

    public void bindMessage(final Message message){

    }


}

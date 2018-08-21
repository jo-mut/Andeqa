package com.andeqa.andeqa.message;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.andeqa.andeqa.R;
import com.andeqa.andeqa.models.Message;
import com.andeqa.andeqa.utils.ProportionalImageView;


/**
 * Created by J.EL on 1/17/2018.
 */

public class MessageReceiveViewHolder extends RecyclerView.ViewHolder {

    View mView;
    Context mContext;
    public TextView messageTextView;
    public TextView timeTextView;
    public ImageView statusImageView;
    public LinearLayout receiveLinearLayout;
    public RelativeLayout statusRelativeLayout;
    public TextView photoTextView;
    public ProportionalImageView photoImageView;
    public LinearLayout messageLinearLayout;
    public LinearLayout photoLinearLayout;


    public MessageReceiveViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
        mContext = itemView.getContext();
        messageTextView = (TextView) mView.findViewById(R.id.messageTextView);
        timeTextView = (TextView) mView.findViewById(R.id.timeTextView);
        statusImageView = (ImageView) mView.findViewById(R.id.statusImageView);
        statusRelativeLayout = (RelativeLayout) mView.findViewById(R.id.statusLinearLayout);
        receiveLinearLayout = (LinearLayout) mView.findViewById(R.id.receiveRelativeLayout);
        photoImageView = (ProportionalImageView) mView.findViewById(R.id.photoImageView);
        messageLinearLayout = (LinearLayout) mView.findViewById(R.id.messageLinearLayout);
        photoLinearLayout = (LinearLayout) mView.findViewById(R.id.photoLinearLayout);
        photoTextView = (TextView) mView.findViewById(R.id.messageTextView);

    }

    public void bindMessage(final Message message){

    }


}

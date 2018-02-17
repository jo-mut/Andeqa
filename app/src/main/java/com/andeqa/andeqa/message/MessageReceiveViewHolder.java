package com.andeqa.andeqa.message;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.andeqa.andeqa.R;
import com.andeqa.andeqa.models.Message;

import static com.andeqa.andeqa.R.id.statusView;

/**
 * Created by J.EL on 1/17/2018.
 */

public class MessageReceiveViewHolder extends RecyclerView.ViewHolder {

    View mView;
    Context mContext;
    public TextView messageTextView;
    public TextView timeTextView;
//    public ImageView statusImageView;
    public RelativeLayout receiveRelativeLayout;
    public RelativeLayout statusRelativeLayout;
    public TextView dateTextView;

    public MessageReceiveViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
        mContext = itemView.getContext();
        messageTextView = (TextView) mView.findViewById(R.id.messageTextView);
        timeTextView = (TextView) mView.findViewById(R.id.timeTextView);
//        statusImageView = (ImageView) mView.findViewById(R.id.statusImageView);
        statusRelativeLayout = (RelativeLayout) mView.findViewById(R.id.statusRelativeLayout);
        receiveRelativeLayout = (RelativeLayout) mView.findViewById(R.id.receiveRelativeLayout);
        dateTextView = (TextView) mView.findViewById(R.id.dateTextView);

    }

    public void bindMessage(final Message message){

    }


}

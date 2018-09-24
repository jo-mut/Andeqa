package com.andeqa.andeqa.chatting;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.andeqa.andeqa.R;

public class MessageReceivePhotoViewHolder  extends RecyclerView.ViewHolder {

    View mView;
    Context mContext;
    public TextView timeTextView;
    public LinearLayout statusLinearLayout;
    public TextView messageTextView;
    public ImageView photoImageView;
    public ImageView statusImageView;
    public LinearLayout messageLinearLayout;
    public LinearLayout photoLinearLayout;


    public MessageReceivePhotoViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
        mContext = itemView.getContext();
        timeTextView = (TextView) mView.findViewById(R.id.timeTextView);
        statusLinearLayout = (LinearLayout) mView.findViewById(R.id.statusLinearLayout);
        photoImageView = (ImageView) mView.findViewById(R.id.photoImageView);
        statusImageView = (ImageView) mView.findViewById(R.id.statusImageView);
        messageLinearLayout = (LinearLayout) mView.findViewById(R.id.messageLinearLayout);
        photoLinearLayout = (LinearLayout) mView.findViewById(R.id.photoLinearLayout);
        messageTextView = (TextView) mView.findViewById(R.id.messageTextView);

    }

}

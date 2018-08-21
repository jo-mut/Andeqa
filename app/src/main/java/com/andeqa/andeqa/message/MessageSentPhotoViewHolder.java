package com.andeqa.andeqa.message;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.andeqa.andeqa.R;

public class MessageSentPhotoViewHolder extends RecyclerView.ViewHolder {

    View mView;
    Context mContext;
    public TextView timeTextView;
    public LinearLayout statusLinearLayout;
    public TextView photoTextView;
    public ImageView photoImageView;
    public TextView statusTextView;
    public LinearLayout messageLinearLayout;
    public LinearLayout photoLinearLayout;


    public MessageSentPhotoViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
        mContext = itemView.getContext();
        timeTextView = (TextView) mView.findViewById(R.id.timeTextView);
        statusLinearLayout = (LinearLayout) mView.findViewById(R.id.statusLinearLayout);
        statusTextView = (TextView) mView.findViewById(R.id.statusImageView);
        photoImageView = (ImageView) mView.findViewById(R.id.photoImageView);
        messageLinearLayout = (LinearLayout) mView.findViewById(R.id.messageLinearLayout);
        photoLinearLayout = (LinearLayout) mView.findViewById(R.id.photoLinearLayout);
        photoTextView = (TextView) mView.findViewById(R.id.messageTextView);

    }

}

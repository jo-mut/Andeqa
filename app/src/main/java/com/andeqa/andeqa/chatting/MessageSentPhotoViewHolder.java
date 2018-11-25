package com.andeqa.andeqa.chatting;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.andeqa.andeqa.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageSentPhotoViewHolder extends RecyclerView.ViewHolder {

    View mView;
    Context mContext;
    public TextView timeTextView;
    public TextView messageTextView;
    public ImageView photoImageView;
    public TextView dateTextView;
    public LinearLayout photoLinearLayout;
    public ImageView seenImageView;
    public LinearLayout statusRelativeLayout;


    public MessageSentPhotoViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
        mContext = itemView.getContext();
        timeTextView = (TextView) mView.findViewById(R.id.statusTextView);
        photoImageView = (ImageView) mView.findViewById(R.id.photoImageView);
        photoLinearLayout = (LinearLayout) mView.findViewById(R.id.photoLinearLayout);
        messageTextView = (TextView) mView.findViewById(R.id.messageTextView);
        seenImageView = (ImageView) mView.findViewById(R.id.seenImageView);
        statusRelativeLayout = (LinearLayout) mView.findViewById(R.id.timeRelativeLayout);
        dateTextView = (TextView) mView.findViewById(R.id.dateTextView);

    }

}

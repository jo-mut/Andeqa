package com.andeqa.andeqa.more;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.andeqa.andeqa.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class MorePeopleViewHolder extends RecyclerView.ViewHolder{
    View mView;
    Context mContext;
    public TextView fullNameTextView;
    public CircleImageView profileImageView;
    public ImageView followImageView;
    public TextView usernameTextView;

    public ImageView sendMessageImageView;

    public MorePeopleViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
        mContext = itemView.getContext();
        fullNameTextView = (TextView) itemView.findViewById(R.id.fullNameTextView);
        profileImageView = (CircleImageView) itemView.findViewById(R.id.profileImageView);
        usernameTextView = (TextView) itemView.findViewById(R.id.usernameTextView);
    }
}

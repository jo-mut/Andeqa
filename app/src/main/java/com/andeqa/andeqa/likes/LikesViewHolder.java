package com.andeqa.andeqa.likes;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.andeqa.andeqa.R;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by J.EL on 6/25/2017.
 */

public class LikesViewHolder extends RecyclerView.ViewHolder {

    View mView;
    Context mContext;
    public TextView usernameTextView;
    public CircleImageView profileImageView;
    public TextView fullNameTextView;
    @Bind(R.id.followRelativeLayout)RelativeLayout followRelativeLayout;
    @Bind(R.id.followButton)Button followButton;
    @Bind(R.id.sendMessageImageView)ImageView sendMessageImageView;
    @Bind(R.id.sendMessageRelativeLayout) RelativeLayout mSendMessageRelativeLayout;


    public LikesViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
        mContext = itemView.getContext();
        ButterKnife.bind(this, mView);
        usernameTextView = (TextView)itemView.findViewById(R.id.usernameTextView);
        profileImageView = (CircleImageView) itemView.findViewById(R.id.profileImageView);
        fullNameTextView = (TextView) itemView.findViewById(R.id.fullNameTextView);
        followRelativeLayout = (RelativeLayout) itemView.findViewById(R.id.followRelativeLayout);
        sendMessageImageView = (ImageView) itemView.findViewById(R.id.sendMessageImageView);
        followButton = (Button) itemView.findViewById(R.id.followButton);
        mSendMessageRelativeLayout = (RelativeLayout) itemView.findViewById(R.id.sendMessageRelativeLayout);
    }

}

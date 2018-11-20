package com.andeqa.andeqa.people;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.andeqa.andeqa.R;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class PeopleRelationsViewHolder extends RecyclerView.ViewHolder {
    Context mContext;
    View mView;
    @Bind(R.id.usernameTextView)TextView mUsernameTextView;
    @Bind(R.id.fullNameTextView)TextView mFullNameTextView;
    @Bind(R.id.profileImageView)CircleImageView mProfileImageView;
    @Bind(R.id.followButton) Button mFollowButton;
    @Bind(R.id.sendMessageImageView)ImageView mSendMessageImageView;

    public PeopleRelationsViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
        mContext = itemView.getContext();
        ButterKnife.bind(this, mView);
    }
}

package com.andeqa.andeqa.people;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.andeqa.andeqa.R;

import butterknife.Bind;
import butterknife.ButterKnife;

public class FollowViewHolder extends RecyclerView.ViewHolder {
    Context context;
    View mView;
    @Bind(R.id.followButton)Button followButton;
    @Bind(R.id.usernameTextView)TextView usernameTextView;
    @Bind(R.id.profileCoverImageView)ImageView profileCoverImageView;
    @Bind(R.id.profileImageView)ImageView profileImageView;
    @Bind(R.id.followLinearLayout)LinearLayout followLinearLayout;

    public FollowViewHolder(View itemView) {
        super(itemView);
        context = itemView.getContext();
        mView = itemView;
        ButterKnife.bind(this, mView);
    }
}

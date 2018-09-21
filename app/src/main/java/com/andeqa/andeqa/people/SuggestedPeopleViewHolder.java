package com.andeqa.andeqa.people;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.andeqa.andeqa.R;

import butterknife.Bind;
import butterknife.ButterKnife;

public class SuggestedPeopleViewHolder extends RecyclerView.ViewHolder {
    View mView;
    Context context;
    @Bind(R.id.followButton)Button followButton;
    @Bind(R.id.usernameTextView)TextView usernameTextView;
    @Bind(R.id.profileCoverImageView)ImageView profileCoverImageView;
    @Bind(R.id.profileImageView)ImageView profileImageView;
    @Bind(R.id.profileCoverRelativeLayout)RelativeLayout profileCoverRelativeLayout;
    @Bind(R.id.bioTextView)TextView mBioTextView;

    public SuggestedPeopleViewHolder(View itemView) {
        super(itemView);
        context = itemView.getContext();
        mView = itemView;
        ButterKnife.bind(this, mView);
    }
}

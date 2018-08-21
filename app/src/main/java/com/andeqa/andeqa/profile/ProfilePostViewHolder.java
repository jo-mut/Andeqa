package com.andeqa.andeqa.profile;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.andeqa.andeqa.R;
import com.google.firebase.firestore.CollectionReference;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfilePostViewHolder extends RecyclerView.ViewHolder {

    View mView;
    Context mContext;
    public ImageView likesImageView;
    public ImageView dislikeImageView;
    public LinearLayout dislikeLinearLayout;
    public LinearLayout likesLinearLayout;
    public LinearLayout commentsLinearLayout;
    public TextView dislikeCountTextView;
    public ImageView commentsImageView;
    public TextView likesCountTextView;
    public TextView titleTextView;
    public TextView descriptionTextView;
    public TextView usernameTextView;
    public CircleImageView profileImageView;
    public TextView commentsCountTextView;
    public TextView senseCreditsTextView;
    public ImageView postImageView;
    public static final int MAX_WIDTH = 400;
    public static final int MAX_HEIGHT = 400;
    public LinearLayout likesRelativeLayout;
    public LinearLayout mCommentsLinearLayout;
    public TextView timeTextView;
    public LinearLayout mCreditsLinearLayout;
    public RelativeLayout postRelativeLayout;

    public ProfilePostViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
        mContext = itemView.getContext();
        likesImageView = (ImageView) itemView.findViewById(R.id.likesImageView);

        dislikeImageView = (ImageView) itemView.findViewById(R.id.dislikeImageView);
        dislikeCountTextView = (TextView) itemView.findViewById(R.id.dislikesCountTextView);
        commentsImageView = (ImageView) itemView.findViewById(R.id.commentsImageView);
        descriptionTextView = (TextView) itemView.findViewById(R.id.descriptionTextView);
        titleTextView = (TextView) itemView.findViewById(R.id.titleTextView);
        usernameTextView = (TextView) itemView.findViewById(R.id.usernameTextView);
        profileImageView = (CircleImageView) itemView.findViewById(R.id.profileImageView);
        commentsCountTextView = (TextView) itemView.findViewById(R.id.commentsCountTextView);
        postImageView = (ImageView) mView.findViewById(R.id.postImageView);
        senseCreditsTextView = (TextView) mView.findViewById(R.id.creditsTextView);
        likesRelativeLayout = (LinearLayout) mView.findViewById(R.id.likesLinearLayout);
        likesCountTextView = (TextView) mView.findViewById(R.id.likesCountTextView);
        mCommentsLinearLayout = (LinearLayout) mView.findViewById(R.id.commentsLinearLayout);
        mCreditsLinearLayout = (LinearLayout) mView.findViewById(R.id.creditsLinearLayout);
        postRelativeLayout = (RelativeLayout) mView.findViewById(R.id.postRelativeLayout);
        dislikeLinearLayout = (LinearLayout) mView.findViewById(R.id.dislikeLinearLayout);
    }

}

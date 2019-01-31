package com.andeqa.andeqa.home;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.andeqa.andeqa.R;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by J.EL on 2/13/2018.
 */

public class PhotoPostViewHolder extends RecyclerView.ViewHolder {

    View mView;
    Context mContext;
    public LinearLayout commentsLinearLayout;
    public ImageView commentsImageView;
    public TextView titleTextView;
    public TextView descriptionTextView;
    public TextView usernameTextView;
    public CircleImageView profileImageView;
    public TextView commentsCountTextView;
    public RelativeLayout titleRelativeLayout;
    public RelativeLayout descriptionRelativeLayout;
    public TextView timeTextView;
    public LinearLayout captionLinearLayout;
    public ConstraintLayout postConstraintLayout;
    public ImageView postImageView;
    public TextView collectionNameTextView;
    public LinearLayout mLikesLinearLayout;
    public ImageView mLikeImageView;
    public TextView  mLikesTextView;

    public PhotoPostViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
        mContext = itemView.getContext();
        captionLinearLayout  =  (LinearLayout)  itemView.findViewById(R.id.captionLinearLayout);
        commentsImageView = (ImageView) itemView.findViewById(R.id.commentsImageView);
        descriptionTextView = (TextView) itemView.findViewById(R.id.descriptionTextView);
        titleTextView = (TextView) itemView.findViewById(R.id.titleTextView);
        usernameTextView = (TextView) itemView.findViewById(R.id.usernameTextView);
        profileImageView = (CircleImageView) itemView.findViewById(R.id.profileImageView);
        commentsCountTextView = (TextView) itemView.findViewById(R.id.commentsCountTextView);
        titleRelativeLayout = (RelativeLayout) itemView.findViewById(R.id.titleRelativeLayout);
        descriptionRelativeLayout  = (RelativeLayout) itemView.findViewById(R.id.descriptionRelativeLayout);
        postImageView = (ImageView) itemView.findViewById(R.id.postImageView);
        postConstraintLayout = (ConstraintLayout) itemView.findViewById(R.id.postConstraintLayout);
        collectionNameTextView = (TextView) itemView.findViewById(R.id.collectionNameTextView);
        mLikesLinearLayout = (LinearLayout) itemView.findViewById(R.id.likesLinearLayout);
        mLikesTextView = (TextView) itemView.findViewById(R.id.likesTextView);
        mLikeImageView = (ImageView) itemView.findViewById(R.id.likesImageView);
    }


}

package com.andeqa.andeqa.home;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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
    ProgressBar progressBar;
    public ImageView viewsImageView;
    public LinearLayout viewsLinearLayout;
    public TextView viewsCountTextView;
    public LinearLayout commentsLinearLayout;
    public ImageView commentsImageView;
    public TextView titleTextView;
    public TextView descriptionTextView;
    public TextView usernameTextView;
    public CircleImageView profileImageView;
    public TextView commentsCountTextView;
    public TextView senseCreditsTextView;
    public RelativeLayout titleRelativeLayout;
    public static final int MAX_WIDTH = 400;
    public static final int MAX_HEIGHT = 400;
    public RelativeLayout descriptionRelativeLayout;
    public LinearLayout mCommentsLinearLayout;
    public TextView timeTextView;
    public LinearLayout captionLinearLayout;
    public LinearLayout mCreditsLinearLayout;
    public ConstraintLayout postConstraintLayout;
    public ImageView postImageView;

    public PhotoPostViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
        mContext = itemView.getContext();
        viewsImageView = (ImageView) itemView.findViewById(R.id.viewsImageView);
        viewsLinearLayout = (LinearLayout) mView.findViewById(R.id.viewsLinearLayout);
        viewsCountTextView = (TextView) mView.findViewById(R.id.viewsCountTextView);
        captionLinearLayout  =  (LinearLayout)  itemView.findViewById(R.id.captionLinearLayout);
        commentsImageView = (ImageView) itemView.findViewById(R.id.commentsImageView);
        descriptionTextView = (TextView) itemView.findViewById(R.id.descriptionTextView);
        titleTextView = (TextView) itemView.findViewById(R.id.titleTextView);
        usernameTextView = (TextView) itemView.findViewById(R.id.usernameTextView);
        profileImageView = (CircleImageView) itemView.findViewById(R.id.profileImageView);
        commentsCountTextView = (TextView) itemView.findViewById(R.id.commentsCountTextView);
        titleRelativeLayout = (RelativeLayout) itemView.findViewById(R.id.titleRelativeLayout);
        descriptionRelativeLayout  = (RelativeLayout) mView.findViewById(R.id.descriptionRelativeLayout);
        postImageView = (ImageView) mView.findViewById(R.id.postImageView);
        postConstraintLayout = (ConstraintLayout) mView.findViewById(R.id.postConstrantLayout);
        senseCreditsTextView = (TextView) mView.findViewById(R.id.creditsTextView);
        mCommentsLinearLayout = (LinearLayout) mView.findViewById(R.id.commentsLinearLayout);
        mCreditsLinearLayout = (LinearLayout) mView.findViewById(R.id.creditsLinearLayout);
    }


}

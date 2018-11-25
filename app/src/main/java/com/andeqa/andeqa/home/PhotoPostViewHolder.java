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
import com.andeqa.andeqa.utils.ProportionalImageView;

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
    public ProportionalImageView postImageView;
    public TextView collectionNameTextView;

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
        postImageView = (ProportionalImageView) itemView.findViewById(R.id.postImageView);
        postConstraintLayout = (ConstraintLayout) itemView.findViewById(R.id.postConstraintLayout);
        collectionNameTextView = (TextView) itemView.findViewById(R.id.collectionNameTextView);
    }


}

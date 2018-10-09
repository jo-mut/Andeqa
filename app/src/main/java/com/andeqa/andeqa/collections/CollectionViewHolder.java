package com.andeqa.andeqa.collections;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.andeqa.andeqa.R;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by J.EL on 2/28/2018.
 */

public class CollectionViewHolder extends RecyclerView.ViewHolder {

    View mView;
    Context mContext;
    public ImageView mCollectionCoverImageView;
    public Button followButton;
    public LinearLayout followRelativeLayout;
    public TextView mCollectionNameTextView;
    public TextView mCollectionsNoteTextView;
    public LinearLayout mCollectionsLinearLayout;
    public TextView followingCountTextView;
    public TextView postsCountTextView;
    public CircleImageView profileImageView;


    public CollectionViewHolder(View itemView) {
        super(itemView);
        mContext = itemView.getContext();
        mView = itemView;
        mCollectionCoverImageView = (ImageView) mView.findViewById(R.id.collectionCoverImageView);
        mCollectionNameTextView = (TextView) mView.findViewById(R.id.collectionNameTextView);
        mCollectionsNoteTextView = (TextView) mView.findViewById(R.id.collectionsNoteTextView);
        mCollectionsLinearLayout = (LinearLayout) mView.findViewById(R.id.collectionLinearLayout);
        followButton = (Button) mView.findViewById(R.id.followButton);
        postsCountTextView = (TextView) mView.findViewById(R.id.postsCountTextView);
        followingCountTextView  = (TextView) mView.findViewById(R.id.followingCountTextView);
        followRelativeLayout = (LinearLayout) mView.findViewById(R.id.followRelativeLayout);
        profileImageView = (CircleImageView) mView.findViewById(R.id.profileImageView);


    }


}


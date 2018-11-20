package com.andeqa.andeqa.collections;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.andeqa.andeqa.R;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by J.EL on 2/28/2018.
 */

public class ExploreCollectionViewHolder extends RecyclerView.ViewHolder {

    View mView;
    Context mContext;
    public ImageView mCollectionCoverImageView;
    public TextView mCollectionNameTextView;
    public TextView mCollectionsNoteTextView;
    public LinearLayout mCollectionsLinearLayout;
    public CircleImageView mProfileImageView;
    public RelativeLayout mFavoriteRelativeLayout;


    public ExploreCollectionViewHolder(View itemView) {
        super(itemView);
        mContext = itemView.getContext();
        mView = itemView;
        mCollectionCoverImageView = (ImageView) mView.findViewById(R.id.collectionCoverImageView);
        mCollectionNameTextView = (TextView) mView.findViewById(R.id.collectionNameTextView);
        mCollectionsNoteTextView = (TextView) mView.findViewById(R.id.collectionsNoteTextView);
        mCollectionsLinearLayout = (LinearLayout) mView.findViewById(R.id.collectionLinearLayout);
        mProfileImageView = (CircleImageView) mView.findViewById(R.id.profileImageView);
        mFavoriteRelativeLayout = (RelativeLayout) mView.findViewById(R.id.favoriteRelativeLayout);
    }
}

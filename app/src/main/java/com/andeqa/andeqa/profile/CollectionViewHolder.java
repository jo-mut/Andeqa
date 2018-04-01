package com.andeqa.andeqa.profile;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.andeqa.andeqa.R;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by J.EL on 2/28/2018.
 */

public class CollectionViewHolder extends RecyclerView.ViewHolder {

    View mView;
    Context mContext;
    @Bind(R.id.collectionCoverImageView)ImageView mCollectionCoverImageView;
    @Bind(R.id.collectionNameTextView)TextView mCollectionNameTextView;
    @Bind(R.id.collectionsNoteTextView)TextView mCollectionsNoteTextView;
    @Bind(R.id.collectionLinearLayout)LinearLayout collectionsLinearLayout;
    @Bind(R.id.collectionSettingsImageView)ImageView mCollectionsSettingsImageView;

    public CollectionViewHolder(View itemView) {
        super(itemView);
        mContext = itemView.getContext();
        mView = itemView;
        ButterKnife.bind(this, mView);

    }


}


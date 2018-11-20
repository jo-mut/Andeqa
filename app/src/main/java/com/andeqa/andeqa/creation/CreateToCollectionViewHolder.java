package com.andeqa.andeqa.creation;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.andeqa.andeqa.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class CreateToCollectionViewHolder extends RecyclerView.ViewHolder {

    View mView;
    Context mContext;
    public ImageView mCollectionCoverImageView;
    public TextView mCollectionNameTextView;
    public TextView mCollectionsNoteTextView;


    public CreateToCollectionViewHolder(View itemView) {
        super(itemView);
        mContext = itemView.getContext();
        mView = itemView;
        mCollectionCoverImageView = (ImageView) mView.findViewById(R.id.collectionCoverImageView);
        mCollectionNameTextView = (TextView) mView.findViewById(R.id.collectionNameTextView);
        mCollectionsNoteTextView = (TextView) mView.findViewById(R.id.collectionsNoteTextView);
    }
}

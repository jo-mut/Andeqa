package com.andeqa.andeqa.profile;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.andeqa.andeqa.R;
import com.andeqa.andeqa.utils.ProportionalImageView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileCollectionsViewHolder extends RecyclerView.ViewHolder {

    View mView;
    Context mContext;
    public ImageView mCollectionCoverImageView;
    public TextView mCollectionNameTextView;
    public TextView mCollectionsNoteTextView;
    public LinearLayout mCollectionsLinearLayout;
    public TextView postCountTextVew;


    public ProfileCollectionsViewHolder(View itemView) {
        super(itemView);
        mContext = itemView.getContext();
        mView = itemView;

        mCollectionCoverImageView = (ImageView) mView.findViewById(R.id.collectionCoverImageView);
        mCollectionNameTextView = (TextView) mView.findViewById(R.id.collectionNameTextView);
        mCollectionsNoteTextView = (TextView) mView.findViewById(R.id.collectionsNoteTextView);
        mCollectionsLinearLayout = (LinearLayout) mView.findViewById(R.id.collectionLinearLayout);
        postCountTextVew = (TextView) mView.findViewById(R.id.postsCountTextView);

    }

}

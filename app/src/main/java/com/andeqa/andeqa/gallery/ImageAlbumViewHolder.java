package com.andeqa.andeqa.gallery;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.andeqa.andeqa.R;

public class ImageAlbumViewHolder extends RecyclerView.ViewHolder {
    Context context;
    View view;
    public ImageView albumImageView;
    public TextView galleryTitleTextView;
    public LinearLayout albumLinearLayout;

    public ImageAlbumViewHolder(View itemView) {
        super(itemView);
        context = itemView.getContext();
        view = itemView;
        albumImageView = (ImageView) view.findViewById(R.id.galleryImageView);
        albumLinearLayout = (LinearLayout) view.findViewById(R.id.albumLinearLayout);
        galleryTitleTextView = (TextView) view.findViewById(R.id.galleryTitleTextView);
    }
}

package com.andeka.andeka.gallery;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.andeka.andeka.R;

public class VideoAlbumViewHolder extends RecyclerView.ViewHolder{
    Context context;
    View view;
    public ImageView videoImageView;
    public TextView galleryTitleTextView;

    public VideoAlbumViewHolder(View itemView) {
        super(itemView);
        context = itemView.getContext();
        view = itemView;
        videoImageView = (ImageView) view.findViewById(R.id.videoImageView);
        galleryTitleTextView = (TextView) view.findViewById(R.id.galleryTitleTextView);
    }
}

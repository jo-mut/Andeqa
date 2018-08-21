package com.andeqa.andeqa.camera;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.andeqa.andeqa.R;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;

public class VideoAlbumViewHolder extends RecyclerView.ViewHolder{
    Context context;
    View view;
    public ImageView videoImageView;
    public TextView galleryCountTextView;
    public TextView galleryTitleTextView;

    public VideoAlbumViewHolder(View itemView) {
        super(itemView);
        context = itemView.getContext();
        view = itemView;
        videoImageView = (ImageView) view.findViewById(R.id.videoImageView);
        galleryCountTextView = (TextView) view.findViewById(R.id.galleryCount);
        galleryTitleTextView = (TextView) view.findViewById(R.id.galleryTitle);
    }
}

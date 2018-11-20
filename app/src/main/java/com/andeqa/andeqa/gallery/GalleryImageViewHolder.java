package com.andeqa.andeqa.gallery;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.VideoView;

import com.andeqa.andeqa.R;

public class GalleryImageViewHolder extends RecyclerView.ViewHolder {
    Context context;
    View view;
    public ImageView picsImageView;
    public VideoView videoView;
    public RelativeLayout videoRelativeLayout;
    public RelativeLayout imageRelativeLayout;

    public GalleryImageViewHolder(View itemView) {
        super(itemView);
        context = itemView.getContext();
        view = itemView;
        videoView = (VideoView) view.findViewById(R.id.simpleExoPlayerView);
        picsImageView = (ImageView) view.findViewById(R.id.galleryImageView);
        imageRelativeLayout = (RelativeLayout) view.findViewById(R.id.imageRelativeLayout);
    }

}

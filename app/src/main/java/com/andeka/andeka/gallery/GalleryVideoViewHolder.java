package com.andeka.andeka.gallery;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.andeka.andeka.R;

public class GalleryVideoViewHolder extends RecyclerView.ViewHolder {
    Context context;
    View view;
    public ImageView videoImageView;
    public ImageView playImageView;
    public ImageView pauseImageView;

    public GalleryVideoViewHolder(View itemView) {
        super(itemView);
        context = itemView.getContext();
        view = itemView;
        videoImageView = (ImageView) view.findViewById(R.id.videoImageView);
        playImageView = (ImageView) view.findViewById(R.id.playImageView);
        pauseImageView = (ImageView) view.findViewById(R.id.pauseImageView);
    }

}

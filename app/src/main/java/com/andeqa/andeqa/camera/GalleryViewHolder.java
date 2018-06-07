package com.andeqa.andeqa.camera;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.andeqa.andeqa.R;

public class GalleryViewHolder extends RecyclerView.ViewHolder {
    View mView;
    Context mContext;
    ImageView picsImageView;

    public GalleryViewHolder(View itemView) {
        super(itemView);
        mContext = itemView.getContext();
        mView = itemView;
        picsImageView = (ImageView) itemView.findViewById(R.id.picImageView);
    }

}

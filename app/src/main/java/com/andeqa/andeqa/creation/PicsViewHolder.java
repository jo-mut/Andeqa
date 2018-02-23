package com.andeqa.andeqa.creation;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.andeqa.andeqa.R;

/**
 * Created by J.EL on 2/22/2018.
 */

public class PicsViewHolder extends RecyclerView.ViewHolder {

    View mView;
    Context mContext;
    public ImageView picsImageView;

    public PicsViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
        mContext = itemView.getContext();
        picsImageView = (ImageView) mView.findViewById(R.id.picImageView);

    }
}

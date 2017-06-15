package com.cinggl.cinggl.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.cinggl.cinggl.R;
import com.cinggl.cinggl.models.Cingle;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

/**
 * Created by J.EL on 6/8/2017.
 */

public class FirebaseProfileCinglesViewHolder extends RecyclerView.ViewHolder {

    View mView;
    Context mContext;


    public FirebaseProfileCinglesViewHolder(View itemView){
        super(itemView);
        mView = itemView;
        mContext = itemView.getContext();

    }

    public void bindProfileCingle(Cingle cingle){
        ImageView cingleImageView = (ImageView) mView.findViewById(R.id.cingleItemImageView);


        Picasso.with(mContext)
                .load(cingle.getCingleImageUrl())
                .fit()
                .centerCrop()
                .into(cingleImageView, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {

                    }
                });

    }



}

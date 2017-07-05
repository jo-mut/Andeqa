package com.cinggl.cinggl.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.cinggl.cinggl.Constants;
import com.cinggl.cinggl.R;
import com.cinggl.cinggl.models.Cingle;
import com.cinggl.cinggl.profile.CingleDetailActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.parceler.Parcels;

import java.util.ArrayList;

import static com.cinggl.cinggl.R.id.cingleItemImageView;

/**
 * Created by J.EL on 6/8/2017.
 */

public class FirebaseProfileCinglesViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    View mView;
    Context mContext;
    private ImageView cingleItemImageView;


    public FirebaseProfileCinglesViewHolder(View itemView){
        super(itemView);
        mView = itemView;
        mContext = itemView.getContext();
        itemView.setOnClickListener(this);
        cingleItemImageView = (ImageView) itemView.findViewById(R.id.cingleItemImageView);

    }

    public void bindProfileCingle(final Cingle cingle){
        final ImageView cingleImageView = (ImageView) mView.findViewById(R.id.cingleItemImageView);

        Picasso.with(mContext)
                .load(cingle.getCingleImageUrl())
                .fit()
                .centerCrop()
                .networkPolicy(NetworkPolicy.OFFLINE)
                .into(cingleImageView, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {
                        Picasso.with(mContext)
                                .load(cingle.getCingleImageUrl())
                                .fit()
                                .centerCrop()
                                .into(cingleImageView);


                    }
                });
    }

    @Override
    public  void onClick(View v){


    }



}

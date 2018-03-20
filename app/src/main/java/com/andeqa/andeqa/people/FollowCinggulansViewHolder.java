package com.andeqa.andeqa.people;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.andeqa.andeqa.R;
import com.andeqa.andeqa.models.Andeqan;
import com.google.firebase.firestore.DocumentSnapshot;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by J.EL on 12/12/2017.
 */

public class FollowCinggulansViewHolder extends RecyclerView.ViewHolder{
    View mView;
    Context mContext;
    public CircleImageView mCinggulanImageView;
    public TextView mFullNameTextView;
    public TextView mBioTextView;
    public ImageView mProfileCoverImageView;
    public Button mFollowButton;
    public RelativeLayout mFollowButtonRelativeLayout;
    public RelativeLayout mBioRelativeLayout;

    public FollowCinggulansViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
        mContext = itemView.getContext();

    }

    public void bindCinggulans(final DocumentSnapshot documentSnapshot){
        final Andeqan andeqan = documentSnapshot.toObject(Andeqan.class);
        mCinggulanImageView = (CircleImageView) mView.findViewById(R.id.profileImageView);
        mBioTextView = (TextView) mView.findViewById(R.id.bioTextView);
        mFullNameTextView = (TextView) mView.findViewById(R.id.fullNameTextView);
        mProfileCoverImageView = (ImageView) mView.findViewById(R.id.profileCoverImageView);
        mFollowButton = (Button) mView.findViewById(R.id.followButton);
        mBioRelativeLayout = (RelativeLayout) mView.findViewById(R.id.bioRelativeLayout);
        mFollowButtonRelativeLayout = (RelativeLayout) mView.findViewById(R.id.followButtonRelativeLayout);

        Picasso.with(mContext)
                .load(andeqan.getProfileImage())
                .networkPolicy(NetworkPolicy.OFFLINE)
                .into(mCinggulanImageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        Log.v("Picasso", "Fetched image");
                    }

                    @Override
                    public void onError() {
                        Picasso.with(mContext)
                                .load(andeqan.getProfileImage())
                                .into(mCinggulanImageView, new Callback() {
                                    @Override
                                    public void onSuccess() {

                                    }

                                    @Override
                                    public void onError() {
                                        Log.v("Picasso", "Could not fetch image");
                                    }
                                });


                    }
                });

        if (andeqan.getProfileCover() != null){
            Picasso.with(mContext)
                    .load(andeqan.getProfileCover())
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .into(mProfileCoverImageView, new Callback() {
                        @Override
                        public void onSuccess() {
                            Log.v("Picasso", "Fetched image");
                        }

                        @Override
                        public void onError() {
                            Picasso.with(mContext)
                                    .load(andeqan.getProfileCover())
                                    .into(mProfileCoverImageView, new Callback() {
                                        @Override
                                        public void onSuccess() {

                                        }

                                        @Override
                                        public void onError() {
                                            Log.v("Picasso", "Could not fetch image");
                                        }
                                    });


                        }
                    });
        }

        mFullNameTextView.setText(andeqan.getUsername());

        final String bio = andeqan.getBio();
        if (TextUtils.isEmpty(bio)){
            mBioRelativeLayout.setVisibility(View.GONE);
        }else {
            mBioRelativeLayout.setVisibility(View.VISIBLE);
            mBioTextView.setText(andeqan.getBio());
        }


    }
}

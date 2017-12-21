package com.cinggl.cinggl.viewholders;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cinggl.cinggl.App;
import com.cinggl.cinggl.R;
import com.cinggl.cinggl.models.Cinggulan;
import com.cinggl.cinggl.models.Relation;
import com.google.firebase.firestore.DocumentSnapshot;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;
import static com.cinggl.cinggl.R.id.bioRelativeLayout;
import static com.cinggl.cinggl.R.id.descriptionRelativeLayout;
import static com.cinggl.cinggl.R.id.descriptionTextView;
import static com.cinggl.cinggl.R.id.postImageView;
import static com.cinggl.cinggl.R.id.titleRelativeLayout;
import static com.cinggl.cinggl.R.id.titleTextView;

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
        final Cinggulan cinggulan = documentSnapshot.toObject(Cinggulan.class);
        mCinggulanImageView = (CircleImageView) mView.findViewById(R.id.creatorImageView);
        mBioTextView = (TextView) mView.findViewById(R.id.bioTextView);
        mFullNameTextView = (TextView) mView.findViewById(R.id.fullNameTextView);
        mProfileCoverImageView = (ImageView) mView.findViewById(R.id.profileCoverImageView);
        mFollowButton = (Button) mView.findViewById(R.id.followButton);
        mBioRelativeLayout = (RelativeLayout) mView.findViewById(R.id.bioRelativeLayout);
        mFollowButtonRelativeLayout = (RelativeLayout) mView.findViewById(R.id.followButtonRelativeLayout);

        Picasso.with(mContext)
                .load(cinggulan.getProfileImage())
                .networkPolicy(NetworkPolicy.OFFLINE)
                .into(mCinggulanImageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        Log.v("Picasso", "Fetched image");
                    }

                    @Override
                    public void onError() {
                        Picasso.with(mContext)
                                .load(cinggulan.getProfileImage())
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

        if (cinggulan.getProfileCover() != null){
            Picasso.with(mContext)
                    .load(cinggulan.getProfileCover())
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .into(mProfileCoverImageView, new Callback() {
                        @Override
                        public void onSuccess() {
                            Log.v("Picasso", "Fetched image");
                        }

                        @Override
                        public void onError() {
                            Picasso.with(mContext)
                                    .load(cinggulan.getProfileCover())
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

        mFullNameTextView.setText(cinggulan.getUsername());

        final String bio = cinggulan.getBio();
        if (TextUtils.isEmpty(bio)){
            mBioRelativeLayout.setVisibility(View.GONE);
        }else {
            mBioRelativeLayout.setVisibility(View.VISIBLE);
            mBioTextView.setText(cinggulan.getBio());
        }


    }
}

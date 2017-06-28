package com.cinggl.cinggl.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.cinggl.cinggl.R;
import com.cinggl.cinggl.models.Cingle;
import com.cinggl.cinggl.models.Cingulan;
import com.cinggl.cinggl.profile.UpdateProfileActivity;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by J.EL on 6/10/2017.
 */

public class ProfileInfoViewHolder extends RecyclerView.ViewHolder {

    View mView;
    Context mContext;
    public Button editProfileButton;
    public ImageView sendMessageImageView;

    public ProfileInfoViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
        mContext = itemView.getContext();

    }

    public void bindProfileInfo(final Cingulan cingulan) {
        final CircleImageView profilePictureImageView = (CircleImageView) mView.findViewById(R.id.profilePictureImageView);
        TextView accountUsernameTextView = (TextView) mView.findViewById(R.id.accountUsernameTextView);
        TextView bioTextView = (TextView) mView.findViewById(R.id.bioTextView);
        TextView firstNameTextView = (TextView) mView.findViewById(R.id.firstNameTextView);
        TextView secondNameTextView = (TextView) mView.findViewById(R.id.secondNameTextView);
//        TextView followersCountTextView = (TextView) mView.findViewById(R.id.followersCountTextView);
//        TextView followingCountTextView = (TextView) mView.findViewById(R.id.followingCountTextView);

        Picasso.with(mContext)
                .load(cingulan.getProfileImage())
                .fit()
                .centerCrop()
                .networkPolicy(NetworkPolicy.OFFLINE)
                .into(profilePictureImageView, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {
                        Picasso.with(mContext)
                                .load(cingulan.getProfileImage())
                                .fit()
                                .centerCrop()
                                .into(profilePictureImageView);

                    }
                });

        bioTextView.setText(cingulan.getBio());
        firstNameTextView.setText(cingulan.getFirstName());
        secondNameTextView.setText(cingulan.getSecondName());
    }


}
package com.cinggl.cinggl.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.cinggl.cinggl.R;
import com.cinggl.cinggl.models.Like;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by J.EL on 8/17/2017.
 */

public class UsersWhoLiked extends RecyclerView.ViewHolder {
    View mView;
    Context mContext;
    public CircleImageView usersWhoLikedProfileImageView;

    public UsersWhoLiked(View itemView) {
        super(itemView);
        mView = itemView;
        mContext = itemView.getContext();
        usersWhoLikedProfileImageView = (CircleImageView ) mView.findViewById(R.id.usersWhoLikedProfileImageView);

    }

    public void bindUsersWhoLiked(final Like like){
        final CircleImageView profileImageView = (CircleImageView) mView.findViewById(R.id.usersWhoLikedProfileImageView);

        Picasso.with(mContext)
                .load(like.getProfileImage())
                .fit()
                .centerCrop()
                .placeholder(R.drawable.profle_image_background)
                .networkPolicy(NetworkPolicy.OFFLINE)
                .into(profileImageView, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {
                        Picasso.with(mContext)
                                .load(like.getProfileImage())
                                .fit()
                                .centerCrop()
                                .placeholder(R.drawable.profle_image_background)
                                .into(profileImageView);


                    }
                });

    }

}

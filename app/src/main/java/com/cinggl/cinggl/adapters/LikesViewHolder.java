package com.cinggl.cinggl.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.cinggl.cinggl.R;
import com.cinggl.cinggl.models.Like;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by J.EL on 6/25/2017.
 */

public class LikesViewHolder extends RecyclerView.ViewHolder {

    View mView;
    Context mContext;
    private ImageView userProfileImageView;
    private TextView usernameTextView;

    public LikesViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
        mContext = itemView.getContext();
        usernameTextView = (TextView)itemView.findViewById(R.id.accountUsernameTextView);
    }

    public void bindLikes(final Like like){
        TextView usernameTextView =(TextView) mView.findViewById(R.id.accountUsernameTextView);
        final CircleImageView userProfileImageView = (CircleImageView) mView.findViewById(R.id.userProfileImageView);

        usernameTextView.setText(like.getUsername());

        Picasso.with(mContext)
                .load(like.getProfileImage())
                .fit()
                .centerCrop()
                .placeholder(R.drawable.profle_image_background)
                .networkPolicy(NetworkPolicy.OFFLINE)
                .into(userProfileImageView, new Callback() {
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
                                .into(userProfileImageView);


                    }
                });

    }

}

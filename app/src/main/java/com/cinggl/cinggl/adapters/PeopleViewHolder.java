package com.cinggl.cinggl.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.cinggl.cinggl.R;
import com.cinggl.cinggl.models.Cingulan;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by J.EL on 7/3/2017.
 */

public class PeopleViewHolder extends RecyclerView.ViewHolder {
    View mView;
    Context mContext;
    public TextView firstNameTextView;
    public TextView secondNameTextView;
    public CircleImageView profileImageView;
    public Button followButton;

    public PeopleViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
        mContext = itemView.getContext();
        firstNameTextView = (TextView) itemView.findViewById(R.id.firstNameTextView);
        secondNameTextView = (TextView) itemView.findViewById(R.id.secondNameTextView);
        profileImageView = (CircleImageView) itemView.findViewById(R.id.profileImageView);
        followButton = (Button) itemView.findViewById(R.id.followButton);
    }

    public void bindPeople(final Cingulan cingulan){
        final CircleImageView profileImageView = (CircleImageView) mView.findViewById(R.id.profileImageView);
        final TextView firstNameTextView = (TextView) mView.findViewById(R.id.firstNameTextView);
        final TextView secondNameTextView = (TextView) mView.findViewById(R.id.secondNameTextView);

        firstNameTextView.setText(cingulan.getFirstName());
        secondNameTextView.setText(cingulan.getSecondName());

        Picasso.with(mContext)
                .load(cingulan.getProfileImage())
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
                                .load(cingulan.getProfileImage())
                                .fit()
                                .centerCrop()
                                .placeholder(R.drawable.profle_image_background)
                                .into(profileImageView);


                    }
                });
    }
}

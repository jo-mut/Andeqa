package com.cinggl.cinggl.viewholders;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.cinggl.cinggl.R;
import com.cinggl.cinggl.models.Cingulan;
import com.cinggl.cinggl.models.Relation;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.cinggl.cinggl.R.id.secondNameTextView;

/**
 * Created by J.EL on 7/3/2017.
 */

public class PeopleViewHolder extends RecyclerView.ViewHolder {
    View mView;
    Context mContext;
    public TextView fullNameTextView;
    public CircleImageView profileImageView;
    public Button followButton;
    public TextView usernameTextView;

    public PeopleViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
        mContext = itemView.getContext();
        fullNameTextView = (TextView) itemView.findViewById(R.id.fullNameTextView);
        profileImageView = (CircleImageView) itemView.findViewById(R.id.creatorImageView);
        followButton = (Button) itemView.findViewById(R.id.followButton);
        usernameTextView = (TextView) itemView.findViewById(R.id.usernameTextView);
    }

    public void bindPeople(final Relation relation){


    }
}

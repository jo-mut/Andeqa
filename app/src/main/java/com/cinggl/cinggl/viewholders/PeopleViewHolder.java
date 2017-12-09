package com.cinggl.cinggl.viewholders;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.cinggl.cinggl.R;
import com.cinggl.cinggl.models.Relation;

import de.hdodenhof.circleimageview.CircleImageView;

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
        followButton = (Button) itemView.findViewById(R.id.followTextView);
        usernameTextView = (TextView) itemView.findViewById(R.id.usernameTextView);
    }

    public void bindPeople(final Relation relation){


    }
}

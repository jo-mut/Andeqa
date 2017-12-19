package com.cinggl.cinggl.viewholders;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cinggl.cinggl.utils.ProportionalImageView;
import com.cinggl.cinggl.R;
import com.google.firebase.firestore.DocumentSnapshot;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * Created by J.EL on 6/8/2017.
 */

public class ProfilePostsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    View mView;
    Context mContext;
    public ImageView likesImageView;
    public ImageView commentsImageView;
    public TextView likesCountTextView;
    public TextView cingleTitleTextView;
    public TextView cingleDescriptionTextView;
    public TextView accountUsernameTextView;
    public CircleImageView profileImageView;
    public TextView commentsCountTextView;
    public ImageView cingleSettingsImageView;
    public RelativeLayout cingleTitleRelativeLayout;
    public RelativeLayout descriptionRelativeLayout;
    public ProportionalImageView cingleImageView;
    public TextView cingleTradeMethodTextView;
    public TextView cingleSenseCreditsTextView;
    public RecyclerView likesRecyclerView;


    public ProfilePostsViewHolder(View itemView){
        super(itemView);
        mView = itemView;
        mContext = itemView.getContext();
        likesImageView = (ImageView) itemView.findViewById(R.id.likesImageView);
        likesCountTextView =(TextView)itemView.findViewById(R.id.likesCountTextView);
        commentsImageView = (ImageView) itemView.findViewById(R.id.commentsImageView);
        cingleDescriptionTextView = (TextView) itemView.findViewById(R.id.descriptionTextView);
        cingleTitleTextView = (TextView) itemView.findViewById(R.id.titleTextView);
        accountUsernameTextView = (TextView) itemView.findViewById(R.id.usernameTextView);
        profileImageView = (CircleImageView) itemView.findViewById(R.id.creatorImageView);
        commentsCountTextView = (TextView) itemView.findViewById(R.id.commentsCountTextView);
        cingleSettingsImageView = (ImageView) itemView.findViewById(R.id.settingsImageView);
        cingleTitleRelativeLayout = (RelativeLayout) itemView.findViewById(R.id.cingleTitleRelativeLayout);
        descriptionRelativeLayout  = (RelativeLayout) mView.findViewById(R.id.descriptionRelativeLayout);
        cingleImageView = (ProportionalImageView) mView.findViewById(R.id.postImageView);
        cingleSenseCreditsTextView = (TextView) mView.findViewById(R.id.postSenseCreditsTextView);
        cingleTradeMethodTextView = (TextView) mView.findViewById(R.id.tradeMethodTextView);
        likesRecyclerView = (RecyclerView) mView.findViewById(R.id.likesRecyclerView);

        likesImageView.setOnClickListener(this);
        commentsImageView.setOnClickListener(this);
        likesCountTextView.setOnClickListener(this);


    }

    public void bindProfileCingle(final DocumentSnapshot documentSnapshot){


    }

    @Override
    public  void onClick(View v){


    }

}

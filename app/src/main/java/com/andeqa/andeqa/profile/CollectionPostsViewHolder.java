package com.andeqa.andeqa.profile;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.andeqa.andeqa.utils.ProportionalImageView;
import com.andeqa.andeqa.R;
import com.google.firebase.firestore.DocumentSnapshot;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * Created by J.EL on 6/8/2017.
 */

public class CollectionPostsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    View mView;
    Context mContext;
    public ImageView likesImageView;
    public ImageView commentsImageView;
    public TextView likesPercentageTextView;
    public TextView dislikePercentageTextView;
    public TextView cingleTitleTextView;
    public TextView cingleDescriptionTextView;
    public TextView accountUsernameTextView;
    public CircleImageView profileImageView;
    public TextView commentsCountTextView;
    public ImageView settingsImageView;
    public RelativeLayout cingleTitleRelativeLayout;
    public RelativeLayout descriptionRelativeLayout;
    public ProportionalImageView postImageView;
    public TextView tradeMethodTextView;
    public TextView cingleSenseCreditsTextView;
    public RecyclerView likesRecyclerView;
    public TextView totalLikesCountTextView;
    public ImageView dislikeImageView;
    public RelativeLayout likesRelativeLayout;


    public CollectionPostsViewHolder(View itemView){
        super(itemView);
        mView = itemView;
        mContext = itemView.getContext();
        likesImageView = (ImageView) itemView.findViewById(R.id.likesImageView);
        dislikeImageView = (ImageView) itemView.findViewById(R.id.dislikesImageView);
        likesPercentageTextView =(TextView)itemView.findViewById(R.id.likesPercentageTextView);
        dislikePercentageTextView = (TextView) itemView.findViewById(R.id.dislikesPercentageTextView);
        commentsImageView = (ImageView) itemView.findViewById(R.id.commentsImageView);
        cingleDescriptionTextView = (TextView) itemView.findViewById(R.id.descriptionTextView);
        cingleTitleTextView = (TextView) itemView.findViewById(R.id.titleTextView);
        accountUsernameTextView = (TextView) itemView.findViewById(R.id.usernameTextView);
        profileImageView = (CircleImageView) itemView.findViewById(R.id.profileImageView);
        commentsCountTextView = (TextView) itemView.findViewById(R.id.commentsCountTextView);
        settingsImageView = (ImageView) itemView.findViewById(R.id.settingsImageView);
        cingleTitleRelativeLayout = (RelativeLayout) itemView.findViewById(R.id.cingleTitleRelativeLayout);
        descriptionRelativeLayout  = (RelativeLayout) mView.findViewById(R.id.descriptionRelativeLayout);
        postImageView = (ProportionalImageView) mView.findViewById(R.id.postImageView);
        cingleSenseCreditsTextView = (TextView) mView.findViewById(R.id.postSenseCreditsTextView);
        tradeMethodTextView = (TextView) mView.findViewById(R.id.tradeMethodTextView);
        likesRecyclerView = (RecyclerView) mView.findViewById(R.id.likesRecyclerView);
        likesRelativeLayout = (RelativeLayout) mView.findViewById(R.id.likesRelativeLayout);
        totalLikesCountTextView = (TextView) mView.findViewById(R.id.totalLikesCountTextView);

    }

    public void bindProfileCingle(final DocumentSnapshot documentSnapshot){


    }

    @Override
    public  void onClick(View v){


    }

}

package com.cinggl.cinggl.viewholders;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cinggl.cinggl.R;
import com.cinggl.cinggl.ProportionalImageView;
import com.cinggl.cinggl.models.Credit;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by J.EL on 7/6/2017.
 */

public class BestPostsViewHolder extends RecyclerView.ViewHolder {

    View mView;
    Context mContext;
    ProgressBar progressBar;
    public ImageView likesImageView;
    public ImageView commentsImageView;
    public TextView likesCountTextView;
    public TextView titleTextView;
    public TextView descriptionTextView;
    public CircleImageView profileImageView;
    public TextView usernameTextView;
    public TextView commentsCountTextView;
    public ImageView settingsImageView;
    public TextView tradeMethodTextView;
    public TextView senseCreditsTextView;
    public ProportionalImageView postImageView;
    public RelativeLayout toolsRelativeLayout;
    public static final int MAX_WIDTH = 400;
    public static final int MAX_HEIGHT = 400;
    public RelativeLayout titleRelativeLayout;
    public RelativeLayout descriptionRelativeLayout;
    public TextView postMomentTextView;
    public RelativeLayout postMomentRelativeLayout;
    public RecyclerView likesRecyclerView;
    public RelativeLayout postTradingRelativeLayout;
    public TextView postOwnerTextView;
    public CircleImageView ownerImageView;
    public TextView postSalePriceTextView;
    public RelativeLayout postSalePriceTitleRelativeLayout;
    public TextView datePostedTextView;


    public BestPostsViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
        mContext = itemView.getContext();
        likesImageView = (ImageView) itemView.findViewById(R.id.likesImageView);
        likesCountTextView =(TextView)itemView.findViewById(R.id.likesCountTextView);
        commentsImageView = (ImageView) itemView.findViewById(R.id.commentsImageView);
        descriptionTextView = (TextView) itemView.findViewById(R.id.descriptionTextView);
        titleTextView = (TextView) itemView.findViewById(R.id.titleTextView);
        profileImageView = (CircleImageView) itemView.findViewById(R.id.creatorImageView);
        commentsCountTextView = (TextView) itemView.findViewById(R.id.commentsCountTextView);
        usernameTextView = (TextView) itemView.findViewById(R.id.usernameTextView);
        settingsImageView = (ImageView) mView.findViewById(R.id.settingsImageView);
        toolsRelativeLayout = (RelativeLayout) mView.findViewById(R.id.toolsRelativeLayout);
        titleRelativeLayout = (RelativeLayout) mView.findViewById(R.id.titleRelativeLayout);
        descriptionRelativeLayout = (RelativeLayout) mView.findViewById(R.id.descriptionRelativeLayout);
        postImageView = (ProportionalImageView) mView.findViewById(R.id.postImageView);
        tradeMethodTextView = (TextView) mView.findViewById(R.id.tradeMethodTextView);
        postMomentTextView = (TextView) mView.findViewById(R.id.postMomentTextView);
        postMomentRelativeLayout = (RelativeLayout) mView.findViewById(R.id.postMomentRelativeLayout);
        likesRecyclerView = (RecyclerView) mView.findViewById(R.id.likesRecyclerView);
        postTradingRelativeLayout = (RelativeLayout) mView.findViewById(R.id.tradingRelativeLayout);
        postOwnerTextView = (TextView) mView.findViewById(R.id.postOwnerTextView);
        ownerImageView = (CircleImageView) mView.findViewById(R.id.ownerImageView);
        postSalePriceTextView = (TextView) mView.findViewById(R.id.postSalePriceTextView);
        postSalePriceTitleRelativeLayout = (RelativeLayout) mView.findViewById(R.id.postSalePriceTitleRelativeLayout);
        datePostedTextView = (TextView) mView.findViewById(R.id.datePostedTextView);
        senseCreditsTextView = (TextView) mView.findViewById(R.id.postSenseCreditsTextView);

    }

    public void bindBestCingle(final Credit credit){
        senseCreditsTextView = (TextView) mView.findViewById(R.id.postSenseCreditsTextView);
        senseCreditsTextView.setText(credit.getAmount() + "");


    }
}

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
import com.cinggl.cinggl.models.Credits;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by J.EL on 7/6/2017.
 */

public class BestCinglesViewHolder extends RecyclerView.ViewHolder {

    View mView;
    Context mContext;
    ProgressBar progressBar;
    public ImageView likesImageView;
    public ImageView commentsImageView;
    public TextView likesCountTextView;
    public TextView cingleTitleTextView;
    public TextView cingleDescriptionTextView;
    public CircleImageView profileImageView;
    public TextView usernameTextView;
    public TextView commentsCountTextView;
    public ImageView cingleSettingsImageView;
    public TextView cingleTradeMethodTextView;
    public TextView cingleSenseCreditsTextView;
    public ProportionalImageView cingleImageView;
    public RelativeLayout cingleToolsRelativeLayout;
    public static final int MAX_WIDTH = 400;
    public static final int MAX_HEIGHT = 400;
    public RelativeLayout cingleTitleRelativeLayout;
    public RelativeLayout descriptionRelativeLayout;
    public TextView cingleMomentTextView;
    public RelativeLayout cingleMomentRelativeLayout;
    public RecyclerView likesRecyclerView;
    public RelativeLayout cingleTradingRelativeLayout;
    public TextView cingleOwnerTextView;
    public CircleImageView ownerImageView;
    public TextView cingleSalePriceTextView;
    public RelativeLayout cingleSalePriceTitleRelativeLayout;
    public TextView datePostedTextView;


    public BestCinglesViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
        mContext = itemView.getContext();
        likesImageView = (ImageView) itemView.findViewById(R.id.likesImageView);
        likesCountTextView =(TextView)itemView.findViewById(R.id.likesCountTextView);
        commentsImageView = (ImageView) itemView.findViewById(R.id.commentsImageView);
        cingleDescriptionTextView = (TextView) itemView.findViewById(R.id.cingleDescriptionTextView);
        cingleTitleTextView = (TextView) itemView.findViewById(R.id.cingleTitleTextView);
        profileImageView = (CircleImageView) itemView.findViewById(R.id.creatorImageView);
        commentsCountTextView = (TextView) itemView.findViewById(R.id.commentsCountTextView);
        usernameTextView = (TextView) itemView.findViewById(R.id.usernameTextView);
        cingleSettingsImageView = (ImageView) mView.findViewById(R.id.cingleSettingsImageView);
        cingleToolsRelativeLayout = (RelativeLayout) mView.findViewById(R.id.cingleToolsRelativeLayout);
        cingleTitleRelativeLayout = (RelativeLayout) mView.findViewById(R.id.titleRelativeLayout);
        descriptionRelativeLayout = (RelativeLayout) mView.findViewById(R.id.descriptionRelativeLayout);
        cingleImageView = (ProportionalImageView) mView.findViewById(R.id.cingleImageView);
        cingleSenseCreditsTextView = (TextView) mView.findViewById(R.id.cingleSenseCreditsCountTextView);
        cingleTradeMethodTextView = (TextView) mView.findViewById(R.id.cingleTradeMethodTextView);
        cingleMomentTextView = (TextView) mView.findViewById(R.id.cingleMomentTextView);
        cingleMomentRelativeLayout = (RelativeLayout) mView.findViewById(R.id.cingleMomentRelativeLayout);
        likesRecyclerView = (RecyclerView) mView.findViewById(R.id.likesRecyclerView);
        cingleTradingRelativeLayout = (RelativeLayout) mView.findViewById(R.id.cingleTradingRelativeLayout);
        cingleOwnerTextView = (TextView) mView.findViewById(R.id.cingleOwnerTextView);
        ownerImageView = (CircleImageView) mView.findViewById(R.id.ownerImageView);
        cingleSalePriceTextView = (TextView) mView.findViewById(R.id.cingleSalePriceTextView);
        cingleSalePriceTitleRelativeLayout = (RelativeLayout) mView.findViewById(R.id.cingleSalePriceTitleRelativeLayout);
        datePostedTextView = (TextView) mView.findViewById(R.id.datePostedTextView);
        cingleSenseCreditsTextView = (TextView) mView.findViewById(R.id.cingleSenseCreditsTextView);

    }

    public void bindBestCingle(final Credits credits){


    }
}

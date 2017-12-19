package com.cinggl.cinggl.viewholders;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cinggl.cinggl.utils.ProportionalImageView;
import com.cinggl.cinggl.R;
import com.cinggl.cinggl.models.PostSale;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by J.EL on 9/14/2017.
 */

public class PostSellingViewHolder extends RecyclerView.ViewHolder {
    View mView;
    Context mContext;
    ProgressBar progressBar;
    public CircleImageView creatorImageView;
    public TextView usernameTextView;
    public TextView cingleTradeMethodTextView;
    public TextView cingleSenseCreditsTextView;
    public ProportionalImageView cingleImageView;
    public RelativeLayout cingleTradingRelativeLayout;
    public TextView cingleOwnerTextView;
    public CircleImageView ownerImageView;
    public TextView datePostedTextView;
    public TextView cingleSalePriceTextView;
    public LinearLayout ifairCinglesLinearLayout;
    public TextView timeTextView;

    public PostSellingViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
        mContext = itemView.getContext();
        creatorImageView = (CircleImageView) itemView.findViewById(R.id.creatorImageView);
        cingleImageView = (ProportionalImageView) mView.findViewById(R.id.postImageView);
        cingleSenseCreditsTextView = (TextView) mView.findViewById(R.id.senseCreditsCountTextView);
        cingleTradeMethodTextView = (TextView) mView.findViewById(R.id.tradeMethodTextView);
        cingleTradingRelativeLayout = (RelativeLayout) mView.findViewById(R.id.tradingRelativeLayout);
        cingleOwnerTextView = (TextView) mView.findViewById(R.id.postOwnerTextView);
        ownerImageView = (CircleImageView) mView.findViewById(R.id.ownerImageView);
        datePostedTextView = (TextView) mView.findViewById(R.id.datePostedTextView);
        usernameTextView = (TextView) mView.findViewById(R.id.usernameTextView);
        cingleSalePriceTextView = (TextView) mView.findViewById(R.id.postSalePriceTextView);
        ifairCinglesLinearLayout = (LinearLayout) mView.findViewById(R.id.ifair_cingles_linear_layout);


    }

    public void bindIfairCingle(final PostSale postSale){



    }
}

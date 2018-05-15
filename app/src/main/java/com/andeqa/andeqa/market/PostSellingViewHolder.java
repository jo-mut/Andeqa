package com.andeqa.andeqa.market;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.andeqa.andeqa.R;
import com.andeqa.andeqa.models.Market;

/**
 * Created by J.EL on 9/14/2017.
 */

public class PostSellingViewHolder extends RecyclerView.ViewHolder {
    View mView;
    Context mContext;
    public TextView creatorTextView;
    public ImageView postImageView;
    public TextView ownerTextView;
    public TextView salePriceTextView;
    public LinearLayout ifairCinglesLinearLayout;


    public PostSellingViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
        mContext = itemView.getContext();
        postImageView = (ImageView) mView.findViewById(R.id.postImageView);
        ownerTextView = (TextView) mView.findViewById(R.id.ownerTextView);
        creatorTextView = (TextView) mView.findViewById(R.id.creatorTextView);
        salePriceTextView = (TextView) mView.findViewById(R.id.postPriceTextView);
        ifairCinglesLinearLayout = (LinearLayout) mView.findViewById(R.id.ifair_cingles_linear_layout);
    }

    public void bindIfairCingle(final Market market){



    }
}

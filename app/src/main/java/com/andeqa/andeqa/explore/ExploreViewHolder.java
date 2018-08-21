package com.andeqa.andeqa.explore;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.andeqa.andeqa.R;
import com.andeqa.andeqa.models.Market;
import com.andeqa.andeqa.utils.ProportionalImageView;

/**
 * Created by J.EL on 9/14/2017.
 */

public class ExploreViewHolder extends RecyclerView.ViewHolder {
    View mView;
    Context mContext;
    public TextView creatorTextView;
    public ProportionalImageView postImageView;
    public TextView ownerTextView;
    public TextView salePriceTextView;
    public LinearLayout exploreLinearLayout;


    public ExploreViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
        mContext = itemView.getContext();
        postImageView = (ProportionalImageView) mView.findViewById(R.id.postImageView);
        exploreLinearLayout = (LinearLayout) mView.findViewById(R.id.explore_linear_layout);
    }

    public void bindIfairCingle(final Market market){



    }
}

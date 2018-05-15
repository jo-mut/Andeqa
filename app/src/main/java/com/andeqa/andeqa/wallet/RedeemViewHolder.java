package com.andeqa.andeqa.wallet;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.andeqa.andeqa.R;
import com.andeqa.andeqa.utils.ProportionalImageView;

import butterknife.ButterKnife;

public class RedeemViewHolder extends RecyclerView.ViewHolder {
    View mView;
    Context mContext;
    public ImageView postImageView;
    public TextView amountTransferredTextView;
    public ImageView deleteHistoryImageView;

    public RedeemViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
        mContext = itemView.getContext();
        ButterKnife.bind(this, mView);
        postImageView = (ImageView) mView.findViewById(R.id.postImageView);
        deleteHistoryImageView = (ImageView) mView.findViewById(R.id.deleteHistoryImageView);
        amountTransferredTextView = (TextView) mView.findViewById(R.id.transactionsHistoryTextView);


    }
}

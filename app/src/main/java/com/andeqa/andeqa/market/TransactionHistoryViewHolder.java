package com.andeqa.andeqa.market;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.andeqa.andeqa.utils.ProportionalImageView;
import com.andeqa.andeqa.R;
import com.andeqa.andeqa.models.TransactionDetails;

import java.text.DecimalFormat;


/**
 * Created by J.EL on 9/8/2017.
 */

public class TransactionHistoryViewHolder extends RecyclerView.ViewHolder {

    View mView;
    Context mContext;
    public ProportionalImageView postImageView;
    public TextView amountTransferredTextView;
    public ImageView deleteHistoryImageView;


    public TransactionHistoryViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
        mContext = itemView.getContext();
        postImageView = (ProportionalImageView) mView.findViewById(R.id.postImageView);
        deleteHistoryImageView = (ImageView) mView.findViewById(R.id.deleteHistoryImageView);


    }

}

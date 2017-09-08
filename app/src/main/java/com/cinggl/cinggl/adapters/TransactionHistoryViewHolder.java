package com.cinggl.cinggl.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.cinggl.cinggl.ProportionalImageView;
import com.cinggl.cinggl.R;
import com.cinggl.cinggl.models.TransactionDetails;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.V;
import static com.cinggl.cinggl.R.id.cingleImageView;

/**
 * Created by J.EL on 9/8/2017.
 */

public class TransactionHistoryViewHolder extends RecyclerView.ViewHolder {

    View mView;
    Context mContext;
    public ProportionalImageView cingleImageView;
    public TextView amountTransferredTextView;
    public TextView transactionDateTextView;
    public TextView newWalletBalanceTextView;

    public TransactionHistoryViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
        mContext = itemView.getContext();
        newWalletBalanceTextView = (TextView) mView.findViewById(R.id.newWalletBalanceTextView);
        cingleImageView = (ProportionalImageView) mView.findViewById(R.id.cingleImageView);


    }

    public void bindTransactionHistory(final TransactionDetails transactionDetails){
        TextView amountTransferredTextView = (TextView) mView.findViewById(R.id.amountTransferredTextView);
        TextView transactionDateTextView = (TextView) mView.findViewById(R.id.transactionDateTextView);

        amountTransferredTextView.setText(Double.toString(transactionDetails.getAmount()));
        transactionDateTextView.setText(transactionDetails.getDatePosted());
    }
}

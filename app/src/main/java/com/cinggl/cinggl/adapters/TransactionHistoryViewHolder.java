package com.cinggl.cinggl.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.cinggl.cinggl.ProportionalImageView;
import com.cinggl.cinggl.R;
import com.cinggl.cinggl.models.TransactionDetails;

import java.text.DecimalFormat;

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
    public ImageView deleteHistoryImageView;


    public TransactionHistoryViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
        mContext = itemView.getContext();
        newWalletBalanceTextView = (TextView) mView.findViewById(R.id.newWalletBalanceTextView);
        cingleImageView = (ProportionalImageView) mView.findViewById(R.id.cingleImageView);
        deleteHistoryImageView = (ImageView) mView.findViewById(R.id.deleteHistoryImageView);


    }

    public void bindTransactionHistory(final TransactionDetails transactionDetails){
        TextView amountTransferredTextView = (TextView) mView.findViewById(R.id.amountTransferredTextView);
        TextView transactionDateTextView = (TextView) mView.findViewById(R.id.transactionDateTextView);
        TextView newWalletBalanceTextView = (TextView) mView.findViewById(R.id.newWalletBalanceTextView);

        DecimalFormat formatter =  new DecimalFormat("0.00000000");
        amountTransferredTextView.setText("Amount transferred:" + " " + "CSC" + " " + formatter
                .format(transactionDetails.getAmount()));
        transactionDateTextView.setText("Date of transaction:" + "" + transactionDetails.getDate());
        newWalletBalanceTextView.setText("Your new wallet balance: is" + " "+ "CSC" + " " + formatter
                .format(transactionDetails.getWalletBalance()));
    }
}

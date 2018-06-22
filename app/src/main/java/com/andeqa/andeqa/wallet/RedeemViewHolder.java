package com.andeqa.andeqa.wallet;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.andeqa.andeqa.R;

public class RedeemViewHolder extends RecyclerView.ViewHolder {

    View mView;
    Context mContext;
    public TextView creditsTextView;
    public Button redeemButton;
    public ImageView postImageView;
    public EditText redeemCreditEditText;

    public RedeemViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
        mContext = itemView.getContext();
        creditsTextView = (TextView) itemView.findViewById(R.id.creditsTextView);
        redeemCreditEditText = (EditText) itemView.findViewById(R.id.redeemCreditEditText);
        postImageView = (ImageView) itemView.findViewById(R.id.postImageView);
        redeemButton = (Button) itemView.findViewById(R.id.redeemButton);
    }
}

package com.andeqa.andeqa.search;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.andeqa.andeqa.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class SearchCountViewHolder extends RecyclerView.ViewHolder{
    View mView;
    Context mContext;
    public TextView searchWordTextView;
    public TextView searchCountTextVieww;


    public SearchCountViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
        mContext = itemView.getContext();
        searchWordTextView = (TextView) itemView.findViewById(R.id.searchWordTextView);
        searchCountTextVieww = (TextView) itemView.findViewById(R.id.searchCountTextView);
    }
}

package com.andeqa.andeqa.home;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.andeqa.andeqa.R;
import com.andeqa.andeqa.utils.ProportionalImageView;

import java.math.BigDecimal;
import java.math.RoundingMode;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by J.EL on 2/13/2018.
 */

public class PostViewHolder extends RecyclerView.ViewHolder {

    View mView;
    Context mContext;
    ProgressBar progressBar;
    public ImageView likesImageView;
    public ImageView dislikeImageView;
    public TextView dislikeCountTextView;
    public ImageView commentsImageView;
    public TextView likesCountTextView;
    public TextView titleTextView;
    public TextView descriptionTextView;
    public TextView usernameTextView;
    public CircleImageView profileImageView;
    public TextView commentsCountTextView;
    public TextView senseCreditsTextView;
    public TextView mTradeButton;
    public RelativeLayout titleRelativeLayout;
    public ProportionalImageView postImageView;
    public static final int MAX_WIDTH = 400;
    public static final int MAX_HEIGHT = 400;
    public RelativeLayout descriptionRelativeLayout;
    public RelativeLayout likesRelativeLayout;



    public PostViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
        mContext = itemView.getContext();
        likesImageView = (ImageView) itemView.findViewById(R.id.likesImageView);
        dislikeImageView = (ImageView) itemView.findViewById(R.id.dislikesImageView);
        dislikeCountTextView = (TextView) itemView.findViewById(R.id.dislikesCountTextView);
        commentsImageView = (ImageView) itemView.findViewById(R.id.commentsImageView);
        descriptionTextView = (TextView) itemView.findViewById(R.id.descriptionTextView);
        titleTextView = (TextView) itemView.findViewById(R.id.titleTextView);
        usernameTextView = (TextView) itemView.findViewById(R.id.usernameTextView);
        profileImageView = (CircleImageView) itemView.findViewById(R.id.profileImageView);
        commentsCountTextView = (TextView) itemView.findViewById(R.id.commentsCountTextView);
        titleRelativeLayout = (RelativeLayout) itemView.findViewById(R.id.titleRelativeLayout);
        descriptionRelativeLayout  = (RelativeLayout) mView.findViewById(R.id.descriptionRelativeLayout);
        postImageView = (ProportionalImageView) mView.findViewById(R.id.postImageView);
        senseCreditsTextView = (TextView) mView.findViewById(R.id.creditsTextView);
        mTradeButton = (TextView) mView.findViewById(R.id.tradeButton);
        likesRelativeLayout = (RelativeLayout) mView.findViewById(R.id.likesRelativeLayout);
        likesCountTextView = (TextView) mView.findViewById(R.id.likesCountTextView);
    }


    private static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}

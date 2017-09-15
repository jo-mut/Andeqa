package com.cinggl.cinggl.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cinggl.cinggl.ProportionalImageView;
import com.cinggl.cinggl.R;
import com.cinggl.cinggl.models.Cingle;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.cinggl.cinggl.R.id.cingleImageView;
import static com.cinggl.cinggl.R.id.datePostedTextView;

/**
 * Created by J.EL on 9/14/2017.
 */

public class IfairCinglesViewHolder extends RecyclerView.ViewHolder {
    View mView;
    Context mContext;
    ProgressBar progressBar;
    public CircleImageView profileImageView;
    public TextView usernameTextView;
    public TextView cingleTradeMethodTextView;
    public TextView cingleSenseCreditsTextView;
    public ImageView cingleImageView;
    public RelativeLayout cingleTradingRelativeLayout;
    public TextView cingleOwnerTextView;
    public CircleImageView ownerImageView;
    public TextView datePostedTextView;

    public IfairCinglesViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
        mContext = itemView.getContext();
        profileImageView = (CircleImageView) itemView.findViewById(R.id.profileImageView);
        cingleImageView = (ImageView) mView.findViewById(R.id.cingleImageView);
        cingleSenseCreditsTextView = (TextView) mView.findViewById(R.id.cingleSenseCreditsCountTextView);
        cingleTradeMethodTextView = (TextView) mView.findViewById(R.id.cingleTradeMethodTextView);
        cingleTradingRelativeLayout = (RelativeLayout) mView.findViewById(R.id.cingleTradingRelativeLayout);
        cingleOwnerTextView = (TextView) mView.findViewById(R.id.cingleOwnerTextView);
        ownerImageView = (CircleImageView) mView.findViewById(R.id.ownerImageView);
        datePostedTextView = (TextView) mView.findViewById(R.id.datePostedTextView);
        usernameTextView = (TextView) mView.findViewById(R.id.usernameTextView);


    }

//    public void bindBestCingle(final Cingle cingle){
//        final ProportionalImageView cingleImageView = (ProportionalImageView) mView.findViewById(R.id.cingleImageView);
//        final CircleImageView profileImageView = (CircleImageView) mView.findViewById(R.id.profileImageView);
//        TextView cingleTitleTextView = (TextView) mView.findViewById(R.id.cingleTitleTextView);
//        TextView cingleDescriptionTextView = (TextView) mView.findViewById(R.id.cingleDescriptionTextView);
//        TextView cingleSenseCreditsTextView = (TextView) mView.findViewById(R.id.cingleSenseCreditsCountTextView);
//        TextView datePostedTextView = (TextView) mView.findViewById(R.id.datePostedTextView);
//
//
//    }
}

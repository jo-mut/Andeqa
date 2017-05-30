package com.cinggl.cinggl.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.cinggl.cinggl.R;
import com.cinggl.cinggl.models.Cingle;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

/**
 * Created by J.EL on 5/26/2017.
 */

public class FirebaseCingleOutViewHolder extends RecyclerView.ViewHolder {

    View mView;
    Context mContext;
    ProgressBar progressBar;

    public FirebaseCingleOutViewHolder(View itemView){
        super(itemView);
        mView = itemView;
        mContext = itemView.getContext();
    }

    public void bindCingle(Cingle cingle){
        ImageView profileImageView = (ImageView) mView.findViewById(R.id.profileImageView);
        ImageView cingleImageView = (ImageView) mView.findViewById(R.id.cingleImageView);
        TextView viewCountTextView = (TextView) mView.findViewById(R.id.viewCountTextView);
        TextView likesCountImageView = (TextView) mView.findViewById(R.id.likesCountTextView);
        TextView commentsCountTextView = (TextView) mView.findViewById(R.id.commentsCountTextView);
        TextView cingleWorthTextView = (TextView) mView.findViewById(R.id.cingleWorthTextView);
        ImageView cingleSettingsImageView = (ImageView) mView.findViewById(R.id.cingleSettingsImageView);
        TextView accountUsernameTextView = (TextView) mView.findViewById(R.id.accountUsernameTextView);
        TextView timeTextView = (TextView) mView.findViewById(R.id.timeTextView);
        TextView cingleTitleTextView = (TextView) mView.findViewById(R.id.cingleTitleTextView);
        TextView cingleDescriptionTextView = (TextView) mView.findViewById(R.id.cingleDescriptionTextView);
        TextView streamedCommentsTextView = (TextView) mView.findViewById(R.id.streamedCommentsTextView);


        Picasso.with(mContext)
                .load(cingle.getCingleImageUrl())
                .fit()
                .centerCrop()
                .noFade()
                .into(cingleImageView, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {

                    }
                });


        cingleTitleTextView.setText((cingle.getTitle()));
        cingleDescriptionTextView.setText(cingle.getDescription());
        accountUsernameTextView.setText(cingle.getAccountUserName());

    }
}

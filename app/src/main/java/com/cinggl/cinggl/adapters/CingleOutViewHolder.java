package com.cinggl.cinggl.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.cinggl.cinggl.R;
import com.cinggl.cinggl.models.Cingle;
import com.cinggl.cinggl.utils.ProportionalImageView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by J.EL on 5/26/2017.
 */

public class CingleOutViewHolder extends RecyclerView.ViewHolder {

    View mView;
    Context mContext;
    ProgressBar progressBar;
    public ImageView likesImageView;
    public ImageView commentsImageView;
    public TextView likesCountTextView;
    public TextView cingleTitleTextView;
    public TextView cingleDescriptionTextView;
    public TextView accountUsernameTextView;
    public CircleImageView profileImageView;
    public TextView commentsCountTextView;
    public TextView sensePointsTextView;
    public TextView timeTextView;
    public ImageView cingleSettingsImageView;
    public static final int MAX_WIDTH = 400;
    public static final int MAX_HEIGHT = 400;

    public CingleOutViewHolder(View itemView){
        super(itemView);
        mView = itemView;
        mContext = itemView.getContext();
        likesImageView = (ImageView) itemView.findViewById(R.id.likesImageView);
        likesCountTextView =(TextView)itemView.findViewById(R.id.likesCountTextView);
        commentsImageView = (ImageView) itemView.findViewById(R.id.commentsImageView);
        cingleDescriptionTextView = (TextView) itemView.findViewById(R.id.cingleDescriptionTextView);
        cingleTitleTextView = (TextView) itemView.findViewById(R.id.cingleTitleTextView);
        accountUsernameTextView = (TextView) itemView.findViewById(R.id.accountUsernameTextView);
        profileImageView = (CircleImageView) itemView.findViewById(R.id.userProfileImageView);
        commentsCountTextView = (TextView) itemView.findViewById(R.id.commentsCountTextView);
        sensePointsTextView = (TextView) itemView.findViewById(R.id.sensePointsDescTextView);
        timeTextView = (TextView) itemView.findViewById(R.id.timeTextView);
        cingleSettingsImageView = (ImageView) itemView.findViewById(R.id.cingleSettingsImageView);

    }

    public void bindCingle(final Cingle cingle){
        final ProportionalImageView cingleImageView = (ProportionalImageView) mView.findViewById(R.id.cingleImageView);
        TextView cingleTitleTextView = (TextView) mView.findViewById(R.id.cingleTitleTextView);
        TextView cingleDescriptionTextView = (TextView) mView.findViewById(R.id.cingleDescriptionTextView);
        TextView sensePointsTextView = (TextView) mView.findViewById(R.id.sensePointsDescTextView);
        TextView timeTextView = (TextView) mView.findViewById(R.id.timeTextView);

        Picasso.with(mContext)
                .load(cingle.getCingleImageUrl())
                .networkPolicy(NetworkPolicy.OFFLINE)
                .into(cingleImageView, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {
                        Picasso.with(mContext)
                                .load(cingle.getCingleImageUrl())
                                .into(cingleImageView);


                    }
                });


        cingleTitleTextView.setText((cingle.getTitle()));
        cingleDescriptionTextView.setText(cingle.getDescription());
        sensePointsTextView.setText("SP" + " " + (cingle.getSensepoint()));

    }



}

package com.cinggl.cinggl.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.cinggl.cinggl.R;
import com.cinggl.cinggl.models.Cingle;
import com.cinggl.cinggl.ProportionalImageView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by J.EL on 7/6/2017.
 */

public class BestCinglesViewHolder extends RecyclerView.ViewHolder {

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
    public TextView usernameTextView;
    public TextView commentsCountTextView;
    public TextView currentDateTextView;
    public ImageView cingleSettingsImageView;
    public TextView sensePointsTextView;
    public static final int MAX_WIDTH = 400;
    public static final int MAX_HEIGHT = 400;

    public BestCinglesViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
        mContext = itemView.getContext();
        likesImageView = (ImageView) itemView.findViewById(R.id.likesImageView);
        likesCountTextView =(TextView)itemView.findViewById(R.id.likesCountTextView);
        commentsImageView = (ImageView) itemView.findViewById(R.id.commentsImageView);
        cingleDescriptionTextView = (TextView) itemView.findViewById(R.id.cingleDescriptionTextView);
        cingleTitleTextView = (TextView) itemView.findViewById(R.id.cingleTitleTextView);
        profileImageView = (CircleImageView) itemView.findViewById(R.id.userProfileImageView);
        commentsCountTextView = (TextView) itemView.findViewById(R.id.commentsCountTextView);
        currentDateTextView = (TextView) itemView.findViewById(R.id.currentDateTextView);
        usernameTextView = (TextView) itemView.findViewById(R.id.usernameTextView);
        cingleSettingsImageView = (ImageView) mView.findViewById(R.id.cingleSettingsImageView);
        sensePointsTextView= (TextView) mView.findViewById(R.id.sensePointsTextView);

    }

    public void bindBestCingle(final Cingle cingle){
        final ProportionalImageView cingleImageView = (ProportionalImageView) mView.findViewById(R.id.cingleImageView);
        final CircleImageView profileImageView = (CircleImageView) mView.findViewById(R.id.userProfileImageView);
        TextView cingleTitleTextView = (TextView) mView.findViewById(R.id.cingleTitleTextView);
        TextView cingleDescriptionTextView = (TextView) mView.findViewById(R.id.cingleDescriptionTextView);
        TextView sensePointsTextView = (TextView) mView.findViewById(R.id.sensePointsTextView);
        TextView datePostedTextView = (TextView) mView.findViewById(R.id.datePostedTextView);

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

        Picasso.with(mContext)
                .load(cingle.getProfileImageUrl())
                .fit()
                .centerCrop()
                .networkPolicy(NetworkPolicy.OFFLINE)
                .into(profileImageView, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {
                        Picasso.with(mContext)
                                .load(cingle.getProfileImageUrl())
                                .fit()
                                .centerCrop()
                                .placeholder(R.drawable.profle_image_background)
                                .into(profileImageView);

                    }
                });


        cingleTitleTextView.setText((cingle.getTitle()));
        cingleDescriptionTextView.setText(cingle.getDescription());
        datePostedTextView.setText(cingle.getDatePosted());

        //REMOVE SCIENTIFIC NOATATION
        DecimalFormat formatter =  new DecimalFormat("0.00000000");
        sensePointsTextView.setText("SP" + " " + formatter.format(cingle.getSensepoint()));

    }
}

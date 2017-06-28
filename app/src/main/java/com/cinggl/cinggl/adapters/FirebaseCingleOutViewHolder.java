package com.cinggl.cinggl.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.cinggl.cinggl.R;
import com.cinggl.cinggl.models.Cingle;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by J.EL on 5/26/2017.
 */

public class FirebaseCingleOutViewHolder extends RecyclerView.ViewHolder {

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
    public static final int MAX_WIDTH = 400;
    public static final int MAX_HEIGHT = 400;

    public FirebaseCingleOutViewHolder(View itemView){
        super(itemView);
        mView = itemView;
        mContext = itemView.getContext();
        likesImageView = (ImageView) itemView.findViewById(R.id.likesImageView);
        likesCountTextView =(TextView)itemView.findViewById(R.id.likesCountTextView);
        commentsImageView = (ImageView) itemView.findViewById(R.id.commentsImageView);
        cingleDescriptionTextView = (TextView) itemView.findViewById(R.id.cingleDescriptionTextView);
        cingleTitleTextView = (TextView) itemView.findViewById(R.id.cingleTitleTextView);
        accountUsernameTextView = (TextView) itemView.findViewById(R.id.accountUsernameTextView);
        profileImageView = (CircleImageView) itemView.findViewById(R.id.profileImageView);
        commentsCountTextView = (TextView) itemView.findViewById(R.id.commentsCountTextView);

    }

    public void bindCingle(final Cingle cingle){
        final CircleImageView profileImageView = (CircleImageView) mView.findViewById(R.id.profileImageView);
        final ImageView cingleImageView = (ImageView) mView.findViewById(R.id.cingleImageView);
        TextView viewCountTextView = (TextView) mView.findViewById(R.id.viewCountTextView);
        TextView likesCountImageView = (TextView) mView.findViewById(R.id.likesCountTextView);
        ImageView likesImageView =(ImageView) mView.findViewById(R.id.likesImageView);
        TextView commentsCountTextView = (TextView) mView.findViewById(R.id.commentsCountTextView);
        TextView cingleWorthTextView = (TextView) mView.findViewById(R.id.cingleWorthTextView);
        ImageView cingleSettingsImageView = (ImageView) mView.findViewById(R.id.cingleSettingsImageView);
        TextView accountUsernameTextView = (TextView) mView.findViewById(R.id.accountUsernameTextView);
        TextView cingleTitleTextView = (TextView) mView.findViewById(R.id.cingleTitleTextView);
        TextView cingleDescriptionTextView = (TextView) mView.findViewById(R.id.cingleDescriptionTextView);

        Picasso.with(mContext)
                .load(cingle.getCingleImageUrl())
                .fit()
                .centerCrop()
                .networkPolicy(NetworkPolicy.OFFLINE)
                .into(cingleImageView, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {
                        Picasso.with(mContext)
                                .load(cingle.getCingleImageUrl())
                                .fit()
                                .centerCrop()
                                .into(cingleImageView);


                    }
                });


        cingleTitleTextView.setText((cingle.getTitle()));
        cingleDescriptionTextView.setText(cingle.getDescription());

    }

}

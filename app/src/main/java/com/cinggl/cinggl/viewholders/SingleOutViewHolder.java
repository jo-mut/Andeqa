package com.cinggl.cinggl.viewholders;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cinggl.cinggl.ProportionalImageView;
import com.cinggl.cinggl.R;
import com.cinggl.cinggl.models.Post;
import com.google.firebase.database.DatabaseReference;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.math.BigDecimal;
import java.math.RoundingMode;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by J.EL on 11/17/2017.
 */

public class SingleOutViewHolder extends RecyclerView.ViewHolder {

    View mView;
    Context mContext;
    ProgressBar progressBar;
    public ImageView likesImageView;
    public ImageView commentsImageView;
    public TextView likesCountTextView;
    public TextView titleTextView;
    public TextView descriptionTextView;
    public TextView accountUsernameTextView;
    public CircleImageView profileImageView;
    public TextView commentsCountTextView;
    public TextView timeTextView;
    public TextView senseCreditsTextView;
    public TextView tradeMethodTextView;
    public ImageView settingsImageView;
    public RelativeLayout titleRelativeLayout;
    public ProportionalImageView postImageView;
    public static final int MAX_WIDTH = 400;
    public static final int MAX_HEIGHT = 400;
    private DatabaseReference cinglesReference;
    public RelativeLayout descriptionRelativeLayout;
    public RecyclerView likesRecyclerView;

    public SingleOutViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
        mContext = itemView.getContext();
        likesImageView = (ImageView) itemView.findViewById(R.id.likesImageView);
        likesCountTextView =(TextView)itemView.findViewById(R.id.likesCountTextView);
        commentsImageView = (ImageView) itemView.findViewById(R.id.commentsImageView);
        descriptionTextView = (TextView) itemView.findViewById(R.id.descriptionTextView);
        titleTextView = (TextView) itemView.findViewById(R.id.titleTextView);
        accountUsernameTextView = (TextView) itemView.findViewById(R.id.usernameTextView);
        profileImageView = (CircleImageView) itemView.findViewById(R.id.creatorImageView);
        commentsCountTextView = (TextView) itemView.findViewById(R.id.commentsCountTextView);
        timeTextView = (TextView) itemView.findViewById(R.id.timeTextView);
        settingsImageView = (ImageView) itemView.findViewById(R.id.settingsImageView);
        titleRelativeLayout = (RelativeLayout) itemView.findViewById(R.id.cingleTitleRelativeLayout);
        descriptionRelativeLayout  = (RelativeLayout) mView.findViewById(R.id.descriptionRelativeLayout);
        postImageView = (ProportionalImageView) mView.findViewById(R.id.postImageView);
        senseCreditsTextView = (TextView) mView.findViewById(R.id.postSenseCreditsTextView);
        tradeMethodTextView = (TextView) mView.findViewById(R.id.tradeMethodTextView);
        likesRecyclerView = (RecyclerView) mView.findViewById(R.id.likesRecyclerView);
    }

    public void bindRandomCingles(final Post post){
        postImageView = (ProportionalImageView) mView.findViewById(R.id.postImageView);
        descriptionTextView = (TextView) itemView.findViewById(R.id.descriptionTextView);
        titleTextView = (TextView) itemView.findViewById(R.id.titleTextView);
        timeTextView = (TextView) itemView.findViewById(R.id.timeTextView);
        titleRelativeLayout = (RelativeLayout) itemView.findViewById(R.id.cingleTitleRelativeLayout);
        descriptionRelativeLayout  = (RelativeLayout) mView.findViewById(R.id.descriptionRelativeLayout);
        senseCreditsTextView = (TextView) mView.findViewById(R.id.postSenseCreditsTextView);
        tradeMethodTextView = (TextView) mView.findViewById(R.id.tradeMethodTextView);

        Picasso.with(mContext)
                .load(post.getCingleImageUrl())
                .networkPolicy(NetworkPolicy.OFFLINE)
                .into(postImageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        Log.v("Picasso", "Fetched image");
                    }

                    @Override
                    public void onError() {
                        Picasso.with(mContext)
                                .load(post.getCingleImageUrl())
                                .into(postImageView, new Callback() {
                                    @Override
                                    public void onSuccess() {

                                    }

                                    @Override
                                    public void onError() {
                                        Log.v("Picasso", "Could not fetch image");
                                    }
                                });


                    }
                });

        if (post.getTitle().equals("")){
            titleRelativeLayout.setVisibility(View.GONE);
        }else {
            titleTextView.setText(post.getTitle());
        }

        if (post.getDescription().equals("")){
            descriptionRelativeLayout.setVisibility(View.GONE);
        }else {
            descriptionTextView.setText(post.getDescription());
        }

        timeTextView.setText(DateUtils.getRelativeTimeSpanString((long) post.getTimeStamp()));

    }


    private static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
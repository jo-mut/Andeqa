package com.cinggl.cinggl.home;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cinggl.cinggl.utils.ProportionalImageView;
import com.cinggl.cinggl.R;
import com.cinggl.cinggl.models.Post;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.math.BigDecimal;
import java.math.RoundingMode;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * Created by J.EL on 11/17/2017.
 */

public class MainPostsViewHolder extends RecyclerView.ViewHolder {

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
    public TextView accountUsernameTextView;
    public CircleImageView profileImageView;
    public TextView commentsCountTextView;
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
    public TextView totalLikesCountTextView;


    public MainPostsViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
        mContext = itemView.getContext();
        likesImageView = (ImageView) itemView.findViewById(R.id.likesImageView);
        dislikeImageView = (ImageView) itemView.findViewById(R.id.dislikesImageView);
        dislikeCountTextView = (TextView) itemView.findViewById(R.id.dislikesPercentageTextView);
        likesCountTextView =(TextView)itemView.findViewById(R.id.likesPercentageTextView);
        commentsImageView = (ImageView) itemView.findViewById(R.id.commentsImageView);
        descriptionTextView = (TextView) itemView.findViewById(R.id.descriptionTextView);
        titleTextView = (TextView) itemView.findViewById(R.id.titleTextView);
        accountUsernameTextView = (TextView) itemView.findViewById(R.id.usernameTextView);
        profileImageView = (CircleImageView) itemView.findViewById(R.id.profileImageView);
        commentsCountTextView = (TextView) itemView.findViewById(R.id.commentsCountTextView);
        settingsImageView = (ImageView) itemView.findViewById(R.id.settingsImageView);
        titleRelativeLayout = (RelativeLayout) itemView.findViewById(R.id.cingleTitleRelativeLayout);
        descriptionRelativeLayout  = (RelativeLayout) mView.findViewById(R.id.descriptionRelativeLayout);
        postImageView = (ProportionalImageView) mView.findViewById(R.id.postImageView);
        senseCreditsTextView = (TextView) mView.findViewById(R.id.postSenseCreditsTextView);
        tradeMethodTextView = (TextView) mView.findViewById(R.id.tradeMethodTextView);
        likesRecyclerView = (RecyclerView) mView.findViewById(R.id.likesRecyclerView);
        totalLikesCountTextView = (TextView) mView.findViewById(R.id.totalLikesCountTextView);
    }

    public void bindRandomCingles(final DocumentSnapshot documentSnapshot){
        final Post post = documentSnapshot.toObject(Post.class);
        postImageView = (ProportionalImageView) mView.findViewById(R.id.postImageView);
        descriptionTextView = (TextView) itemView.findViewById(R.id.descriptionTextView);
        titleTextView = (TextView) itemView.findViewById(R.id.titleTextView);
        titleRelativeLayout = (RelativeLayout) itemView.findViewById(R.id.cingleTitleRelativeLayout);
        descriptionRelativeLayout  = (RelativeLayout) mView.findViewById(R.id.descriptionRelativeLayout);
        senseCreditsTextView = (TextView) mView.findViewById(R.id.postSenseCreditsTextView);
        tradeMethodTextView = (TextView) mView.findViewById(R.id.tradeMethodTextView);


        Picasso.with(mContext)
                .load(post.getImage())
                .networkPolicy(NetworkPolicy.OFFLINE)
                .into(postImageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        Log.v("Picasso", "Fetched image");
                    }

                    @Override
                    public void onError() {
                        Picasso.with(mContext)
                                .load(post.getImage())
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


        if (!TextUtils.isEmpty(post.getTitle())){
            titleTextView.setText(post.getTitle());
            titleRelativeLayout.setVisibility(View.VISIBLE);

        }

        if (!TextUtils.isEmpty(post.getDescription())){
            descriptionTextView.setText(post.getDescription());
            descriptionRelativeLayout.setVisibility(View.VISIBLE);
        }

    }


    private static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}

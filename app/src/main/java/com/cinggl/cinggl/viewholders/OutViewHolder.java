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
import com.google.firebase.firestore.DocumentSnapshot;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.math.BigDecimal;
import java.math.RoundingMode;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by J.EL on 11/24/2017.
 */

public class OutViewHolder extends RecyclerView.ViewHolder {

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
    public TextView timeTextView;
    public TextView cingleSenseCreditsTextView;
    public TextView cingleTradeMethodTextView;
    public ImageView cingleSettingsImageView;
    public RelativeLayout cingleTitleRelativeLayout;
    public ProportionalImageView cingleImageView;
    public static final int MAX_WIDTH = 400;
    public static final int MAX_HEIGHT = 400;
    private DatabaseReference cinglesReference;
    public RelativeLayout descriptionRelativeLayout;
    public RecyclerView likesRecyclerView;


    public OutViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
        mContext = itemView.getContext();
        likesImageView = (ImageView) itemView.findViewById(R.id.likesImageView);
        likesCountTextView =(TextView)itemView.findViewById(R.id.likesCountTextView);
        commentsImageView = (ImageView) itemView.findViewById(R.id.commentsImageView);
        cingleDescriptionTextView = (TextView) itemView.findViewById(R.id.descriptionTextView);
        cingleTitleTextView = (TextView) itemView.findViewById(R.id.titleTextView);
        accountUsernameTextView = (TextView) itemView.findViewById(R.id.usernameTextView);
        profileImageView = (CircleImageView) itemView.findViewById(R.id.creatorImageView);
        commentsCountTextView = (TextView) itemView.findViewById(R.id.commentsCountTextView);
        timeTextView = (TextView) itemView.findViewById(R.id.timeTextView);
        cingleSettingsImageView = (ImageView) itemView.findViewById(R.id.settingsImageView);
        cingleTitleRelativeLayout = (RelativeLayout) itemView.findViewById(R.id.cingleTitleRelativeLayout);
        descriptionRelativeLayout  = (RelativeLayout) mView.findViewById(R.id.descriptionRelativeLayout);
        cingleImageView = (ProportionalImageView) mView.findViewById(R.id.postImageView);
        cingleSenseCreditsTextView = (TextView) mView.findViewById(R.id.postSenseCreditsTextView);
        cingleTradeMethodTextView = (TextView) mView.findViewById(R.id.tradeMethodTextView);
        likesRecyclerView = (RecyclerView) mView.findViewById(R.id.likesRecyclerView);
    }


    public void bindRandomCingles(final DocumentSnapshot documentSnapshot){
        final Post post = documentSnapshot.toObject(Post.class);
        cingleImageView = (ProportionalImageView) mView.findViewById(R.id.postImageView);
        cingleDescriptionTextView = (TextView) itemView.findViewById(R.id.descriptionTextView);
        cingleTitleTextView = (TextView) itemView.findViewById(R.id.titleTextView);
        timeTextView = (TextView) itemView.findViewById(R.id.timeTextView);
        cingleTitleRelativeLayout = (RelativeLayout) itemView.findViewById(R.id.cingleTitleRelativeLayout);
        descriptionRelativeLayout  = (RelativeLayout) mView.findViewById(R.id.descriptionRelativeLayout);
        cingleSenseCreditsTextView = (TextView) mView.findViewById(R.id.postSenseCreditsTextView);
        cingleTradeMethodTextView = (TextView) mView.findViewById(R.id.tradeMethodTextView);

        Picasso.with(mContext)
                .load(post.getCingleImageUrl())
                .networkPolicy(NetworkPolicy.OFFLINE)
                .into(cingleImageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        Log.v("Picasso", "Fetched image");
                    }

                    @Override
                    public void onError() {
                        Picasso.with(mContext)
                                .load(post.getCingleImageUrl())
                                .into(cingleImageView, new Callback() {
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
            cingleTitleRelativeLayout.setVisibility(View.GONE);
        }else {
            cingleTitleTextView.setText(post.getTitle());
        }

        if (post.getDescription().equals("")){
            descriptionRelativeLayout.setVisibility(View.GONE);
        }else {
            cingleDescriptionTextView.setText(post.getDescription());
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

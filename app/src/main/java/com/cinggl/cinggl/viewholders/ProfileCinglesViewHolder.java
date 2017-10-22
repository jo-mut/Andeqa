package com.cinggl.cinggl.viewholders;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.ImageView;
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


/**
 * Created by J.EL on 6/8/2017.
 */

public class ProfileCinglesViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    View mView;
    Context mContext;
    public ImageView likesImageView;
    public ImageView commentsImageView;
    public TextView likesCountTextView;
    public TextView cingleTitleTextView;
    public TextView cingleDescriptionTextView;
    public TextView accountUsernameTextView;
    public CircleImageView profileImageView;
    public TextView commentsCountTextView;
    public TextView timeTextView;
    public ImageView cingleSettingsImageView;
    public RelativeLayout cingleTitleRelativeLayout;
    public RelativeLayout descriptionRelativeLayout;
    public ProportionalImageView cingleImageView;
    public TextView cingleTradeMethodTextView;
    public TextView cingleSenseCreditsTextView;
    public RecyclerView likesRecyclerView;


    public ProfileCinglesViewHolder(View itemView){
        super(itemView);
        mView = itemView;
        mContext = itemView.getContext();
        likesImageView = (ImageView) itemView.findViewById(R.id.likesImageView);
        likesCountTextView =(TextView)itemView.findViewById(R.id.likesCountTextView);
        commentsImageView = (ImageView) itemView.findViewById(R.id.commentsImageView);
        cingleDescriptionTextView = (TextView) itemView.findViewById(R.id.cingleDescriptionTextView);
        cingleTitleTextView = (TextView) itemView.findViewById(R.id.cingleTitleTextView);
        accountUsernameTextView = (TextView) itemView.findViewById(R.id.usernameTextView);
        profileImageView = (CircleImageView) itemView.findViewById(R.id.creatorImageView);
        commentsCountTextView = (TextView) itemView.findViewById(R.id.commentsCountTextView);
        timeTextView = (TextView) itemView.findViewById(R.id.timeTextView);
        cingleSettingsImageView = (ImageView) itemView.findViewById(R.id.cingleSettingsImageView);
        cingleTitleRelativeLayout = (RelativeLayout) itemView.findViewById(R.id.cingleTitleRelativeLayout);
        descriptionRelativeLayout  = (RelativeLayout) mView.findViewById(R.id.descriptionRelativeLayout);
        cingleImageView = (ProportionalImageView) mView.findViewById(R.id.cingleImageView);
        cingleSenseCreditsTextView = (TextView) mView.findViewById(R.id.cingleSenseCreditsTextView);
        cingleTradeMethodTextView = (TextView) mView.findViewById(R.id.cingleTradeMethodTextView);
        likesRecyclerView = (RecyclerView) mView.findViewById(R.id.likesRecyclerView);

        likesImageView.setOnClickListener(this);
        commentsImageView.setOnClickListener(this);
        likesCountTextView.setOnClickListener(this);


    }

    public void bindProfileCingle(final Cingle cingle){
        final ProportionalImageView cingleImageView = (ProportionalImageView) mView.findViewById(R.id.cingleImageView);
        TextView cingleTitleTextView = (TextView) mView.findViewById(R.id.cingleTitleTextView);
        TextView cingleDescriptionTextView = (TextView) mView.findViewById(R.id.cingleDescriptionTextView);
        TextView cingleSenseCreditsTextView = (TextView) mView.findViewById(R.id.cingleSenseCreditsTextView);
        TextView timeTextView = (TextView) mView.findViewById(R.id.timeTextView);
        RelativeLayout cingleTitleRelativeLayout = (RelativeLayout) itemView.findViewById(R.id.cingleTitleRelativeLayout);

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


        if (cingle.getTitle().equals("")){
            cingleTitleRelativeLayout.setVisibility(View.GONE);
        }else {
            cingleTitleTextView.setText(cingle.getTitle());
        }

        if (cingle.getDescription().equals("")){
            descriptionRelativeLayout.setVisibility(View.GONE);
        }else {
            cingleDescriptionTextView.setText(cingle.getDescription());

        }

        DecimalFormat formatter =  new DecimalFormat("0.00000000");
        cingleSenseCreditsTextView.setText("CSC" + " " + formatter.format(cingle.getSensepoint()));
        timeTextView.setText(DateUtils.getRelativeTimeSpanString((long) cingle.getTimeStamp()));

    }



    @Override
    public  void onClick(View v){


    }

}

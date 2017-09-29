package com.cinggl.cinggl.adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableStringBuilder;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cinggl.cinggl.R;
import com.cinggl.cinggl.models.Cingle;
import com.cinggl.cinggl.ProportionalImageView;
import com.cinggl.cinggl.utils.ExpandableTextView;
import com.google.firebase.database.DatabaseReference;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;
import static com.cinggl.cinggl.R.id.cingleDescriptionTextView;

/**
 * Created by J.EL on 5/26/2017.
 */

public class CingleOutViewHolder extends RecyclerView.ViewHolder{

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
    private RelativeLayout descriptionRelativeLayout;
    public RecyclerView likesRecyclerView;


    public CingleOutViewHolder(View itemView){
        super(itemView);
        mView = itemView;
        mContext = itemView.getContext();
        likesImageView = (ImageView) itemView.findViewById(R.id.likesImageView);
        likesCountTextView =(TextView)itemView.findViewById(R.id.likesCountTextView);
        commentsImageView = (ImageView) itemView.findViewById(R.id.commentsImageView);
        cingleDescriptionTextView = (TextView) itemView.findViewById(R.id.cingleDescriptionTextView);
        cingleTitleTextView = (TextView) itemView.findViewById(R.id.cingleTitleTextView);
        accountUsernameTextView = (TextView) itemView.findViewById(R.id.usernameTextView);
        profileImageView = (CircleImageView) itemView.findViewById(R.id.profileImageView);
        commentsCountTextView = (TextView) itemView.findViewById(R.id.commentsCountTextView);
        timeTextView = (TextView) itemView.findViewById(R.id.timeTextView);
        cingleSettingsImageView = (ImageView) itemView.findViewById(R.id.cingleSettingsImageView);
        cingleTitleRelativeLayout = (RelativeLayout) itemView.findViewById(R.id.cingleTitleRelativeLayout);
        descriptionRelativeLayout  = (RelativeLayout) mView.findViewById(R.id.descriptionRelativeLayout);
        cingleImageView = (ProportionalImageView) mView.findViewById(R.id.cingleImageView);
        cingleSenseCreditsTextView = (TextView) mView.findViewById(R.id.cingleSenseCreditsTextView);
        cingleTradeMethodTextView = (TextView) mView.findViewById(R.id.cingleTradeMethodTextView);
        likesRecyclerView = (RecyclerView) mView.findViewById(R.id.likesRecyclerView);


    }


    public void bindCingle(final Cingle cingle){
        final ProportionalImageView cingleImageView = (ProportionalImageView) mView.findViewById(R.id.cingleImageView);
        TextView cingleTitleTextView = (TextView) mView.findViewById(R.id.cingleTitleTextView);
        TextView cingleDescriptionTextView = (TextView) mView.findViewById(R.id.cingleDescriptionTextView);
        TextView cingleSenseCreditsTextView = (TextView) mView.findViewById(R.id.cingleSenseCreditsTextView);
        TextView timeTextView = (TextView) mView.findViewById(R.id.timeTextView);
        RelativeLayout cingleTitleRelativeLayout = (RelativeLayout) mView.findViewById(R.id.cingleTitleRelativeLayout);
        TextView cingleTradeMethodTextView = (TextView) mView.findViewById(R.id.cingleTradeMethodTextView);

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

        timeTextView.setText(DateUtils.getRelativeTimeSpanString((long) cingle.getTimeStamp()));
        cingleTradeMethodTextView.setText("@CingleBacking");

        //REMOVE SCIENTIFIC NOATATION
        DecimalFormat formatter =  new DecimalFormat("0.00000000");
        if (cingle.getSensepoint() == 0.00){
            cingleSenseCreditsTextView.setText("CSC" + " " + "0.00");
        }else {
            cingleSenseCreditsTextView.setText("CSC" + " " + formatter.format(cingle.getSensepoint()));
        }
    }

    private static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }


}

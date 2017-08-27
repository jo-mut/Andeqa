package com.cinggl.cinggl.ifair;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.cinggl.cinggl.ProportionalImageView;
import com.cinggl.cinggl.R;
import com.cinggl.cinggl.models.Cingle;
import com.cinggl.cinggl.models.Ifair;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.R.attr.button;
import static com.cinggl.cinggl.R.id.cingleImageView;
import static com.cinggl.cinggl.R.id.cingleTradeMethodTextView;
import static com.cinggl.cinggl.R.id.datePostedTextView;
import static com.cinggl.cinggl.R.id.descriptionRelativeLayout;
import static com.cinggl.cinggl.R.id.profileImageView;
import static com.cinggl.cinggl.R.id.titleRelativeLayout;

/**
 * Created by J.EL on 8/20/2017.
 */

public class TradeCinglesViewHolder extends RecyclerView.ViewHolder {

    View mView;
    Context mContext;
    public ProportionalImageView cingleImageView;
    public Button mBackCingleButton;

    public TradeCinglesViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
        mContext = itemView.getContext();
        cingleImageView = (ProportionalImageView) mView.findViewById(R.id.cingleImageView);
        mBackCingleButton = (Button) mView.findViewById(R.id.backCingleButton);
    }

    public void bindTradedCingles(final Ifair ifair){
        final ProportionalImageView cingleImageView = (ProportionalImageView) mView.findViewById(R.id.cingleImageView);

        Picasso.with(mContext)
                .load(ifair.getImage())
                .networkPolicy(NetworkPolicy.OFFLINE)
                .into(cingleImageView, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {
                        Picasso.with(mContext)
                                .load(ifair.getImage())
                                .into(cingleImageView);


                    }
                });

    }
}

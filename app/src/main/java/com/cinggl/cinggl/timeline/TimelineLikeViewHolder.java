package com.cinggl.cinggl.timeline;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cinggl.cinggl.R;
import com.cinggl.cinggl.models.Timeline;
import com.cinggl.cinggl.utils.ProportionalImageView;

import static com.cinggl.cinggl.R.id.followButton;

/**
 * Created by J.EL on 1/18/2018.
 */

public class TimelineLikeViewHolder extends RecyclerView.ViewHolder {


    View mView;
    Context mContext;
    public TextView usernameTextView;
    public ProportionalImageView postImageView;
    public TextView timelineTextView;
    public LinearLayout timelineLikeLinearLayout;

    public TimelineLikeViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
        mContext = itemView.getContext();
        usernameTextView = (TextView)itemView.findViewById(R.id.usernameTextView);
        postImageView = (ProportionalImageView) itemView.findViewById(R.id.postImageView);
        timelineTextView = (TextView) itemView.findViewById(R.id.timelineTextView);
        timelineLikeLinearLayout = (LinearLayout) itemView.findViewById(R.id.timelineLikeLinearLayout);
    }

    public void bindTimelineLike(final Timeline timeline){



    }


}

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

import de.hdodenhof.circleimageview.CircleImageView;

import static com.cinggl.cinggl.R.id.followButton;
import static com.cinggl.cinggl.R.id.postImageView;
import static com.cinggl.cinggl.R.id.profileImageView;

/**
 * Created by J.EL on 1/18/2018.
 */

public class TimelineLikeViewHolder extends RecyclerView.ViewHolder {


    View mView;
    Context mContext;
    public TextView usernameTextView;
    public CircleImageView profileImageView;
    public TextView timelineTextView;
    public LinearLayout timelineLikeLinearLayout;
    public View statusView;

    public TimelineLikeViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
        mContext = itemView.getContext();
        usernameTextView = (TextView)itemView.findViewById(R.id.usernameTextView);
        profileImageView = (CircleImageView) itemView.findViewById(R.id.profileImageView);
        timelineTextView = (TextView) itemView.findViewById(R.id.timelineTextView);
        statusView = (View) mView.findViewById(R.id.statusView);
        timelineLikeLinearLayout = (LinearLayout) itemView.findViewById(R.id.timelineLikeLinearLayout);
    }

    public void bindTimelineLike(final Timeline timeline){



    }


}

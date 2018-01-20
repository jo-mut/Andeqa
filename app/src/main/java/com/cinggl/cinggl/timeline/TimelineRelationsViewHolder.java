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

import static com.cinggl.cinggl.R.id.postImageView;
import static com.cinggl.cinggl.R.id.profileImageView;

/**
 * Created by J.EL on 1/18/2018.
 */

public class TimelineRelationsViewHolder extends RecyclerView.ViewHolder {


    View mView;
    Context mContext;
    private TextView commentCountTextView;
    public TextView usernameTextView;
    public CircleImageView profileImageView;
    public Button followButton;
    public TextView timelineTextView;
    public LinearLayout timelineRelationLinearLayout;
    public View statusView;

    public TimelineRelationsViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
        mContext = itemView.getContext();
        usernameTextView = (TextView)itemView.findViewById(R.id.usernameTextView);
        commentCountTextView = (TextView) itemView.findViewById(R.id.commentsCountTextView);
        profileImageView = (CircleImageView) itemView.findViewById(R.id.profileImageView);
        followButton = (Button) itemView.findViewById(R.id.followTextView);
        statusView = (View) mView.findViewById(R.id.statusView);
        timelineTextView = (TextView) itemView.findViewById(R.id.timelineTextView);
        timelineRelationLinearLayout = (LinearLayout) itemView.findViewById(R.id.timelineRelationLinealLayout);
    }

    public void bindTimelineRelations(final Timeline timeline){



    }


}

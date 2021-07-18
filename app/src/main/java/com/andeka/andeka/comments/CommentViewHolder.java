package com.andeka.andeka.comments;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.andeka.andeka.R;

import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by J.EL on 6/16/2017.
 */

public class CommentViewHolder extends RecyclerView.ViewHolder {

    View mView;
    Context mContext;
    public TextView usernameTextView;
    public CircleImageView profileImageView;
    public TextView fullNameTextView;
    public RelativeLayout followRelativeLayout;
    public Button followButton;
    public ImageView sendMessageImageView;
    public RelativeLayout mSendMessageRelativeLayout;
    public TextView mCommentTextView;


    public CommentViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
        mContext = itemView.getContext();
        ButterKnife.bind(this, mView);
        usernameTextView = (TextView)itemView.findViewById(R.id.usernameTextView);
        profileImageView = (CircleImageView) itemView.findViewById(R.id.profileImageView);
        fullNameTextView = (TextView) itemView.findViewById(R.id.fullNameTextView);
        followRelativeLayout = (RelativeLayout) itemView.findViewById(R.id.followRelativeLayout);
        sendMessageImageView = (ImageView) itemView.findViewById(R.id.sendMessageImageView);
        followButton = (Button) itemView.findViewById(R.id.followButton);
        mCommentTextView = (TextView) itemView.findViewById(R.id.commentTextView);
        mSendMessageRelativeLayout = (RelativeLayout) itemView.findViewById(R.id.sendMessageRelativeLayout);
    }


}

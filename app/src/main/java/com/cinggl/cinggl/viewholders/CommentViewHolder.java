package com.cinggl.cinggl.viewholders;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.cinggl.cinggl.R;
import com.cinggl.cinggl.models.Comment;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by J.EL on 6/16/2017.
 */

public class CommentViewHolder extends RecyclerView.ViewHolder {

    View mView;
    Context mContext;
    private TextView commentCountTextView;
    public TextView usernameTextView;
    public CircleImageView profileImageView;
    public Button followButton;
    public TextView fullNameTextView;

    public CommentViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
        mContext = itemView.getContext();
        usernameTextView = (TextView)itemView.findViewById(R.id.usernameTextView);
        commentCountTextView = (TextView) itemView.findViewById(R.id.commentsCountTextView);
        profileImageView = (CircleImageView) itemView.findViewById(R.id.creatorImageView);
        followButton = (Button) itemView.findViewById(R.id.followTextView);
        fullNameTextView = (TextView) itemView.findViewById(R.id.fullNameTextView);
    }

    public void bindComment(final Comment comment){
        TextView commentTextView = (TextView) mView.findViewById(R.id.commentTextView);

        commentTextView.setText(comment.getCommentText());


    }
}

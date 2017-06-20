package com.cinggl.cinggl.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.cinggl.cinggl.R;
import com.cinggl.cinggl.models.Comment;
import com.squareup.picasso.Picasso;

import butterknife.Bind;

/**
 * Created by J.EL on 6/16/2017.
 */

public class CommentViewHolder extends RecyclerView.ViewHolder {

    View mView;
    Context mContext;
    private ImageView userProfileImageView;
    private TextView usernameTextView;

    public CommentViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
        mContext = itemView.getContext();
        usernameTextView = (TextView)itemView.findViewById(R.id.usernameTextView);
    }

    public void bindComment(Comment comment){
        TextView usernameTextView =(TextView) mView.findViewById(R.id.usernameTextView);
        TextView commentTextView = (TextView) mView.findViewById(R.id.commentTextView);

        usernameTextView.setText(comment.getUsername());
        commentTextView.setText(comment.getCommentText());

    }
}

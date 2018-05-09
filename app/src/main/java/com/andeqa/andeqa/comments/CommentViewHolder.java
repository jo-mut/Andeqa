package com.andeqa.andeqa.comments;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.andeqa.andeqa.R;
import com.andeqa.andeqa.models.Comment;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

import static android.R.attr.id;

/**
 * Created by J.EL on 6/16/2017.
 */

public class CommentViewHolder extends RecyclerView.ViewHolder {

    View mView;
    Context mContext;
    private TextView commentCountTextView;
    public TextView usernameTextView;
    public CircleImageView profileImageView;
    public TextView fullNameTextView;
    @Bind(R.id.commentTextView)TextView mCommentTextView;


    public CommentViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
        mContext = itemView.getContext();
        ButterKnife.bind(this, mView);
        usernameTextView = (TextView)itemView.findViewById(R.id.usernameTextView);
        commentCountTextView = (TextView) itemView.findViewById(R.id.commentsCountTextView);
        profileImageView = (CircleImageView) itemView.findViewById(R.id.profileImageView);
        fullNameTextView = (TextView) itemView.findViewById(R.id.fullNameTextView);
    }


}

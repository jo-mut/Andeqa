package com.cinggl.cinggl.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.cinggl.cinggl.R;
import com.cinggl.cinggl.models.Comment;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by J.EL on 6/16/2017.
 */

public class CommentViewHolder extends RecyclerView.ViewHolder {

    View mView;
    Context mContext;
    private ImageView userProfileImageView;
    private TextView usernameTextView;
    private TextView commentCountTextView;

    public CommentViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
        mContext = itemView.getContext();
        usernameTextView = (TextView)itemView.findViewById(R.id.accountUsernameTextView);
        commentCountTextView = (TextView) itemView.findViewById(R.id.commentsCountTextView);

    }

    public void bindComment(final Comment comment){
        TextView usernameTextView =(TextView) mView.findViewById(R.id.accountUsernameTextView);
        TextView commentTextView = (TextView) mView.findViewById(R.id.commentTextView);
        final CircleImageView userProfileImageView = (CircleImageView) mView.findViewById(R.id.userProfileImageView);


        usernameTextView.setText(comment.getUsername());
        commentTextView.setText(comment.getCommentText());

        Picasso.with(mContext)
                .load(comment.getProfileImage())
                .fit()
                .centerCrop()
                .placeholder(R.drawable.profle_image_background)
                .networkPolicy(NetworkPolicy.OFFLINE)
                .into(userProfileImageView, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {
                        Picasso.with(mContext)
                                .load(comment.getProfileImage())
                                .fit()
                                .centerCrop()
                                .placeholder(R.drawable.profle_image_background)
                                .into(userProfileImageView);
                    }
                });


    }
}

package com.cinggl.cinggl.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.cinggl.cinggl.R;
import com.cinggl.cinggl.models.Comment;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by J.EL on 6/16/2017.
 */

public class CommentAdapter extends RecyclerView.Adapter<CommentViewHolder> {

    private Context mContext;
    private DatabaseReference mDatabaseReference;
    private ChildEventListener mChildEventListener;
    private static final String TAG = "CommentAdapater";

    private List<String> mCommentIds = new ArrayList<>();
    private List<Comment> mComments = new ArrayList<>();

    public CommentAdapter(final Context context, DatabaseReference ref){
        mContext = context;
        mDatabaseReference = ref;

        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "onChildAdded:" + dataSnapshot.getKey());

                // add the a new comment to the list to be displeyed
                Comment comment = dataSnapshot.getValue(Comment.class);

                //update the recyclerView
                mCommentIds.add(dataSnapshot.getKey());
                mComments.add(comment);
                notifyItemInserted(mComments.size() - 1);

                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Log.e(snapshot.getKey(), snapshot.getChildrenCount() + "commentsCount");

                }

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "onChildChanged" + dataSnapshot.getKey());

                /*A comment has been changed, using the key we asses if we
                * are already displaying the updated comment and if not display it*/

                Comment changedComment = dataSnapshot.getValue(Comment.class);
                String commentKey = dataSnapshot.getKey();

                int commentIndex = mComments.indexOf(commentKey);
                if(commentIndex > -1){
                    mComments.set(commentIndex, changedComment);

                    //update the recyclerView;
                    notifyItemChanged(commentIndex);
                }else {
                    Log.w(TAG, "onChildeChnaged: the child is not known" + commentKey);
                }

                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Log.e(snapshot.getKey(), snapshot.getChildrenCount() + "commentsCount");

                }


            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onChildRemoved:" + dataSnapshot.getKey());

                 /*A comment has been changed, using the key we asses if we
                * are already displaying the updated comment and if not display it*/

                String commentKey = dataSnapshot.getKey();

                int commentIndex = mComments.indexOf(commentKey);
                if(commentIndex > -1){
                    mComments.remove(commentIndex);

                    //update the recyclerview
                    notifyItemRemoved(commentIndex);
                }else{
                    Log.w(TAG, "onChildRemoved: the child is not known" + commentKey);
                }

                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Log.e(snapshot.getKey(), snapshot.getChildrenCount() + "commentsCount");

                }


            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Log.e(snapshot.getKey(), snapshot.getChildrenCount() + "commentsCount");

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "newComment:onCancelled", databaseError.toException());
                Toast.makeText(mContext, "Failed to load more comments.", Toast.LENGTH_SHORT).show();



            }
        };
        ref.addChildEventListener(childEventListener);
        mChildEventListener = childEventListener;
    }

    public void cleanupListener() {
        if (mChildEventListener != null) {
            mDatabaseReference.removeEventListener(mChildEventListener);
        }
    }

    @Override
    public void onBindViewHolder(CommentViewHolder holder, int position) {
        Comment comment = mComments.get(position);
        holder.bindComment(comment);
    }

    @Override
    public CommentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.comments_layout_list, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return mComments.size();
    }



}

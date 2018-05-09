package com.andeqa.andeqa.comments;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.R;
import com.andeqa.andeqa.models.Andeqan;
import com.andeqa.andeqa.models.Comment;
import com.andeqa.andeqa.models.Relation;
import com.andeqa.andeqa.models.Timeline;
import com.andeqa.andeqa.profile.ProfileActivity;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by J.EL on 3/23/2018.
 */

public class CommentsAdapter extends RecyclerView.Adapter<CommentViewHolder> {
    private Context mContext;
    private FirebaseAuth firebaseAuth;
    private String mPostId;
    //firebase
    private DatabaseReference databaseReference;
    //firestore
    private Query mQuery;
    private ListenerRegistration mRegistration;
    private CollectionReference usersCollection;
    private CollectionReference relationsCollection;
    private CollectionReference timelineCollection;
    private static final String COLLECTION_ID = "collection id";
    private static final String EXTRA_POST_ID = "post id";
    private static final String EXTRA_USER_UID = "uid";
    private static final String TAG = CommentsAdapter.class.getSimpleName();
    private boolean processFollow = false;
    private List<DocumentSnapshot> documentSnapshots = new ArrayList<>();


    public CommentsAdapter(Context mContext) {
        this.mContext = mContext;
    }

    protected void setPostComments(List<DocumentSnapshot> mSnapshots){
        this.documentSnapshots = mSnapshots;
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    protected DocumentSnapshot getSnapshot(int index) {
        return documentSnapshots.get(index);
    }


    @Override
    public int getItemCount() {
        return documentSnapshots.size();
    }

    @Override
    public CommentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comments_layout_list, parent, false);
        return new CommentViewHolder(view);

    }

    @Override
    public void onBindViewHolder(final CommentViewHolder holder, int position) {
        Comment comment = getSnapshot(holder.getAdapterPosition()).toObject(Comment.class);
        final String uid = comment.getUser_id();

        firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() != null){
            //firestore
            usersCollection = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);
            relationsCollection = FirebaseFirestore.getInstance().collection(Constants.RELATIONS);
            timelineCollection = FirebaseFirestore.getInstance().collection(Constants.TIMELINE);
            //firebase
            databaseReference = FirebaseDatabase.getInstance().getReference(Constants.RANDOM_PUSH_ID);

        }

//        addReadLess(comment.getComment_text(), holder.mCommentTextView);
//        addReadMore(comment.getComment_text(), holder.mCommentTextView);
        holder.mCommentTextView.setText(comment.getComment_text());

        holder.profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, ProfileActivity.class);
                intent.putExtra(CommentsAdapter.EXTRA_USER_UID, uid);
                mContext.startActivity(intent);
            }
        });

        //get the profile of the user wh just commented
        usersCollection.document(uid).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (documentSnapshot.exists()){
                    final Andeqan andeqan = documentSnapshot.toObject(Andeqan.class);
                    final String profileImage = andeqan.getProfile_image();
                    final String username = andeqan.getUsername();

                    holder.usernameTextView.setText(username);
                    Picasso.with(mContext)
                            .load(profileImage)
                            .fit()
                            .centerCrop()
                            .placeholder(R.drawable.profle_image_background)
                            .networkPolicy(NetworkPolicy.OFFLINE)
                            .into(holder.profileImageView, new Callback() {
                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onError() {
                                    Picasso.with(mContext)
                                            .load(profileImage)
                                            .fit()
                                            .centerCrop()
                                            .placeholder(R.drawable.profle_image_background)
                                            .into(holder.profileImageView);
                                }
                            });

                }
            }
        });


    }

    @Override
    public void onViewRecycled(CommentViewHolder holder) {
        super.onViewRecycled(holder);
    }

    private void addReadMore(final String text, final TextView textView) {

        final String [] strings = text.split("");

        final int size = strings.length;

        if (size <= 120){
            //setence will not have read more
        }else {
            SpannableString ss = new SpannableString(text.substring(0, 119) + "...read more");
            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(View view) {
                    addReadLess(text, textView);
                }
                @Override
                public void updateDrawState(TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setUnderlineText(false);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        ds.setColor(mContext.getResources().getColor(R.color.colorPrimary, mContext.getTheme()));
                    } else {
                        ds.setColor(mContext.getResources().getColor(R.color.colorPrimary));
                    }
                }
            };
            ss.setSpan(clickableSpan, ss.length() - 10, ss.length() , Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            textView.setText(ss);
            textView.setMovementMethod(LinkMovementMethod.getInstance());
        }
    }

    private void addReadLess(final String text, final TextView textView) {
        final String [] strings = text.split("");

        final int size = strings.length;

        if (size > 120){
            SpannableString ss = new SpannableString(text + " read less");
            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(View view) {
                    addReadMore(text, textView);
                }
                @Override
                public void updateDrawState(TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setUnderlineText(false);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        ds.setColor(mContext.getResources().getColor(R.color.colorPrimary, mContext.getTheme()));
                    } else {
                        ds.setColor(mContext.getResources().getColor(R.color.colorPrimary));
                    }
                }
            };
            ss.setSpan(clickableSpan, ss.length() - 10, ss.length() , Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            textView.setText(ss);
            textView.setMovementMethod(LinkMovementMethod.getInstance());
        }

    }
}

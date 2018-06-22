package com.andeqa.andeqa.timeline;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.R;
import com.andeqa.andeqa.home.PostDetailActivity;
import com.andeqa.andeqa.models.Andeqan;
import com.andeqa.andeqa.models.CollectionPost;
import com.andeqa.andeqa.models.Comment;
import com.andeqa.andeqa.models.Credit;
import com.andeqa.andeqa.models.Post;
import com.andeqa.andeqa.models.Timeline;
import com.andeqa.andeqa.profile.ProfileActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

/**
 * Created by J.EL on 1/18/2018.
 */

public class NotificationsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = NotificationsAdapter.class.getSimpleName();
    private Context mContext;
    private static final String KEY_LAYOUT_POSITION = "layout pooition";
    private static final String EXTRA_USER_UID = "uid";
    private static final String EXTRA_POST_ID = "post id";
    private static final String COLLECTION_ID = "collection id";
    private static final String TYPE = "type";

    private static final int MAX_WIDTH = 200;
    private static final int MAX_HEIGHT = 200;
    private FirebaseAuth firebaseAuth;
    public static final int LIKE_TYPE=0;
    public static final int COMMENT_TYPE=1;
    public static final int RELATION_FOLLOWER_TYPE=2;
    private boolean processFollow = false;

    private CollectionReference usersCollection;
    private CollectionReference postCollection;
    private CollectionReference collectionsPostCollections;
    private CollectionReference relationsCollection;
    private CollectionReference timelineCollection;
    private CollectionReference senseCreditCollection;
    private CollectionReference commentCollection;
    private DatabaseReference databaseReference;
    private List<DocumentSnapshot> documentSnapshots = new ArrayList<>();


    public NotificationsAdapter(Context mContext) {
        this.mContext = mContext;
    }

    protected void setTimelineActivities(List<DocumentSnapshot> mSnapshots){
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
    public int getItemViewType(int position) {
        Timeline timeline = getSnapshot(position).toObject(Timeline.class);
        final String type = timeline.getType();

        if (type.equals("like")){
            return LIKE_TYPE;
        }else if (type.equals("comment")){
            return COMMENT_TYPE;
        }else {
            return RELATION_FOLLOWER_TYPE;
        }

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;

        switch (viewType){
            case LIKE_TYPE:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.notifications_like_layout, parent, false);
                return new NotificationsLikeViewHolder(view);
            case COMMENT_TYPE:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.notifications_comment_layout, parent, false);
                return  new NotificationsCommentViewHolder(view);

        }

        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Timeline timeline = getSnapshot(position).toObject(Timeline.class);
        final String type = timeline.getType();

        if (type.equals("like")){
            populateTimelineLike((NotificationsLikeViewHolder)holder, position);
        }else if (type.equals("comment")){
            populateTimelineComment((NotificationsCommentViewHolder)holder, position);
        }else {
            //onothing
        }
    }

    private void populateTimelineLike(final NotificationsLikeViewHolder holder, int position){
        Timeline timeline = getSnapshot(position).toObject(Timeline.class);
        holder.bindTimelineLike(timeline);
        final String activityId = timeline.getActivity_id();
        final String uid = timeline.getUser_id();
        final String status = timeline.getStatus();
        final String postId = timeline.getPost_id();
        firebaseAuth = FirebaseAuth.getInstance();

        usersCollection = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);
        timelineCollection = FirebaseFirestore.getInstance().collection(Constants.TIMELINE);
        postCollection = FirebaseFirestore.getInstance().collection(Constants.POSTS);
        collectionsPostCollections = FirebaseFirestore.getInstance().collection(Constants.COLLECTIONS_POSTS);
        senseCreditCollection = FirebaseFirestore.getInstance().collection(Constants.CREDITS);

        postCollection.document(postId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (documentSnapshot.exists()){
                    Post post = documentSnapshot.toObject(Post.class);
                    final String collectionId = post.getCollection_id();
                    final String type = post.getType();

                    collectionsPostCollections.document("collections").collection(collectionId)
                            .document(postId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {

                            if (e != null) {
                                Log.w(TAG, "Listen error", e);
                                return;
                            }

                            if (documentSnapshot.exists()){
                                CollectionPost collectionPost = documentSnapshot.toObject(CollectionPost.class);
                                final String image = collectionPost.getImage();

                                Picasso.with(mContext)
                                        .load(image)
                                        .resize(MAX_WIDTH, MAX_HEIGHT)
                                        .centerCrop()
                                        .placeholder(R.drawable.image_place_holder)
                                        .networkPolicy(NetworkPolicy.OFFLINE)
                                        .into(holder.postImageView, new Callback() {
                                            @Override
                                            public void onSuccess() {

                                            }

                                            @Override
                                            public void onError() {
                                                Picasso.with(mContext)
                                                        .load(image)
                                                        .resize(MAX_WIDTH, MAX_HEIGHT)
                                                        .centerCrop()
                                                        .placeholder(R.drawable.image_place_holder)
                                                        .into(holder.postImageView);

                                            }
                                        });

                                holder.postImageView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Intent intent = new Intent(mContext, PostDetailActivity.class);
                                        intent.putExtra(NotificationsAdapter.EXTRA_USER_UID, uid);
                                        intent.putExtra(NotificationsAdapter.EXTRA_POST_ID, postId);
                                        intent.putExtra(NotificationsAdapter.COLLECTION_ID, collectionId);
                                        intent.putExtra(NotificationsAdapter.TYPE, type);
                                        mContext.startActivity(intent);
                                    }
                                });
                            }

                        }
                    });
                }
            }
        });



        usersCollection.document(uid).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (documentSnapshot.exists()){
                    Andeqan cinggulan = documentSnapshot.toObject(Andeqan.class);
                    final String username = cinggulan.getUsername();
                    final String profileImage = cinggulan.getProfile_image();

                    Picasso.with(mContext)
                            .load(profileImage)
                            .resize(MAX_WIDTH, MAX_HEIGHT)
                            .onlyScaleDown()
                            .centerCrop()
                            .placeholder(R.drawable.ic_user)
                            .networkPolicy(NetworkPolicy.OFFLINE)
                            .into(holder.profileImageView, new Callback() {
                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onError() {
                                    Picasso.with(mContext)
                                            .load(profileImage)
                                            .resize(MAX_WIDTH, MAX_HEIGHT)
                                            .onlyScaleDown()
                                            .centerCrop()
                                            .placeholder(R.drawable.ic_user)
                                            .into(holder.profileImageView);

                                }
                            });


                    senseCreditCollection.document(postId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                            if (e != null) {
                                Log.w(TAG, "Listen error", e);
                                return;
                            }

                            if (documentSnapshot.exists()){

                                Credit credit = documentSnapshot.toObject(Credit.class);
                                final double senseCredit = credit.getAmount();

                                String boldText = username;
                                String normalText = " liked your post. Your new credo balance is now ";
                                DecimalFormat formatter = new DecimalFormat("0.00000000");
                                String creditText = formatter.format(senseCredit);
                                SpannableString likeText = new SpannableString(boldText + normalText + creditText);
                                likeText.setSpan(new StyleSpan(Typeface.BOLD), 0, boldText.length(),  Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                holder.usernameTextView.setText(likeText);

                            }else {
                                String boldText = username;
                                String normalText = " liked your post.";
                                SpannableString str = new SpannableString(boldText + normalText);
                                str.setSpan(new StyleSpan(Typeface.BOLD), 0, boldText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                holder.usernameTextView.setText(str);

                            }
                        }
                    });
                }
            }
        });

        holder.profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, ProfileActivity.class);
                intent.putExtra(NotificationsAdapter.EXTRA_USER_UID, uid);
                mContext.startActivity(intent);
            }
        });


        if (status.equals("un_read")){
            holder.usernameTextView.setTypeface(holder.usernameTextView.getTypeface(), Typeface.BOLD);
            holder.timelineLikeLinearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    timelineCollection.document(firebaseAuth.getCurrentUser().getUid())
                            .collection("activities").document(postId).update("status", "read");
                }
            });
        }else {
            holder.usernameTextView.setTypeface(holder.usernameTextView.getTypeface(), Typeface.NORMAL);
        }
    }

    private void populateTimelineComment(final NotificationsCommentViewHolder holder, int position){
        Timeline timeline = getSnapshot(position).toObject(Timeline.class);
        holder.bindTimelineComment(timeline);
        final String activityId = timeline.getActivity_id();
        final String uid = timeline.getUser_id();
        final String status = timeline.getStatus();
        final String postId = timeline.getPost_id();
        firebaseAuth = FirebaseAuth.getInstance();

        usersCollection = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);
        postCollection = FirebaseFirestore.getInstance().collection(Constants.POSTS);
        timelineCollection = FirebaseFirestore.getInstance().collection(Constants.TIMELINE);
        collectionsPostCollections = FirebaseFirestore.getInstance().collection(Constants.COLLECTIONS_POSTS);
        senseCreditCollection = FirebaseFirestore.getInstance().collection(Constants.CREDITS);
        commentCollection = FirebaseFirestore.getInstance().collection(Constants.COMMENTS);

        postCollection.document(postId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (documentSnapshot.exists()){
                    Post post = documentSnapshot.toObject(Post.class);
                    final String collectionId = post.getCollection_id();
                    final String type  = post.getType();

                    collectionsPostCollections.document("collections").collection(collectionId)
                            .document(postId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {

                            if (e != null) {
                                Log.w(TAG, "Listen error", e);
                                return;
                            }

                            if (documentSnapshot.exists()){
                                CollectionPost collectionPost = documentSnapshot.toObject(CollectionPost.class);
                                final String image = collectionPost.getImage();

                                Picasso.with(mContext)
                                        .load(image)
                                        .resize(MAX_WIDTH, MAX_HEIGHT)
                                        .centerCrop()
                                        .placeholder(R.drawable.image_place_holder)
                                        .networkPolicy(NetworkPolicy.OFFLINE)
                                        .into(holder.postImageView, new Callback() {
                                            @Override
                                            public void onSuccess() {

                                            }

                                            @Override
                                            public void onError() {
                                                Picasso.with(mContext)
                                                        .load(image)
                                                        .resize(MAX_WIDTH, MAX_HEIGHT)
                                                        .centerCrop()
                                                        .placeholder(R.drawable.image_place_holder)
                                                        .into(holder.postImageView);

                                            }
                                        });

                                holder.postImageView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Intent intent = new Intent(mContext, PostDetailActivity.class);
                                        intent.putExtra(NotificationsAdapter.EXTRA_USER_UID, uid);
                                        intent.putExtra(NotificationsAdapter.EXTRA_POST_ID, postId);
                                        intent.putExtra(NotificationsAdapter.COLLECTION_ID, collectionId);
                                        intent.putExtra(NotificationsAdapter.TYPE, type);
                                        mContext.startActivity(intent);
                                    }
                                });
                            }

                        }
                    });
                }
            }
        });


        usersCollection.document(uid).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (documentSnapshot.exists()){
                    final Andeqan cinggulan = documentSnapshot.toObject(Andeqan.class);
                    final String username = cinggulan.getUsername();
                    final String profileImage = cinggulan.getProfile_image();
                    final String uid = cinggulan.getUser_id();

                    Picasso.with(mContext)
                            .load(profileImage)
                            .resize(MAX_WIDTH, MAX_HEIGHT)
                            .onlyScaleDown()
                            .centerCrop()
                            .placeholder(R.drawable.ic_user)
                            .networkPolicy(NetworkPolicy.OFFLINE)
                            .into(holder.profileImageView, new Callback() {
                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onError() {
                                    Picasso.with(mContext)
                                            .load(profileImage)
                                            .resize(MAX_WIDTH, MAX_HEIGHT)
                                            .onlyScaleDown()
                                            .centerCrop()
                                            .placeholder(R.drawable.ic_user)
                                            .into(holder.profileImageView);

                                }
                            });

                    commentCollection.document("post_ids").collection(postId)
                            .document(activityId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                            if (e != null) {
                                Log.w(TAG, "Listen error", e);
                                return;
                            }

                            if (documentSnapshot.exists()){
                                Comment comment = documentSnapshot.toObject(Comment.class);
                                final String commmentText = comment.getComment_text();
                                String boldText = username;
                                String normalText = " commented on your post. " + '"' + commmentText + '"';
                                SpannableString str = new SpannableString(boldText + normalText);
                                str.setSpan(new StyleSpan(Typeface.BOLD), 0, boldText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                holder.usernameTextView.setText(str);
                            }else {
                                String boldText = username;
                                String normalText = " commented on your post.";
                                SpannableString str = new SpannableString(boldText + normalText);
                                str.setSpan(new StyleSpan(Typeface.BOLD), 0, boldText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                holder.usernameTextView.setText(str);
                            }
                        }
                    });




                }
            }
        });

        holder.profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, ProfileActivity.class);
                intent.putExtra(NotificationsAdapter.EXTRA_USER_UID, uid);
                mContext.startActivity(intent);
            }
        });


        if (status.equals("un_read")){
            holder.usernameTextView.setTypeface(holder.usernameTextView.getTypeface(), Typeface.BOLD);
            holder.timelineCommentLinearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    timelineCollection.document(firebaseAuth.getCurrentUser().getUid())
                            .collection("activities").document(activityId).update("status", "read");
                }
            });

        }else {
            holder.usernameTextView.setTypeface(holder.usernameTextView.getTypeface(), Typeface.NORMAL);
        }


    }

}

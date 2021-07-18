package com.andeka.andeka.notifications;

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

import com.andeka.andeka.Constants;
import com.andeka.andeka.R;
import com.andeka.andeka.post_detail.PostDetailActivity;
import com.andeka.andeka.models.Andeqan;
import com.andeka.andeka.models.Comment;
import com.andeka.andeka.models.Post;
import com.andeka.andeka.models.Relation;
import com.andeka.andeka.models.Timeline;
import com.andeka.andeka.profile.ProfileActivity;
import com.andeka.andeka.utils.BottomReachedListener;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Nullable;

/**
 * Created by J.EL on 1/18/2018.
 */

public class NotificationsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = NotificationsAdapter.class.getSimpleName();
    private Context mContext;
    private static final String EXTRA_USER_UID = "uid";
    private static final String EXTRA_POST_ID = "post id";
    private static final String COLLECTION_ID = "collection id";
    private static final String POST_HEIGHT = "height";
    private static final String POST_WIDTH = "width";
    private static final String TYPE = "type";

    private FirebaseAuth firebaseAuth;
    private static final int COMMENT_TYPE=1;
    private static final int FOLLOW_TYPE =2;
    private static final int LIKE_TYPE = 3;
    private boolean processFollow = false;

    private CollectionReference usersCollection;
    private CollectionReference postCollection;
    private CollectionReference peopleCollection;
    private CollectionReference timelineCollection;
    private CollectionReference commentCollection;
    private DatabaseReference databaseReference;
    private CollectionReference likeReference;
    //lists
    private List<DocumentSnapshot> documentSnapshots;

    private BottomReachedListener mBottomReachedListener;


    public NotificationsAdapter(Context mContext, List<DocumentSnapshot> documents) {
        this.mContext = mContext;
        this.documentSnapshots = new ArrayList<>();
        this.documentSnapshots = documents;
        initReferences();
    }

    public void setBottomReachedListener(BottomReachedListener bottomReachedListener){
        this.mBottomReachedListener = bottomReachedListener;
    }

    private void initReferences(){
        firebaseAuth = FirebaseAuth.getInstance();
        usersCollection = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);
        timelineCollection = FirebaseFirestore.getInstance().collection(Constants.TIMELINE);
        postCollection = FirebaseFirestore.getInstance().collection(Constants.POSTS);
        commentCollection = FirebaseFirestore.getInstance().collection(Constants.COMMENTS);
        peopleCollection = FirebaseFirestore.getInstance().collection(Constants.PEOPLE_RELATIONS);
        databaseReference = FirebaseDatabase.getInstance().getReference(Constants.RANDOM_PUSH_ID);
        likeReference = FirebaseFirestore.getInstance().collection(Constants.LIKES);
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
        if (getSnapshot(position) != null){
            Timeline timeline = getSnapshot(position).toObject(Timeline.class);
            if ( timeline.getType() != null &&  timeline.getType().equals("comment")){
                return COMMENT_TYPE;
            }else if ( timeline.getType() != null &&  timeline.getType().equals("follow")){
                return FOLLOW_TYPE;
            }else {
                return LIKE_TYPE;
            }
        }return position;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;

        switch (viewType){
            case COMMENT_TYPE:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.layout_notifications_comments, parent, false);
                return  new NotificationsCommentViewHolder(view);
            case FOLLOW_TYPE:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.layout_notifications_people, parent, false);
                return new PeopleNotificationsViewHolder(view);
            case LIKE_TYPE:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.layout_notification_like, parent, false);
                return new NotificationLikeViewHolder(view);
        }

        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        switch (holder.getItemViewType()){
            case COMMENT_TYPE:
                populateTimelineComment((NotificationsCommentViewHolder)holder, position);
                break;
            case FOLLOW_TYPE:
                populateNotificationPeople((PeopleNotificationsViewHolder)holder, position);
                break;
            case LIKE_TYPE:
                populateNotificationLike((NotificationLikeViewHolder) holder, position);
                break;
        }

        try {
            if (position == getItemCount() - 1){
                mBottomReachedListener.onBottomReached(position);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    private void populateTimelineComment(final NotificationsCommentViewHolder holder, int position){
        Timeline timeline = getSnapshot(position).toObject(Timeline.class);
        final String activityId = timeline.getActivity_id();
        final String uid = timeline.getUser_id();
        final String status = timeline.getStatus();
        final String postId = timeline.getPost_id();


        postCollection.document(postId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (documentSnapshot.exists()){
                    final Post post = documentSnapshot.toObject(Post.class);
                    final String collectionId = post.getCollection_id();
                    final String type  = post.getType();
                    final String url = post.getUrl();

                    Glide.with(mContext.getApplicationContext())
                            .load(url)
                            .apply(new RequestOptions()
                                    .placeholder(R.drawable.post_placeholder)
                                    .diskCacheStrategy(DiskCacheStrategy.DATA))
                            .into(holder.postImageView);

                    if (post.getWidth() != null && post.getHeight() != null){
                        holder.postImageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent =  new Intent(mContext, PostDetailActivity.class);
                                intent.putExtra(NotificationsAdapter.EXTRA_POST_ID, postId);
                                intent.putExtra(NotificationsAdapter.COLLECTION_ID, collectionId);
                                intent.putExtra(NotificationsAdapter.EXTRA_USER_UID, uid);
                                intent.putExtra(NotificationsAdapter.TYPE, type);
                                intent.putExtra(NotificationsAdapter.POST_HEIGHT, post.getHeight());
                                intent.putExtra(NotificationsAdapter.POST_WIDTH, post.getWidth());
                                mContext.startActivity(intent);
                            }
                        });
                    }else {
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

                    Glide.with(mContext.getApplicationContext())
                            .load(profileImage)
                            .apply(new RequestOptions()
                                    .placeholder(R.drawable.ic_user)
                                    .diskCacheStrategy(DiskCacheStrategy.DATA))
                            .into(holder.profileImageView);

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
                                String commentText = comment.getComment_text();
                                String boldText = username;
                                SpannableString str = new SpannableString(boldText);
                                str.setSpan(new StyleSpan(Typeface.BOLD), 0, boldText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                holder.usernameTextView.setText(str);
                                holder.activityTextView.setText("commented on your post. " + '"' + commentText + '"');
                            }else {
                                timelineCollection.document(firebaseAuth.getCurrentUser().getUid())
                                        .collection("activities").document(activityId)
                                        .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                    @Override
                                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                                        if (e != null) {
                                            Log.w(TAG, "Listen error", e);
                                            return;
                                        }

                                        if (documentSnapshot.exists()){
                                            timelineCollection.document(firebaseAuth.getCurrentUser().getUid())
                                                    .collection("activities").document(activityId)
                                                    .delete();
                                        }
                                    }
                                });
                            }
                        }
                    });

                }else {
                    timelineCollection.document(firebaseAuth.getCurrentUser().getUid())
                            .collection("activities").document(activityId)
                            .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                @Override
                                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                                    if (e != null) {
                                        Log.w(TAG, "Listen error", e);
                                        return;
                                    }

                                    if (documentSnapshot.exists()){
                                        timelineCollection.document(firebaseAuth.getCurrentUser().getUid())
                                                .collection("activities").document(activityId)
                                                .delete();
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

    private void populateNotificationPeople(final PeopleNotificationsViewHolder holder, int position){
        Timeline timeline = getSnapshot(position).toObject(Timeline.class);
        final String activityId = timeline.getActivity_id();
        final String status = timeline.getStatus();
        final String userId = timeline.getUser_id();

        holder.profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, ProfileActivity.class);
                intent.putExtra(NotificationsAdapter.EXTRA_USER_UID, userId);
                mContext.startActivity(intent);
            }
        });

        //show if following or not
        peopleCollection.document("followers").collection(userId)
                .whereEqualTo("following_id", firebaseAuth.getCurrentUser().getUid())
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                        if (e != null) {
                            Log.w(TAG, "Listen error", e);
                            return;
                        }

                        if (documentSnapshots.isEmpty()){
                            if (!userId.equals(firebaseAuth.getCurrentUser().getUid())){
                                holder.followButton.setVisibility(View.VISIBLE);
                                holder.followButton.setText("Follow");
                            }
                        }else {
                            if (!userId.equals(firebaseAuth.getCurrentUser().getUid())){
                                holder.followButton.setVisibility(View.VISIBLE);
                                holder.followButton.setText("Following");
                            }
                        }
                    }
                });

        //follow or unfollow
        holder.followButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                processFollow = true;
                peopleCollection.document("followers")
                        .collection(userId)
                        .whereEqualTo("following_id", firebaseAuth.getCurrentUser().getUid())
                        .addSnapshotListener(new EventListener<QuerySnapshot>() {
                            @Override
                            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {


                                if (e != null) {
                                    Log.w(TAG, "Listen error", e);
                                    return;
                                }

                                if (processFollow){
                                    if (documentSnapshots.isEmpty()){
                                        //set followers and following
                                        Relation follow = new Relation();
                                        follow.setFollowing_id(firebaseAuth.getCurrentUser().getUid());
                                        follow.setFollowed_id(userId);
                                        follow.setTime(System.currentTimeMillis());
                                        peopleCollection.document("followers").collection(userId)
                                                .document(firebaseAuth.getCurrentUser().getUid()).set(follow)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Timeline timeline = new Timeline();
                                                        final long time = new Date().getTime();
                                                        final String postid =  databaseReference.push().getKey();
                                                        timeline.setPost_id(userId);
                                                        timeline.setTime(time);
                                                        timeline.setUser_id(firebaseAuth.getCurrentUser().getUid());
                                                        timeline.setType("follow");
                                                        timeline.setActivity_id(postid);
                                                        timeline.setStatus("un_read");

                                                        timelineCollection.document(userId).collection("activities")
                                                                .document(firebaseAuth.getCurrentUser().getUid())
                                                                .set(timeline);
                                                    }
                                                });
                                        final Relation following = new Relation();
                                        following.setFollowing_id(firebaseAuth.getCurrentUser().getUid());
                                        following.setFollowed_id(userId);
                                        following.setType("following_user");
                                        following.setTime(System.currentTimeMillis());
                                        peopleCollection.document("following").collection(firebaseAuth.getCurrentUser().getUid())
                                                .document(userId).set(following);
                                        holder.followButton.setText("Following");

                                        processFollow = false;
                                    }else {
                                        peopleCollection.document("followers").collection(userId)
                                                .document(firebaseAuth.getCurrentUser().getUid()).delete();
                                        peopleCollection.document("following").collection(firebaseAuth.getCurrentUser().getUid())
                                                .document(userId).delete();
                                        holder.followButton.setText("Follow");
                                        processFollow = false;
                                    }
                                }
                            }
                        });

            }
        });

        usersCollection.document(userId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (documentSnapshot.exists()){
                    final Andeqan andeqan = documentSnapshot.toObject(Andeqan.class);
                    final String username = andeqan.getUsername();
                    final String profileImage = andeqan.getProfile_image();
                    final String uid = andeqan.getUser_id();

                    String boldText = username;
                    SpannableString str = new SpannableString(boldText);
                    str.setSpan(new StyleSpan(Typeface.BOLD), 0, boldText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    holder.usernameTextView.setText(str);
                    holder.activityTextView.setText(username + " is now following you");
                    Glide.with(mContext)
                            .load(profileImage)
                            .apply(new RequestOptions()
                                    .placeholder(R.drawable.ic_user)
                                    .diskCacheStrategy(DiskCacheStrategy.DATA))
                            .into(holder.profileImageView);
                }else {
                    timelineCollection.document(firebaseAuth.getCurrentUser().getUid())
                            .collection("activities").document(activityId)
                            .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                @Override
                                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                                    if (e != null) {
                                        Log.w(TAG, "Listen error", e);
                                        return;
                                    }

                                    if (documentSnapshot.exists()){
                                        timelineCollection.document(userId).collection("activities")
                                                .document(firebaseAuth.getCurrentUser().getUid())
                                                .delete();
                                    }
                                }
                            });
                }
            }
        });

    }

    private void populateNotificationLike(final NotificationLikeViewHolder holder, int position) {
        Timeline timeline = getSnapshot(position).toObject(Timeline.class);
        final String userId = timeline.getUser_id();
        final String activityId = timeline.getActivity_id();
        final String uid = timeline.getUser_id();
        final String postId = timeline.getPost_id();


        holder.profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, ProfileActivity.class);
                intent.putExtra(NotificationsAdapter.EXTRA_USER_UID, uid);
                mContext.startActivity(intent);
            }
        });

        postCollection.document(postId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (documentSnapshot.exists()){
                    final Post post = documentSnapshot.toObject(Post.class);
                    final String collectionId = post.getCollection_id();
                    final String type  = post.getType();
                    final String url = post.getUrl();

                    Glide.with(mContext.getApplicationContext())
                            .load(url)
                            .apply(new RequestOptions()
                                    .placeholder(R.drawable.post_placeholder)
                                    .diskCacheStrategy(DiskCacheStrategy.DATA))
                            .into(holder.postImageView);

                    if (post.getWidth() != null && post.getHeight() != null){
                        holder.postImageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent =  new Intent(mContext, PostDetailActivity.class);
                                intent.putExtra(NotificationsAdapter.EXTRA_POST_ID, postId);
                                intent.putExtra(NotificationsAdapter.COLLECTION_ID, collectionId);
                                intent.putExtra(NotificationsAdapter.EXTRA_USER_UID, uid);
                                intent.putExtra(NotificationsAdapter.TYPE, type);
                                intent.putExtra(NotificationsAdapter.POST_HEIGHT, post.getHeight());
                                intent.putExtra(NotificationsAdapter.POST_WIDTH, post.getWidth());
                                mContext.startActivity(intent);
                            }
                        });
                    }else {
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
            }
        });


        usersCollection.document(userId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (documentSnapshot.exists()){
                    final Andeqan andeqan = documentSnapshot.toObject(Andeqan.class);
                    final String username = andeqan.getUsername();
                    final String profileImage = andeqan.getProfile_image();
                    final String uid = andeqan.getUser_id();

                    String boldText = username;
                    SpannableString str = new SpannableString(boldText);
                    str.setSpan(new StyleSpan(Typeface.BOLD), 0, boldText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    holder.usernameTextView.setText(str);
                    holder.activityTextView.setText(username + " is now following you");
                    Glide.with(mContext)
                            .load(profileImage)
                            .apply(new RequestOptions()
                                    .placeholder(R.drawable.ic_user)
                                    .diskCacheStrategy(DiskCacheStrategy.DATA))
                            .into(holder.profileImageView);

                    timelineCollection.document("post_ids").collection(postId)
                            .document(userId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                            if (e != null) {
                                Log.w(TAG, "Listen error", e);
                                return;
                            }

                            if (documentSnapshot.exists()){
                                String boldText = username;
                                SpannableString str = new SpannableString(boldText);
                                str.setSpan(new StyleSpan(Typeface.BOLD), 0, boldText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                holder.usernameTextView.setText(str);
                                holder.activityTextView.setText("likes your post");
                            }else {
                                timelineCollection.document(firebaseAuth.getCurrentUser().getUid())
                                        .collection("activities").document(activityId)
                                        .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                            @Override
                                            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                                                if (e != null) {
                                                    Log.w(TAG, "Listen error", e);
                                                    return;
                                                }

                                                if (documentSnapshot.exists()){
                                                    timelineCollection.document(firebaseAuth.getCurrentUser().getUid())
                                                            .collection("activities").document(activityId)
                                                            .delete();
                                                }
                                            }
                                        });
                            }
                        }
                    });

                }else {
                    timelineCollection.document(firebaseAuth.getCurrentUser().getUid())
                            .collection("activities").document(activityId)
                            .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                @Override
                                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                                    if (e != null) {
                                        Log.w(TAG, "Listen error", e);
                                        return;
                                    }

                                    if (documentSnapshot.exists()){
                                        timelineCollection.document(userId).collection("activities")
                                                .document(firebaseAuth.getCurrentUser().getUid())
                                                .delete();
                                    }
                                }
                            });
                }
            }
        });

    }

}

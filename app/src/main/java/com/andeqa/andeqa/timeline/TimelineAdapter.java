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
import com.andeqa.andeqa.firestore.FirestoreAdapter;
import com.andeqa.andeqa.models.Andeqan;
import com.andeqa.andeqa.models.Comment;
import com.andeqa.andeqa.models.Credit;
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
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by J.EL on 1/18/2018.
 */

public class TimelineAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = TimelineAdapter.class.getSimpleName();
    private Context mContext;
    private static final String KEY_LAYOUT_POSITION = "layout pooition";
    private static final String EXTRA_USER_UID = "uid";
    private static final String EXTRA_POST_KEY = "post key";
    private static final int MAX_WIDTH = 200;
    private static final int MAX_HEIGHT = 200;
    private FirebaseAuth firebaseAuth;
    public static final int LIKE_TYPE=0;
    public static final int COMMENT_TYPE=1;
    public static final int RELATION_FOLLOWER_TYPE=2;
    private boolean processFollow = false;

    private CollectionReference usersCollection;
    private CollectionReference postCollection;
    private CollectionReference relationsCollection;
    private CollectionReference timelineCollection;
    private CollectionReference senseCreditCollection;
    private CollectionReference commentCollection;
    private DatabaseReference databaseReference;
    private List<DocumentSnapshot> documentSnapshots = new ArrayList<>();


    public TimelineAdapter(Context mContext) {
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
                        .inflate(R.layout.timeline_like_layout, parent, false);
                return new TimelineLikeViewHolder(view);
            case COMMENT_TYPE:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.timeline_comment_layout, parent, false);
                return  new TimelineCommentViewHolder(view);
            case RELATION_FOLLOWER_TYPE:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.timeline_relations_layout, parent, false);
                return  new TimelineRelationsViewHolder(view);
        }

        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Timeline timeline = getSnapshot(position).toObject(Timeline.class);
        final String type = timeline.getType();

        if (type.equals("like")){
            populateTimelineLike((TimelineLikeViewHolder)holder, position);
        }else if (type.equals("comment")){
            populateTimelineComment((TimelineCommentViewHolder)holder, position);
        }else {
            populateTimelineRelation((TimelineRelationsViewHolder)holder, position);
        }
    }

    private void populateTimelineLike(final TimelineLikeViewHolder holder, int position){
        Timeline timeline = getSnapshot(position).toObject(Timeline.class);
        holder.bindTimelineLike(timeline);
        final String postId = timeline.getPostId();
        final String uid = timeline.getUid();
        final String status = timeline.getStatus();
        final String pushId = timeline.getPushId();
        firebaseAuth = FirebaseAuth.getInstance();

        usersCollection = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);
        timelineCollection = FirebaseFirestore.getInstance().collection(Constants.TIMELINE);
        postCollection = FirebaseFirestore.getInstance().collection(Constants.COLLECTIONS);
        senseCreditCollection = FirebaseFirestore.getInstance().collection(Constants.SENSECREDITS);

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
                    final String profileImage = cinggulan.getProfileImage();

                    Picasso.with(mContext)
                            .load(profileImage)
                            .resize(MAX_WIDTH, MAX_HEIGHT)
                            .onlyScaleDown()
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
                                            .resize(MAX_WIDTH, MAX_HEIGHT)
                                            .onlyScaleDown()
                                            .centerCrop()
                                            .placeholder(R.drawable.profle_image_background)
                                            .into(holder.profileImageView);

                                }
                            });


                    senseCreditCollection.document(pushId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
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
                                String normalText = " liked your post. Your new Sense Credit balance is now ";
                                DecimalFormat formatter = new DecimalFormat("0.00000000");
                                String creditText = "SC" + " " + formatter.format(senseCredit);
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
                intent.putExtra(TimelineAdapter.EXTRA_USER_UID, uid);
                mContext.startActivity(intent);
            }
        });


        if (status.equals("unRead")){
            holder.statusView.setVisibility(View.VISIBLE);
            holder.timelineLikeLinearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    timelineCollection.document(firebaseAuth.getCurrentUser().getUid())
                            .collection("timeline").document(postId).update("status", "read");
                }
            });
        }else {
            holder.statusView.setVisibility(View.GONE);
        }
    }

    private void populateTimelineComment(final TimelineCommentViewHolder holder, int position){
        Timeline timeline = getSnapshot(position).toObject(Timeline.class);
        holder.bindTimelineComment(timeline);
        final String postId = timeline.getPostId();
        final String uid = timeline.getUid();
        final String status = timeline.getStatus();
        final String pushId = timeline.getPushId();
        firebaseAuth = FirebaseAuth.getInstance();

        usersCollection = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);
        postCollection = FirebaseFirestore.getInstance().collection(Constants.COLLECTIONS);
        timelineCollection = FirebaseFirestore.getInstance().collection(Constants.TIMELINE);
        senseCreditCollection = FirebaseFirestore.getInstance().collection(Constants.SENSECREDITS);
        commentCollection = FirebaseFirestore.getInstance().collection(Constants.COMMENTS);

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
                    final String profileImage = cinggulan.getProfileImage();
                    final String uid = cinggulan.getUid();

                    Picasso.with(mContext)
                            .load(profileImage)
                            .resize(MAX_WIDTH, MAX_HEIGHT)
                            .onlyScaleDown()
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
                                            .resize(MAX_WIDTH, MAX_HEIGHT)
                                            .onlyScaleDown()
                                            .centerCrop()
                                            .placeholder(R.drawable.profle_image_background)
                                            .into(holder.profileImageView);

                                }
                            });

                    commentCollection.document("push_ids").collection(pushId)
                            .document(postId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                            if (e != null) {
                                Log.w(TAG, "Listen error", e);
                                return;
                            }

                            if (documentSnapshot.exists()){
                                Comment comment = documentSnapshot.toObject(Comment.class);
                                final String commmentText = comment.getCommentText();
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
                intent.putExtra(TimelineAdapter.EXTRA_USER_UID, uid);
                mContext.startActivity(intent);
            }
        });

        if (status.equals("unRead")){
            holder.statusView.setVisibility(View.VISIBLE);
            holder.timelineCommentLinearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    timelineCollection.document(firebaseAuth.getCurrentUser().getUid())
                            .collection("timeline").document(postId).update("status", "read");
                }
            });

        }else {
            holder.statusView.setVisibility(View.GONE);
        }


    }

    private void populateTimelineRelation(final TimelineRelationsViewHolder holder, int position){
        Timeline timeline = getSnapshot(position).toObject(Timeline.class);
        final String uid = timeline.getUid();
        final String pushId = timeline.getPushId();
        final String status = timeline.getStatus();
        firebaseAuth = FirebaseAuth.getInstance();

        //firestore
        relationsCollection = FirebaseFirestore.getInstance().collection(Constants.RELATIONS);
        usersCollection = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);
        timelineCollection = FirebaseFirestore.getInstance().collection(Constants.TIMELINE);
        senseCreditCollection = FirebaseFirestore.getInstance().collection(Constants.SENSECREDITS);
        //firebase
        databaseReference = FirebaseDatabase.getInstance().getReference(Constants.RANDOM_PUSH_ID);


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
                    final String profileImage = cinggulan.getProfileImage();

                    Picasso.with(mContext)
                            .load(profileImage)
                            .resize(MAX_WIDTH, MAX_HEIGHT)
                            .onlyScaleDown()
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
                                            .resize(MAX_WIDTH, MAX_HEIGHT)
                                            .onlyScaleDown()
                                            .centerCrop()
                                            .placeholder(R.drawable.profle_image_background)
                                            .into(holder.profileImageView);

                                }
                            });

                    String boldText = username;
                    String normalText = " is now following you";
                    SpannableString str = new SpannableString(boldText + normalText);
                    str.setSpan(new StyleSpan(Typeface.BOLD), 0, boldText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    holder.usernameTextView.setText(str);
                }
            }
        });


        holder.followButton.setVisibility(View.VISIBLE);
        relationsCollection.document("following").collection(uid)
                .document(firebaseAuth.getCurrentUser().getUid())
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen error", e);
                            return;
                        }

                        if (documentSnapshot.exists()){
                            holder.followButton.setText("following");
                        }else {
                            holder.followButton.setText("follow");
                        }
                    }
                });


        holder.followButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                processFollow = true;
                relationsCollection.document("followers")
                        .collection(uid).whereEqualTo("uid", firebaseAuth.getCurrentUser().getUid())
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
                                        Relation follower = new Relation();
                                        follower.setUid(firebaseAuth.getCurrentUser().getUid());
                                        relationsCollection.document("followers").collection(uid)
                                                .document(firebaseAuth.getCurrentUser().getUid()).set(follower)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Timeline timeline = new Timeline();
                                                        final long time = new Date().getTime();

                                                        final String postid = databaseReference.push().getKey();
                                                        timeline.setPushId(uid);
                                                        timeline.setTime(time);
                                                        timeline.setUid(firebaseAuth.getCurrentUser().getUid());
                                                        timeline.setType("followers");
                                                        timeline.setPostId(postid);
                                                        timeline.setStatus("unRead");

                                                        timelineCollection.document(uid).collection("timeline").document(uid)
                                                                .set(timeline);
                                                    }
                                                });
                                        final Relation following = new Relation();
                                        following.setUid(uid);
                                        relationsCollection.document("following").collection(firebaseAuth.getCurrentUser().getUid())
                                                .document(uid).set(following);
                                        processFollow = false;
                                        holder.followButton.setText("Following");
                                    }else {
                                        relationsCollection.document("followers").collection(uid)
                                                .document(firebaseAuth.getCurrentUser().getUid()).delete();
                                        relationsCollection.document("following").collection(firebaseAuth.getCurrentUser().getUid())
                                                .document(uid).delete();
                                        processFollow = false;
                                        holder.followButton.setText("Follow");
                                    }
                                }
                            }
                        });

            }
        });
        holder.profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, ProfileActivity.class);
                intent.putExtra(TimelineAdapter.EXTRA_USER_UID, uid);
                mContext.startActivity(intent);
            }
        });

        if (status.equals("unRead")){
            holder.statusView.setVisibility(View.VISIBLE);
            holder.timelineRelationLinearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    timelineCollection.document(firebaseAuth.getCurrentUser().getUid())
                            .collection("timeline").document(uid).update("status", "read");
                }
            });
        }else {
            holder.statusView.setVisibility(View.GONE);
        }
    }



}

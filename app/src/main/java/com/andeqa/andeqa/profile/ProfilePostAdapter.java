package com.andeqa.andeqa.profile;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintSet;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.R;
import com.andeqa.andeqa.comments.CommentsActivity;
import com.andeqa.andeqa.home.PostDetailActivity;
import com.andeqa.andeqa.home.VideoPostViewHolder;
import com.andeqa.andeqa.impressions.ImpressionTracker;
import com.andeqa.andeqa.models.Andeqan;
import com.andeqa.andeqa.models.CollectionPost;
import com.andeqa.andeqa.models.Credit;
import com.andeqa.andeqa.models.Like;
import com.andeqa.andeqa.models.Post;
import com.andeqa.andeqa.models.Timeline;
import com.andeqa.andeqa.models.VideoPost;
import com.andeqa.andeqa.player.Player;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.WeakHashMap;

public class ProfilePostAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = ProfilePostsAdapter.class.getSimpleName();
    private Context mContext;
    //firestore
    private FirebaseFirestore firebaseFirestore;
    private CollectionReference collectionsPosts;
    private com.google.firebase.firestore.Query commentsCountQuery;
    private CollectionReference usersReference;
    private CollectionReference commentsReference;
    private CollectionReference likesReference;
    private CollectionReference relationsReference;
    private CollectionReference timelineCollection;
    private Query likesQuery;
    //firebase
    private DatabaseReference databaseReference;
    private DatabaseReference impressionReference;
    //firebase adapter
    private FirestoreRecyclerAdapter firestoreRecyclerAdapter;
    //firebase auth
    private FirebaseAuth firebaseAuth;
    private boolean processLikes = false;
    private boolean processCredit = false;
    private boolean showOnClick = false;
    private boolean processDislikes = false;
    private static final double GOLDEN_RATIO = 1.618;
    private static final double DEFAULT_PRICE = 1.5;
    private static final String COLLECTION_ID = "collection id";
    private static final String EXTRA_POST_ID = "post id";
    private static final String EXTRA_USER_UID = "uid";
    private static final String TYPE = "type";
    private static final String POST_HEIGHT = "height";
    private static final String POST_WIDTH = "width";
    private  static final int MAX_WIDTH = 200;
    private static final int MAX_HEIGHT = 200;
    private static final int VIDEO_POST = 1;
    private static final int IMAGE_POST = 2;
    private ConstraintSet constraintSet;
    private List<DocumentSnapshot> documentSnapshots = new ArrayList<>();
    private Player player;
    private long startTime;
    private long stopTime;
    private long duration;
    private ImpressionTracker impressionTracker;
    private final WeakHashMap<View, Integer> mViewPositionMap = new WeakHashMap<>();


    public ProfilePostAdapter(Context context) {
        this.mContext = context;
        initReferences();

    }

    @Override
    public int getItemCount() {
        return documentSnapshots.size();
    }

    protected void setCollectionPosts(List<DocumentSnapshot> mSnapshots){
        this.documentSnapshots = mSnapshots;
        notifyDataSetChanged();
    }

    public DocumentSnapshot getSnapshot(int index) {
        return documentSnapshots.get(index);
    }



    private void initReferences(){
        firebaseAuth = FirebaseAuth.getInstance();
        usersReference = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);
        timelineCollection = FirebaseFirestore.getInstance().collection(Constants.TIMELINE);
        //firebase
        databaseReference = FirebaseDatabase.getInstance().getReference(Constants.RANDOM_PUSH_ID);
        //document reference
        commentsReference  = FirebaseFirestore.getInstance().collection(Constants.COMMENTS);
        commentsCountQuery= commentsReference;
        likesReference = FirebaseFirestore.getInstance().collection(Constants.LIKES);
        //firebase database references
        impressionReference = FirebaseDatabase.getInstance().getReference(Constants.VIEWS);

    }
    @Override
    public int getItemViewType(int position) {
        Post post = getSnapshot(position).toObject(Post.class);
        final String type = post.getType();
        if (type.equals("single_video_post") || type.equals("collection_video_post")){
            return VIDEO_POST;
        }else {
            return IMAGE_POST;
        }
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case VIDEO_POST:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.layout_profile_video, parent, false);
                return new VideoPostViewHolder(view);
            case IMAGE_POST:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.layout_profile_posts, parent, false);
                return new ProfilePostViewHolder(view);
        }
        return null;
    }


    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        final Post post = getSnapshot(holder.getAdapterPosition()).toObject(Post.class);
        final String type = post.getType();

        if (type.equals("single_video_post") || type.equals("collection_video_post")){
            populateVideo((VideoPostViewHolder)holder, position);
        }else {
            populateImage((ProfilePostViewHolder)holder, position);

        }
    }


    private void populateVideo(final VideoPostViewHolder holder, final int position){
        final Post post = getSnapshot(holder.getAdapterPosition()).toObject(Post.class);
        final String postId = post.getPost_id();
        final String uid = post.getUser_id();
        final String collectionId = post.getCollection_id();
        final String type = post.getType();


        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser()!= null){
            //firestore

            if (type.equals("single") || type.equals("single_video_post")){
                collectionsPosts = FirebaseFirestore.getInstance().collection(Constants.COLLECTIONS_POSTS)
                        .document("singles").collection(collectionId);
            }else {
                collectionsPosts = FirebaseFirestore.getInstance().collection(Constants.COLLECTIONS_POSTS)
                        .document("collections").collection(collectionId);
            }


        }

        commentsReference.document("post_ids").collection(postId);
        collectionsPosts.document(postId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }
                if (documentSnapshot.exists()){
                    final VideoPost videoPost = documentSnapshot.toObject(VideoPost.class);
                    Log.d("video post url", videoPost.getVideo());
                    player = new Player(mContext.getApplicationContext(), holder.postVideoView);
                    player.addMedia(videoPost.getVideo());
                    holder.playImageView.setVisibility(View.VISIBLE);
                    holder.puaseImageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (holder.postVideoView.getPlayer() == null){
                                holder.postVideoView.getPlayer().setPlayWhenReady(true);
                                holder.puaseImageView.setVisibility(View.VISIBLE);
                            }
                        }
                    });

                    if (!TextUtils.isEmpty(videoPost.getTitle())){
                        holder.bottomLinearLayout.setVisibility(View.VISIBLE);
                        holder.titleTextView.setText(videoPost.getTitle());
                        holder.titleRelativeLayout.setVisibility(View.VISIBLE);
                    }else {
                        holder.titleRelativeLayout.setVisibility(View.GONE);
                    }

                    if (!TextUtils.isEmpty(videoPost.getDescription())){
                        //prevent collection note from overlapping other layouts
                        final String [] strings = videoPost.getDescription().split("");
                        final int size = strings.length;
                        if (size <= 120){
                            holder.bottomLinearLayout.setVisibility(View.VISIBLE);
                            holder.descriptionRelativeLayout.setVisibility(View.VISIBLE);
                            holder.descriptionTextView.setText(videoPost.getDescription());
                        }else{
                            holder.bottomLinearLayout.setVisibility(View.VISIBLE);
                            holder.descriptionRelativeLayout.setVisibility(View.VISIBLE);
                            final String boldMore = "...";
                            final String boldLess = "";
                            String normalText = videoPost.getDescription().substring(0, 119);
                            holder.descriptionTextView.setText(normalText + boldMore);
                            holder.descriptionRelativeLayout.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (showOnClick){
                                        String normalText = videoPost.getDescription();
                                        holder.descriptionTextView.setText(normalText + boldLess);
                                        showOnClick = false;
                                    }else {
                                        String normalText = videoPost.getDescription().substring(0, 119);
                                        holder.descriptionTextView.setText(normalText + boldMore);
                                        showOnClick = true;
                                    }
                                }
                            });
                        }
                    }else {
                        holder.descriptionRelativeLayout.setVisibility(View.GONE);
                    }
                }
            }
        });

        holder.mCommentsLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent =  new Intent(mContext, CommentsActivity.class);
                intent.putExtra(ProfilePostAdapter.EXTRA_POST_ID, postId);
                intent.putExtra(ProfilePostAdapter.COLLECTION_ID, collectionId);
                intent.putExtra(ProfilePostAdapter.TYPE, type);
                mContext.startActivity(intent);
            }
        });

        holder.postVideoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent =  new Intent(mContext, PostDetailActivity.class);
                intent.putExtra(ProfilePostAdapter.EXTRA_POST_ID, postId);
                intent.putExtra(ProfilePostAdapter.COLLECTION_ID, collectionId);
                intent.putExtra(ProfilePostAdapter.EXTRA_USER_UID, uid);
                intent.putExtra(ProfilePostAdapter.TYPE, type);
                mContext.startActivity(intent);
            }
        });

        holder.profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, ProfileActivity.class);
                intent.putExtra(ProfilePostAdapter.EXTRA_USER_UID, uid);
                mContext.startActivity(intent);
            }
        });


        usersReference.document(uid).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (documentSnapshot.exists()){
                    final Andeqan andeqan = documentSnapshot.toObject(Andeqan.class);
                    holder.usernameTextView.setText(andeqan.getUsername());

                    Glide.with(mContext)
                            .load(andeqan.getProfile_image())
                            .apply(new RequestOptions()
                                    .placeholder(R.drawable.ic_user)
                                    .diskCacheStrategy(DiskCacheStrategy.NONE))
                            .into(holder.profileImageView);
                }
            }
        });

        //get the number of commments in a single
        commentsCountQuery.orderBy("comment_id").whereEqualTo("post_id", postId)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                        if (e != null) {
                            Log.w(TAG, "Listen error", e);
                            return;
                        }

                        if (!documentSnapshots.isEmpty()){
                            final int commentsCount = documentSnapshots.size();
                            holder.commentsCountTextView.setText(commentsCount + "");
                        }else {
                            holder.commentsCountTextView.setText("0");
                        }
                    }
                });


        likesReference.document(postId).collection("likes")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                        if (e != null) {
                            Log.w(TAG, "Listen error", e);
                            return;
                        }

                        if (!documentSnapshots.isEmpty()){
                            holder.likesCountTextView.setText(documentSnapshots.size() + " ");
                        }else {
                            holder.likesCountTextView.setText("0");
                        }

                    }
                });

        likesReference.document(postId).collection("dislikes")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                        if (e != null) {
                            Log.w(TAG, "Listen error", e);
                            return;
                        }

                        if (!documentSnapshots.isEmpty()){
                            holder.dislikeCountTextView.setText(documentSnapshots.size() + " ");
                        }else {
                            holder.dislikeCountTextView.setText("0");
                        }

                    }
                });


        //color the like image view if the user has liked
        likesReference.document(postId).collection("likes")
                .whereEqualTo("user_id", firebaseAuth.getCurrentUser().getUid())
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                        if (e != null) {
                            Log.w(TAG, "Listen error", e);
                            return;
                        }

                        if (documentSnapshots.isEmpty()){
                            //change the like image view backgroud color
                            holder.likesImageView.setColorFilter(Color.BLACK);
                        }else {
                            //changed the like image view background color to show user has liked
                            holder.likesImageView.setColorFilter(Color.RED);
                        }

                    }
                });


//      color the like image view if the user has dislikes
        likesReference.document(postId).collection("dislikes")
                .whereEqualTo("user_id", firebaseAuth.getCurrentUser().getUid())
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                        if (e != null) {
                            Log.w(TAG, "Listen error", e);
                            return;
                        }

                        if (documentSnapshots.isEmpty()){
                            //changed the dislike image view background color to show user has not disliked
                            holder.dislikeImageView.setColorFilter(Color.BLACK);

                        }else {
                            //changed the dislike image view background color to show user has disliked
                            holder.dislikeImageView.setColorFilter(Color.RED);

                        }

                    }
                });


        holder.dislikeLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                processDislikes = true;
                likesReference.document(postId).collection("dislikes")
                        .whereEqualTo("user_id", firebaseAuth.getCurrentUser().getUid())
                        .addSnapshotListener(new EventListener<QuerySnapshot>() {
                            @Override
                            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                                if (e != null) {
                                    Log.w(TAG, "Listen error", e);
                                    return;
                                }


                                if (processDislikes){
                                    if (documentSnapshots.isEmpty()){
                                        Like like = new Like();
                                        like.setUser_id(firebaseAuth.getCurrentUser().getUid());
                                        likesReference.document(postId).collection("dislikes")
                                                .document(firebaseAuth.getCurrentUser().getUid()).set(like);
                                        processDislikes = false;
                                        holder.dislikeImageView.setColorFilter(Color.RED);

                                    }else {
                                        likesReference.document(postId).collection("dislikes")
                                                .document(firebaseAuth.getCurrentUser().getUid()).delete();
                                        processDislikes = false;
                                        holder.dislikeImageView.setColorFilter(Color.BLACK);

                                    }
                                }

                            }
                        });
            }
        });


        holder.likesRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                processLikes = true;
                processCredit = true;
                likesReference.document(postId).collection("likes")
                        .whereEqualTo("user_id", firebaseAuth.getCurrentUser().getUid())
                        .addSnapshotListener(new EventListener<QuerySnapshot>() {
                            @Override
                            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                                if (e != null) {
                                    Log.w(TAG, "Listen error", e);
                                    return;
                                }


                                if (processLikes){
                                    if (documentSnapshots.isEmpty()){
                                        final Like like = new Like();
                                        like.setUser_id(firebaseAuth.getCurrentUser().getUid());
                                        likesReference.document(postId).collection("likes")
                                                .document(firebaseAuth.getCurrentUser().getUid()).set(like);

                                        timelineCollection.document(uid).collection("activities")
                                                .whereEqualTo("post_id", postId)
                                                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                                    @Override
                                                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                                                        if (e != null) {
                                                            Log.w(TAG, "Listen error", e);
                                                            return;
                                                        }


                                                        if (documentSnapshots.isEmpty()){
                                                            Log.d("timeline is empty", postId);
                                                            final Timeline timeline = new Timeline();
                                                            final long time = new Date().getTime();

                                                            final String activityId = databaseReference.push().getKey();
                                                            timeline.setPost_id(postId);
                                                            timeline.setTime(time);
                                                            timeline.setUser_id(firebaseAuth.getCurrentUser().getUid());
                                                            timeline.setType("like");
                                                            timeline.setActivity_id(activityId);
                                                            timeline.setStatus("un_read");
                                                            timeline.setReceiver_id(uid);

                                                            if (uid.equals(firebaseAuth.getCurrentUser().getUid())){
                                                                //do nothing
                                                            }else {
                                                                timelineCollection.document(uid).collection("activities")
                                                                        .document(postId).set(timeline);
                                                            }
                                                        }
                                                    }
                                                });



                                        processLikes = false;
                                        holder.likesImageView.setColorFilter(Color.RED);

                                    }else {
                                        likesReference.document(postId).collection("likes")
                                                .document(firebaseAuth.getCurrentUser().getUid()).delete();
                                        processLikes = false;
                                        holder.likesImageView.setColorFilter(Color.BLACK);

                                    }
                                }

                            }
                        });
            }
        });


    }

    private void populateImage(final ProfilePostViewHolder holder, final int position){
        final Post post = getSnapshot(holder.getAdapterPosition()).toObject(Post.class);
        final String postId = post.getPost_id();
        final String uid = post.getUser_id();
        final String collectionId = post.getCollection_id();
        final String type = post.getType();


        if (type.equals("single") || type.equals("single_image_post")){
            collectionsPosts = FirebaseFirestore.getInstance().collection(Constants.COLLECTIONS_POSTS)
                    .document("singles").collection(collectionId);
        }else {
            collectionsPosts = FirebaseFirestore.getInstance().collection(Constants.COLLECTIONS_POSTS)
                    .document("collections").collection(collectionId);
        }

        commentsReference.document("post_ids").collection(postId);
        collectionsPosts.document(postId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (documentSnapshot.exists()){
                    final CollectionPost collectionPost = documentSnapshot.toObject(CollectionPost.class);

                    Glide.with(mContext.getApplicationContext())
                            .load(collectionPost.getImage())
                            .apply(new RequestOptions()
                                    .placeholder(R.drawable.post_placeholder)
                                    .diskCacheStrategy(DiskCacheStrategy.DATA))
                            .into(holder.postImageView);

                }
            }
        });


        holder.mCommentsLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, CommentsActivity.class);
                intent.putExtra(ProfilePostAdapter.COLLECTION_ID, collectionId);
                intent.putExtra(ProfilePostAdapter.EXTRA_POST_ID, postId);
                intent.putExtra(ProfilePostAdapter.TYPE, type);
                mContext.startActivity(intent);
            }
        });


        if (post.getWidth() != null && post.getHeight() != null){
            holder.postImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent =  new Intent(mContext, PostDetailActivity.class);
                    intent.putExtra(ProfilePostAdapter.EXTRA_POST_ID, postId);
                    intent.putExtra(ProfilePostAdapter.COLLECTION_ID, collectionId);
                    intent.putExtra(ProfilePostAdapter.EXTRA_USER_UID, uid);
                    intent.putExtra(ProfilePostAdapter.TYPE, type);
                    intent.putExtra(ProfilePostAdapter.POST_HEIGHT, post.getHeight());
                    intent.putExtra(ProfilePostAdapter.POST_WIDTH, post.getWidth());
                    mContext.startActivity(intent);
                }
            });
        }else {
            holder.postImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent =  new Intent(mContext, PostDetailActivity.class);
                    intent.putExtra(ProfilePostAdapter.EXTRA_POST_ID, postId);
                    intent.putExtra(ProfilePostAdapter.COLLECTION_ID, collectionId);
                    intent.putExtra(ProfilePostAdapter.EXTRA_USER_UID, uid);
                    intent.putExtra(ProfilePostAdapter.TYPE, type);
                    mContext.startActivity(intent);
                }
            });

        }

        usersReference.document(uid).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (documentSnapshot.exists()) {
                    final Andeqan andeqan = documentSnapshot.toObject(Andeqan.class);
                    final String username = andeqan.getUsername();
                    final String profileImage = andeqan.getProfile_image();

                    holder.usernameTextView.setText(username);
                    Glide.with(mContext.getApplicationContext())
                            .load(andeqan.getProfile_image())
                            .apply(new RequestOptions()
                                    .placeholder(R.drawable.ic_user)
                                    .diskCacheStrategy(DiskCacheStrategy.DATA))
                            .into(holder.profileImageView);
                }
            }
        });



        //get the number of commments in a single
        commentsCountQuery.orderBy("comment_id").whereEqualTo("post_id", postId)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                        if (e != null) {
                            Log.w(TAG, "Listen error", e);
                            return;
                        }

                        if (!documentSnapshots.isEmpty()){
                            final int commentsCount = documentSnapshots.size();
                            holder.commentsCountTextView.setText(commentsCount + "");
                        }else {
                            holder.commentsCountTextView.setText("0");
                        }
                    }
                });

    }


    //region listeners
    private static double roundCredits(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }


}

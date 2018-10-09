package com.andeqa.andeqa.collections;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintSet;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.R;
import com.andeqa.andeqa.home.PostDetailActivity;
import com.andeqa.andeqa.home.PhotoPostViewHolder;
import com.andeqa.andeqa.home.VideoDetailActivity;
import com.andeqa.andeqa.home.VideoPostViewHolder;
import com.andeqa.andeqa.impressions.ImpressionTracker;
import com.andeqa.andeqa.models.Andeqan;
import com.andeqa.andeqa.models.CollectionPost;
import com.andeqa.andeqa.models.Post;
import com.andeqa.andeqa.player.Player;
import com.andeqa.andeqa.profile.ProfileActivity;
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
import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;

public class MinePostsAdapters extends RecyclerView.Adapter<RecyclerView.ViewHolder>
        implements ImpressionTracker.VisibilityTrackerListener{
    private static final String TAG = MinePostsAdapters.class.getSimpleName();
    private Context mContext;
    //firestore
    private CollectionReference collectionsPosts;
    private com.google.firebase.firestore.Query commentsCountQuery;
    private CollectionReference usersReference;
    private CollectionReference commentsReference;
    private CollectionReference creditsCollection;
    private CollectionReference likesReference;
    private CollectionReference timelineCollection;
    private CollectionReference collectionPostReference;
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
    private boolean processDislikes = false;
    private boolean showOnClick = false;

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
    private List<DocumentSnapshot> documentSnapshots = new ArrayList<>();
    private ConstraintSet constraintSet;
    private Player player;
    //impressions
    private long startTime;
    private long stopTime;
    private long duration;
    private ImpressionTracker impressionTracker;
    private final WeakHashMap<View, Integer> mViewPositionMap = new WeakHashMap<>();


    public MinePostsAdapters(Activity activity) {
        this.mContext = activity;
        impressionTracker = new ImpressionTracker(activity);
        initReferences();

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
        if (firebaseAuth.getCurrentUser() != null){
            //firestore references
            usersReference = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);
            timelineCollection = FirebaseFirestore.getInstance().collection(Constants.TIMELINE);
            collectionsPosts = FirebaseFirestore.getInstance().collection(Constants.COLLECTIONS_OF_POSTS);
            commentsReference  = FirebaseFirestore.getInstance().collection(Constants.COMMENTS);
            commentsCountQuery= commentsReference;
            likesReference = FirebaseFirestore.getInstance().collection(Constants.LIKES);
            //firebase database references
            impressionReference = FirebaseDatabase.getInstance().getReference(Constants.VIEWS);
            impressionReference.keepSynced(true);
            databaseReference = FirebaseDatabase.getInstance().getReference(Constants.RANDOM_PUSH_ID);
            collectionPostReference = FirebaseFirestore.getInstance().collection(Constants.COLLECTIONS);

        }

    }

    @Override
    public int getItemViewType(int position) {
        Post post = getSnapshot(position).toObject(Post.class);
        final String type = post.getType();
        if (type.equals("collection_video_post")){
            return VIDEO_POST;
        }else {
            return IMAGE_POST;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case VIDEO_POST:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.layout_video_post, parent, false);
                return new VideoPostViewHolder(view);
            case IMAGE_POST:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.layout_image_post, parent, false);
                return new PhotoPostViewHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(final @NonNull RecyclerView.ViewHolder holder, int position) {
        final Post post = getSnapshot(holder.getAdapterPosition()).toObject(Post.class);
        final String type = post.getType();

        if (type.equals("collection_video_post")){
//            populateVideo((VideoPostViewHolder) holder, position);
        }else {
            populateImage((PhotoPostViewHolder)holder, position);
        }

    }

    @Override
    public void onVisibilityChanged(List<View> visibleViews, List<View> invisibleViews) {


    }

    private void populateVideo(final VideoPostViewHolder holder, final int position){
        final Post post = getSnapshot(position).toObject(Post.class);
        final String postId = post.getPost_id();
        final String uid = post.getUser_id();
        final String collectionId = post.getCollection_id();
        final String type = post.getType();


        player = new Player(mContext.getApplicationContext(), holder.postVideoView);
        player.addMedia(post.getUrl());
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

        if (!TextUtils.isEmpty(post.getTitle())){
            holder.bottomLinearLayout.setVisibility(View.VISIBLE);
            holder.titleTextView.setText(post.getTitle());
            holder.titleRelativeLayout.setVisibility(View.VISIBLE);
        }else {
            holder.titleRelativeLayout.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(post.getDescription())){
            //prevent collection note from overlapping other layouts
            final String [] strings = post.getDescription().split("");
            final int size = strings.length;
            if (size <= 50){
                holder.bottomLinearLayout.setVisibility(View.VISIBLE);
                holder.descriptionRelativeLayout.setVisibility(View.VISIBLE);
                holder.descriptionTextView.setText(post.getDescription());
            }else{
                holder.bottomLinearLayout.setVisibility(View.VISIBLE);
                holder.descriptionRelativeLayout.setVisibility(View.VISIBLE);
                final String boldMore = "...";
                final String boldLess = "";
                String normalText = post.getDescription().substring(0, 49);
                holder.descriptionTextView.setText(normalText + boldMore);
                holder.descriptionRelativeLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (showOnClick){
                            String normalText = post.getDescription();
                            holder.descriptionTextView.setText(normalText + boldLess);
                            showOnClick = false;
                        }else {
                            String normalText = post.getDescription().substring(0, 49);
                            holder.descriptionTextView.setText(normalText + boldMore);
                            showOnClick = true;
                        }
                    }
                });
            }
        }else {
            holder.descriptionRelativeLayout.setVisibility(View.GONE);
        }

        holder.postVideoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent =  new Intent(mContext, VideoDetailActivity.class);
                intent.putExtra(MinePostsAdapters.EXTRA_POST_ID, postId);
                intent.putExtra(MinePostsAdapters.COLLECTION_ID, collectionId);
                intent.putExtra(MinePostsAdapters.EXTRA_USER_UID, uid);
                intent.putExtra(MinePostsAdapters.TYPE, type);
                mContext.startActivity(intent);
            }
        });

        holder.profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, ProfileActivity.class);
                intent.putExtra(MinePostsAdapters.EXTRA_USER_UID, uid);
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
        commentsReference.document("post_ids").collection(postId)
                .orderBy("comment_id").whereEqualTo("post_id", postId)
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


    private void populateImage(final PhotoPostViewHolder holder, int position){
        final Post post = getSnapshot(position).toObject(Post.class);
        final String postId = post.getPost_id();
        final String uid = post.getUser_id();
        final String collectionId = post.getCollection_id();
        final String type = post.getType();

        if (post.getHeight() != null && post.getWidth() != null){
            final float width = (float) Integer.parseInt(post.getWidth());
            final float height = (float) Integer.parseInt(post.getHeight());
            float ratio = height/width;

            constraintSet = new ConstraintSet();
            constraintSet.clone(holder.postConstraintLayout);
            constraintSet.setDimensionRatio(holder.postImageView.getId(), "H," + ratio);
            holder.postImageView.setImageResource(R.drawable.post_placeholder);
            constraintSet.applyTo(holder.postConstraintLayout);

        }else {
            constraintSet = new ConstraintSet();
            constraintSet.clone(holder.postConstraintLayout);
            constraintSet.setDimensionRatio(holder.postImageView.getId(), "H," + 1);
            holder.postImageView.setImageResource(R.drawable.post_placeholder);
            constraintSet.applyTo(holder.postConstraintLayout);

        }

        //calculate view visibility and add visible views to impression tracker
        mViewPositionMap.put(holder.itemView, position);
        impressionTracker.addView(holder.itemView, 100, postId);

        if (post.getUrl() == null){
            //firebase firestore references
            if (type.equals("single")|| type.equals("single_image_post")){
                collectionsPosts = FirebaseFirestore.getInstance().collection(Constants.COLLECTIONS_OF_POSTS)
                        .document("singles").collection(collectionId);
            }else{
                collectionsPosts = FirebaseFirestore.getInstance().collection(Constants.COLLECTIONS_OF_POSTS)
                        .document("collections").collection(collectionId);
            }
            collectionsPosts.document(postId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                    if (e != null) {
                        Log.w(TAG, "Listen error", e);
                        return;
                    }

                    if (documentSnapshot.exists()){
                        final CollectionPost collectionPost = documentSnapshot.toObject(CollectionPost.class);
                        //set the image on the image view
                        Glide.with(mContext.getApplicationContext())
                                .load(collectionPost.getImage())
                                .apply(new RequestOptions()
                                        .placeholder(R.drawable.post_placeholder)
                                        .diskCacheStrategy(DiskCacheStrategy.DATA))
                                .into(holder.postImageView);

                        if (!TextUtils.isEmpty(collectionPost.getTitle())){
                            holder.captionLinearLayout.setVisibility(View.VISIBLE);
                            holder.titleTextView.setText(collectionPost.getTitle());
                            holder.titleRelativeLayout.setVisibility(View.VISIBLE);
                        }else {
                            holder.titleRelativeLayout.setVisibility(View.GONE);
                        }

                        if (!TextUtils.isEmpty(collectionPost.getDescription())){
                            //prevent collection note from overlapping other layouts
                            final String [] strings = collectionPost.getDescription().split("");
                            final int size = strings.length;
                            if (size <= 50){
                                holder.captionLinearLayout.setVisibility(View.VISIBLE);
                                holder.descriptionRelativeLayout.setVisibility(View.VISIBLE);
                                holder.descriptionTextView.setText(collectionPost.getDescription());
                            }else{
                                holder.captionLinearLayout.setVisibility(View.VISIBLE);
                                holder.descriptionRelativeLayout.setVisibility(View.VISIBLE);
                                final String boldMore = "...";
                                String normalText = collectionPost.getDescription().substring(0, 49);
                                holder.descriptionTextView.setText(normalText + boldMore);
                            }
                        }else {
                            holder.captionLinearLayout.setVisibility(View.GONE);
                        }
                    }else {
                        //post does not exist
                    }
                }
            });
        }else {
            Glide.with(mContext.getApplicationContext())
                    .load(post.getUrl())
                    .apply(new RequestOptions()
                            .placeholder(R.drawable.post_placeholder)
                            .diskCacheStrategy(DiskCacheStrategy.DATA))
                    .into(holder.postImageView);

            if (!TextUtils.isEmpty(post.getTitle())){
                holder.captionLinearLayout.setVisibility(View.VISIBLE);
                holder.titleTextView.setText(post.getTitle());
                holder.titleRelativeLayout.setVisibility(View.VISIBLE);
            }else {
                holder.titleRelativeLayout.setVisibility(View.GONE);
            }

            if (!TextUtils.isEmpty(post.getDescription())){
                //prevent collection note from overlapping other layouts
                final String [] strings = post.getDescription().split("");
                final int size = strings.length;
                if (size <= 50){
                    holder.captionLinearLayout.setVisibility(View.VISIBLE);
                    holder.descriptionRelativeLayout.setVisibility(View.VISIBLE);
                    holder.descriptionTextView.setText(post.getDescription());
                }else{
                    holder.captionLinearLayout.setVisibility(View.VISIBLE);
                    holder.descriptionRelativeLayout.setVisibility(View.VISIBLE);
                    final String boldMore = "...";
                    String normalText = post.getDescription().substring(0, 49);
                    holder.descriptionTextView.setText(normalText + boldMore);
                }
            }else {
                holder.captionLinearLayout.setVisibility(View.GONE);
            }
        }

        if (!TextUtils.isEmpty(post.getTitle())){
            holder.titleTextView.setText(post.getTitle());
            holder.titleRelativeLayout.setVisibility(View.GONE);
        }else {
            holder.titleTextView.setText("");
            holder.titleRelativeLayout.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(post.getDescription())){
            final String [] strings = post.getDescription().split("");

            final int size = strings.length;

            if (size <= 120){
                holder.descriptionRelativeLayout.setVisibility(View.VISIBLE);
                holder.descriptionTextView.setText(post.getDescription());
            }else{

                holder.descriptionRelativeLayout.setVisibility(View.VISIBLE);
                final String boldMore = "...";
                final String boldLess = "";
                String normalText = post.getDescription().substring(0, 119);
                holder.descriptionTextView.setText(normalText + boldMore);
            }

        }else {
            holder.descriptionRelativeLayout.setVisibility(View.GONE);
        }

        if (post.getWidth() != null && post.getHeight() != null){
            holder.postImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent =  new Intent(mContext, PostDetailActivity.class);
                    intent.putExtra(MinePostsAdapters.EXTRA_POST_ID, postId);
                    intent.putExtra(MinePostsAdapters.COLLECTION_ID, collectionId);
                    intent.putExtra(MinePostsAdapters.EXTRA_USER_UID, uid);
                    intent.putExtra(MinePostsAdapters.TYPE, type);
                    intent.putExtra(MinePostsAdapters.POST_HEIGHT, post.getHeight());
                    intent.putExtra(MinePostsAdapters.POST_WIDTH, post.getWidth());
                    mContext.startActivity(intent);
                }
            });
        }else {
            holder.postImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent =  new Intent(mContext, PostDetailActivity.class);
                    intent.putExtra(MinePostsAdapters.EXTRA_POST_ID, postId);
                    intent.putExtra(MinePostsAdapters.COLLECTION_ID, collectionId);
                    intent.putExtra(MinePostsAdapters.EXTRA_USER_UID, uid);
                    intent.putExtra(MinePostsAdapters.TYPE, type);
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



//        //calculate the generated points from the compiled time
//        impressionReference.child("compiled_views").child(postId)
//                .addValueEventListener(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                        if (dataSnapshot.exists()){
//                            holder.mCreditsLinearLayout.setVisibility(View.VISIBLE);
//                            ViewDuration impression = dataSnapshot.getValue(ViewDuration.class);
//                            final long compiledDuration = impression.getCompiled_duration();
//                            Log.d("compiled duration", compiledDuration + "");
//                            //get seconds in milliseconds
//                            final long durationInSeconds = compiledDuration / 1000;
//                            //get the points generate
//                            final double points = durationInSeconds * 0.000015;
//                            DecimalFormat formatter = new DecimalFormat("0.000000");
//                            final String pts = formatter.format(points);
//                            holder.senseCreditsTextView.setText(pts + " points");
//
//                        }else {
//                            holder.mCreditsLinearLayout.setVisibility(View.VISIBLE);
//                            final double points = 0.00;
//                            DecimalFormat formatter = new DecimalFormat("0.00");
//                            final String pts = formatter.format(points);
//                            holder.senseCreditsTextView.setText(pts + " points");
//                        }
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                    }
//                });

//        impressionReference.child("post_views").child(postId)
//                .addValueEventListener(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                        if (dataSnapshot.exists()){
//                            final long size = dataSnapshot.getChildrenCount();
//                            int childrenCount = (int) size;
//                            holder.viewsCountTextView.setText(childrenCount + "");
//                        }else {
//                            holder.viewsCountTextView.setText("0");
//                        }
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                    }
//                });


        //get the number of commments in a single
        commentsReference.document("post_ids").collection(postId)
                .orderBy("comment_id").whereEqualTo("post_id", postId)
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

    @Override
    public int getItemCount() {
        return documentSnapshots.size();
    }

}

package com.andeka.andeka.home;

import android.content.Context;
import android.content.Intent;
import android.support.constraint.ConstraintSet;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.andeka.andeka.Constants;
import com.andeka.andeka.R;
import com.andeka.andeka.collections.CollectionPostsActivity;
import com.andeka.andeka.comments.CommentsActivity;
import com.andeka.andeka.models.Andeqan;
import com.andeka.andeka.models.Collection;
import com.andeka.andeka.models.CollectionPost;
import com.andeka.andeka.models.Post;
import com.andeka.andeka.models.Timeline;
import com.andeka.andeka.player.Player;
import com.andeka.andeka.post_detail.PostDetailActivity;
import com.andeka.andeka.post_detail.VideoDetailActivity;
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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

public class PostsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG =  PostsAdapter.class.getSimpleName();
    private static final String EXTRA_POST_ID = "post id";
    private static final String COLLECTION_ID = "collection id";
    private static final String VIDEO = "video";
    private static final String EXTRA_USER_UID =  "uid";
    private static final String TYPE = "type";
    private static final String POST_HEIGHT = "height";
    private static final String POST_WIDTH = "width";
    private static final int VIDEO_POST = 0;
    private static final int IMAGE_POST = 1;
    private static final int LIMIT = 10;
    //firestore reference
    private CollectionReference collectionsPosts;
    private CollectionReference usersReference;
    private CollectionReference commentsReference;
    private DatabaseReference impressionReference;
    private CollectionReference collectionsPostReference;
    private CollectionReference mLikesCollectionsReference;
    private CollectionReference timelineCollection;
    private DatabaseReference databaseReference;
    //firebase auth
    private FirebaseAuth firebaseAuth;
    public boolean showOnClick = true;
    private boolean processLikes = false;
    private ConstraintSet constraintSet;
    private Player player;
    private List<DocumentSnapshot> documentSnapshots;

    private BottomReachedListener mBottomReachedListener;
    private Context mContext;


    public PostsAdapter(Context ctx, List<DocumentSnapshot> documents) {
        this.mContext = ctx;
        this.documentSnapshots = new ArrayList<>();
        this.documentSnapshots = documents;
        initReferences();

    }


    public DocumentSnapshot getSnapshot(int index) {
        return documentSnapshots.get(index);
    }

    private void initReferences(){
        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null){
            usersReference = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);
            collectionsPostReference = FirebaseFirestore.getInstance().collection(Constants.COLLECTIONS);
            mLikesCollectionsReference = FirebaseFirestore.getInstance().collection(Constants.LIKES);
            timelineCollection = FirebaseFirestore.getInstance().collection(Constants.TIMELINE);
            databaseReference = FirebaseDatabase.getInstance().getReference(Constants.RANDOM_PUSH_ID);
            //firebase
            //document reference
            commentsReference  = FirebaseFirestore.getInstance().collection(Constants.COMMENTS);
            //firebase database references
            impressionReference = FirebaseDatabase.getInstance().getReference(Constants.VIEWS);
            impressionReference.keepSynced(true);
        }

    }

    @Override
    public int getItemCount() {
        return documentSnapshots.size();
    }



    public void setBottomReachedListener(BottomReachedListener bottomReachedListener){
        this.mBottomReachedListener = bottomReachedListener;
    }


    @Override
    public int getItemViewType(int position) {
        Post post = documentSnapshots.get(position).toObject(Post.class);
        final String type = post.getType();
        if (type.equals("video")){
            return VIDEO_POST;
        }else {
            return IMAGE_POST;
        }
    };

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;

        if (viewType == VIDEO_POST){
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.layout_explore_posts, parent, false);
            return new PhotoPostViewHolder(view);
        }else if (viewType == IMAGE_POST){
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.layout_explore_posts, parent, false);
            return new PhotoPostViewHolder(view);
        }
        return null;
    }




    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()){
            case 0:
                populateVideo((PhotoPostViewHolder)holder, position);
                break;
            case 1:
                populateConstrainedImage((PhotoPostViewHolder)holder, position);
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



    @Override
    public long getItemId(int position) {
        Post post = documentSnapshots.get(position).toObject(Post.class);
        return post.getNumber();
    }


    @Override
    public void setHasStableIds(boolean hasStableIds) {
        super.setHasStableIds(hasStableIds);
    }

    private void populateVideo(final PhotoPostViewHolder holder, final int position){
        final Post post = documentSnapshots.get(position).toObject(Post.class);
        final String postId = post.getPost_id();
        final String uid = post.getUser_id();
        final String collectionId = post.getCollection_id();
        final String type = post.getType();

        Glide.with(mContext.getApplicationContext())
                .load(post.getUrl())
                .apply(new RequestOptions()
                        .placeholder(R.drawable.post_placeholder))
                .into(holder.postImageView);

        if (!TextUtils.isEmpty(post.getTitle())){
            holder.titleTextView.setText(post.getTitle());
            holder.titleRelativeLayout.setVisibility(View.VISIBLE);
        }else {
            holder.titleTextView.setText("");
            holder.titleRelativeLayout.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(post.getDescription())){
            //prevent collection note from overlapping other layouts
            final String [] strings = post.getDescription().split("");
            final int size = strings.length;
            if (size <= 50){
                holder.descriptionRelativeLayout.setVisibility(View.VISIBLE);
                holder.descriptionTextView.setText(post.getDescription());
            }else{
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
            holder.descriptionTextView.setText("");
            holder.descriptionRelativeLayout.setVisibility(View.GONE);
        }

        holder.profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, ProfileActivity.class);
                intent.putExtra(PostsAdapter.EXTRA_USER_UID, uid);
                mContext.startActivity(intent);
            }
        });

        holder.postConstraintLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent =  new Intent(mContext, VideoDetailActivity.class);
                intent.putExtra(PostsAdapter.EXTRA_POST_ID, postId);
                intent.putExtra(PostsAdapter.COLLECTION_ID, collectionId);
                intent.putExtra(PostsAdapter.EXTRA_USER_UID, uid);
                intent.putExtra(PostsAdapter.TYPE, type);
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

    private void populateConstrainedImage(final PhotoPostViewHolder holder, final int position){
        final Post post = documentSnapshots.get(position).toObject(Post.class);
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


        if (post.getUrl() == null){
            //firebase firestore references
            if (type.equals("single")|| type.equals("single_image_post")){
                collectionsPosts = FirebaseFirestore.getInstance().collection(Constants.POSTS_OF_COLLECTION)
                        .document("singles").collection(collectionId);
            }else{
                collectionsPosts = FirebaseFirestore.getInstance().collection(Constants.POSTS_OF_COLLECTION)
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


        if (post.getWidth() != null && post.getHeight() != null){
            holder.postImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent =  new Intent(mContext, PostDetailActivity.class);
                    intent.putExtra(PostsAdapter.EXTRA_POST_ID, postId);
                    intent.putExtra(PostsAdapter.COLLECTION_ID, collectionId);
                    intent.putExtra(PostsAdapter.EXTRA_USER_UID, uid);
                    intent.putExtra(PostsAdapter.TYPE, type);
                    intent.putExtra(PostsAdapter.POST_HEIGHT, post.getHeight());
                    intent.putExtra(PostsAdapter.POST_WIDTH, post.getWidth());
                    mContext.startActivity(intent);
                }
            });
        }else {
            holder.postImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent =  new Intent(mContext, PostDetailActivity.class);
                    intent.putExtra(PostsAdapter.EXTRA_POST_ID, postId);
                    intent.putExtra(PostsAdapter.COLLECTION_ID, collectionId);
                    intent.putExtra(PostsAdapter.EXTRA_USER_UID, uid);
                    intent.putExtra(PostsAdapter.TYPE, type);
                    mContext.startActivity(intent);
                }
            });

        }

        holder.profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, ProfileActivity.class);
                intent.putExtra(PostsAdapter.EXTRA_USER_UID, uid);
                mContext.startActivity(intent);
            }
        });


        holder.commentsImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, CommentsActivity.class);
                intent.putExtra(PostsAdapter.EXTRA_POST_ID, postId);
                mContext.startActivity(intent);
            }
        });


//        //calculate the generated points from the compiled time
//        impressionReference.child("compiled_views").child(postId)
//                .addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                if (dataSnapshot.exists()){
//                    holder.mCreditsLinearLayout.setVisibility(View.VISIBLE);
//                    ViewDuration impression = dataSnapshot.getValue(ViewDuration.class);
//                    final long compiledDuration = impression.getCompiled_duration();
//                    Log.d("compiled duration", compiledDuration + "");
//                    //get seconds in milliseconds
//                    final long durationInSeconds = compiledDuration / 1000;
//                    //get the points generate
//                    final double points = durationInSeconds * 0.000001;
//                    DecimalFormat formatter = new DecimalFormat("0.0000");
//                    final String pts = formatter.format(points);
//                    holder.senseCreditsTextView.setText(pts + " points");
//
//                }else {
//                    holder.mCreditsLinearLayout.setVisibility(View.VISIBLE);
//                    final double points = 0.00;
//                    DecimalFormat formatter = new DecimalFormat("0.00");
//                    final String pts = formatter.format(points);
//                    holder.senseCreditsTextView.setText(pts + " points");
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });

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


        collectionsPostReference.document(collectionId)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen error", e);
                            return;
                        }

                        if (documentSnapshot.exists()){
                            Collection collection = documentSnapshot.toObject(Collection.class);
                            final String name = collection.getName();
                            final String creatorUid = collection.getUser_id();
                            holder.collectionNameTextView.setText("@" + name);
                            holder.collectionNameTextView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent(mContext, CollectionPostsActivity.class);
                                    intent.putExtra(PostsAdapter.COLLECTION_ID, collectionId);
                                    intent.putExtra(PostsAdapter.EXTRA_USER_UID, creatorUid);
                                    mContext.startActivity(intent);
                                }
                            });
                        }else {
                            holder.collectionNameTextView.setText("");
                        }
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

        mLikesCollectionsReference.document("post_ids").collection(postId)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot documentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen error", e);
                            return;
                        }

                        if (!documentSnapshots.isEmpty()){
                            final int count = documentSnapshots.size();
                            holder.mLikesTextView.setText(count + "");
                        }else {
                            holder.mLikesTextView.setText("0");
                        }
                    }
                });

        mLikesCollectionsReference.document("post_ids").collection(postId)
                .whereEqualTo("user_id", firebaseAuth.getCurrentUser().getUid())
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot documentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen error", e);
                            return;
                        }

                        if (!documentSnapshots.isEmpty()){
                            holder.mLikeImageView.setBackgroundResource(R.drawable.ic_heart_fill);
                        }else {
                            holder.mLikeImageView.setBackgroundResource(R.drawable.ic_heart_grey);

                        }
                    }
                });

        // like a post
        holder.mLikesLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                processLikes = true;
                mLikesCollectionsReference.document("post_ids")
                        .collection(postId).whereEqualTo("user_id",
                        firebaseAuth.getCurrentUser().getUid())
                        .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot documentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen error", e);
                            return;
                        }

                        if (processLikes) {
                            if (!documentSnapshots.isEmpty()){
                                mLikesCollectionsReference.document("post_ids").collection(postId)
                                        .document(firebaseAuth.getCurrentUser().getUid()).delete();
                                holder.mLikeImageView.setBackgroundResource(R.drawable.ic_heart_grey);

                                processLikes = false;
                            }else {
                                Map<String, String> like = new HashMap<>();
                                like.put("user_id", firebaseAuth.getCurrentUser().getUid());
                                mLikesCollectionsReference.document("post_ids").collection(postId)
                                        .document(firebaseAuth.getCurrentUser().getUid())
                                        .set(like).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Timeline timeline = new Timeline();
                                        final long time = new Date().getTime();
                                        final String postid =  databaseReference.push().getKey();
                                        timeline.setPost_id(postId);
                                        timeline.setTime(time);
                                        timeline.setUser_id(firebaseAuth.getCurrentUser().getUid());
                                        timeline.setType("like");
                                        timeline.setActivity_id(postid);
                                        timeline.setStatus("un_read");

                                        timelineCollection.document(postId).collection("activities")
                                                .document(firebaseAuth.getCurrentUser().getUid())
                                                .set(timeline);
                                        holder.mLikeImageView.setBackgroundResource(R.drawable.ic_heart_fill);

                                    }
                                });
                                processLikes = false;
                            }
                        }
                    }
                });

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

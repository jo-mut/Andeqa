package com.andeqa.andeqa.more;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.constraint.ConstraintSet;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.R;
import com.andeqa.andeqa.collections.CollectionPostsActivity;
import com.andeqa.andeqa.comments.CommentsActivity;
import com.andeqa.andeqa.home.PhotoPostViewHolder;
import com.andeqa.andeqa.home.PostsAdapter;
import com.andeqa.andeqa.home.VideoPostViewHolder;
import com.andeqa.andeqa.impressions.ImpressionTracker;
import com.andeqa.andeqa.models.Andeqan;
import com.andeqa.andeqa.models.Collection;
import com.andeqa.andeqa.models.CollectionPost;
import com.andeqa.andeqa.models.Post;
import com.andeqa.andeqa.player.Player;
import com.andeqa.andeqa.post_detail.PostDetailActivity;
import com.andeqa.andeqa.post_detail.VideoDetailActivity;
import com.andeqa.andeqa.profile.ProfileActivity;
import com.andeqa.andeqa.utils.BottomReachedListener;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
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
import java.util.List;
import java.util.WeakHashMap;

import javax.annotation.Nullable;

public class MorePostsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
        implements ImpressionTracker.VisibilityTrackerListener{
    private static final String TAG =  PostsAdapter.class.getSimpleName();
    private Context mContext;
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
    //firebase auth
    private FirebaseAuth firebaseAuth;
    public boolean showOnClick = true;
    private ConstraintSet constraintSet;
    private Player player;
    private List<DocumentSnapshot> documentSnapshots = new ArrayList<>();
    //impression tracking
    private ImpressionTracker impressionTracker;
    private final WeakHashMap<View, Integer> mViewPositionMap = new WeakHashMap<>();
    //bottom reached listener
    BottomReachedListener BottomReachedListener;


    public MorePostsAdapter(Activity activity) {
        this.mContext = activity;
        impressionTracker = new ImpressionTracker(activity);
        initReferences();
    }

    public void setExplorePosts(List<DocumentSnapshot> posts) {
        this.documentSnapshots = posts;
    }

    public void setBottomReachedListener(BottomReachedListener bottomReachedListener){
        this.BottomReachedListener = bottomReachedListener;
    }

    public void clear(){
        documentSnapshots.clear();
        notifyDataSetChanged();
    }

    public DocumentSnapshot getSnapshot(int index) {
        return documentSnapshots.get(index);
    }

    @Override
    public void onVisibilityChanged(List<View> visibleViews, List<View> invisibleViews) {


    }

    private void initReferences(){
        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null){
            usersReference = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);
            collectionsPostReference = FirebaseFirestore.getInstance().collection(Constants.COLLECTIONS);
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


    @Override
    public int getItemViewType(int position) {
        Post post = documentSnapshots.get(position).toObject(Post.class);
        final String type = post.getType();
        if (type.equals("single_video_post") || type.equals("collection_video_post")){
            return VIDEO_POST;
        }else {
            return IMAGE_POST;
        }
    };

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case VIDEO_POST:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.layout_video_post, parent, false);
                return new VideoPostViewHolder(view);
            case IMAGE_POST:
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
//                populateVideo((VideoPostViewHolder)holder, position);
                break;
            case 1:
                populateConstrainedImage((PhotoPostViewHolder)holder, position);
                break;
        }

        try {
            if (position == documentSnapshots.size() - 1){
                BottomReachedListener.onBottomReached(position);
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

    private void populateVideo(final VideoPostViewHolder holder, final int position){
        final Post post = documentSnapshots.get(position).toObject(Post.class);
        final String postId = post.getPost_id();
        final String uid = post.getUser_id();
        final String collectionId = post.getCollection_id();
        final String type = post.getType();

        //firestore references
        player = new Player(mContext.getApplicationContext(), holder.postVideoView);
        player.addMedia(post.getUrl());
        holder.playImageView.setVisibility(View.VISIBLE);
        holder.playImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.postVideoView.getPlayer() == null){
                    holder.postVideoView.getPlayer().setPlayWhenReady(true);
                    holder.puaseImageView.setVisibility(View.VISIBLE);
                    holder.playImageView.setVisibility(View.GONE);
                }else {
                    player.releasePlayer();
                    holder.postVideoView.getPlayer().setPlayWhenReady(true);
                    holder.puaseImageView.setVisibility(View.VISIBLE);
                    holder.playImageView.setVisibility(View.GONE);
                }
            }
        });

        holder.puaseImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.postVideoView.getPlayer().setPlayWhenReady(false);
                holder.puaseImageView.setVisibility(View.GONE);
                holder.playImageView.setVisibility(View.VISIBLE);

            }
        });

        if (!TextUtils.isEmpty(post.getTitle())){
            holder.bottomLinearLayout.setVisibility(View.VISIBLE);
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


        holder.postRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent =  new Intent(mContext, VideoDetailActivity.class);
                intent.putExtra(MorePostsAdapter.EXTRA_POST_ID, postId);
                intent.putExtra(MorePostsAdapter.COLLECTION_ID, collectionId);
                intent.putExtra(MorePostsAdapter.EXTRA_USER_UID, uid);
                intent.putExtra(MorePostsAdapter.TYPE, type);
                mContext.startActivity(intent);
            }
        });

        holder.profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, ProfileActivity.class);
                intent.putExtra(MorePostsAdapter.EXTRA_USER_UID, uid);
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

        //calculate view visibility and add visible views to impression tracker
        mViewPositionMap.put(holder.itemView, position);
        impressionTracker.addView(holder.itemView, 100, postId, "post", post.getUser_id());

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


        if (post.getWidth() != null && post.getHeight() != null){
            holder.postImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent =  new Intent(mContext, PostDetailActivity.class);
                    intent.putExtra(MorePostsAdapter.EXTRA_POST_ID, postId);
                    intent.putExtra(MorePostsAdapter.COLLECTION_ID, collectionId);
                    intent.putExtra(MorePostsAdapter.EXTRA_USER_UID, uid);
                    intent.putExtra(MorePostsAdapter.TYPE, type);
                    intent.putExtra(MorePostsAdapter.POST_HEIGHT, post.getHeight());
                    intent.putExtra(MorePostsAdapter.POST_WIDTH, post.getWidth());
                    mContext.startActivity(intent);
                }
            });
        }else {
            holder.postImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent =  new Intent(mContext, PostDetailActivity.class);
                    intent.putExtra(MorePostsAdapter.EXTRA_POST_ID, postId);
                    intent.putExtra(MorePostsAdapter.COLLECTION_ID, collectionId);
                    intent.putExtra(MorePostsAdapter.EXTRA_USER_UID, uid);
                    intent.putExtra(MorePostsAdapter.TYPE, type);
                    mContext.startActivity(intent);
                }
            });

        }

        holder.profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, ProfileActivity.class);
                intent.putExtra(MorePostsAdapter.EXTRA_USER_UID, uid);
                mContext.startActivity(intent);
            }
        });

        holder.commentsImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, CommentsActivity.class);
                intent.putExtra(MorePostsAdapter.EXTRA_POST_ID, postId);
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
                                    intent.putExtra(MorePostsAdapter.COLLECTION_ID, collectionId);
                                    intent.putExtra(MorePostsAdapter.EXTRA_USER_UID, creatorUid);
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


    }

    //region listeners
    private static double roundCredits(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}

package com.andeqa.andeqa.profile;

import android.content.Context;
import android.content.Intent;
import android.support.constraint.ConstraintSet;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.R;
import com.andeqa.andeqa.home.PostDetailActivity;
import com.andeqa.andeqa.home.VideoPostViewHolder;
import com.andeqa.andeqa.impressions.ImpressionTracker;
import com.andeqa.andeqa.models.CollectionPost;
import com.andeqa.andeqa.models.Post;
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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
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
                return new ProfilePostAdapter.ProfilePostViewHolder(view);
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
            populateImage((ProfilePostAdapter.ProfilePostViewHolder)holder, position);

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
        }

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


    }

    private void populateImage(final ProfilePostAdapter.ProfilePostViewHolder holder, final int position){
        final Post post = getSnapshot(holder.getAdapterPosition()).toObject(Post.class);
        final String postId = post.getPost_id();
        final String uid = post.getUser_id();
        final String collectionId = post.getCollection_id();
        final String type = post.getType();


        if (type.equals("single") || type.equals("single_image_post")){
            collectionsPosts = FirebaseFirestore.getInstance().collection(Constants.COLLECTIONS_OF_POSTS)
                    .document("singles").collection(collectionId);
        }else {
            collectionsPosts = FirebaseFirestore.getInstance().collection(Constants.COLLECTIONS_OF_POSTS)
                    .document("collections").collection(collectionId);
        }

        if (post.getUrl() == null){
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

        }else {
            Glide.with(mContext.getApplicationContext())
                    .load(post.getUrl())
                    .apply(new RequestOptions()
                            .placeholder(R.drawable.post_placeholder)
                            .diskCacheStrategy(DiskCacheStrategy.DATA))
                    .into(holder.postImageView);

        }



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

    }


    //region listeners
    private static double roundCredits(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public static class ProfilePostViewHolder extends RecyclerView.ViewHolder{
        View mView;
        Context mContext;
        public ImageView postImageView;


        public ProfilePostViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            mContext = itemView.getContext();
            postImageView = (ImageView) mView.findViewById(R.id.postImageView);

        }
    }


}

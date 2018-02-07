package com.cinggl.cinggl.home;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cinggl.cinggl.Constants;
import com.cinggl.cinggl.R;
import com.cinggl.cinggl.firestore.FirestoreAdapter;
import com.cinggl.cinggl.models.Post;
import com.cinggl.cinggl.models.Timeline;
import com.cinggl.cinggl.comments.CommentsActivity;
import com.cinggl.cinggl.likes.LikesActivity;
import com.cinggl.cinggl.models.Balance;
import com.cinggl.cinggl.models.Cinggulan;
import com.cinggl.cinggl.models.Credit;
import com.cinggl.cinggl.models.Like;
import com.cinggl.cinggl.models.TransactionDetails;
import com.cinggl.cinggl.people.FollowerProfileActivity;
import com.cinggl.cinggl.profile.PersonalProfileActivity;
import com.cinggl.cinggl.likes.WhoLikedViewHolder;
import com.cinggl.cinggl.settings.FragmentPostSettings;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.firebase.ui.firestore.ObservableSnapshotArray;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Date;

/**
 * Created by J.EL on 11/17/2017.
 */

public class MainPostsAdapter extends FirestoreAdapter<MainPostsViewHolder> {
    private static final String TAG =  MainPostsAdapter.class.getSimpleName();
    private Context mContext;
    private static final String EXTRA_POST_KEY = "post key";
    private static final String EXTRA_USER_UID =  "uid";
    private boolean processLikes = false;
    private boolean processDislikes = false;
    private boolean processCredits = false;
    private static final double DEFAULT_PRICE = 1.5;
    private static final double GOLDEN_RATIO = 1.618;
    private static final int MAX_WIDTH = 200;
    private static final int MAX_HEIGHT = 200;
    private static final int LIMIT = 10;
    //firestore reference
    private FirebaseFirestore firebaseFirestore;
    private CollectionReference cinglesReference;
    private CollectionReference ifairReference;
    private com.google.firebase.firestore.Query commentsCountQuery;
    private CollectionReference ownerReference;
    private CollectionReference usersReference;
    private CollectionReference commentsReference;
    private CollectionReference relationsReference;
    private CollectionReference likesReference;
    private CollectionReference senseCreditReference;
    private CollectionReference postWalletReference;
    private CollectionReference timelineCollection;
    private Query likesQuery;
    //firebase
    private DatabaseReference databaseReference;
    //firebase auth
    private FirebaseAuth firebaseAuth;
    //adapters
    private FirestoreRecyclerAdapter firestoreRecyclerAdapter;


    public MainPostsAdapter(Query query, Context mContext) {
        super(query);
        this.mContext = mContext;
    }

    @Override
    public int getItemCount() {
        return super.getItemCount();

    }

    @Override
    public MainPostsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new MainPostsViewHolder(inflater.inflate(R.layout.post_layout, parent, false));
    }


    @Override
    public void onBindViewHolder(final MainPostsViewHolder holder, int position) {
        final Post post = getSnapshot(position).toObject(Post.class);
        holder.bindRandomCingles(getSnapshot(position));
        final String postKey = post.getPushId();
        final String uid = post.getUid();
        Log.d("post postkey", postKey);


        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser()!= null){
            //firestore
            cinglesReference = FirebaseFirestore.getInstance().collection(Constants.POSTS);
            ownerReference = FirebaseFirestore.getInstance().collection(Constants.CINGLE_ONWERS);
            usersReference = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);
            ifairReference = FirebaseFirestore.getInstance().collection(Constants.SELLING);
            commentsReference = FirebaseFirestore.getInstance().collection(Constants.COMMENTS);
            senseCreditReference = FirebaseFirestore.getInstance().collection(Constants.SENSECREDITS);
            relationsReference = FirebaseFirestore.getInstance().collection(Constants.RELATIONS);
            timelineCollection = FirebaseFirestore.getInstance().collection(Constants.TIMELINE);
            //firebase
            databaseReference = FirebaseDatabase.getInstance().getReference(Constants.RANDOM_PUSH_ID);
            //document reference
            commentsCountQuery= commentsReference;
            likesReference = FirebaseFirestore.getInstance().collection(Constants.LIKES);
            postWalletReference = FirebaseFirestore.getInstance().collection(Constants.CINGLE_WALLET);

        }

        holder.totalLikesCountTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, LikesActivity.class);
                intent.putExtra(MainPostsAdapter.EXTRA_POST_KEY, postKey);
                mContext.startActivity(intent);
            }
        });

        holder.commentsImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent =  new Intent(mContext, CommentsActivity.class);
                intent.putExtra(MainPostsAdapter.EXTRA_POST_KEY, postKey);
                mContext.startActivity(intent);
            }
        });

        holder.postImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, FullImageViewActivity.class);
                intent.putExtra(MainPostsAdapter.EXTRA_POST_KEY, postKey);
                mContext.startActivity(intent);
            }
        });

        holder.tradeMethodTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent =  new Intent(mContext, PostDetailActivity.class);
                intent.putExtra(MainPostsAdapter.EXTRA_POST_KEY, postKey);
                mContext.startActivity(intent);
            }
        });

        holder.settingsImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putString(MainPostsAdapter.EXTRA_POST_KEY, postKey);
                FragmentManager fragmenManager = ((AppCompatActivity)mContext).getSupportFragmentManager();
                FragmentPostSettings fragmentPostSettings = FragmentPostSettings.newInstance("post settngs");
                fragmentPostSettings.setArguments(bundle);
                fragmentPostSettings.show(fragmenManager, "post settings fragment");
            }
        });

        holder.profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ((firebaseAuth.getCurrentUser().getUid()).equals(uid)){
                    Intent intent = new Intent(mContext, PersonalProfileActivity.class);
                    intent.putExtra(MainPostsAdapter.EXTRA_USER_UID, uid);
                    mContext.startActivity(intent);
                    Log.d("profile uid", firebaseAuth.getCurrentUser().getUid());
                }else {
                    Intent intent = new Intent(mContext, FollowerProfileActivity.class);
                    intent.putExtra(MainPostsAdapter.EXTRA_USER_UID, uid);
                    Log.d("follower uid", uid);
                    mContext.startActivity(intent);
                }
            }
        });

        senseCreditReference.document(postKey).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (documentSnapshot.exists()){
                    Credit credit = documentSnapshot.toObject(Credit.class);
                    final double senseCredits = credit.getAmount();
                    DecimalFormat formatter = new DecimalFormat("0.00000000");
                    holder.senseCreditsTextView.setText("SC" + " " + formatter.format(senseCredits));

                }else {
                    holder.senseCreditsTextView.setText("SC 0.00000000");
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
                    final Cinggulan cinggulan = documentSnapshot.toObject(Cinggulan.class);
                    holder.accountUsernameTextView.setText(cinggulan.getUsername());

                    Picasso.with(mContext)
                            .load(cinggulan.getProfileImage())
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
                                            .load(cinggulan.getProfileImage())
                                            .resize(MAX_WIDTH, MAX_HEIGHT)
                                            .onlyScaleDown()
                                            .centerCrop()
                                            .placeholder(R.drawable.profle_image_background)
                                            .into(holder.profileImageView);
                                }
                            });
                }
            }
        });

        ownerReference.document(postKey).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (documentSnapshot.exists()){
                    TransactionDetails transactionDetails = documentSnapshot.toObject(TransactionDetails.class);
                    final String ownerUid = transactionDetails.getUid();
                    Log.d("owner uid", ownerUid);

                    if (firebaseAuth.getCurrentUser().getUid().equals(ownerUid)){
                        holder.settingsImageView.setVisibility(View.VISIBLE);
                    }else {
                        holder.settingsImageView.setVisibility(View.INVISIBLE);
                    }
                }
            }
        });

        //get the number of commments in a post
        commentsCountQuery.orderBy("postId").whereEqualTo("pushId", postKey)
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


        //check if post is listed on the marketplace
        ifairReference.document(postKey).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (documentSnapshot.exists()){
                    holder.tradeMethodTextView.setText("@Selling");
                }else {
                    holder.tradeMethodTextView.setText("@NotListed");

                }

            }
        });

        //color the like image view if the user has liked
        likesReference.document(postKey).collection("likes")
                .whereEqualTo("uid", firebaseAuth.getCurrentUser().getUid())
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                        if (e != null) {
                            Log.w(TAG, "Listen error", e);
                            return;
                        }

                        if (!documentSnapshots.isEmpty()){
                            holder.likesImageView.setColorFilter(Color.RED);
                        }else {
                            holder.likesImageView.setColorFilter(Color.BLACK);
                        }

                    }
                });

        //get the count of likes after the top 5
        likesReference.document(postKey).collection("likes").orderBy("uid").startAt(6)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                        if (e != null) {
                            Log.w(TAG, "Listen error", e);
                            return;
                        }

                        if (!documentSnapshots.isEmpty()){
                            final int otherLikes = documentSnapshots.size();
                            holder.totalLikesCountTextView.setText(otherLikes + " " +
                                    "Likes");
                        }else {
                            holder.totalLikesCountTextView.setVisibility(View.GONE);
                        }
                    }
                });

//        color the like image view if the user has dislikes
        likesReference.document(postKey).collection("dislikes")
                .whereEqualTo("uid", firebaseAuth.getCurrentUser().getUid())
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                        if (e != null) {
                            Log.w(TAG, "Listen error", e);
                            return;
                        }

                        if (!documentSnapshots.isEmpty()){
                            holder.dislikeImageView.setColorFilter(Color.RED);
                        }else {
                            holder.dislikeImageView.setColorFilter(Color.BLACK);
                        }

                    }
                });


        //calculate the percentage of likes to dislikes
        likesReference.document(postKey).collection("dislikes")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot dislikesSnapshots, FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen error", e);
                            return;
                        }

                        if (!dislikesSnapshots.isEmpty()){
                            final int dislikes = dislikesSnapshots.size();
                            Log.d("dislikes count", dislikes + "");
                            likesReference.document(postKey).collection("likes")
                                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                        @Override
                                        public void onEvent(QuerySnapshot likesSnapshots, FirebaseFirestoreException e) {
                                            if (e != null) {
                                                Log.w(TAG, "Listen error", e);
                                                return;
                                            }

                                            if (!likesSnapshots.isEmpty()){
                                                //calculate likes in percentage
                                                final int likes = likesSnapshots.size();
                                                Log.d("likes size", likes + "");
                                                final int likesPlusDislikes = likes + dislikes;
                                                Log.d("likes plus dislikes", likesPlusDislikes + "");
                                                final int percentLikes = 100 * likes/likesPlusDislikes;
                                                Log.d("likes percentage", percentLikes + "");
                                                final int roundedPercent = roundPercentage(percentLikes, 2);
                                                holder.likesCountTextView.setText(roundedPercent + "%" + " " + "Likes");
                                            }else {
//                                        //calculate likes in percentage
                                                holder.likesCountTextView.setText("0%" + " " + "Likes");
                                            }
                                        }
                                    });
                        }else {
                            final int dislikes = dislikesSnapshots.size();
                            Log.d("dislikes count", dislikes + "");
                            likesReference.document(postKey).collection("likes")
                                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                        @Override
                                        public void onEvent(QuerySnapshot likesSnapshots, FirebaseFirestoreException e) {
                                            if (e != null) {
                                                Log.w(TAG, "Listen error", e);
                                                return;
                                            }

                                            if (!likesSnapshots.isEmpty()){
                                                //calculate likes in percentage
                                                final int likes = likesSnapshots.size();
                                                Log.d("likes size", likes + "");
                                                final int likesPlusDislikes = likes + dislikes;
                                                Log.d("likes plus dislikes", likesPlusDislikes + "");
                                                final int percentLikes = 100 * likes/likesPlusDislikes;
                                                Log.d("likes percentage", percentLikes + "");
                                                final int roundedPercent = roundPercentage(percentLikes, 2);
                                                holder.likesCountTextView.setText(roundedPercent + "%" + " " + "Likes");
                                            }else {
                                                holder.likesCountTextView.setText("0%" + " " + "Likes");
                                            }
                                        }
                                    });
                        }


                    }
                });

        //calculate the percentage of likes to dislikes
        likesReference.document(postKey).collection("likes")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot likesSnapshots, FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen error", e);
                            return;
                        }

                        if (!likesSnapshots.isEmpty()){
                            final int likes = likesSnapshots.size();
                            Log.d("likes count size", likes + "");
                            likesReference.document(postKey).collection("dislikes")
                                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                        @Override
                                        public void onEvent(QuerySnapshot dislikesSnapshots, FirebaseFirestoreException e) {
                                            if (e != null) {
                                                Log.w(TAG, "Listen error", e);
                                                return;
                                            }

                                            if (!dislikesSnapshots.isEmpty()){
                                                //calculate likes in percentage
                                                final int dislikes = dislikesSnapshots.size();
                                                Log.d("dislikes size", dislikes + "");
                                                final int likesPlusDislikes = likes + dislikes;
                                                Log.d("disikes plus dislikes", likesPlusDislikes + "");
                                                final int percentDislikes = 100 * dislikes/likesPlusDislikes;
                                                Log.d("dislikes percentage", percentDislikes + "");
                                                final int roundedPercent = roundPercentage(percentDislikes, 2);
                                                holder.dislikeCountTextView.setText(roundedPercent + "%" + " " + "  Dislikes");
                                            }else {
                                                //calculate likes in percentage
                                                final int dislikes = dislikesSnapshots.size();
                                                Log.d("dislikes size", dislikes + "");
                                                final int likesPlusDislikes = likes + dislikes;
                                                Log.d("disikes plus dislikes", likesPlusDislikes + "");
                                                final int percentDislikes = 100 * dislikes/likesPlusDislikes;
                                                Log.d("dislikes percentage", percentDislikes + "");
                                                final int roundedPercent = roundPercentage(percentDislikes, 2);
                                                holder.dislikeCountTextView.setText(roundedPercent + "%" + " " + "Dislikes");                                            }

                                        }
                                    });
                        }else {
                            final int likes = likesSnapshots.size();
                            Log.d("likes count size", likes + "");
                            likesReference.document(postKey).collection("dislikes")
                                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                        @Override
                                        public void onEvent(QuerySnapshot dislikesSnapshots, FirebaseFirestoreException e) {
                                            if (e != null) {
                                                Log.w(TAG, "Listen error", e);
                                                return;
                                            }

                                            if (!dislikesSnapshots.isEmpty()){
                                                //calculate likes in percentage
                                                final int dislikes = dislikesSnapshots.size();
                                                Log.d("dislikes size", dislikes + "");
                                                final int likesPlusDislikes = likes + dislikes;
                                                Log.d("disikes plus dislikes", likesPlusDislikes + "");
                                                final int percentDislikes = 100 * dislikes/likesPlusDislikes;
                                                Log.d("dislikes percentage", percentDislikes + "");
                                                final int roundedPercent = roundPercentage(percentDislikes, 2);
                                                holder.dislikeCountTextView.setText(roundedPercent + "%" + " " + "Dislikes");
                                            }else {
                                                holder.dislikeCountTextView.setText("0%" + " " + "Dislikes");                                            }

                                        }
                                    });
                        }


                    }
                });


        likesReference.document(postKey).collection("likes")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(final QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                        if (e != null) {
                            Log.w(TAG, "Listen error", e);
                            return;
                        }

                        if (!documentSnapshots.isEmpty()){
                            if (documentSnapshots.size() > 0){
                                holder.likesRecyclerView.setVisibility(View.VISIBLE);
                                likesQuery = likesReference.document(postKey).collection("likes").orderBy("uid").limit(5);
                                FirestoreRecyclerOptions<Like> options = new FirestoreRecyclerOptions.Builder<Like>()
                                        .setQuery(likesQuery, Like.class)
                                        .build();

                                firestoreRecyclerAdapter = new FirestoreRecyclerAdapter<Like, WhoLikedViewHolder>(options) {

                                    @Override
                                    protected void onBindViewHolder(final WhoLikedViewHolder viewHolder, int position, Like model) {
                                        viewHolder.bindWhoLiked(getSnapshots().getSnapshot(position));
                                        Like like = getSnapshots().getSnapshot(position).toObject(Like.class);
                                        final String uid = like.getUid();

                                        viewHolder.whoLikedImageView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                Intent intent = new Intent(mContext, LikesActivity.class);
                                                intent.putExtra(MainPostsAdapter.EXTRA_POST_KEY, postKey);
                                                mContext.startActivity(intent);
                                            }
                                        });

                                        //get the profile of the user who just liked
                                        usersReference.document(uid).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                            @Override
                                            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                                                if (e != null) {
                                                    Log.w(TAG, "Listen error", e);
                                                    return;
                                                }

                                                if (documentSnapshot.exists()){
                                                    final Cinggulan cinggulan = documentSnapshot.toObject(Cinggulan.class);
                                                    final String profileImage = cinggulan.getProfileImage();

                                                    Picasso.with(mContext)
                                                            .load(profileImage)
                                                            .fit()
                                                            .centerCrop()
                                                            .placeholder(R.drawable.profle_image_background)
                                                            .networkPolicy(NetworkPolicy.OFFLINE)
                                                            .into(viewHolder.whoLikedImageView, new Callback() {
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
                                                                            .into(viewHolder.whoLikedImageView);


                                                                }
                                                            });

                                                }
                                            }
                                        });


                                    }

                                    @Override
                                    public ObservableSnapshotArray<Like> getSnapshots() {
                                        return super.getSnapshots();
                                    }

                                    @Override
                                    public WhoLikedViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                                        View view = LayoutInflater.from(parent.getContext()).inflate
                                                (R.layout.who_liked_count, parent, false);
                                        return new WhoLikedViewHolder(view);

                                    }

                                    @Override
                                    public int getItemCount() {
                                        return super.getItemCount();
                                    }
                                };

                                holder.likesRecyclerView.setAdapter(firestoreRecyclerAdapter);
                                firestoreRecyclerAdapter.startListening();
                                holder.likesRecyclerView.setHasFixedSize(false);
                                RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(mContext,
                                        LinearLayoutManager.HORIZONTAL, true);
                                layoutManager.setAutoMeasureEnabled(true);
                                holder.likesRecyclerView.setNestedScrollingEnabled(false);
                                holder.likesRecyclerView.setLayoutManager(layoutManager);

                            }else {
                                holder.likesRecyclerView.setVisibility(View.GONE);
                            }
                        }
                    }
                });

        holder.dislikeImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                processDislikes = true;
                likesReference.document(postKey).collection("dislikes")
                        .whereEqualTo("uid", firebaseAuth.getCurrentUser().getUid())
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
                                        like.setUid(firebaseAuth.getCurrentUser().getUid());
                                        like.setPushId(firebaseAuth.getCurrentUser().getUid());
                                        likesReference.document(postKey).collection("dislikes")
                                                .document(firebaseAuth.getCurrentUser().getUid()).set(like);
                                        processDislikes = false;
                                        holder.dislikeImageView.setColorFilter(Color.RED);

                                    }else {
                                        likesReference.document(postKey).collection("dislikes")
                                                .document(firebaseAuth.getCurrentUser().getUid()).delete();
                                        processDislikes = false;
                                        holder.dislikeImageView.setColorFilter(Color.BLACK);

                                    }
                                }

                            }
                        });
            }
        });


        holder.likesImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                processLikes = true;
                likesReference.document(postKey).collection("likes")
                        .whereEqualTo("uid", firebaseAuth.getCurrentUser().getUid())
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
                                        like.setUid(firebaseAuth.getCurrentUser().getUid());
                                        like.setPushId(firebaseAuth.getCurrentUser().getUid());
                                        likesReference.document(postKey).collection("likes")
                                                .document(firebaseAuth.getCurrentUser().getUid()).set(like)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        final Timeline timeline = new Timeline();
                                                        final long time = new Date().getTime();

                                                        timelineCollection.document(uid).collection("timeline")
                                                                .whereEqualTo("postKey", postKey)
                                                                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                                                    @Override
                                                                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                                                                        if (e != null) {
                                                                            Log.w(TAG, "Listen error", e);
                                                                            return;
                                                                        }


                                                                        if (documentSnapshots.isEmpty()){
                                                                            Log.d("timeline is empty", postKey);
                                                                            final String postId = databaseReference.push().getKey();
                                                                            timeline.setPushId(postKey);
                                                                            timeline.setTimeStamp(time);
                                                                            timeline.setUid(firebaseAuth.getCurrentUser().getUid());
                                                                            timeline.setType("like");
                                                                            timeline.setPostId(postId);
                                                                            timeline.setStatus("unRead");

                                                                            if (uid.equals(firebaseAuth.getCurrentUser().getUid())){
                                                                                //do nothing
                                                                            }else {
                                                                                timelineCollection.document(uid).collection("timeline")
                                                                                        .document(postId)
                                                                                        .set(timeline);
                                                                            }
                                                                        }
                                                                    }
                                                                });


                                                    }
                                                });
                                        processLikes = false;
                                        holder.likesImageView.setColorFilter(Color.RED);

                                    }else {
                                        likesReference.document(postKey).collection("likes")
                                                .document(firebaseAuth.getCurrentUser().getUid()).delete();
                                        processLikes = false;
                                        holder.likesImageView.setColorFilter(Color.BLACK);

                                    }
                                }

                                likesReference.document(postKey).collection("likes")
                                        .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                            @Override
                                            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                                                if (e != null) {
                                                    Log.w(TAG, "Listen error", e);
                                                    return;
                                                }

                                                if (!documentSnapshots.isEmpty()){
                                                    int likesCount = documentSnapshots.size();
                                                    if ( likesCount > 0){
                                                        //mille is a thousand likes
                                                        double MILLE = 1000.0;
                                                        //get the number of likes per a thousand likes
                                                        double likesPerMille = likesCount/MILLE;
                                                        //get the default rate of likes per unit time in seconds;
                                                        double rateOfLike = 1000.0/1800.0;
                                                        //get the current rate of likes per unit time in seconds;
                                                        double currentRateOfLkes = likesCount * rateOfLike/MILLE;
                                                        //get the current price of post
                                                        final double currentPrice = currentRateOfLkes * DEFAULT_PRICE/rateOfLike;
                                                        //get the perfection value of post's interactivity online
                                                        double perfectionValue = GOLDEN_RATIO/likesCount;
                                                        //get the new worth of Post price in Sen
                                                        final double cingleWorth = perfectionValue * likesPerMille * currentPrice;
                                                        //round of the worth of the post to 10 decimal number
                                                        final double finalPoints = roundCredits( cingleWorth, 10);

                                                        Log.d("finalpoints > 0", finalPoints + "");

                                                        postWalletReference.document(postKey).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                                            @Override
                                                            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                                                                if (e != null) {
                                                                    Log.w(TAG, "Listen error", e);
                                                                    return;
                                                                }


                                                                if (documentSnapshot.exists()){
                                                                    final Balance balance = documentSnapshot.toObject(Balance.class);
                                                                    final double amountRedeemed = balance.getAmountRedeemed();
                                                                    Log.d(amountRedeemed + "", "amount redeemed");
                                                                    final  double amountDeposited = balance.getAmountDeposited();
                                                                    Log.d(amountDeposited + "", "amount deposited");
                                                                    final double senseCredits = amountDeposited + finalPoints;
                                                                    Log.d("sense credit", senseCredits + "");
                                                                    final double totalSenseCredits = senseCredits - amountRedeemed;
                                                                    Log.d("total sense credit", totalSenseCredits + "");

                                                                    senseCreditReference.document(postKey).update("amount", totalSenseCredits);
                                                                }else {
                                                                    Credit credit = new Credit();
                                                                    credit.setPushId(postKey);
                                                                    credit.setAmount(finalPoints);
                                                                    credit.setUid(firebaseAuth.getCurrentUser().getUid());
                                                                    senseCreditReference.document(postKey).set(credit);
                                                                    Log.d("new sense credits", finalPoints + "");
                                                                }
                                                            }
                                                        });

                                                    }
                                                }else {
                                                    final double finalPoints = 0.00;
                                                    Log.d("finalpoints <= 0", finalPoints + "");
                                                    postWalletReference.document(postKey).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                                        @Override
                                                        public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                                                            if (e != null) {
                                                                Log.w(TAG, "Listen error", e);
                                                                return;
                                                            }

                                                            if (documentSnapshot.exists()){
                                                                final Balance balance = documentSnapshot.toObject(Balance.class);
                                                                final double amountRedeemed = balance.getAmountRedeemed();
                                                                Log.d(amountRedeemed + "", "amount redeemed");
                                                                final  double amountDeposited = balance.getAmountDeposited();
                                                                Log.d(amountDeposited + "", "amount deposited");
                                                                final double senseCredits = amountDeposited + finalPoints;
                                                                Log.d("sense credit", senseCredits + "");
                                                                final double totalSenseCredits = senseCredits - amountRedeemed;
                                                                Log.d("total sense credit", totalSenseCredits + "");

                                                                senseCreditReference.document(postKey).update("amount", totalSenseCredits);
                                                            }else {
                                                                Credit credit = new Credit();
                                                                credit.setPushId(postKey);
                                                                credit.setAmount(finalPoints);
                                                                credit.setUid(firebaseAuth.getCurrentUser().getUid());
                                                                senseCreditReference.document(postKey).set(credit);
                                                            }
                                                        }
                                                    });
                                                }
                                            }
                                        });


                            }
                        });
            }
        });




    }


    @Override
    protected void onDocumentAdded(DocumentChange change) {
        super.onDocumentAdded(change);

    }

    @Override
    protected void onDocumentModified(DocumentChange change) {
        super.onDocumentModified(change);
    }

    @Override
    protected void onDocumentRemoved(DocumentChange change) {
        super.onDocumentRemoved(change);
        removeAt(change.getOldIndex());
    }


    @Override
    protected void onError(FirebaseFirestoreException e) {
        super.onError(e);
    }

    @Override
    protected void onDataChanged() {
        super.onDataChanged();
    }

    //region listeners
    private static double roundCredits(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    private static int roundPercentage(int value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.intValue();
    }
}

package com.andeqa.andeqa.profile;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.R;
import com.andeqa.andeqa.home.PostDetailActivity;
import com.andeqa.andeqa.models.Andeqan;
import com.andeqa.andeqa.models.CollectionPost;
import com.andeqa.andeqa.models.Single;
import com.andeqa.andeqa.comments.CommentsActivity;
import com.andeqa.andeqa.home.FullImageViewActivity;
import com.andeqa.andeqa.likes.LikesActivity;
import com.andeqa.andeqa.models.Balance;
import com.andeqa.andeqa.models.Market;
import com.andeqa.andeqa.models.Credit;
import com.andeqa.andeqa.models.Like;
import com.andeqa.andeqa.models.Timeline;
import com.andeqa.andeqa.models.TransactionDetails;
import com.andeqa.andeqa.likes.WhoLikedViewHolder;
import com.andeqa.andeqa.settings.DialogFragmentPostSettings;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.firebase.ui.firestore.ObservableSnapshotArray;
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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by J.EL on 11/14/2017.
 */

public class CollectionPostsAdapter extends RecyclerView.Adapter<CollectionPostsViewHolder> {
    private static final String TAG = CollectionPostsAdapter.class.getSimpleName();
    private Context mContext;
    private List<Single> singles = new ArrayList<>();
    //firestore
    private FirebaseFirestore firebaseFirestore;
    private CollectionReference postsCollection;
    private com.google.firebase.firestore.Query commentsCountQuery;
    private CollectionReference ownerReference;
    private CollectionReference usersReference;
    private CollectionReference commentsReference;
    private CollectionReference ifairReference;
    private CollectionReference senseCreditReference;
    private CollectionReference sellingReference;
    private CollectionReference likesReference;
    private CollectionReference relationsReference;
    private CollectionReference timelineCollection;
    private CollectionReference postWalletReference;
    private Query likesQuery;
    //firebase
    private DatabaseReference databaseReference;
    //firebase adapter
    private FirestoreRecyclerAdapter firestoreRecyclerAdapter;
    //firebase auth
    private FirebaseAuth firebaseAuth;
    private boolean processLikes = false;
    private boolean processDislikes = false;

    private static final double GOLDEN_RATIO = 1.618;
    private static final double DEFAULT_PRICE = 1.5;
    private static final String COLLECTION_ID = "collection id";
    private static final String EXTRA_POST_ID = "post id";
    private static final String EXTRA_USER_UID = "uid";
    private  static final int MAX_WIDTH = 200;
    private static final int MAX_HEIGHT = 200;
    private List<DocumentSnapshot> documentSnapshots = new ArrayList<>();


    public CollectionPostsAdapter(Context mContext) {
        this.mContext = mContext;
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

    @Override
    public CollectionPostsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_layout, parent, false);
        return new CollectionPostsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final CollectionPostsViewHolder holder, int position) {
        final CollectionPost collectionPost = getSnapshot(holder.getAdapterPosition()).toObject(CollectionPost.class);
        final String collectionId = collectionPost.getCollectionId();
        final String postId = collectionPost.getPushId();
        final String uid = collectionPost.getUid();


        //initialize firebase auth
        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null){
            //initialize firestore
            postsCollection = FirebaseFirestore.getInstance().collection(Constants.COLLECTIONS)
                    .document("collection_posts").collection(collectionId);
            ownerReference = FirebaseFirestore.getInstance().collection(Constants.POST_OWNERS);
            usersReference = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);
            ifairReference = FirebaseFirestore.getInstance().collection(Constants.SELLING);
            commentsReference = FirebaseFirestore.getInstance().collection(Constants.COMMENTS);
            likesReference = FirebaseFirestore.getInstance().collection(Constants.LIKES);
            relationsReference = FirebaseFirestore.getInstance().collection(Constants.RELATIONS);
            commentsCountQuery = commentsReference;
            senseCreditReference = FirebaseFirestore.getInstance().collection(Constants.SENSECREDITS);
            sellingReference = FirebaseFirestore.getInstance().collection(Constants.SELLING);
            timelineCollection = FirebaseFirestore.getInstance().collection(Constants.TIMELINE);
            postWalletReference = FirebaseFirestore.getInstance().collection(Constants.POST_WALLET);
            //firebase
            databaseReference = FirebaseDatabase.getInstance().getReference(Constants.RANDOM_PUSH_ID);


        }

        Picasso.with(mContext)
                .load(collectionPost.getImage())
                .networkPolicy(NetworkPolicy.OFFLINE)
                .into(holder.postImageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        //successfully loads from CACHE
                        Log.d("picasso", "successfully loaded");
                    }

                    @Override
                    public void onError() {
                        // fetch online because cache is not there
                        Picasso.with(mContext)
                                .load(collectionPost.getImage())
                                .into(holder.postImageView);
                    }
                });


        if (collectionPost.getTitle().equals("")){
            holder.cingleTitleRelativeLayout.setVisibility(View.GONE);
        }else {
            holder.cingleTitleTextView.setText(collectionPost.getTitle());
        }

        if (!TextUtils.isEmpty(collectionPost.getDescription())){
//                        holder.descriptionTextView.setText(collectionPost.getDescription());
            addReadMore(collectionPost.getDescription(), holder.descriptionTextView);
            addReadLess(collectionPost.getDescription(), holder.descriptionTextView);
            holder.descriptionRelativeLayout.setVisibility(View.VISIBLE);
        }

        holder.totalLikesCountTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, LikesActivity.class);
                intent.putExtra(CollectionPostsAdapter.EXTRA_POST_ID, postId);
                mContext.startActivity(intent);
            }
        });

        holder.commentsImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, CommentsActivity.class);
                intent.putExtra(CollectionPostsAdapter.COLLECTION_ID, collectionId);
                intent.putExtra(CollectionPostsAdapter.EXTRA_POST_ID, postId);
                mContext.startActivity(intent);
            }
        });

        holder.postImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, FullImageViewActivity.class);
                intent.putExtra(CollectionPostsAdapter.COLLECTION_ID, collectionId);
                intent.putExtra(CollectionPostsAdapter.EXTRA_POST_ID, postId);
                mContext.startActivity(intent);
            }
        });

        holder.tradeMethodTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, PostDetailActivity.class);
                intent.putExtra(CollectionPostsAdapter.COLLECTION_ID, collectionId);
                intent.putExtra(CollectionPostsAdapter.EXTRA_POST_ID, postId);
                mContext.startActivity(intent);
            }
        });

        holder.settingsImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putString(CollectionPostsAdapter.COLLECTION_ID, collectionId);
                bundle.putString(CollectionPostsAdapter.EXTRA_POST_ID, postId);
                FragmentManager fragmenManager = ((AppCompatActivity)mContext).getSupportFragmentManager();
                DialogFragmentPostSettings dialogFragmentPostSettings = DialogFragmentPostSettings.newInstance("single settngs");
                dialogFragmentPostSettings.setArguments(bundle);
                dialogFragmentPostSettings.show(fragmenManager, "single settings fragment");
            }
        });

        holder.profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, ProfileActivity.class);
                intent.putExtra(CollectionPostsAdapter.EXTRA_USER_UID, uid);
                mContext.startActivity(intent);
            }
        });


        senseCreditReference.document(postId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
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
                    holder.cingleSenseCreditsTextView.setText("SC" + " " + formatter.format(senseCredits));

                }else {
                    holder.cingleSenseCreditsTextView.setText("SC 0.00000000");
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

                if (documentSnapshot.exists()) {
                    final Andeqan cinggulan = documentSnapshot.toObject(Andeqan.class);
                    final String username = cinggulan.getUsername();
                    final String profileImage = cinggulan.getProfileImage();

                    holder.accountUsernameTextView.setText(username);
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
                }
            }
        });

        ownerReference.document(postId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
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



        //get the number of commments in a single
        commentsCountQuery.orderBy("postId").whereEqualTo("pushId", postId)
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


        sellingReference.document(postId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (documentSnapshot.exists()){
                    final Market market = documentSnapshot.toObject(Market.class);
                    holder.tradeMethodTextView.setText("@Selling");
                }else {
                    holder.tradeMethodTextView.setText("@NotOnSale");

                }

            }
        });


        //check get the count of likes after the top 5
        likesReference.document(postId).collection("likes").orderBy("uid").startAt(6)
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
                                    "more");
                        }else {
                            holder.totalLikesCountTextView.setVisibility(View.GONE);
                        }
                    }
                });

        //calculate the percentage of likes to dislikes
        likesReference.document(postId).collection("dislikes")
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
                            likesReference.document(postId).collection("likes")
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
                                                holder.likesPercentageTextView.setText(roundedPercent + "%" + " " + "Likes");
                                            }else {
                                                // calculate likes in percentage
                                                holder.likesPercentageTextView.setText("0%" + " " + "Likes");
                                            }
                                        }
                                    });
                        }else {
                            final int dislikes = dislikesSnapshots.size();
                            Log.d("dislikes count", dislikes + "");
                            likesReference.document(postId).collection("likes")
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
                                                holder.likesPercentageTextView.setText(roundedPercent + "%" + " " + "Likes");
                                            }else {
                                                holder.likesPercentageTextView.setText("0%" + " " + "Likes");
                                            }
                                        }
                                    });
                        }


                    }
                });

        //calculate the percentage of likes to dislikes
        likesReference.document(postId).collection("likes")
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
                            likesReference.document(postId).collection("dislikes")
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
                                                holder.dislikePercentageTextView.setText(roundedPercent + "%" + " " + "  Dislikes");
                                            }else {
                                                //calculate likes in percentage
                                                final int dislikes = dislikesSnapshots.size();
                                                Log.d("dislikes size", dislikes + "");
                                                final int likesPlusDislikes = likes + dislikes;
                                                Log.d("disikes plus dislikes", likesPlusDislikes + "");
                                                final int percentDislikes = 100 * dislikes/likesPlusDislikes;
                                                Log.d("dislikes percentage", percentDislikes + "");
                                                final int roundedPercent = roundPercentage(percentDislikes, 2);
                                                holder.dislikePercentageTextView.setText(roundedPercent + "%" + " " + "Dislikes");                                            }

                                        }
                                    });
                        }else {
                            final int likes = likesSnapshots.size();
                            Log.d("likes count size", likes + "");
                            likesReference.document(postId).collection("dislikes")
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
                                                holder.dislikePercentageTextView.setText(roundedPercent + "%" + " " + "Dislikes");
                                            }else {
                                                holder.dislikePercentageTextView.setText("0%" + " " + "Dislikes");                                            }

                                        }
                                    });
                        }


                    }
                });

        likesReference.document(postId).collection("likes")
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


        holder.dislikeImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                processDislikes = true;
                likesReference.document(postId).collection("dislikes")
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


        likesReference.document(postId)
                .collection("likes").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (!documentSnapshots.isEmpty()){
                    if (documentSnapshots.size() > 0){
                        holder.likesRelativeLayout.setVisibility(View.VISIBLE);
                        likesQuery = likesReference.document(postId).collection("likes").orderBy("uid");
                        FirestoreRecyclerOptions<Like> options = new FirestoreRecyclerOptions.Builder<Like>()
                                .setQuery(likesQuery, Like.class)
                                .build();

                        firestoreRecyclerAdapter = new FirestoreRecyclerAdapter<Like, WhoLikedViewHolder>(options) {

                            @Override
                            protected void onBindViewHolder(final WhoLikedViewHolder holder, int position, Like model) {
                                holder.bindWhoLiked(getSnapshots().getSnapshot(position));
                                Like like = getSnapshots().getSnapshot(position).toObject(Like.class);
                                final String uid = like.getUid();

                                holder.whoLikedImageView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Intent intent = new Intent(mContext, LikesActivity.class);
                                        intent.putExtra(CollectionPostsAdapter.COLLECTION_ID, collectionId);
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
                                            final Andeqan cinggulan = documentSnapshot.toObject(Andeqan.class);
                                            final String profileImage = cinggulan.getProfileImage();

                                            Picasso.with(mContext)
                                                    .load(profileImage)
                                                    .fit()
                                                    .centerCrop()
                                                    .placeholder(R.drawable.profle_image_background)
                                                    .networkPolicy(NetworkPolicy.OFFLINE)
                                                    .into(holder.whoLikedImageView, new Callback() {
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
                                                                    .into(holder.whoLikedImageView);


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
                        holder.likesRelativeLayout.setVisibility(View.GONE);
                    }
                }
            }
        });


        holder.likesImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                processLikes = true;
                likesReference.document(postId).collection("likes")
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
                                        Like like = new Like();
                                        like.setUid(firebaseAuth.getCurrentUser().getUid());
                                        like.setPushId(firebaseAuth.getCurrentUser().getUid());
                                        likesReference.document(postId).collection("likes")
                                                .document(firebaseAuth.getCurrentUser().getUid()).set(like)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        final Timeline timeline = new Timeline();
                                                        final long time = new Date().getTime();

                                                        timelineCollection.document(uid).collection("timeline")
                                                                .whereEqualTo("type", "like").whereEqualTo("pushId", postId)
                                                                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                                                    @Override
                                                                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                                                                        if (e != null) {
                                                                            Log.w(TAG, "Listen error", e);
                                                                            return;
                                                                        }

                                                                        if (documentSnapshots.isEmpty()){
                                                                            final String postId = databaseReference.push().getKey();
                                                                            timeline.setPushId(postId);
                                                                            timeline.setTime(time);
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
                                        likesReference.document(postId).collection("likes")
                                                .document(firebaseAuth.getCurrentUser().getUid()).delete();
                                        processLikes = false;
                                        holder.likesImageView.setColorFilter(Color.BLACK);

                                    }
                                }

                                likesReference.document(postId).collection("likes")
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
                                                        //get the current price of single
                                                        final double currentPrice = currentRateOfLkes * DEFAULT_PRICE/rateOfLike;
                                                        //get the perfection value of single's interactivity online
                                                        double perfectionValue = GOLDEN_RATIO/likesCount;
                                                        //get the new worth of Single price in Sen
                                                        final double cingleWorth = perfectionValue * likesPerMille * currentPrice;
                                                        //round of the worth of the single to 10 decimal number
                                                        final double finalPoints = roundCredits( cingleWorth, 10);

                                                        Log.d("finalpoints > 0", finalPoints + "");

                                                        postWalletReference.document(postId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
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

                                                                    senseCreditReference.document(postId).update("amount", totalSenseCredits);
                                                                }else {
                                                                    Credit credit = new Credit();
                                                                    credit.setPushId(postId);
                                                                    credit.setAmount(finalPoints);
                                                                    credit.setUid(firebaseAuth.getCurrentUser().getUid());
                                                                    senseCreditReference.document(postId).set(credit);
                                                                    Log.d("new sense credits", finalPoints + "");
                                                                }
                                                            }
                                                        });

                                                    }
                                                }else {
                                                    final double finalPoints = 0.00;
                                                    Log.d("finalpoints <= 0", finalPoints + "");
                                                    postWalletReference.document(postId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
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

                                                                senseCreditReference.document(postId).update("amount", totalSenseCredits);
                                                            }else {
                                                                Credit credit = new Credit();
                                                                credit.setPushId(postId);
                                                                credit.setAmount(finalPoints);
                                                                credit.setUid(firebaseAuth.getCurrentUser().getUid());
                                                                senseCreditReference.document(postId).set(credit);
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


    private static int roundPercentage(int value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.intValue();
    }

    //region listeners
    private static double roundCredits(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    private void addReadMore(final String text, final TextView textView) {
        if (text.substring(0, 120) != null){
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
        SpannableString ss = new SpannableString(text + " read less");
        addReadMore(text, textView);

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



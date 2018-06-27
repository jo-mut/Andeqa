package com.andeqa.andeqa.home;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.R;
import com.andeqa.andeqa.comments.CommentsActivity;
import com.andeqa.andeqa.likes.LikesActivity;
import com.andeqa.andeqa.market.RedeemCreditsActivity;
import com.andeqa.andeqa.models.Wallet;
import com.andeqa.andeqa.models.Andeqan;
import com.andeqa.andeqa.models.CollectionPost;
import com.andeqa.andeqa.models.Credit;
import com.andeqa.andeqa.models.Like;
import com.andeqa.andeqa.models.Post;
import com.andeqa.andeqa.models.Timeline;
import com.andeqa.andeqa.profile.ProfileActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
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

import javax.annotation.Nullable;

import static android.media.CamcorderProfile.get;

/**
 * Created by J.EL on 3/20/2018.
 */

public class PostsAdapter extends RecyclerView.Adapter<PostViewHolder> {
    private static final String TAG =  PostsAdapter.class.getSimpleName();
    private Context mContext;
    private static final String EXTRA_POST_ID = "post id";
    private static final String COLLECTION_ID = "collection id";
    private static final String TYPE = "type";
    private static final String EXTRA_USER_UID =  "uid";

    private static final String SOURCE = PostsAdapter.class.getSimpleName();

    private boolean processLikes = false;
    private boolean processDislikes = false;
    private boolean processWallet = false;
    private boolean processAmount = false;
    private boolean processCredit = false;
    private static final double DEFAULT_PRICE = 1.5;
    private static final double GOLDEN_RATIO = 1.618;
    private static final int MAX_WIDTH = 200;
    private static final int MAX_HEIGHT = 200;
    private static final int LIMIT = 10;
    //firestore reference
    private CollectionReference collectionsPosts;
    private com.google.firebase.firestore.Query commentsCountQuery;
    private CollectionReference usersReference;
    private CollectionReference commentsReference;
    private CollectionReference likesReference;
    private CollectionReference postWalletCollection;
    private CollectionReference postWalletReference;
    private CollectionReference timelineCollection;
    private CollectionReference creditsCollection;
    //firebase
    private DatabaseReference databaseReference;
    //firebase auth
    private FirebaseAuth firebaseAuth;
    //adapters

    public boolean showOnClick = true;

    DecimalFormat formatter = new DecimalFormat("0.000");
    private List<DocumentSnapshot> documentSnapshots = new ArrayList<>();


    public PostsAdapter(Context mContext) {
        this.mContext = mContext;
    }

    public void setRandomPosts(List<DocumentSnapshot> posts){
        this.documentSnapshots = posts;
        notifyDataSetChanged();
    }


    public DocumentSnapshot getSnapshot(int index) {
        return documentSnapshots.get(index);
    }


    @Override
    public int getItemCount() {
        return documentSnapshots.size();
    }



    @Override
    public PostViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_layout, parent, false);
        return new PostViewHolder(view);
    }



    @Override
    public void onBindViewHolder(final PostViewHolder holder, int position) {
        final Post post = getSnapshot(holder.getAdapterPosition()).toObject(Post.class);
        final String postId = post.getPost_id();
        final String uid = post.getUser_id();
        final String collectionId = post.getCollection_id();
        final String type = post.getType();


        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser()!= null){
            //firestore

            if (type.equals("single")){
                collectionsPosts = FirebaseFirestore.getInstance().collection(Constants.COLLECTIONS_POSTS)
                        .document("singles").collection(collectionId);
            }else if (type.equals("post")){
                collectionsPosts = FirebaseFirestore.getInstance().collection(Constants.COLLECTIONS_POSTS)
                        .document("collections").collection(collectionId);
            }else {
                //there is no query to run
            }

            usersReference = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);
            commentsReference = FirebaseFirestore.getInstance().collection(Constants.COMMENTS)
                    .document("post_ids").collection(postId);
            timelineCollection = FirebaseFirestore.getInstance().collection(Constants.TIMELINE);
            //firebase
            databaseReference = FirebaseDatabase.getInstance().getReference(Constants.RANDOM_PUSH_ID);
            //document reference
            commentsCountQuery= commentsReference;
            likesReference = FirebaseFirestore.getInstance().collection(Constants.LIKES);
            postWalletReference = FirebaseFirestore.getInstance().collection(Constants.POST_WALLET);
            postWalletCollection = FirebaseFirestore.getInstance().collection(Constants.POST_WALLET);
            creditsCollection = FirebaseFirestore.getInstance().collection(Constants.CREDITS);


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
                    Picasso.with(mContext)
                            .load(collectionPost.getImage())
                            .networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.image_place_holder)
                            .into(holder.postImageView, new Callback() {
                                @Override
                                public void onSuccess() {
                                    Log.v("Picasso", "Fetched image");
                                }

                                @Override
                                public void onError() {
                                    Picasso.with(mContext)
                                            .load(collectionPost.getImage())
                                            .placeholder(R.drawable.image_place_holder)
                                            .into(holder.postImageView, new Callback() {
                                                @Override
                                                public void onSuccess() {

                                                }

                                                @Override
                                                public void onError() {
                                                    Log.v("Picasso", "Could not fetch image");
                                                }
                                            });


                                }
                            });

                    if (!TextUtils.isEmpty(collectionPost.getTitle())){
                        holder.bottomLinearLayout.setVisibility(View.VISIBLE);
                        holder.titleTextView.setText(collectionPost.getTitle());
                        holder.titleRelativeLayout.setVisibility(View.VISIBLE);
                    }else {
                        holder.titleRelativeLayout.setVisibility(View.GONE);
                    }

                    if (!TextUtils.isEmpty(collectionPost.getDescription())){
                        //prevent collection note from overlapping other layouts
                        final String [] strings = collectionPost.getDescription().split("");

                        final int size = strings.length;

                        if (size <= 120){
                            holder.bottomLinearLayout.setVisibility(View.VISIBLE);
                            holder.descriptionRelativeLayout.setVisibility(View.VISIBLE);
                            holder.descriptionTextView.setText(collectionPost.getDescription());
                        }else{
                            holder.bottomLinearLayout.setVisibility(View.VISIBLE);
                            holder.descriptionRelativeLayout.setVisibility(View.VISIBLE);
                            final String boldMore = "...";
                            final String boldLess = "";
                            String normalText = collectionPost.getDescription().substring(0, 119);
                            holder.descriptionTextView.setText(normalText + boldMore);
                            holder.descriptionRelativeLayout.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (showOnClick){
                                        String normalText = collectionPost.getDescription();
                                        holder.descriptionTextView.setText(normalText + boldLess);
                                        showOnClick = false;
                                    }else {
                                        String normalText = collectionPost.getDescription().substring(0, 119);
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
                intent.putExtra(PostsAdapter.EXTRA_POST_ID, postId);
                intent.putExtra(PostsAdapter.COLLECTION_ID, collectionId);
                intent.putExtra(PostsAdapter.TYPE, type);
                mContext.startActivity(intent);
            }
        });


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

        holder.profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, ProfileActivity.class);
                intent.putExtra(PostsAdapter.EXTRA_USER_UID, uid);
                mContext.startActivity(intent);
            }
        });

        holder.mCreditsLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (uid.equals(firebaseAuth.getCurrentUser().getUid())){
                    Intent intent = new Intent(mContext, RedeemCreditsActivity.class);
                    intent.putExtra(PostsAdapter.EXTRA_POST_ID, postId);
                    intent.putExtra(PostsAdapter.COLLECTION_ID, collectionId);
                    intent.putExtra(PostsAdapter.TYPE, type);
                    mContext.startActivity(intent);
                }else {
                    Toast.makeText(mContext, "You can only redeem credo from your posts or singles",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });


        creditsCollection.document(postId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
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
                    holder.senseCreditsTextView.setText("Credo" + " " + formatter.format(senseCredits));

                }else {
                    holder.senseCreditsTextView.setText("Credo" + " " + "0.00000000");
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
                    final Andeqan cinggulan = documentSnapshot.toObject(Andeqan.class);
                    holder.usernameTextView.setText(cinggulan.getUsername());

                    Picasso.with(mContext)
                            .load(cinggulan.getProfile_image())
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
                                            .load(cinggulan.getProfile_image())
                                            .resize(MAX_WIDTH, MAX_HEIGHT)
                                            .onlyScaleDown()
                                            .centerCrop()
                                            .placeholder(R.drawable.ic_user)
                                            .into(holder.profileImageView);
                                }
                            });
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

//        likesReference.document(postId).collection("dislikes")
//                .addSnapshotListener(new EventListener<QuerySnapshot>() {
//                    @Override
//                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
//
//                        if (e != null) {
//                            Log.w(TAG, "Listen error", e);
//                            return;
//                        }
//
//                        if (!documentSnapshots.isEmpty()){
//                            holder.dislikeCountTextView.setText(documentSnapshots.size() + " ");
//                        }else {
//                            holder.dislikeCountTextView.setText("0");
//                        }
//
//                    }
//                });


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
                            //record new impression when on likes
//                            if (duration >= 3000){
//                                impressionCollection.document(postId)
//                                        .collection("post_impressions")
//                                        .whereEqualTo("user_id", firebaseAuth.getCurrentUser().getUid())
//                                        .addSnapshotListener(new EventListener<QuerySnapshot>() {
//                                            @Override
//                                            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
//                                                if (e != null) {
//                                                    Transaction.w(TAG, "Listen error", e);
//                                                    return;
//                                                }
//
//                                                if (queryDocumentSnapshots.isEmpty()){
//                                                    final String impression_id = databaseReference.push().getKey();
//                                                    Impression impression = new Impression();
//                                                    impression.setPost_id(postId);
//                                                    impression.setImpression_id(impression_id);
//                                                    impression.setUser_id(firebaseAuth.getCurrentUser().getUid());
//                                                    impressionCollection.document(postId).collection("post_impressions")
//                                                            .document(impression_id).set(impression);
//                                                }else {
//                                                    final String impression_id = databaseReference.push().getKey();
//                                                    Impression impression = new Impression();
//                                                    impression.setPost_id(postId);
//                                                    impression.setImpression_id(impression_id);
//                                                    impression.setUser_id(firebaseAuth.getCurrentUser().getUid());
//                                                    impressionCollection.document(postId).collection("post_impressions")
//                                                            .document(impression_id).set(impression);
//                                                }
//                                            }
//                                        });
//                            }

                        }else {
                            //changed the like image view background color to show user has liked
                            holder.likesImageView.setColorFilter(Color.RED);

//                            if (duration >= 3000){
//                                impressionCollection.document(postId)
//                                        .collection("post_impressions")
//                                        .whereEqualTo("user_id", firebaseAuth.getCurrentUser().getUid())
//                                        .addSnapshotListener(new EventListener<QuerySnapshot>() {
//                                            @Override
//                                            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
//                                                if (e != null) {
//                                                    Transaction.w(TAG, "Listen error", e);
//                                                    return;
//                                                }
//
//                                                if (queryDocumentSnapshots.isEmpty()){
//                                                    final String impression_id = databaseReference.push().getKey();
//                                                    Impression impression = new Impression();
//                                                    impression.setPost_id(postId);
//                                                    impression.setImpression_id(impression_id);
//                                                    impression.setUser_id(firebaseAuth.getCurrentUser().getUid());
//                                                    impressionCollection.document(postId).collection("post_impressions")
//                                                            .document(impression_id).set(impression);
//                                                }else {
//                                                    //do not record new impression because previous records were successfull impressions
//                                                }
//                                            }
//                                        });
//
//                            }

                        }

                    }
                });


//      color the like image view if the user has dislikes
//        likesReference.document(postId).collection("dislikes")
//                .whereEqualTo("user_id", firebaseAuth.getCurrentUser().getUid())
//                .addSnapshotListener(new EventListener<QuerySnapshot>() {
//                    @Override
//                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
//
//                        if (e != null) {
//                            Log.w(TAG, "Listen error", e);
//                            return;
//                        }

//                        if (documentSnapshots.isEmpty()){
                            //changed the dislike image view background color to show user has not disliked
//                            holder.dislikeImageView.setColorFilter(Color.BLACK);

//                            if (duration >= 3000){
//                                impressionCollection.document(postId)
//                                        .collection("post_impressions")
//                                        .whereEqualTo("user_id", firebaseAuth.getCurrentUser().getUid())
//                                        .addSnapshotListener(new EventListener<QuerySnapshot>() {
//                                            @Override
//                                            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
//                                                if (e != null) {
//                                                    Transaction.w(TAG, "Listen error", e);
//                                                    return;
//                                                }
//
//                                                if (queryDocumentSnapshots.isEmpty()){
//                                                    final String impression_id = databaseReference.push().getKey();
//                                                    Impression impression = new Impression();
//                                                    impression.setPost_id(postId);
//                                                    impression.setImpression_id(impression_id);
//                                                    impression.setUser_id(firebaseAuth.getCurrentUser().getUid());
//                                                    impressionCollection.document(postId).collection("post_impressions")
//                                                            .document(impression_id).set(impression);
//                                                }else {
//                                                    final String impression_id = databaseReference.push().getKey();
//                                                    Impression impression = new Impression();
//                                                    impression.setPost_id(postId);
//                                                    impression.setImpression_id(impression_id);
//                                                    impression.setUser_id(firebaseAuth.getCurrentUser().getUid());
//                                                    impressionCollection.document(postId).collection("post_impressions")
//                                                            .document(impression_id).set(impression);
//                                                }
//                                            }
//                                        });
//
//                            }
//                        }else {
                            //changed the dislike image view background color to show user has disliked
//                            holder.dislikeImageView.setColorFilter(Color.RED);
//                            if (duration >= 3000){
//                                impressionCollection.document(postId)
//                                        .collection("post_impressions")
//                                        .whereEqualTo("user_id", firebaseAuth.getCurrentUser().getUid())
//                                        .addSnapshotListener(new EventListener<QuerySnapshot>() {
//                                    @Override
//                                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
//                                        if (e != null) {
//                                            Transaction.w(TAG, "Listen error", e);
//                                            return;
//                                        }
//
//                                        if (queryDocumentSnapshots.isEmpty()){
//                                            final String impression_id = databaseReference.push().getKey();
//                                            Impression impression = new Impression();
//                                            impression.setPost_id(postId);
//                                            impression.setImpression_id(impression_id);
//                                            impression.setUser_id(firebaseAuth.getCurrentUser().getUid());
//                                            impressionCollection.document(postId).collection("post_impressions")
//                                                    .document(impression_id).set(impression);
//                                        }else {
//                                            //do not record new impression because previous records were successfull impressions
//                                        }
//                                    }
//                                });
//
//                            }
//                        }
//
//                    }
//                });

//        holder.dislikeImageView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                processDislikes = true;
//                processWallet = true;
//                likesReference.document(postId).collection("dislikes")
//                        .whereEqualTo("user_id", firebaseAuth.getCurrentUser().getUid())
//                        .addSnapshotListener(new EventListener<QuerySnapshot>() {
//                            @Override
//                            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
//
//                                if (e != null) {
//                                    Log.w(TAG, "Listen error", e);
//                                    return;
//                                }
//
//
//                                if (processDislikes){
//                                    if (documentSnapshots.isEmpty()){
//                                        Like like = new Like();
//                                        like.setUser_id(firebaseAuth.getCurrentUser().getUid());
//                                        likesReference.document(postId).collection("dislikes")
//                                                .document(firebaseAuth.getCurrentUser().getUid()).set(like);
//                                        processDislikes = false;
//                                        holder.dislikeImageView.setColorFilter(Color.RED);
//
//                                    }else {
//                                        likesReference.document(postId).collection("dislikes")
//                                                .document(firebaseAuth.getCurrentUser().getUid()).delete();
//                                        processDislikes = false;
//                                        holder.dislikeImageView.setColorFilter(Color.BLACK);
//
//                                    }
//                                }
//
//                            }
//                        });
//            }
//        });


        holder.likesRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                processLikes = true;
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
                                                                    final Credit credit = documentSnapshot.toObject(Credit.class);
                                                                    final double amountRedeemed =   credit.getAmount();
                                                                    Log.d(amountRedeemed + "", "amount redeemed");
                                                                    final  double amountDeposited = credit.getDeposited();
                                                                    Log.d(amountDeposited + "", "amount deposited");
                                                                    final double senseCredits = amountDeposited + finalPoints;
                                                                    Log.d("sense credit", senseCredits + "");
                                                                    final double totalSenseCredits = senseCredits - amountRedeemed;
                                                                    Log.d("total sense credit", totalSenseCredits + "");

                                                                    creditsCollection.document(postId).update("amount", totalSenseCredits);
                                                                }else {
                                                                    Credit credit = new Credit();
                                                                    credit.setPost_id(postId);
                                                                    credit.setAmount(finalPoints);
                                                                    credit.setUser_id(firebaseAuth.getCurrentUser().getUid());
                                                                    credit.setDeposited(0.0);
                                                                    credit.setRedeemed(0.0);
                                                                    creditsCollection.document(postId).set(credit);
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
                                                                final Credit credit = documentSnapshot.toObject(Credit.class);
                                                                final double amountRedeemed =   credit.getAmount();
                                                                Log.d(amountRedeemed + "", "amount redeemed");
                                                                final  double amountDeposited = credit.getDeposited();
                                                                Log.d(amountDeposited + "", "amount deposited");
                                                                final double senseCredits = amountDeposited + finalPoints;
                                                                Log.d("sense credit", senseCredits + "");
                                                                final double totalSenseCredits = senseCredits - amountRedeemed;
                                                                Log.d("total sense credit", totalSenseCredits + "");

                                                                creditsCollection.document(postId).update("amount", totalSenseCredits);
                                                            }else {
                                                                Credit credit = new Credit();
                                                                credit.setPost_id(postId);
                                                                credit.setAmount(finalPoints);
                                                                credit.setUser_id(firebaseAuth.getCurrentUser().getUid());
                                                                credit.setDeposited(0.0);
                                                                credit.setRedeemed(0.0);
                                                                creditsCollection.document(postId).set(credit);
                                                                Log.d("new sense credits", finalPoints + "");
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
    public void onViewAttachedToWindow(PostViewHolder holder) {
        super.onViewAttachedToWindow(holder);
//        startTime = System.currentTimeMillis();
    }

    @Override
    public void onViewDetachedFromWindow(PostViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
//        endTime = System.currentTimeMillis();
//        duration = endTime - startTime;

//        if (documentSnapshots.size() >0 ){
//            Post post = getSnapshot(holder.getAdapterPosition()).toObject(Post.class);
//            final String postId = post.getPost_id();
//
//            if (duration >= 3000){
//
//                viewCollection.document(postId)
//                        .addSnapshotListener(new EventListener<DocumentSnapshot>() {
//                            @Override
//                            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
//                                if (e != null) {
//                                    Transaction.w(TAG, "Listen error", e);
//                                    return;
//                                }
//
//                                if (documentSnapshot.exists()){
//                                    traceData = documentSnapshot.toObject(TraceData.class);
//                                    final long recordedDuration = traceData.getDuration();
//                                    final long newDuration = recordedDuration + duration;
//                                    //record total time spent on the post
//                                    viewCollection.document(postId).update("duration", newDuration);
//                                    Transaction.d("traced post_id", traceData.getPost_id());
//                                    Transaction.d("traced duration", newDuration + "");
//                                    //record the time each user spent on a post under their id
//                                    viewCollection.document(postId).collection(firebaseAuth.getCurrentUser().getUid())
//                                            .document(firebaseAuth.getCurrentUser().getUid())
//                                            .addSnapshotListener(new EventListener<DocumentSnapshot>() {
//                                                @Override
//                                                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
//                                                    if (e != null) {
//                                                        Transaction.w(TAG, "Listen error", e);
//                                                        return;
//                                                    }
//
//                                                    if (documentSnapshot.exists()){
//                                                        traceData = documentSnapshot.toObject(TraceData.class);
//                                                        final long recordedDuration = traceData.getDuration();
//                                                        final long newDuration = recordedDuration + duration;
//                                                        traceData.setDuration(newDuration);
//                                                        traceData.setPost_id(postId);
//                                                        traceData.setUser_id(firebaseAuth.getCurrentUser().getUid());
//                                                        viewCollection.document(postId).collection(firebaseAuth.getCurrentUser().getUid())
//                                                                .document(firebaseAuth.getCurrentUser().getUid()).set(traceData);
//                                                    }else {
//                                                        traceData = new TraceData();
//                                                        traceData.setDuration(duration + 3000);
//                                                        traceData.setPost_id(postId);
//                                                        traceData.setUser_id(firebaseAuth.getCurrentUser().getUid());
//                                                        viewCollection.document(postId).collection(firebaseAuth.getCurrentUser().getUid())
//                                                                .document(firebaseAuth.getCurrentUser().getUid()).set(traceData);
//                                                    }
//                                                }
//                                            });
//                                }else {
//                                    traceData = new TraceData();
//                                    traceData.setDuration(duration + 3000);
//                                    traceData.setPost_id(postId);
//                                    traceData.setUser_id(firebaseAuth.getCurrentUser().getUid());
//                                    viewCollection.document(postId).set(traceData);
//
//                                    Transaction.d("traced post_id", traceData.getPost_id());
//                                    Transaction.d("traced duration", traceData.getDuration() + "");
//
//                                    //record the time each user spent on a post under their id
//                                    viewCollection.document(postId).collection(firebaseAuth.getCurrentUser().getUid())
//                                            .document(firebaseAuth.getCurrentUser().getUid())
//                                            .addSnapshotListener(new EventListener<DocumentSnapshot>() {
//                                                @Override
//                                                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
//                                                    if (e != null) {
//                                                        Transaction.w(TAG, "Listen error", e);
//                                                        return;
//                                                    }
//
//                                                    if (documentSnapshot.exists()){
//                                                        traceData = documentSnapshot.toObject(TraceData.class);
//                                                        final long recordedDuration = traceData.getDuration();
//                                                        final long newDuration = recordedDuration + duration;
//                                                        traceData.setDuration(newDuration);
//                                                        traceData.setPost_id(postId);
//                                                        traceData.setUser_id(firebaseAuth.getCurrentUser().getUid());
//                                                        viewCollection.document(postId).collection(firebaseAuth.getCurrentUser().getUid())
//                                                                .document(firebaseAuth.getCurrentUser().getUid()).set(traceData);
//                                                    }else {
//                                                        traceData = new TraceData();
//                                                        traceData.setDuration(duration + 3000);
//                                                        traceData.setPost_id(postId);
//                                                        traceData.setUser_id(firebaseAuth.getCurrentUser().getUid());
//                                                        viewCollection.document(postId).collection(firebaseAuth.getCurrentUser().getUid())
//                                                                .document(firebaseAuth.getCurrentUser().getUid()).set(traceData);
//                                                    }
//                                                }
//                                            });
//                                }
//                            }
//                        });
//            }
//        }

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

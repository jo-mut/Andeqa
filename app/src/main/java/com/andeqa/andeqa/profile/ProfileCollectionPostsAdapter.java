package com.andeqa.andeqa.profile;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.R;
import com.andeqa.andeqa.collections.CollectionPostsAdapter;
import com.andeqa.andeqa.collections.CollectionPostsViewHolder;
import com.andeqa.andeqa.comments.CommentsActivity;
import com.andeqa.andeqa.home.ImageViewActivity;
import com.andeqa.andeqa.home.PostDetailActivity;
import com.andeqa.andeqa.home.PostViewHolder;
import com.andeqa.andeqa.likes.LikesActivity;
import com.andeqa.andeqa.models.Andeqan;
import com.andeqa.andeqa.models.Balance;
import com.andeqa.andeqa.models.CollectionPost;
import com.andeqa.andeqa.models.Credit;
import com.andeqa.andeqa.models.Like;
import com.andeqa.andeqa.models.Timeline;
import com.andeqa.andeqa.models.TransactionDetails;
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

public class ProfileCollectionPostsAdapter extends RecyclerView.Adapter<CollectionPostsViewHolder> {
    private static final String TAG = ProfileCollectionPostsAdapter.class.getSimpleName();
    private Context mContext;
    //firestore
    private FirebaseFirestore firebaseFirestore;
    private CollectionReference collectionsPosts;
    private com.google.firebase.firestore.Query commentsCountQuery;
    private CollectionReference postOwnersReference;
    private CollectionReference usersReference;
    private CollectionReference commentsReference;
    private CollectionReference sellingCollection;
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
    private boolean showOnClick = false;

    private static final double GOLDEN_RATIO = 1.618;
    private static final double DEFAULT_PRICE = 1.5;
    private static final String COLLECTION_ID = "collection id";
    private static final String EXTRA_POST_ID = "post id";
    private static final String EXTRA_USER_UID = "uid";
    private  static final int MAX_WIDTH = 200;
    private static final int MAX_HEIGHT = 200;
    private List<DocumentSnapshot> documentSnapshots = new ArrayList<>();

    public ProfileCollectionPostsAdapter(Context mContext) {
        this.mContext = mContext;
    }

    protected void setCollectionPosts(List<DocumentSnapshot> mSnapshots){
        this.documentSnapshots = mSnapshots;
        notifyDataSetChanged();
    }

    public DocumentSnapshot getSnapshot(int index) {
        return documentSnapshots.get(index);
    }


    @NonNull
    @Override
    public CollectionPostsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_list_layout, parent, false);
        return new CollectionPostsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final @NonNull CollectionPostsViewHolder holder, int position) {
        final CollectionPost collectionPost = getSnapshot(holder.getAdapterPosition()).toObject(CollectionPost.class);
        final String collectionId = collectionPost.getCollection_id();
        final String postId = collectionPost.getPost_id();
        final String uid = collectionPost.getUser_id();


        //initialize firebase auth
        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null){
            //initialize firestore
            collectionsPosts = FirebaseFirestore.getInstance().collection(Constants.COLLECTIONS_POSTS)
                    .document("collection").collection(collectionId);
            postOwnersReference = FirebaseFirestore.getInstance().collection(Constants.POST_OWNERS);
            usersReference = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);
            sellingCollection = FirebaseFirestore.getInstance().collection(Constants.SELLING);
            commentsReference = FirebaseFirestore.getInstance().collection(Constants.COMMENTS)
                    .document("post_ids").collection(postId);
            likesReference = FirebaseFirestore.getInstance().collection(Constants.LIKES);
            relationsReference = FirebaseFirestore.getInstance().collection(Constants.RELATIONS);
            commentsCountQuery = commentsReference;
            senseCreditReference = FirebaseFirestore.getInstance().collection(Constants.CREDITS);
            sellingReference = FirebaseFirestore.getInstance().collection(Constants.SELLING);
            timelineCollection = FirebaseFirestore.getInstance().collection(Constants.TIMELINE);
            postWalletReference = FirebaseFirestore.getInstance().collection(Constants.POST_WALLET);
            //firebase
            databaseReference = FirebaseDatabase.getInstance().getReference(Constants.RANDOM_PUSH_ID);

        }

        Picasso.with(mContext)
                .load(collectionPost.getImage())
                .networkPolicy(NetworkPolicy.OFFLINE)
                .placeholder(R.drawable.image_place_holder)
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
                                .placeholder(R.drawable.image_place_holder)
                                .into(holder.postImageView);
                    }
                });


        if (!TextUtils.isEmpty(collectionPost.getTitle())){
            holder.titleTextView.setText(collectionPost.getTitle());
            holder.titleRelativeLayout.setVisibility(View.GONE);
        }else {
            holder.titleTextView.setText("");
            holder.titleRelativeLayout.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(collectionPost.getDescription())){
            final String [] strings = collectionPost.getDescription().split("");

            final int size = strings.length;

            if (size <= 120){
                holder.descriptionRelativeLayout.setVisibility(View.VISIBLE);
                holder.descriptionTextView.setText(collectionPost.getDescription());
            }else{

                holder.descriptionRelativeLayout.setVisibility(View.VISIBLE);
                final String boldMore = "...read more";
                final String boldLess = "...read less";
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


        holder.likesRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, LikesActivity.class);
                intent.putExtra(ProfileCollectionPostsAdapter.EXTRA_POST_ID, postId);
                mContext.startActivity(intent);
            }
        });

        holder.mTradeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent =  new Intent(mContext, PostDetailActivity.class);
                intent.putExtra(ProfileCollectionPostsAdapter.EXTRA_POST_ID, postId);
                intent.putExtra(ProfileCollectionPostsAdapter.COLLECTION_ID, collectionId);
                intent.putExtra(ProfileCollectionPostsAdapter.EXTRA_USER_UID, uid);
                mContext.startActivity(intent);
                ((Activity)mContext).finish();
            }
        });

        holder.commentsImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, CommentsActivity.class);
                intent.putExtra(ProfileCollectionPostsAdapter.COLLECTION_ID, collectionId);
                intent.putExtra(ProfileCollectionPostsAdapter.EXTRA_POST_ID, postId);
                mContext.startActivity(intent);
            }
        });

        holder.postImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, ImageViewActivity.class);
                intent.putExtra(ProfileCollectionPostsAdapter.COLLECTION_ID, collectionId);
                intent.putExtra(ProfileCollectionPostsAdapter.EXTRA_POST_ID, postId);
                mContext.startActivity(intent);
            }
        });

        holder.profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((Activity)mContext).finish();
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
                    holder.senseCreditsTextView.setText("uC" + " " + formatter.format(senseCredits));

                }else {
                    holder.senseCreditsTextView.setText("uC 0.00000000");
                }
            }
        });

        //check if single is listed on the marketplace
        sellingCollection.document(postId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (documentSnapshot.exists()){
                    postOwnersReference.document(postId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {

                            if (e != null) {
                                Log.w(TAG, "Listen error", e);
                                return;
                            }

                            if (documentSnapshot.exists()){
                                final TransactionDetails td = documentSnapshot.toObject(TransactionDetails.class);
                                final String ownerUid = td.getUser_id();

                                if (firebaseAuth.getCurrentUser().getUid().equals(ownerUid)){
                                    holder.mTradeButton.setText("Unlist");
                                }else {
                                    holder.mTradeButton.setText("Buy");
                                }

                            }
                        }
                    });
                }else if (firebaseAuth.getCurrentUser().getUid().equals(uid)){
                    holder.mTradeButton.setText("Sell");
                }else{
                    holder.mTradeButton.setText("Info");
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
                    final String profileImage = cinggulan.getProfile_image();

                    holder.usernameTextView.setText(username);
                    Picasso.with(mContext)
                            .load(profileImage)
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
                                            .load(profileImage)
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


        //get the count of likes after the top 5
        likesReference.document(postId).collection("likes")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                        if (e != null) {
                            Log.w(TAG, "Listen error", e);
                            return;
                        }

                        if (!documentSnapshots.isEmpty()){
                            holder.likesCountTextView.setText(documentSnapshots.size() + "");
                        }else {
                            holder.likesCountTextView.setText("0");
                        }
                    }
                });

        //get the count of likes after the top 5
        likesReference.document(postId).collection("dislikes")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                        if (e != null) {
                            Log.w(TAG, "Listen error", e);
                            return;
                        }

                        if (!documentSnapshots.isEmpty()){
                            holder.dislikeCountTextView.setText(documentSnapshots.size() + "");
                        }else {
                            holder.dislikeCountTextView.setText("0");
                        }
                    }
                });


        likesReference.document(postId).collection("likes")
                .whereEqualTo("user_id", firebaseAuth.getCurrentUser().getUid())
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

        holder.likesImageView.setOnClickListener(new View.OnClickListener() {
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
                                        Like like = new Like();
                                        like.setUser_id(firebaseAuth.getCurrentUser().getUid());
                                        likesReference.document(postId).collection("likes")
                                                .document(firebaseAuth.getCurrentUser().getUid()).set(like);

                                        timelineCollection.document(uid).collection("timeline")
                                                .whereEqualTo("type", "like").whereEqualTo("post_id", postId)
                                                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                                    @Override
                                                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                                                        if (e != null) {
                                                            Log.w(TAG, "Listen error", e);
                                                            return;
                                                        }

                                                        if (documentSnapshots.isEmpty()){
                                                            final String activityId = databaseReference.push().getKey();
                                                            final Timeline timeline = new Timeline();
                                                            final long time = new Date().getTime();
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
                                                                        .document(postId)
                                                                        .set(timeline);
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
                                                                    final Balance balance = documentSnapshot.toObject(Balance.class);
                                                                    final double amountRedeemed = balance.getAmount_redeemed();
                                                                    Log.d(amountRedeemed + "", "amount redeemed");
                                                                    final  double amountDeposited = balance.getAmount_deposited();
                                                                    Log.d(amountDeposited + "", "amount deposited");
                                                                    final double senseCredits = amountDeposited + finalPoints;
                                                                    Log.d("sense credit", senseCredits + "");
                                                                    final double totalSenseCredits = senseCredits - amountRedeemed;
                                                                    Log.d("total sense credit", totalSenseCredits + "");

                                                                    senseCreditReference.document(postId).update("amount", totalSenseCredits);
                                                                }else {
                                                                    Credit credit = new Credit();
                                                                    credit.setPost_id(postId);
                                                                    credit.setAmount(finalPoints);
                                                                    credit.setUser_id(firebaseAuth.getCurrentUser().getUid());
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
                                                                final double amountRedeemed = balance.getAmount_redeemed();
                                                                Log.d(amountRedeemed + "", "amount redeemed");
                                                                final  double amountDeposited = balance.getAmount_deposited();
                                                                Log.d(amountDeposited + "", "amount deposited");
                                                                final double senseCredits = amountDeposited + finalPoints;
                                                                Log.d("sense credit", senseCredits + "");
                                                                final double totalSenseCredits = senseCredits - amountRedeemed;
                                                                Log.d("total sense credit", totalSenseCredits + "");

                                                                senseCreditReference.document(postId).update("amount", totalSenseCredits);
                                                            }else {
                                                                Credit credit = new Credit();
                                                                credit.setPost_id(postId);
                                                                credit.setAmount(finalPoints);
                                                                credit.setUser_id(firebaseAuth.getCurrentUser().getUid());
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

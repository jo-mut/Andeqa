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
import com.cinggl.cinggl.comments.CommentsActivity;
import com.cinggl.cinggl.firestore.FirestoreAdapter;
import com.cinggl.cinggl.likes.LikesActivity;
import com.cinggl.cinggl.models.Balance;
import com.cinggl.cinggl.models.Cinggulan;
import com.cinggl.cinggl.models.Credit;
import com.cinggl.cinggl.models.Like;
import com.cinggl.cinggl.models.Post;
import com.cinggl.cinggl.models.PostSale;
import com.cinggl.cinggl.models.Timeline;
import com.cinggl.cinggl.models.TransactionDetails;
import com.cinggl.cinggl.people.FollowerProfileActivity;
import com.cinggl.cinggl.settings.DialogPostsSettingsFragment;
import com.cinggl.cinggl.profile.PersonalProfileActivity;
import com.cinggl.cinggl.likes.WhoLikedViewHolder;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.firebase.ui.firestore.ObservableSnapshotArray;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.util.Log.d;

/**
 * Created by J.EL on 12/9/2017.
 */

public class OtherPostAdapter extends FirestoreAdapter<OtherPostViewHolder> {
    private static final String TAG = OtherPostAdapter.class.getSimpleName();
    private Context mContext;
    private List<Credit> credits = new ArrayList<>();
    private static final String EXTRA_POST_KEY = "post key";
    private static final String EXTRA_USER_UID = "uid";
    private boolean processLikes = false;
    private static final double DEFAULT_PRICE = 1.5;
    private static final double GOLDEN_RATIO = 1.618;
    private static final int MAX_WIDTH = 200;
    private static final int MAX_HEIGHT = 200;
    //firestore reference
    private FirebaseFirestore firebaseFirestore;
    private CollectionReference cinglesReference;
    private CollectionReference ifairReference;
    private Query commentsCountQuery;
    private CollectionReference ownerReference;
    private CollectionReference usersReference;
    private CollectionReference commentsReference;
    private CollectionReference senseCreditReference;
    private CollectionReference postWalletReference;
    private CollectionReference likesReference;
    private CollectionReference timelineCollection;
    private Query likesQuery;
    //firebase auth
    private FirebaseAuth firebaseAuth;
    //adapters
    private FirestoreRecyclerAdapter firestoreRecyclerAdapter;

    public OtherPostAdapter(Query query, Context mContext) {
        super(query);
        this.mContext = mContext;
    }


    @Override
    public int getItemCount() {
        return super.getItemCount();

    }

    @Override
    public OtherPostViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new OtherPostViewHolder(inflater.inflate(R.layout.post_best_list, parent, false));
    }

    @Override
    public void onBindViewHolder(final OtherPostViewHolder holder, int position) {
        final Credit credit = getSnapshot(position).toObject(Credit.class);
        holder.bindBestCingle(getSnapshot(position));
        final String postKey = credit.getPushId();
        final double senseCredits = credit.getAmount();
        Log.d("best cingle postkey", postKey);


        firebaseAuth = FirebaseAuth.getInstance();
       if (firebaseAuth.getCurrentUser() != null){
           //firestore
           cinglesReference = FirebaseFirestore.getInstance().collection(Constants.POSTS);
           ownerReference = FirebaseFirestore.getInstance().collection(Constants.CINGLE_ONWERS);
           usersReference = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);
           ifairReference = FirebaseFirestore.getInstance().collection(Constants.SELLING);
           commentsReference = FirebaseFirestore.getInstance().collection(Constants.COMMENTS);
           senseCreditReference = FirebaseFirestore.getInstance().collection(Constants.SENSECREDITS);
           //document reference
           commentsCountQuery= commentsReference;
           likesReference = FirebaseFirestore.getInstance().collection(Constants.LIKES);
           postWalletReference = FirebaseFirestore.getInstance().collection(Constants.CINGLE_WALLET);
           timelineCollection = FirebaseFirestore.getInstance().collection(Constants.TIMELINE);
       }

        if (senseCredits > 0){
            DecimalFormat formatter = new DecimalFormat("0.00000000");
            holder.senseCreditsTextView.setText("SC" + " " + formatter.format(senseCredits));
        }else if (senseCredits == 0){
            holder.senseCreditsTextView.setText("SC 0.00000000");
        }else {
            DecimalFormat formatter = new DecimalFormat("0.00000000");
            holder.senseCreditsTextView.setText("SC" + " " + formatter.format(senseCredits));
        }

        //path to cingle wallet reference
        holder.likesCountTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, LikesActivity.class);
                intent.putExtra(OtherPostAdapter.EXTRA_POST_KEY, postKey);
                mContext.startActivity(intent);
            }
        });

        holder.commentsImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent =  new Intent(mContext, CommentsActivity.class);
                intent.putExtra(OtherPostAdapter.EXTRA_POST_KEY, postKey);
                mContext.startActivity(intent);
            }
        });

        holder.postImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, FullImageViewActivity.class);
                intent.putExtra(OtherPostAdapter.EXTRA_POST_KEY, postKey);
                mContext.startActivity(intent);
            }
        });

        holder.tradeMethodTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent =  new Intent(mContext, PostDetailActivity.class);
                intent.putExtra(OtherPostAdapter.EXTRA_POST_KEY, postKey);
                mContext.startActivity(intent);
            }
        });


        holder.settingsImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putString(OtherPostAdapter.EXTRA_POST_KEY, postKey);
                FragmentManager fragmenManager = ((AppCompatActivity)mContext).getSupportFragmentManager();
                DialogPostsSettingsFragment dialogPostsSettingsFragment = DialogPostsSettingsFragment.newInstance("best posts settings");
                dialogPostsSettingsFragment.setArguments(bundle);
                dialogPostsSettingsFragment.show(fragmenManager, "best settings fragment");

            }
        });


        cinglesReference.document(postKey).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (documentSnapshot.exists()){
                    final Post post = documentSnapshot.toObject(Post.class);
                    final String uid = post.getUid();


                    Picasso.with(mContext)
                            .load(post.getImage())
                            .networkPolicy(NetworkPolicy.OFFLINE)
                            .into(holder.postImageView, new Callback() {
                                @Override
                                public void onSuccess() {
                                    Log.v("Picasso", "Fetched best image");
                                }

                                @Override
                                public void onError() {
                                    Picasso.with(mContext)
                                            .load(post.getImage())
                                            .into(holder.postImageView, new Callback() {
                                                @Override
                                                public void onSuccess() {

                                                }

                                                @Override
                                                public void onError() {
                                                    Log.v("Picasso", "Could not best fetch image");
                                                }
                                            });


                                }
                            });

                    if (post.getTitle().equals("")){
                        holder.titleRelativeLayout.setVisibility(View.GONE);
                    }else {
                        holder.titleTextView.setText(post.getTitle());
                    }

                    if (post.getDescription().equals("")){
                        holder.descriptionRelativeLayout.setVisibility(View.GONE);
                    }else {
                        holder.descriptionTextView.setText(post.getDescription());
                    }

                    holder.datePostedTextView.setText(post.getDatePosted());

                    holder.profileImageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if ((firebaseAuth.getCurrentUser().getUid()).equals(uid)){
                                Intent intent = new Intent(mContext, PersonalProfileActivity.class);
                                intent.putExtra(OtherPostAdapter.EXTRA_USER_UID, uid);
                                mContext.startActivity(intent);
                                d("profile uid", firebaseAuth.getCurrentUser().getUid());
                            }else {
                                Intent intent = new Intent(mContext, FollowerProfileActivity.class);
                                intent.putExtra(OtherPostAdapter.EXTRA_USER_UID, uid);
                                d("follower uid", uid);
                                mContext.startActivity(intent);
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

                                holder.usernameTextView.setText(cinggulan.getUsername());
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
                                                    Like like = new Like();
                                                    like.setUid(firebaseAuth.getCurrentUser().getUid());
                                                    like.setPushId(firebaseAuth.getCurrentUser().getUid());
                                                    likesReference.document(postKey).collection("likes")
                                                            .document(firebaseAuth.getCurrentUser().getUid()).set(like)
                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {
                                                                    Timeline timeline = new Timeline();
                                                                    final long time = new Date().getTime();

                                                                    final String postId = timelineCollection.document(uid).collection("timeline")
                                                                            .document().getId();

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
                                                                                .document(postId).set(timeline);
                                                                    }

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
                                                                    final double finalPoints = round( cingleWorth, 10);

                                                                    Log.d("final points", finalPoints + "");

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
            }
        });

        ifairReference.document(postKey).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (documentSnapshot.exists()){
                    final PostSale postSale = documentSnapshot.toObject(PostSale.class);
                    DecimalFormat formatter = new DecimalFormat("0.00000000");
                    holder.postSalePriceTextView.setText("SC" + " " + formatter.format(postSale.getSalePrice()));
                    holder.tradeMethodTextView.setText("@Selling");
                }else {
                    holder.postSalePriceTitleRelativeLayout.setVisibility(View.GONE);
                    holder.tradeMethodTextView.setText("@NotListed");

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
                    d("owner uid", ownerUid);

                    holder.ownerImageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if ((firebaseAuth.getCurrentUser().getUid()).equals(ownerUid)){
                                Intent intent = new Intent(mContext, PersonalProfileActivity.class);
                                intent.putExtra(OtherPostAdapter.EXTRA_USER_UID, ownerUid);
                                mContext.startActivity(intent);
                                d("profile uid", firebaseAuth.getCurrentUser().getUid());
                            }else {
                                Intent intent = new Intent(mContext, FollowerProfileActivity.class);
                                intent.putExtra(OtherPostAdapter.EXTRA_USER_UID, ownerUid);
                                d("follower uid", ownerUid);
                                mContext.startActivity(intent);
                            }
                        }
                    });

                    usersReference.document(ownerUid).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                            if (e != null) {
                                Log.w(TAG, "Listen error", e);
                                return;
                            }

                            if (documentSnapshot.exists()){
                                Cinggulan cinggulan = documentSnapshot.toObject(Cinggulan.class);
                                final String profileImage = cinggulan.getProfileImage();
                                final String username = cinggulan.getUsername();
                                holder.postOwnerTextView.setText(username);
                                Picasso.with(mContext)
                                        .load(profileImage)
                                        .resize(MAX_WIDTH, MAX_HEIGHT)
                                        .onlyScaleDown()
                                        .centerCrop()
                                        .placeholder(R.drawable.profle_image_background)
                                        .networkPolicy(NetworkPolicy.OFFLINE)
                                        .into(holder.ownerImageView, new Callback() {
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
                                                        .into(holder.ownerImageView);
                                            }
                                        });
                            }
                        }
                    });

                    if (firebaseAuth.getCurrentUser().getUid().equals(ownerUid)){
                        holder.settingsImageView.setVisibility(View.VISIBLE);
                    }else {
                        holder.settingsImageView.setVisibility(View.INVISIBLE);
                    }
                }
            }
        });

        //get the number of commments in a cingle
        commentsCountQuery.orderBy("pushId").whereEqualTo("postId", postKey)
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

        likesReference.document(postKey).collection("likes")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                        if (e != null) {
                            Log.w(TAG, "Listen error", e);
                            return;
                        }

                        if (!documentSnapshots.isEmpty()){
                            holder.likesCountTextView.setText(documentSnapshots.size() + " " + "Likes");
                        }else {
                            holder.likesCountTextView.setText("0" + " " + "Likes");
                        }

                    }
                });


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



        likesReference.document(postKey).collection("likes").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (!documentSnapshots.isEmpty()){
                    if (documentSnapshots.size() > 0){
                        holder.likesRecyclerView.setVisibility(View.VISIBLE);
                        likesQuery = likesReference.document(postKey).collection("likes").orderBy("uid");
                        FirestoreRecyclerOptions<Like> options = new FirestoreRecyclerOptions.Builder<Like>()
                                .setQuery(likesQuery, Like.class)
                                .build();

                        firestoreRecyclerAdapter = new FirestoreRecyclerAdapter<Like, WhoLikedViewHolder>(options) {

                            @Override
                            protected void onBindViewHolder(final WhoLikedViewHolder holder, int position, Like model) {
                                holder.bindWhoLiked(getSnapshots().getSnapshot(position));
                                Like like = getSnapshots().getSnapshot(position).toObject(Like.class);
                                final String postKey = like.getPushId();
                                final String uid = like.getUid();

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
                        holder.likesRecyclerView.setVisibility(View.GONE);
                    }
                }
            }
        });

    }


    @Override
    protected void onDocumentRemoved(DocumentChange change) {
        super.onDocumentRemoved(change);
        removeAt(change.getOldIndex());
    }


    //region listeners
    private static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

}

package com.cinggl.cinggl.adapters;

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
import com.cinggl.cinggl.home.PostDetailActivity;
import com.cinggl.cinggl.models.Post;
import com.cinggl.cinggl.preferences.CingleSettingsDialog;
import com.cinggl.cinggl.comments.CommentsActivity;
import com.cinggl.cinggl.home.FullImageViewActivity;
import com.cinggl.cinggl.likes.LikesActivity;
import com.cinggl.cinggl.models.Balance;
import com.cinggl.cinggl.models.PostSale;
import com.cinggl.cinggl.models.Cinggulan;
import com.cinggl.cinggl.models.Credit;
import com.cinggl.cinggl.models.Like;
import com.cinggl.cinggl.models.TransactionDetails;
import com.cinggl.cinggl.people.FollowerProfileActivity;
import com.cinggl.cinggl.profile.PersonalProfileActivity;
import com.cinggl.cinggl.viewholders.BestPostsViewHolder;
import com.cinggl.cinggl.viewholders.WhoLikedViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import static android.util.Log.d;

/**
 * Created by J.EL on 7/19/2017.
 */

//CURRENTLY NOT IN USE

public class TopPostsAdapter extends RecyclerView.Adapter<BestPostsViewHolder> {
    private static final String TAG = TopPostsAdapter.class.getSimpleName();
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
    //firebase
    private DatabaseReference cingleWalletRef;
    private DatabaseReference likesRef;
    private com.google.firebase.database.Query likesQuery;
    //firebase auth
    private FirebaseAuth firebaseAuth;
    //adapters
    private FirebaseRecyclerAdapter firebaseRecyclerAdapter;


    public TopPostsAdapter(Context mContext) {
        this.mContext = mContext;
    }

    public void setBestCingles(List<Credit> credits) {
        this.credits = credits;
        notifyDataSetChanged();
    }

    public void removeAt(int position){
        credits.remove(credits.get(position));
    }

    @Override
    public int getItemCount() {
        return credits.size();
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    @Override
    public BestPostsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new BestPostsViewHolder(inflater.inflate(R.layout.best_posts_list, parent, false));
    }


    @Override
    public void onBindViewHolder(final BestPostsViewHolder holder, int position) {
        final Credit credit = credits.get(position);
        holder.bindBestCingle(credit);
        final String postKey = credits.get(position).getPushId();
        final double senseCredits = credits.get(position).getAmount();
        Log.d("best cingle postkey", postKey);


        firebaseAuth = FirebaseAuth.getInstance();

        //firebase
        likesRef = FirebaseDatabase.getInstance().getReference(Constants.LIKES);
        likesQuery = likesRef.child(postKey).limitToFirst(5);
        cingleWalletRef = FirebaseDatabase.getInstance().getReference(Constants.CINGLE_WALLET);

        likesRef.keepSynced(true);
        cingleWalletRef.keepSynced(true);

        //firestore
        cinglesReference = FirebaseFirestore.getInstance().collection(Constants.POSTS);
        ownerReference = FirebaseFirestore.getInstance().collection(Constants.CINGLE_ONWERS);
        usersReference = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);
        ifairReference = FirebaseFirestore.getInstance().collection(Constants.IFAIR);
        commentsReference = FirebaseFirestore.getInstance().collection(Constants.COMMENTS);
        senseCreditReference = FirebaseFirestore.getInstance().collection(Constants.SENSECREDITS);
        //document reference
        commentsCountQuery= commentsReference;

        if (senseCredits > 0){
            DecimalFormat formatter = new DecimalFormat("0.00000000");
            holder.senseCreditsTextView.setText("CSC" + " " + formatter.format(senseCredits));
        }else if (senseCredits == 0){
            holder.senseCreditsTextView.setText("CSC 0.00000000");
        }else {
            DecimalFormat formatter = new DecimalFormat("0.00000000");
            holder.senseCreditsTextView.setText("CSC" + " " + formatter.format(senseCredits));
        }

        //path to cingle wallet reference
        holder.likesCountTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, LikesActivity.class);
                intent.putExtra(TopPostsAdapter.EXTRA_POST_KEY, postKey);
                mContext.startActivity(intent);
            }
        });

        holder.commentsImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent =  new Intent(mContext, CommentsActivity.class);
                intent.putExtra(TopPostsAdapter.EXTRA_POST_KEY, postKey);
                mContext.startActivity(intent);
            }
        });

        holder.postImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, FullImageViewActivity.class);
                intent.putExtra(TopPostsAdapter.EXTRA_POST_KEY, postKey);
                mContext.startActivity(intent);
            }
        });

        holder.tradeMethodTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent =  new Intent(mContext, PostDetailActivity.class);
                intent.putExtra(TopPostsAdapter.EXTRA_POST_KEY, postKey);
               mContext.startActivity(intent);
            }
        });


        holder.settingsImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putString(TopPostsAdapter.EXTRA_POST_KEY, postKey);
                FragmentManager fragmenManager = ((AppCompatActivity)mContext).getSupportFragmentManager();
                CingleSettingsDialog cingleSettingsDialog = CingleSettingsDialog.newInstance("cingle settings");
                cingleSettingsDialog.setArguments(bundle);
                cingleSettingsDialog.show(fragmenManager, "cingle settings fragment");
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
                            .load(post.getCingleImageUrl())
                            .networkPolicy(NetworkPolicy.OFFLINE)
                            .into(holder.postImageView, new Callback() {
                                @Override
                                public void onSuccess() {
                                    Log.v("Picasso", "Fetched image");
                                }

                                @Override
                                public void onError() {
                                    Picasso.with(mContext)
                                            .load(post.getCingleImageUrl())
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
                                intent.putExtra(TopPostsAdapter.EXTRA_USER_UID, uid);
                                mContext.startActivity(intent);
                                d("profile uid", firebaseAuth.getCurrentUser().getUid());
                            }else {
                                Intent intent = new Intent(mContext, FollowerProfileActivity.class);
                                intent.putExtra(TopPostsAdapter.EXTRA_USER_UID, uid);
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
                    holder.postSalePriceTextView.setText("CSC" + " " + formatter.format(postSale.getSalePrice()));
                    holder.tradeMethodTextView.setText("@CingleSelling");
                }else {
                    holder.postSalePriceTitleRelativeLayout.setVisibility(View.GONE);
                    holder.tradeMethodTextView.setText("@NotOnTrade");

                }

            }
        });

        ownerReference.document("Ownership").collection(postKey)
                .document("Owner").addSnapshotListener(new EventListener<DocumentSnapshot>() {
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
                                intent.putExtra(TopPostsAdapter.EXTRA_USER_UID, ownerUid);
                                mContext.startActivity(intent);
                                d("profile uid", firebaseAuth.getCurrentUser().getUid());
                            }else {
                                Intent intent = new Intent(mContext, FollowerProfileActivity.class);
                                intent.putExtra(TopPostsAdapter.EXTRA_USER_UID, ownerUid);
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
        commentsCountQuery.whereEqualTo("pushId", postKey)
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


        likesRef.child(postKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                holder.likesCountTextView.setText(dataSnapshot.getChildrenCount() +" " + "Likes");

                if (dataSnapshot.hasChild(firebaseAuth.getCurrentUser().getUid())){
                    holder.likesImageView.setColorFilter(Color.RED);
                }else {
                    holder.likesImageView.setColorFilter(Color.BLACK);
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        //retrieve the first users who liked
        likesRef.child(postKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    Log.d("likes count", dataSnapshot.getChildrenCount() + "");
                    if (dataSnapshot.getChildrenCount()>0){
                        holder.likesRecyclerView.setVisibility(View.VISIBLE);
                        //SETUP USERS WHO LIKED THE CINGLE
                        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Like, WhoLikedViewHolder>
                                (Like.class, R.layout.who_liked_count, WhoLikedViewHolder.class, likesQuery) {
                            @Override
                            public int getItemCount() {
                                return super.getItemCount();

                            }

                            @Override
                            public long getItemId(int position) {
                                return super.getItemId(position);
                            }

                            @Override
                            protected void populateViewHolder(final WhoLikedViewHolder viewHolder, final Like model, final int position) {
                                DatabaseReference userRef = getRef(position);
                                final String likesPostKey = userRef.getKey();
                                Log.d(TAG, "likes post key" + likesPostKey);

                                likesRef.child(postKey).child(likesPostKey).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.child("uid").exists()){
                                            final String uid = (String) dataSnapshot.child("uid").getValue();
                                            Log.d(TAG, "uid in likes post" + uid);

                                            usersReference.document(uid).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                                @Override
                                                public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                                                    if (documentSnapshot.exists()) {
                                                        Cinggulan cinggulan = documentSnapshot.toObject(Cinggulan.class);
                                                        final String profileImage = cinggulan.getProfileImage();

                                                        Picasso.with(mContext)
                                                                .load(profileImage)
                                                                .resize(MAX_WIDTH, MAX_HEIGHT)
                                                                .onlyScaleDown()
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
                                                                                .resize(MAX_WIDTH, MAX_HEIGHT)
                                                                                .onlyScaleDown()
                                                                                .centerCrop()
                                                                                .placeholder(R.drawable.profle_image_background)
                                                                                .into(viewHolder.whoLikedImageView);


                                                                    }
                                                                });
                                                    }
                                                }
                                            });
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });

                            }
                        };

                        holder.likesRecyclerView.setAdapter(firebaseRecyclerAdapter);
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

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        holder.likesImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                processLikes = true;
                likesRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(final DataSnapshot dataSnapshot) {
                        if(processLikes){
                            if(dataSnapshot.child(postKey).hasChild(firebaseAuth.getCurrentUser().getUid())){
                                likesRef.child(postKey).child(firebaseAuth.getCurrentUser()
                                        .getUid())
                                        .removeValue();
                                onLikeCounter(false);
                                processLikes = false;
                                holder.likesImageView.setColorFilter(Color.BLACK);

                            }else {
                                Like like = new Like();
                                like.setUid(firebaseAuth.getCurrentUser().getUid());
                                likesRef.child(postKey).child(firebaseAuth.getCurrentUser().getUid())
                                        .setValue(like);
                                processLikes = false;
                                onLikeCounter(false);
                                holder.likesImageView.setColorFilter(Color.RED);
                            }
                        }


                        String likesCount = dataSnapshot.child(postKey).getChildrenCount() + "";
                        d(likesCount, "all the likes in one cingle");
                        //convert children count which is a string to integer
                        final int x = Integer.parseInt(likesCount);

                        if (x > 0){
                            //mille is a thousand likes
                            double MILLE = 1000.0;
                            //get the number of likes per a thousand likes
                            double likesPerMille = x/MILLE;
                            //get the default rate of likes per unit time in seconds;
                            double rateOfLike = 1000.0/1800.0;
                            //get the current rate of likes per unit time in seconds;
                            double currentRateOfLkes = x * rateOfLike/MILLE;
                            //get the current price of cingle
                            final double currentPrice = currentRateOfLkes * DEFAULT_PRICE/rateOfLike;
                            //get the perfection value of cingle's interactivity online
                            double perfectionValue = GOLDEN_RATIO/x;
                            //get the new worth of Post price in Sen
                            final double cingleWorth = perfectionValue * likesPerMille * currentPrice;
                            //round of the worth of the cingle to 10 decimal number
                            final double finalPoints = round( cingleWorth, 10);

                            d("final points", finalPoints + "");

                            cingleWalletRef.child(postKey).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        final Balance balance = dataSnapshot.getValue(Balance.class);
                                        final double amountRedeemed = balance.getAmountRedeemed();
                                        d(amountRedeemed + "", "amount redeemed");
                                        final  double amountDeposited = balance.getAmountDeposited();
                                        d(amountDeposited + "", "amount deposited");
                                        final double senseCredits = amountDeposited + finalPoints;
                                        d("sense credit", senseCredits + "");
                                        final double totalSenseCredits = senseCredits - amountRedeemed;
                                        d("total sense credit", totalSenseCredits + "");

                                        Credit credit = new Credit();
                                        credit.setPushId(postKey);
                                        credit.setAmount(totalSenseCredits);
                                        credit.setUid(firebaseAuth.getCurrentUser().getUid());
                                        senseCreditReference.document(postKey).set(credit, SetOptions.merge());

                                    }else {
                                        Credit credit = new Credit();
                                        credit.setPushId(postKey);
                                        credit.setAmount(finalPoints);
                                        credit.setUid(firebaseAuth.getCurrentUser().getUid());
                                        senseCreditReference.document(postKey).set(credit, SetOptions.merge());

                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }
                        else{
                            final double finalPoints = 0.00;
                            d("final points", finalPoints + "");
                            cingleWalletRef.child(postKey).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        final Balance balance = dataSnapshot.getValue(Balance.class);
                                        final double amountRedeemed = balance.getAmountRedeemed();
                                        d(amountRedeemed + "", "amount redeemed");
                                        final  double amountDeposited = balance.getAmountDeposited();
                                        d(amountDeposited + "", "amount deposited");
                                        final double senseCredits = amountDeposited + finalPoints;
                                        d("sense credit", senseCredits + "");
                                        final double totalSenseCredits = senseCredits - amountRedeemed;
                                        d("total sense credit", totalSenseCredits + "");

                                        Credit credit = new Credit();
                                        credit.setPushId(postKey);
                                        credit.setAmount(totalSenseCredits);
                                        credit.setUid(firebaseAuth.getCurrentUser().getUid());
                                        senseCreditReference.document(postKey).set(credit, SetOptions.merge());

                                    }else {
                                        Credit credit = new Credit();
                                        credit.setPushId(postKey);
                                        credit.setAmount(finalPoints);
                                        credit.setUid(firebaseAuth.getCurrentUser().getUid());
                                        senseCreditReference.document(postKey).set(credit, SetOptions.merge());

                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });


    }

    private void onLikeCounter(final boolean increament){
        likesRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                if(mutableData.getValue() != null){
                    int value = mutableData.getValue(Integer.class);
                    if(increament){
                        value++;
                    }else{
                        value--;
                    }
                    mutableData.setValue(value);
                }
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b,
                                   DataSnapshot dataSnapshot) {
                Log.d(TAG, "likeTransaction:onComplete" + databaseError);

            }
        });
    }

    //region listeners
    private static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }


}

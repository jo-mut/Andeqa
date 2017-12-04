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
import com.cinggl.cinggl.viewholders.ProfilePostsViewHolder;
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

/**
 * Created by J.EL on 11/14/2017.
 */

public class ProfilePostsAdapter extends RecyclerView.Adapter<ProfilePostsViewHolder> {
    private static final String TAG = ProfilePostsAdapter.class.getSimpleName();
    private Context mContext;
    private List<Post> posts = new ArrayList<>();
    //firestore
    private FirebaseFirestore firebaseFirestore;
    private CollectionReference cinglesReference;
    private com.google.firebase.firestore.Query profileCinglesQuery;
    private com.google.firebase.firestore.Query commentsCountQuery;
    private CollectionReference ownerReference;
    private CollectionReference usersReference;
    private CollectionReference commentsReference;
    private CollectionReference ifairReference;
    private CollectionReference senseCreditReference;
    private CollectionReference sellingReference;
    private CollectionReference likesReference;
    private CollectionReference relationsReference;
    //firebase adapter
    private FirebaseRecyclerAdapter firebaseRecyclerAdapter;
    //firebase auth
    private FirebaseAuth firebaseAuth;
    //firebase
    private DatabaseReference likesRef;
    private DatabaseReference cingleWalletRef;
    private com.google.firebase.database.Query likesQuery;

    private boolean processLikes = false;

    private static final double GOLDEN_RATIO = 1.618;
    private static final double DEFAULT_PRICE = 1.5;
    private static final String EXTRA_POST_KEY = "post key";
    private static final String EXTRA_USER_UID = "uid";
    private  static final int MAX_WIDTH = 200;
    private static final int MAX_HEIGHT = 200;

    public ProfilePostsAdapter(Context mContext) {
        this.mContext = mContext;
    }

    public void setPosts(List<Post> posts) {
        this.posts = posts;
        notifyDataSetChanged();
    }


    public void removeAt(int position){
        posts.remove(posts.get(position));
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    @Override
    public ProfilePostsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_out_list, parent, false);
        return new ProfilePostsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ProfilePostsViewHolder holder, int position) {
        final Post post = posts.get(position);
        holder.bindProfileCingle(post);
        final String postKey = posts.get(position).getPushId();
        final String uid = posts.get(position).getUid();
        Log.d("post postkey", postKey);

        //initialize firebase auth
        firebaseAuth = FirebaseAuth.getInstance();
        //initialize firestore
        cinglesReference = FirebaseFirestore.getInstance().collection(Constants.POSTS);
        ownerReference = FirebaseFirestore.getInstance().collection(Constants.CINGLE_ONWERS);
        usersReference = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);
        ifairReference = FirebaseFirestore.getInstance().collection(Constants.IFAIR);
        commentsReference = FirebaseFirestore.getInstance().collection(Constants.COMMENTS);
        likesReference = FirebaseFirestore.getInstance().collection(Constants.LIKES);
        relationsReference = FirebaseFirestore.getInstance().collection(Constants.RELATIONS);
        commentsCountQuery = commentsReference;
        profileCinglesQuery = cinglesReference.whereEqualTo("uid", firebaseAuth.getCurrentUser().getUid());
        senseCreditReference = FirebaseFirestore.getInstance().collection(Constants.SENSECREDITS);
        sellingReference = FirebaseFirestore.getInstance().collection(Constants.IFAIR);
        //initialize firebase
        likesRef = FirebaseDatabase.getInstance().getReference(Constants.LIKES);
        cingleWalletRef = FirebaseDatabase.getInstance().getReference(Constants.CINGLE_WALLET);


        likesQuery = likesRef.child(postKey).limitToFirst(5);


        holder.likesCountTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, LikesActivity.class);
                intent.putExtra(ProfilePostsAdapter.EXTRA_POST_KEY, postKey);
                mContext.startActivity(intent);
            }
        });

        holder.commentsImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, CommentsActivity.class);
                intent.putExtra(ProfilePostsAdapter.EXTRA_POST_KEY, postKey);
                mContext.startActivity(intent);
            }
        });

        holder.cingleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, FullImageViewActivity.class);
                intent.putExtra(ProfilePostsAdapter.EXTRA_POST_KEY, postKey);
                mContext.startActivity(intent);
            }
        });

        holder.cingleTradeMethodTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, PostDetailActivity.class);
                intent.putExtra(ProfilePostsAdapter.EXTRA_POST_KEY, postKey);
                mContext.startActivity(intent);
            }
        });

        holder.cingleSettingsImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putString(ProfilePostsAdapter.EXTRA_POST_KEY, postKey);
                FragmentManager fragmenManager = ((AppCompatActivity)mContext).getSupportFragmentManager();
                CingleSettingsDialog cingleSettingsDialog = CingleSettingsDialog.newInstance("post settings");
                cingleSettingsDialog.setArguments(bundle);
                cingleSettingsDialog.show(fragmenManager, "post settings fragment");
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
                    holder.cingleSenseCreditsTextView.setText("CSC" + " " + formatter.format(senseCredits));

                }else {
                    holder.cingleSenseCreditsTextView.setText("CSC 0.00000000");
                }
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

                    Picasso.with(mContext)
                            .load(post.getCingleImageUrl())
                            .networkPolicy(NetworkPolicy.OFFLINE)
                            .into(holder.cingleImageView, new Callback() {
                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onError() {
                                    Picasso.with(mContext)
                                            .load(post.getCingleImageUrl())
                                            .into(holder.cingleImageView, new Callback() {
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
                        holder.cingleTitleRelativeLayout.setVisibility(View.GONE);
                    }else {
                        holder.cingleTitleTextView.setText(post.getTitle());
                    }

                    if (post.getDescription().equals("")){
                        holder.descriptionRelativeLayout.setVisibility(View.GONE);
                    }else {
                        holder.cingleDescriptionTextView.setText(post.getDescription());
                    }

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
                    final Cinggulan cinggulan = documentSnapshot.toObject(Cinggulan.class);
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
                    Log.d("owner uid", ownerUid);

                    if (firebaseAuth.getCurrentUser().getUid().equals(ownerUid)){
                        holder.cingleSettingsImageView.setVisibility(View.VISIBLE);
                    }else {
                        holder.cingleSettingsImageView.setVisibility(View.INVISIBLE);
                    }
                }
            }
        });



        //get the number of commments in a post
        commentsCountQuery.whereEqualTo("pushId", postKey).addSnapshotListener(new EventListener<QuerySnapshot>() {
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



        sellingReference.document(postKey).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (documentSnapshot.exists()){
                    final PostSale postSale = documentSnapshot.toObject(PostSale.class);
                    holder.cingleTradeMethodTextView.setText("@CingleSelling");
                }else {
                    holder.cingleTradeMethodTextView.setText("@NotOnTrade");

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
                                            Log.d(TAG, "uid in likes post" + uid);
                                            final String uid = (String) dataSnapshot.child("uid").getValue();

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
                        Log.d(likesCount, "all the likes in one post");
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
                            //get the current price of post
                            final double currentPrice = currentRateOfLkes * DEFAULT_PRICE/rateOfLike;
                            //get the perfection value of post's interactivity online
                            double perfectionValue = GOLDEN_RATIO/x;
                            //get the new worth of Post price in Sen
                            final double cingleWorth = perfectionValue * likesPerMille * currentPrice;
                            //round of the worth of the post to 10 decimal number
                            final double finalPoints = round( cingleWorth, 10);

                            Log.d("final points", finalPoints + "");

                            cingleWalletRef.child(postKey).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        final Balance balance = dataSnapshot.getValue(Balance.class);
                                        final double amountRedeemed = balance.getAmountRedeemed();
                                        Log.d(amountRedeemed + "", "amount redeemed");
                                        final  double amountDeposited = balance.getAmountDeposited();
                                        Log.d(amountDeposited + "", "amount deposited");
                                        final double senseCredits = amountDeposited + finalPoints;
                                        Log.d("sense credit", senseCredits + "");
                                        final double totalSenseCredits = senseCredits - amountRedeemed;
                                        Log.d("total sense credit", totalSenseCredits + "");

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
                            Log.d("final points", finalPoints + "");
                            cingleWalletRef.child(postKey).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        final Balance balance = dataSnapshot.getValue(Balance.class);
                                        final double amountRedeemed = balance.getAmountRedeemed();
                                        Log.d(amountRedeemed + "", "amount redeemed");
                                        final  double amountDeposited = balance.getAmountDeposited();
                                        Log.d(amountDeposited + "", "amount deposited");
                                        final double senseCredits = amountDeposited + finalPoints;
                                        Log.d("sense credit", senseCredits + "");
                                        final double totalSenseCredits = senseCredits - amountRedeemed;
                                        Log.d("total sense credit", totalSenseCredits + "");

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



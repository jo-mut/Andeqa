package com.cinggl.cinggl.people;

import android.content.Intent;
import android.graphics.Color;
import android.os.Parcelable;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.cinggl.cinggl.Constants;
import com.cinggl.cinggl.R;
import com.cinggl.cinggl.home.CingleDetailActivity;
import com.cinggl.cinggl.home.CingleSettingsDialog;
import com.cinggl.cinggl.home.CommentsActivity;
import com.cinggl.cinggl.home.FullImageViewActivity;
import com.cinggl.cinggl.home.LikesActivity;
import com.cinggl.cinggl.models.Balance;
import com.cinggl.cinggl.models.Cingle;
import com.cinggl.cinggl.models.Cingulan;
import com.cinggl.cinggl.models.Like;
import com.cinggl.cinggl.models.TransactionDetails;
import com.cinggl.cinggl.profile.PersonalProfileActivity;
import com.cinggl.cinggl.viewholders.ProfileCinglesViewHolder;
import com.cinggl.cinggl.viewholders.WhoLikedViewHolder;
import com.firebase.ui.common.ChangeEventType;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.firebase.ui.firestore.ObservableSnapshotArray;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

import static com.cinggl.cinggl.home.CingleDetailActivity.round;

public class FollowerProfileActivity extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG = FollowerProfileActivity.class.getSimpleName();

    @Bind(R.id.profileCinglesRecyclerView)RecyclerView mProfileCinglesRecyclerView;
    @Bind(R.id.creatorImageView)CircleImageView mProifleImageView;
    @Bind(R.id.firstNameTextView)TextView mFirstNameTextView;
    @Bind(R.id.secondNameTextView)TextView  mSecondNameTextView;
    @Bind(R.id.bioTextView)TextView mBioTextView;
    @Bind(R.id.followersCountTextView) TextView mFollowersCountTextView;
    @Bind(R.id.followingCountTextView)TextView mFollowingCountTextView;
    @Bind(R.id.cinglesCountTextView)TextView mCinglesCountTextView;
    @Bind(R.id.header_cover_image)ImageView mProfileCover;
    @Bind(R.id.followButton)Button mFollowButton;

    //firestore
    private CollectionReference relationsReference;
    private CollectionReference cinglesReference;
    private CollectionReference commentsReference;
    private CollectionReference usersReference;
    private CollectionReference ownerReference;
    private CollectionReference ifairReference;
    private com.google.firebase.firestore.Query profileCinglesQuery;
    private com.google.firebase.firestore.Query commentsCountQuery;
    //firebase
    private DatabaseReference likesRef;
    private DatabaseReference cingleWalletRef;
    private Query likesQuery;
    private DatabaseReference relationsRef;
    //firebase auth
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    //firestore adapter
    private FirestoreRecyclerAdapter firestoreRecyclerAdapter;
    private FirebaseRecyclerAdapter firebaseRecyclerAdapter;

    private boolean processLikes = false;
    private  static final int MAX_WIDTH = 200;
    private static final int MAX_HEIGHT = 200;
    private static final double GOLDEN_RATIO = 1.618;
    private static final double DEFAULT_PRICE = 1.5;
    private static final String EXTRA_POST_KEY = "post key";
    private static final String EXTRA_USER_UID = "uid";
    private int mProfileCinglesRecyclerViewPosition = 0;
    private Query cinglesQuery;

    private LinearLayoutManager layoutManager;
    private String mUid;
    private boolean processFollow = false;

    private List<Cingle> cingles = new ArrayList<>();
    private List<String> cinglesIds = new ArrayList<>();

    private int currentPage = 0;
    private static final int TOTAL_ITEM_EACH_LOAD = 10;
    private static final String KEY_LAYOUT_POSITION = "layout pooition";
    private Parcelable recyclerViewState;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follower_profile);
        ButterKnife.bind(this);

        //FIREBASE AUTH
        firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser()!= null){
            mUid = getIntent().getStringExtra(EXTRA_USER_UID);
            if(mUid == null){
                throw new IllegalArgumentException("pass an EXTRA_USER_UID");
            }

            //firebase
            likesRef = FirebaseDatabase.getInstance().getReference(Constants.LIKES);
            cingleWalletRef = FirebaseDatabase.getInstance().getReference(Constants.CINGLE_WALLET);
            likesQuery = likesRef.limitToFirst(5);
            relationsRef = FirebaseDatabase.getInstance().getReference(Constants.RELATIONS);
            //firestore
            cinglesReference = FirebaseFirestore.getInstance().collection(Constants.POSTS);
            relationsReference = FirebaseFirestore.getInstance().collection(Constants.RELATIONS);
            usersReference = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);
            commentsReference = FirebaseFirestore.getInstance().collection(Constants.COMMENTS);
            profileCinglesQuery = cinglesReference.whereEqualTo("uid", mUid);
            ownerReference = FirebaseFirestore.getInstance().collection(Constants.CINGLE_ONWERS);
            ifairReference = FirebaseFirestore.getInstance().collection(Constants.IFAIR);
            commentsCountQuery = commentsReference;

            fetchData();
            setProfileCingles();

        }

        //INITIALIZE CLICK LISTENERS
        mFollowersCountTextView.setOnClickListener(this);
        mFollowingCountTextView.setOnClickListener(this);
        mFollowersCountTextView.setOnClickListener(this);
        mFollowingCountTextView.setOnClickListener(this);
        mFollowButton.setOnClickListener(this);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }


    private void fetchData(){

        //retrieve the count of followers for this user
        relationsRef.child("followers").child(mUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Log.e(snapshot.getKey(), snapshot.getChildrenCount() + "followers Count");
                }

                //SET TEXT ON BUTTON FOLLOWING IF FOLLOWING
                if (dataSnapshot.hasChild(firebaseAuth.getCurrentUser().getUid())){
                    mFollowButton.setText("FOLLOWING");
                }else {
                    mFollowButton.setText("FOLLOW");
                }

                //SET FOLLOWERS COUNT IF ANY
                if (dataSnapshot.hasChildren()){
                    mFollowersCountTextView.setText(dataSnapshot.getChildrenCount() + "");
                }else {
                    mFollowersCountTextView.setText("0");
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        //retrieve the count of users followed by this user
        relationsRef.child("following").child(mUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.hasChildren()){
                            mFollowingCountTextView.setText(dataSnapshot.getChildrenCount() + "");
                        }else {
                            mFollowingCountTextView.setText("0");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


        usersReference.document(mUid)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                        if (e != null) {
                            Log.w(TAG, "Listen error", e);
                            return;
                        }

                        if (documentSnapshot.exists()){
                            final Cingulan cingulan = documentSnapshot.toObject(Cingulan.class);

                            String firstName = cingulan.getFirstName();
                            String secondName = cingulan.getSecondName();
                            final String profileImage = cingulan.getProfileImage();
                            String bio = cingulan.getBio();
                            final String profileCover = cingulan.getProfileCover();

                            mFirstNameTextView.setText(firstName);
                            mSecondNameTextView.setText(secondName);
                            mBioTextView.setText(bio);

                            Picasso.with(FollowerProfileActivity.this)
                                    .load(profileImage)
                                    .resize(MAX_WIDTH, MAX_HEIGHT)
                                    .onlyScaleDown()
                                    .centerCrop()
                                    .placeholder(R.drawable.profle_image_background)
                                    .networkPolicy(NetworkPolicy.OFFLINE)
                                    .into(mProifleImageView, new Callback() {
                                        @Override
                                        public void onSuccess() {

                                        }

                                        @Override
                                        public void onError() {
                                            Picasso.with(FollowerProfileActivity.this)
                                                    .load(profileImage)
                                                    .resize(MAX_WIDTH, MAX_HEIGHT)
                                                    .onlyScaleDown()
                                                    .centerCrop()
                                                    .placeholder(R.drawable.profle_image_background)
                                                    .into(mProifleImageView);

                                        }
                                    });

                            Picasso.with(FollowerProfileActivity.this)
                                    .load(profileCover)
                                    .fit()
                                    .centerCrop()
                                    .networkPolicy(NetworkPolicy.OFFLINE)
                                    .into(mProfileCover, new Callback() {
                                        @Override
                                        public void onSuccess() {

                                        }

                                        @Override
                                        public void onError() {
                                            Picasso.with(FollowerProfileActivity.this)
                                                    .load(profileCover)
                                                    .fit()
                                                    .centerCrop()
                                                    .into(mProfileCover);


                                        }
                                    });

                        }

                    }
                });

    }

    private void setProfileCingles(){
        FirestoreRecyclerOptions<Cingle> options = new FirestoreRecyclerOptions.Builder<Cingle>()
                .setQuery(profileCinglesQuery, Cingle.class)
                .build();

        firestoreRecyclerAdapter = new FirestoreRecyclerAdapter<Cingle, ProfileCinglesViewHolder>(options) {
            @Override
            protected void onBindViewHolder(final ProfileCinglesViewHolder holder, int position, Cingle model) {
//                holder.bindProfileCingle(model);
//                final String postKey = getSnapshots().get(position).getPushId();
//                final String uid = getSnapshots().get(position).getUid();
//
//                //init document references
//                ownerReference.document(postKey);
//                cinglesReference.document(postKey);
//                likesQuery = likesRef.child(postKey).limitToFirst(5);
//
//                //path to ownerRef
//                DocumentReference ownerRef = ownerReference.document("owner");
//                //path to sensCredits
//                final DocumentReference creditsRef = cinglesReference.document("sensepoint");
//                //path to cingle wallet reference
//                holder.likesCountTextView.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        Intent intent = new Intent(FollowerProfileActivity.this, LikesActivity.class);
//                        intent.putExtra(FollowerProfileActivity.EXTRA_POST_KEY, postKey);
//                        startActivity(intent);
//                    }
//                });
//
//                holder.commentsImageView.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        Intent intent =  new Intent(FollowerProfileActivity.this, CommentsActivity.class);
//                        intent.putExtra(FollowerProfileActivity.EXTRA_POST_KEY, postKey);
//                        startActivity(intent);
//                    }
//                });
//
//                holder.cingleImageView.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        Intent intent = new Intent(FollowerProfileActivity.this, FullImageViewActivity.class);
//                        intent.putExtra(FollowerProfileActivity.EXTRA_POST_KEY, postKey);
//                        startActivity(intent);
//                    }
//                });
//
//                holder.cingleTradeMethodTextView.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        Intent intent =  new Intent(FollowerProfileActivity.this, CingleDetailActivity.class);
//                        intent.putExtra(FollowerProfileActivity.EXTRA_POST_KEY, postKey);
//                        startActivity(intent);
//                    }
//                });
//
//                holder.cingleSettingsImageView.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        Bundle bundle = new Bundle();
//                        bundle.putString(FollowerProfileActivity.EXTRA_POST_KEY, postKey);
//                        FragmentManager fragmenManager = getSupportFragmentManager();
//                        CingleSettingsDialog cingleSettingsDialog = CingleSettingsDialog.newInstance("cingle settings");
//                        cingleSettingsDialog.setArguments(bundle);
//                        cingleSettingsDialog.show(fragmenManager, "cingle settings fragment");
//                    }
//                });
//
//                holder.profileImageView.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        if ((firebaseAuth.getCurrentUser().getUid()).equals(uid)){
//                            Intent intent = new Intent(FollowerProfileActivity.this, PersonalProfileActivity.class);
//                            intent.putExtra(FollowerProfileActivity.EXTRA_USER_UID, uid);
//                            startActivity(intent);
//                            Log.d("profile uid", firebaseAuth.getCurrentUser().getUid());
//                        }else {
//                            Intent intent = new Intent(FollowerProfileActivity.this, FollowerProfileActivity.class);
//                            intent.putExtra(FollowerProfileActivity.EXTRA_USER_UID, uid);
//                            Log.d("follower uid", uid);
//                            startActivity(intent);
//                        }
//                    }
//                });
//
//                ownerRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
//                    @Override
//                    public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
//
//                        if (e != null) {
//                            Log.w(TAG, "Listen error", e);
//                            return;
//                        }
//
//                        if (documentSnapshot.exists()){
//                            TransactionDetails transactionDetails = documentSnapshot.toObject(TransactionDetails.class);
//                            final String ownerUid = transactionDetails.getUid();
//                            Log.d("owner uid", ownerUid);
//
//                            if (firebaseAuth.getCurrentUser().getUid().equals(ownerUid)){
//                                holder.cingleSettingsImageView.setVisibility(View.VISIBLE);
//                            }else {
//                                holder.cingleSettingsImageView.setVisibility(View.INVISIBLE);
//                            }
//                        }
//
//
//                    }
//                });
//
//
//                //get the number of commments in a cingle
//                commentsCountQuery.whereEqualTo("uid", mUid).addSnapshotListener(new EventListener<QuerySnapshot>() {
//                    @Override
//                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
//                        if (!documentSnapshots.isEmpty()){
//                            final int likesCount = documentSnapshots.size();
//                            holder.commentsCountTextView.setText(likesCount + "");
//
//                        }
//                    }
//                });
//
//
//                ifairReference.addSnapshotListener(new EventListener<QuerySnapshot>() {
//                    @Override
//                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
//                        if (!documentSnapshots.isEmpty()){
//                            ifairReference.document("Cingle Selling").addSnapshotListener(new EventListener<DocumentSnapshot>() {
//                                @Override
//                                public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
//                                    if (documentSnapshot.contains(postKey)){
//                                        holder.cingleTradeMethodTextView.setText("@Cingle Selling");
//                                    }
//
//                                }
//                            });
//                        }
//                    }
//                });
//
//                likesRef.child(postKey).addValueEventListener(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(DataSnapshot dataSnapshot) {
//                        holder.likesCountTextView.setText(dataSnapshot.getChildrenCount() +" " + "Likes");
//
//                        if (dataSnapshot.hasChild(firebaseAuth.getCurrentUser().getUid())){
//                            holder.likesImageView.setColorFilter(Color.RED);
//                        }else {
//                            holder.likesImageView.setColorFilter(Color.BLACK);
//                        }
//                    }
//
//                    @Override
//                    public void onCancelled(DatabaseError databaseError) {
//
//                    }
//                });
//
//
//                likesRef.child(postKey).addValueEventListener(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(DataSnapshot dataSnapshot) {
//                        if (dataSnapshot.exists()){
//                            Log.d("likes count", dataSnapshot.getChildrenCount() + "");
//                            if (dataSnapshot.getChildrenCount()>0){
//                                holder.likesRecyclerView.setVisibility(View.VISIBLE);
//                                //SETUP USERS WHO LIKED THE CINGLE
//                                firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Like, WhoLikedViewHolder>
//                                        (Like.class, R.layout.who_liked_count, WhoLikedViewHolder.class, likesQuery) {
//                                    @Override
//                                    public int getItemCount() {
//                                        return super.getItemCount();
//
//                                    }
//
//                                    @Override
//                                    public long getItemId(int position) {
//                                        return super.getItemId(position);
//                                    }
//
//                                    @Override
//                                    protected void populateViewHolder(final WhoLikedViewHolder viewHolder, final Like model, final int position) {
//                                        DatabaseReference userRef = getRef(position);
//                                        final String likesPostKey = userRef.getKey();
//                                        Log.d(TAG, "likes post key" + likesPostKey);
//
//                                        likesRef.child(postKey).child(likesPostKey).addValueEventListener(new ValueEventListener() {
//                                            @Override
//                                            public void onDataChange(DataSnapshot dataSnapshot) {
//                                                if (dataSnapshot.child("uid").exists()){
//                                                    Log.d(TAG, "uid in likes post" + uid);
//                                                    final String uid = (String) dataSnapshot.child("uid").getValue();
//
//                                                    usersReference.document(uid).addSnapshotListener(new EventListener<DocumentSnapshot>() {
//                                                        @Override
//                                                        public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
//                                                            if (documentSnapshot.exists()) {
//                                                                Cingulan cingulan = documentSnapshot.toObject(Cingulan.class);
//                                                                final String profileImage = cingulan.getProfileImage();
//
//                                                                Picasso.with(FollowerProfileActivity.this)
//                                                                        .load(profileImage)
//                                                                        .resize(MAX_WIDTH, MAX_HEIGHT)
//                                                                        .onlyScaleDown()
//                                                                        .centerCrop()
//                                                                        .placeholder(R.drawable.profle_image_background)
//                                                                        .networkPolicy(NetworkPolicy.OFFLINE)
//                                                                        .into(viewHolder.whoLikedImageView, new Callback() {
//                                                                            @Override
//                                                                            public void onSuccess() {
//
//                                                                            }
//
//                                                                            @Override
//                                                                            public void onError() {
//                                                                                Picasso.with(FollowerProfileActivity.this)
//                                                                                        .load(profileImage)
//                                                                                        .resize(MAX_WIDTH, MAX_HEIGHT)
//                                                                                        .onlyScaleDown()
//                                                                                        .centerCrop()
//                                                                                        .placeholder(R.drawable.profle_image_background)
//                                                                                        .into(viewHolder.whoLikedImageView);
//
//
//                                                                            }
//                                                                        });
//                                                            }
//                                                        }
//                                                    });
//                                                }
//                                            }
//
//                                            @Override
//                                            public void onCancelled(DatabaseError databaseError) {
//
//                                            }
//                                        });
//
//                                    }
//                                };
//
//                                holder.likesRecyclerView.setAdapter(firebaseRecyclerAdapter);
//                                holder.likesRecyclerView.setHasFixedSize(false);
//                                RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(FollowerProfileActivity.this,
//                                        LinearLayoutManager.HORIZONTAL, true);
//                                layoutManager.setAutoMeasureEnabled(true);
//                                holder.likesRecyclerView.setNestedScrollingEnabled(false);
//                                holder.likesRecyclerView.setLayoutManager(layoutManager);
//
//                            }else {
//                                holder.likesRecyclerView.setVisibility(View.GONE);
//                            }
//
//                        }
//                    }
//
//                    @Override
//                    public void onCancelled(DatabaseError databaseError) {
//
//                    }
//                });
//
//
//                holder.likesImageView.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        processLikes = true;
//                        likesRef.addValueEventListener(new ValueEventListener() {
//                            @Override
//                            public void onDataChange(final DataSnapshot dataSnapshot) {
//                                if(processLikes){
//                                    if(dataSnapshot.child(postKey).hasChild(firebaseAuth.getCurrentUser().getUid())){
//                                        likesRef.child(postKey).child(firebaseAuth.getCurrentUser()
//                                                .getUid())
//                                                .removeValue();
//                                        onLikeCounter(false);
//                                        processLikes = false;
//                                        holder.likesImageView.setColorFilter(Color.BLACK);
//
//                                    }else {
//                                        Like like = new Like();
//                                        like.setUid(firebaseAuth.getCurrentUser().getUid());
//                                        likesRef.child(postKey).child(firebaseAuth.getCurrentUser().getUid())
//                                                .child(firebaseAuth.getCurrentUser().getUid()).setValue(like);
//                                        processLikes = false;
//                                        onLikeCounter(false);
//                                        holder.likesImageView.setColorFilter(Color.RED);
//                                    }
//                                }
//
//
//                                String likesCount = dataSnapshot.child(postKey).getChildrenCount() + "";
//                                Log.d(likesCount, "all the likes in one cingle");
//                                //convert children count which is a string to integer
//                                final int x = Integer.parseInt(likesCount);
//
//                                if (x > 0){
//                                    //mille is a thousand likes
//                                    double MILLE = 1000.0;
//                                    //get the number of likes per a thousand likes
//                                    double likesPerMille = x/MILLE;
//                                    //get the default rate of likes per unit time in seconds;
//                                    double rateOfLike = 1000.0/1800.0;
//                                    //get the current rate of likes per unit time in seconds;
//                                    double currentRateOfLkes = x * rateOfLike/MILLE;
//                                    //get the current price of cingle
//                                    final double currentPrice = currentRateOfLkes * DEFAULT_PRICE/rateOfLike;
//                                    //get the perfection value of cingle's interactivity online
//                                    double perfectionValue = GOLDEN_RATIO/x;
//                                    //get the new worth of Cingle price in Sen
//                                    final double cingleWorth = perfectionValue * likesPerMille * currentPrice;
//                                    //round of the worth of the cingle to 10 decimal number
//                                    final double finalPoints = round( cingleWorth, 10);
//
//                                    Log.d("final points", finalPoints + "");
//
//                                    cingleWalletRef.child(postKey).addListenerForSingleValueEvent(new ValueEventListener() {
//                                        @Override
//                                        public void onDataChange(DataSnapshot dataSnapshot) {
//                                            if (dataSnapshot.exists()) {
//                                                final Balance balance = dataSnapshot.getValue(Balance.class);
//                                                final double amountRedeemed = balance.getAmountRedeemed();
//                                                Log.d(amountRedeemed + "", "amount redeemed");
//                                                final  double amountDeposited = balance.getAmountDeposited();
//                                                Log.d(amountDeposited + "", "amount deposited");
//                                                final double senseCredits = amountDeposited + finalPoints;
//                                                Log.d("sense credits", senseCredits + "");
//                                                final double totalSenseCredits = senseCredits - amountRedeemed;
//                                                Log.d("total sense credits", totalSenseCredits + "");
//
//                                                Map<String, Cingle> credits = new HashMap<String, Cingle>();
//                                                creditsRef.update("sensepoint", totalSenseCredits);
//
//                                            }else {
//                                                Map<String, Cingle> credits = new HashMap<String, Cingle>();
//                                                creditsRef.update("sensepoint", finalPoints);
//                                            }
//                                        }
//
//                                        @Override
//                                        public void onCancelled(DatabaseError databaseError) {
//
//                                        }
//                                    });
//                                }
//                                else{
//                                    final double finalPoints = 0.00;
//                                    Log.d("final points", finalPoints + "");
//                                    cingleWalletRef.child(postKey).addListenerForSingleValueEvent(new ValueEventListener() {
//                                        @Override
//                                        public void onDataChange(DataSnapshot dataSnapshot) {
//                                            if (dataSnapshot.exists()) {
//                                                final Balance balance = dataSnapshot.getValue(Balance.class);
//                                                final double amountRedeemed = balance.getAmountRedeemed();
//                                                Log.d(amountRedeemed + "", "amount redeemed");
//                                                final  double amountDeposited = balance.getAmountDeposited();
//                                                Log.d(amountDeposited + "", "amount deposited");
//                                                final double senseCredits = amountDeposited + finalPoints;
//                                                Log.d("sense credits", senseCredits + "");
//                                                final double totalSenseCredits = senseCredits - amountRedeemed;
//                                                Log.d("total sense credits", totalSenseCredits + "");
//                                                Map<String, Cingle> credits = new HashMap<String, Cingle>();
//                                                creditsRef.update("sensepoint", totalSenseCredits);
//
//                                            }else {
//                                                Map<String, Cingle> credits = new HashMap<String, Cingle>();
//                                                creditsRef.update("sensepoint", finalPoints);
//                                            }
//                                        }
//
//                                        @Override
//                                        public void onCancelled(DatabaseError databaseError) {
//
//                                        }
//                                    });
//
//                                }
//                            }
//
//                            @Override
//                            public void onCancelled(DatabaseError databaseError) {
//
//                            }
//                        });
//                    }
//                });


            }

            @Override
            public ProfileCinglesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.poeple_list, parent, false);
                return new ProfileCinglesViewHolder(view);
            }

            @Override
            public void onError(FirebaseFirestoreException e) {
                super.onError(e);
            }

            @Override
            public void onDataChanged() {
                super.onDataChanged();
            }

            @Override
            public void onChildChanged(ChangeEventType type, DocumentSnapshot snapshot, int newIndex, int oldIndex) {
                super.onChildChanged(type, snapshot, newIndex, oldIndex);
            }

            @Override
            public int getItemCount() {
                return super.getItemCount();
            }

            @Override
            public Cingle getItem(int position) {
                return super.getItem(position);
            }

            @Override
            public ObservableSnapshotArray<Cingle> getSnapshots() {
                return super.getSnapshots();
            }
        };

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(KEY_LAYOUT_POSITION, layoutManager.onSaveInstanceState());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        recyclerViewState = savedInstanceState.getParcelable(KEY_LAYOUT_POSITION);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (recyclerViewState != null){
            layoutManager.onRestoreInstanceState(recyclerViewState);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        firestoreRecyclerAdapter.stopListening();
    }

    @Override
    protected void onStart() {
        super.onStart();
        firestoreRecyclerAdapter.startListening();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onClick(View v){
        if (v == mFollowingCountTextView) {
            Intent intent = new Intent(this, FollowingActivity.class);
            intent.putExtra(FollowerProfileActivity.EXTRA_USER_UID, mUid);
            startActivity(intent);

        }

        if (v == mFollowersCountTextView){
            Intent intent = new Intent(this, FollowersActivity.class);
            intent.putExtra(FollowerProfileActivity.EXTRA_USER_UID, mUid);
            startActivity(intent);

        }

        if (v == mFollowButton){
            processFollow = true;

            relationsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (processFollow){
                        Log.d("muid", mUid);
                        if (dataSnapshot.child("followers").child(mUid).hasChild(firebaseAuth.getCurrentUser().getUid())){
                            relationsRef.child("followers").child(mUid).child(firebaseAuth.getCurrentUser().getUid())
                                    .removeValue();
                            relationsRef.child("following").child(firebaseAuth.getCurrentUser().getUid()).child(mUid)
                                    .removeValue();
                            processFollow = false;
                            onFollow(false);
                            //set the text on the button to follow if the user in not yet following;
                            mFollowButton.setText("FOLLOW");
                            mFollowersCountTextView.setText(dataSnapshot.getChildrenCount() + "");

                        }else {
                            //set followers of mUid;
                            relationsRef.child("followers").child(mUid).child(firebaseAuth.getCurrentUser().getUid())
                                    .child("uid")
                                    .setValue(firebaseAuth.getCurrentUser().getUid());
                            //set the uid you are following
                            relationsRef.child("following").child(firebaseAuth.getCurrentUser().getUid()).child(mUid)
                                    .child("uid").setValue(mUid);
                            processFollow = false;
                            onFollow(false);
                            //set text on the button following;
                            mFollowButton.setText("FOLLOWING");
                            mFollowersCountTextView.setText(dataSnapshot.getChildrenCount() + "");

                        }

                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }
    }
    private void onFollow(final boolean increament){
        relationsRef.runTransaction(new Transaction.Handler() {
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
                Log.d(TAG, "followTransaction:onComplete" + databaseError);

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


}
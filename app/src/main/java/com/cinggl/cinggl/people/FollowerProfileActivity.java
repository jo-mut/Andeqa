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
import com.cinggl.cinggl.adapters.ProfileCinglesAdapter;
import com.cinggl.cinggl.home.CingleDetailActivity;
import com.cinggl.cinggl.home.CingleSettingsDialog;
import com.cinggl.cinggl.home.CommentsActivity;
import com.cinggl.cinggl.home.FullImageViewActivity;
import com.cinggl.cinggl.home.LikesActivity;
import com.cinggl.cinggl.models.Cingle;
import com.cinggl.cinggl.models.Cingulan;
import com.cinggl.cinggl.models.Like;
import com.cinggl.cinggl.models.TransactionDetails;
import com.cinggl.cinggl.profile.PersonalProfileActivity;
import com.cinggl.cinggl.viewholders.CingleOutViewHolder;
import com.cinggl.cinggl.viewholders.WhoLikedViewHolder;
import com.firebase.ui.common.ChangeEventType;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.firebase.ui.firestore.ObservableSnapshotArray;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.Query;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class FollowerProfileActivity extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG = FollowerProfileActivity.class.getSimpleName();

    @Bind(R.id.profileCinglesRecyclerView)RecyclerView mProfileCinglesRecyclerView;
    @Bind(R.id.profileImageView)CircleImageView mProifleImageView;
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
    private CollectionReference likesReference;
    private CollectionReference cingleWalletReference;
    private CollectionReference ifairReference;
    private com.google.firebase.firestore.Query profileCinglesQuery;
    private com.google.firebase.firestore.Query commentsCountQuery;
    private com.google.firebase.firestore.Query likesCountQuery;
    //adapters
    private FirestoreRecyclerAdapter firestoreRecyclerAdapter;

    private boolean processLikes = false;
    private  static final int MAX_WIDTH = 200;
    private static final int MAX_HEIGHT = 200;
    private static final double GOLDEN_RATIO = 1.618;
    private static final double DEFAULT_PRICE = 1.5;
    private static final String EXTRA_POST_KEY = "post key";
    private static final String EXTRA_USER_UID = "uid";
    private int mProfileCinglesRecyclerViewPosition = 0;
    private Query cinglesQuery;

    private FirebaseAuth firebaseAuth;
    private ProfileCinglesAdapter profileCinglesAdapter;

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

            cinglesReference = FirebaseFirestore.getInstance().collection(Constants.POSTS);
            relationsReference = FirebaseFirestore.getInstance().collection(Constants.RELATIONS);
            usersReference = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);
            commentsReference = FirebaseFirestore.getInstance().collection(Constants.COMMENTS);
            likesReference = FirebaseFirestore.getInstance().collection(Constants.LIKES);
            profileCinglesQuery = cinglesReference.whereEqualTo("uid", mUid);
            ownerReference = FirebaseFirestore.getInstance().collection(Constants.CINGLE_ONWERS);
            likesReference = FirebaseFirestore.getInstance().collection(Constants.LIKES);
            cingleWalletReference = FirebaseFirestore.getInstance().collection(Constants.CINGLE_WALLET);
            ifairReference = FirebaseFirestore.getInstance().collection(Constants.IFAIR);
            commentsCountQuery = commentsReference;
            likesCountQuery = likesReference;

            fetchData();
            initializeViewsAdapter();
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


    private void initializeViewsAdapter(){
        layoutManager =  new LinearLayoutManager(this);
        mProfileCinglesRecyclerView.setLayoutManager(layoutManager);
        mProfileCinglesRecyclerView.setHasFixedSize(true);
        profileCinglesAdapter = new ProfileCinglesAdapter(this);
        mProfileCinglesRecyclerView.setAdapter(profileCinglesAdapter);
        profileCinglesAdapter.notifyDataSetChanged();
    }

    private void fetchData(){
        profileCinglesQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (documentSnapshots.isEmpty()){
                    mCinglesCountTextView.setText("0");
                }else {
                    mCinglesCountTextView.setText(documentSnapshots.size());
                }

            }
        });

        relationsReference.document("followers").collection(mUid).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (documentSnapshots.getDocuments().contains(firebaseAuth.getCurrentUser().getUid())){
                    mFollowButton.setText("FOLLOWING");
                }else {
                    mFollowButton.setText("FOLLOW");
                }

                if (documentSnapshots.isEmpty()){
                    mFollowersCountTextView.setText("0");
                }else {
                    mFollowersCountTextView.setText(documentSnapshots.size());
                }
            }
        });

        relationsReference.document("following").collection(mUid)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (documentSnapshots.isEmpty()){
                    mFollowersCountTextView.setText(documentSnapshots.size());
                }else {
                    mFollowingCountTextView.setText("0");
                }
            }
        });


        usersReference.document(mUid).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (documentSnapshot.exists()){
                    Cingulan cingulan = documentSnapshot.toObject(Cingulan.class);
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

        firestoreRecyclerAdapter = new FirestoreRecyclerAdapter<Cingle, CingleOutViewHolder>(options) {
            @Override
            protected void onBindViewHolder(final CingleOutViewHolder holder, int position, Cingle model) {
                holder.bindCingle(model);
                final String postKey = getSnapshots().get(position).getPushId();
                final String uid = getSnapshots().get(position).getUid();

                //init document references
                ownerReference.document(postKey);
                cinglesReference.document(postKey);
                likesReference.document(postKey);
                //path to ownerRef
                DocumentReference ownerRef = ownerReference.document("owner");
                //path to sensCredits
                final DocumentReference creditsRef = cinglesReference.document("sensepoint");
                //path to cingle wallet reference
                final DocumentReference cingleWR = cingleWalletReference.document(postKey);
                holder.likesCountTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(FollowerProfileActivity.this, LikesActivity.class);
                        intent.putExtra(FollowerProfileActivity.EXTRA_POST_KEY, postKey);
                        startActivity(intent);
                    }
                });

                holder.commentsImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent =  new Intent(FollowerProfileActivity.this, CommentsActivity.class);
                        intent.putExtra(FollowerProfileActivity.EXTRA_POST_KEY, postKey);
                        startActivity(intent);
                    }
                });

                holder.cingleImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(FollowerProfileActivity.this, FullImageViewActivity.class);
                        intent.putExtra(FollowerProfileActivity.EXTRA_POST_KEY, postKey);
                        startActivity(intent);
                    }
                });

                holder.cingleTradeMethodTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent =  new Intent(FollowerProfileActivity.this, CingleDetailActivity.class);
                        intent.putExtra(FollowerProfileActivity.EXTRA_POST_KEY, postKey);
                        startActivity(intent);
                    }
                });

                holder.cingleSettingsImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Bundle bundle = new Bundle();
                        bundle.putString(FollowerProfileActivity.EXTRA_POST_KEY, postKey);
                        FragmentManager fragmenManager = getSupportFragmentManager();
                        CingleSettingsDialog cingleSettingsDialog = CingleSettingsDialog.newInstance("cingle settings");
                        cingleSettingsDialog.setArguments(bundle);
                        cingleSettingsDialog.show(fragmenManager, "cingle settings fragment");
                    }
                });

                holder.profileImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if ((firebaseAuth.getCurrentUser().getUid()).equals(uid)){
                            Intent intent = new Intent(FollowerProfileActivity.this, PersonalProfileActivity.class);
                            intent.putExtra(FollowerProfileActivity.EXTRA_USER_UID, uid);
                            startActivity(intent);
                            Log.d("profile uid", firebaseAuth.getCurrentUser().getUid());
                        }else {
                            Intent intent = new Intent(FollowerProfileActivity.this, FollowerProfileActivity.class);
                            intent.putExtra(FollowerProfileActivity.EXTRA_USER_UID, uid);
                            Log.d("follower uid", uid);
                            startActivity(intent);
                        }
                    }
                });

                ownerRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
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


                //get the number of commments in a cingle
                commentsCountQuery.whereEqualTo("uid", mUid).addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                        if (!documentSnapshots.isEmpty()){
                            final int likesCount = documentSnapshots.size();
                            holder.commentsCountTextView.setText(likesCount + "");

                        }
                    }
                });

                //get the number of likes in a cingle
                likesCountQuery.whereEqualTo("uid", mUid)
                        .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                        if (!documentSnapshots.isEmpty()){
                            final int likesCount = documentSnapshots.size();
                            holder.likesCountTextView.setText(likesCount + "");

                            if (documentSnapshots.getDocuments().contains(firebaseAuth.getCurrentUser().getUid())){
                                holder.likesImageView.setColorFilter(Color.RED);
                            }else {
                                holder.likesImageView.setColorFilter(Color.BLACK);
                            }
                        }
                    }
                });

                ifairReference.addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                        if (!documentSnapshots.isEmpty()){
                            ifairReference.document("Cingle Selling").addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                @Override
                                public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                                    if (documentSnapshot.contains(postKey)){
                                        holder.cingleTradeMethodTextView.setText("@Cingle Selling");
                                    }

                                }
                            });
                        }
                    }
                });


                likesReference.addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen error", e);
                            return;
                        }

                        if (!documentSnapshots.isEmpty()){
                            Log.i("likes count", documentSnapshots.size() + "");
                            holder.likesRecyclerView.setVisibility(View.VISIBLE);
                            //set the users who have liked the cingle

                            FirestoreRecyclerOptions<Like> options = new FirestoreRecyclerOptions.Builder<Like>()
                                    .setQuery(profileCinglesQuery, Like.class)
                                    .build();

                            firestoreRecyclerAdapter = new FirestoreRecyclerAdapter<Like, WhoLikedViewHolder>(options) {
                                @Override
                                protected void onBindViewHolder(final WhoLikedViewHolder holder, int position, Like model) {
                                    final String uid = getSnapshots().get(position).getUid();

                                    usersReference.document(uid).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                        @Override
                                        public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                                            if (documentSnapshot.exists()){
                                                final Cingulan cingulan = documentSnapshot.toObject(Cingulan.class);
                                                final String profileImage = cingulan.getProfileImage();

                                                Picasso.with(FollowerProfileActivity.this)
                                                        .load(profileImage)
                                                        .resize(MAX_WIDTH, MAX_HEIGHT)
                                                        .onlyScaleDown()
                                                        .centerCrop()
                                                        .placeholder(R.drawable.profle_image_background)
                                                        .networkPolicy(NetworkPolicy.OFFLINE)
                                                        .into(holder.whoLikedImageView, new Callback() {
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
                                                                        .into(holder.whoLikedImageView);


                                                            }
                                                        });
                                            }
                                        }
                                    });

                                }

                                @Override
                                public WhoLikedViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                                    View view = LayoutInflater.from(parent.getContext())
                                            .inflate(R.layout.who_liked_count, parent, false);
                                    return new WhoLikedViewHolder(view);
                                }
                            };

                            holder.likesRecyclerView.setAdapter(firestoreRecyclerAdapter);
                            holder.likesRecyclerView.setHasFixedSize(false);
                            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(FollowerProfileActivity.this,
                                    LinearLayoutManager.HORIZONTAL, true);
                            layoutManager.setAutoMeasureEnabled(true);
                            holder.likesRecyclerView.setNestedScrollingEnabled(false);
                            holder.likesRecyclerView.setLayoutManager(layoutManager);

                        }else {
                            holder.likesRecyclerView.setVisibility(View.GONE);
                        }

                    }
                });


                final Long timeStamp = System.currentTimeMillis();

                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("d");
                String date = simpleDateFormat.format(new Date());

                if (date.endsWith("1") && !date.endsWith("11"))
                    simpleDateFormat = new SimpleDateFormat("d'st' MMM yyyy");
                else if (date.endsWith("2") && !date.endsWith("12"))
                    simpleDateFormat = new SimpleDateFormat("d'nd' MMM yyyy");
                else if (date.endsWith("3") && !date.endsWith("13"))
                    simpleDateFormat = new SimpleDateFormat("d'rd' MMM yyyy");
                else
                    simpleDateFormat = new SimpleDateFormat("d'th' MMM yyyy");
                final String currentDate = simpleDateFormat.format(new Date());


//                holder.likesImageView.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        processLikes = true;
//                        likesRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
//                            @Override
//                            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
//                                if (e != null) {
//                                    Log.w(TAG, "Listen error", e);
//                                    return;
//                                }
//
//                                if (processLikes) {
//                                    if (documentSnapshots.getDocuments().contains(firebaseAuth.getCurrentUser().getUid())) {
//                                        likesRef.document(firebaseAuth.getCurrentUser().getUid()).delete();
//                                        processLikes = false;
//                                    } else {
//                                        Map<String, Object> like = new HashMap<String, Object>();
//                                        like.put("uid", firebaseAuth.getCurrentUser().getUid());
//                                        like.put("dateLiked", currentDate);
//                                        like.put("timestamp", timeStamp);
//
//                                        likesRef.document(firebaseAuth.getCurrentUser().getUid()).set(like);
//                                        processLikes = false;
//                                    }
//                                }
//
//                                final int x = documentSnapshots.size();
//                                Log.d("count of likes", x + "");
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
//                                    cingleWR.addSnapshotListener(new EventListener<DocumentSnapshot>() {
//                                        @Override
//                                        public void onEvent(final DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
//                                            if (e != null) {
//                                                Log.w(TAG, "Listen error", e);
//                                                return;
//                                            }
//
//                                            if (documentSnapshot.exists()){
//                                                final Balance balance = documentSnapshot.toObject(Balance.class);
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
//                                    });
//
//                                }else {
//                                    final double finalPoints = 0.00;
//                                    Log.d("final points", finalPoints + "");
//                                    cingleWR.addSnapshotListener(new EventListener<DocumentSnapshot>() {
//                                        @Override
//                                        public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
//                                            if (e != null) {
//                                                Log.w(TAG, "Listen error", e);
//                                                return;
//                                            }
//
//                                            if (documentSnapshot.exists()){
//                                                final Balance balance = documentSnapshot.toObject(Balance.class);
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
//                                    });
//                                }
//                            }
//
//                        });
//
//                    }
//                });


            }

            @Override
            public CingleOutViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.poeple_list, parent, false);
                return new CingleOutViewHolder(view);
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

//            relationsRef.addListenerForSingleValueEvent(new ValueEventListener() {
//                @Override
//                public void onDataChange(DataSnapshot dataSnapshot) {
//                    if (processFollow){
//                        Log.d("muid", mUid);
//                        if (dataSnapshot.child("followers").child(mUid).hasChild(firebaseAuth.getCurrentUser().getUid())){
//                            relationsRef.child("followers").child(mUid).child(firebaseAuth.getCurrentUser().getUid())
//                                    .removeValue();
//                            relationsRef.child("following").child(firebaseAuth.getCurrentUser().getUid()).child(mUid)
//                                    .removeValue();
//                            processFollow = false;
//                            onFollow(false);
//                            //set the text on the button to follow if the user in not yet following;
//                            mFollowButton.setText("FOLLOW");
//                            mFollowersCountTextView.setText(dataSnapshot.getChildrenCount() + "");
//
//                        }else {
//                            //set followers of mUid;
//                            relationsRef.child("followers").child(mUid).child(firebaseAuth.getCurrentUser().getUid())
//                                    .child("uid")
//                                    .setValue(firebaseAuth.getCurrentUser().getUid());
//                            //set the uid you are following
//                            relationsRef.child("following").child(firebaseAuth.getCurrentUser().getUid()).child(mUid)
//                                    .child("uid").setValue(mUid);
//                            processFollow = false;
//                            onFollow(false);
//                            //set text on the button following;
//                            mFollowButton.setText("FOLLOWING");
//                            mFollowersCountTextView.setText(dataSnapshot.getChildrenCount() + "");
//
//                        }
//
//                    }
//
//                }
//
//                @Override
//                public void onCancelled(DatabaseError databaseError) {
//
//                }
//            });

        }
    }

//    private void onFollow(final boolean increament){
//        relationsRef.runTransaction(new Transaction.Handler() {
//            @Override
//            public Transaction.Result doTransaction(MutableData mutableData) {
//                if(mutableData.getValue() != null){
//                    int value = mutableData.getValue(Integer.class);
//                    if(increament){
//                        value++;
//                    }else{
//                        value--;
//                    }
//                    mutableData.setValue(value);
//                }
//                return Transaction.success(mutableData);
//            }
//
//            @Override
//            public void onComplete(DatabaseError databaseError, boolean b,
//                                   DataSnapshot dataSnapshot) {
//                Log.d(TAG, "followTransaction:onComplete" + databaseError);
//
//            }
//        });
//    }

}

package com.cinggl.cinggl.home;


import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.cinggl.cinggl.Constants;
import com.cinggl.cinggl.R;
import com.cinggl.cinggl.adapters.BestCinglesAdapter;
import com.cinggl.cinggl.models.Balance;
import com.cinggl.cinggl.models.Cingle;
import com.cinggl.cinggl.models.Cingulan;
import com.cinggl.cinggl.models.Like;
import com.cinggl.cinggl.models.TransactionDetails;
import com.cinggl.cinggl.people.FollowerProfileActivity;
import com.cinggl.cinggl.profile.PersonalProfileActivity;
import com.cinggl.cinggl.viewholders.BestCinglesViewHolder;
import com.cinggl.cinggl.viewholders.WhoLikedViewHolder;
import com.firebase.ui.common.ChangeEventType;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.firebase.ui.firestore.ObservableSnapshotArray;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;

import static android.media.CamcorderProfile.get;

/**
 * A simple {@link Fragment} subclass.
 */
public class BestCinglesFragment extends Fragment {
    //firestore reference
    private FirebaseFirestore firebaseFirestore;
    private CollectionReference cinglesReference;
    private com.google.firebase.firestore.Query bestCinglesQuery;
    private com.google.firebase.firestore.Query likesCountQuery;
    private com.google.firebase.firestore.Query commentsCountQuery;
    private CollectionReference ownerReference;
    private CollectionReference usersReference;
    private CollectionReference commentsReference;
    private CollectionReference ifairReference;
    //firebaase
    private DatabaseReference cingleWalletReference;
    private DatabaseReference likesRef;
    private Query likesQuery;
    //firebase auth
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    //firestore adapter
    private FirebaseRecyclerAdapter firebaseRecyclerAdapter;
    private FirestoreRecyclerAdapter firestoreRecyclerAdapter;
    private static final String TAG = "BestCingleFragment";
    private static final String KEY_LAYOUT_POSITION = "layout position";
    private static final String EXTRA_POST_KEY = "post key";
    private static final String EXTRA_USER_UID = "uid";
    private static final double DEFAULT_PRICE = 1.5;
    private static final double GOLDEN_RATIO = 1.618;
    private static final int MAX_WIDTH = 200;
    private static final int MAX_HEIGHT = 200;
    private int bestCingleRecyclerPosition = 0;
    private List<Cingle> bestCingles = new ArrayList<>();
    private List<String> cinglesIds = new ArrayList<>();
    private LinearLayoutManager layoutManager;
    private boolean processLikes = false;
    private int currentPage = 0;

    private ChildEventListener mChildEventListener;
    private static final int TOTAL_ITEM_EACH_LOAD = 2;
    private Parcelable recyclerViewState;

    @Bind(R.id.bestCinglesRecyclerView)RecyclerView bestCinglesRecyclerView;
    @Bind(R.id.bestCinglesProgressbar)ProgressBar progressBar;

    public BestCinglesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() != null){
            //firebase
            likesRef = FirebaseDatabase.getInstance().getReference(Constants.LIKES);
            cingleWalletReference = FirebaseDatabase.getInstance().getReference(Constants.CINGLE_WALLET);
            likesQuery = likesRef.limitToFirst(5);
            //firestore
            cinglesReference = FirebaseFirestore.getInstance().collection(Constants.POSTS);
            ownerReference = FirebaseFirestore.getInstance().collection(Constants.CINGLE_ONWERS);
            usersReference = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);
            ifairReference = FirebaseFirestore.getInstance().collection(Constants.IFAIR);
            commentsReference = FirebaseFirestore.getInstance().collection(Constants.COMMENTS);
            bestCinglesQuery = cinglesReference.document("Cingles").collection("Cingles")
                    .orderBy("sensepoint");

            bestCinglesQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e){
                    final int size = documentSnapshots.size();
                    Log.d("size of Cingles", size + " ");

                    if (e != null) {
                        Log.w(TAG, "Listen error", e);
                        return;
                    }

                    if (documentSnapshots.isEmpty()){
                        progressBar.setVisibility(View.VISIBLE);
                    }else {
                        progressBar.setVisibility(View.GONE);
                    }
                }
            });

        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_best_cingles, container, false);
        ButterKnife.bind(this, view);

        setCurrentDate();
        setBestCingles();

        return view;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null){
            recyclerViewState = savedInstanceState.getParcelable(KEY_LAYOUT_POSITION);
        }
    }

    private void setCurrentDate(){
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
        String currentDate = simpleDateFormat.format(new Date());

    }


    public void setBestCingles(){
        FirestoreRecyclerOptions<Cingle> options = new FirestoreRecyclerOptions.Builder<Cingle>()
                .setQuery(bestCinglesQuery, Cingle.class)
                .build();

        firestoreRecyclerAdapter = new FirestoreRecyclerAdapter<Cingle, BestCinglesViewHolder>(options) {
            @Override
            protected void onBindViewHolder(final BestCinglesViewHolder holder, int position, Cingle model) {
                holder.bindBestCingle(model);
                final String postKey = getSnapshots().get(position).getPushId();
                final String uid = getSnapshots().get(position).getUid();
                Log.d("best cingles postKey", postKey);

                //document reference
                commentsCountQuery= commentsReference;
                //init document references
                ownerReference.document(postKey);
                cinglesReference.document(postKey);
                //path to ownerRef
                DocumentReference ownerRef = ownerReference.document("ownership")
                        .collection(postKey).document("owner");
                //path to sensCredits
                final DocumentReference creditsRef = cinglesReference
                        .document("Cingles").collection("Cingles").document(postKey);
                //path to cingle wallet reference
                holder.likesCountTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getActivity(), LikesActivity.class);
                        intent.putExtra(BestCinglesFragment.EXTRA_POST_KEY, postKey);
                        startActivity(intent);
                    }
                });

                holder.commentsImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent =  new Intent(getActivity(), CommentsActivity.class);
                        intent.putExtra(BestCinglesFragment.EXTRA_POST_KEY, postKey);
                        startActivity(intent);
                    }
                });

                holder.cingleImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getActivity(), FullImageViewActivity.class);
                        intent.putExtra(BestCinglesFragment.EXTRA_POST_KEY, postKey);
                        startActivity(intent);
                    }
                });

                holder.cingleTradeMethodTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent =  new Intent(getActivity(), CingleDetailActivity.class);
                        intent.putExtra(BestCinglesFragment.EXTRA_POST_KEY, postKey);
                        startActivity(intent);
                    }
                });

                holder.cingleSettingsImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Bundle bundle = new Bundle();
                        bundle.putString(BestCinglesFragment.EXTRA_POST_KEY, postKey);
                        FragmentManager fragmenManager = getChildFragmentManager();
                        CingleSettingsDialog cingleSettingsDialog = CingleSettingsDialog.newInstance("cingle settings");
                        cingleSettingsDialog.setArguments(bundle);
                        cingleSettingsDialog.show(fragmenManager, "cingle settings fragment");
                    }
                });

                holder.profileImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if ((firebaseAuth.getCurrentUser().getUid()).equals(uid)){
                            Intent intent = new Intent(getActivity(), PersonalProfileActivity.class);
                            intent.putExtra(BestCinglesFragment.EXTRA_USER_UID, uid);
                            startActivity(intent);
                            Log.d("profile uid", firebaseAuth.getCurrentUser().getUid());
                        }else {
                            Intent intent = new Intent(getActivity(), FollowerProfileActivity.class);
                            intent.putExtra(BestCinglesFragment.EXTRA_USER_UID, uid);
                            Log.d("follower uid", uid);
                            startActivity(intent);
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
                            final Cingulan cingulan = documentSnapshot.toObject(Cingulan.class);
                            final String username = cingulan.getUsername();
                            final String profileImage = cingulan.getProfileImage();

                            holder.usernameTextView.setText(cingulan.getUsername());
                            Picasso.with(getContext())
                                    .load(cingulan.getProfileImage())
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
                                            Picasso.with(getContext())
                                                    .load(cingulan.getProfileImage())
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

                            usersReference.document(ownerUid).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                @Override
                                public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                                    if (e != null) {
                                        Log.w(TAG, "Listen error", e);
                                        return;
                                    }

                                    if (documentSnapshot.exists()){
                                        Cingulan cingulan = documentSnapshot.toObject(Cingulan.class);
                                        final String profileImage = cingulan.getProfileImage();
                                        final String username = cingulan.getUsername();
                                        holder.cingleOwnerTextView.setText(username);
                                        Picasso.with(getContext())
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
                                                        Picasso.with(getContext())
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
                        }
                    }
                });

                //get the number of commments in a cingle
                commentsCountQuery.whereEqualTo("pushId", postKey).addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                        if (!documentSnapshots.isEmpty()){
                            final int likesCount = documentSnapshots.size();
                            holder.commentsCountTextView.setText(likesCount + "");

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

                ifairReference.addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen error", e);
                            return;
                        }

                        if (!documentSnapshots.isEmpty()){
                            ifairReference.document("Cingles").addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                @Override
                                public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                                    if (e != null) {
                                        Log.w(TAG, "Listen error", e);
                                        return;
                                    }

                                    if (documentSnapshot.exists()){
                                        Log.d("Cingle Selling", "this document exists");
                                    }
                                }
                            });
                        }
                    }
                });

//                ifairReference.document("Cingles").addSnapshotListener(new EventListener<DocumentSnapshot>() {
//                    @Override
//                    public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
//                        if (e != null) {
//                            Log.w(TAG, "Listen error", e);
//                            return;
//                        }
//
//                        if (documentSnapshot.exists()){
//                            if (documentSnapshot.contains("Cingle Backing")){
//                                documentSnapshot.getDocumentReference("Cingle Backing")
//                                        .addSnapshotListener(new EventListener<DocumentSnapshot>() {
//                                            @Override
//                                            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
//                                                if (e != null) {
//                                                    Log.w(TAG, "Listen error", e);
//                                                    return;
//                                                }
//
//                                                if (documentSnapshot.exists()){
//                                                    holder.cingleTradeMethodTextView.setText("@CingleBacking");
//                                                }
//                                            }
//                                        });
//                            }else if (documentSnapshot.contains("Cingle Lacing")){
//                                documentSnapshot.getDocumentReference("Cingle Lacing")
//                                        .addSnapshotListener(new EventListener<DocumentSnapshot>() {
//                                            @Override
//                                            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
//                                                if (e != null) {
//                                                    Log.w(TAG, "Listen error", e);
//                                                    return;
//                                                }
//
//                                                if (documentSnapshot.exists()){
//                                                    holder.cingleTradeMethodTextView.setText("@CingleLacing");
//                                                }
//                                            }
//                                        });
//                            }else if (documentSnapshot.contains("Cingle Leasing")){
//                                documentSnapshot.getDocumentReference("Cingle Leasing")
//                                        .addSnapshotListener(new EventListener<DocumentSnapshot>() {
//                                            @Override
//                                            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
//                                                if (e != null) {
//                                                    Log.w(TAG, "Listen error", e);
//                                                    return;
//                                                }
//
//                                                if (documentSnapshot.exists()){
//                                                    holder.cingleTradeMethodTextView.setText("@CingleLeasing");
//                                                }
//                                            }
//                                        });
//                            }else if (documentSnapshot.contains("Cingle Selling")){
//                                Log.d("Cingle Selling", "this document exists");
//                                documentSnapshot.getDocumentReference("Cingle Selling")
//                                        .addSnapshotListener(new EventListener<DocumentSnapshot>() {
//                                            @Override
//                                            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
//                                                if (e != null) {
//                                                    Log.w(TAG, "Listen error", e);
//                                                    return;
//                                                }
//
//                                                if (documentSnapshot.exists()){
//                                                    holder.cingleTradeMethodTextView.setText("@CingleSelling");
//                                                }
//                                            }
//                                        });
//                            }else {
//                                holder.cingleTradeMethodTextView.setText("@NotForTrade");
//                            }
//                        }
//                    }
//                });

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
                                                                Cingulan cingulan = documentSnapshot.toObject(Cingulan.class);
                                                                final String profileImage = cingulan.getProfileImage();

                                                                Picasso.with(getContext())
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
                                                                                Picasso.with(getContext())
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
                                RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext(),
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
                                                .child(firebaseAuth.getCurrentUser().getUid()).setValue(like);
                                        processLikes = false;
                                        onLikeCounter(false);
                                        holder.likesImageView.setColorFilter(Color.RED);
                                    }
                                }


                                String likesCount = dataSnapshot.child(postKey).getChildrenCount() + "";
                                Log.d(likesCount, "all the likes in one cingle");
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
                                    //get the new worth of Cingle price in Sen
                                    final double cingleWorth = perfectionValue * likesPerMille * currentPrice;
                                    //round of the worth of the cingle to 10 decimal number
                                    final double finalPoints = round( cingleWorth, 10);

                                    Log.d("final points", finalPoints + "");

                                    cingleWalletReference.child(postKey).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.exists()) {
                                                final Balance balance = dataSnapshot.getValue(Balance.class);
                                                final double amountRedeemed = balance.getAmountRedeemed();
                                                Log.d(amountRedeemed + "", "amount redeemed");
                                                final  double amountDeposited = balance.getAmountDeposited();
                                                Log.d(amountDeposited + "", "amount deposited");
                                                final double senseCredits = amountDeposited + finalPoints;
                                                Log.d("sense credits", senseCredits + "");
                                                final double totalSenseCredits = senseCredits - amountRedeemed;
                                                Log.d("total sense credits", totalSenseCredits + "");

                                                Map<String, Cingle> credits = new HashMap<String, Cingle>();
                                                creditsRef.update("sensepoint", totalSenseCredits);

                                            }else {
                                                Map<String, Cingle> credits = new HashMap<String, Cingle>();
                                                creditsRef.update("sensepoint", finalPoints);
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
                                    cingleWalletReference.child(postKey).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.exists()) {
                                                final Balance balance = dataSnapshot.getValue(Balance.class);
                                                final double amountRedeemed = balance.getAmountRedeemed();
                                                Log.d(amountRedeemed + "", "amount redeemed");
                                                final  double amountDeposited = balance.getAmountDeposited();
                                                Log.d(amountDeposited + "", "amount deposited");
                                                final double senseCredits = amountDeposited + finalPoints;
                                                Log.d("sense credits", senseCredits + "");
                                                final double totalSenseCredits = senseCredits - amountRedeemed;
                                                Log.d("total sense credits", totalSenseCredits + "");
                                                Map<String, Cingle> credits = new HashMap<String, Cingle>();
                                                creditsRef.update("sensepoint", totalSenseCredits);

                                            }else {
                                                Map<String, Cingle> credits = new HashMap<String, Cingle>();
                                                creditsRef.update("sensepoint", finalPoints);
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

            @Override
            public BestCinglesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.best_cingles_list, parent, false);
                return new BestCinglesViewHolder(view);
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

        layoutManager =  new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        bestCinglesRecyclerView.setLayoutManager(layoutManager);
        bestCinglesRecyclerView.setHasFixedSize(false);
        bestCinglesRecyclerView.setAdapter(firestoreRecyclerAdapter);

    }

    //region listeners
    private static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(KEY_LAYOUT_POSITION, layoutManager.onSaveInstanceState());
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
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStart() {
        super.onStart();
        firestoreRecyclerAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        firestoreRecyclerAdapter.stopListening();
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

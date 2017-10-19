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
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.firebase.ui.firestore.ObservableSnapshotArray;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
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
    private CollectionReference likesRef;
    private CollectionReference cingleWalletReference;
    private CollectionReference usersReference;
    private CollectionReference commentsReference;
    private CollectionReference ifairReference;
    //firebase auth
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    //firestore adapter
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
    private BestCinglesAdapter bestCinglesAdapter;
    private LinearLayoutManager layoutManager;
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
            //firestore
            cinglesReference = FirebaseFirestore.getInstance().collection(Constants.POSTS);
            ownerReference = FirebaseFirestore.getInstance().collection(Constants.CINGLE_ONWERS);
            likesRef = FirebaseFirestore.getInstance().collection(Constants.LIKES);
            cingleWalletReference = FirebaseFirestore.getInstance().collection(Constants.CINGLE_WALLET);
            usersReference = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);
            ifairReference = FirebaseFirestore.getInstance().collection(Constants.IFAIR);
            commentsReference = FirebaseFirestore.getInstance().collection(Constants.COMMENTS);
            bestCinglesQuery = FirebaseFirestore.getInstance().collection(Constants.POSTS)
                    .orderBy("sensepoint");

        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_best_cingles, container, false);
        ButterKnife.bind(this, view);

        setCurrentDate();
        setBestCingles(currentPage);

        return view;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initializeViewsAdapter();

        if (savedInstanceState != null){
            recyclerViewState = savedInstanceState.getParcelable(KEY_LAYOUT_POSITION);
        }
    }

    private void initializeViewsAdapter(){
        layoutManager =  new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        bestCinglesRecyclerView.setLayoutManager(layoutManager);
        bestCinglesRecyclerView.setHasFixedSize(false);
        bestCinglesAdapter = new BestCinglesAdapter(getContext());
        bestCinglesRecyclerView.setAdapter(bestCinglesAdapter);

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

    public void setBestCingles(int start){
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
                likesCountQuery = likesRef.document("Cingle Likes").collection("Likes");
                commentsCountQuery= commentsReference;
                //init document references
                ownerReference.document(postKey);
                cinglesReference.document(postKey);
                likesRef.document(postKey);
                //path to ownerRef
                DocumentReference ownerRef = ownerReference.document("ownership")
                        .collection(postKey).document("owner");
                //path to sensCredits
                final DocumentReference creditsRef = cinglesReference
                        .document("Cingles").collection("Cingles").document(postKey);
                //path to cingle wallet reference
                final DocumentReference cingleWR = cingleWalletReference.document(postKey);
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
                commentsCountQuery.whereEqualTo("pushId", postKey).addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                        if (!documentSnapshots.isEmpty()){
                            final int likesCount = documentSnapshots.size();
                            holder.commentsCountTextView.setText(likesCount + "");

                        }
                    }
                });

                //get the number of likes in a cingle
                likesCountQuery.whereEqualTo("postKey", postKey).addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                        if (!documentSnapshots.isEmpty()){
                            final int likesCount = documentSnapshots.size();
                            holder.likesCountTextView.setText(likesCount + "");

                            if (documentSnapshots.getDocuments().contains(firebaseUser.getUid())){
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


                likesRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
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
                                    .setQuery(likesCountQuery, Like.class)
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

                                                Picasso.with(getContext())
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
                                                                Picasso.with(getContext())
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
                            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext(),
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


                holder.likesImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        likesRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
                            @Override
                            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                                if (e != null) {
                                    Log.w(TAG, "Listen error", e);
                                    return;
                                }

                                if (documentSnapshots.getDocuments().contains(firebaseAuth.getCurrentUser().getUid())) {
                                    likesRef.document(firebaseAuth.getCurrentUser().getUid()).delete();
                                } else {
                                    Map<String, Object> like = new HashMap<String, Object>();
                                    like.put("uid", firebaseAuth.getCurrentUser().getUid());
                                    like.put("dateLiked", currentDate);
                                    like.put("timestamp", timeStamp);
                                    like.put("postKey", postKey);
                                    likesRef.document(firebaseAuth.getCurrentUser().getUid()).set(like);
                                }

                                final int x = documentSnapshots.size();
                                Log.d("count of likes", x + "");

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

                                    cingleWR.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                        @Override
                                        public void onEvent(final DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
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
                                    });

                                }else {
                                    final double finalPoints = 0.00;
                                    Log.d("final points", finalPoints + "");
                                    cingleWR.addSnapshotListener(new EventListener<DocumentSnapshot>() {
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
                                    });
                                }
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
}

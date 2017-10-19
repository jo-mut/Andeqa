package com.cinggl.cinggl.home;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cinggl.cinggl.Constants;
import com.cinggl.cinggl.R;
import com.cinggl.cinggl.ifair.SendCreditsDialogFragment;
import com.cinggl.cinggl.models.Cingle;
import com.cinggl.cinggl.models.CingleSale;
import com.cinggl.cinggl.models.Cingulan;
import com.cinggl.cinggl.models.Like;
import com.cinggl.cinggl.models.TransactionDetails;
import com.cinggl.cinggl.people.FollowerProfileActivity;
import com.cinggl.cinggl.profile.PersonalProfileActivity;
import com.cinggl.cinggl.viewholders.WhoLikedViewHolder;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

import static com.cinggl.cinggl.R.id.cingleImageView;
import static com.cinggl.cinggl.R.id.cingleOwnerTextView;
import static com.cinggl.cinggl.R.id.cingleSalePriceTextView;
import static com.cinggl.cinggl.R.id.cingleSenseCreditsTextView;
import static com.cinggl.cinggl.R.id.cingleTradeMethodTextView;
import static com.cinggl.cinggl.R.id.commentsCountTextView;
import static com.cinggl.cinggl.R.id.likesCountTextView;
import static com.cinggl.cinggl.R.id.likesImageView;
import static com.cinggl.cinggl.R.id.likesRecyclerView;

public class CingleDetailActivity extends AppCompatActivity implements View.OnClickListener{

    @Bind(R.id.usernameTextView)TextView mUsernameTextView;
    @Bind(cingleImageView)CircleImageView mCingleImageView;
    @Bind(R.id.profileImageView)ImageView mProfileImageView;
    @Bind(R.id.lacedCingleImageView)ImageView mLacedCingleImageView;
    @Bind(R.id.cingleTitleTextView)TextView mCingleTitleTextView;
    @Bind(R.id.cingleTitleRelativeLayout)RelativeLayout mCingleTitleRelativeLayout;
    @Bind(R.id.cingleDescriptionRelativeLayout)RelativeLayout mCingleDescriptionRelatvieLayout;
    @Bind(R.id.cingleDescriptionTextView)TextView mCingleDescriptionTextView;
    @Bind(cingleTradeMethodTextView)TextView mCingleTradeMethodTextView;
    @Bind(cingleOwnerTextView)TextView mCingleOwnerTextView;
    @Bind(likesImageView)ImageView mLikesImageView;
    @Bind(likesCountTextView)TextView mLikesCountTextView;
    @Bind(R.id.commentsImageView)ImageView mCommentImageView;
    @Bind(commentsCountTextView)TextView mCommentCountTextView;
    @Bind(likesRecyclerView)RecyclerView mLikesRecyclerView;
    @Bind(R.id.lacedCinglesRecyclerView)RecyclerView mLacedCinglesReyclerView;
    @Bind(cingleSenseCreditsTextView)TextView mCingleSenseCreditsTextView;
    @Bind(R.id.tradeCingleButton)Button mTradeCingleButton;
    @Bind(cingleSalePriceTextView)TextView mCingleSalePriceTextView;
    @Bind(R.id.datePostedTextView)TextView mDatePostedTextView;
    @Bind(R.id.ownerImageView)CircleImageView mOwnerImageView;
    @Bind(R.id.editSalePriceImageView)ImageView mEditSalePriceImageView;
    @Bind(R.id.editSalePriceEditText)EditText mEditSalePriceEditText;
    @Bind(R.id.doneEditingImageView)ImageView mDoneEditingImageView;
    @Bind(R.id.salePriceProgressbar)ProgressBar mSalePriceProgressBar;
    @Bind(R.id.cingleSalePriceTitleRelativeLayout)RelativeLayout mCingleSalePriceTitleRelativeLayout;


    //firestore reference
    private FirebaseFirestore firebaseFirestore;
    private CollectionReference cinglesReference;
    private com.google.firebase.firestore.Query randomQuery;
    private com.google.firebase.firestore.Query likesCountQuery;
    private com.google.firebase.firestore.Query whoLikedQuery;
    private com.google.firebase.firestore.Query commentsCountQuery;
    private com.google.firebase.firestore.Query likesQuery;
    private CollectionReference ownerReference;
    private CollectionReference likesRef;
    private CollectionReference cingleWalletReference;
    private CollectionReference usersReference;
    private CollectionReference commentsReference;
    private CollectionReference ifairReference;
    private CollectionReference cingleOwnerReference;
    //firebase auth
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    //firestore adapter
    private FirestoreRecyclerAdapter firestoreRecyclerAdapter;
    //process likes
    private boolean processLikes = false;
    private static final double DEFAULT_PRICE = 1.5;
    private static final double GOLDEN_RATIO = 1.618;
    private Context mContext;
    private String mPostKey;
    private static final String EXTRA_POST_KEY = "post key";
    private static final String EXTRA_USER_UID = "uid";
    private static final String TAG = CingleDetailActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cingle_detail);
        ButterKnife.bind(this);

        firebaseAuth = FirebaseAuth.getInstance();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser()!= null){
            mPostKey = getIntent().getStringExtra(EXTRA_POST_KEY);
            if(mPostKey == null){
                throw new IllegalArgumentException("pass an EXTRA_POST_KEY");
            }

            //INITIALIASE CLICK LISTENER
            mLikesImageView.setOnClickListener(this);
            mLikesRecyclerView.setOnClickListener(this);
            mCommentImageView.setOnClickListener(this);
            mLikesCountTextView.setOnClickListener(this);
            mTradeCingleButton.setOnClickListener(this);
            mCingleImageView.setOnClickListener(this);
            mEditSalePriceImageView.setOnClickListener(this);
            mDoneEditingImageView.setOnClickListener(this);

            //firestore
            cinglesReference = FirebaseFirestore.getInstance().collection(Constants.POSTS);
            ownerReference = FirebaseFirestore.getInstance().collection(Constants.CINGLE_ONWERS);
            likesRef = FirebaseFirestore.getInstance().collection(Constants.LIKES);
            cingleWalletReference = FirebaseFirestore.getInstance().collection(Constants.CINGLE_WALLET);
            cingleOwnerReference = FirebaseFirestore.getInstance().collection(Constants.CINGLE_ONWERS);
            usersReference = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);
            ifairReference = FirebaseFirestore.getInstance().collection(Constants.IFAIR);
            commentsReference = FirebaseFirestore.getInstance().collection(Constants.COMMENTS);
            randomQuery = FirebaseFirestore.getInstance().collection(Constants.POSTS)
                    .orderBy("randomNumber");
            whoLikedQuery = FirebaseFirestore.getInstance().collection(Constants.LIKES)
                    .orderBy("uid").limit(5);
            commentsCountQuery = commentsReference;
            likesQuery = cinglesReference.document("Cingles Likes").collection("Likes");



            //RETRIEVE DATA FROM FIREBASE
            setCingleData();
            setTextOnButton();
            setCingleInfo();
            setEditTextFilter();
            showBuyButton();
            showEditImageView();

        }
    }

    private void setCingleData(){
        commentsCountQuery.whereEqualTo("postKey", mPostKey).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                mCommentCountTextView.setText(documentSnapshots.getDocuments().size());
            }
        });

        likesQuery.whereEqualTo("postKey", mPostKey).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                mLikesCountTextView.setText(documentSnapshots.getDocuments().size());
            }
        });

        cinglesReference.document("Cingles").collection("Cingles").document(mPostKey)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (documentSnapshot.exists()){
                    final Cingle cingle = documentSnapshot.toObject(Cingle.class);
                    final String image = cingle.getCingleImageUrl();
                    final String uid = cingle.getUid();
                    final String title = cingle.getTitle();
                    final String description = cingle.getDescription();
                    final double sensecredits = cingle.getSensepoint();

                    //LAUCNH PROFILE IF ITS NOT DELETED ELSE CATCH THE EXCEPTION
                    mProfileImageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (uid.equals(firebaseAuth.getCurrentUser().getUid())){
                                Intent intent = new Intent(CingleDetailActivity.this, PersonalProfileActivity.class);
                                startActivity(intent);
                            }else {
                                Intent intent = new Intent(CingleDetailActivity.this, FollowerProfileActivity.class);
                                intent.putExtra(CingleDetailActivity.EXTRA_USER_UID, uid);
                                startActivity(intent);
                            }
                        }
                    });

//                    set the title of the cingle
                    if (title.equals("")){
                        mCingleTitleRelativeLayout.setVisibility(View.GONE);
                    }else {
                        mCingleTitleTextView.setText(title);
                    }

                    if (description.equals("")){
                        mCingleDescriptionRelatvieLayout.setVisibility(View.GONE);
                    }else {
                        mCingleDescriptionTextView.setText(description);
                    }

                    mCingleSenseCreditsTextView.setText("CSC" + " " + " " + sensecredits);
                    mDatePostedTextView.setText(cingle.getDatePosted());

                    //set the cingle image
                    Picasso.with(CingleDetailActivity.this)
                            .load(image)
                            .networkPolicy(NetworkPolicy.OFFLINE)
                            .into(mCingleImageView, new Callback() {
                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onError() {
                                    Picasso.with(CingleDetailActivity.this)
                                            .load(image)
                                            .into(mCingleImageView);
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

                                mUsernameTextView.setText(username);

                                Picasso.with(CingleDetailActivity.this)
                                        .load(profileImage)
                                        .fit()
                                        .centerCrop()
                                        .placeholder(R.drawable.profle_image_background)
                                        .networkPolicy(NetworkPolicy.OFFLINE)
                                        .into(mProfileImageView, new Callback() {
                                            @Override
                                            public void onSuccess() {

                                            }

                                            @Override
                                            public void onError() {
                                                Picasso.with(CingleDetailActivity.this)
                                                        .load(profileImage)
                                                        .fit()
                                                        .centerCrop()
                                                        .placeholder(R.drawable.profle_image_background)
                                                        .into(mProfileImageView);
                                            }
                                        });
                            }
                        }
                    });
                }
            }
        });

    }

    /**Cingle can only be bought by someone else except for the owner of that cingle*/
    private void showBuyButton(){
        cingleOwnerReference.document(mPostKey).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (documentSnapshot.exists()){
                    documentSnapshot.getDocumentReference("owner").addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                            TransactionDetails transactionDetails = documentSnapshot.toObject(TransactionDetails.class);
                            final String ownerUid = transactionDetails.getUid();
                            Log.d("owner uid", ownerUid);

                            if (firebaseAuth.getCurrentUser().getUid().equals(ownerUid)){
                                mTradeCingleButton.setVisibility(View.INVISIBLE);
                            }else {
                                mTradeCingleButton.setVisibility(View.VISIBLE);
                            }
                        }
                    });
                }
            }
        });

    }

    /**set the the text on buy button*/
    private void setTextOnButton(){
        ifairReference.document("Cingle Selling").addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                if (documentSnapshot.contains(mPostKey)){
                 mTradeCingleButton.setText("Buy");
                }
            }
        });

    }

    /**display the price of the cingle*/
    private void setCingleInfo() {
        cinglesReference.document("Cingles").collection("Cingles")
                .document(mPostKey).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (documentSnapshot.exists()) {
                    Cingle cingle = documentSnapshot.toObject(Cingle.class);
                    final String datePosted = cingle.getDatePosted();
                    mDatePostedTextView.setText(datePosted);

                }
            }
        });

        ifairReference.document("Cingle Selling").addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (documentSnapshot.exists()) {
                    documentSnapshot.getDocumentReference(mPostKey).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                            if (e != null) {
                                Log.w(TAG, "Listen error", e);
                                return;
                            }

                            if (documentSnapshot.exists()) {
                                CingleSale cingleSale = documentSnapshot.toObject(CingleSale.class);
                                DecimalFormat formatter = new DecimalFormat("0.00000000");
                                mCingleSalePriceTextView.setText("CSC" + " " + formatter.format(cingleSale.getSalePrice()));
                            } else {
                                mCingleSalePriceTitleRelativeLayout.setVisibility(View.GONE);
                            }
                        }
                    });
                }
            }
        });

        ifairReference.document("Cingles").collection("Cingle Selling")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (documentSnapshots.getDocuments().contains(mPostKey)){

                }
            }
        });

        ifairReference.document("Cingles").collection("Cingle Selling")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (documentSnapshots.getDocuments().contains(mPostKey)){
                    mCingleTradeMethodTextView.setText("@CingleSelling");
                }
            }
        });

        ifairReference.document("Cingles").collection("Cingle Lacing")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen error", e);
                            return;
                        }

                        if (documentSnapshots.getDocuments().contains(mPostKey)){
                            mCingleTradeMethodTextView.setText("@CingleLacing");
                        }
                    }
                });

        ifairReference.document("Cingles").collection("Cingle Leasing")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (documentSnapshots.getDocuments().contains(mPostKey)){
                    mCingleTradeMethodTextView.setText("@CingleLeasing");
                }
            }
        });

        ifairReference.document("Cingles").collection("Cingle Backing")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen error", e);
                            return;
                        }

                        if (documentSnapshots.getDocuments().contains(mPostKey)){
                            mCingleTradeMethodTextView.setText("@CingleBacking");
                        }
                    }
                });

        cinglesReference.document("Cingles").collection("Cingles")
                .document(mPostKey).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (documentSnapshot.exists()) {
                    final Cingle cingle = documentSnapshot.toObject(Cingle.class);

                    Picasso.with(mContext)
                            .load(cingle.getCingleImageUrl())
                            .networkPolicy(NetworkPolicy.OFFLINE)
                            .into(mCingleImageView, new Callback() {
                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onError() {
                                    Picasso.with(mContext)
                                            .load(cingle.getCingleImageUrl())
                                            .into(mCingleImageView);


                                }
                            });
                    DecimalFormat formatter = new DecimalFormat("0.00000000");
                    mCingleSenseCreditsTextView.setText("CSC" + "" + "" + formatter.format(cingle.getSensepoint()));
                    mDatePostedTextView.setText(cingle.getDatePosted());
                }
            }
        });


        //RETRIEVE THE FIRST FIVE USERS WHO LIKED
        likesRef.document("Cingle Likes").collection("Likes").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (!documentSnapshots.isEmpty()){
                    Log.d("likes count", documentSnapshots.size() + "");

                    if (documentSnapshots.size() > 0){
                        mLikesRecyclerView.setVisibility(View.VISIBLE);
                        FirestoreRecyclerOptions<Like> options = new FirestoreRecyclerOptions.Builder<Like>()
                                .setQuery(likesQuery, Like.class)
                                .build();

                        firestoreRecyclerAdapter = new FirestoreRecyclerAdapter<Like, WhoLikedViewHolder>(options) {
                            @Override
                            protected void onBindViewHolder(final WhoLikedViewHolder holder, int position, Like model) {
                                holder.bindWhoLiked(model);
                                final String whoLikedPostKey = getSnapshots().get(position).getPushId();
                                final String uid = getSnapshots().get(position).getUid();
                                Log.d("who liked postkey", whoLikedPostKey);

                                usersReference.document(uid).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                    @Override
                                    public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                                        if (e != null) {
                                            Log.w(TAG, "Listen error", e);
                                            return;
                                        }

                                        if (documentSnapshot.exists()){
                                            final Cingulan cingulan = documentSnapshot.toObject(Cingulan.class);
                                            final String profileImage = cingulan.getProfileImage();

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
                            public WhoLikedViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                                View view = LayoutInflater.from(parent.getContext())
                                        .inflate(R.layout.who_liked_count, parent, false);
                                return new WhoLikedViewHolder(view);
                            }
                        };

                        mLikesRecyclerView.setAdapter(firestoreRecyclerAdapter);
                        mLikesRecyclerView.setHasFixedSize(false);
                        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(mContext,
                                LinearLayoutManager.HORIZONTAL, true);
                        layoutManager.setAutoMeasureEnabled(true);
                        mLikesRecyclerView.setNestedScrollingEnabled(false);
                        mLikesRecyclerView.setLayoutManager(layoutManager);

                    }else {
                        mLikesRecyclerView.setVisibility(View.GONE);
                    }
                }

            }
        });

        ifairReference.document("Cingles").collection("Cingle Selling")
                .document(mPostKey).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (documentSnapshot.exists()){
                    documentSnapshot.getDocumentReference(mPostKey).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                            if (e != null) {
                                Log.w(TAG, "Listen error", e);
                                return;
                            }

                            if (documentSnapshot.exists()){
                                CingleSale cingleSale = documentSnapshot.toObject(CingleSale.class);
                                final String uid = cingleSale.getUid();

                                DecimalFormat formatter = new DecimalFormat("0.00000000");
                                mCingleSalePriceTextView.setText("CSC" + " " + "" + formatter.format(cingleSale.getSalePrice()));

                                usersReference.document(uid).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                    @Override
                                    public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                                        if (e != null) {
                                            Log.w(TAG, "Listen error", e);
                                            return;
                                        }

                                        if (documentSnapshot.exists()){
                                            final Cingulan cingulan = documentSnapshot.toObject(Cingulan.class);
                                            Picasso.with(mContext)
                                                    .load(cingulan.getProfileImage())
                                                    .networkPolicy(NetworkPolicy.OFFLINE)
                                                    .into(mProfileImageView, new Callback() {
                                                        @Override
                                                        public void onSuccess() {

                                                        }

                                                        @Override
                                                        public void onError() {
                                                            Picasso.with(mContext)
                                                                    .load(cingulan.getProfileImage())
                                                                    .into(mProfileImageView);


                                                        }
                                                    });
                                            mUsernameTextView.setText(cingulan.getUsername());
                                        }
                                    }
                                });
                            }

                        }
                    });
                }
            }
        });

        /**display the person who currently owns the cingle*/
        cingleOwnerReference.document("Ownership").collection(mPostKey)
                .document("Owner").addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (documentSnapshot.exists()) {
                    TransactionDetails transactionDetails = documentSnapshot.toObject(TransactionDetails.class);
                    final String ownerUid = transactionDetails.getUid();

                    usersReference.document(ownerUid).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                            if (documentSnapshot.exists()) {
                                Cingulan cingulan = documentSnapshot.toObject(Cingulan.class);
                                final String username = cingulan.getUsername();
                                final String profileImage = cingulan.getProfileImage();

                                mCingleOwnerTextView.setText(username);
                                Picasso.with(mContext)
                                        .load(profileImage)
                                        .fit()
                                        .centerCrop()
                                        .placeholder(R.drawable.profle_image_background)
                                        .networkPolicy(NetworkPolicy.OFFLINE)
                                        .into(mOwnerImageView, new Callback() {
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
                                                        .into(mOwnerImageView);
                                            }
                                        });
                            }
                        }
                    });
                }
            }
        });

    }

    @Override
    public void onClick(View v){
        if (v == mLikesImageView){
//            processLikes = true;
//            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
//                @Override
//                public void onDataChange(DataSnapshot dataSnapshot) {
//                    if (dataSnapshot.child(mPostKey).exists()){
//                        processLikes = true;
//                        likesRef.addValueEventListener(new ValueEventListener() {
//                            @Override
//                            public void onDataChange(final DataSnapshot dataSnapshot) {
//                                if(processLikes){
//                                    if(dataSnapshot.child(mPostKey).hasChild(firebaseAuth.getCurrentUser().getUid())){
//                                        likesRef.child(mPostKey).child(firebaseAuth.getCurrentUser()
//                                                .getUid())
//                                                .removeValue();
//                                        onLikeCounter(false);
//                                        processLikes = false;
//                                        mLikesImageView.setColorFilter(Color.BLACK);
//
//                                    }else {
//                                        likesRef.child(mPostKey).child(firebaseAuth.getCurrentUser().getUid())
//                                                .child("uid").setValue(firebaseAuth.getCurrentUser().getUid());
//                                        processLikes = false;
//                                        onLikeCounter(false);
//                                        mLikesImageView.setColorFilter(Color.RED);
//                                    }
//                                }
//
//
//                                String likesCount = dataSnapshot.child(mPostKey).getChildrenCount() + "";
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
//                                    cingleWalletReference.child(mPostKey).addListenerForSingleValueEvent(new ValueEventListener() {
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
//                                                databaseReference.child(mPostKey).child("sensepoint").setValue(totalSenseCredits);
//                                            }else {
//                                                databaseReference.child(mPostKey).child("sensepoint").setValue(finalPoints);
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
//                                    cingleWalletReference.child(mPostKey).addListenerForSingleValueEvent(new ValueEventListener() {
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
//                                                databaseReference.child(mPostKey).child("sensepoint").setValue(totalSenseCredits);
//                                            }else {
//                                                databaseReference.child(mPostKey).child("sensepoint").setValue(finalPoints);
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
//                }
//
//                @Override
//                public void onCancelled(DatabaseError databaseError) {
//
//                }
//            });

        }

        if (v == mCommentImageView){
            Intent intent = new Intent(CingleDetailActivity.this, CommentsActivity.class);
            intent.putExtra(CingleDetailActivity.EXTRA_POST_KEY, mPostKey);
            startActivity(intent);
        }

        if (v == mLikesCountTextView){
            Intent intent = new Intent(CingleDetailActivity.this, LikesActivity.class);
            intent.putExtra(CingleDetailActivity.EXTRA_POST_KEY, mPostKey);
            startActivity(intent);
        }

        if (v == mCingleImageView){
            Intent intent = new Intent(CingleDetailActivity.this, FullImageViewActivity.class);
            intent.putExtra(CingleDetailActivity.EXTRA_POST_KEY, mPostKey);
            startActivity(intent);
        }

        if (v == mTradeCingleButton){
            Bundle bundle = new Bundle();
            bundle.putString(CingleDetailActivity.EXTRA_POST_KEY, mPostKey);
            FragmentManager fragmenManager = getSupportFragmentManager();
            SendCreditsDialogFragment sendCreditsDialogFragment = SendCreditsDialogFragment.newInstance("sens credits");
            sendCreditsDialogFragment.setArguments(bundle);
            sendCreditsDialogFragment.show(fragmenManager, "send credits fragment");
        }

        if (v == mDoneEditingImageView){
            setNewPrice();
        }

        if (v == mEditSalePriceImageView){
            mEditSalePriceEditText.setVisibility(View.VISIBLE);
            mCingleSalePriceTextView.setVisibility(View.GONE);
            mEditSalePriceImageView.setVisibility(View.GONE);
            mDoneEditingImageView.setVisibility(View.VISIBLE);
        }
    }

    private void showEditImageView(){
        cingleOwnerReference.document("Ownership").collection(mPostKey)
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

                    if (firebaseAuth.getCurrentUser().getUid().equals(ownerUid)){
                        mEditSalePriceImageView.setVisibility(View.VISIBLE);
                    }else {
                        mEditSalePriceImageView.setVisibility(View.GONE);
                    }
                }

            }
        });
    }

    private void setNewPrice(){
        final String stringSalePrice = mEditSalePriceEditText.getText().toString().trim();
        if (stringSalePrice.equals("")){
            mEditSalePriceEditText.setError("Sale price is empty!");
        }else {
            final double intSalePrice = Double.parseDouble(stringSalePrice);

            cinglesReference.document("Cingles").collection("Cingles")
                    .document(mPostKey).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                    if (e != null) {
                        Log.w(TAG, "Listen error", e);
                        return;
                    }

                    if (documentSnapshot.exists()){
                        Cingle cingle = documentSnapshot.toObject(Cingle.class);
                        final double sensecredits = cingle.getSensepoint();

                        if (intSalePrice < sensecredits){
                            mEditSalePriceEditText.setError("Sale price is less than Cingle Sense Crdits!");
                        }else {
                            mSalePriceProgressBar.setVisibility(View.VISIBLE);
                            ifairReference.document("Cingles").collection("Cingle Selling").document(mPostKey)
                                    .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                @Override
                                public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                                    if (e != null) {
                                        Log.w(TAG, "Listen error", e);
                                        return;
                                    }

                                    Map<String, Double> credit = new HashMap<String, Double>();
                                    credit.put("salePrice", intSalePrice);
                                    ifairReference.document("Cingles").collection("Cingle Seling").document(mPostKey)
                                            .set(credit);

                                }
                            });
                        }
                    }
                }
            });
            mEditSalePriceEditText.setText("");
        }
    }
//
//    private void onLikeCounter(final boolean increament){
//        likesRef.runTransaction(new Transaction.Handler() {
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
//                Log.d(TAG, "likeTransaction:onComplete" + databaseError);
//
//            }
//        });
//    }

    //region listeners
    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    //cingle sense edittext filter
    public void setEditTextFilter(){
        mEditSalePriceEditText.setFilters(new InputFilter[] {
                new DigitsKeyListener(Boolean.FALSE, Boolean.TRUE) {
                    int beforeDecimal = 13, afterDecimal = 8;

                    @Override
                    public CharSequence filter(CharSequence source, int start, int end,
                                               Spanned dest, int dstart, int dend) {
                        String temp = mEditSalePriceEditText.getText() + source.toString();

                        if (temp.equals(".")) {
                            return "0.";
                        }else if (temp.equals("0")){
                            return "0.";//if number begins with 0 return decimal place right after
                        }
                        else if (temp.toString().indexOf(".") == -1) {
                            // no decimal point placed yet
                            if (temp.length() > beforeDecimal) {
                                return "";
                            }
                        } else {
                            temp = temp.substring(temp.indexOf(".") + 1);
                            if (temp.length() > afterDecimal) {
                                return "";
                            }
                        }

                        return super.filter(source, start, end, dest, dstart, dend);
                    }
                }
        });

    }
}

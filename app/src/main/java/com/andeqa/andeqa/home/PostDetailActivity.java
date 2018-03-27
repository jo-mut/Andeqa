package com.andeqa.andeqa.home;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.R;
import com.andeqa.andeqa.comments.CommentsActivity;
import com.andeqa.andeqa.market.DialogSendCredits;
import com.andeqa.andeqa.likes.LikesActivity;
import com.andeqa.andeqa.models.Andeqan;
import com.andeqa.andeqa.models.Balance;
import com.andeqa.andeqa.models.Single;
import com.andeqa.andeqa.models.Market;
import com.andeqa.andeqa.models.Credit;
import com.andeqa.andeqa.models.Like;
import com.andeqa.andeqa.models.Timeline;
import com.andeqa.andeqa.models.TransactionDetails;
import com.andeqa.andeqa.profile.ProfileActivity;
import com.andeqa.andeqa.utils.ProportionalImageView;
import com.andeqa.andeqa.likes.WhoLikedViewHolder;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.firebase.ui.firestore.ObservableSnapshotArray;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
import java.util.Date;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

import static android.util.Log.d;

public class PostDetailActivity extends AppCompatActivity implements View.OnClickListener{

    @Bind(R.id.usernameTextView)TextView mUsernameTextView;
    @Bind(R.id.postImageView)ProportionalImageView mPostImageView;
    @Bind(R.id.profileImageView)ImageView mProfileImageView;
    @Bind(R.id.titleTextView)TextView mCingleTitleTextView;
    @Bind(R.id.cingleTitleRelativeLayout)RelativeLayout mCingleTitleRelativeLayout;
    @Bind(R.id.descriptionRelativeLayout)RelativeLayout mDescriptionRelativeLayout;
    @Bind(R.id.descriptionTextView)TextView mDescriptionTextView;
    @Bind(R.id.tradeMethodTextView)TextView mTradeMethodTextView;
    @Bind(R.id.postOwnerTextView)TextView mPostOwnerTextView;
    @Bind(R.id.totalLikesCountTextView)TextView mTotalLikesCountTextView;
    @Bind(R.id.likesImageView)ImageView mLikesImageView;
    @Bind(R.id.dislikesImageView)ImageView mDislikeImageView;
    @Bind(R.id.likesPercentageTextView)TextView mLikesPercentageTextView;
    @Bind(R.id.dislikesPercentageTextView) TextView mDislikePercentageTextView;
    @Bind(R.id.commentsImageView)ImageView mCommentImageView;
    @Bind(R.id.commentsCountTextView)TextView mCommentCountTextView;
    @Bind(R.id.likesRecyclerView)RecyclerView mLikesRecyclerView;
    @Bind(R.id.buyPostButton)Button mBuyPostButton;
    @Bind(R.id.postSalePriceTextView)TextView mSalePriceTextView;
    @Bind(R.id.datePostedTextView)TextView mDatePostedTextView;
    @Bind(R.id.ownerImageView)CircleImageView mOwnerImageView;
    @Bind(R.id.editSalePriceImageView)ImageView mEditSalePriceImageView;
    @Bind(R.id.editSalePriceEditText)EditText mEditSalePriceEditText;
    @Bind(R.id.doneEditingImageView)ImageView mDoneEditingImageView;
    @Bind(R.id.salePriceProgressbar)ProgressBar mSalePriceProgressBar;
    @Bind(R.id.postSenseCreditsTextView)TextView mPoseSenseCreditsTextView;
    @Bind(R.id.postSalePriceRelativeLayout)RelativeLayout mPostSalePriceRelativeLayout;


    //firestore reference
    private FirebaseFirestore firebaseFirestore;
    private CollectionReference collecctionsCollection;
    private com.google.firebase.firestore.Query randomQuery;
    private com.google.firebase.firestore.Query commentsCountQuery;
    private CollectionReference usersReference;
    private CollectionReference commentsReference;
    private CollectionReference ownerReference;
    private CollectionReference senseCreditReference;
    private CollectionReference ifairReference;
    private CollectionReference likesReference;
    private CollectionReference postWalletReference;
    private CollectionReference timelineCollection;
    private com.google.firebase.firestore.Query likesQuery;
    //firebase references
    private DatabaseReference databaseReference;
    //firebase auth
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    //firestore adapter
    private FirestoreRecyclerAdapter firestoreRecyclerAdapter;
    //process likes
    private boolean processLikes = false;
    private boolean processDislikes = false;
    private static final double DEFAULT_PRICE = 1.5;
    private static final double GOLDEN_RATIO = 1.618;
    private String mPostId;
    private String mCollectionId;
    private static final String COLLECTION_ID = "collection id";
    private static final String EXTRA_POST_ID = "post id";
    private static final String EXTRA_USER_UID = "uid";
    private static final int MAX_WIDTH = 200;
    private static final int MAX_HEIGHT = 200;
    private static final String TAG = PostDetailActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);
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

        FirebaseFirestore.setLoggingEnabled(true);

        if (firebaseAuth.getCurrentUser()!= null){
            mPostId = getIntent().getStringExtra(EXTRA_POST_ID);
            if(mPostId == null){
                throw new IllegalArgumentException("pass a post id");
            }

            mCollectionId = getIntent().getStringExtra(COLLECTION_ID);
            if (mCollectionId == null){
                throw new IllegalArgumentException("pass a collection id");
            }
            //INITIALIASE CLICK LISTENER
            mLikesImageView.setOnClickListener(this);
            mLikesRecyclerView.setOnClickListener(this);
            mCommentImageView.setOnClickListener(this);
            mLikesPercentageTextView.setOnClickListener(this);
            mBuyPostButton.setOnClickListener(this);
            mPostImageView.setOnClickListener(this);
            mEditSalePriceImageView.setOnClickListener(this);
            mDoneEditingImageView.setOnClickListener(this);
            mDislikeImageView.setOnClickListener(this);
            mTotalLikesCountTextView.setOnClickListener(this);

            //firestore
            collecctionsCollection = FirebaseFirestore.getInstance().collection(Constants.COLLECTIONS)
                    .document("collection_posts").collection(mCollectionId);
            ownerReference = FirebaseFirestore.getInstance().collection(Constants.POST_OWNERS);
            ownerReference = FirebaseFirestore.getInstance().collection(Constants.POST_OWNERS);
            usersReference = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);
            commentsReference = FirebaseFirestore.getInstance().collection(Constants.COMMENTS);
            ifairReference = FirebaseFirestore.getInstance().collection(Constants.SELLING);
            randomQuery = FirebaseFirestore.getInstance().collection(Constants.COLLECTIONS)
                    .orderBy("randomNumber");
            senseCreditReference = FirebaseFirestore.getInstance().collection(Constants.SENSECREDITS);
            //firebase
            databaseReference = FirebaseDatabase.getInstance().getReference(Constants.RANDOM_PUSH_ID);
            commentsCountQuery = commentsReference;
            likesReference = FirebaseFirestore.getInstance().collection(Constants.LIKES);
            postWalletReference = FirebaseFirestore.getInstance().collection(Constants.POST_WALLET);
            timelineCollection = FirebaseFirestore.getInstance().collection(Constants.TIMELINE);

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
        senseCreditReference.document(mPostId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
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
                    mPoseSenseCreditsTextView.setText("SC" + " " + formatter.format(senseCredits));
                }else {
                    mPoseSenseCreditsTextView.setText("SC 0.00000000");
                }

            }
        });

        //get the number of commments in a cingle
        commentsCountQuery.orderBy("postId").whereEqualTo("pushId", mPostId)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (!documentSnapshots.isEmpty()){
                    final int commentsCount = documentSnapshots.size();
                    mCommentCountTextView.setText(commentsCount + "");
                }else {
                    mCommentCountTextView.setText("0");

         }

            }
        });

        collecctionsCollection.document(mPostId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (documentSnapshot.exists()){
                    final Single single = documentSnapshot.toObject(Single.class);
                    final String image = single.getImage();
                    final String uid = single.getUid();
                    final String title = single.getTitle();
                    final String description = single.getDescription();

                    //LAUCNH PROFILE IF ITS NOT DELETED ELSE CATCH THE EXCEPTION
                    mProfileImageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(PostDetailActivity.this, ProfileActivity.class);
                            intent.putExtra(PostDetailActivity.EXTRA_USER_UID, uid);
                            startActivity(intent);
                        }
                    });


                    //set the title of the single
                    if (title.equals("")){
                        mCingleTitleRelativeLayout.setVisibility(View.GONE);
                    }else {
                        mCingleTitleTextView.setText(title);
                    }

                    if (!TextUtils.isEmpty(single.getDescription())){
                        mDescriptionRelativeLayout.setVisibility(View.VISIBLE);
                        mDescriptionTextView.setText(description);
                    }


                    //set the single image
                    Picasso.with(PostDetailActivity.this)
                            .load(image)
                            .networkPolicy(NetworkPolicy.OFFLINE)
                            .into(mPostImageView, new Callback() {
                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onError() {
                                    Picasso.with(PostDetailActivity.this)
                                            .load(image)
                                            .into(mPostImageView);
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
                                final String username = cinggulan.getUsername();
                                final String profileImage = cinggulan.getProfileImage();

                                mUsernameTextView.setText(username);
                                Picasso.with(PostDetailActivity.this)
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
                                                Picasso.with(PostDetailActivity.this)
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

    /**Single can only be bought by someone else except for the owner of that cingle*/
    private void showBuyButton(){
        ifairReference.document(mPostId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (documentSnapshot.exists()){
                    final Market market = documentSnapshot.toObject(Market.class);
                    DecimalFormat formatter = new DecimalFormat("0.00000000");
                    mSalePriceTextView.setText("SC" + " " +
                            formatter.format(market.getSalePrice()));
                    mPostSalePriceRelativeLayout.setVisibility(View.VISIBLE);
                    ownerReference.document(mPostId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
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

                                if (documentSnapshot.exists()){
                                    if (firebaseAuth.getCurrentUser().getUid().equals(ownerUid)){
                                        mBuyPostButton.setVisibility(View.GONE);
                                    }else {
                                        mBuyPostButton.setVisibility(View.VISIBLE);
                                    }
                                }
                            }
                        }
                    });
                }else {
                    mPostSalePriceRelativeLayout.setVisibility(View.GONE);
                }
            }
        });


        likesReference.document(mPostId).collection("likes")
                .whereEqualTo("uid", firebaseAuth.getCurrentUser().getUid())
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                        if (e != null) {
                            Log.w(TAG, "Listen error", e);
                            return;
                        }

                        if (!documentSnapshots.isEmpty()){
                            mLikesImageView.setColorFilter(Color.RED);
                        }else {
                            mLikesImageView.setColorFilter(Color.BLACK);
                        }

                    }
                });

        likesReference.document(mPostId).collection("likes")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                        if (e != null) {
                            Log.w(TAG, "Listen error", e);
                            return;
                        }

                        if (!documentSnapshots.isEmpty()){
                            mTotalLikesCountTextView.setText(documentSnapshots.size() + " " + "Likes");
                        }else {
                            mTotalLikesCountTextView.setText("0" + " " + "Likes");
                        }

                    }
                });


        //calculate the percentage of likes to dislikes
        likesReference.document(mPostId).collection("dislikes")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot dislikesSnapshots, FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen error", e);
                            return;
                        }

                        if (!dislikesSnapshots.isEmpty()){
                            final int dislikes = dislikesSnapshots.size();
                            Log.d("dislikes count", dislikes + "");
                            likesReference.document(mPostId).collection("likes")
                                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                        @Override
                                        public void onEvent(QuerySnapshot likesSnapshots, FirebaseFirestoreException e) {
                                            if (e != null) {
                                                Log.w(TAG, "Listen error", e);
                                                return;
                                            }

                                            if (!likesSnapshots.isEmpty()){
                                                //calculate likes in percentage
                                                final int likes = likesSnapshots.size();
                                                Log.d("likes size", likes + "");
                                                final int likesPlusDislikes = likes + dislikes;
                                                Log.d("likes plus dislikes", likesPlusDislikes + "");
                                                final int percentLikes = 100 * likes/likesPlusDislikes;
                                                Log.d("likes percentage", percentLikes + "");
                                                final int roundedPercent = roundPercentage(percentLikes, 2);
                                                mLikesPercentageTextView.setText(roundedPercent + "%" + " " + "Likes");
                                            }else {
//                                        //calculate likes in percentage
                                                mLikesPercentageTextView.setText("0%" + " " + "Likes");
                                            }
                                        }
                                    });
                        }else {
                            final int dislikes = dislikesSnapshots.size();
                            Log.d("dislikes count", dislikes + "");
                            likesReference.document(mPostId).collection("likes")
                                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                        @Override
                                        public void onEvent(QuerySnapshot likesSnapshots, FirebaseFirestoreException e) {
                                            if (e != null) {
                                                Log.w(TAG, "Listen error", e);
                                                return;
                                            }

                                            if (!likesSnapshots.isEmpty()){
                                                //calculate likes in percentage
                                                final int likes = likesSnapshots.size();
                                                Log.d("likes size", likes + "");
                                                final int likesPlusDislikes = likes + dislikes;
                                                Log.d("likes plus dislikes", likesPlusDislikes + "");
                                                final int percentLikes = 100 * likes/likesPlusDislikes;
                                                Log.d("likes percentage", percentLikes + "");
                                                final int roundedPercent = roundPercentage(percentLikes, 2);
                                                mLikesPercentageTextView.setText(roundedPercent + "%" + " " + "Likes");
                                            }else {
                                                mLikesPercentageTextView.setText("0%" + " " + "Likes");
                                            }
                                        }
                                    });
                        }


                    }
                });

        //calculate the percentage of likes to dislikes
        likesReference.document(mPostId).collection("likes")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot likesSnapshots, FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen error", e);
                            return;
                        }

                        if (!likesSnapshots.isEmpty()){
                            final int likes = likesSnapshots.size();
                            Log.d("likes count size", likes + "");
                            likesReference.document(mPostId).collection("dislikes")
                                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                        @Override
                                        public void onEvent(QuerySnapshot dislikesSnapshots, FirebaseFirestoreException e) {
                                            if (e != null) {
                                                Log.w(TAG, "Listen error", e);
                                                return;
                                            }

                                            if (!dislikesSnapshots.isEmpty()){
                                                //calculate likes in percentage
                                                final int dislikes = dislikesSnapshots.size();
                                                Log.d("dislikes size", dislikes + "");
                                                final int likesPlusDislikes = likes + dislikes;
                                                Log.d("disikes plus dislikes", likesPlusDislikes + "");
                                                final int percentDislikes = 100 * dislikes/likesPlusDislikes;
                                                Log.d("dislikes percentage", percentDislikes + "");
                                                final int roundedPercent = roundPercentage(percentDislikes, 2);
                                                mDislikePercentageTextView.setText(roundedPercent + "%" + " " + "  Dislikes");
                                            }else {
                                                //calculate likes in percentage
                                                final int dislikes = dislikesSnapshots.size();
                                                Log.d("dislikes size", dislikes + "");
                                                final int likesPlusDislikes = likes + dislikes;
                                                Log.d("disikes plus dislikes", likesPlusDislikes + "");
                                                final int percentDislikes = 100 * dislikes/likesPlusDislikes;
                                                Log.d("dislikes percentage", percentDislikes + "");
                                                final int roundedPercent = roundPercentage(percentDislikes, 2);
                                                mDislikePercentageTextView.setText(roundedPercent + "%" + " " + "Dislikes");                                            }

                                        }
                                    });
                        }else {
                            final int likes = likesSnapshots.size();
                            Log.d("likes count size", likes + "");
                            likesReference.document(mPostId).collection("dislikes")
                                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                        @Override
                                        public void onEvent(QuerySnapshot dislikesSnapshots, FirebaseFirestoreException e) {
                                            if (e != null) {
                                                Log.w(TAG, "Listen error", e);
                                                return;
                                            }

                                            if (!dislikesSnapshots.isEmpty()){
                                                //calculate likes in percentage
                                                final int dislikes = dislikesSnapshots.size();
                                                Log.d("dislikes size", dislikes + "");
                                                final int likesPlusDislikes = likes + dislikes;
                                                Log.d("disikes plus dislikes", likesPlusDislikes + "");
                                                final int percentDislikes = 100 * dislikes/likesPlusDislikes;
                                                Log.d("dislikes percentage", percentDislikes + "");
                                                final int roundedPercent = roundPercentage(percentDislikes, 2);
                                                mDislikePercentageTextView.setText(roundedPercent + "%" + " " + "Dislikes");
                                            }else {
                                                mDislikePercentageTextView.setText("0%" + " " + "Dislikes");                                            }

                                        }
                                    });
                        }


                    }
                });

        mDislikeImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                processDislikes = true;
                likesReference.document(mPostId).collection("dislikes")
                        .whereEqualTo("uid", firebaseAuth.getCurrentUser().getUid())
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
                                        like.setUid(firebaseAuth.getCurrentUser().getUid());
                                        like.setPushId(firebaseAuth.getCurrentUser().getUid());
                                        likesReference.document(mPostId).collection("dislikes")
                                                .document(firebaseAuth.getCurrentUser().getUid()).set(like);
                                        processDislikes = false;
                                        mDislikeImageView.setColorFilter(Color.RED);

                                    }else {
                                        likesReference.document(mPostId).collection("dislikes")
                                                .document(firebaseAuth.getCurrentUser().getUid()).delete();
                                        processDislikes = false;
                                        mDislikeImageView.setColorFilter(Color.BLACK);

                                    }
                                }

                            }
                        });
            }
        });



        likesReference.document(mPostId).collection("likes").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (!documentSnapshots.isEmpty()){
                    if (documentSnapshots.size() > 0){
                        mLikesRecyclerView.setVisibility(View.VISIBLE);
                        likesQuery = likesReference.document(mPostId).collection("likes").orderBy("uid");
                        FirestoreRecyclerOptions<Like> options = new FirestoreRecyclerOptions.Builder<Like>()
                                .setQuery(likesQuery, Like.class)
                                .build();

                        firestoreRecyclerAdapter = new FirestoreRecyclerAdapter<Like, WhoLikedViewHolder>(options) {

                            @Override
                            protected void onBindViewHolder(final WhoLikedViewHolder holder, int position, Like model) {
                                holder.bindWhoLiked(getSnapshots().getSnapshot(position));
                                Like like = getSnapshots().getSnapshot(position).toObject(Like.class);
                                final String uid = like.getUid();

                                holder.whoLikedImageView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Intent intent = new Intent(PostDetailActivity.this, LikesActivity.class);
                                        intent.putExtra(PostDetailActivity.EXTRA_POST_ID, mPostId);
                                        startActivity(intent);
                                    }
                                });

                                //get the profile of the user who just liked
                                usersReference.document(uid).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                    @Override
                                    public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                                        if (e != null) {
                                            Log.w(TAG, "Listen error", e);
                                            return;
                                        }

                                        if (documentSnapshot.exists()){
                                            final Andeqan cinggulan = documentSnapshot.toObject(Andeqan.class);
                                            final String profileImage = cinggulan.getProfileImage();

                                            Picasso.with(PostDetailActivity.this)
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
                                                            Picasso.with(PostDetailActivity.this)
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

                        mLikesRecyclerView.setAdapter(firestoreRecyclerAdapter);
                        firestoreRecyclerAdapter.startListening();
                        mLikesRecyclerView.setHasFixedSize(false);
                        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(PostDetailActivity.this,
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


    }

    /**set the the text on buy button*/
    private void setTextOnButton(){
        ifairReference.document(mPostId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                if (documentSnapshot.exists()){
                    mTradeMethodTextView.setText("@Selling");
                }else {
                    mTradeMethodTextView.setText("@NotListed");
                    mBuyPostButton.setVisibility(View.GONE);
                }
            }
        });

    }

    /**display the price of the cingle*/
    private void setCingleInfo() {
        /**display the person who currently owns the cingle*/
        ownerReference.document(mPostId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
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
                                Andeqan cinggulan = documentSnapshot.toObject(Andeqan.class);
                                final String username = cinggulan.getUsername();
                                final String profileImage = cinggulan.getProfileImage();

                                mPostOwnerTextView.setText(username);
                                Picasso.with(PostDetailActivity.this)
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
                                                Picasso.with(PostDetailActivity.this)
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

                    mOwnerImageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(PostDetailActivity.this, ProfileActivity.class);
                            intent.putExtra(PostDetailActivity.EXTRA_USER_UID, ownerUid);
                            startActivity(intent);
                        }
                    });
                }
            }
        });

    }

    @Override
    public void onClick(View v){

        if (v == mCommentImageView){
            Intent intent = new Intent(PostDetailActivity.this, CommentsActivity.class);
            intent.putExtra(PostDetailActivity.EXTRA_POST_ID, mPostId);
            intent.putExtra(PostDetailActivity.COLLECTION_ID, mCollectionId);
            startActivity(intent);
        }

        if (v == mTotalLikesCountTextView){
            Intent intent = new Intent(PostDetailActivity.this, LikesActivity.class);
            intent.putExtra(PostDetailActivity.EXTRA_POST_ID, mPostId);
            startActivity(intent);
        }

        if (v == mPostImageView){
            Intent intent = new Intent(PostDetailActivity.this, FullImageViewActivity.class);
            intent.putExtra(PostDetailActivity.EXTRA_POST_ID, mPostId);
            intent.putExtra(PostDetailActivity.COLLECTION_ID, mCollectionId);
            startActivity(intent);
        }

        if (v == mBuyPostButton){
            Bundle bundle = new Bundle();
            bundle.putString(PostDetailActivity.EXTRA_POST_ID, mPostId);
            bundle.putString(PostDetailActivity.COLLECTION_ID, mCollectionId);
            FragmentManager fragmenManager = getSupportFragmentManager();
            DialogSendCredits dialogSendCredits = DialogSendCredits.newInstance("sens credits");
            dialogSendCredits.setArguments(bundle);
            dialogSendCredits.show(fragmenManager, "send credits fragment");
        }

        if (v == mDoneEditingImageView){
            setNewPrice();
        }

        if (v == mEditSalePriceImageView){
            mEditSalePriceEditText.setVisibility(View.VISIBLE);
            mSalePriceTextView.setVisibility(View.GONE);
            mEditSalePriceImageView.setVisibility(View.GONE);
            mDoneEditingImageView.setVisibility(View.VISIBLE);
        }

        if (v == mLikesImageView){
            displayPopupWindow(v);
            mLikesImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    processLikes = true;
                    likesReference.document(mPostId).collection("likes")
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
                                            likesReference.document(mPostId).collection("likes")
                                                    .document(firebaseAuth.getCurrentUser().getUid()).set(like)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    collecctionsCollection.document(mPostId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                                        @Override
                                                        public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                                                            if (e != null) {
                                                                Log.w(TAG, "Listen error", e);
                                                                return;
                                                            }


                                                            if (documentSnapshot.exists()){
                                                                Single single = documentSnapshot.toObject(Single.class);
                                                                final String uid = single.getUid();

                                                                final Timeline timeline = new Timeline();
                                                                final long time = new Date().getTime();

                                                                timelineCollection.document(uid).collection("timeline")
                                                                        .orderBy(firebaseAuth.getCurrentUser().getUid())
                                                                        .whereEqualTo("postKey", mPostId)
                                                                        .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                                                            @Override
                                                                            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                                                                                if (e != null) {
                                                                                    Log.w(TAG, "Listen error", e);
                                                                                    return;
                                                                                }


                                                                                if (documentSnapshots.isEmpty()){
                                                                                    final String postId = databaseReference.push().getKey();
                                                                                    timeline.setPushId(mPostId);
                                                                                    timeline.setTime(time);
                                                                                    timeline.setUid(firebaseAuth.getCurrentUser().getUid());
                                                                                    timeline.setType("like");
                                                                                    timeline.setPostId(postId);
                                                                                    timeline.setStatus("unRead");

                                                                                    if (uid.equals(firebaseAuth.getCurrentUser().getUid())){
                                                                                        //do nothing
                                                                                    }else {
                                                                                        timelineCollection.document(uid).collection("timeline")
                                                                                                .document(postId)
                                                                                                .set(timeline);
                                                                                    }
                                                                                }
                                                                            }
                                                                        });

                                                            }
                                                        }
                                                    });
                                                }
                                            });
                                            processLikes = false;
                                            mLikesImageView.setColorFilter(Color.RED);

                                        }else {
                                            likesReference.document(mPostId).collection("likes")
                                                    .document(firebaseAuth.getCurrentUser().getUid()).delete();
                                            processLikes = false;
                                            mLikesImageView.setColorFilter(Color.BLACK);

                                        }
                                    }

                                    likesReference.document(mPostId).collection("likes")
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
                                                            //get the new worth of Single price in Sen
                                                            final double cingleWorth = perfectionValue * likesPerMille * currentPrice;
                                                            //round of the worth of the post to 10 decimal number
                                                            final double finalPoints = round( cingleWorth, 10);

                                                            Log.d("final points", finalPoints + "");

                                                            postWalletReference.document(mPostId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
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

                                                                        senseCreditReference.document(mPostId).update("amount", totalSenseCredits);
                                                                    }else {
                                                                        Credit credit = new Credit();
                                                                        credit.setPushId(mPostId);
                                                                        credit.setAmount(finalPoints);
                                                                        credit.setUid(firebaseAuth.getCurrentUser().getUid());
                                                                        senseCreditReference.document(mPostId).set(credit);
                                                                        Log.d("new sense credits", finalPoints + "");
                                                                    }
                                                                }
                                                            });

                                                        }

                                                    }else {
                                                        final double finalPoints = 0.00;
                                                        Log.d("finalpoints <= 0", finalPoints + "");
                                                        postWalletReference.document(mPostId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
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

                                                                    senseCreditReference.document(mPostId).update("amount", totalSenseCredits);
                                                                }else {
                                                                    Credit credit = new Credit();
                                                                    credit.setPushId(mPostId);
                                                                    credit.setAmount(finalPoints);
                                                                    credit.setUid(firebaseAuth.getCurrentUser().getUid());
                                                                    senseCreditReference.document(mPostId).set(credit);
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

    private void showEditImageView(){
        ownerReference.document(mPostId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
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

            senseCreditReference.document(mPostId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                    if (e != null) {
                        Log.w(TAG, "Listen error", e);
                        return;
                    }

                    if (documentSnapshot.exists()){
                        final Credit credit = documentSnapshot.toObject(Credit.class);
                        final double senseCredits = credit.getAmount();
                        final DecimalFormat formatter = new DecimalFormat("0.00000000");
                        mPoseSenseCreditsTextView.setText("SC" + " " + formatter.format(senseCredits));

                        if (intSalePrice < senseCredits){
                            mEditSalePriceEditText.setError("Sale price is less than Single Sense Crdits!");
                        }else {
                            ifairReference.document(mPostId).update("salePrice", intSalePrice);

                        }
                    }
                }
            });

            mEditSalePriceEditText.setText("");
            mSalePriceTextView.setVisibility(View.VISIBLE);
            mDoneEditingImageView.setVisibility(View.GONE);
            mEditSalePriceImageView.setVisibility(View.VISIBLE);
            mEditSalePriceEditText.setVisibility(View.GONE);
        }
    }

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

    private static int roundPercentage(int value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.intValue();
    }

    private void displayPopupWindow(View anchorView) {
        PopupWindow popup = new PopupWindow(PostDetailActivity.this);
        View layout = getLayoutInflater().inflate(R.layout.popup_layout, null);

        TextView textView = (TextView) layout.findViewById(R.id.popupTextView);
        textView.setText("Like this post");

        popup.setContentView(layout);
        // Set content width and height
        popup.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        popup.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
        // Closes the popup window when touch outside of it - when looses focus
        popup.setOutsideTouchable(true);
        popup.setFocusable(true);
        // Show anchored to button
        popup.setBackgroundDrawable(new BitmapDrawable());
        popup.showAsDropDown(anchorView);
    }

}

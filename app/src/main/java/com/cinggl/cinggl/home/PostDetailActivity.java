package com.cinggl.cinggl.home;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
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
import com.cinggl.cinggl.comments.CommentsActivity;
import com.cinggl.cinggl.market.SendCreditsDialogFragment;
import com.cinggl.cinggl.likes.LikesActivity;
import com.cinggl.cinggl.models.Balance;
import com.cinggl.cinggl.models.Post;
import com.cinggl.cinggl.models.PostSale;
import com.cinggl.cinggl.models.Cinggulan;
import com.cinggl.cinggl.models.Credit;
import com.cinggl.cinggl.models.Like;
import com.cinggl.cinggl.models.TransactionDetails;
import com.cinggl.cinggl.people.FollowerProfileActivity;
import com.cinggl.cinggl.profile.PersonalProfileActivity;
import com.cinggl.cinggl.viewholders.WhoLikedViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.firebase.ui.firestore.ObservableSnapshotArray;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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

import butterknife.Bind;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

import static android.util.Log.d;
import static com.cinggl.cinggl.R.id.postImageView;
import static com.cinggl.cinggl.R.id.postOwnerTextView;
import static com.cinggl.cinggl.R.id.postSalePriceTextView;
import static com.cinggl.cinggl.R.id.postSenseCreditsTextView;
import static com.cinggl.cinggl.R.id.tradeMethodTextView;
import static com.cinggl.cinggl.R.id.commentsCountTextView;
import static com.cinggl.cinggl.R.id.likesCountTextView;
import static com.cinggl.cinggl.R.id.likesImageView;
import static com.cinggl.cinggl.R.id.likesRecyclerView;
import static com.cinggl.cinggl.R.id.ownerImageView;

public class PostDetailActivity extends AppCompatActivity implements View.OnClickListener{

    @Bind(R.id.usernameTextView)TextView mUsernameTextView;
    @Bind(postImageView)CircleImageView mCingleImageView;
    @Bind(R.id.creatorImageView)ImageView mProfileImageView;
    @Bind(R.id.lacedCingleImageView)ImageView mLacedCingleImageView;
    @Bind(R.id.titleTextView)TextView mCingleTitleTextView;
    @Bind(R.id.cingleTitleRelativeLayout)RelativeLayout mCingleTitleRelativeLayout;
    @Bind(R.id.cingleDescriptionRelativeLayout)RelativeLayout mCingleDescriptionRelatvieLayout;
    @Bind(R.id.descriptionTextView)TextView mCingleDescriptionTextView;
    @Bind(tradeMethodTextView)TextView mCingleTradeMethodTextView;
    @Bind(postOwnerTextView)TextView mCingleOwnerTextView;
    @Bind(likesImageView)ImageView mLikesImageView;
    @Bind(likesCountTextView)TextView mLikesCountTextView;
    @Bind(R.id.commentsImageView)ImageView mCommentImageView;
    @Bind(commentsCountTextView)TextView mCommentCountTextView;
    @Bind(likesRecyclerView)RecyclerView mLikesRecyclerView;
    @Bind(R.id.lacedCinglesRecyclerView)RecyclerView mLacedCinglesReyclerView;
    @Bind(postSenseCreditsTextView)TextView mCingleSenseCreditsTextView;
    @Bind(R.id.tradeCingleButton)Button mTradeCingleButton;
    @Bind(postSalePriceTextView)TextView mCingleSalePriceTextView;
    @Bind(R.id.datePostedTextView)TextView mDatePostedTextView;
    @Bind(ownerImageView)CircleImageView mOwnerImageView;
    @Bind(R.id.editSalePriceImageView)ImageView mEditSalePriceImageView;
    @Bind(R.id.editSalePriceEditText)EditText mEditSalePriceEditText;
    @Bind(R.id.doneEditingImageView)ImageView mDoneEditingImageView;
    @Bind(R.id.salePriceProgressbar)ProgressBar mSalePriceProgressBar;
    @Bind(R.id.postSalePriceTitleRelativeLayout)RelativeLayout mCingleSalePriceTitleRelativeLayout;


    //firestore reference
    private FirebaseFirestore firebaseFirestore;
    private CollectionReference cinglesReference;
    private com.google.firebase.firestore.Query randomQuery;
    private com.google.firebase.firestore.Query commentsCountQuery;
    private CollectionReference usersReference;
    private CollectionReference commentsReference;
    private CollectionReference ownerReference;
    private CollectionReference senseCreditReference;
    private CollectionReference ifairReference;
    private CollectionReference likesReference;
    private CollectionReference postWalletReference;
    private com.google.firebase.firestore.Query likesQuery;
    //firebase auth
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    //firestore adapter
    private FirestoreRecyclerAdapter firestoreRecyclerAdapter;
    private FirebaseRecyclerAdapter firebaseRecyclerAdapter;
    //process likes
    private boolean processLikes = false;
    private static final double DEFAULT_PRICE = 1.5;
    private static final double GOLDEN_RATIO = 1.618;
    private Context mContext;
    private String mPostKey;
    private static final String EXTRA_POST_KEY = "post key";
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
            ownerReference = FirebaseFirestore.getInstance().collection(Constants.CINGLE_ONWERS);
            usersReference = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);
            commentsReference = FirebaseFirestore.getInstance().collection(Constants.COMMENTS);
            ifairReference = FirebaseFirestore.getInstance().collection(Constants.IFAIR);
            randomQuery = FirebaseFirestore.getInstance().collection(Constants.POSTS)
                    .orderBy("randomNumber");
            senseCreditReference = FirebaseFirestore.getInstance().collection(Constants.SENSECREDITS);

            commentsCountQuery = commentsReference;
            likesReference = FirebaseFirestore.getInstance().collection(Constants.LIKES);
            postWalletReference = FirebaseFirestore.getInstance().collection(Constants.CINGLE_WALLET);



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
        ifairReference.document(mPostKey).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (documentSnapshot.exists()){
                    final PostSale postSale = documentSnapshot.toObject(PostSale.class);
                    DecimalFormat formatter = new DecimalFormat("0.00000000");
                    mCingleSalePriceTextView.setText("SC" + " " +
                            formatter.format(postSale.getSalePrice()));
                    mTradeCingleButton.setVisibility(View.VISIBLE);
                }else {
                    mCingleSalePriceTitleRelativeLayout.setVisibility(View.GONE);
                    mTradeCingleButton.setVisibility(View.GONE);
                }
            }
        });

        senseCreditReference.document(mPostKey).addSnapshotListener(new EventListener<DocumentSnapshot>() {
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
                    mCingleSenseCreditsTextView.setText("SC" + " " + formatter.format(senseCredits));

                }else {
                    mCingleSenseCreditsTextView.setText("SC 0.00000000");
                }

            }
        });

        //get the number of commments in a cingle
        commentsCountQuery.whereEqualTo("pushId", mPostKey).addSnapshotListener(new EventListener<QuerySnapshot>() {
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


        cinglesReference.document(mPostKey).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (documentSnapshot.exists()){
                    final Post post = documentSnapshot.toObject(Post.class);

                    final String image = post.getImage();
                    final String uid = post.getUid();
                    final String title = post.getTitle();
                    final String description = post.getDescription();
                    final String datePosted = post.getDatePosted();

                    //LAUCNH PROFILE IF ITS NOT DELETED ELSE CATCH THE EXCEPTION
                    mProfileImageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (uid.equals(firebaseAuth.getCurrentUser().getUid())){
                                Intent intent = new Intent(PostDetailActivity.this, PersonalProfileActivity.class);
                                startActivity(intent);
                            }else {
                                Intent intent = new Intent(PostDetailActivity.this, FollowerProfileActivity.class);
                                intent.putExtra(PostDetailActivity.EXTRA_USER_UID, uid);
                                startActivity(intent);
                            }
                        }
                    });


                    //set the title of the post
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

                    mDatePostedTextView.setText(datePosted);

                    //set the post image
                    Picasso.with(PostDetailActivity.this)
                            .load(image)
                            .networkPolicy(NetworkPolicy.OFFLINE)
                            .into(mCingleImageView, new Callback() {
                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onError() {
                                    Picasso.with(PostDetailActivity.this)
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
                                final Cinggulan cinggulan = documentSnapshot.toObject(Cinggulan.class);
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

    /**Post can only be bought by someone else except for the owner of that cingle*/
    private void showBuyButton(){
        ownerReference.document(mPostKey).addSnapshotListener(new EventListener<DocumentSnapshot>() {
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
                            mTradeCingleButton.setVisibility(View.GONE);
                        }else {
                            mTradeCingleButton.setVisibility(View.VISIBLE);
                        }
                    }
                }
            }
        });

        likesReference.document(mPostKey).collection("likes")
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

        likesReference.document(mPostKey).collection("likes")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                        if (e != null) {
                            Log.w(TAG, "Listen error", e);
                            return;
                        }

                        if (!documentSnapshots.isEmpty()){
                            mLikesCountTextView.setText(documentSnapshots.size() + " " + "Likes");
                        }else {
                            mLikesCountTextView.setText("0" + " " + "Likes");
                        }

                    }
                });


        likesReference.document(mPostKey).collection("likes").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (!documentSnapshots.isEmpty()){
                    if (documentSnapshots.size() > 0){
                        mLikesRecyclerView.setVisibility(View.VISIBLE);
                        likesQuery = likesReference.document(mPostKey).collection("likes").orderBy("uid");
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

                        mLikesRecyclerView.setAdapter(firestoreRecyclerAdapter);
                        firestoreRecyclerAdapter.startListening();
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


    }

    /**set the the text on buy button*/
    private void setTextOnButton(){
        ifairReference.document(mPostKey).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                if (documentSnapshot.exists()){
                    mCingleTradeMethodTextView.setText("@Selling");
                }else {
                    mCingleTradeMethodTextView.setText("@NotOnTrade");
                    mTradeCingleButton.setVisibility(View.GONE);
                }
            }
        });

    }

    /**display the price of the cingle*/
    private void setCingleInfo() {
        /**display the person who currently owns the cingle*/
        ownerReference.document(mPostKey).addSnapshotListener(new EventListener<DocumentSnapshot>() {
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
                                Cinggulan cinggulan = documentSnapshot.toObject(Cinggulan.class);
                                final String username = cinggulan.getUsername();
                                final String profileImage = cinggulan.getProfileImage();

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

                    mOwnerImageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if ((firebaseAuth.getCurrentUser().getUid()).equals(ownerUid)){
                                Intent intent = new Intent(mContext, PersonalProfileActivity.class);
                                intent.putExtra(PostDetailActivity.EXTRA_USER_UID, ownerUid);
                                mContext.startActivity(intent);
                                d("profile uid", firebaseAuth.getCurrentUser().getUid());
                            }else {
                                Intent intent = new Intent(mContext, FollowerProfileActivity.class);
                                intent.putExtra(PostDetailActivity.EXTRA_USER_UID, ownerUid);
                                d("follower uid", ownerUid);
                                mContext.startActivity(intent);
                            }
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
            intent.putExtra(PostDetailActivity.EXTRA_POST_KEY, mPostKey);
            startActivity(intent);
        }

        if (v == mLikesCountTextView){
            Intent intent = new Intent(PostDetailActivity.this, LikesActivity.class);
            intent.putExtra(PostDetailActivity.EXTRA_POST_KEY, mPostKey);
            startActivity(intent);
        }

        if (v == mCingleImageView){
            Intent intent = new Intent(PostDetailActivity.this, FullImageViewActivity.class);
            intent.putExtra(PostDetailActivity.EXTRA_POST_KEY, mPostKey);
            startActivity(intent);
        }

        if (v == mTradeCingleButton){
            Bundle bundle = new Bundle();
            bundle.putString(PostDetailActivity.EXTRA_POST_KEY, mPostKey);
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

        if (v == mLikesImageView){
            mLikesImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    processLikes = true;
                    likesReference.document(mPostKey).collection("likes")
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
                                            likesReference.document(mPostKey).collection("likes")
                                                    .document(firebaseAuth.getCurrentUser().getUid()).set(like);
                                            processLikes = false;
                                            mLikesImageView.setColorFilter(Color.RED);

                                        }else {
                                            likesReference.document(mPostKey).collection("likes")
                                                    .document(firebaseAuth.getCurrentUser().getUid()).delete();
                                            processLikes = false;
                                            mLikesImageView.setColorFilter(Color.BLACK);

                                        }
                                    }

                                    likesReference.document(mPostKey).collection("likes")
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

                                                            postWalletReference.document(mPostKey).addSnapshotListener(new EventListener<DocumentSnapshot>() {
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

                                                                        senseCreditReference.document(mPostKey).update("amount", totalSenseCredits);
                                                                    }else {
                                                                        Credit credit = new Credit();
                                                                        credit.setPushId(mPostKey);
                                                                        credit.setAmount(finalPoints);
                                                                        credit.setUid(firebaseAuth.getCurrentUser().getUid());
                                                                        senseCreditReference.document(mPostKey).set(credit);
                                                                        Log.d("new sense credits", finalPoints + "");
                                                                    }
                                                                }
                                                            });

                                                        }

                                                    }else {
                                                        final double finalPoints = 0.00;
                                                        Log.d("finalpoints <= 0", finalPoints + "");
                                                        postWalletReference.document(mPostKey).addSnapshotListener(new EventListener<DocumentSnapshot>() {
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

                                                                    senseCreditReference.document(mPostKey).update("amount", totalSenseCredits);
                                                                }else {
                                                                    Credit credit = new Credit();
                                                                    credit.setPushId(mPostKey);
                                                                    credit.setAmount(finalPoints);
                                                                    credit.setUid(firebaseAuth.getCurrentUser().getUid());
                                                                    senseCreditReference.document(mPostKey).set(credit);
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
        ownerReference.document(mPostKey).addSnapshotListener(new EventListener<DocumentSnapshot>() {
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
        mCingleSalePriceTextView.setVisibility(View.GONE);
        final String stringSalePrice = mEditSalePriceEditText.getText().toString().trim();
        if (stringSalePrice.equals("")){
            mEditSalePriceEditText.setError("Sale price is empty!");
        }else {
            final double intSalePrice = Double.parseDouble(stringSalePrice);

            senseCreditReference.document(mPostKey).addSnapshotListener(new EventListener<DocumentSnapshot>() {
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
                        mCingleSenseCreditsTextView.setText("SC" + " " + "" + formatter.format(senseCredits));

                        if (intSalePrice < senseCredits){
                            mEditSalePriceEditText.setError("Sale price is less than Post Sense Crdits!");
                        }else {
                            mSalePriceProgressBar.setVisibility(View.VISIBLE);
                            ifairReference.document(mPostKey).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                @Override
                                public void onEvent(final DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                                    if (e != null) {
                                        Log.w(TAG, "Listen error", e);
                                        return;
                                    }

                                    if (documentSnapshot.exists()){
                                        ifairReference.document(mPostKey).update("salePrice", intSalePrice)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                                final PostSale postSale = documentSnapshot.toObject(PostSale.class);
                                                mCingleSalePriceTextView.setText("SC" + " " + formatter
                                                        .format(postSale.getSalePrice()));
                                                mSalePriceProgressBar.setVisibility(View.GONE);
                                                mCingleSalePriceTextView.setVisibility(View.VISIBLE);
                                                mDoneEditingImageView.setVisibility(View.GONE);
                                                mEditSalePriceImageView.setVisibility(View.VISIBLE);
                                            }
                                        });

                                    }
                                }
                            });
                        }
                    }
                }
            });

            mEditSalePriceEditText.setText("");
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
}

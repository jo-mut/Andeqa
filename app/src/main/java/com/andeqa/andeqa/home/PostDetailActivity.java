package com.andeqa.andeqa.home;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.DigitsKeyListener;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.R;
import com.andeqa.andeqa.comments.CommentsActivity;
import com.andeqa.andeqa.market.DialogSendCredits;
import com.andeqa.andeqa.likes.LikesActivity;
import com.andeqa.andeqa.market.ListOnMarketActivity;
import com.andeqa.andeqa.market.RedeemCreditsActivity;
import com.andeqa.andeqa.market.SellingAdapter;
import com.andeqa.andeqa.models.Andeqan;
import com.andeqa.andeqa.models.Balance;
import com.andeqa.andeqa.models.CollectionPost;
import com.andeqa.andeqa.models.Market;
import com.andeqa.andeqa.models.Credit;
import com.andeqa.andeqa.models.Like;
import com.andeqa.andeqa.models.Timeline;
import com.andeqa.andeqa.models.TransactionDetails;
import com.andeqa.andeqa.profile.ProfileActivity;
import com.andeqa.andeqa.settings.DialogFragmentPostSettings;
import com.andeqa.andeqa.settings.DialogMarketPostSettings;
import com.andeqa.andeqa.utils.ProportionalImageView;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
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

import java.lang.ref.Reference;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.annotation.Nullable;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

import static android.util.Log.d;

public class PostDetailActivity extends AppCompatActivity implements View.OnClickListener{

    @Bind(R.id.usernameTextView)TextView mUsernameTextView;
    @Bind(R.id.postImageView)ProportionalImageView mPostImageView;
    @Bind(R.id.profileImageView)ImageView mProfileImageView;
    @Bind(R.id.titleTextView)TextView mCingleTitleTextView;
    @Bind(R.id.titleRelativeLayout)RelativeLayout mCingleTitleRelativeLayout;
    @Bind(R.id.descriptionRelativeLayout)RelativeLayout mDescriptionRelativeLayout;
    @Bind(R.id.descriptionTextView)TextView mDescriptionTextView;
    @Bind(R.id.postOwnerTextView)TextView mPostOwnerTextView;
    @Bind(R.id.likesCountTextView)TextView mLikesCountTextView;
    @Bind(R.id.dislikesCountTextView)TextView mDislikeCountTextView;
    @Bind(R.id.likesImageView)ImageView mLikesImageView;
    @Bind(R.id.dislikesImageView)ImageView mDislikeImageView;
    @Bind(R.id.commentsImageView)ImageView mCommentImageView;
    @Bind(R.id.commentsCountTextView)TextView mCommentCountTextView;
    @Bind(R.id.tradeButton)Button mTradeButton;
    @Bind(R.id.postPriceTextView)TextView mSalePriceTextView;
    @Bind(R.id.datePostedTextView)TextView mDatePostedTextView;
    @Bind(R.id.ownerImageView)CircleImageView mOwnerImageView;
    @Bind(R.id.editSalePriceImageView)ImageView mEditSalePriceImageView;
    @Bind(R.id.editSalePriceEditText)EditText mEditSalePriceEditText;
    @Bind(R.id.doneEditingImageView)ImageView mDoneEditingImageView;
    @Bind(R.id.salePriceProgressbar)ProgressBar mSalePriceProgressBar;
    @Bind(R.id.creditsTextView)TextView mCreditsTextView;
    @Bind(R.id.priceRelativeLayout)RelativeLayout mPriceRelativeLayout;

    //firestore reference
    private FirebaseFirestore firebaseFirestore;
    private CollectionReference postsCollections;
    private com.google.firebase.firestore.Query commentsCountQuery;
    private CollectionReference usersReference;
    private CollectionReference commentsReference;
    private CollectionReference postOwnersCollection;
    private CollectionReference senseCreditReference;
    private CollectionReference sellingCollection;
    private CollectionReference likesReference;
    private CollectionReference postWalletReference;
    private CollectionReference timelineCollection;
    private CollectionReference postsCollection;
    private CollectionReference collectionsPosts;
    private CollectionReference marketCollections;
    private CollectionReference postOnwersCollection;
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
    private ProgressDialog progressDialog;

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
            mCommentImageView.setOnClickListener(this);
            mTradeButton.setOnClickListener(this);
            mPostImageView.setOnClickListener(this);
            mEditSalePriceImageView.setOnClickListener(this);
            mDoneEditingImageView.setOnClickListener(this);
            mDislikeImageView.setOnClickListener(this);
            mLikesCountTextView.setOnClickListener(this);

            //firestore
            postsCollections = FirebaseFirestore.getInstance().collection(Constants.COLLECTIONS_POSTS)
                    .document("collections").collection(mCollectionId);
            postOwnersCollection = FirebaseFirestore.getInstance().collection(Constants.POST_OWNERS);
            usersReference = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);
            commentsReference = FirebaseFirestore.getInstance().collection(Constants.COMMENTS)
                    .document("post_ids").collection(mPostId);
            sellingCollection = FirebaseFirestore.getInstance().collection(Constants.SELLING);
            postsCollection = FirebaseFirestore.getInstance().collection(Constants.POSTS);
            collectionsPosts = FirebaseFirestore.getInstance().collection(Constants.COLLECTIONS_POSTS)
                    .document("collections").collection(mCollectionId);
            senseCreditReference = FirebaseFirestore.getInstance().collection(Constants.CREDITS);
            marketCollections = FirebaseFirestore.getInstance().collection(Constants.SELLING);
            postOnwersCollection = FirebaseFirestore.getInstance().collection(Constants.POST_OWNERS);


            senseCreditReference = FirebaseFirestore.getInstance().collection(Constants.CREDITS);
            //firebase
            databaseReference = FirebaseDatabase.getInstance().getReference(Constants.RANDOM_PUSH_ID);
            commentsCountQuery = commentsReference;
            likesReference = FirebaseFirestore.getInstance().collection(Constants.LIKES);
            postWalletReference = FirebaseFirestore.getInstance().collection(Constants.POST_WALLET);
            timelineCollection = FirebaseFirestore.getInstance().collection(Constants.TIMELINE);

            //RETRIEVE DATA FROM FIREBASE
            setCingleData();
            setCingleInfo();
            setEditTextFilter();
            showBuyButton();
            showEditImageView();
            deletePostDialog();
            deleteFinish();

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.post_settings, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings){
            Bundle bundle = new Bundle();
            bundle.putString(PostDetailActivity.EXTRA_POST_ID, mPostId);
            bundle.putString(PostDetailActivity.COLLECTION_ID, mCollectionId);
            FragmentManager fragmenManager = getSupportFragmentManager();
            DialogFragmentPostSettings dialogFragmentPostSettings =  DialogFragmentPostSettings.newInstance("post settings");
            dialogFragmentPostSettings.setArguments(bundle);
            dialogFragmentPostSettings.show(fragmenManager, "market post settings fragment");
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        postOnwersCollection.document(mPostId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (documentSnapshot.exists()){
                    TransactionDetails transactionDetails = documentSnapshot.toObject(TransactionDetails.class);
                    final String ownerUid = transactionDetails.getUser_id();
                    Log.d("owner uid", ownerUid);

                    if (!firebaseAuth.getCurrentUser().getUid().equals(ownerUid)){
                        menu.clear();
                    }
                }
            }
        });


        return super.onPrepareOptionsMenu(menu);
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
                    mCreditsTextView.setText("SC" + " " + formatter.format(senseCredits));
                }else {
                    mCreditsTextView.setText("SC 0.00000000");
                }

            }
        });

        //get the number of commments in a cingle
        commentsCountQuery.orderBy("comment_id").whereEqualTo("push_id", mPostId)
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

        postsCollections.document(mPostId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (documentSnapshot.exists()){
                    final CollectionPost collectionPost = documentSnapshot.toObject(CollectionPost.class);
                    final String image = collectionPost.getImage();
                    final String uid = collectionPost.getUser_id();
                    final String title = collectionPost.getTitle();

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

                    if (!TextUtils.isEmpty(collectionPost.getDescription())){
                        mDescriptionRelativeLayout.setVisibility(View.VISIBLE);
                        addReadLess(collectionPost.getDescription(),mDescriptionTextView);
                        addReadMore(collectionPost.getDescription(), mDescriptionTextView);
                    }


                    //get the current date
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

                    mDatePostedTextView.setText(simpleDateFormat.format(collectionPost.getTime()));


                    //set the single image
                    Picasso.with(PostDetailActivity.this)
                            .load(image)
                            .networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.image_place_holder)
                            .into(mPostImageView, new Callback() {
                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onError() {
                                    Picasso.with(PostDetailActivity.this)
                                            .load(image)
                                            .placeholder(R.drawable.image_place_holder)
                                            .into(mPostImageView);
                                }
                            });

                    sellingCollection.document(mPostId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                            if (e != null) {
                                Log.w(TAG, "Listen error", e);
                                return;
                            }

                            if (documentSnapshot.exists()){
                                final Market market = documentSnapshot.toObject(Market.class);
                                DecimalFormat formatter = new DecimalFormat("0.00000000");
                                mSalePriceTextView.setText("uC" + " " + formatter.format(market.getSale_price()));
                                mPriceRelativeLayout.setVisibility(View.VISIBLE);
                                postOwnersCollection.document(mPostId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                    @Override
                                    public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                                        if (e != null) {
                                            Log.w(TAG, "Listen error", e);
                                            return;
                                        }

                                        if (documentSnapshot.exists()){
                                            TransactionDetails transactionDetails = documentSnapshot.toObject(TransactionDetails.class);
                                            final String ownerUid = transactionDetails.getUser_id();
                                            Log.d("owner uid", ownerUid);

                                            if (documentSnapshot.exists()){
                                                if (firebaseAuth.getCurrentUser().getUid().equals(ownerUid)){
                                                    mTradeButton.setText("Unlist");
                                                   mTradeButton.setOnClickListener(new View.OnClickListener() {
                                                       @Override
                                                       public void onClick(View v) {
                                                           Bundle bundle = new Bundle();
                                                           bundle.putString(PostDetailActivity.EXTRA_POST_ID, mPostId);
                                                           bundle.putString(PostDetailActivity.COLLECTION_ID, mCollectionId);
                                                           FragmentManager fragmenManager = getSupportFragmentManager();
                                                           DialogMarketPostSettings dialogMarketPostSettings = DialogMarketPostSettings.newInstance("post settings");
                                                           dialogMarketPostSettings.setArguments(bundle);
                                                           dialogMarketPostSettings.show(fragmenManager, "market post settings fragment");
                                                       }
                                                   });
                                                }else {
                                                    mTradeButton.setText("Buy");
                                                   mTradeButton.setOnClickListener(new View.OnClickListener() {
                                                       @Override
                                                       public void onClick(View v) {
                                                           Bundle bundle = new Bundle();
                                                           bundle.putString(PostDetailActivity.EXTRA_POST_ID, mPostId);
                                                           bundle.putString(PostDetailActivity.COLLECTION_ID, mCollectionId);
                                                           FragmentManager fragmenManager = getSupportFragmentManager();
                                                           DialogSendCredits dialogSendCredits = DialogSendCredits.newInstance("sens credits");
                                                           dialogSendCredits.setArguments(bundle);
                                                           dialogSendCredits.show(fragmenManager, "send credits fragment");
                                                       }
                                                   });
                                                }
                                            }
                                        }
                                    }
                                });
                            }else if (firebaseAuth.getCurrentUser().getUid().equals(uid)) {
                                mTradeButton.setText("Sell");
                               mTradeButton.setOnClickListener(new View.OnClickListener() {
                                   @Override
                                   public void onClick(View v) {
                                       Intent intent = new Intent(PostDetailActivity.this, ListOnMarketActivity.class);
                                       intent.putExtra(PostDetailActivity.EXTRA_POST_ID, mPostId);
                                       intent.putExtra(PostDetailActivity.COLLECTION_ID, mCollectionId);
                                       startActivity(intent);
                                   }
                               });
                            }else {
                                mPriceRelativeLayout.setVisibility(View.GONE);
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
                                final Andeqan cinggulan = documentSnapshot.toObject(Andeqan.class);
                                final String username = cinggulan.getUsername();
                                final String profileImage = cinggulan.getProfile_image();

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

    /**show delete dialog*/
    public void deletePostDialog(){
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Deleting ...");
        progressDialog.setCancelable(false);
    }

    /**delete post method*/
    public void deleteFinish(){
        collectionsPosts.document(mPostId)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen error", e);
                            return;
                        }

                        if (!documentSnapshot.exists()){
                            finish();
                        }
                    }
                });

    }

    private void showBuyButton(){
        likesReference.document(mPostId).collection("likes")
                .whereEqualTo("user_id", firebaseAuth.getCurrentUser().getUid())
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
                            mLikesCountTextView.setText(documentSnapshots.size() + " ");
                        }else {
                            mLikesCountTextView.setText("0");
                        }

                    }
                });

        likesReference.document(mPostId).collection("dislikes")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                        if (e != null) {
                            Log.w(TAG, "Listen error", e);
                            return;
                        }

                        if (!documentSnapshots.isEmpty()){
                            mDislikeCountTextView.setText(documentSnapshots.size() + " ");
                        }else {
                            mDislikeCountTextView.setText("0");
                        }

                    }
                });



        mDislikeImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                processDislikes = true;
                likesReference.document(mPostId).collection("dislikes")
                        .whereEqualTo("user_id", firebaseAuth.getCurrentUser().getUid())
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
                                        like.setUser_id(firebaseAuth.getCurrentUser().getUid());
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

    }

    /**display the price of the cingle*/
    private void setCingleInfo() {
        /**display the person who currently owns the cingle*/
        postOwnersCollection.document(mPostId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (documentSnapshot.exists()) {
                    TransactionDetails transactionDetails = documentSnapshot.toObject(TransactionDetails.class);
                    final String ownerUid = transactionDetails.getUser_id();

                    usersReference.document(ownerUid).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                            if (e != null) {
                                Log.w(TAG, "Listen error", e);
                                return;
                            }


                            if (documentSnapshot.exists()) {
                                Andeqan cinggulan = documentSnapshot.toObject(Andeqan.class);
                                final String username = cinggulan.getUsername();
                                final String profileImage = cinggulan.getProfile_image();

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

        if (v == mLikesCountTextView){
            Intent intent = new Intent(PostDetailActivity.this, LikesActivity.class);
            intent.putExtra(PostDetailActivity.EXTRA_POST_ID, mPostId);
            startActivity(intent);
        }

        if (v == mPostImageView){
            Intent intent = new Intent(PostDetailActivity.this, ImageViewActivity.class);
            intent.putExtra(PostDetailActivity.EXTRA_POST_ID, mPostId);
            intent.putExtra(PostDetailActivity.COLLECTION_ID, mCollectionId);
            startActivity(intent);
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
//            displayPopupWindow(v);
            mLikesImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    processLikes = true;
                    likesReference.document(mPostId).collection("likes")
                            .whereEqualTo("user_id", firebaseAuth.getCurrentUser().getUid())
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
                                            like.setUser_id(firebaseAuth.getCurrentUser().getUid());
                                            likesReference.document(mPostId).collection("likes")
                                                    .document(firebaseAuth.getCurrentUser().getUid()).set(like)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    postsCollections.document(mPostId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                                        @Override
                                                        public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                                                            if (e != null) {
                                                                Log.w(TAG, "Listen error", e);
                                                                return;
                                                            }


                                                            if (documentSnapshot.exists()){
                                                                CollectionPost collectionPost = documentSnapshot.toObject(CollectionPost.class);
                                                                final String uid = collectionPost.getUser_id();

                                                                final Timeline timeline = new Timeline();
                                                                final long time = new Date().getTime();

                                                                timelineCollection.document(uid).collection("activities")
                                                                        .orderBy(firebaseAuth.getCurrentUser().getUid())
                                                                        .whereEqualTo("post_id", mPostId)
                                                                        .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                                                            @Override
                                                                            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                                                                                if (e != null) {
                                                                                    Log.w(TAG, "Listen error", e);
                                                                                    return;
                                                                                }


                                                                                if (documentSnapshots.isEmpty()){
                                                                                    final String activityId = databaseReference.push().getKey();
                                                                                    timeline.setPost_id(mPostId);
                                                                                    timeline.setTime(time);
                                                                                    timeline.setUser_id(firebaseAuth.getCurrentUser().getUid());
                                                                                    timeline.setType("like");
                                                                                    timeline.setActivity_id(activityId);
                                                                                    timeline.setStatus("un_read");
                                                                                    timeline.setReceiver_id(uid);


                                                                                    if (uid.equals(firebaseAuth.getCurrentUser().getUid())){
                                                                                        //do nothing
                                                                                    }else {
                                                                                        timelineCollection.document(uid).collection("activities")
                                                                                                .document(mPostId)
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
                                                                        final double amountRedeemed = balance.getAmount_redeemed();
                                                                        Log.d(amountRedeemed + "", "amount redeemed");
                                                                        final  double amountDeposited = balance.getAmount_deposited();
                                                                        Log.d(amountDeposited + "", "amount deposited");
                                                                        final double senseCredits = amountDeposited + finalPoints;
                                                                        Log.d("sense credit", senseCredits + "");
                                                                        final double totalSenseCredits = senseCredits - amountRedeemed;
                                                                        Log.d("total sense credit", totalSenseCredits + "");

                                                                        senseCreditReference.document(mPostId).update("amount", totalSenseCredits);
                                                                    }else {
                                                                        Credit credit = new Credit();
                                                                        credit.setPost_id(mPostId);
                                                                        credit.setAmount(finalPoints);
                                                                        credit.setUser_id(firebaseAuth.getCurrentUser().getUid());
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
                                                                    final double amountRedeemed = balance.getAmount_redeemed();
                                                                    Log.d(amountRedeemed + "", "amount redeemed");
                                                                    final  double amountDeposited = balance.getAmount_deposited();
                                                                    Log.d(amountDeposited + "", "amount deposited");
                                                                    final double senseCredits = amountDeposited + finalPoints;
                                                                    Log.d("sense credit", senseCredits + "");
                                                                    final double totalSenseCredits = senseCredits - amountRedeemed;
                                                                    Log.d("total sense credit", totalSenseCredits + "");

                                                                    senseCreditReference.document(mPostId).update("amount", totalSenseCredits);
                                                                }else {
                                                                    Credit credit = new Credit();
                                                                    credit.setPost_id(mPostId);
                                                                    credit.setAmount(finalPoints);
                                                                    credit.setUser_id(firebaseAuth.getCurrentUser().getUid());
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
        postOwnersCollection.document(mPostId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (documentSnapshot.exists()){

                    TransactionDetails transactionDetails = documentSnapshot.toObject(TransactionDetails.class);
                    final String ownerUid = transactionDetails.getUser_id();

                    if (firebaseAuth.getCurrentUser().getUid().equals(ownerUid)){
                        mEditSalePriceImageView.setVisibility(View.VISIBLE);
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
                        mCreditsTextView.setText("uC" + " " + formatter.format(senseCredits));

                        if (intSalePrice < senseCredits){
                            mEditSalePriceEditText.setError("Sale price is less than post's uCrdits!");
                        }else {
                            sellingCollection.document(mPostId).update("salePrice", intSalePrice);

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

//    private void displayPopupWindow(View anchorView) {
//        PopupWindow popup = new PopupWindow(PostDetailActivity.this);
//        View layout = getLayoutInflater().inflate(R.layout.popup_layout, null);
//
//        TextView textView = (TextView) layout.findViewById(R.id.popupTextView);
//        textView.setText("Like this post");
//
//        popup.setContentView(layout);
//        // Set content width and height
//        popup.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
//        popup.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
//        // Closes the popup window when touch outside of it - when looses focus
//        popup.setOutsideTouchable(true);
//        popup.setFocusable(true);
//        // Show anchored to button
//        popup.setBackgroundDrawable(new BitmapDrawable());
//        popup.showAsDropDown(anchorView);
//    }

    private void addReadMore(final String text, final TextView textView) {

        final String [] strings = text.split("");

        final int size = strings.length;

        if (size <= 120){
            //setence will not have read more
        }else {
            SpannableString ss = new SpannableString(text.substring(0, 119) + "...read more");
            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(View view) {
                    addReadLess(text, textView);
                }
                @Override
                public void updateDrawState(TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setUnderlineText(false);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        ds.setColor(getResources().getColor(R.color.colorPrimary, getTheme()));
                    } else {
                        ds.setColor(getResources().getColor(R.color.colorPrimary));
                    }
                }
            };
            ss.setSpan(clickableSpan, ss.length() - 10, ss.length() , Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            textView.setText(ss);
            textView.setMovementMethod(LinkMovementMethod.getInstance());
        }
    }

    private void addReadLess(final String text, final TextView textView) {
        final String [] strings = text.split("");

        final int size = strings.length;

        if (size > 120){
            SpannableString ss = new SpannableString(text + " read less");
            addReadMore(text, textView);

            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(View view) {
                    addReadMore(text, textView);
                }
                @Override
                public void updateDrawState(TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setUnderlineText(false);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        ds.setColor(getResources().getColor(R.color.colorPrimary, getTheme()));
                    } else {
                        ds.setColor(getResources().getColor(R.color.colorPrimary));
                    }
                }
            };
            ss.setSpan(clickableSpan, ss.length() - 10, ss.length() , Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            textView.setText(ss);
            textView.setMovementMethod(LinkMovementMethod.getInstance());
        }

    }


}

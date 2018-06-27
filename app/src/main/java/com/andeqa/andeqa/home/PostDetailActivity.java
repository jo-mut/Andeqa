package com.andeqa.andeqa.home;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.R;
import com.andeqa.andeqa.comments.CommentsActivity;
import com.andeqa.andeqa.likes.LikesActivity;
import com.andeqa.andeqa.models.Andeqan;
import com.andeqa.andeqa.models.CollectionPost;
import com.andeqa.andeqa.models.Credit;
import com.andeqa.andeqa.models.Like;
import com.andeqa.andeqa.models.Timeline;
import com.andeqa.andeqa.utils.ProportionalImageView;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Date;

import javax.annotation.Nullable;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

import static android.util.Log.d;

public class PostDetailActivity extends AppCompatActivity
        implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener{

    @Bind(R.id.usernameTextView)TextView mUsernameTextView;
    @Bind(R.id.postImageView)ProportionalImageView mPostImageView;
    @Bind(R.id.profileImageView)CircleImageView mProfileImageView;
    @Bind(R.id.titleTextView)TextView titleTextView;
    @Bind(R.id.titleRelativeLayout)RelativeLayout mTitleRelativeLayout;
    @Bind(R.id.descriptionRelativeLayout)RelativeLayout mDescriptionRelativeLayout;
    @Bind(R.id.descriptionTextView)TextView mDescriptionTextView;
    @Bind(R.id.likesCountTextView)TextView mLikesCountTextView;
//    @Bind(R.id.dislikesCountTextView)TextView mDislikeCountTextView;
    @Bind(R.id.likesImageView)ImageView mLikesImageView;
//    @Bind(R.id.dislikesImageView)ImageView mDislikeImageView;
    @Bind(R.id.commentsImageView)ImageView mCommentImageView;
    @Bind(R.id.commentsCountTextView)TextView mCommentCountTextView;
    @Bind(R.id.creditsTextView)TextView mCreditsTextView;
    @Bind(R.id.likesLinearLayout)LinearLayout mLikesLinearLayout;
    @Bind(R.id.settingsRelativeLayout)RelativeLayout mSettingsRelativeLayout;

    //firestore reference
    private FirebaseFirestore firebaseFirestore;
    private CollectionReference postsCollections;
    private com.google.firebase.firestore.Query commentsCountQuery;
    private CollectionReference usersReference;
    private CollectionReference commentsReference;
    private CollectionReference sellingCollection;
    private CollectionReference likesReference;
    private CollectionReference postWalletReference;
    private CollectionReference timelineCollection;
    private CollectionReference collectionsPosts;
    private CollectionReference marketCollections;
    private CollectionReference collectionsCollection;
    private CollectionReference creditsCollection;
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
    private String mUid;
    private String mType;
    private String mCollectionId;
    private static final String COLLECTION_ID = "collection id";
    private static final String EXTRA_POST_ID = "post id";
    private static final String EXTRA_USER_UID = "uid";
    private static final String TYPE = "type";
    private static final String EXTRA_URI = "uri";
    private static final String SOURCE = PostDetailActivity.class.getSimpleName();
    private static final int MAX_WIDTH = 200;
    private static final int MAX_HEIGHT = 200;
    private static final String TAG = PostDetailActivity.class.getSimpleName();
    private ProgressDialog progressDialog;
    private boolean showOnClick = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);
        ButterKnife.bind(this);

        firebaseAuth = FirebaseAuth.getInstance();

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationIcon(R.drawable.ic_arrow_black);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


        //INITIALIASE CLICK LISTENER
        mLikesImageView.setOnClickListener(this);
        mCommentImageView.setOnClickListener(this);
        mPostImageView.setOnClickListener(this);
//            mDislikeImageView.setOnClickListener(this);
        mLikesCountTextView.setOnClickListener(this);
        mLikesLinearLayout.setOnClickListener(this);
        mSettingsRelativeLayout.setOnClickListener(this);

        if (firebaseAuth.getCurrentUser()!= null){

            mPostId = getIntent().getStringExtra(EXTRA_POST_ID);
            mCollectionId = getIntent().getStringExtra(COLLECTION_ID);
            mUid = getIntent().getStringExtra(EXTRA_USER_UID);
            mType = getIntent().getStringExtra(TYPE);


            //firestore
            if (mType.equals("single")){
                collectionsPosts = FirebaseFirestore.getInstance().collection(Constants.COLLECTIONS_POSTS)
                        .document("singles").collection(mCollectionId);
                getSupportActionBar().setTitle("Single");
            }else{
                collectionsPosts = FirebaseFirestore.getInstance().collection(Constants.COLLECTIONS_POSTS)
                        .document("collections").collection(mCollectionId);
                getSupportActionBar().setTitle("Post");
            }

            usersReference = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);
            commentsReference = FirebaseFirestore.getInstance().collection(Constants.COMMENTS)
                    .document("post_ids").collection(mPostId);
            sellingCollection = FirebaseFirestore.getInstance().collection(Constants.SELLING);
            marketCollections = FirebaseFirestore.getInstance().collection(Constants.SELLING);
            collectionsCollection = FirebaseFirestore.getInstance().collection(Constants.USER_COLLECTIONS);
            //firebase
            databaseReference = FirebaseDatabase.getInstance().getReference(Constants.RANDOM_PUSH_ID);
            commentsCountQuery = commentsReference;
            likesReference = FirebaseFirestore.getInstance().collection(Constants.LIKES);
            postWalletReference = FirebaseFirestore.getInstance().collection(Constants.POST_WALLET);
            timelineCollection = FirebaseFirestore.getInstance().collection(Constants.TIMELINE);
            creditsCollection  = FirebaseFirestore.getInstance().collection(Constants.CREDITS);

        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        //RETRIEVE DATA FROM FIREBASE
        setPostInfo();
        deletePostDialog();
        finishActivity();
    }



    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**show delete dialog*/
    public void deletePostDialog(){
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Deleting ...");
        progressDialog.setCancelable(false);
    }

    /**finish activity if the post does not exist*/
    private void finishActivity(){
        collectionsPosts.document(mPostId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {

                if (e != null) {
                    android.util.Log.w(TAG, "Listen error", e);
                    return;
                }

                if (!documentSnapshot.exists()){
                    finish();
                }

            }
        });
    }


    /**display the price of the cingle*/
    private void setPostInfo() {

        likesReference.document(mPostId).collection("likes")
                .whereEqualTo("user_id", firebaseAuth.getCurrentUser().getUid())
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                        if (e != null) {
                            android.util.Log.w(TAG, "Listen error", e);
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
                            android.util.Log.w(TAG, "Listen error", e);
                            return;
                        }

                        if (!documentSnapshots.isEmpty()){
                            mLikesCountTextView.setText(documentSnapshots.size() + " ");
                        }else {
                            mLikesCountTextView.setText("0");
                        }

                    }
                });

//        likesReference.document(mPostId).collection("dislikes")
//                .addSnapshotListener(new EventListener<QuerySnapshot>() {
//                    @Override
//                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
//
//                        if (e != null) {
//                            android.util.Log.w(TAG, "Listen error", e);
//                            return;
//                        }
//
//                        if (!documentSnapshots.isEmpty()){
//                            mDislikeCountTextView.setText(documentSnapshots.size() + " ");
//                        }else {
//                            mDislikeCountTextView.setText("0");
//                        }
//
//                    }
//                });



//        mDislikeImageView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                processDislikes = true;
//                likesReference.document(mPostId).collection("dislikes")
//                        .whereEqualTo("user_id", firebaseAuth.getCurrentUser().getUid())
//                        .addSnapshotListener(new EventListener<QuerySnapshot>() {
//                            @Override
//                            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
//
//                                if (e != null) {
//                                    android.util.Log.w(TAG, "Listen error", e);
//                                    return;
//                                }
//
//
//                                if (processDislikes){
//                                    if (documentSnapshots.isEmpty()){
//                                        Like like = new Like();
//                                        like.setUser_id(firebaseAuth.getCurrentUser().getUid());
//                                        likesReference.document(mPostId).collection("dislikes")
//                                                .document(firebaseAuth.getCurrentUser().getUid()).set(like);
//                                        processDislikes = false;
//                                        mDislikeImageView.setColorFilter(Color.RED);
//
//                                    }else {
//                                        likesReference.document(mPostId).collection("dislikes")
//                                                .document(firebaseAuth.getCurrentUser().getUid()).delete();
//                                        processDislikes = false;
//                                        mDislikeImageView.setColorFilter(Color.BLACK);
//
//                                    }
//                                }
//
//                            }
//                        });
//            }
//        });

        creditsCollection.document(mPostId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
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
                    mCreditsTextView.setText("Credo" + " " + formatter.format(senseCredits));

                }else {
                    mCreditsTextView.setText("Credo" + " " + "0.00000000");
                }
            }
        });


        //get the number of commments in a cingle
        commentsCountQuery.orderBy("comment_id").whereEqualTo("push_id", mPostId)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                        if (e != null) {
                            android.util.Log.w(TAG, "Listen error", e);
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

        collectionsPosts.document(mPostId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                if (e != null) {
                    android.util.Log.w(TAG, "Listen error", e);
                    return;
                }

                if (documentSnapshot.exists()){
                    final CollectionPost collectionPost = documentSnapshot.toObject(CollectionPost.class);
                    final String image = collectionPost.getImage();
                    final String uid = collectionPost.getUser_id();
                    final String title = collectionPost.getTitle();

                    //set the title of the single
                    if (title.equals("")){
                        mTitleRelativeLayout.setVisibility(View.GONE);
                    }else {
                        mTitleRelativeLayout.setVisibility(View.VISIBLE);
                        titleTextView.setText(title);
                    }

                    if (!TextUtils.isEmpty(collectionPost.getDescription())){
                        final String [] strings = collectionPost.getDescription().split("");

                        final int size = strings.length;

                        if (size <= 120){
                            mDescriptionRelativeLayout.setVisibility(View.VISIBLE);
                            mDescriptionTextView.setText(collectionPost.getDescription());
                        }else{

                            mDescriptionRelativeLayout.setVisibility(View.VISIBLE);
                            final String boldMore = "...";
                            final String boldLess = "";
                            String normalText = collectionPost.getDescription().substring(0, 119);
                            mDescriptionTextView.setText(normalText + boldMore);
                            mDescriptionRelativeLayout.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (showOnClick){
                                        String normalText = collectionPost.getDescription();
                                        mDescriptionTextView.setText(normalText + boldLess);
                                        showOnClick = false;
                                    }else {
                                        String normalText = collectionPost.getDescription().substring(0, 119);
                                        mDescriptionTextView.setText(normalText + boldMore);
                                        showOnClick = true;
                                    }
                                }
                            });
                        }
                    }else {
                        mDescriptionRelativeLayout.setVisibility(View.GONE);
                    }


                    Log.d("post image", image);

                    //set the single image
                    Picasso.with(PostDetailActivity.this)
                            .load(Uri.parse(image))
                            .networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.image_place_holder)
                            .into(mPostImageView, new Callback() {
                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onError() {
                                    Picasso.with(PostDetailActivity.this)
                                            .load(Uri.parse(image))
                                            .placeholder(R.drawable.image_place_holder)
                                            .into(mPostImageView);
                                }
                            });


                    usersReference.document(uid).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                            if (e != null) {
                                android.util.Log.w(TAG, "Listen error", e);
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
                                        .placeholder(R.drawable.ic_user)
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
                                                        .placeholder(R.drawable.ic_user)
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


    public Uri getLocalBitmapUri(ImageView imageView) {
        // Extract Bitmap from ImageView drawable
        Drawable drawable = imageView.getDrawable();
        Bitmap bmp = null;
        if (drawable instanceof BitmapDrawable){
            bmp = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        } else {
            return null;
        }
        // Store image to default external storage directory
        Uri bmpUri = null;
        try {

            File file =  new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "share_image_" + System.currentTimeMillis() + ".png");
            FileOutputStream out = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.close();
            // wrap File object into a content provider. NOTE: authority here should match authority in manifest declaration
            bmpUri = FileProvider.getUriForFile(PostDetailActivity.this, "com.andeqa.andeqa", file);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return bmpUri;
    }

    @Override
    public void onClick(View v){

        if (v == mCommentImageView){
            Intent intent = new Intent(PostDetailActivity.this, CommentsActivity.class);
            intent.putExtra(PostDetailActivity.EXTRA_POST_ID, mPostId);
            intent.putExtra(PostDetailActivity.COLLECTION_ID, mCollectionId);
            intent.putExtra(PostDetailActivity.TYPE, mType);
            startActivity(intent);
        }

        if (v == mLikesLinearLayout){
            Intent intent = new Intent(PostDetailActivity.this, LikesActivity.class);
            intent.putExtra(PostDetailActivity.EXTRA_POST_ID, mPostId);
            startActivity(intent);
        }

        if (v == mPostImageView){
            Intent intent = new Intent(PostDetailActivity.this, ImageViewActivity.class);
            intent.putExtra(PostDetailActivity.EXTRA_POST_ID, mPostId);
            intent.putExtra(PostDetailActivity.COLLECTION_ID, mCollectionId);
            intent.putExtra(PostDetailActivity.TYPE, mType);
            startActivity(intent);
        }

        if (v == mSettingsRelativeLayout){

            final Uri bmpUri = getLocalBitmapUri(mPostImageView);
            if (bmpUri != null){
                Bundle bundle = new Bundle();
                bundle.putString(PostDetailActivity.EXTRA_POST_ID, mPostId);
                bundle.putString(PostDetailActivity.COLLECTION_ID, mCollectionId);
                bundle.putString(PostDetailActivity.TYPE, mType);
                bundle.putString(PostDetailActivity.EXTRA_URI, bmpUri.toString());
                ShareBottomFragment shareBottomFragment = ShareBottomFragment.newInstance();
                shareBottomFragment.setArguments(bundle);
                shareBottomFragment.show(getSupportFragmentManager(), "share bottom fragment");
            }

        }


        if (v == mLikesImageView){
            calculateValueOfLikes();
        }

//        if (v == mDislikeImageView){
//            calculateValueOfDislikes();
//        }
    }



    private void calculateValueOfDislikes(){
//        mDislikeImageView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                processDislikes = true;
//                likesReference.document(mPostId).collection("dislikes")
//                        .whereEqualTo("user_id", firebaseAuth.getCurrentUser().getUid())
//                        .addSnapshotListener(new EventListener<QuerySnapshot>() {
//                            @Override
//                            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
//
//                                if (e != null) {
//                                    Log.w(TAG, "Listen error", e);
//                                    return;
//                                }
//
//
//                                if (processDislikes){
//                                    if (documentSnapshots.isEmpty()){
//                                        Like like = new Like();
//                                        like.setUser_id(firebaseAuth.getCurrentUser().getUid());
//                                        likesReference.document(mPostId).collection("dislikes")
//                                                .document(firebaseAuth.getCurrentUser().getUid()).set(like);
//                                        processDislikes = false;
//                                        mDislikeImageView.setColorFilter(Color.RED);
//
//                                    }else {
//                                        likesReference.document(mPostId).collection("dislikes")
//                                                .document(firebaseAuth.getCurrentUser().getUid()).delete();
//                                        processDislikes = false;
//                                        mDislikeImageView.setColorFilter(Color.BLACK);
//
//                                    }
//
//                                    likesReference.document(mPostId).collection("dislikes")
//                                            .addSnapshotListener(new EventListener<QuerySnapshot>() {
//                                                @Override
//                                                public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
//
//                                                    if (e != null) {
//                                                        Log.w(TAG, "Listen error", e);
//                                                        return;
//                                                    }
//
//                                                    if (!documentSnapshots.isEmpty()){
//                                                        int likesCount = documentSnapshots.size();
//                                                        if ( likesCount > 0){
//                                                            //mille is a thousand likes
//                                                            double MILLE = 1000.0;
//                                                            //get the number of likes per a thousand likes
//                                                            double likesPerMille = likesCount/MILLE;
//                                                            //get the default rate of likes per unit time in seconds;
//                                                            double rateOfLike = 1000.0/1800.0;
//                                                            //get the current rate of likes per unit time in seconds;
//                                                            double currentRateOfLkes = likesCount * rateOfLike/MILLE;
//                                                            //get the current price of single
//                                                            final double currentPrice = currentRateOfLkes * DEFAULT_PRICE/rateOfLike;
//                                                            //get the perfection traceData of single's interactivity online
//                                                            double perfectionValue = GOLDEN_RATIO/likesCount;
//                                                            //get the new worth of Single price in Sen
//                                                            final double cingleWorth = perfectionValue * likesPerMille * currentPrice;
//                                                            //round of the worth of the single to 10 decimal number
//                                                            final double finalPoints = round( cingleWorth, 10);
//                                                            final double finalCredits = finalPoints * -1;
//
//                                                            postWalletReference.document(mPostId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
//                                                                @Override
//                                                                public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
//                                                                    if (e != null) {
//                                                                        Log.w(TAG, "Listen error", e);
//                                                                        return;
//                                                                    }
//
//
//                                                                    if (documentSnapshot.exists()){
//                                                                        final Wallet wallet = documentSnapshot.toObject(Wallet.class);
//                                                                        final double amountRedeemed = wallet.getRedeemed();
//                                                                        final  double amountDeposited = wallet.getDeposited();
//                                                                        final double credits = amountDeposited + finalCredits;
//                                                                        final double creditBalance = credits - amountRedeemed;
//
//                                                                        postWalletReference.document(mPostId).update("balance", creditBalance);
//                                                                        processLikes = false;
//                                                                    }else {
//
//                                                                        long time = new Date().getTime();
//                                                                        Wallet wallet = new Wallet();
//                                                                        wallet.setUser_id(mUid);
//                                                                        wallet.setAddress(mPostId);
//                                                                        wallet.setBalance(finalCredits);
//                                                                        wallet.setTime(time);
//                                                                        wallet.setDeposited(0.0);
//                                                                        wallet.setRedeemed(0.0);
//                                                                        postWalletReference.document(mPostId).update("balance", wallet);
//                                                                        processLikes = false;
//                                                                    }
//                                                                }
//                                                            });
//
//                                                        }
//                                                    }else {
//                                                        final double finalPoints = 0.00;
//                                                        final double finalCredits = finalPoints * -1;
//                                                        Log.d("finalpoints <= 0", finalPoints + "");
//
//                                                        postWalletReference.document(mPostId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
//                                                            @Override
//                                                            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
//                                                                if (e != null) {
//                                                                    Log.w(TAG, "Listen error", e);
//                                                                    return;
//                                                                }
//
//
//                                                                if (documentSnapshot.exists()){
//                                                                    final Wallet wallet = documentSnapshot.toObject(Wallet.class);
//                                                                    final double amountRedeemed = wallet.getRedeemed();
//                                                                    final  double amountDeposited = wallet.getDeposited();
//                                                                    final double credits = amountDeposited + finalCredits;
//                                                                    final double creditBalance = credits - amountRedeemed;
//
//                                                                    postWalletReference.document(mPostId).update("balance", creditBalance);
//                                                                    processLikes = false;
//                                                                }else {
//
//                                                                    long time = new Date().getTime();
//                                                                    Wallet wallet = new Wallet();
//                                                                    wallet.setUser_id(mUid);
//                                                                    wallet.setAddress(mPostId);
//                                                                    wallet.setBalance(finalCredits);
//                                                                    wallet.setTime(time);
//                                                                    wallet.setDeposited(0.0);
//                                                                    wallet.setRedeemed(0.0);
//                                                                    postWalletReference.document(mPostId).update("balance", wallet);
//                                                                    processLikes = false;
//                                                                }
//                                                            }
//                                                        });
//
//
//                                                    }
//                                                }
//                                            });
//
//                                }
//
//
//
//                            }
//                        });
//            }
//        });

    }

    private void calculateValueOfLikes(){
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
                                        final Like like = new Like();
                                        like.setUser_id(firebaseAuth.getCurrentUser().getUid());
                                        likesReference.document(mPostId).collection("likes")
                                                .document(firebaseAuth.getCurrentUser().getUid()).set(like);

                                        timelineCollection.document(mUid).collection("activities")
                                                .whereEqualTo("post_id", mPostId)
                                                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                                    @Override
                                                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                                                        if (e != null) {
                                                            Log.w(TAG, "Listen error", e);
                                                            return;
                                                        }


                                                        if (documentSnapshots.isEmpty()){
                                                            Log.d("timeline is empty", mPostId);
                                                            final Timeline timeline = new Timeline();
                                                            final long time = new Date().getTime();

                                                            final String activityId = databaseReference.push().getKey();
                                                            timeline.setPost_id(mPostId);
                                                            timeline.setTime(time);
                                                            timeline.setUser_id(firebaseAuth.getCurrentUser().getUid());
                                                            timeline.setType("like");
                                                            timeline.setActivity_id(activityId);
                                                            timeline.setStatus("un_read");
                                                            timeline.setReceiver_id(mUid);


                                                            if (mUid.equals(firebaseAuth.getCurrentUser().getUid())){
                                                                //do nothing
                                                            }else {
                                                                timelineCollection.document(mUid).collection("activities")
                                                                        .document(mPostId).set(timeline);
                                                            }
                                                        }
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
                                                        //get the current price of single
                                                        final double currentPrice = currentRateOfLkes * DEFAULT_PRICE/rateOfLike;
                                                        //get the perfection value of single's interactivity online
                                                        double perfectionValue = GOLDEN_RATIO/likesCount;
                                                        //get the new worth of Single price in Sen
                                                        final double cingleWorth = perfectionValue * likesPerMille * currentPrice;
                                                        //round of the worth of the single to 10 decimal number
                                                        final double finalPoints = round( cingleWorth, 8);

                                                        Log.d("finalpoints > 0", finalPoints + "");

                                                        postWalletReference.document(mPostId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                                            @Override
                                                            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                                                                if (e != null) {
                                                                    Log.w(TAG, "Listen error", e);
                                                                    return;
                                                                }


                                                                if (documentSnapshot.exists()){
                                                                    final Credit credit = documentSnapshot.toObject(Credit.class);
                                                                    final double amountRedeemed =   credit.getAmount();
                                                                    Log.d(amountRedeemed + "", "amount redeemed");
                                                                    final  double amountDeposited = credit.getDeposited();
                                                                    Log.d(amountDeposited + "", "amount deposited");
                                                                    final double senseCredits = amountDeposited + finalPoints;
                                                                    Log.d("sense credit", senseCredits + "");
                                                                    final double totalSenseCredits = senseCredits - amountRedeemed;
                                                                    Log.d("total sense credit", totalSenseCredits + "");

                                                                    creditsCollection.document(mPostId).update("amount", totalSenseCredits);
                                                                }else {
                                                                    Credit credit = new Credit();
                                                                    credit.setPost_id(mPostId);
                                                                    credit.setAmount(finalPoints);
                                                                    credit.setUser_id(firebaseAuth.getCurrentUser().getUid());
                                                                    credit.setDeposited(0.0);
                                                                    credit.setRedeemed(0.0);
                                                                    creditsCollection.document(mPostId).set(credit);
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
                                                                final Credit credit = documentSnapshot.toObject(Credit.class);
                                                                final double amountRedeemed =   credit.getAmount();
                                                                Log.d(amountRedeemed + "", "amount redeemed");
                                                                final  double amountDeposited = credit.getDeposited();
                                                                Log.d(amountDeposited + "", "amount deposited");
                                                                final double senseCredits = amountDeposited + finalPoints;
                                                                Log.d("sense credit", senseCredits + "");
                                                                final double totalSenseCredits = senseCredits - amountRedeemed;
                                                                Log.d("total sense credit", totalSenseCredits + "");

                                                                creditsCollection.document(mPostId).update("amount", totalSenseCredits);
                                                            }else {
                                                                Credit credit = new Credit();
                                                                credit.setPost_id(mPostId);
                                                                credit.setAmount(finalPoints);
                                                                credit.setUser_id(firebaseAuth.getCurrentUser().getUid());
                                                                credit.setDeposited(0.0);
                                                                credit.setRedeemed(0.0);
                                                                creditsCollection.document(mPostId).set(credit);
                                                                Log.d("new sense credits", finalPoints + "");
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


    //region listeners
    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
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

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }

}

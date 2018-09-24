package com.andeqa.andeqa.home;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
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
import android.widget.VideoView;

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.R;
import com.andeqa.andeqa.models.Andeqan;
import com.andeqa.andeqa.models.Credit;
import com.andeqa.andeqa.models.Like;
import com.andeqa.andeqa.models.Post;
import com.andeqa.andeqa.models.Timeline;
import com.andeqa.andeqa.player.Player;
import com.andeqa.andeqa.settings.PostSettingsFragment;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
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

public class VideoDetailActivity extends AppCompatActivity implements View.OnClickListener{

    @Bind(R.id.usernameTextView)TextView mUsernameTextView;
    @Bind(R.id.simpleExoPlayerView) SimpleExoPlayerView postVideoView;
    @Bind(R.id.profileImageView)CircleImageView mProfileImageView;
    @Bind(R.id.titleTextView)TextView titleTextView;
    @Bind(R.id.titleRelativeLayout)RelativeLayout mTitleRelativeLayout;
    @Bind(R.id.descriptionRelativeLayout)RelativeLayout mDescriptionRelativeLayout;
    @Bind(R.id.descriptionTextView)TextView mDescriptionTextView;
    @Bind(R.id.viewsCountTextView)TextView mViewsCountTextView;
    @Bind(R.id.viewsLinearLayout)LinearLayout mViewsLinearLayout;
    @Bind(R.id.viewsImageView)ImageView mViewsImageView;
    @Bind(R.id.creditsTextView)TextView mCreditsTextView;
    @Bind(R.id.settingsRelativeLayout)RelativeLayout mSettingsRelativeLayout;
    @Bind(R.id.creditsLinearLayout)LinearLayout mCreditLinearLayout;

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
    private CollectionReference viewsCollection;
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
    private boolean processCredit = false;
    private long startTime;
    private long stopTime;
    private long duration;
    private Player player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_detail);
        ButterKnife.bind(this);

        firebaseAuth = FirebaseAuth.getInstance();

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //Initialise click listners
        mSettingsRelativeLayout.setOnClickListener(this);

        mPostId = getIntent().getStringExtra(EXTRA_POST_ID);
        mCollectionId = getIntent().getStringExtra(COLLECTION_ID);
        mUid = getIntent().getStringExtra(EXTRA_USER_UID);
        mType = getIntent().getStringExtra(TYPE);

        if  (mType.equals("single_video_post")){
            collectionsPosts = FirebaseFirestore.getInstance().collection(Constants.COLLECTIONS_POSTS)
                    .document("singles").collection(mCollectionId);
        }else {
            collectionsPosts = FirebaseFirestore.getInstance().collection(Constants.COLLECTIONS_POSTS)
                    .document("collections").collection(mCollectionId);
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
        viewsCollection = FirebaseFirestore.getInstance().collection(Constants.VIEWS);
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
        player.releasePlayer();
        postVideoView.setPlayer(null);
    }

    @Override
    protected void onPause() {
        super.onPause();
        player.releasePlayer();
        postVideoView.setPlayer(null);
    }

    @Override
    protected void onResume() {
        super.onResume();
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

        creditsCollection.document(mPostId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (documentSnapshot.exists()){
                    mCreditLinearLayout.setVisibility(View.VISIBLE);
                    Credit credit = documentSnapshot.toObject(Credit.class);
                    final double senseCredits = credit.getAmount();
                    DecimalFormat formatter = new DecimalFormat("0.00000000");
                    mCreditsTextView.setText("Credo" + " " + formatter.format(senseCredits));
                }else {
                    mCreditLinearLayout.setVisibility(View.GONE);
                    mCreditsTextView.setText("Credo" + " " + "0.00000000");
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
                    final Post post = documentSnapshot.toObject(Post.class);
                    final String video = post.getUrl();
                    final String uid = post.getUser_id();
                    final String title = post.getTitle();

                    player = new Player(getApplicationContext(), postVideoView);
                    player.addMedia(post.getUrl());
                    postVideoView.getPlayer().setPlayWhenReady(true);

                    //set the title of the single
                    if (title.equals("")){
                        mTitleRelativeLayout.setVisibility(View.GONE);
                    }else {
                        mTitleRelativeLayout.setVisibility(View.VISIBLE);
                        titleTextView.setText(title);
                    }

                    if (!TextUtils.isEmpty(post.getDescription())){
                        final String [] strings = post.getDescription().split("");

                        final int size = strings.length;

                        if (size <= 120){
                            mDescriptionRelativeLayout.setVisibility(View.VISIBLE);
                            mDescriptionTextView.setText(post.getDescription());
                        }else{

                            mDescriptionRelativeLayout.setVisibility(View.VISIBLE);
                            final String boldMore = "...";
                            final String boldLess = "";
                            String normalText = post.getDescription().substring(0, 119);
                            mDescriptionTextView.setText(normalText + boldMore);
                            mDescriptionRelativeLayout.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (showOnClick){
                                        String normalText = post.getDescription();
                                        mDescriptionTextView.setText(normalText + boldLess);
                                        showOnClick = false;
                                    }else {
                                        String normalText = post.getDescription().substring(0, 119);
                                        mDescriptionTextView.setText(normalText + boldMore);
                                        showOnClick = true;
                                    }
                                }
                            });
                        }
                    }else {
                        mDescriptionRelativeLayout.setVisibility(View.GONE);
                    }

                    usersReference.document(uid).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                            if (e != null) {
                                android.util.Log.w(TAG, "Listen error", e);
                                return;
                            }

                            if (documentSnapshot.exists()){
                                final Andeqan andeqan = documentSnapshot.toObject(Andeqan.class);
                                final String username = andeqan.getUsername();
                                final String profileImage = andeqan.getProfile_image();

                                mUsernameTextView.setText(username);
                                Glide.with(getApplicationContext())
                                        .load(andeqan.getProfile_image())
                                        .apply(new RequestOptions()
                                                .placeholder(R.drawable.ic_user)
                                                .diskCacheStrategy(DiskCacheStrategy.DATA))
                                        .into(mProfileImageView);
                            }
                        }
                    });

                }
            }
        });


    }


    @Override
    public void onClick(View v){

        if (v == mSettingsRelativeLayout){
            Bundle bundle = new Bundle();
            bundle.putString(VideoDetailActivity.EXTRA_POST_ID, mPostId);
            bundle.putString(VideoDetailActivity.COLLECTION_ID, mCollectionId);
            bundle.putString(VideoDetailActivity.TYPE, mType);
            PostSettingsFragment postSettingsFragment = PostSettingsFragment.newInstance();
            postSettingsFragment.setArguments(bundle);
            postSettingsFragment.show(getSupportFragmentManager(), "share bottom fragment");

        }

    }


    //region listeners
    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }


//
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

}

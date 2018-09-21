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
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
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
import com.andeqa.andeqa.models.Andeqan;
import com.andeqa.andeqa.models.CollectionPost;
import com.andeqa.andeqa.models.Impression;
import com.andeqa.andeqa.models.Post;
import com.andeqa.andeqa.models.ViewDuration;
import com.andeqa.andeqa.models.Like;
import com.andeqa.andeqa.models.Timeline;
import com.andeqa.andeqa.settings.PostSettingsFragment;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
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
import java.util.Date;

import javax.annotation.Nullable;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

import static android.util.Log.d;

public class PostDetailActivity extends AppCompatActivity
        implements View.OnClickListener{
    @Bind(R.id.usernameTextView)TextView mUsernameTextView;
    @Bind(R.id.postImageView)ImageView postImageView;
    @Bind(R.id.topRelativeLayout)RelativeLayout topRelativeLayout;
    @Bind(R.id.bottomRelativeLayout)RelativeLayout bottomRelativeLayout;
    @Bind(R.id.postConstrantLayout)ConstraintLayout postConstraintLayout;
    @Bind(R.id.profileImageView)CircleImageView mProfileImageView;
    @Bind(R.id.titleTextView)TextView titleTextView;
    @Bind(R.id.titleRelativeLayout)RelativeLayout mTitleRelativeLayout;
    @Bind(R.id.descriptionRelativeLayout)RelativeLayout mDescriptionRelativeLayout;
    @Bind(R.id.descriptionTextView)TextView mDescriptionTextView;
    @Bind(R.id.viewsCountTextView)TextView mViewsCountTextView;
    @Bind(R.id.viewsLinearLayout)LinearLayout mViewsLinearLayout;
    @Bind(R.id.viewsImageView)ImageView mViewsImageView;
    @Bind(R.id.commentsImageView)ImageView mCommentImageView;
    @Bind(R.id.commentsCountTextView)TextView mCommentCountTextView;
    @Bind(R.id.creditsTextView)TextView mCreditsTextView;
    @Bind(R.id.settingsRelativeLayout)RelativeLayout mSettingsRelativeLayout;
    @Bind(R.id.creditsLinearLayout)LinearLayout mCreditLinearLayout;
    @Bind(R.id.itemLinearLayout)LinearLayout itemLinearLayout;

    //firestore reference
    private FirebaseFirestore firebaseFirestore;
    private CollectionReference postsCollections;
    private com.google.firebase.firestore.Query commentsCountQuery;
    private CollectionReference usersReference;
    private CollectionReference commentsReference;
    private CollectionReference collectionsPosts;
    private CollectionReference likesReference;
    private CollectionReference postWalletReference;
    private CollectionReference timelineCollection;
    private CollectionReference marketCollections;
    private CollectionReference collectionsCollection;
    private CollectionReference viewsCollection;
    //firebase database
    private DatabaseReference impressionReference;
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
    private String intentWidth;
    private String intentHeight;
    private static final String COLLECTION_ID = "collection id";
    private static final String EXTRA_POST_ID = "post id";
    private static final String EXTRA_USER_UID = "uid";
    private static final String TYPE = "type";
    private static final String EXTRA_URI = "uri";
    private static final String POST_HEIGHT = "height";
    private static final String POST_WIDTH = "width";
    private static final String SOURCE = PostDetailActivity.class.getSimpleName();
    private static final int MAX_WIDTH = 200;
    private static final int MAX_HEIGHT = 200;
    private static final String TAG = PostDetailActivity.class.getSimpleName();
    private ProgressDialog progressDialog;
    private boolean showOnClick = false;
    private ConstraintSet constraintSet;
    private boolean processCredit = false;
    private boolean processCompiledImpression = false;
    private boolean processOverallImpressions = false;
    private boolean processImpression = false;
    private long startTime;
    private long stopTime;
    private long duration;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);
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
        mCommentImageView.setOnClickListener(this);
        postImageView.setOnClickListener(this);
        mSettingsRelativeLayout.setOnClickListener(this);

        mPostId = getIntent().getStringExtra(EXTRA_POST_ID);
        mCollectionId = getIntent().getStringExtra(COLLECTION_ID);
        mUid = getIntent().getStringExtra(EXTRA_USER_UID);
        mType = getIntent().getStringExtra(TYPE);

        intentWidth = getIntent().getStringExtra(POST_WIDTH);
        intentHeight = getIntent().getStringExtra(POST_HEIGHT);
        if (intentHeight != null && intentWidth != null){
            final float width = (float) Integer.parseInt(intentWidth);
            final float height = (float) Integer.parseInt(intentHeight);
            float ratio = height/width;
            constraintSet = new ConstraintSet();
            constraintSet.clone(postConstraintLayout);
            constraintSet.setDimensionRatio(postImageView.getId(), "H," + ratio);
            constraintSet.applyTo(postConstraintLayout);
        }else {
            constraintSet = new ConstraintSet();
            constraintSet.clone(postConstraintLayout);
            constraintSet.setDimensionRatio(postImageView.getId(), "H," + 1);
            constraintSet.applyTo(postConstraintLayout);
        }

        startTime = System.currentTimeMillis();
        //firestore
        if (mType.equals("single") || mType.equals("single_image_post")){
            collectionsPosts = FirebaseFirestore.getInstance().collection(Constants.COLLECTIONS_POSTS)
                    .document("singles").collection(mCollectionId);
        }else {
            collectionsPosts = FirebaseFirestore.getInstance().collection(Constants.COLLECTIONS_POSTS)
                    .document("collections").collection(mCollectionId);
        }

        postsCollections = FirebaseFirestore.getInstance().collection(Constants.POSTS);
        usersReference = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);
        commentsReference = FirebaseFirestore.getInstance().collection(Constants.COMMENTS)
                .document("post_ids").collection(mPostId);
        marketCollections = FirebaseFirestore.getInstance().collection(Constants.SELLING);
        collectionsCollection = FirebaseFirestore.getInstance().collection(Constants.USER_COLLECTIONS);
        //firebase references
        impressionReference = FirebaseDatabase.getInstance().getReference(Constants.VIEWS);
        databaseReference = FirebaseDatabase.getInstance().getReference(Constants.RANDOM_PUSH_ID);
        impressionReference.keepSynced(true);
        //firestore references
        likesReference = FirebaseFirestore.getInstance().collection(Constants.LIKES);
        postWalletReference = FirebaseFirestore.getInstance().collection(Constants.POST_WALLET);
        timelineCollection = FirebaseFirestore.getInstance().collection(Constants.TIMELINE);
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

    }

    @Override
    protected void onPause() {
        super.onPause();
        stopTime = System.currentTimeMillis();
        duration = stopTime - startTime;
        final long time = System.currentTimeMillis();
        final String impressionId = databaseReference.child("generateId").getKey();
        processImpression = true;
        processCompiledImpression = true;
        processOverallImpressions = true;
        processImpression = true;
        if (processImpression){
            if (duration >= 5000){
                impressionReference.child("user_views").child(firebaseAuth.getCurrentUser().getUid())
                        .child(mPostId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (processOverallImpressions){
                            if (dataSnapshot.exists()){
                                ViewDuration viewDuration = dataSnapshot.getValue(ViewDuration.class);
                                final String type = viewDuration.getType();
                                final long recentDuration = viewDuration.getRecent_duration();
                                final long total_duration = viewDuration.getCompiled_duration();
                                final long newTotalDuration = total_duration + duration;
                                final long newRecentDuration = recentDuration + duration;
                                Log.d("recent duration", recentDuration + "");
                                Log.d("total duration", total_duration + "");
                                impressionReference.child("user_views").child(firebaseAuth.getCurrentUser().getUid())
                                        .child(mPostId).child("compiled_duration").setValue(newTotalDuration);
                                impressionReference.child("user_views").child(firebaseAuth.getCurrentUser().getUid())
                                        .child(mPostId).child("recent_duration").setValue(newRecentDuration)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                impressionReference.child("compiled_views").child(mPostId)
                                                        .addValueEventListener(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                if (processCompiledImpression){
                                                                    if (dataSnapshot.exists()){
                                                                        ViewDuration impress = dataSnapshot.getValue(ViewDuration.class);
                                                                        final long newDuration = impress.getCompiled_duration() + newRecentDuration;
                                                                        impressionReference.child("compiled_views").child(mPostId)
                                                                                .child("compiled_duration").setValue(newDuration);
                                                                        impressionReference.child("compiled_views").child(mPostId)
                                                                                .child("recent_duration").setValue(newRecentDuration);
                                                                        impressionReference.child("user_views").child(firebaseAuth.getCurrentUser().getUid())
                                                                                .child(mPostId).child("recent_duration").setValue(0);
                                                                        processCompiledImpression = false;
                                                                    }else {
                                                                        ViewDuration viewDuration = new ViewDuration();
                                                                        viewDuration.setCompiled_duration(newTotalDuration);
                                                                        viewDuration.setRecent_duration(newRecentDuration);
                                                                        viewDuration.setPost_id(mPostId);
                                                                        viewDuration.setUser_id(firebaseAuth.getCurrentUser().getUid());
                                                                        viewDuration.setImpression_id(impressionId);
                                                                        viewDuration.setTime(time);
                                                                        viewDuration.setType("compiled");
                                                                        impressionReference.child("compiled_views").child(mPostId)
                                                                                .setValue(viewDuration);
                                                                        processCompiledImpression = false;
                                                                    }
                                                                }
                                                            }

                                                            @Override
                                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                                            }
                                                        });
                                            }
                                        });
                                processOverallImpressions = false;

                            }else {
                                ViewDuration viewDuration = new ViewDuration();
                                viewDuration.setCompiled_duration(duration);
                                viewDuration.setRecent_duration(duration);
                                viewDuration.setPost_id(mPostId);
                                viewDuration.setUser_id(firebaseAuth.getCurrentUser().getUid());
                                viewDuration.setImpression_id(impressionId);
                                viewDuration.setTime(time);
                                viewDuration.setType("un_compiled");
                                impressionReference.child("user_views").child(firebaseAuth.getCurrentUser().getUid())
                                        .child(mPostId).setValue(viewDuration);
                                processOverallImpressions = false;
                            }
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }

            processImpression = false;
        }

        impressionReference.child("post_views").child(mPostId)
                .child(firebaseAuth.getCurrentUser().getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (!dataSnapshot.exists()){
                            Impression impression = new Impression();
                            impression.setTime(time);
                            impression.setImpression_id(impressionId);
                            impression.setUser_id(firebaseAuth.getCurrentUser().getUid());
                            impression.setPost_id(mPostId);
                            impressionReference.child("post_views").child(mPostId)
                                    .child(firebaseAuth.getCurrentUser().getUid())
                                    .setValue(impression);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
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
        postsCollections.document(mPostId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
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
//        //calculate the generated points from the compiled time
//        impressionReference.child("compiled_views").child(mPostId)
//                .addValueEventListener(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                        if (dataSnapshot.exists()){
//                            mCreditLinearLayout.setVisibility(View.VISIBLE);
//                            ViewDuration impression = dataSnapshot.getValue(ViewDuration.class);
//                            final long compiledDuration = impression.getCompiled_duration();
//                            //get seconds in milliseconds
//                            final long durationInSeconds = compiledDuration / 1000;
//                            //get the points generate
//                            final double points = durationInSeconds * 0.000015;
//                            DecimalFormat formatter = new DecimalFormat("0.000000");
//                            String pts = formatter.format(points);
//                            mCreditsTextView.setText(pts + " points");
//
//                        }else {
//                            mCreditLinearLayout.setVisibility(View.VISIBLE);
//                            final double points = 0.00;
//                            DecimalFormat formatter = new DecimalFormat("0.00");
//                            String pts = formatter.format(points);
//                            mCreditsTextView.setText(pts + " points");
//                        }
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                    }
//                });

        impressionReference.child("post_views").child(mPostId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            final long size = dataSnapshot.getChildrenCount();
                            int childrenCount = (int) size;
                            mViewsCountTextView.setText(childrenCount + "");
                        }else {
                            mViewsCountTextView.setText("0");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


        //get the number of commments in a cingle
        commentsReference.orderBy("comment_id").whereEqualTo("post_id", mPostId)
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

        postsCollections.document(mPostId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {

                if (e != null) {
                    android.util.Log.w(TAG, "Listen error", e);
                    return;
                }

                if (documentSnapshot.exists()){
                    final Post post = documentSnapshot.toObject(Post.class);
                    if (post.getUrl() == null){
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

                                    Glide.with(getApplicationContext())
                                            .load(image)
                                            .apply(new RequestOptions()
                                                    .diskCacheStrategy(DiskCacheStrategy.DATA))
                                            .into(postImageView);

                                    //set the title of the single
                                    if (TextUtils.isEmpty(title)){
                                        mTitleRelativeLayout.setVisibility(View.GONE);
                                    }else {
                                        mTitleRelativeLayout.setVisibility(View.VISIBLE);
                                        titleTextView.setText(title);
                                    }

                                    if (!TextUtils.isEmpty(collectionPost.getDescription())){
                                        final String [] strings = collectionPost.getDescription().split("");

                                        final int size = strings.length;

                                        if (size <= 150){
                                            mDescriptionRelativeLayout.setVisibility(View.VISIBLE);
                                            mDescriptionTextView.setText(collectionPost.getDescription());
                                        }else{

                                            mDescriptionRelativeLayout.setVisibility(View.VISIBLE);
                                            final String boldMore = "...";
                                            final String boldLess = "";
                                            String normalText = collectionPost.getDescription().substring(0, 149);
                                            mDescriptionTextView.setText(normalText + boldMore);
                                            mDescriptionRelativeLayout.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    if (showOnClick){
                                                        String normalText = collectionPost.getDescription();
                                                        mDescriptionTextView.setText(normalText + boldLess);
                                                        showOnClick = false;
                                                    }else {
                                                        String normalText = collectionPost.getDescription().substring(0, 149);
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
                                                final Andeqan cinggulan = documentSnapshot.toObject(Andeqan.class);
                                                final String username = cinggulan.getUsername();
                                                final String profileImage = cinggulan.getProfile_image();

                                                mUsernameTextView.setText(username);
                                                Glide.with(getApplicationContext())
                                                        .load(profileImage)
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
                    }else {
                        Glide.with(getApplicationContext())
                                .load(post.getUrl())
                                .apply(new RequestOptions()
                                        .diskCacheStrategy(DiskCacheStrategy.DATA))
                                .into(postImageView);

                        //set the title of the single
                        if (TextUtils.isEmpty(post.getTitle())){
                            mTitleRelativeLayout.setVisibility(View.GONE);
                        }else {
                            mTitleRelativeLayout.setVisibility(View.VISIBLE);
                            titleTextView.setText(post.getTitle());
                        }

                        if (!TextUtils.isEmpty(post.getDescription())){
                            final String [] strings = post.getDescription().split("");

                            final int size = strings.length;

                            if (size <= 150){
                                mDescriptionRelativeLayout.setVisibility(View.VISIBLE);
                                mDescriptionTextView.setText(post.getDescription());
                            }else{

                                mDescriptionRelativeLayout.setVisibility(View.VISIBLE);
                                final String boldMore = "...";
                                final String boldLess = "";
                                String normalText = post.getDescription().substring(0, 149);
                                mDescriptionTextView.setText(normalText + boldMore);
                                mDescriptionRelativeLayout.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if (showOnClick){
                                            String normalText = post.getDescription();
                                            mDescriptionTextView.setText(normalText + boldLess);
                                            showOnClick = false;
                                        }else {
                                            String normalText = post.getDescription().substring(0, 149);
                                            mDescriptionTextView.setText(normalText + boldMore);
                                            showOnClick = true;
                                        }
                                    }
                                });
                            }
                        }else {
                            mDescriptionRelativeLayout.setVisibility(View.GONE);
                        }

                        usersReference.document(post.getUser_id())
                                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
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
                                    Glide.with(getApplicationContext())
                                            .load(profileImage)
                                            .apply(new RequestOptions()
                                                    .placeholder(R.drawable.ic_user)
                                                    .diskCacheStrategy(DiskCacheStrategy.DATA))
                                            .into(mProfileImageView);
                                }
                            }
                        });
                    }
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
    public void onClick(View v) {

        if (v == mCommentImageView) {
            Intent intent = new Intent(PostDetailActivity.this, CommentsActivity.class);
            intent.putExtra(PostDetailActivity.EXTRA_POST_ID, mPostId);
            intent.putExtra(PostDetailActivity.COLLECTION_ID, mCollectionId);
            intent.putExtra(PostDetailActivity.TYPE, mType);
            startActivity(intent);
        }

        if (v == mCommentCountTextView) {
            Intent intent = new Intent(PostDetailActivity.this, CommentsActivity.class);
            intent.putExtra(PostDetailActivity.EXTRA_POST_ID, mPostId);
            intent.putExtra(PostDetailActivity.COLLECTION_ID, mCollectionId);
            intent.putExtra(PostDetailActivity.TYPE, mType);
            startActivity(intent);
        }

        if (v == postImageView) {
            Intent intent = new Intent(PostDetailActivity.this, ImageViewActivity.class);
            intent.putExtra(PostDetailActivity.EXTRA_POST_ID, mPostId);
            intent.putExtra(PostDetailActivity.COLLECTION_ID, mCollectionId);
            intent.putExtra(PostDetailActivity.TYPE, mType);
            startActivity(intent);
        }

        if (v == mSettingsRelativeLayout) {

            final Uri bmpUri = getLocalBitmapUri(postImageView);
            if (bmpUri != null) {
                Bundle bundle = new Bundle();
                bundle.putString(PostDetailActivity.EXTRA_POST_ID, mPostId);
                bundle.putString(PostDetailActivity.COLLECTION_ID, mCollectionId);
                bundle.putString(PostDetailActivity.TYPE, mType);
                bundle.putString(PostDetailActivity.EXTRA_URI, bmpUri.toString());
                PostSettingsFragment postSettingsFragment = PostSettingsFragment.newInstance();
                postSettingsFragment.setArguments(bundle);
                postSettingsFragment.show(getSupportFragmentManager(), "share bottom fragment");
            }

        }
    }


    //region listeners
    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }


}

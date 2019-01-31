package com.andeqa.andeqa.post_detail;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.andeqa.andeqa.R;
import com.andeqa.andeqa.models.Andeqan;
import com.andeqa.andeqa.models.Post;
import com.andeqa.andeqa.player.Player;
import com.andeqa.andeqa.utils.FirebaseUtil;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.math.BigDecimal;
import java.math.RoundingMode;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class VideoDetailActivity extends AppCompatActivity implements View.OnClickListener{

    @Bind(R.id.usernameTextView)TextView mUsernameTextView;
    @Bind(R.id.exoPlayerView) SimpleExoPlayerView exoPlayerView;
    @Bind(R.id.profileImageView)CircleImageView mProfileImageView;
    @Bind(R.id.titleTextView)TextView titleTextView;
    @Bind(R.id.titleRelativeLayout)RelativeLayout mTitleRelativeLayout;
    @Bind(R.id.descriptionRelativeLayout)RelativeLayout mDescriptionRelativeLayout;
    @Bind(R.id.descriptionTextView)TextView mDescriptionTextView;

    //firestore reference
    private String mPostId;
    private String mUid;
    private String mType;
    private String mCollectionId;
    private static final String COLLECTION_ID = "collection id";
    private static final String EXTRA_POST_ID = "post id";
    private static final String EXTRA_USER_UID = "uid";
    private static final String TYPE = "type";
    private static final String TAG = PostDetailActivity.class.getSimpleName();
    private ProgressDialog progressDialog;
    private boolean showOnClick = false;
    private Player player;
    private FirebaseUtil mFirebaseUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_detail);
        ButterKnife.bind(this);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        player = new Player(getApplicationContext(), exoPlayerView);
        mFirebaseUtil = new FirebaseUtil(this);

        //get intent extras
        mPostId = getIntent().getStringExtra(EXTRA_POST_ID);
        mCollectionId = getIntent().getStringExtra(COLLECTION_ID);
        mUid = getIntent().getStringExtra(EXTRA_USER_UID);
        mType = getIntent().getStringExtra(TYPE);


    }


    @Override
    protected void onStart() {
        super.onStart();
        //RETRIEVE DATA FROM FIREBASE
        setPostInfo();
        deletePostDialog();
    }

    @Override
    protected void onStop() {
        super.onStop();
        player.releasePlayer();
        exoPlayerView.setPlayer(null);
    }

    @Override
    protected void onPause() {
        super.onPause();

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


    /**display the price of the cingle*/
    private void setPostInfo() {
        mFirebaseUtil.postsPath().document(mPostId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
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

                    player.addMedia(post.getUrl());
                    if (exoPlayerView.getPlayer() != null) {
                        exoPlayerView.getPlayer().setPlayWhenReady(true);
                    }
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

                    mFirebaseUtil.usersPath().document(uid).addSnapshotListener(new EventListener<DocumentSnapshot>() {
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



    }


    //region listeners
    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

}

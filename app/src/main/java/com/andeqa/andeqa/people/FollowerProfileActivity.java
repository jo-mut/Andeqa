package com.andeqa.andeqa.people;

import android.content.Intent;
import android.os.Parcelable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.R;
import com.andeqa.andeqa.models.Room;
import com.andeqa.andeqa.models.Timeline;
import com.andeqa.andeqa.profile.ProfilePostsAdapter;
import com.andeqa.andeqa.message.MessagesAccountActivity;
import com.andeqa.andeqa.models.Post;
import com.andeqa.andeqa.models.Cinggulan;
import com.andeqa.andeqa.models.Relation;
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
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class FollowerProfileActivity extends AppCompatActivity
        implements View.OnClickListener{
    private static final String TAG = FollowerProfileActivity.class.getSimpleName();

    @Bind(R.id.profileCinglesRecyclerView)RecyclerView mProfileCinglesRecyclerView;
    @Bind(R.id.profileImageView)CircleImageView mProifleImageView;
    @Bind(R.id.fullNameTextView)TextView mFullNameTextView;
    @Bind(R.id.bioTextView)TextView mBioTextView;
    @Bind(R.id.followersCountTextView) TextView mFollowersCountTextView;
    @Bind(R.id.followingCountTextView)TextView mFollowingCountTextView;
    @Bind(R.id.postsCountTextView)TextView mCinglesCountTextView;
    @Bind(R.id.profileCoverImageView)ImageView mProfileCover;
    @Bind(R.id.followButton)Button mFollowButton;
    @Bind(R.id.collapsing_toolbar)CollapsingToolbarLayout collapsingToolbarLayout;
    @Bind(R.id.sendMessageImageView)ImageView mSendMessageImageView;

    private CollectionReference cinglesReference;
    private CollectionReference relationsReference;
    private CollectionReference usersReference;
    private CollectionReference timelineCollection;
    private CollectionReference messagesUsersCollection;
    private com.google.firebase.firestore.Query profileCinglesQuery;
    private Query profileCinglesCountQuery;
    //firebase
    private DatabaseReference databaseReference;
    //firebase auth
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    //firestore adapters
    private ProfilePostsAdapter profilePostsAdapter;
    private static final String KEY_LAYOUT_POSITION = "layout pooition";
    private Parcelable recyclerViewState;
    private  static final int MAX_WIDTH = 200;
    private static final int MAX_HEIGHT = 200;
    private String mUid;
    private static final String EXTRA_USER_UID = "uid";
    private static final String EXTRA_ROOM_UID = "roomId";
    //posts meber variables
    private List<Post> posts = new ArrayList<>();
    private List<String> cinglesIds = new ArrayList<>();
    private int TOTAL_ITEMS = 4;
    private DocumentSnapshot lastVisible;
    private boolean processFollow = false;
    private String roomId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follower_profile);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        //FIREBASE AUTH
        firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser()!= null){

            mUid = getIntent().getStringExtra(EXTRA_USER_UID);
            if(mUid == null){
                throw new IllegalArgumentException("pass an EXTRA_UID");
            }

            usersReference = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);
            relationsReference = FirebaseFirestore.getInstance().collection(Constants.RELATIONS);
            cinglesReference = FirebaseFirestore.getInstance().collection(Constants.POSTS);
            messagesUsersCollection = FirebaseFirestore.getInstance().collection(Constants.MESSAGES);
            profileCinglesQuery = cinglesReference.orderBy("timeStamp", Query.Direction.DESCENDING)
                    .whereEqualTo("uid", mUid);
            profileCinglesCountQuery = cinglesReference.whereEqualTo("uid", mUid);
            timelineCollection = FirebaseFirestore.getInstance().collection(Constants.TIMELINE);
            //firebase
            databaseReference = FirebaseDatabase.getInstance().getReference(Constants.RANDOM_PUSH_ID);

            fetchData();
            setTheFirstBacthProfileCingles();
            recyclerViewScrolling();

            //INITIALIZE CLICK LISTENERS
            mFollowersCountTextView.setOnClickListener(this);
            mFollowingCountTextView.setOnClickListener(this);
            mSendMessageImageView.setOnClickListener(this);
            mFollowButton.setOnClickListener(this);

        }

    }


    private void fetchData(){
        profileCinglesCountQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (!documentSnapshots.isEmpty()){
                    final int cingleCount = documentSnapshots.size();
                    mCinglesCountTextView.setText(cingleCount + "");
                }else {
                    mCinglesCountTextView.setText("0");
                }
            }
        });

        //get followers count
        relationsReference.document("followers")
                .collection(mUid)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                        if (e != null) {
                            Log.w(TAG, "Listen error", e);
                            return;
                        }


                        if (documentSnapshots.isEmpty()){
                            mFollowersCountTextView.setText("0");
                        }else {
                            mFollowersCountTextView.setText(documentSnapshots.size() + "");
                        }
                    }
                });

        //get following count
        relationsReference.document("following")
                .collection(mUid)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                        if (e != null) {
                            Log.w(TAG, "Listen error", e);
                            return;
                        }


                        if (documentSnapshots.isEmpty()){
                            mFollowingCountTextView.setText("0");
                        }else {
                            mFollowingCountTextView.setText(documentSnapshots.size() + "");
                        }
                    }
                });

        relationsReference.document("followers").collection(mUid)
                .whereEqualTo("uid", firebaseAuth.getCurrentUser().getUid())
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (documentSnapshots.isEmpty()){
                    mFollowButton.setText("FOLLOW");
                }else {
                    mFollowButton.setText("FOLLOWING");
                }
            }
        });


        usersReference.document(mUid)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                        if (e != null) {
                            Log.w(TAG, "Listen error", e);
                            return;
                        }

                        if (documentSnapshot.exists()){
                            final Cinggulan cinggulan = documentSnapshot.toObject(Cinggulan.class);
                            String username = cinggulan.getUsername();
                            String firstName = cinggulan.getFirstName();
                            String secondName = cinggulan.getSecondName();
                            final String profileImage = cinggulan.getProfileImage();
                            String bio = cinggulan.getBio();
                            final String profileCover = cinggulan.getProfileCover();

                            mFullNameTextView.setText(firstName + " " + secondName);
                            mBioTextView.setText(bio);

                            collapsingToolbarLayout.setTitle(username);

                            Picasso.with(FollowerProfileActivity.this)
                                    .load(profileImage)
                                    .resize(MAX_WIDTH, MAX_HEIGHT)
                                    .onlyScaleDown()
                                    .centerCrop()
                                    .placeholder(R.drawable.profle_image_background)
                                    .networkPolicy(NetworkPolicy.OFFLINE)
                                    .into(mProifleImageView, new Callback() {
                                        @Override
                                        public void onSuccess() {

                                        }

                                        @Override
                                        public void onError() {
                                            Picasso.with(FollowerProfileActivity.this)
                                                    .load(profileImage)
                                                    .resize(MAX_WIDTH, MAX_HEIGHT)
                                                    .onlyScaleDown()
                                                    .centerCrop()
                                                    .placeholder(R.drawable.profle_image_background)
                                                    .into(mProifleImageView);

                                        }
                                    });

                            Picasso.with(FollowerProfileActivity.this)
                                    .load(profileCover)
                                    .fit()
                                    .centerCrop()
                                    .networkPolicy(NetworkPolicy.OFFLINE)
                                    .into(mProfileCover, new Callback() {
                                        @Override
                                        public void onSuccess() {

                                        }

                                        @Override
                                        public void onError() {
                                            Picasso.with(FollowerProfileActivity.this)
                                                    .load(profileCover)
                                                    .fit()
                                                    .centerCrop()
                                                    .into(mProfileCover);


                                        }
                                    });
                        }
                    }
                });
    }

    private void setTheFirstBacthProfileCingles(){
        profileCinglesQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (!documentSnapshots.isEmpty()){
                    // RecyclerView
                    profilePostsAdapter = new ProfilePostsAdapter(profileCinglesQuery, FollowerProfileActivity.this);
                    profilePostsAdapter.startListening();
                    mProfileCinglesRecyclerView.setAdapter(profilePostsAdapter);
                    mProfileCinglesRecyclerView.setHasFixedSize(false);
                    LinearLayoutManager layoutManager = new LinearLayoutManager(FollowerProfileActivity.this);
                    layoutManager.setAutoMeasureEnabled(true);
                    mProfileCinglesRecyclerView.setLayoutManager(layoutManager);
                    mProfileCinglesRecyclerView.setNestedScrollingEnabled(false);

                }
            }
        });



    }


    private void onError(FirebaseFirestoreException e) {};

    private void onDataChanged() {}



    private void recyclerViewScrolling(){
        mProfileCinglesRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (!recyclerView.canScrollVertically(-1)) {
                    onScrolledToTop();
                } else if (!recyclerView.canScrollVertically(1)) {
                    onScrolledToBottom();
                } else if (dy < 0) {
                    onScrolledUp();
                } else if (dy > 0) {
                    onScrolledDown();
                }
            }
        });
    }

    public void onScrolledUp() {}

    public void onScrolledDown() {}

    public void onScrolledToTop() {

    }

    public void onScrolledToBottom() {

    }


    @Override
    public void onClick(View v){
        if (v == mFollowingCountTextView) {
            Intent intent = new Intent(FollowerProfileActivity.this, FollowingActivity.class);
            intent.putExtra(FollowerProfileActivity.EXTRA_USER_UID, mUid);
            startActivity(intent);
        }

        if (v == mSendMessageImageView){
            messagesUsersCollection.document("messaging users")
                    .collection(mUid).document(firebaseAuth.getCurrentUser().getUid())
                    .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                    if (e != null) {
                        Log.w(TAG, "Listen error", e);
                        return;
                    }

                    if (documentSnapshot.exists()){
                        Room room = documentSnapshot.toObject(Room.class);
                        roomId = room.getRoomId();
                        Intent intent = new Intent(FollowerProfileActivity.this, MessagesAccountActivity.class);
                        intent.putExtra(FollowerProfileActivity.EXTRA_ROOM_UID, roomId);
                        intent.putExtra(FollowerProfileActivity.EXTRA_USER_UID, mUid);
                        startActivity(intent);
                    }else {
                        roomId = databaseReference.push().getKey();
                        Intent intent = new Intent(FollowerProfileActivity.this, MessagesAccountActivity.class);
                        intent.putExtra(FollowerProfileActivity.EXTRA_ROOM_UID, roomId);
                        intent.putExtra(FollowerProfileActivity.EXTRA_USER_UID, mUid);
                        startActivity(intent);
                    }
                }
            });

        }

        if (v == mFollowersCountTextView){
            Intent intent = new Intent(FollowerProfileActivity.this, FollowersActivity.class);
            intent.putExtra(FollowerProfileActivity.EXTRA_USER_UID, mUid);
            startActivity(intent);
        }

        if (v == mFollowButton){
            processFollow = true;
            relationsReference.document("followers")
                    .collection(mUid)
                    .whereEqualTo("uid", firebaseAuth.getCurrentUser().getUid())
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {


                            if (e != null) {
                                Log.w(TAG, "Listen error", e);
                                return;
                            }

                            if (processFollow){
                                if (documentSnapshots.isEmpty()){
                                    //set followers and following
                                    Relation follower = new Relation();
                                    follower.setUid(firebaseAuth.getCurrentUser().getUid());
                                    relationsReference.document("followers").collection(mUid)
                                            .document(firebaseAuth.getCurrentUser().getUid()).set(follower)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Timeline timeline = new Timeline();
                                                    final long time = new Date().getTime();

                                                    final String postid = databaseReference.push().getKey();
                                                    timeline.setPushId(mUid);
                                                    timeline.setTimeStamp(time);
                                                    timeline.setUid(firebaseAuth.getCurrentUser().getUid());
                                                    timeline.setType("followers");
                                                    timeline.setPostId(postid);
                                                    timeline.setStatus("unRead");

                                                    timelineCollection.document(mUid).collection("timeline")
                                                            .document(firebaseAuth.getCurrentUser().getUid())
                                                            .set(timeline);
                                                }
                                            });
                                    final Relation following = new Relation();
                                    following.setUid(mUid);
                                    relationsReference.document("following").collection(firebaseAuth.getCurrentUser().getUid())
                                            .document(mUid).set(following);
                                    processFollow = false;
                                    mFollowButton.setText("Following");
                                }else {
                                    relationsReference.document("followers").collection(mUid)
                                            .document(firebaseAuth.getCurrentUser().getUid()).delete();
                                    relationsReference.document("following").collection(firebaseAuth.getCurrentUser().getUid())
                                            .document(mUid).delete();
                                    processFollow = false;
                                    mFollowButton.setText("Follow");
                                }
                            }
                        }
                    });
        }


    }

}
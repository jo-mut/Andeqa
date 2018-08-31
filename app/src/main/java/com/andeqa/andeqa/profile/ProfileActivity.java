package com.andeqa.andeqa.profile;

import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.R;
import com.andeqa.andeqa.creation.CreateCollectionActivity;
import com.andeqa.andeqa.creation.CreateCollectionPostActivity;
import com.andeqa.andeqa.message.MessagingActivity;
import com.andeqa.andeqa.models.Andeqan;
import com.andeqa.andeqa.models.Relation;
import com.andeqa.andeqa.models.Room;

import com.andeqa.andeqa.models.Timeline;
import com.andeqa.andeqa.people.FollowersActivity;
import com.andeqa.andeqa.people.FollowingActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Date;

import javax.annotation.Nullable;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener{
    //BIND VIEWS
    @Bind(R.id.profileImageView)CircleImageView mProifleImageView;
    @Bind(R.id.fullNameTextView)TextView mFullNameTextView;
    @Bind(R.id.bioTextView)TextView mBioTextView;
    @Bind(R.id.profileCoverImageView)ImageView mProfileCover;
    @Bind(R.id.sendMessageButton)Button mSendMessageButton;
    @Bind(R.id.followersCountTextView)TextView mFollowerCountTextView;
    @Bind(R.id.followingCountTextView)TextView mFollowingCountTextView;
    @Bind(R.id.followButton)Button followButton;
    @Bind(R.id.followRelativeLayout)RelativeLayout mFollowRelativeLayout;
    @Bind(R.id.postsCountTextView)TextView mPostCountTextView;
    @Bind(R.id.collectionCountTextView)TextView mCollectionsCountTextView;
    @Bind(R.id.connectCardView)CardView mConnectCardView;
    @Bind(R.id.postCardView)CardView mPostsCardView;
    @Bind(R.id.collectionsCardView) CardView mCollectionsCardView;
    @Bind(R.id.addPostImageView)ImageView addPostImageView;
    @Bind(R.id.addCollectionImageView)ImageView addCollectionImageView;
    @Bind(R.id.post_container)FrameLayout mPostContainerFrameLayout;
    @Bind(R.id.collection_container) FrameLayout mCollectionsContainerFrameLayout;
    @Bind(R.id.createPostRelativeLayout)RelativeLayout mCreatePostRelativeLayout;
    @Bind(R.id.createCollectionsRelativeLayout)RelativeLayout mCreateCollectionRelativeLayout;
    @Bind(R.id.viewPostRelativelayout)RelativeLayout mViewPostRelativeLayout;
    @Bind(R.id.viewCollectionsRelativeLayout)RelativeLayout mViewCollectionsRelativeLayout;

    private static final String TAG = ProfileActivity.class.getSimpleName();
    //firestore reference
    private CollectionReference collectionCollection;
    private CollectionReference usersCollections;
    private CollectionReference roomsCollection;
    private CollectionReference postsCollection;
    private CollectionReference peopleCollection;
    private CollectionReference followingCollection;
    private CollectionReference timelineCollection;
    private Query followersQuery;
    private Query followingQuery;
    private Query followQuery;
    //firebase
    private DatabaseReference databaseReference;
    //firebase auth
    private FirebaseAuth firebaseAuth;
    //firestore adapters
    private  static final int MAX_WIDTH = 200;
    private static final int MAX_HEIGHT = 200;
    //singles meber variables
    private static final String EXTRA_USER_UID = "uid";
    private static final String EXTRA_ROOM_UID = "roomId";
    private String mUid;
    private boolean processFollow = false;
    private String roomId;
    private boolean processRoom = false;
    private FragmentManager fragmentManager = getSupportFragmentManager();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        ButterKnife.bind(this);

        //FIREBASE AUTH
        firebaseAuth = FirebaseAuth.getInstance();

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationIcon(R.drawable.ic_arrow);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        if (firebaseAuth.getCurrentUser()!= null){

            mUid = getIntent().getStringExtra(EXTRA_USER_UID);
            usersCollections = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);
            collectionCollection = FirebaseFirestore.getInstance().collection(Constants.USER_COLLECTIONS);
            roomsCollection = FirebaseFirestore.getInstance().collection(Constants.MESSAGES);
            postsCollection = FirebaseFirestore.getInstance().collection(Constants.POSTS);
            peopleCollection = FirebaseFirestore.getInstance().collection(Constants.PEOPLE);
            followersQuery = peopleCollection.document("followers")
                    .collection(mUid);
            followingQuery = peopleCollection.document("following")
                    .collection(mUid);
            followQuery = peopleCollection.document("followers")
                    .collection(mUid);
            //firebase
            databaseReference = FirebaseDatabase.getInstance().getReference(Constants.RANDOM_PUSH_ID);
            timelineCollection = FirebaseFirestore.getInstance().collection(Constants.TIMELINE);
            fetchData();

            //show hidden views
            if (!firebaseAuth.getCurrentUser().getUid().equals(mUid)){
                mConnectCardView.setVisibility(View.VISIBLE);
            }else {
                addPostImageView.setVisibility(View.VISIBLE);
                addCollectionImageView.setVisibility(View.VISIBLE);
            }

            usersCollections.document(mUid).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                    if (e != null) {
                        Log.w(TAG, "Listen error", e);
                        return;
                    }

                    if (documentSnapshot.exists()){
                        Andeqan andeqan = documentSnapshot.toObject(Andeqan.class);
                        if (firebaseAuth.getCurrentUser().getUid().equals(mUid)){
                            toolbar.setTitle("Profile");
                        }else {
                            toolbar.getMenu().clear();
                            toolbar.setTitle(andeqan.getUsername());
                        }
                    }

                }
            });

            /**initialize click listners*/
            mSendMessageButton.setOnClickListener(this);
            followButton.setOnClickListener(this);
            mViewPostRelativeLayout.setOnClickListener(this);
            mViewCollectionsRelativeLayout.setOnClickListener(this);
            mFollowerCountTextView.setOnClickListener(this);
            mFollowingCountTextView.setOnClickListener(this);
            addCollectionImageView.setOnClickListener(this);
            addPostImageView.setOnClickListener(this);

            PostsFragment postsFragment = new PostsFragment();
            fragmentManager = this.getSupportFragmentManager();
            FragmentTransaction ft = fragmentManager.beginTransaction();
            ft.replace(R.id.post_container, postsFragment);
            ft.commit();

            ProfileCollectionFragment profileCollectionFragment = new ProfileCollectionFragment();
            fragmentManager = this.getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.collection_container, profileCollectionFragment);
            transaction.commit();
        }

    }


    private void fetchData(){
        followQuery.orderBy("time").whereEqualTo("following_id",
                firebaseAuth.getCurrentUser().getUid())
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot documentSnapshots,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (!documentSnapshots.isEmpty()){
                    followButton.setText("Following");
                }else {
                    followButton.setText("Follow");
                }
            }
        });

        followingQuery.orderBy("time").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot documentSnapshots,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (!documentSnapshots.isEmpty()){
                    final int count = documentSnapshots.size();
                    if (count > 0){
                        mFollowingCountTextView.setText(count + " following");
                    }
                }else {
                    mFollowingCountTextView.setText("0 following");
                }
            }
        });

        followersQuery.orderBy("time").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot documentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (!documentSnapshots.isEmpty()){
                    final int count = documentSnapshots.size();
                    if (count > 0){
                        mFollowerCountTextView.setText(count + " followers");
                    }
                }else {
                    mFollowerCountTextView.setText("0 followers");
                }
            }
        });

        usersCollections.document(mUid)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                        if (e != null) {
                            Log.w(TAG, "Listen error", e);
                            return;
                        }

                        if (documentSnapshot.exists()){
                            final Andeqan andeqan = documentSnapshot.toObject(Andeqan.class);
                            String firstName = andeqan.getFirst_name();
                            String secondName = andeqan.getSecond_name();
                            String username = andeqan.getUsername();
                            final String profileImage = andeqan.getProfile_image();
                            String bio = andeqan.getBio();
                            final String profileCover = andeqan.getProfile_cover();

                            mFullNameTextView.setText(firstName + " " + secondName);

                            if (TextUtils.isEmpty(bio)){
                                if (firebaseAuth.getCurrentUser().getUid().equals(mUid)){
                                    mBioTextView.setText(username + " you can add bio line to your profile");
                                }else {
                                    mBioTextView.setText("Your are looking at " + username +"'s" + " profile");
                                }
                            }else {
                                mBioTextView.setText(bio);
                            }

                            Glide.with(getApplicationContext())
                                    .load(profileImage)
                                    .apply(new RequestOptions()
                                            .placeholder(R.drawable.ic_user_white)
                                            .diskCacheStrategy(DiskCacheStrategy.DATA))
                                    .into(mProifleImageView);


                            Glide.with(getApplicationContext())
                                    .load(profileCover)
                                    .apply(new RequestOptions()
                                            .diskCacheStrategy(DiskCacheStrategy.DATA))
                                    .into(mProfileCover);

                        }
                    }
                });
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStart() {
        super.onStart();
        postCount();
        collectionsCount();
    }

    @Override
    public void onStop() {
        super.onStop();
    }


    @Override
    public void onClick(View v){
        if (v == mSendMessageButton){
            sendMessage();
        }

        if (v == mViewPostRelativeLayout){
            Intent intent = new Intent(this, ProfilePostsActivity.class);
            intent.putExtra(ProfileActivity.EXTRA_USER_UID, mUid);
            startActivity(intent);
        }

        if (v == mViewCollectionsRelativeLayout){
            Intent intent = new Intent(this, ProfileCollectionsActivity.class);
            intent.putExtra(ProfileActivity.EXTRA_USER_UID, mUid);
            startActivity(intent);
        }


        if (v == followButton){
            followProfile();
        }

        if (v == mFollowerCountTextView){
            Intent intent = new Intent(this, FollowersActivity.class);
            intent.putExtra(ProfileActivity.EXTRA_USER_UID, mUid);
            startActivity(intent);
        }

        if (v == mFollowingCountTextView){
            Intent intent = new Intent(this, FollowingActivity.class);
            intent.putExtra(ProfileActivity.EXTRA_USER_UID, mUid);
            startActivity(intent);
        }

        if (v == addCollectionImageView){
            Intent intent = new Intent(this, CreateCollectionActivity.class);
            startActivity(intent);
        }

        if (v == addPostImageView){
            Intent intent = new Intent(this, CreateCollectionPostActivity.class);
            startActivity(intent);
        }
    }

    private void followProfile(){
        //follow or unfollow
        processFollow = true;
        peopleCollection.document("followers")
                .collection(mUid).whereEqualTo("following_id", firebaseAuth.getCurrentUser().getUid())
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
                                follower.setFollowing_id(firebaseAuth.getCurrentUser().getUid());
                                follower.setFollowed_id(mUid);
                                follower.setType("followed_user");
                                follower.setTime(System.currentTimeMillis());
                                peopleCollection.document("followers").collection(mUid)
                                        .document(firebaseAuth.getCurrentUser().getUid()).set(follower)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Timeline timeline = new Timeline();
                                                final long time = new Date().getTime();
                                                final String postid =  databaseReference.push().getKey();
                                                timeline.setPost_id(mUid);
                                                timeline.setTime(time);
                                                timeline.setUser_id(firebaseAuth.getCurrentUser().getUid());
                                                timeline.setType("follow");
                                                timeline.setActivity_id(postid);
                                                timeline.setStatus("un_read");

                                                timelineCollection.document(mUid).collection("activities")
                                                        .document(firebaseAuth.getCurrentUser().getUid())
                                                        .set(timeline);
                                            }
                                        });
                                final Relation following = new Relation();
                                following.setFollowing_id(firebaseAuth.getCurrentUser().getUid());
                                following.setFollowed_id(mUid);
                                following.setType("following_user");
                                following.setTime(System.currentTimeMillis());
                                peopleCollection.document("following").collection(firebaseAuth.getCurrentUser().getUid())
                                        .document(mUid).set(following);
                                followButton.setText("Following");
                                processFollow = false;
                            }else {
                                peopleCollection.document("followers").collection(mUid)
                                        .document(firebaseAuth.getCurrentUser().getUid()).delete();
                                peopleCollection.document("following").collection(firebaseAuth.getCurrentUser().getUid())
                                        .document(mUid).delete();
                                followButton.setText("Follow");
                                processFollow = false;
                            }
                        }
                    }
                });

    }

    private void collectionsCount(){
        collectionCollection.orderBy("user_id").whereEqualTo("user_id", mUid)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                        if (e != null) {
                            Log.w(TAG, "Listen error", e);
                            return;
                        }

                        if (!documentSnapshots.isEmpty()){
                            final int collectionCount = documentSnapshots.size();
                            mCollectionsContainerFrameLayout.setVisibility(View.VISIBLE);
                            mCollectionsCountTextView.setText("Collections: " + collectionCount);
                            mViewCollectionsRelativeLayout.setVisibility(View.VISIBLE);
                        }else {
                            mCollectionsCountTextView.setText("Collections: 0");
                            mCreateCollectionRelativeLayout.setVisibility(View.VISIBLE);

                        }
                    }
                });



    }

    private void postCount(){
        postsCollection.orderBy("user_id").whereEqualTo("user_id", mUid)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots,
                                        @javax.annotation.Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen error", e);
                            return;
                        }

                        if (!queryDocumentSnapshots.isEmpty()){
                            mPostContainerFrameLayout.setVisibility(View.VISIBLE);
                            mViewPostRelativeLayout.setVisibility(View.VISIBLE);
                            final int count = queryDocumentSnapshots.size();
                            mPostCountTextView.setText("Posts: " + count);
                        }else {
                            mPostCountTextView.setText("Posts: 0");
                            mCreatePostRelativeLayout.setVisibility(View.VISIBLE);
                        }

                    }
                });
    }


    private void sendMessage(){
        //look to see if current user has a chat history with mUid
        processRoom = true;
        roomsCollection.document(mUid).collection("last message")
                .document(firebaseAuth.getCurrentUser().getUid())
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen error", e);
                            return;
                        }

                        if (processRoom){
                            if (documentSnapshot.exists()){
                                Room room = documentSnapshot.toObject(Room.class);
                                roomId = room.getRoom_id();
                                Intent intent = new Intent(ProfileActivity.this, MessagingActivity.class);
                                intent.putExtra(ProfileActivity.EXTRA_ROOM_UID, roomId);
                                intent.putExtra(ProfileActivity.EXTRA_USER_UID, mUid);
                                startActivity(intent);

                                processRoom = false;
                            }else {
                                roomsCollection.document(firebaseAuth.getCurrentUser().getUid())
                                        .collection("last message")
                                        .document(mUid).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                    @Override
                                    public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                                        if (e != null) {
                                            Log.w(TAG, "Listen error", e);
                                            return;
                                        }

                                        if (processRoom){
                                            if (documentSnapshot.exists()){
                                                Room room = documentSnapshot.toObject(Room.class);
                                                roomId = room.getRoom_id();
                                                Intent intent = new Intent(ProfileActivity.this, MessagingActivity.class);
                                                intent.putExtra(ProfileActivity.EXTRA_ROOM_UID, roomId);
                                                intent.putExtra(ProfileActivity.EXTRA_USER_UID, mUid);
                                                startActivity(intent);

                                                processRoom = false;

                                            }else {
                                                //start a chat with mUid since they have no chatting history
                                                roomId = databaseReference.push().getKey();
                                                Intent intent = new Intent(ProfileActivity.this, MessagingActivity.class);
                                                intent.putExtra(ProfileActivity.EXTRA_ROOM_UID, roomId);
                                                intent.putExtra(ProfileActivity.EXTRA_USER_UID, mUid);
                                                startActivity(intent);

                                                processRoom = false;
                                            }
                                        }
                                    }
                                });
                            }
                        }

                    }
                });

    }

}

package com.andeqa.andeqa.profile;

import android.content.Intent;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.R;
import com.andeqa.andeqa.chatting.ChatActivity;
import com.andeqa.andeqa.camera.CameraActivity;
import com.andeqa.andeqa.creation.CreateChannelActivity;
import com.andeqa.andeqa.models.Andeqan;
import com.andeqa.andeqa.models.Relation;
import com.andeqa.andeqa.models.Room;

import com.andeqa.andeqa.models.Timeline;
import com.andeqa.andeqa.people.FollowersActivity;
import com.andeqa.andeqa.people.FollowingActivity;
import com.andeqa.andeqa.settings.SettingsActivity;
import com.andeqa.andeqa.utils.FirebaseUtil;
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
    @Bind(R.id.bioLinearLayout)LinearLayout mBioRelativeLayout;
    @Bind(R.id.profileCoverImageView)ImageView mProfileCover;
    @Bind(R.id.sendMessageButton)Button mSendMessageButton;
    @Bind(R.id.followersCountTextView)TextView mFollowerCountTextView;
    @Bind(R.id.followingCountTextView)TextView mFollowingCountTextView;
    @Bind(R.id.followButton)Button followButton;
    @Bind(R.id.followRelativeLayout)RelativeLayout mFollowRelativeLayout;
    @Bind(R.id.postsCountTextView)TextView mPostCountTextView;
    @Bind(R.id.addPostImageView)ImageView addPostImageView;
    @Bind(R.id.post_container)FrameLayout mPostContainerFrameLayout;
    @Bind(R.id.createPostRelativeLayout)RelativeLayout mCreatePostRelativeLayout;
    @Bind(R.id.viewPostRelativelayout)RelativeLayout mViewPostRelativeLayout;
    @Bind(R.id.connectLinearLayout)LinearLayout mConnectLinearLayout;
    @Bind(R.id.addChannelImageView)ImageView mAddChannelImageView;
    @Bind(R.id.channels_container) FrameLayout mChannelsFrameLayout;
    @Bind(R.id.viewChannelsRelativelayout) RelativeLayout mViewChannelsRelativeLayout;
    @Bind(R.id.createChannelRelativeLayout)RelativeLayout mCreateChannelRelativeLayout;
    @Bind(R.id.channelsCountTextView) TextView mChannelCountTextView;

    private static final String TAG = ProfileActivity.class.getSimpleName();
    //firestore reference
    private CollectionReference collectionCollection;
    private CollectionReference usersCollections;
    private CollectionReference roomsCollection;
    private CollectionReference postsCollection;
    private CollectionReference peopleCollection;
    private CollectionReference followingCollection;
    private CollectionReference timelineCollection;
    private CollectionReference mChannelsCollection;
    private Query followersQuery;
    private Query followingQuery;
    //firebase
    private DatabaseReference databaseReference;
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

    private FirebaseUtil mFirebaseUtil;
    private String mCurrentUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        ButterKnife.bind(this);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mFirebaseUtil = new FirebaseUtil(this);
        mCurrentUser = mFirebaseUtil.firebaseAuth().getUid();

        toolbar.setNavigationIcon(R.drawable.ic_arrow);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        mUid = getIntent().getStringExtra(EXTRA_USER_UID);
        usersCollections = mFirebaseUtil.usersPath();
        collectionCollection = mFirebaseUtil.collectionsPath();
        roomsCollection = mFirebaseUtil.collectionsPath();
        postsCollection = mFirebaseUtil.postsPath();
        peopleCollection = mFirebaseUtil.peoplePath();
        followersQuery = peopleCollection.document("followers")
                .collection(mUid);
        followingQuery = peopleCollection.document("following")
                .collection(mUid);
        //firebase
        databaseReference = mFirebaseUtil.pushPath();
        timelineCollection = mFirebaseUtil.timelinePath();
        fetchData();

        //show hidden views
        if (mCurrentUser.equals(mUid)){
            mConnectLinearLayout.setVisibility(View.VISIBLE);
        }else {
            addPostImageView.setVisibility(View.VISIBLE);
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
                    if (mCurrentUser.equals(mUid)){
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
        mFollowerCountTextView.setOnClickListener(this);
        mFollowingCountTextView.setOnClickListener(this);
        addPostImageView.setOnClickListener(this);
        mAddChannelImageView.setOnClickListener(this);

        PostsFragment postsFragment = new PostsFragment();
        FragmentTransaction ft = this.getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.post_container, postsFragment);
        ft.commit();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.profile_menu, menu);
        MenuItem item = menu.findItem(R.id.action_people);
        item.setVisible(false);
        if (!mUid.equals(mCurrentUser)){
            menu.clear();
        }

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings){
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        }


        if (id == R.id.action_people){

        }



        return super.onOptionsItemSelected(item);
    }

    private void fetchData(){
        followersQuery.orderBy("time").whereEqualTo("following_id",
                mCurrentUser).addSnapshotListener(new EventListener<QuerySnapshot>() {
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
                                if (mCurrentUser.equals(mUid)){
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

                            if (profileCover == null){

                                Glide.with(getApplicationContext())
                                        .load(profileImage)
                                        .apply(new RequestOptions()
                                                .diskCacheStrategy(DiskCacheStrategy.DATA))
                                        .into(mProfileCover);
                            }else {

                                Glide.with(getApplicationContext())
                                        .load(profileCover)
                                        .apply(new RequestOptions()
                                                .diskCacheStrategy(DiskCacheStrategy.DATA))
                                        .into(mProfileCover);
                            }

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
        getProfilePostCount();
        getProfileChannels();
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

        if (v == addPostImageView){
            Intent intent = new Intent(this, CameraActivity.class);
            startActivity(intent);
        }

        if (v == mAddChannelImageView){
            Intent intent = new Intent(this, CreateChannelActivity.class);
            startActivity(intent);
        }
    }

    private void followProfile(){
        //follow or unfollow
        processFollow = true;
        peopleCollection.document("followers")
                .collection(mUid).whereEqualTo("following_id", mCurrentUser)
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
                                follower.setFollowing_id(mCurrentUser);
                                follower.setFollowed_id(mUid);
                                follower.setType("followed_user");
                                follower.setTime(System.currentTimeMillis());
                                peopleCollection.document("followers").collection(mUid)
                                        .document(mCurrentUser).set(follower)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Timeline timeline = new Timeline();
                                                final long time = new Date().getTime();
                                                final String postid =  databaseReference.push().getKey();
                                                timeline.setPost_id(mUid);
                                                timeline.setTime(time);
                                                timeline.setUser_id(mCurrentUser);
                                                timeline.setType("follow");
                                                timeline.setActivity_id(postid);
                                                timeline.setStatus("un_read");

                                                timelineCollection.document(mUid).collection("activities")
                                                        .document(mCurrentUser)
                                                        .set(timeline);
                                            }
                                        });
                                final Relation following = new Relation();
                                following.setFollowing_id(mCurrentUser);
                                following.setFollowed_id(mUid);
                                following.setType("following_user");
                                following.setTime(System.currentTimeMillis());
                                peopleCollection.document("following").collection(mCurrentUser)
                                        .document(mUid).set(following);
                                followButton.setText("Following");

                                processFollow = false;
                            }else {
                                peopleCollection.document("followers").collection(mUid)
                                        .document(mCurrentUser).delete();
                                peopleCollection.document("following").collection(mCurrentUser)
                                        .document(mUid).delete();
                                followButton.setText("Follow");
                                processFollow = false;
                            }
                        }
                    }
                });

    }

    private void getProfilePostCount(){
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
                            if (mFirebaseUtil.firebaseAuth().getUid().equals(mUid)){
                                addPostImageView.setVisibility(View.VISIBLE);
                            }else {
                                addPostImageView.setVisibility(View.GONE);
                            }
                        }else {
                           if (mCurrentUser.equals(mUid)){
                               mPostCountTextView.setText("Posts: 0");
                               mCreatePostRelativeLayout.setVisibility(View.VISIBLE);
                               addPostImageView.setVisibility(View.VISIBLE);
                           }else {
                               addPostImageView.setVisibility(View.GONE);
                           }
                        }

                    }
                });
    }

    private void getProfileChannels() {
        mFirebaseUtil.channelsPath().whereEqualTo("user_id",
                mFirebaseUtil.firebaseAuth().getUid())
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot documentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (!documentSnapshots.isEmpty()) {
                    final int count = documentSnapshots.size();
                    mChannelCountTextView.setText("Channels: " + count);
                    mChannelsFrameLayout.setVisibility(View.VISIBLE);
                    mViewChannelsRelativeLayout.setVisibility(View.VISIBLE);
                    if (mFirebaseUtil.firebaseAuth().getUid().equals(mUid)){
                        mAddChannelImageView.setVisibility(View.VISIBLE);
                    }else {
                        mAddChannelImageView.setVisibility(View.GONE);
                    }
                }else {
                    if (mFirebaseUtil.firebaseAuth().getUid().equals(mUid)){
                        mChannelCountTextView.setText("Channels: 0" );
                        mAddChannelImageView.setVisibility(View.VISIBLE);
                        mCreateChannelRelativeLayout.setVisibility(View.VISIBLE);
                    }else {
                        mAddChannelImageView.setVisibility(View.GONE);
                    }
                }
            }
        });
    }

    private void sendMessage(){
        //look to see if current user has a chat history with mUid
        processRoom = true;
        roomsCollection.document(mUid).collection("last message")
                .document(mFirebaseUtil.firebaseAuth().getUid())
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
                                Intent intent = new Intent(ProfileActivity.this, ChatActivity.class);
                                intent.putExtra(ProfileActivity.EXTRA_ROOM_UID, roomId);
                                intent.putExtra(ProfileActivity.EXTRA_USER_UID, mUid);
                                startActivity(intent);

                                processRoom = false;
                            }else {
                                roomsCollection.document(mCurrentUser)
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
                                                Intent intent = new Intent(ProfileActivity.this, ChatActivity.class);
                                                intent.putExtra(ProfileActivity.EXTRA_ROOM_UID, roomId);
                                                intent.putExtra(ProfileActivity.EXTRA_USER_UID, mUid);
                                                startActivity(intent);

                                                processRoom = false;

                                            }else {
                                                //start a chat with mUid since they have no chatting history
                                                roomId = databaseReference.push().getKey();
                                                Intent intent = new Intent(ProfileActivity.this, ChatActivity.class);
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

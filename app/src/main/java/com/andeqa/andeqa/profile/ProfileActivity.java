package com.andeqa.andeqa.profile;

import android.content.Intent;
import android.os.Parcelable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.R;
import com.andeqa.andeqa.collections.MineCollectionsAdapter;
import com.andeqa.andeqa.message.MessagesAccountActivity;
import com.andeqa.andeqa.models.Andeqan;
import com.andeqa.andeqa.models.Room;

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
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener{
    //BIND VIEWS
    @Bind(R.id.profileImageView)CircleImageView mProifleImageView;
    @Bind(R.id.fullNameTextView)TextView mFullNameTextView;
    @Bind(R.id.bioTextView)TextView mBioTextView;
    @Bind(R.id.bioRelativeLayout)RelativeLayout mBioRelativeLayout;
    @Bind(R.id.collectionsCountTextView)TextView mCollectionsCountTextView;
    @Bind(R.id.postsCountTextView)TextView mPostCountTextView;
    @Bind(R.id.singlesCountTextView)TextView mSinglesCountTextView;
    @Bind(R.id.collectionsRelativeLayout)RelativeLayout mCollectionCountRelativeLayout;
    @Bind(R.id.profileCoverImageView)ImageView mProfileCover;
    @Bind(R.id.sendMessageButton)Button mSendMessageButton;
    @Bind(R.id.sendMessageRelativeLayout)RelativeLayout mSendMessageRelativeLayout;


    private static final String TAG = ProfileActivity.class.getSimpleName();
    //firestore reference
    private CollectionReference collectionCollection;
    private CollectionReference usersCollections;
    private CollectionReference roomsCollection;
    private CollectionReference postsCollection;
    //firebase
    private DatabaseReference databaseReference;


    //firebase auth
    private FirebaseAuth firebaseAuth;
    //firestore adapters
    private MineCollectionsAdapter mineCollectionsAdapter;
    private static final String KEY_LAYOUT_POSITION = "layout pooition";
    private Parcelable recyclerViewState;
    private  static final int MAX_WIDTH = 200;
    private static final int MAX_HEIGHT = 200;
    //singles meber variables
    private List<String> cinglesIds = new ArrayList<>();
    private DocumentSnapshot lastVisible;
    private LinearLayoutManager layoutManager;
    private static final String EXTRA_USER_UID = "uid";
    private static final int TOTAL_ITEMS = 20;

    private static final String EXTRA_ROOM_UID = "roomId";
    private String mUid;
    private List<String> collectionsIds = new ArrayList<>();
    private List<DocumentSnapshot> documentSnapshots = new ArrayList<>();

    private boolean processFollow = false;
    private String roomId;
    private boolean processRoom = false;
    private boolean hideMenu = false;

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private ProfilePagerAdapter profilePagerAdapter;




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

        profilePagerAdapter = new ProfilePagerAdapter(getSupportFragmentManager());
        tabLayout = (TabLayout)findViewById(R.id.tabs);
        viewPager = (ViewPager)findViewById(R.id.container);
        viewPager.setAdapter(profilePagerAdapter);
        tabLayout.setupWithViewPager(viewPager);

        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(viewPager));




        if (firebaseAuth.getCurrentUser()!= null){

            mUid = getIntent().getStringExtra(EXTRA_USER_UID);

            usersCollections = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);
            collectionCollection = FirebaseFirestore.getInstance().collection(Constants.USER_COLLECTIONS);
            roomsCollection = FirebaseFirestore.getInstance().collection(Constants.MESSAGES);
            postsCollection = FirebaseFirestore.getInstance().collection(Constants.POSTS);

            //firebase
            databaseReference = FirebaseDatabase.getInstance().getReference(Constants.RANDOM_PUSH_ID);

            fetchData();

            //show hidden views
            if (!firebaseAuth.getCurrentUser().getUid().equals(mUid)){
                mSendMessageRelativeLayout.setVisibility(View.VISIBLE);
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
            mCollectionCountRelativeLayout.setOnClickListener(this);

        }


    }


    private void fetchData(){

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
                            mCollectionsCountTextView.setText(collectionCount + "");
                        }else {
                            mCollectionsCountTextView.setText("0");
                        }
                    }
                });


        postsCollection.orderBy("user_id").whereEqualTo("user_id", mUid)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (!queryDocumentSnapshots.isEmpty()){
                    final int count = queryDocumentSnapshots.size();
                    if (count > 0){
                        mPostCountTextView.setText(count + "");
                    }
                }else {
                    mPostCountTextView.setText("0");
                }

            }
        });

        postsCollection.orderBy("user_id").whereEqualTo("type", "single")
                .whereEqualTo("user_id", mUid)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen error", e);
                            return;
                        }

                        if (!queryDocumentSnapshots.isEmpty()){
                            final int count = queryDocumentSnapshots.size();
                            if (count > 0){
                                mSinglesCountTextView.setText(count + "");
                            }
                        }else {
                            mPostCountTextView.setText("0");
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

                            Picasso.with(ProfileActivity.this)
                                    .load(profileImage)
                                    .resize(MAX_WIDTH, MAX_HEIGHT)
                                    .centerCrop()
                                    .placeholder(R.drawable.ic_user)
                                    .networkPolicy(NetworkPolicy.OFFLINE)
                                    .into(mProifleImageView, new Callback() {
                                        @Override
                                        public void onSuccess() {

                                        }

                                        @Override
                                        public void onError() {
                                            Picasso.with(ProfileActivity.this)
                                                    .load(profileImage)
                                                    .resize(MAX_WIDTH, MAX_HEIGHT)
                                                    .centerCrop()
                                                    .placeholder(R.drawable.ic_user)
                                                    .into(mProifleImageView);

                                        }
                                    });

                            Picasso.with(ProfileActivity.this)
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
                                            Picasso.with(ProfileActivity.this)
                                                    .load(profileCover)
                                                    .fit()
                                                    .centerCrop()
                                                    .into(mProfileCover, new Callback() {
                                                        @Override
                                                        public void onSuccess() {
                                                            Log.d("profile cover", "profile cover found");
                                                        }

                                                        @Override
                                                        public void onError() {
                                                            Log.d("prifle cover", "profile cover not found");
                                                        }
                                                    });


                                        }
                                    });
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
    }

    @Override
    public void onStop() {
        super.onStop();
    }


    @Override
    public void onClick(View v){
        if (v == mSendMessageButton){
            //look to see if current user has a chat history with mUid
            processRoom = true;
            roomsCollection.document("last messages")
                    .collection("last message").document(mUid)
                    .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                            if (e != null) {
                                Log.w(TAG, "Listen error", e);
                                return;
                            }

                            if (processRoom){
                                //have current user sent message to mUid to start a chat or to reply bfre
                                if (documentSnapshot.exists()){
                                    Room room = documentSnapshot.toObject(Room.class);
                                    roomId = room.getRoom_id();
                                    Intent intent = new Intent(ProfileActivity.this, MessagesAccountActivity.class);
                                    intent.putExtra(ProfileActivity.EXTRA_ROOM_UID, roomId);
                                    intent.putExtra(ProfileActivity.EXTRA_USER_UID, mUid);
                                    startActivity(intent);

                                    processRoom = false;

                                }else {
                                    //has mUid sent current user any message to start a chat or reply
                                    roomsCollection.document("last messages")
                                            .collection("last message").document(firebaseAuth.getCurrentUser().getUid())
                                            .addSnapshotListener(new EventListener<DocumentSnapshot>() {
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
                                                            Intent intent = new Intent(ProfileActivity.this, MessagesAccountActivity.class);
                                                            intent.putExtra(ProfileActivity.EXTRA_ROOM_UID, roomId);
                                                            intent.putExtra(ProfileActivity.EXTRA_USER_UID, mUid);
                                                            startActivity(intent);

                                                            processRoom = false;

                                                        }else {
                                                            //start a chat with mUid since they have no chatting history
                                                            roomId = databaseReference.push().getKey();
                                                            Intent intent = new Intent(ProfileActivity.this, MessagesAccountActivity.class);
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

}

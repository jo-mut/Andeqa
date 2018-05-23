package com.andeqa.andeqa.profile;

import android.content.Intent;
import android.os.Parcelable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.R;
import com.andeqa.andeqa.message.MessagesAccountActivity;
import com.andeqa.andeqa.models.Andeqan;
import com.andeqa.andeqa.models.Room;

import com.andeqa.andeqa.wallet.WalletActivity;
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
    @Bind(R.id.collectionsCountTextView)TextView mCollectionsCountTextView;
    @Bind(R.id.collectionsCountRelativeLayout)RelativeLayout mCollectionCountRelativeLayout;
    @Bind(R.id.profileCoverImageView)ImageView mProfileCover;
    @Bind(R.id.collapsing_toolbar)CollapsingToolbarLayout collapsingToolbarLayout;
    @Bind(R.id.sendMessageButton)Button mSendMessageButton;
    @Bind(R.id.sendMessageRelativeLayout)RelativeLayout mSendMessageRelativeLayout;
    @Bind(R.id.bioRelativeLayout)FrameLayout mBioRelativeLayout;

    private static final String TAG = ProfileActivity.class.getSimpleName();
    //firestore reference
    private CollectionReference collectionCollection;
    private CollectionReference usersCollections;
    private CollectionReference roomsCollection;
    //firebase
    private DatabaseReference databaseReference;


    //firebase auth
    private FirebaseAuth firebaseAuth;
    //firestore adapters
    private ProfileCollectionsAdapter profileCollectionsAdapter;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        ButterKnife.bind(this);

        //FIREBASE AUTH
        firebaseAuth = FirebaseAuth.getInstance();

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


        if (firebaseAuth.getCurrentUser()!= null){

            mUid = getIntent().getStringExtra(EXTRA_USER_UID);
            if(mUid == null){
                throw new IllegalArgumentException("pass an EXTRA_UID");
            }

            usersCollections = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);
            collectionCollection = FirebaseFirestore.getInstance().collection(Constants.USER_COLLECTIONS);
            roomsCollection = FirebaseFirestore.getInstance().collection(Constants.MESSAGES);

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
                            collapsingToolbarLayout.setTitle("Profile");
                        }else {
                            toolbar.getMenu().clear();
                            collapsingToolbarLayout.setTitle(andeqan.getUsername());
                        }
                    }

                }
            });


            /**initialize click listners*/
            mSendMessageButton.setOnClickListener(this);
            mCollectionCountRelativeLayout.setOnClickListener(this);


        }


    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (firebaseAuth.getCurrentUser().getUid().equals(mUid)){
            getMenuInflater().inflate(R.menu.profile_menu, menu);

        }

        return true;

    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        // Handle action b item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        int id = item.getItemId();

        if (id == R.id.action_wallet){
            Intent intent = new Intent(ProfileActivity.this, WalletActivity.class);
            startActivity(intent);
        }

        if (id == R.id.action_signout){
            FragmentManager fragmentManager = getSupportFragmentManager();
            DialogConfirmSingOutFragment dialogConfirmSingOutFragment = DialogConfirmSingOutFragment.newInstance("sing out");
            dialogConfirmSingOutFragment.show(fragmentManager, "delete account fragment");
        }

        if (id == R.id.action_account_settings){
            Intent intent = new Intent(ProfileActivity.this, UpdateProfileActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }



    private void fetchData(){

        collectionCollection.orderBy("time", Query.Direction.DESCENDING)
                .whereEqualTo("user_id", mUid).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                final int collectionCount = queryDocumentSnapshots.size();

                if (!queryDocumentSnapshots.isEmpty()){
                    mCollectionsCountTextView.setText(collectionCount + "");
                }else {
                    mCollectionsCountTextView.setText("0");
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
                            final String profileImage = andeqan.getProfile_image();
                            String bio = andeqan.getBio();
                            final String profileCover = andeqan.getProfile_cover();

                            mFullNameTextView.setText(firstName + " " + secondName);
                            mBioTextView.setText(bio);

                            Picasso.with(ProfileActivity.this)
                                    .load(profileImage)
                                    .resize(MAX_WIDTH, MAX_HEIGHT)
                                    .onlyScaleDown()
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
                                                    .onlyScaleDown()
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
            processRoom = true;
            roomsCollection.document("rooms")
                    .collection(mUid).document(firebaseAuth.getCurrentUser().getUid())
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

        if (v == mCollectionCountRelativeLayout){
            Intent intent = new Intent(ProfileActivity.this, ProfileCollectionsActivity.class);
            intent.putExtra(ProfileActivity.EXTRA_USER_UID, mUid);
            startActivity(intent);
        }

    }

}

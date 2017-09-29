package com.cinggl.cinggl.relations;

import android.content.Intent;
import android.os.Parcelable;
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
import android.widget.Toast;

import com.cinggl.cinggl.Constants;
import com.cinggl.cinggl.R;
import com.cinggl.cinggl.adapters.ProfileCinglesAdapter;
import com.cinggl.cinggl.models.Cingle;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class FollowerProfileActivity extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG = FollowerProfileActivity.class.getSimpleName();

    @Bind(R.id.profileCinglesRecyclerView)RecyclerView mProfileCinglesRecyclerView;
    @Bind(R.id.profileImageView)CircleImageView mProifleImageView;
    @Bind(R.id.firstNameTextView)TextView mFirstNameTextView;
    @Bind(R.id.secondNameTextView)TextView  mSecondNameTextView;
    @Bind(R.id.bioTextView)TextView mBioTextView;
    @Bind(R.id.followersCountTextView) TextView mFollowersCountTextView;
    @Bind(R.id.followingCountTextView)TextView mFollowingCountTextView;
    @Bind(R.id.cinglesCountTextView)TextView mCinglesCountTextView;
    @Bind(R.id.header_cover_image)ImageView mProfileCover;
    @Bind(R.id.followButton)Button mFollowButton;


    //DATABASE REFERENCES
    private Query profileInfoQuery;
    private DatabaseReference relationsRef;
    private boolean processLikes = false;
    private  static final int MAX_WIDTH = 300;
    private static final int MAX_HEIGHT = 300;
    private static final double GOLDEN_RATIO = 1.618;
    private static final double DEFAULT_PRICE = 1.5;
    private static final String EXTRA_POST_KEY = "post key";
    private static final String EXTRA_USER_UID = "uid";
    private int mProfileCinglesRecyclerViewPosition = 0;
    private DatabaseReference databaseReference;
    private Query cinglesQuery;
    private ChildEventListener mChildEventListener;
    private DatabaseReference usernameRef;
    private DatabaseReference likesRef;
    private FirebaseAuth firebaseAuth;
    private ProfileCinglesAdapter profileCinglesAdapter;
    private DatabaseReference sensepointRef;
    private DatabaseReference commentReference;
    private DatabaseReference profileCinglesReference;
    private LinearLayoutManager layoutManager;
    private String mUid;
    private boolean processFollow = false;

    private List<Cingle> cingles = new ArrayList<>();
    private List<String> cinglesIds = new ArrayList<>();

    private int currentPage = 0;
    private static final int TOTAL_ITEM_EACH_LOAD = 10;
    private static final String KEY_LAYOUT_POSITION = "layout pooition";
    private Parcelable recyclerViewState;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follower_profile);
        ButterKnife.bind(this);

        //FIREBASE AUTH
        firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser()!= null){
            mUid = getIntent().getStringExtra(EXTRA_USER_UID);
            if(mUid == null){
                throw new IllegalArgumentException("pass an EXTRA_USER_UID");
            }

            //DATABASE REFERENCE
            databaseReference = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CINGLES);
            profileInfoQuery = databaseReference.orderByChild("uid").equalTo(mUid);
            relationsRef = FirebaseDatabase.getInstance().getReference(Constants.RELATIONS);
            usernameRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_USERS);
            commentReference = FirebaseDatabase.getInstance().getReference(Constants.COMMENTS);
            likesRef = FirebaseDatabase.getInstance().getReference(Constants.LIKES);
            sensepointRef = FirebaseDatabase.getInstance().getReference("Sense points");
            profileCinglesReference =  FirebaseDatabase.getInstance().getReference(Constants.PROFILE_CINGLES)
                    .child(mUid);

            usernameRef.keepSynced(true);
            relationsRef.keepSynced(true);
            databaseReference.keepSynced(true);
            likesRef.keepSynced(true);
            commentReference.keepSynced(true);
            profileInfoQuery.keepSynced(true);
            profileCinglesReference.keepSynced(true);

            fetchData();
//            setUpFirebaseAdapter();
            setProfileCingles(currentPage);
            initializeViewsAdapter();

        }

        //INITIALIZE CLICK LISTENERS
        mFollowersCountTextView.setOnClickListener(this);
        mFollowingCountTextView.setOnClickListener(this);
        mFollowersCountTextView.setOnClickListener(this);
        mFollowingCountTextView.setOnClickListener(this);
        mFollowButton.setOnClickListener(this);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }


    private void initializeViewsAdapter(){
        layoutManager =  new LinearLayoutManager(this);
        mProfileCinglesRecyclerView.setLayoutManager(layoutManager);
        mProfileCinglesRecyclerView.setHasFixedSize(true);
        profileCinglesAdapter = new ProfileCinglesAdapter(this);
        mProfileCinglesRecyclerView.setAdapter(profileCinglesAdapter);
        profileCinglesAdapter.notifyDataSetChanged();
    }

    private void fetchData(){

        profileInfoQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Log.e(snapshot.getKey(), snapshot.getChildrenCount() + "cingles Count");
                }

                if (dataSnapshot.hasChildren()){
                    mCinglesCountTextView.setText(dataSnapshot.getChildrenCount()+ "");
                }else {
                    mCinglesCountTextView.setText("0");
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //retrieve the count of followers for this user
        relationsRef.child("followers").child(mUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Log.e(snapshot.getKey(), snapshot.getChildrenCount() + "followers Count");
                }

                //SET TEXT ON BUTTON FOLLOWING IF FOLLOWING
                if (dataSnapshot.hasChild(firebaseAuth.getCurrentUser().getUid())){
                    mFollowButton.setText("FOLLOWING");
                }else {
                    mFollowButton.setText("FOLLOW");
                }

                //SET FOLLOWERS COUNT IF ANY
                if (dataSnapshot.hasChildren()){
                    mFollowersCountTextView.setText(dataSnapshot.getChildrenCount() + "");
                }else {
                    mFollowersCountTextView.setText("0");
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        //retrieve the count of users followed by this user
        relationsRef.child("following").child(mUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.hasChildren()){
                            mFollowingCountTextView.setText(dataSnapshot.getChildrenCount() + "");
                        }else {
                            mFollowingCountTextView.setText("0");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


        usernameRef.child(mUid).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            String firstName = (String) dataSnapshot.child("firstName").getValue();
                            String secondName = (String) dataSnapshot.child("secondName").getValue();
                            final String profileImage = (String) dataSnapshot.child("profileImage").getValue();
                            String bio = (String) dataSnapshot.child("bio").getValue();
                            final String profileCover = (String) dataSnapshot.child("profileCover").getValue();

                            mFirstNameTextView.setText(firstName);
                            mSecondNameTextView.setText(secondName);
                            mBioTextView.setText(bio);

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

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }


    public void setProfileCingles(int start){
//        progressBar.setVisibility(View.VISIBLE);
        cinglesQuery = profileCinglesReference.orderByChild("number").startAt(start)
                .endAt(start + TOTAL_ITEM_EACH_LOAD);
        cinglesQuery.keepSynced(true);

        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d("Snapshot", dataSnapshot.toString());
//                progressBar.setVisibility(View.GONE);

                Cingle cingle = dataSnapshot.getValue(Cingle.class);
                cinglesIds.add(dataSnapshot.getKey());
                cingles.add(cingle);

                currentPage += 10;
                profileCinglesAdapter.setProfileCingles(cingles);
                profileCinglesAdapter.notifyItemInserted(cingles.size());
                profileCinglesAdapter.getItemCount();
                Log.d("size of all cingles", cingles.size() + "");

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Cingle cingle =  dataSnapshot.getValue(Cingle.class);

                String cingle_key = dataSnapshot.getKey();

                //exclude
                int cingle_index = cinglesIds.indexOf(cingle_key);
                if (cingle_index > - 1){

                    //replace with the new cingle
                    cingles.set(cingle_index, cingle);
                    profileCinglesAdapter.notifyItemChanged(cingle_index);
                    profileCinglesAdapter.notifyDataSetChanged();
                    profileCinglesAdapter.getItemCount();
                }else {
                    Log.w(TAG, "onChildChanged:unknown_child" + cingle_key);
                }


            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onChiledRemoved:" + dataSnapshot.getKey());

                //a cingle has changed. use the key to determine if the cingle
                // is being displayed and
                //so remove it.
                String cingle_key = dataSnapshot.getKey();
                //exclude
                int cingle_index = cinglesIds.indexOf(cingle_key);
                if (cingle_index > - 1){
                    //remove data from the list
                    cinglesIds.remove(cingle_index);
                    cingles.remove(cingle_key);
                    profileCinglesAdapter.removeAt(cingle_index);
                    profileCinglesAdapter.notifyItemRemoved(cingle_index);

                }else {
                    Log.w(TAG, "onChildRemoved:unknown_child:" + cingle_key);
                }

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                Cingle cingle = dataSnapshot.getValue(Cingle.class);
                String cingle_key = dataSnapshot.getKey();

                //...

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "load Cingles : onCancelled", databaseError.toException());
                Toast.makeText(FollowerProfileActivity.this, "Failed to load comments.", Toast.LENGTH_SHORT).show();

            }
        };
        cinglesQuery.addChildEventListener(childEventListener);
        mChildEventListener = childEventListener;
    }

    public void cleanUpListener(){
        if (mChildEventListener != null){
            cinglesQuery.removeEventListener(mChildEventListener);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(KEY_LAYOUT_POSITION, layoutManager.onSaveInstanceState());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        recyclerViewState = savedInstanceState.getParcelable(KEY_LAYOUT_POSITION);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cleanUpListener();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (recyclerViewState != null){
            layoutManager.onRestoreInstanceState(recyclerViewState);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onClick(View v){
        if (v == mFollowingCountTextView) {
            Intent intent = new Intent(this, FollowingActivity.class);
            intent.putExtra(FollowerProfileActivity.EXTRA_USER_UID, mUid);
            startActivity(intent);

        }

        if (v == mFollowersCountTextView){
            Intent intent = new Intent(this, FollowersActivity.class);
            intent.putExtra(FollowerProfileActivity.EXTRA_USER_UID, mUid);
            startActivity(intent);

        }

        if (v == mFollowButton){
            processFollow = true;

            relationsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (processFollow){
                        Log.d("muid", mUid);
                        if (dataSnapshot.child("followers").child(mUid).hasChild(firebaseAuth.getCurrentUser().getUid())){
                            relationsRef.child("followers").child(mUid).child(firebaseAuth.getCurrentUser().getUid())
                                    .removeValue();
                            relationsRef.child("following").child(firebaseAuth.getCurrentUser().getUid()).child(mUid)
                                    .removeValue();
                            processFollow = false;
                            onFollow(false);
                            //set the text on the button to follow if the user in not yet following;
                            mFollowButton.setText("FOLLOW");
                            mFollowersCountTextView.setText(dataSnapshot.getChildrenCount() + "");

                        }else {
                            //set followers of mUid;
                            relationsRef.child("followers").child(mUid).child(firebaseAuth.getCurrentUser().getUid())
                                    .child("uid")
                                    .setValue(firebaseAuth.getCurrentUser().getUid());
                            //set the uid you are following
                            relationsRef.child("following").child(firebaseAuth.getCurrentUser().getUid()).child(mUid)
                                    .child("uid").setValue(mUid);
                            processFollow = false;
                            onFollow(false);
                            //set text on the button following;
                            mFollowButton.setText("FOLLOWING");
                            mFollowersCountTextView.setText(dataSnapshot.getChildrenCount() + "");

                        }

                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }
    }

    private void onFollow(final boolean increament){
        relationsRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                if(mutableData.getValue() != null){
                    int value = mutableData.getValue(Integer.class);
                    if(increament){
                        value++;
                    }else{
                        value--;
                    }
                    mutableData.setValue(value);
                }
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b,
                                   DataSnapshot dataSnapshot) {
                Log.d(TAG, "followTransaction:onComplete" + databaseError);

            }
        });
    }

}

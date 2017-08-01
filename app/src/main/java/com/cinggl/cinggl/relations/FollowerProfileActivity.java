package com.cinggl.cinggl.relations;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.cinggl.cinggl.Constants;
import com.cinggl.cinggl.R;
import com.cinggl.cinggl.adapters.ProfileCinglesViewHolder;
import com.cinggl.cinggl.models.Cingle;
import com.cinggl.cinggl.profile.CingleDetailActivity;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
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

import butterknife.Bind;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class FollowerProfileActivity extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG = FollowerProfileActivity.class.getSimpleName();
    private String mUid;
    private DatabaseReference usernameRef;
    private DatabaseReference databaseReference;
    private DatabaseReference relationsRef;
    private Query profileCinglesQuery;
    private Query profileInfoQuery;
    private FirebaseAuth firebaseAuth;
    private FirebaseRecyclerAdapter firebaseRecyclerAdapter;
    private static final String EXTRA_USER_UID = "uid";
    private static  final int MAX_WIDTH = 400;
    private static final int MAX_HEIGHT = 400;
    private boolean processFollow = false;

    @Bind(R.id.profileCinglesRecyclerView)RecyclerView mProfileCinglesRecyclerView;
    @Bind(R.id.userProfileImageView)CircleImageView mProifleImageView;
    @Bind(R.id.firstNameTextView)TextView mFirstNameTextView;
    @Bind(R.id.secondNameTextView)TextView  mSecondNameTextView;
    @Bind(R.id.bioTextView)TextView mBioTextView;
    @Bind(R.id.followersCountTextView) TextView mFollowersCountTextView;
    @Bind(R.id.followingCountTextView)TextView mFollowingCountTextView;
    @Bind(R.id.cinglesCountTextView)TextView mCinglesCountTextView;
    @Bind(R.id.followButton)Button mFollowButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follower_profile);
        ButterKnife.bind(this);

        firebaseAuth = FirebaseAuth.getInstance();

        mFollowersCountTextView.setOnClickListener(this);
        mFollowingCountTextView.setOnClickListener(this);
        mFollowButton.setOnClickListener(this);
//        fetchUserData();
//        setUpFirebaseAdapter();

        mUid = getIntent().getStringExtra(EXTRA_USER_UID);
        if(mUid == null){
            throw new IllegalArgumentException("pass an EXTRA_USER_UID");
        }

        Log.d("the uid passed", mUid);


        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CINGLES);
        profileCinglesQuery = databaseReference.orderByChild("uid").equalTo(mUid);
        databaseReference = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_USERS);
        usernameRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_USERS);
        relationsRef = FirebaseDatabase.getInstance().getReference(Constants.RELATIONS);
        profileInfoQuery = databaseReference.orderByChild("uid").equalTo(mUid);


        usernameRef.keepSynced(true);
        profileInfoQuery.keepSynced(true);
        relationsRef.keepSynced(true);
        databaseReference.keepSynced(true);
        profileCinglesQuery.keepSynced(true);

        setUpUserProfile();

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

    private void setUpUserProfile(){

        //retrieve the cingles posted by the user
        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Cingle, ProfileCinglesViewHolder>
                (Cingle.class, R.layout.profile_cingles_item_list, ProfileCinglesViewHolder.class, profileCinglesQuery) {
            @Override
            protected void populateViewHolder(final ProfileCinglesViewHolder viewHolder, final Cingle model, int position) {
                final String postKey = getRef(position).getKey();

                profileCinglesQuery.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChildren()){
                            viewHolder.bindProfileCingle(model);
                        }else {
                            //display a message there are no cingles
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(FollowerProfileActivity.this, CingleDetailActivity.class);
                        intent.putExtra("cingle_id", postKey);
                        startActivity(intent);
                    }
                });


            }

        };

        mProfileCinglesRecyclerView.setAdapter(firebaseRecyclerAdapter);
        mProfileCinglesRecyclerView.setHasFixedSize(false);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(this, 3);
        layoutManager.setAutoMeasureEnabled(true);
        mProfileCinglesRecyclerView.setNestedScrollingEnabled(false);
        mProfileCinglesRecyclerView.setLayoutManager(layoutManager);

        //retrieve user profile information
        usernameRef.child(mUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final String uid = (String) dataSnapshot.child("uid").getValue();
                final String firstName = (String) dataSnapshot.child("firstName").getValue();
                final String secondName = (String) dataSnapshot.child("secondName").getValue();
                final String profileImage = (String) dataSnapshot.child("profileImage").getValue();
                final String bio = (String) dataSnapshot.child("bio").getValue();
//
                mFirstNameTextView.setText(firstName);
                mSecondNameTextView.setText(secondName);
                mBioTextView.setText(bio);

                Picasso.with(FollowerProfileActivity.this)
                        .load(profileImage)
                        .fit()
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
                                        .fit()
                                        .centerCrop()
                                        .placeholder(R.drawable.profle_image_background)
                                        .into(mProifleImageView);
                            }
                        });



            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        //retrieve the count of cingles posted by the user
        profileCinglesQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Log.e(snapshot.getKey(), snapshot.getChildrenCount() + "cingles Count");

                    if (dataSnapshot.hasChildren()){
                        mCinglesCountTextView.setText(dataSnapshot.getChildrenCount()+ "");
                    }else {
                        mCinglesCountTextView.setText("0");
                    }

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
//
//        //get the database node key of this user
//        DatabaseReference reference = usernameRef.child(mUid);
//        final String refKey = reference.getKey();
//        Log.d(refKey, "current user reference key");

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

            relationsRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (processFollow){
                        if (dataSnapshot.child("followers").child(mUid).hasChild(firebaseAuth.getCurrentUser().getUid())){
                            relationsRef.child("followers").child(mUid)
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

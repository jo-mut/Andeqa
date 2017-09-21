package com.cinggl.cinggl.relations;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
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

import com.cinggl.cinggl.Constants;
import com.cinggl.cinggl.R;
import com.cinggl.cinggl.adapters.ProfileCinglesViewHolder;
import com.cinggl.cinggl.home.CingleSettingsDialog;
import com.cinggl.cinggl.home.CommentsActivity;
import com.cinggl.cinggl.home.LikesActivity;
import com.cinggl.cinggl.models.Cingle;
import com.cinggl.cinggl.models.Cingulan;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class FollowerProfileActivity extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG = FollowerProfileActivity.class.getSimpleName();
    private String mUid;
    private DatabaseReference usernameRef;
    private DatabaseReference databaseReference;
    private DatabaseReference relationsRef;
    private DatabaseReference commentReference;
    private DatabaseReference likesRef;
    private Query profileCinglesQuery;
    private Query profileInfoQuery;
    private FirebaseAuth firebaseAuth;
    private FirebaseRecyclerAdapter firebaseRecyclerAdapter;
    private static final String EXTRA_USER_UID = "uid";
    private static  final int MAX_WIDTH = 300;
    private static final int MAX_HEIGHT = 300;
    private static final int MAX_COVER_HEIGHT = 400;
    private static final int MAX_COVER_WIDTH = 400;
    private static final double DEFAULT_PRICE = 1.5;
    private static final double GOLDEN_RATIO = 1.618;
    private static final String EXTRA_POST_KEY = "post key";
    private boolean processFollow = false;
    private boolean processLikes = false;

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



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follower_profile);
        ButterKnife.bind(this);

        firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() != null){
            mFollowersCountTextView.setOnClickListener(this);
            mFollowingCountTextView.setOnClickListener(this);
            mFollowButton.setOnClickListener(this);
//        fetchUserData();
//        setUpFirebaseAdapter();

            mUid = getIntent().getStringExtra(EXTRA_USER_UID);
            if(mUid == null){
                throw new IllegalArgumentException("pass an EXTRA_USER_UID");
            }


            firebaseAuth = FirebaseAuth.getInstance();
            databaseReference = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CINGLES);
            profileCinglesQuery = databaseReference.orderByChild("uid").equalTo(mUid);
            usernameRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_USERS);
            relationsRef = FirebaseDatabase.getInstance().getReference(Constants.RELATIONS);
            profileInfoQuery = databaseReference.orderByChild("uid").equalTo(mUid);
            commentReference = FirebaseDatabase.getInstance().getReference(Constants.COMMENTS);
            likesRef = FirebaseDatabase.getInstance().getReference(Constants.LIKES);


            usernameRef.keepSynced(true);
            profileInfoQuery.keepSynced(true);
            relationsRef.keepSynced(true);
            databaseReference.keepSynced(true);
            likesRef.keepSynced(true);
            commentReference.keepSynced(true);
            profileCinglesQuery.keepSynced(true);

            setUpUserProfile();
        }


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
                (Cingle.class, R.layout.cingle_out_list, ProfileCinglesViewHolder.class, profileCinglesQuery) {
            @Override
            protected void populateViewHolder(final ProfileCinglesViewHolder viewHolder, final Cingle model, int position) {
                final String postKey = getRef(position).getKey();

                viewHolder.bindProfileCingle(model);

                //retrieve user profile information
                usernameRef.child(mUid).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            final String uid = (String) dataSnapshot.child("uid").getValue();
                            final String firstName = (String) dataSnapshot.child("firstName").getValue();
                            final String secondName = (String) dataSnapshot.child("secondName").getValue();
                            final String profileImage = (String) dataSnapshot.child("profileImage").getValue();
                            final String bio = (String) dataSnapshot.child("bio").getValue();
                            final String profileCover = (String) dataSnapshot.child("profileCover").getValue();

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


                //retrieve the count of cingles posted by the user
                profileCinglesQuery.addValueEventListener(new ValueEventListener() {
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


                //DATABASE REFERENCE TO READ THE UID OF THE USER IN THE CINGLE
                databaseReference.child(postKey).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            final String uid = (String) dataSnapshot.child("uid").getValue();

                            viewHolder.likesCountTextView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent intent = new Intent(FollowerProfileActivity.this, LikesActivity.class);
                                    intent.putExtra(FollowerProfileActivity.EXTRA_POST_KEY, postKey);
                                    startActivity(intent);
                                }
                            });

                            viewHolder.commentsImageView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent intent =  new Intent(FollowerProfileActivity.this, CommentsActivity.class);
                                    intent.putExtra(FollowerProfileActivity.EXTRA_POST_KEY, postKey);
                                    startActivity(intent);
                                }
                            });


                            //SHOW CINGLE SETTINGS TO THE CINGLE CREATOR ONLY
                            if (firebaseAuth.getCurrentUser().getUid().equals(uid)){
                                viewHolder.cingleSettingsImageView.setVisibility(View.VISIBLE);

                                viewHolder.cingleSettingsImageView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Bundle args = new Bundle();
                                        args.putString(FollowerProfileActivity.EXTRA_POST_KEY, postKey);

                                        FragmentManager fragmenManager = getSupportFragmentManager();
                                        CingleSettingsDialog cingleSettingsDialog = CingleSettingsDialog.newInstance("cingle settings");
                                        cingleSettingsDialog.setArguments(args);
                                        cingleSettingsDialog.show(fragmenManager, "new post fragment");


                                    }
                                });


                            }else {
                                viewHolder.cingleSettingsImageView.setVisibility(View.GONE);
                            }

                            //RETRIEVE USER INFO IF AND CATCH EXCEPTION IF CINGLES IS NOT DELETED;
                            //SET THE CINGULAN CURRENT USERNAME AND PROFILE IMAGE
                            usernameRef.child(mUid).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    final Cingulan cingulan = dataSnapshot.getValue(Cingulan.class);

                                    viewHolder.accountUsernameTextView.setText(cingulan.getUsername());
                                    Picasso.with(FollowerProfileActivity.this)
                                            .load(cingulan.getProfileImage())
                                            .fit()
                                            .centerCrop()
                                            .placeholder(R.drawable.profle_image_background)
                                            .networkPolicy(NetworkPolicy.OFFLINE)
                                            .into(viewHolder.profileImageView, new Callback() {
                                                @Override
                                                public void onSuccess() {

                                                }

                                                @Override
                                                public void onError() {
                                                    Picasso.with(FollowerProfileActivity.this)
                                                            .load(cingulan.getProfileImage())
                                                            .fit()
                                                            .centerCrop()
                                                            .placeholder(R.drawable.profle_image_background)
                                                            .into(viewHolder.profileImageView);
                                                }
                                            });
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                            //RETRIEVE SENSEPOINTS AND CATCH EXCEPTION IF CINGLE ISN'T DELETED
                            databaseReference.child(postKey).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                                        Log.d(snapshot.getKey(), snapshot.getChildrenCount() + "sensepoint");
                                    }
                                    Cingle cingle = dataSnapshot.getValue(Cingle.class);

                                    DecimalFormat formatter =  new DecimalFormat("0.00000000");
                                    try {
                                        viewHolder.cingleSenseCreditsTextView.setText("CSC" + " " + formatter.format(cingle.getSensepoint()));
                                    }catch (Exception e){
                                        e.printStackTrace();
                                    }

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                            //RETRIEVE TIME POSTED ANC CATCH EXCEPTIONS
                            databaseReference.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    final Long time = (Long) dataSnapshot.child("timestamp").getValue(Long.class);
//
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });


                            //RETRIEVE COMMENTS COUNT AND CATCH EXCEPTIONS IF ANY
                            commentReference.child(postKey).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                                        Log.e(snapshot.getKey(), snapshot.getChildrenCount() + "commentsCount");
                                    }

                                    viewHolder.commentsCountTextView.setText(dataSnapshot.getChildrenCount() + "");
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });


                            //RETRIEVE LIKES COUNT AND CATCH EXCEPTIONS IF CINGLE DELETED
                            likesRef.child(postKey).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                                        Log.e(snapshot.getKey(), snapshot.getChildrenCount() + "likesCount");

                                    }
                                    viewHolder.likesCountTextView.setText(dataSnapshot.getChildrenCount() + " " + "Likes");

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                            viewHolder.likesImageView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    processLikes = true;
                                    likesRef.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(final DataSnapshot dataSnapshot) {
                                            if(processLikes){
                                                if(dataSnapshot.child(postKey).hasChild(firebaseAuth.getCurrentUser().getUid())){
                                                    likesRef.child(postKey).child(firebaseAuth.getCurrentUser()
                                                            .getUid())
                                                            .removeValue();

                                                    onLikeCounter(false);
                                                    processLikes = false;

                                                }else {
                                                    likesRef.child(postKey).child(firebaseAuth.getCurrentUser().getUid())
                                                            .child("uid").setValue(firebaseAuth.getCurrentUser().getUid());
                                                    processLikes = false;
                                                    onLikeCounter(false);
                                                }

                                            }


                                            String likesCount = dataSnapshot.child(postKey).getChildrenCount() + "";
                                            Log.d(likesCount, "all the likes in one cingle");
                                            //convert children count which is a string to integer
                                            final int x = Integer.parseInt(likesCount);

                                            if (x > 0){
                                                //mille is a thousand likes
                                                double MILLE = 1000.0;
                                                //get the number of likes per a thousand likes
                                                double likesPerMille = x/MILLE;
                                                //get the default rate of likes per unit time in seconds;
                                                double rateOfLike = 1000.0/1800.0;
                                                //get the current rate of likes per unit time in seconds;
                                                double currentRateOfLkes = x * rateOfLike/MILLE;
                                                //get the current price of cingle
                                                final double currentPrice = currentRateOfLkes * DEFAULT_PRICE/rateOfLike;
                                                //get the perfection value of cingle's interactivity online
                                                double perfectionValue = GOLDEN_RATIO/x;
                                                //get the new worth of Cingle price in Sen
                                                final double cingleWorth = perfectionValue * likesPerMille * currentPrice;
                                                //round of the worth of the cingle to 4 decimal number
//                                        double finalPoints = Math.round( cingleWorth * 10000.0)/10000.0;

                                                double finalPoints = round( cingleWorth, 10);

                                                databaseReference.child(postKey).child("sensepoint").setValue(finalPoints);
                                            }
                                            else {
                                                double sensepoint = 0.00;

                                                databaseReference.child(postKey).child("sensepoint").setValue(sensepoint);
                                            }

                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                                }
                            });
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

        };

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setAutoMeasureEnabled(true);
        mProfileCinglesRecyclerView.setLayoutManager(layoutManager);
        mProfileCinglesRecyclerView.setAdapter(firebaseRecyclerAdapter);
        mProfileCinglesRecyclerView.setHasFixedSize(false);
        mProfileCinglesRecyclerView.setNestedScrollingEnabled(false);

    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onStop(){
        super.onStop();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        firebaseRecyclerAdapter.cleanup();
    }

    private void onLikeCounter(final boolean increament){
        likesRef.runTransaction(new Transaction.Handler() {
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
                Log.d(TAG, "likeTransaction:onComplete" + databaseError);

            }
        });
    }


    //region listeners
    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
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
                            relationsRef.child("following").child(firebaseAuth.getCurrentUser().getUid());
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

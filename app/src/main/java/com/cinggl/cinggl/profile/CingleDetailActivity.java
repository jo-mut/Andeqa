package com.cinggl.cinggl.profile;

import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cinggl.cinggl.Constants;
import com.cinggl.cinggl.R;
import com.cinggl.cinggl.home.CingleSettingsDialog;
import com.cinggl.cinggl.home.DeleteAccountDialog;
import com.cinggl.cinggl.home.CommentsActivity;
import com.cinggl.cinggl.home.LikesActivity;
import com.cinggl.cinggl.models.Cingle;
import com.cinggl.cinggl.ProportionalImageView;
import com.cinggl.cinggl.relations.FollowerProfileActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
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

import static android.R.attr.x;
import static com.cinggl.cinggl.R.id.sensePointsTextView;

public class CingleDetailActivity extends AppCompatActivity implements
        View.OnClickListener{
    @Bind(R.id.userProfileImageView)ImageView mProfileImageView;
    @Bind(R.id.cingleImageView)ProportionalImageView mCingleImageView;
    @Bind(R.id.accountUsernameTextView)TextView mAccountUsernameTextView;
    @Bind(R.id.cingleTitleTextView)TextView mCingleTitleTextView;
    @Bind(R.id.cingleDescriptionTextView)TextView mCingleDescriptionTextView;
    @Bind(R.id.likesCountTextView) TextView mLikesCountTextView;
    @Bind(R.id.commentsCountTextView)TextView mCommentsCountTextView;
    @Bind(R.id.commentsImageView)ImageView mCommentsImageView;
    @Bind(R.id.likesImageView)ImageView mLikesImageView;
    @Bind(R.id.sensePointsDescTextView)TextView mSensePointsTextView;
    @Bind(R.id.cingleSettingsImageView)ImageView mCingleSettngsImageView;
    @Bind(R.id.cingleTitleRelativeLayout)RelativeLayout mCingleTitleRelativeLayout;
    @Bind(R.id.cingleDescriptionRelativeLayout)RelativeLayout mCingleDescriptionRelativeLayout;
    @Bind(R.id.timeTextView)TextView mTimeTextView;

    private Cingle cingle;
    private DatabaseReference likesRef;
    private DatabaseReference usernameRef;
    private DatabaseReference commentReference;
    private DatabaseReference cinglesReference;
    private DatabaseReference databaseReference;
    private static final int MAX_WIDTH = 400;
    private static final int MAX_HEIGHT = 400;
    private String mPostKey;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private static final String TAG = CingleDetailActivity.class.getSimpleName();
    private boolean processLikes = false;
    public static final String EXTRA_POST_KEY = "post key";
    private static final String EXTRA_USER_UID = "uid";
    private static final double DEFAULT_PRICE = 1.5;
    private static final double GOLDEN_RATIO = 1.618;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cingle_detail);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mCommentsImageView.setOnClickListener(this);
        mLikesImageView.setOnClickListener(this);
        mLikesCountTextView.setOnClickListener(this);
        mCingleSettngsImageView.setOnClickListener(this);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        mPostKey = getIntent().getExtras().getString("cingle_id");
        if(mPostKey == null){
            throw  new IllegalArgumentException("pass the cingle's post key");
        }

        cinglesReference = FirebaseDatabase.getInstance()
                .getReference(Constants.FIREBASE_CINGLES).child(mPostKey);
        databaseReference = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CINGLES);
        commentReference = FirebaseDatabase.getInstance()
                .getReference(Constants.COMMENTS).child(mPostKey);
        usernameRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_USERS);
        likesRef = FirebaseDatabase.getInstance()
                .getReference(Constants.LIKES);

        cinglesReference.keepSynced(true);
        usernameRef.keepSynced(true);
        cinglesReference.keepSynced(true);
        likesRef.keepSynced(true);

        //add back naviagtion on the toolbar
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        cinglesReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final String uid = (String) dataSnapshot.child("uid").getValue();

                try {
                    usernameRef.child(uid).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String username = (String) dataSnapshot.child("username").getValue();
                            final String profileImage = (String) dataSnapshot.child("profileImage").getValue();

                            mAccountUsernameTextView.setText(username);



                            Picasso.with(CingleDetailActivity.this)
                                    .load(profileImage)
                                    .fit()
                                    .centerCrop()
                                    .placeholder(R.drawable.profle_image_background)
                                    .networkPolicy(NetworkPolicy.OFFLINE)
                                    .into(mProfileImageView, new Callback() {
                                        @Override
                                        public void onSuccess() {

                                        }

                                        @Override
                                        public void onError() {
                                            Picasso.with(CingleDetailActivity.this)
                                                    .load(profileImage)
                                                    .fit()
                                                    .centerCrop()
                                                    .placeholder(R.drawable.profle_image_background)
                                                    .into(mProfileImageView);
                                        }
                                    });
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }catch (Exception e){

                }

                //set a clickListener on the profile image, if current user launch his profile if follower lauchn their profile
                mProfileImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (uid.equals(firebaseAuth.getCurrentUser().getUid())){
                            Intent intent = new Intent(CingleDetailActivity.this, PersonalProfileActivity.class);
                            startActivity(intent);
                        }else {
                            Intent intent = new Intent(CingleDetailActivity.this, FollowerProfileActivity.class);
                            intent.putExtra(CingleDetailActivity.this.EXTRA_USER_UID, uid);
                            startActivity(intent);
                        }
                    }
                });

                //set a click listner of the settings. only the creator can delete the cingle
                if (firebaseAuth.getCurrentUser().getUid().equals(uid)){
                    mCingleSettngsImageView.setVisibility(View.VISIBLE);

                    mCingleSettngsImageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Bundle args = new Bundle();
                            args.putString(CingleDetailActivity.EXTRA_POST_KEY, mPostKey);
                            FragmentManager fragmenManager = getSupportFragmentManager();
                            CingleSettingsDialog cingleSettingsDialog = CingleSettingsDialog.newInstance("cingle settings");
                            cingleSettingsDialog.setArguments(args);
                            cingleSettingsDialog.show(fragmenManager, "new post fragment");
                        }
                    });
                }else {
                    mCingleSettngsImageView.setVisibility(View.GONE);
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });

        cinglesReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final Cingle cingle = dataSnapshot.getValue(Cingle.class);

                try {

                    mTimeTextView.setText(DateUtils.getRelativeTimeSpanString((long) cingle.getTimeStamp()));
                    mSensePointsTextView.setText("SP" + " " + Double.toString(cingle.getSensepoint()));
                    Picasso.with(CingleDetailActivity.this).load(cingle.getCingleImageUrl())
                            .networkPolicy(NetworkPolicy.OFFLINE)
                            .into(mCingleImageView, new Callback() {
                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onError() {
                                    Picasso.with(CingleDetailActivity.this).load(cingle.getCingleImageUrl())
                                            .into(mCingleImageView);

                                }
                            });

                    //check if the title is empty. hide the title layout if empty
                    if (cingle.getTitle().equals("")){
                        mCingleTitleRelativeLayout.setVisibility(View.GONE);
                    }else {
                        mCingleTitleTextView.setText(cingle.getTitle());
                    }

                    //chech if the description is empty and hide the layout or retrieve the description
                    if (cingle.getDescription().equals("")){
                        mCingleDescriptionRelativeLayout.setVisibility(View.GONE);
                    }else {
                        mCingleDescriptionTextView.setText(cingle.getDescription());

                    }

                }catch (Exception e){
                    Toast.makeText(CingleDetailActivity.this, "This Cingle cannot be reached", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(CingleDetailActivity.this, PersonalProfileActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        commentReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Log.e(snapshot.getKey(), snapshot.getChildrenCount() + "commentsCount");
                }

                mCommentsCountTextView.setText(dataSnapshot.getChildrenCount() + "");

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        likesRef.child(mPostKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Log.e(snapshot.getKey(), snapshot.getChildrenCount() + "likesCount");
                }

                mLikesCountTextView.setText(dataSnapshot.getChildrenCount() + "" + "Likes");

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void onClick(View v){
        if (v == mLikesImageView){
            processLikes = true;
            likesRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(processLikes){
                        if (dataSnapshot.child(mPostKey).hasChild(firebaseAuth.getCurrentUser().getUid())){
                            likesRef.child(mPostKey)
                                    .removeValue();
                            processLikes = false;
                            onLikeCounter(false);
                        }else {
                            likesRef.child(mPostKey).child(firebaseAuth.getCurrentUser().getUid())
                                    .child("uid").setValue(firebaseAuth.getCurrentUser().getUid());
                            processLikes = false;
                            onLikeCounter(false);


                        }
                    }

                    String likesCount = dataSnapshot.child(mPostKey).getChildrenCount() + "";
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

                        cinglesReference.child("sensepoint").setValue(finalPoints);
                    }
                    else {
                        double sensepoint = 0.00;

                        cinglesReference.child("sensepoint").setValue(sensepoint);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });


            //DISPLAY SENSEPOINTS ON NEW lIKE
            cinglesReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    final Cingle cingle = dataSnapshot.getValue(Cingle.class);

                    DecimalFormat formatter =  new DecimalFormat("0.00000000");
                    mSensePointsTextView.setText("SP" + " " + formatter.format(cingle.getSensepoint()));


                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        if(v == mLikesCountTextView){
            Intent intent = new Intent(CingleDetailActivity.this, LikesActivity.class);
            intent.putExtra(CingleDetailActivity.EXTRA_POST_KEY, mPostKey);
            startActivity(intent);
        }

        if(v == mCommentsImageView){
            Intent intent = new Intent(CingleDetailActivity.this, CommentsActivity.class);
            intent.putExtra(CingleDetailActivity.EXTRA_POST_KEY, mPostKey);
            startActivity(intent);
        }

        if (v == mCingleSettngsImageView){
            FragmentManager fragmenManager = getSupportFragmentManager();
            DeleteAccountDialog deleteAccountDialog = DeleteAccountDialog.newInstance("create your cingle");
            deleteAccountDialog.show(fragmenManager, "new post fragment");
        }

    }

    //region listeners
    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
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
}

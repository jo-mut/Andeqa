package com.cinggl.cinggl.ifair;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cinggl.cinggl.Constants;
import com.cinggl.cinggl.R;
import com.cinggl.cinggl.adapters.CingleOutAdapter;
import com.cinggl.cinggl.home.CingleSettingsDialog;
import com.cinggl.cinggl.home.CommentsActivity;
import com.cinggl.cinggl.home.LikesActivity;
import com.cinggl.cinggl.models.Cingle;
import com.cinggl.cinggl.profile.PersonalProfileActivity;
import com.cinggl.cinggl.relations.FollowerProfileActivity;
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

import java.math.BigDecimal;
import java.math.RoundingMode;

import butterknife.Bind;
import butterknife.ButterKnife;

import static com.cinggl.cinggl.R.id.likesCountTextView;
import static java.lang.System.load;

public class TradeDetailActivity extends AppCompatActivity implements View.OnClickListener{
    @Bind(R.id.usernameTextView)TextView mUsernameTextView;
    @Bind(R.id.cingleImageView)ImageView mCingleImageView;
    @Bind(R.id.profileImageView)ImageView mProfileImageView;
    @Bind(R.id.cingleTitleTextView)TextView mCingleTitleTextView;
    @Bind(R.id.cingleTitleRelativeLayout)RelativeLayout mCingleTitleRelativeLayout;
    @Bind(R.id.cingleDescriptionRelativeLayout)RelativeLayout mCingleDescriptionRelatvieLayout;
    @Bind(R.id.cingleDescriptionTextView)TextView mCingleDescriptionTextView;
    @Bind(R.id.cingleTradeMethodTextView)TextView mCingleTradeMethodTextView;
    @Bind(R.id.cingleOwnerTextView)TextView mCingleOwnerTextView;
    @Bind(R.id.likesImageView)ImageView mLikesImageView;
    @Bind(likesCountTextView)TextView mLikesCountTextView;
    @Bind(R.id.commentsImageView)ImageView mCommentImageView;
    @Bind(R.id.commentsCountTextView)TextView mCommentCountTextView;
    @Bind(R.id.likesRecyclerView)RecyclerView mLikesRecyclerView;
    @Bind(R.id.cingleOwnersRecyclerView)RecyclerView mCingleOnwersRecyclerView;
    @Bind(R.id.cingleSenseCreditsTextView)TextView mCingleSenseCreditsTextView;
    @Bind(R.id.tradeCingleButton)Button mTradeCingleButton;

    private DatabaseReference databaseReference;
    private DatabaseReference commentReference;
    private DatabaseReference usersRef;
    private  DatabaseReference likesRef;
    private DatabaseReference ifairReference;
    private DatabaseReference cingleWalletReference;
    private DatabaseReference cinglesReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseRecyclerAdapter firebaseRecyclerAdapter;
    private Query likesQuery;
    private Query likesQueryCount;
    private boolean processLikes = false;
    private static final double DEFAULT_PRICE = 1.5;
    private static final double GOLDEN_RATIO = 1.618;
    private Context mContext;
    private String mPostKey;
    private static final String EXTRA_POST_KEY = "post key";
    private static final String EXTRA_USER_UID = "uid";
    private static final String TAG = TradeDetailActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trading_detail);
        ButterKnife.bind(this);

        firebaseAuth = FirebaseAuth.getInstance();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        firebaseAuth = FirebaseAuth.getInstance();

        mPostKey = getIntent().getStringExtra(EXTRA_POST_KEY);
        if(mPostKey == null){
            throw new IllegalArgumentException("pass an EXTRA_POST_KEY");
        }

        //INITIALIASE CLICK LISTENER
        mLikesImageView.setOnClickListener(this);
        mLikesRecyclerView.setOnClickListener(this);
        mCommentImageView.setOnClickListener(this);
        mLikesCountTextView.setOnClickListener(this);
        mTradeCingleButton.setOnClickListener(this);


        //DATABASE REFERENCE PATH;
        commentReference = FirebaseDatabase.getInstance().getReference(Constants.COMMENTS);
        cinglesReference = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CINGLES);
        usersRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_USERS);
        likesRef = FirebaseDatabase.getInstance().getReference(Constants.LIKES);
        databaseReference = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CINGLES);
        ifairReference = FirebaseDatabase.getInstance().getReference(Constants.IFAIR);
        cingleWalletReference = FirebaseDatabase.getInstance().getReference(Constants.CINGLE_WALLET);
        likesQueryCount = likesRef;



        usersRef.keepSynced(true);
        databaseReference.keepSynced(true);
        likesRef.keepSynced(true);
        commentReference.keepSynced(true);
        cingleWalletReference.keepSynced(true);
        ifairReference.keepSynced(true);
        cinglesReference.keepSynced(true);

        //RETRIEVE DATA FROM FIREBASE
        setCingleData();
        setTextOnButton();
    }

    public void setCingleData(){
        //RETRIEVE LIKES COUNT AND CATCH EXCEPTIONS IF CINGLE DELETED
        likesRef.child(mPostKey).startAt(6)
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mLikesCountTextView.setText("+" + dataSnapshot.getChildrenCount() +" " + "Likes");

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //CINGLE REFERENCE
        cinglesReference.child(mPostKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    Cingle cingle = dataSnapshot.getValue(Cingle.class);
                    final String image = cingle.getCingleImageUrl();
                    final String uid = cingle.getUid();
                    final String title = cingle.getTitle();
                    final String description = cingle.getDescription();
                    final double sensecredits = cingle.getSensepoint();

                    usersRef.child(uid).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String username = (String) dataSnapshot.child("username").getValue();
                            final String profileImage = (String) dataSnapshot.child("profileImage").getValue();

                            mUsernameTextView.setText(username);

                            Picasso.with(TradeDetailActivity.this)
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
                                            Picasso.with(TradeDetailActivity.this)
                                                    .load(profileImage)
                                                    .fit()
                                                    .centerCrop()
                                                    .placeholder(R.drawable.profle_image_background)
                                                    .into(mProfileImageView);
                                        }
                                    });

                            //LAUCNH PROFILE IF ITS NOT DELETED ELSE CATCH THE EXCEPTION
                            mProfileImageView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    if (uid.equals(firebaseAuth.getCurrentUser().getUid())){
                                        Intent intent = new Intent(TradeDetailActivity.this, PersonalProfileActivity.class);
                                        startActivity(intent);
                                    }else {
                                        Intent intent = new Intent(TradeDetailActivity.this, FollowerProfileActivity.class);
                                        intent.putExtra(TradeDetailActivity.EXTRA_USER_UID, uid);
                                        startActivity(intent);
                                    }
                                }
                            });
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
//
//
//                    set the title of the cingle
                    if (title.equals("")){
                        mCingleTitleRelativeLayout.setVisibility(View.GONE);
                    }else {
                        mCingleTitleTextView.setText(title);
                    }

                    if (description.equals("")){
                        mCingleDescriptionRelatvieLayout.setVisibility(View.GONE);
                    }else {
                        mCingleDescriptionTextView.setText(description);
                    }

                    mCingleSenseCreditsTextView.setText(Double.toString(sensecredits));

                    //set the cingle image
                    Picasso.with(TradeDetailActivity.this)
                            .load(image)
                            .networkPolicy(NetworkPolicy.OFFLINE)
                            .into(mCingleImageView, new Callback() {
                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onError() {
                                    Picasso.with(TradeDetailActivity.this)
                                            .load(image)
                                            .into(mCingleImageView);
                                }
                            });

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void setTextOnButton(){
        ifairReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.child("Cingle Selling").hasChild(mPostKey)){
                    mTradeCingleButton.setText("Buy");
                }else {
                    mTradeCingleButton.setVisibility(View.GONE);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onClick(View v){
        if (v == mLikesImageView){
            processLikes = true;
            likesRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(final DataSnapshot dataSnapshot) {
                    if(processLikes){
                        if(dataSnapshot.child(mPostKey).hasChild(firebaseAuth.getCurrentUser().getUid())){
                            likesRef.child(mPostKey).child(firebaseAuth.getCurrentUser()
                                    .getUid())
                                    .removeValue();
                            onLikeCounter(false);
                            processLikes = false;

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
//
                        double finalPoints = round( cingleWorth, 10);

                        databaseReference.child(mPostKey).child("sensepoint").setValue(finalPoints);
                    }
                    else {
                        double sensepoint = 0.00;

                        databaseReference.child(mPostKey).child("sensepoint").setValue(sensepoint);
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        if (v == mCommentImageView){
            Intent intent = new Intent(TradeDetailActivity.this, CommentsActivity.class);
            intent.putExtra(TradeDetailActivity.EXTRA_POST_KEY, mPostKey);
            startActivity(intent);
        }

        if (v == mLikesCountTextView){
            Intent intent = new Intent(TradeDetailActivity.this, LikesActivity.class);
            intent.putExtra(TradeDetailActivity.EXTRA_POST_KEY, mPostKey);
            startActivity(intent);
        }

        if (v == mTradeCingleButton){
            Bundle bundle = new Bundle();
            bundle.putString(TradeDetailActivity.EXTRA_POST_KEY, mPostKey);
            FragmentManager fragmenManager = getSupportFragmentManager();
            SendCreditsDialogFragment sendCreditsDialogFragment = SendCreditsDialogFragment.newInstance("sens credits");
            sendCreditsDialogFragment.setArguments(bundle);
            sendCreditsDialogFragment.show(fragmenManager, "send credits fragment");
        }
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

}

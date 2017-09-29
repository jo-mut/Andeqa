package com.cinggl.cinggl.ifair;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cinggl.cinggl.Constants;
import com.cinggl.cinggl.R;
import com.cinggl.cinggl.adapters.CingleOutAdapter;
import com.cinggl.cinggl.adapters.IfairCingleAdapter;
import com.cinggl.cinggl.adapters.UsersWhoLiked;
import com.cinggl.cinggl.home.CingleDetailActivity;
import com.cinggl.cinggl.home.CingleSettingsDialog;
import com.cinggl.cinggl.home.CommentsActivity;
import com.cinggl.cinggl.home.LikesActivity;
import com.cinggl.cinggl.models.Balance;
import com.cinggl.cinggl.models.Cingle;
import com.cinggl.cinggl.models.CingleSale;
import com.cinggl.cinggl.models.Cingulan;
import com.cinggl.cinggl.models.Like;
import com.cinggl.cinggl.models.TransactionDetails;
import com.cinggl.cinggl.profile.PersonalProfileActivity;
import com.cinggl.cinggl.relations.FollowerProfileActivity;
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

import static android.icu.lang.UCharacter.GraphemeClusterBreak.L;
import static android.icu.lang.UCharacter.GraphemeClusterBreak.V;
import static com.cinggl.cinggl.R.id.cingleImageView;
import static com.cinggl.cinggl.R.id.cingleOwnerTextView;
import static com.cinggl.cinggl.R.id.cingleSalePriceTextView;
import static com.cinggl.cinggl.R.id.cingleSenseCreditsTextView;
import static com.cinggl.cinggl.R.id.cingleTradeMethodTextView;
import static com.cinggl.cinggl.R.id.commentsCountTextView;
import static com.cinggl.cinggl.R.id.datePostedTextView;
import static com.cinggl.cinggl.R.id.likesCountTextView;
import static com.cinggl.cinggl.R.id.likesImageView;
import static com.cinggl.cinggl.R.id.likesRecyclerView;
import static com.cinggl.cinggl.R.id.ownerImageView;
import static java.lang.System.load;

public class TradeDetailActivity extends AppCompatActivity implements View.OnClickListener{
    @Bind(R.id.usernameTextView)TextView mUsernameTextView;
    @Bind(cingleImageView)ImageView mCingleImageView;
    @Bind(R.id.profileImageView)ImageView mProfileImageView;
    @Bind(R.id.cingleTitleTextView)TextView mCingleTitleTextView;
    @Bind(R.id.cingleTitleRelativeLayout)RelativeLayout mCingleTitleRelativeLayout;
    @Bind(R.id.cingleDescriptionRelativeLayout)RelativeLayout mCingleDescriptionRelatvieLayout;
    @Bind(R.id.cingleDescriptionTextView)TextView mCingleDescriptionTextView;
    @Bind(cingleTradeMethodTextView)TextView mCingleTradeMethodTextView;
    @Bind(cingleOwnerTextView)TextView mCingleOwnerTextView;
    @Bind(likesImageView)ImageView mLikesImageView;
    @Bind(likesCountTextView)TextView mLikesCountTextView;
    @Bind(R.id.commentsImageView)ImageView mCommentImageView;
    @Bind(commentsCountTextView)TextView mCommentCountTextView;
    @Bind(likesRecyclerView)RecyclerView mLikesRecyclerView;
    @Bind(R.id.cingleOwnersRecyclerView)RecyclerView mCingleOnwersRecyclerView;
    @Bind(cingleSenseCreditsTextView)TextView mCingleSenseCreditsTextView;
    @Bind(R.id.tradeCingleButton)Button mTradeCingleButton;
    @Bind(cingleSalePriceTextView)TextView mCingleSalePriceTextView;
    @Bind(R.id.datePostedTextView)TextView mDatePostedTextView;
    @Bind(R.id.ownerImageView)CircleImageView mOwnerImageView;
    @Bind(R.id.editSalePriceImageView)ImageView mEditSalePriceImageView;
    @Bind(R.id.editSalePriceEditText)EditText mEditSalePriceEditText;
    @Bind(R.id.doneEditingImageView)ImageView mDoneEditingImageView;
    @Bind(R.id.salePriceProgressbar)ProgressBar mSalePriceProgressBar;
    @Bind(R.id.cingleSalePriceTitleRelativeLayout)RelativeLayout mCingleSalePriceTitleRelativeLayout;

    private DatabaseReference databaseReference;
    private DatabaseReference commentReference;
    private DatabaseReference cingleOwnerReference;
    private DatabaseReference usersRef;
    private  DatabaseReference likesRef;
    private DatabaseReference ifairReference;
    private DatabaseReference cingleWalletReference;
    private DatabaseReference cinglesReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseRecyclerAdapter firebaseRecyclerAdapter;
    private Query likesQuery;
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

        if (firebaseAuth.getCurrentUser()!= null){
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
            mCingleImageView.setOnClickListener(this);
            mEditSalePriceImageView.setOnClickListener(this);
            mDoneEditingImageView.setOnClickListener(this);


            //DATABASE REFERENCE PATH;
            commentReference = FirebaseDatabase.getInstance().getReference(Constants.COMMENTS);
            cinglesReference = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CINGLES);
            usersRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_USERS);
            likesRef = FirebaseDatabase.getInstance().getReference(Constants.LIKES);
            databaseReference = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CINGLES);
            ifairReference = FirebaseDatabase.getInstance().getReference(Constants.IFAIR);
            cingleWalletReference = FirebaseDatabase.getInstance().getReference(Constants.CINGLE_WALLET);
            likesQuery = likesRef.child(mPostKey).limitToFirst(5);
            cingleOwnerReference = FirebaseDatabase.getInstance().getReference(Constants.CINGLE_ONWERS);

            usersRef.keepSynced(true);
            databaseReference.keepSynced(true);
            likesRef.keepSynced(true);
            commentReference.keepSynced(true);
            cingleWalletReference.keepSynced(true);
            ifairReference.keepSynced(true);
            cinglesReference.keepSynced(true);
            cingleOwnerReference.keepSynced(true);


            //RETRIEVE DATA FROM FIREBASE
            setCingleData();
            setTextOnButton();
            setSalePrice();
            setEditTextFilter();
            showBuyButton();
            showEditImageView();

        }
    }


    private void setCingleData(){
        commentReference.child(mPostKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){

                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                        Log.e(snapshot.getKey(), snapshot.getChildrenCount() + "commentsCount");
                    }

                    mCommentCountTextView.setText(dataSnapshot.getChildrenCount() + "");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        likesRef.child(mPostKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
               if (dataSnapshot.exists()){
                   mLikesCountTextView.setText(dataSnapshot.getChildrenCount() + " " + "Likes");

                   if (dataSnapshot.hasChildren()){
                       mLikesImageView.setColorFilter(Color.RED);
                   }else {
                       mLikesImageView.setColorFilter(Color.BLACK);
                   }


               }
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
                            if (dataSnapshot.exists()){
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
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

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

                    mCingleSenseCreditsTextView.setText("CSC" + " " + " " + sensecredits);
                    mDatePostedTextView.setText(cingle.getDatePosted());

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

    /**Cingle can only be bought by someone else except for the owner of that cingle*/
    private void showBuyButton(){
        cingleOwnerReference.child(mPostKey).child("owner").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
               if (dataSnapshot.exists()){
                   TransactionDetails transactionDetails = dataSnapshot.getValue(TransactionDetails.class);
                   final String ownerUid = transactionDetails.getUid();
                   Log.d("owner uid", ownerUid);

                   if (firebaseAuth.getCurrentUser().getUid().equals(ownerUid)){
                       mTradeCingleButton.setVisibility(View.INVISIBLE);
                   }else {
                       mTradeCingleButton.setVisibility(View.VISIBLE);
                   }

               }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /**set the the text on buy button*/
    private void setTextOnButton(){
        ifairReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.child("Cingle Selling").hasChild(mPostKey)){
                    mTradeCingleButton.setText("Buy");
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /**display the price of the cingle*/
    private void setSalePrice(){

        databaseReference.child(mPostKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    final String datePosted = dataSnapshot.child("datePosted").getValue(String.class);
                    mDatePostedTextView.setText(datePosted);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        ifairReference.child("Cingle Selling").child(mPostKey).addValueEventListener
                (new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
               if (dataSnapshot.exists()){
                   CingleSale cingleSale = dataSnapshot.getValue(CingleSale.class);
                   DecimalFormat formatter =  new DecimalFormat("0.00000000");
                   mCingleSalePriceTextView.setText("CSC" + " " + formatter.format(cingleSale.getSalePrice()));
               }else {
                   mCingleSalePriceTitleRelativeLayout.setVisibility(View.GONE);
               }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //SET THE TRADE METHOD TEXT ACCORDING TO THE TRADE METHOD OF THE CINGLE
        ifairReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final String uid = dataSnapshot.child("Cingle Selling")
                        .child(mPostKey).child("uid").getValue(String.class);

                //SET CINGLE TRADE METHOD WHEN THERE ARE ALL TRADE METHODS
                if (dataSnapshot.child("Cingle Lacing").hasChild(mPostKey)){
                    mCingleTradeMethodTextView.setText("@CingleLacing");
                }else if (dataSnapshot.child("Cingle Leasing").hasChild(mPostKey)){
                    mCingleTradeMethodTextView.setText("@CingleLeasing");

                }else if (dataSnapshot.child("Cingle Selling").hasChild(mPostKey)){
                    mCingleTradeMethodTextView.setText("@CingleSelling");
                }else if ( dataSnapshot.child("Cingle Backing").hasChild(mPostKey)){
                    mCingleTradeMethodTextView.setText("@CingleBacking");
                }else {
                    mCingleTradeMethodTextView.setText("@NotForTrade");
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        cinglesReference.child(mPostKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    final Cingle cingle = dataSnapshot.getValue(Cingle.class);

                    Picasso.with(mContext)
                            .load(cingle.getCingleImageUrl())
                            .networkPolicy(NetworkPolicy.OFFLINE)
                            .into(mCingleImageView, new Callback() {
                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onError() {
                                    Picasso.with(mContext)
                                            .load(cingle.getCingleImageUrl())
                                            .into(mCingleImageView);


                                }
                            });
                    DecimalFormat formatter =  new DecimalFormat("0.00000000");
                    mCingleSenseCreditsTextView.setText("CSC" + "" + "" + formatter.format(cingle.getSensepoint()));
                    mDatePostedTextView.setText(cingle.getDatePosted());

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //RETRIEVE THE FIRST FIVE USERS WHO LIKED
        likesRef.child(mPostKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    Log.d("likes count", dataSnapshot.getChildrenCount() + "");
                    if (dataSnapshot.getChildrenCount()>0){
                        mLikesRecyclerView.setVisibility(View.VISIBLE);
                        //SETUP USERS WHO LIKED THE CINGLE
                        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Like, UsersWhoLiked>
                                (Like.class, R.layout.users_who_liked_count, UsersWhoLiked.class, likesQuery) {
                            @Override
                            public int getItemCount() {
                                return super.getItemCount();

                            }

                            @Override
                            public long getItemId(int position) {
                                return super.getItemId(position);
                            }

                            @Override
                            protected void populateViewHolder(final UsersWhoLiked viewHolder, final Like model, final int position) {
                                DatabaseReference userRef = getRef(position);
                                final String likesPostKey = userRef.getKey();
                                Log.d(TAG, "likes post key" + likesPostKey);

                                likesRef.child(mPostKey).child(likesPostKey)
                                        .addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.child("uid").exists()){
                                            final String uid = (String) dataSnapshot.child("uid").getValue();
                                            usersRef.child(uid).addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    final String profileImage = (String) dataSnapshot.child("profileImage").getValue();

                                                    Picasso.with(mContext)
                                                            .load(profileImage)
                                                            .fit()
                                                            .centerCrop()
                                                            .placeholder(R.drawable.profle_image_background)
                                                            .networkPolicy(NetworkPolicy.OFFLINE)
                                                            .into(viewHolder.usersWhoLikedProfileImageView, new Callback() {
                                                                @Override
                                                                public void onSuccess() {

                                                                }

                                                                @Override
                                                                public void onError() {
                                                                    Picasso.with(mContext)
                                                                            .load(profileImage)
                                                                            .fit()
                                                                            .centerCrop()
                                                                            .placeholder(R.drawable.profle_image_background)
                                                                            .into(viewHolder.usersWhoLikedProfileImageView);


                                                                }
                                                            });
                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {

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

                        mLikesRecyclerView.setAdapter(firebaseRecyclerAdapter);
                        mLikesRecyclerView.setHasFixedSize(false);
                        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(mContext,
                                LinearLayoutManager.HORIZONTAL, true);
                        layoutManager.setAutoMeasureEnabled(true);
                        mLikesRecyclerView.setNestedScrollingEnabled(false);
                        mLikesRecyclerView.setLayoutManager(layoutManager);

                    }else {
                        mLikesRecyclerView.setVisibility(View.GONE);
                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        ifairReference.child("Cingle Selling").child(mPostKey)
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    CingleSale sale = dataSnapshot.getValue(CingleSale.class);
                    final String uid = sale.getUid();
                    final String pushId = sale.getPushId();
                    final double salePrice = sale.getSalePrice();

                    DecimalFormat formatter =  new DecimalFormat("0.00000000");
                    mCingleSalePriceTextView.setText("CSC" + " " + "" + formatter.format(sale.getSalePrice()));

                    //retrieve user info
                    usersRef.child(uid).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()){
                                final Cingulan cingulan = dataSnapshot.getValue(Cingulan.class);

                                Picasso.with(mContext)
                                        .load(cingulan.getProfileImage())
                                        .networkPolicy(NetworkPolicy.OFFLINE)
                                        .into(mProfileImageView, new Callback() {
                                            @Override
                                            public void onSuccess() {

                                            }

                                            @Override
                                            public void onError() {
                                                Picasso.with(mContext)
                                                        .load(cingulan.getProfileImage())
                                                        .into(mProfileImageView);


                                            }
                                        });
                                mUsernameTextView.setText(cingulan.getUsername());

                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        /**display the person who currently owns the cingle*/
        cingleOwnerReference.child(mPostKey).child("owner").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    TransactionDetails transactionDetails = dataSnapshot.getValue(TransactionDetails.class);
                    final String ownerUid = transactionDetails.getUid();

                    usersRef.child(ownerUid).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                           if (dataSnapshot.exists()){
                               Cingulan cingulan = dataSnapshot.getValue(Cingulan.class);
                               final String username = cingulan.getUsername();
                               final String profileImage = cingulan.getProfileImage();
                               mCingleOwnerTextView.setText(username);
                               Picasso.with(mContext)
                                       .load(profileImage)
                                       .fit()
                                       .centerCrop()
                                       .placeholder(R.drawable.profle_image_background)
                                       .networkPolicy(NetworkPolicy.OFFLINE)
                                       .into(mOwnerImageView, new Callback() {
                                           @Override
                                           public void onSuccess() {

                                           }

                                           @Override
                                           public void onError() {
                                               Picasso.with(mContext)
                                                       .load(profileImage)
                                                       .fit()
                                                       .centerCrop()
                                                       .placeholder(R.drawable.profle_image_background)
                                                       .into(mOwnerImageView);
                                           }
                                       });
                           }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
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
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.child(mPostKey).exists()){
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
                                        mLikesImageView.setColorFilter(Color.BLACK);

                                    }else {
                                        likesRef.child(mPostKey).child(firebaseAuth.getCurrentUser().getUid())
                                                .child("uid").setValue(firebaseAuth.getCurrentUser().getUid());
                                        processLikes = false;
                                        onLikeCounter(false);
                                        mLikesImageView.setColorFilter(Color.RED);
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
                                    //round of the worth of the cingle to 10 decimal number
                                    final double finalPoints = round( cingleWorth, 10);

                                    Log.d("final points", finalPoints + "");

                                    cingleWalletReference.child(mPostKey).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.exists()) {
                                                final Balance balance = dataSnapshot.getValue(Balance.class);
                                                final double amountRedeemed = balance.getAmountRedeemed();
                                                Log.d(amountRedeemed + "", "amount redeemed");
                                                final  double amountDeposited = balance.getAmountDeposited();
                                                Log.d(amountDeposited + "", "amount deposited");
                                                final double senseCredits = amountDeposited + finalPoints;
                                                Log.d("sense credits", senseCredits + "");
                                                final double totalSenseCredits = senseCredits - amountRedeemed;
                                                Log.d("total sense credits", totalSenseCredits + "");
                                                databaseReference.child(mPostKey).child("sensepoint").setValue(totalSenseCredits);
                                            }else {
                                                databaseReference.child(mPostKey).child("sensepoint").setValue(finalPoints);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                                }
                                else{
                                    final double finalPoints = 0.00;
                                    Log.d("final points", finalPoints + "");
                                    cingleWalletReference.child(mPostKey).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.exists()) {
                                                final Balance balance = dataSnapshot.getValue(Balance.class);
                                                final double amountRedeemed = balance.getAmountRedeemed();
                                                Log.d(amountRedeemed + "", "amount redeemed");
                                                final  double amountDeposited = balance.getAmountDeposited();
                                                Log.d(amountDeposited + "", "amount deposited");
                                                final double senseCredits = amountDeposited + finalPoints;
                                                Log.d("sense credits", senseCredits + "");
                                                final double totalSenseCredits = senseCredits - amountRedeemed;
                                                Log.d("total sense credits", totalSenseCredits + "");
                                                databaseReference.child(mPostKey).child("sensepoint").setValue(totalSenseCredits);
                                            }else {
                                                databaseReference.child(mPostKey).child("sensepoint").setValue(finalPoints);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });

                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
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

        if (v == mCingleImageView){
            Intent intent = new Intent(TradeDetailActivity.this, CingleDetailActivity.class);
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

        if (v == mDoneEditingImageView){
           setNewPrice();
        }

        if (v == mEditSalePriceImageView){
            mEditSalePriceEditText.setVisibility(View.VISIBLE);
            mCingleSalePriceTextView.setVisibility(View.GONE);
            mEditSalePriceImageView.setVisibility(View.GONE);
            mDoneEditingImageView.setVisibility(View.VISIBLE);
        }
    }

    private void showEditImageView(){
        cingleOwnerReference.child(mPostKey).child("owner")
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
               if (dataSnapshot.exists()){
                   TransactionDetails td = dataSnapshot.getValue(TransactionDetails.class);
                   final String ownerUid = td.getUid();

                   if (firebaseAuth.getCurrentUser().getUid().equals(ownerUid)){
                       mEditSalePriceImageView.setVisibility(View.VISIBLE);
                   }else {
                       mEditSalePriceImageView.setVisibility(View.GONE);
                   }
               }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setNewPrice(){
        final String stringSalePrice = mEditSalePriceEditText.getText().toString().trim();
        if (stringSalePrice.equals("")){
            mEditSalePriceEditText.setError("Sale price is empty!");
        }else {
            final double intSalePrice = Double.parseDouble(stringSalePrice);
            cinglesReference.child(mPostKey).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()){
                        Cingle cingle = dataSnapshot.getValue(Cingle.class);
                        final double sensecredits =  cingle.getSensepoint();

                        if (intSalePrice < sensecredits){
                            mEditSalePriceEditText.setError("Sale price is less than Cingle Sense Crdits!");
                        }else {
                            mSalePriceProgressBar.setVisibility(View.VISIBLE);
                            ifairReference.child("Cingle Selling").child(mPostKey)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.exists()){
                                                CingleSale cingleSale = dataSnapshot.getValue(CingleSale.class);
                                                final String uid = cingleSale.getUid();

                                                ifairReference.child("Cingle Selling").child(mPostKey).child("salePrice")
                                                        .setValue(intSalePrice)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()){
                                                                    mSalePriceProgressBar.setVisibility(View.GONE);
                                                                    mCingleSalePriceTextView.setVisibility(View.VISIBLE);
                                                                    mEditSalePriceEditText.setVisibility(View.GONE);
                                                                    mDoneEditingImageView.setVisibility(View.GONE);
                                                                    mEditSalePriceImageView.setVisibility(View.VISIBLE);
                                                                    mSalePriceProgressBar.setVisibility(View.GONE);

                                                                }

                                                            }
                                                        });
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            mEditSalePriceEditText.setText("");
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

    //cingle sense edittext filter
    public void setEditTextFilter(){
        mEditSalePriceEditText.setFilters(new InputFilter[] {
                new DigitsKeyListener(Boolean.FALSE, Boolean.TRUE) {
                    int beforeDecimal = 13, afterDecimal = 8;

                    @Override
                    public CharSequence filter(CharSequence source, int start, int end,
                                               Spanned dest, int dstart, int dend) {
                        String temp = mEditSalePriceEditText.getText() + source.toString();

                        if (temp.equals(".")) {
                            return "0.";
                        }else if (temp.equals("0")){
                            return "0.";//if number begins with 0 return decimal place right after
                        }
                        else if (temp.toString().indexOf(".") == -1) {
                            // no decimal point placed yet
                            if (temp.length() > beforeDecimal) {
                                return "";
                            }
                        } else {
                            temp = temp.substring(temp.indexOf(".") + 1);
                            if (temp.length() > afterDecimal) {
                                return "";
                            }
                        }

                        return super.filter(source, start, end, dest, dstart, dend);
                    }
                }
        });

    }

}

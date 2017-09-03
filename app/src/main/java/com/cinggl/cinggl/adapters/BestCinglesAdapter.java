package com.cinggl.cinggl.adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.cinggl.cinggl.Constants;
import com.cinggl.cinggl.R;
import com.cinggl.cinggl.ifair.TradeDetailActivity;
import com.cinggl.cinggl.home.BestCinglesFragment;
import com.cinggl.cinggl.home.CingleDetailActivity;
import com.cinggl.cinggl.home.CingleSettingsDialog;
import com.cinggl.cinggl.home.CommentsActivity;
import com.cinggl.cinggl.home.LikesActivity;
import com.cinggl.cinggl.models.Cingle;
import com.cinggl.cinggl.models.Cingulan;
import com.cinggl.cinggl.models.Like;
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
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by J.EL on 7/19/2017.
 */

//CURRENTLY NOT IN USE

public class BestCinglesAdapter extends RecyclerView.Adapter<BestCinglesViewHolder>{
    private Context mContext;
    private static final String EXTRA_POST_KEY = "post key";
    private static final String EXTRA_USER_UID = "uid";
    private DatabaseReference databaseReference;
    private DatabaseReference commentReference;
    private DatabaseReference usersRef;
    private  DatabaseReference likesRef;
    private DatabaseReference ifairReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseRecyclerAdapter firebaseRecyclerAdapter;
    private Query likesQuery;
    private Query likesQueryCount;
    private boolean processLikes = false;
    private static final double DEFAULT_PRICE = 1.5;
    private static final double GOLDEN_RATIO = 1.618;

    private Query mQuery;
    private static final String TAG = BestCinglesFragment.class.getSimpleName();
    private List<Cingle> bestCingles = new ArrayList<>();

    public BestCinglesAdapter(Context mContext) {
        this.mContext = mContext;

    }

    public void setCingles(List<Cingle> bestCingles) {
        this.bestCingles = bestCingles;
        notifyDataSetChanged();
    }

    public void removeAt(int position){
        bestCingles.remove(bestCingles.get(position));
    }


    public void animate(BestCinglesViewHolder viewHolder){
        final Animation animAnticipateOvershoot = AnimationUtils.loadAnimation(mContext, R.anim.bounce_interpolator);
        viewHolder.itemView.setAnimation(animAnticipateOvershoot);

        final Animation a = AnimationUtils.loadAnimation(mContext, R.anim.anticipate_overshoot_interpolator);
        viewHolder.itemView.setAnimation(a);
    }


    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public int getItemCount() {
        return bestCingles.size();
    }


    @Override
    public BestCinglesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.best_cingles_list, parent, false );

        return new BestCinglesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final BestCinglesViewHolder holder, final int position) {
        final Cingle cingle = bestCingles.get(position);
        final String postKey = bestCingles.get(position).getPushId();
        holder.bindBestCingle(cingle);
//        animate(holder);
        //CALL THE METHOD TO ANIMATE RECYCLER_VIEW
        firebaseAuth = FirebaseAuth.getInstance();

        if (position == bestCingles.size() - 1){
            holder.cingleMomentTextView.setText("The Cingle Of The Moment");
        }else if (position == bestCingles.size() - 2){
            holder.cingleMomentTextView.setText("1st Runners Up Cingle Of The Moment");
        }else if (position == bestCingles.size() - 3){
            holder.cingleMomentTextView.setText("2nd Runners Up Cingle Of The Moment");
        }else {
            holder.cingleMomentRelativeLayout.setVisibility(View.GONE);
        }


        //DATABASE REFERENCE PATH;
        commentReference = FirebaseDatabase.getInstance().getReference(Constants.COMMENTS);
        usersRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_USERS);
        likesRef = FirebaseDatabase.getInstance().getReference(Constants.LIKES);
        likesQuery = likesRef.child(postKey).limitToFirst(5);
        likesQueryCount = likesRef.child(postKey).startAt(6);
        databaseReference = FirebaseDatabase.getInstance()
                .getReference(Constants.FIREBASE_CINGLES);
        ifairReference = FirebaseDatabase.getInstance().getReference(Constants.IFAIR);
        usersRef.keepSynced(true);
        databaseReference.keepSynced(true);
        likesRef.keepSynced(true);
        commentReference.keepSynced(true);

        //DATABASE REFERENCE TO READ THE UID OF THE USER IN THE CINGLE
        databaseReference.child(postKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final String uid = (String) dataSnapshot.child("uid").getValue();

                holder.likesCountTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(mContext, LikesActivity.class);
                        intent.putExtra(BestCinglesAdapter.EXTRA_POST_KEY, postKey);
                        mContext.startActivity(intent);
                    }
                });

                holder.commentsImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent =  new Intent(mContext, CommentsActivity.class);
                        intent.putExtra(BestCinglesAdapter.EXTRA_POST_KEY, postKey);
                        mContext.startActivity(intent);
                    }
                });

                holder.cingleImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(mContext, CingleDetailActivity.class);
                        intent.putExtra(BestCinglesAdapter.EXTRA_POST_KEY, postKey);
                        mContext.startActivity(intent);
                    }
                });

                holder.cingleTradeMethodTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent =  new Intent(mContext, TradeDetailActivity.class);
                        intent.putExtra(BestCinglesAdapter.EXTRA_POST_KEY, postKey);
                        mContext.startActivity(intent);
                    }
                });

                //SHOW CINGLE SETTINGS TO THE CINGLE CREATOR ONLY
                holder.cingleSettingsImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Bundle bundle = new Bundle();
                        bundle.putString(BestCinglesAdapter.EXTRA_POST_KEY, postKey);
                        FragmentManager fragmenManager = ((AppCompatActivity)mContext)
                                .getSupportFragmentManager();
                        CingleSettingsDialog cingleSettingsDialog = CingleSettingsDialog
                                .newInstance("cingle settings");
                        cingleSettingsDialog.setArguments(bundle);
                        cingleSettingsDialog.show(fragmenManager, "cingle settings fragment");
                    }
                });

                holder.profileImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (uid.equals(firebaseAuth.getCurrentUser().getUid())){
                            Intent intent = new Intent(mContext, PersonalProfileActivity.class);
                            intent.putExtra(BestCinglesAdapter.EXTRA_USER_UID, uid);
                            mContext.startActivity(intent);

                        }else {
                            Intent intent = new Intent(mContext, FollowerProfileActivity.class);
                            intent.putExtra(BestCinglesAdapter.EXTRA_USER_UID, uid);
                            mContext.startActivity(intent);
                        }
                    }
                });


                //SET THE TRADE METHOD TEXT ACCORDING TO THE TRADE METHOD OF THE CINGLE
                ifairReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final String uid = dataSnapshot.child("Cingle Selling")
                                .child(postKey).child("uid").getValue(String.class);

//                        //SET CINGLE TRADE METHOD WHEN THERE ARE ALL TRADE METHODS
//                        if (dataSnapshot.child("Cingle Lacing").hasChild(postKey)){
//                            holder.cingleTradeMethodTextView.setText("@CingleLacing");
//                        }else if (dataSnapshot.child("Cingle Leasing").hasChild(postKey)){
//                            holder.cingleTradeMethodTextView.setText("@CingleLeasing");
//
//                        }else if (dataSnapshot.child("Cingle Selling").hasChild(postKey)){
//                            holder.cingleTradeMethodTextView.setText("@CingleSelling");
//                        }else if ( dataSnapshot.child("Cingle Backing").hasChild(postKey)){
//                            holder.cingleTradeMethodTextView.setText("@CingleBacking");
//                        }else {
//                            holder.cingleTradeMethodTextView.setText("@NotForTrade");
//                        }

                        //SET CINGLE ON SALE
                        if (dataSnapshot.child("Cingle Selling").hasChild(postKey)){
                            holder.cingleTradeMethodTextView.setText("@CingleSelling");
                        }

                        //HIDE TRADING LAYOUT IF CINGLE IS NOT ON IFAIR
                        if (!dataSnapshot.child("Cingle Selling").hasChild(postKey)){
                            holder.cingleTradingRelativeLayout.setVisibility(View.GONE);
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                //SET THE CINGULAN CURRENT USERNAME AND PROFILE IMAGE
                if (dataSnapshot.exists()){
                    usersRef.child(uid).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()){
                                final Cingulan cingulan = dataSnapshot.getValue(Cingulan.class);

                                holder.usernameTextView.setText(cingulan.getUsername());
                                Picasso.with(mContext)
                                        .load(cingulan.getProfileImage())
                                        .fit()
                                        .centerCrop()
                                        .placeholder(R.drawable.profle_image_background)
                                        .networkPolicy(NetworkPolicy.OFFLINE)
                                        .into(holder.profileImageView, new Callback() {
                                            @Override
                                            public void onSuccess() {

                                            }

                                            @Override
                                            public void onError() {
                                                Picasso.with(mContext)
                                                        .load(cingulan.getProfileImage())
                                                        .fit()
                                                        .centerCrop()
                                                        .placeholder(R.drawable.profle_image_background)
                                                        .into(holder.profileImageView);
                                            }
                                        });
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }


                //RETRIEVE SENSEPOINTS AND CATCH EXCEPTION IF CINGLE ID DELETED;
                    databaseReference.child(postKey).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()){
                                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                                    Log.d(snapshot.getKey(), snapshot.getChildrenCount() + "sensepoint");
                                }
                                Cingle cingle = dataSnapshot.getValue(Cingle.class);

                                DecimalFormat formatter =  new DecimalFormat("0.00000000");

                                holder.cingleSenseCreditsTextView.setText("CSC" + " " + formatter
                                        .format(cingle.getSensepoint()));

                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });


                //RETRIVE COMMENTS COUNTS AND CATCH EXCEPTION IF CINGLE IS DELETED
                commentReference.child(postKey).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                                Log.e(snapshot.getKey(), snapshot.getChildrenCount() + "commentsCount");
                            }

                            holder.commentsCountTextView.setText(dataSnapshot.getChildrenCount() + "");
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                //RETRIEVE LIKES COUNT AND CATCH EXCEPTIONS IF CINGLE DELETED
                likesQueryCount.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        holder.likesCountTextView.setText("+" + dataSnapshot.getChildrenCount() +" " + "Likes");

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


                //RETRIEVE THE FIRST FIVE USERS WHO LIKED
                likesRef.child(postKey).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.getChildrenCount()>0){
                            holder.likesRecyclerView.setVisibility(View.VISIBLE);
                            //SETUP USERS WHO LIKED THE CINGLE
                            firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Like, UsersWhoLiked>
                                    (Like.class, R.layout.users_who_liked_count, UsersWhoLiked.class, likesQuery) {
                                @Override
                                protected void populateViewHolder(final UsersWhoLiked viewHolder, final Like model, final int position) {
//                                    viewHolder.bindUsersWhoLiked(model);
                                    DatabaseReference userRef = getRef(position);
                                    final String likesPostKey = userRef.getKey();
                                    Log.d(TAG, "likes post key" + likesPostKey);

                                    likesRef.child(postKey).child(likesPostKey).addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.child("uid").exists()){
                                                Log.d(TAG, "uid in likes post" + uid);
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

                            holder.likesRecyclerView.setAdapter(firebaseRecyclerAdapter);
                            holder.likesRecyclerView.setHasFixedSize(false);
                            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, true);
                            layoutManager.setAutoMeasureEnabled(true);
                            holder.likesRecyclerView.setNestedScrollingEnabled(false);
                            holder.likesRecyclerView.setLayoutManager(layoutManager);

                        }else {
                            holder.likesRecyclerView.setVisibility(View.GONE);
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                databaseReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.child(postKey).exists()){
                            holder.likesImageView.setVisibility(View.VISIBLE);
                            holder.likesImageView.setOnClickListener(new View.OnClickListener() {
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
//
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
                        }else {
                            holder.cingleToolsRelativeLayout.setVisibility(View.GONE);
                            holder.likesCountTextView.setVisibility(View.GONE);
                            holder.profileImageView.setVisibility(View.GONE);

                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

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


    public void clearAll(){
        bestCingles.clear();
        notifyDataSetChanged();
    }

}

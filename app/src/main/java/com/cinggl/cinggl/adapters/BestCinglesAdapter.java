package com.cinggl.cinggl.adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.cinggl.cinggl.Constants;
import com.cinggl.cinggl.R;
import com.cinggl.cinggl.home.BestCinglesFragment;
import com.cinggl.cinggl.home.CingleSettingsDialog;
import com.cinggl.cinggl.home.CommentsActivity;
import com.cinggl.cinggl.home.LikesActivity;
import com.cinggl.cinggl.models.Cingle;
import com.cinggl.cinggl.models.Cingulan;
import com.cinggl.cinggl.profile.PersonalProfileActivity;
import com.cinggl.cinggl.relations.FollowerProfileActivity;
import com.cinggl.cinggl.utils.CinglesItemClickListener;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by J.EL on 7/19/2017.
 */

//CURRENTLY NOT IN USE

public class BestCinglesAdapter extends RecyclerView.Adapter<BestCinglesViewHolder> {

    private CinglesItemClickListener cinglesItemClickListener;
    private Context mContext;
    private static final String EXTRA_POST_KEY = "post key";
    private static final String EXTRA_USER_UID = "uid";
    private DatabaseReference databaseReference;
    private DatabaseReference commentReference;
    private DatabaseReference usersRef;
    private  DatabaseReference likesRef;
    private FirebaseAuth firebaseAuth;
    private boolean processLikes = false;
    private static final double DEFAULT_PRICE = 1.5;
    private static final double GOLDEN_RATIO = 1.618;

    private Query mQuery;
    private static final String TAG = BestCinglesFragment.class.getSimpleName();
    private List<Cingle> bestCingles = new ArrayList<>();

    public BestCinglesAdapter(Context mContext, CinglesItemClickListener cinglesItemClickListener) {
        this.mContext = mContext;
        this.cinglesItemClickListener = cinglesItemClickListener;

    }

    public void setCingles(List<Cingle> bestCingles) {
        this.bestCingles = bestCingles;
        notifyDataSetChanged();
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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.best_cingles_list, parent, false );
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

        //DATABASE REFERENCE PATH;
        commentReference = FirebaseDatabase.getInstance().getReference(Constants.COMMENTS);
        usersRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_USERS);
        likesRef = FirebaseDatabase.getInstance().getReference(Constants.LIKES);
        databaseReference = FirebaseDatabase.getInstance()
                .getReference(Constants.FIREBASE_CINGLES);

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


                holder.cingleSettingsImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                });

                //SHOW CINGLE SETTINGS TO THE CINGLE CREATOR ONLY
                if (firebaseAuth.getCurrentUser().getUid().equals(uid)){
                    holder.cingleSettingsImageView.setVisibility(View.VISIBLE);

                    holder.cingleSettingsImageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Bundle args = new Bundle();
                            args.putString(BestCinglesAdapter.EXTRA_POST_KEY, postKey);

                            FragmentManager fragmenManager = ((AppCompatActivity) mContext).getSupportFragmentManager();
                            CingleSettingsDialog cingleSettingsDialog = CingleSettingsDialog.newInstance("cingle settings");
                            cingleSettingsDialog.setArguments(args);
                            cingleSettingsDialog.show(fragmenManager, "new post fragment");


                        }
                    });


                }else {
                    holder.cingleSettingsImageView.setVisibility(View.GONE);
                }

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

                //SET THE CINGULAN CURRENT USERNAME AND PROFILE IMAGE
                try {
                    usersRef.child(uid).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
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

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }catch (Exception e){
                    e.printStackTrace();
                }

                //RETRIEVE SENSEPOINTS AND CATCH EXCEPTION IF CINGLE ID DELETED;
                try {
                    databaseReference.child(postKey).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                                Log.d(snapshot.getKey(), snapshot.getChildrenCount() + "sensepoint");
                            }
                            Cingle cingle = dataSnapshot.getValue(Cingle.class);

                            DecimalFormat formatter =  new DecimalFormat("0.00000000");
                            try {
                                holder.sensePointsTextView.setText("SP" + " " + formatter.format(cingle.getSensepoint()));
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }catch (Exception e){
                    e.printStackTrace();
                }

                //RETRIVE COMMENTS COUNTS AND CATCH EXCEPTION IF CINGLE IS DELETED
                try {
                    commentReference.child(postKey).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                                Log.e(snapshot.getKey(), snapshot.getChildrenCount() + "commentsCount");
                            }

                            holder.commentsCountTextView.setText(dataSnapshot.getChildrenCount() + "");
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }catch (Exception e){
                    e.printStackTrace();
                }

                //RETRIEVE LIKES COUNT AND CATCH EXCEPTION IF CINGLE IS DELETED;
                try {
                    likesRef.child(postKey).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                                Log.e(snapshot.getKey(), snapshot.getChildrenCount() + "likesCount");

                            }
                            holder.likesCountTextView.setText(dataSnapshot.getChildrenCount() + " " + "Likes");

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }catch (Exception e){
                    e.printStackTrace();
                }

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
                                        if(processLikes){
                                            if (dataSnapshot.child(postKey).hasChild(firebaseAuth.getCurrentUser().getUid())){
                                                likesRef.child(postKey)
                                                        .removeValue();
                                                processLikes = false;
                                                onLikeCounter(false);
                                            }else {
                                                likesRef.child(postKey).child(firebaseAuth.getCurrentUser().getUid())
                                                        .child("uid").setValue(firebaseAuth.getCurrentUser().getUid());
                                                processLikes = false;
                                                onLikeCounter(false);
                                            }
                                        }
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

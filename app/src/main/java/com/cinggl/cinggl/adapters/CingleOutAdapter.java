package com.cinggl.cinggl.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.cinggl.cinggl.Constants;
import com.cinggl.cinggl.R;
import com.cinggl.cinggl.backing.BakingDetailActivity;
import com.cinggl.cinggl.home.CingleDetailActivity;
import com.cinggl.cinggl.home.CommentsActivity;
import com.cinggl.cinggl.home.LikesActivity;
import com.cinggl.cinggl.models.Cingle;
import com.cinggl.cinggl.models.Cingulan;
import com.cinggl.cinggl.relations.FollowerProfileActivity;
import com.cinggl.cinggl.profile.PersonalProfileActivity;
import com.google.firebase.auth.FirebaseAuth;
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
import java.util.ArrayList;
import java.util.List;

/**
 * Created by J.EL on 7/19/2017.
 */

public class CingleOutAdapter extends RecyclerView.Adapter<CingleOutViewHolder> {
    private static final String TAG =  CingleOutAdapter.class.getSimpleName();
    private List<Cingle> cingles = new ArrayList<>();
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

    public CingleOutAdapter(Context mContext) {
        this.mContext = mContext;

    }

    public void setCingles(List<Cingle> cingles) {
        this.cingles = cingles;
        notifyDataSetChanged();
    }

    public void removeAt(int position){
        cingles.remove(cingles.get(position));
    }


    public void animate(CingleOutViewHolder viewHolder){
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
        return cingles.size();
    }

    @Override
    public CingleOutViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cingle_out_list, parent, false);
        CingleOutViewHolder cingleOutViewHolder = new CingleOutViewHolder(view);

        return cingleOutViewHolder;
    }

    @Override
    public void onBindViewHolder(final CingleOutViewHolder holder, final int position) {
        final Cingle cingle = cingles.get(position);
        holder.bindCingle(cingle);
        final String postKey = cingles.get(position).getPushId();
        //CALL THE METHOD TO ANIMATE RECYCLERVIEW
//        animate(holder);
        firebaseAuth = FirebaseAuth.getInstance();

        //DATABASE REFERENCE PATH;
        commentReference = FirebaseDatabase.getInstance().getReference(Constants.COMMENTS);
        usersRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_USERS);
        likesRef = FirebaseDatabase.getInstance().getReference(Constants.LIKES);
        databaseReference = FirebaseDatabase.getInstance()
                .getReference(Constants.FIREBASE_CINGLES);

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
                        intent.putExtra(CingleOutAdapter.EXTRA_POST_KEY, postKey);
                        mContext.startActivity(intent);
                    }
                });

                holder.commentsImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent =  new Intent(mContext, CommentsActivity.class);
                        intent.putExtra(CingleOutAdapter.EXTRA_POST_KEY, postKey);
                        mContext.startActivity(intent);
                    }
                });

                holder.cingleImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(mContext, CingleDetailActivity.class);
                        intent.putExtra(CingleOutAdapter.EXTRA_POST_KEY, postKey);
                        mContext.startActivity(intent);
                    }
                });

                holder.cingleTradeMethodTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent =  new Intent(mContext, BakingDetailActivity.class);
//                        intent.putExtra(CingleOutAdapter.EXTRA_POST_KEY, postKey);
                        mContext.startActivity(intent);
                    }
                });

                //SHOW CINGLE SETTINGS TO THE CINGLE CREATOR ONLY
                if (firebaseAuth.getCurrentUser().getUid().equals(uid)){
                    holder.cingleSettingsImageView.setVisibility(View.VISIBLE);

                    holder.cingleSettingsImageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            databaseReference.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.child(postKey).exists()){
                                        if (dataSnapshot.hasChild(postKey)){
                                            databaseReference.child(postKey).removeValue();
                                            cingles.remove(cingles.get(position));
                                        }

                                        notifyItemRemoved(position);
                                        notifyItemRangeChanged(position, cingles.size());
                                    }else {
                                        Log.d("data is deleted", postKey);

                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                        }
                    });


                }else {
                    holder.cingleSettingsImageView.setVisibility(View.GONE);
                }


                holder.profileImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if ((firebaseAuth.getCurrentUser().getUid()).equals(uid)){
                            Intent intent = new Intent(mContext, PersonalProfileActivity.class);
                            intent.putExtra(CingleOutAdapter.EXTRA_USER_UID, uid);
                            mContext.startActivity(intent);

                        }else {
                            Intent intent = new Intent(mContext, FollowerProfileActivity.class);
                            intent.putExtra(CingleOutAdapter.EXTRA_USER_UID, uid);
                            mContext.startActivity(intent);
                        }
                    }
                });


                    //SET THE CINGULAN CURRENT USERNAME AND PROFILE IMAGE
                    if (dataSnapshot.exists()){
                        usersRef.child(uid).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()){
                                    final Cingulan cingulan = dataSnapshot.getValue(Cingulan.class);

                                    holder.accountUsernameTextView.setText(cingulan.getUsername());
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

                //RETRIEVE SENSEPOINTS AND CATCH EXCEPTION IF CINGLE ISN'T DELETED
                   databaseReference.child(postKey).addValueEventListener(new ValueEventListener() {
                       @Override
                       public void onDataChange(DataSnapshot dataSnapshot) {
                          if (dataSnapshot.exists()){
                              for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                                  Log.d(snapshot.getKey(), snapshot.getChildrenCount() + "sensepoint");
                              }
                              Cingle cingle = dataSnapshot.getValue(Cingle.class);

                              DecimalFormat formatter =  new DecimalFormat("0.00000000");
                              holder.cingleSenseCreditsTextView.setText("CSC" + " " + formatter.format(cingle.getSensepoint()));


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
                            if (dataSnapshot.exists()){
                                final Long time = (Long) dataSnapshot.child("timestamp").getValue(Long.class);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });


                //RETRIEVE COMMENTS COUNT AND CATCH EXCEPTIONS IF ANY
                    commentReference.child(postKey).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            if (dataSnapshot.exists()){
                                if (dataSnapshot.exists()){
                                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                                        Log.e(snapshot.getKey(), snapshot.getChildrenCount() + "commentsCount");
                                    }

                                    holder.commentsCountTextView.setText(dataSnapshot.getChildrenCount() + "");
                                }
                            }
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
                                holder.likesCountTextView.setText("+" + dataSnapshot.getChildrenCount() +" " + "Likes");
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



    public void clearAll(){
        cingles.clear();
        notifyDataSetChanged();
    }

    //region listeners
    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

}

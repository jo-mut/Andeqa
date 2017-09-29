package com.cinggl.cinggl.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cinggl.cinggl.Constants;
import com.cinggl.cinggl.R;
import com.cinggl.cinggl.home.BestCinglesFragment;
import com.cinggl.cinggl.home.CingleDetailActivity;
import com.cinggl.cinggl.ifair.TradeDetailActivity;
import com.cinggl.cinggl.models.Cingle;
import com.cinggl.cinggl.models.CingleSale;
import com.cinggl.cinggl.models.Cingulan;
import com.cinggl.cinggl.models.TransactionDetails;
import com.cinggl.cinggl.profile.PersonalProfileActivity;
import com.cinggl.cinggl.relations.FollowerProfileActivity;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import static android.media.CamcorderProfile.get;
import static com.cinggl.cinggl.R.id.cingleImageView;

/**
 * Created by J.EL on 9/14/2017.
 */

public class IfairCingleAdapter extends RecyclerView.Adapter<IfairCinglesViewHolder> {
    private Context mContext;
    private static final String EXTRA_POST_KEY = "post key";
    private static final String EXTRA_USER_UID = "uid";
    private DatabaseReference cinglesReference;
    private DatabaseReference usersRef;
    private DatabaseReference ifairReference;
    private DatabaseReference cingleOwnerReference;
    private FirebaseAuth firebaseAuth;
    private boolean processLikes = false;
    private static final double DEFAULT_PRICE = 1.5;
    private static final double GOLDEN_RATIO = 1.618;
    private static final String TAG = IfairCingleAdapter.class.getSimpleName();
    private List<CingleSale> ifairCingles = new ArrayList<>();
    public static final int MAX_WIDTH = 400;
    public static final int MAX_HEIGHT = 400;

    public IfairCingleAdapter(Context mContext) {
        this.mContext = mContext;

    }

    public void setIfairCingles(List<CingleSale> ifairCingles) {
        this.ifairCingles = ifairCingles;
        notifyDataSetChanged();
    }

    public void removeAt(int position) {
        ifairCingles.remove(ifairCingles.get(position));
    }


    @Override
    public int getItemCount() {
        return ifairCingles.size();
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    @Override
    public IfairCinglesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.ifair_cingles_layout, parent, false );

        return new IfairCinglesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final IfairCinglesViewHolder holder, int position) {
        final CingleSale cingleSale = ifairCingles.get(position);
        final String postKey = ifairCingles.get(position).getPushId();
        Log.d(postKey, "ifair cingles postkey");
//        animate(holder);
        //CALL THE METHOD TO ANIMATE RECYCLER_VIEW
        firebaseAuth = FirebaseAuth.getInstance();

        //DATABASE REFERENCE PATH;
        usersRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_USERS);
        cinglesReference = FirebaseDatabase.getInstance()
                .getReference(Constants.FIREBASE_CINGLES);
        ifairReference = FirebaseDatabase.getInstance().getReference(Constants.IFAIR);
        cingleOwnerReference = FirebaseDatabase.getInstance().getReference(Constants.CINGLE_ONWERS);

        usersRef.keepSynced(true);
        cinglesReference.keepSynced(true);
        cingleOwnerReference.keepSynced(true);

        cinglesReference.child(postKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    final String uid = (String)dataSnapshot.child("uid").getValue();

                    holder.cingleTradeMethodTextView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent =  new Intent(mContext, TradeDetailActivity.class);
                            intent.putExtra(IfairCingleAdapter.EXTRA_POST_KEY, postKey);
                            mContext.startActivity(intent);
                        }
                    });

                    holder.cingleImageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(mContext, TradeDetailActivity.class);
                            intent.putExtra(IfairCingleAdapter.EXTRA_POST_KEY, postKey);
                            mContext.startActivity(intent);
                        }
                    });

                    holder.ownerImageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (uid.equals(firebaseAuth.getCurrentUser().getUid())){
                                Intent intent = new Intent(mContext, PersonalProfileActivity.class);
                                intent.putExtra(IfairCingleAdapter.EXTRA_USER_UID, uid);
                                mContext.startActivity(intent);

                            }else {
                                Intent intent = new Intent(mContext, FollowerProfileActivity.class);
                                intent.putExtra(IfairCingleAdapter.EXTRA_USER_UID, uid);
                                mContext.startActivity(intent);
                            }
                        }
                    });

                    holder.profileImageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (uid.equals(firebaseAuth.getCurrentUser().getUid())){
                                Intent intent = new Intent(mContext, PersonalProfileActivity.class);
                                intent.putExtra(IfairCingleAdapter.EXTRA_USER_UID, uid);
                                mContext.startActivity(intent);

                            }else {
                                Intent intent = new Intent(mContext, FollowerProfileActivity.class);
                                intent.putExtra(IfairCingleAdapter.EXTRA_USER_UID, uid);
                                mContext.startActivity(intent);
                            }
                        }
                    });

                    ifairReference.child("Cingle Selling").child(postKey).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()){
                                CingleSale sale = dataSnapshot.getValue(CingleSale.class);
                                final String uid = sale.getUid();
                                final String pushId = sale.getPushId();
                                final double salePrice = sale.getSalePrice();

                                DecimalFormat formatter =  new DecimalFormat("0.00000000");
                                holder.cingleSalePriceTextView.setText("CSC" + " " + "" + formatter.format(sale.getSalePrice()));

                                //retrieve cingle info
                                cinglesReference.child(pushId).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.exists()){
                                            final Cingle cingle = dataSnapshot.getValue(Cingle.class);

                                            Picasso.with(mContext)
                                                    .load(cingle.getCingleImageUrl())
                                                    .networkPolicy(NetworkPolicy.OFFLINE)
                                                    .into(holder.cingleImageView, new Callback() {
                                                        @Override
                                                        public void onSuccess() {

                                                        }

                                                        @Override
                                                        public void onError() {
                                                            Picasso.with(mContext)
                                                                    .load(cingle.getCingleImageUrl())
                                                                    .into(holder.cingleImageView);


                                                        }
                                                    });
                                            DecimalFormat formatter =  new DecimalFormat("0.00000000");
                                            holder.cingleSenseCreditsTextView.setText("CSC" + " " + "" + formatter.format(cingle.getSensepoint()));
                                            holder.datePostedTextView.setText(cingle.getDatePosted());

                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });

                                //retrieve user info
                                usersRef.child(uid).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.exists()){
                                            final Cingulan cingulan = dataSnapshot.getValue(Cingulan.class);

                                            Picasso.with(mContext)
                                                    .load(cingulan.getProfileImage())
                                                    .networkPolicy(NetworkPolicy.OFFLINE)
                                                    .into(holder.profileImageView, new Callback() {
                                                        @Override
                                                        public void onSuccess() {

                                                        }

                                                        @Override
                                                        public void onError() {
                                                            Picasso.with(mContext)
                                                                    .load(cingulan.getProfileImage())
                                                                    .into(holder.profileImageView);


                                                        }
                                                    });
                                            holder.usernameTextView.setText(cingulan.getUsername());

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

                    //SET THE TRADE METHOD TEXT ACCORDING TO THE TRADE METHOD OF THE CINGLE
                    ifairReference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            final String uid = dataSnapshot.child("Cingle Selling")
                                    .child(postKey).child("uid").getValue(String.class);

                            //SET CINGLE TRADE METHOD WHEN THERE ARE ALL TRADE METHODS
                            if (dataSnapshot.child("Cingle Lacing").hasChild(postKey)){
                                holder.cingleTradeMethodTextView.setText("@CingleLacing");
                            }else if (dataSnapshot.child("Cingle Leasing").hasChild(postKey)){
                                holder.cingleTradeMethodTextView.setText("@CingleLeasing");

                            }else if (dataSnapshot.child("Cingle Selling").hasChild(postKey)){
                                holder.cingleTradeMethodTextView.setText("@CingleSelling");
                            }else if ( dataSnapshot.child("Cingle Backing").hasChild(postKey)){
                                holder.cingleTradeMethodTextView.setText("@CingleBacking");
                            }else {
                                holder.cingleTradeMethodTextView.setText("@NotForTrade");
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });


                    /**display the person who currently owns the cingle*/
                    cingleOwnerReference.child(postKey).child("owner").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            TransactionDetails transactionDetails = dataSnapshot.getValue(TransactionDetails.class);
                            final String ownerUid = transactionDetails.getUid();

                            usersRef.child(ownerUid).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    Cingulan cingulan = dataSnapshot.getValue(Cingulan.class);
                                    final String username = cingulan.getUsername();
                                    final String profileImage = cingulan.getProfileImage();
                                    holder.cingleOwnerTextView.setText(username);
                                    Picasso.with(mContext)
                                            .load(profileImage)
                                            .fit()
                                            .centerCrop()
                                            .placeholder(R.drawable.profle_image_background)
                                            .networkPolicy(NetworkPolicy.OFFLINE)
                                            .into(holder.ownerImageView, new Callback() {
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
                                                            .into(holder.ownerImageView);
                                                }
                                            });
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

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

}

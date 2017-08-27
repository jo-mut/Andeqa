package com.cinggl.cinggl.profile;


import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cinggl.cinggl.Constants;
import com.cinggl.cinggl.R;
import com.cinggl.cinggl.adapters.CingleOutAdapter;
import com.cinggl.cinggl.adapters.ProfileCinglesViewHolder;
import com.cinggl.cinggl.home.CingleSettingsDialog;
import com.cinggl.cinggl.home.CommentsActivity;
import com.cinggl.cinggl.home.HomeActivity;
import com.cinggl.cinggl.home.HomeFragment;
import com.cinggl.cinggl.home.LikesActivity;
import com.cinggl.cinggl.ifair.WalletActivity;
import com.cinggl.cinggl.lacing.LacingFragment;
import com.cinggl.cinggl.lacing.LacingPagerAdapter;
import com.cinggl.cinggl.models.Cingle;
import com.cinggl.cinggl.models.Cingulan;
import com.cinggl.cinggl.preferences.SettingsActivity;
import com.cinggl.cinggl.relations.PeopleActivity;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

import static android.R.attr.fragment;
import static android.icu.lang.UCharacter.GraphemeClusterBreak.L;
import static android.os.Build.VERSION_CODES.M;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment implements View.OnClickListener{
    @Bind(R.id.profileCinglesRecyclerView)RecyclerView mProfileCinglesRecyclerView;
    @Bind(R.id.userProfileImageView)CircleImageView mProifleImageView;
    @Bind(R.id.firstNameTextView)TextView mFirstNameTextView;
    @Bind(R.id.secondNameTextView)TextView  mSecondNameTextView;
    @Bind(R.id.bioTextView)TextView mBioTextView;
    @Bind(R.id.followersCountTextView) TextView mFollowersCountTextView;
    @Bind(R.id.followingCountTextView)TextView mFollowingCountTextView;
    @Bind(R.id.cinglesCountTextView)TextView mCinglesCountTextView;
    @Bind(R.id.header_cover_image)ImageView mProfileCover;
    @Bind(R.id.editProfileImageView)ImageView mEditProfileImageView;


    private DatabaseReference databaseReference;
    private FirebaseRecyclerAdapter firebaseRecyclerAdapter;
    private Query profileCinglesQuery;
    private Query relationsQuery;
    private Query profileInfoQuery;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private DatabaseReference usernameRef;
    private DatabaseReference likesRef;
    private boolean processFollow = false;
    private DatabaseReference relationsRef;
    private DatabaseReference commentReference;
    private FragmentManager fragmentManager;
    private boolean processLikes = false;
    private static final String TAG = "ProfileFragment";
    private  static final int MAX_WIDTH = 300;
    private static final int MAX_HEIGHT = 300;
    private static final double GOLDEN_RATIO = 1.618;
    private static final double DEFAULT_PRICE = 1.5;
    private static final String EXTRA_POST_KEY = "post key";
    private static final String EXTRA_USER_UID = "uid";
    private static final int TOTAL_ITEM_EACH_LOAD = 10;
    private int currentPage = 0;


    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        databaseReference = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CINGLES);
        profileInfoQuery = databaseReference.orderByChild("uid").equalTo(firebaseAuth.getCurrentUser().getUid());
        relationsRef = FirebaseDatabase.getInstance().getReference(Constants.RELATIONS);
        usernameRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_USERS);
        relationsQuery = relationsRef.orderByChild("uid").equalTo(firebaseAuth.getCurrentUser().getUid());
        commentReference = FirebaseDatabase.getInstance().getReference(Constants.COMMENTS);
        likesRef = FirebaseDatabase.getInstance().getReference(Constants.LIKES);
        profileCinglesQuery = databaseReference.orderByChild("uid").equalTo(firebaseAuth.getCurrentUser().getUid());

        fragmentManager = getChildFragmentManager();

        databaseReference.keepSynced(true);
        profileInfoQuery.keepSynced(true);
        usernameRef.keepSynced(true);
        likesRef.keepSynced(true);
        commentReference.keepSynced(true);
        relationsRef.keepSynced(true);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        ButterKnife.bind(this, view);

        setUpFirebaseAdapter();
        fetchData();

        mFollowingCountTextView.setOnClickListener(this);
        mFollowersCountTextView.setOnClickListener(this);
        mEditProfileImageView.setOnClickListener(this);

        profileCinglesQuery.keepSynced(true);
        databaseReference.keepSynced(true);
        usernameRef.keepSynced(true);
        relationsRef.keepSynced(true);

        return view;
    }



    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.profile_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action b item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_account_settings) {
            Intent intent = new Intent(getActivity(), SettingsActivity.class);
            startActivity(intent);
        }

        if (id == R.id.action_wallet){
            Intent intent = new Intent(getActivity(), WalletActivity.class);
            startActivity(intent);
        }


        return super.onOptionsItemSelected(item);
    }

    private void fetchData(){

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
        relationsRef.child("followers").child(firebaseAuth.getCurrentUser().getUid())
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Log.e(snapshot.getKey(), snapshot.getChildrenCount() + "followers Count");

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
        relationsRef.child("following").child(firebaseAuth.getCurrentUser().getUid())
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

        usernameRef.child(firebaseAuth.getCurrentUser().getUid())
                .addValueEventListener(new ValueEventListener() {
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

                   Picasso.with(getContext())
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
                                   Picasso.with(getContext())
                                           .load(profileImage)
                                           .resize(MAX_WIDTH, MAX_HEIGHT)
                                           .onlyScaleDown()
                                           .centerCrop()
                                           .placeholder(R.drawable.profle_image_background)
                                           .into(mProifleImageView);

                               }
                           });

                   Picasso.with(getContext())
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
                                   Picasso.with(getContext())
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

    private void setUpFirebaseAdapter(){
        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Cingle, ProfileCinglesViewHolder>
                (Cingle.class, R.layout.cingle_out_list, ProfileCinglesViewHolder.class, profileCinglesQuery) {
            @Override
            protected void populateViewHolder(final ProfileCinglesViewHolder viewHolder, final Cingle model, final int position) {
                final String postKey = getRef(position).getKey();
                viewHolder.bindProfileCingle(model);

                //DATABASE REFERENCE TO READ THE UID OF THE USER IN THE CINGLE
                databaseReference.child(postKey).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final String uid = (String) dataSnapshot.child("uid").getValue();

                        viewHolder.likesCountTextView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(getContext(), LikesActivity.class);
                                intent.putExtra(ProfileFragment.EXTRA_POST_KEY, postKey);
                                getContext().startActivity(intent);
                            }
                        });

                        viewHolder.commentsImageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent =  new Intent(getContext(), CommentsActivity.class);
                                intent.putExtra(ProfileFragment.EXTRA_POST_KEY, postKey);
                                getContext().startActivity(intent);
                            }
                        });

                        viewHolder.cingleSettingsImageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Bundle bundle = new Bundle();
                                bundle.putString(ProfileFragment.EXTRA_POST_KEY, postKey);
                                FragmentManager fragmenManager = getChildFragmentManager();
                                CingleSettingsDialog cingleSettingsDialog = CingleSettingsDialog.newInstance("cingle settings");
                                cingleSettingsDialog.setArguments(bundle);
                                cingleSettingsDialog.show(fragmenManager, "cingle settings fragment");
                            }
                        });


                        //RETRIEVE USER INFO IF AND CATCH EXCEPTION IF CINGLES IS NOT DELETED;

                            //SET THE CINGULAN CURRENT USERNAME AND PROFILE IMAGE
                            if (dataSnapshot.exists()){
                                usernameRef.child(uid).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        final Cingulan cingulan = dataSnapshot.getValue(Cingulan.class);

                                        viewHolder.accountUsernameTextView.setText(cingulan.getUsername());
                                        Picasso.with(getContext())
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
                                                        Picasso.with(getContext())
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

                                        viewHolder.sensePointsTextView.setText("CSC" + " " + formatter.format(cingle.getSensepoint()));

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

                                        for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                                            Log.e(snapshot.getKey(), snapshot.getChildrenCount() + "commentsCount");
                                        }

                                        viewHolder.commentsCountTextView.setText(dataSnapshot.getChildrenCount() + "");
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
                                   if (dataSnapshot.exists()){
                                       for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                                           Log.e(snapshot.getKey(), snapshot.getChildrenCount() + "likesCount");

                                       }
                                       viewHolder.likesCountTextView.setText("+" + dataSnapshot.getChildrenCount());

                                   }
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

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

        };

        mProfileCinglesRecyclerView.setAdapter(firebaseRecyclerAdapter);
        mProfileCinglesRecyclerView.setHasFixedSize(false);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setAutoMeasureEnabled(true);
        mProfileCinglesRecyclerView.setNestedScrollingEnabled(false);
        mProfileCinglesRecyclerView.setLayoutManager(layoutManager);

    }

    @Override
    public void onClick(View v){
        if (v == mFollowingCountTextView) {
            Intent intent = new Intent(getActivity(), PeopleActivity.class);
            startActivity(intent);
        }

        if (v == mFollowersCountTextView){
            Intent intent = new Intent(getActivity(), PeopleActivity.class);
            startActivity(intent);
        }

        if (v == mEditProfileImageView){
            Intent intent = new Intent(getActivity(), UpdateProfileActivity.class);
            startActivity(intent);
        }

    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onStop(){
        super.onStop();
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
//
//    // Method to manually check connection status
//    private void checkConnection() {
//        boolean isConnected = ConnectivityReceiver.isConnected();
//        showConnection(isConnected);
//    }
//
//    //Showing the status in Snackbar
//    private void showConnection(boolean isConnected) {
//        String message;
//        if (isConnected) {
//            mConnectonEstablishedTextView.setText("Connection established");
//
//            final Handler handler = new Handler();
//            Timer t = new Timer();
//            t.schedule(new TimerTask() {
//                public void run() {
//                    handler.post(new Runnable() {
//                        public void run() {
//                            mNetworkRelativeLayout.setVisibility(View.GONE);
//                        }
//                    });
//                }
//            }, 2000);
//
//        } else {
//            mNetworkRelativeLayout.setVisibility(View.VISIBLE);
//            mConnectonEstablishedTextView.setText("Disconnected");
//        }
//
////        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
//
//    }
//
//    @Override
//    public void onResume() {
//        super.onResume();
//
//        // register connection status listener
//        App.getInstance().setConnectivityListener(this);
//        checkConnection();
//    }
//
//    /**
//     * Callback will be triggered when there is change in
//     * network connection
//     */
//    @Override
//    public void onNetworkConnectionChanged(boolean isConnected) {
//        showConnection(isConnected);
//    }

}

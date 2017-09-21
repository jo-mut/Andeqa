package com.cinggl.cinggl.profile;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cinggl.cinggl.Constants;
import com.cinggl.cinggl.R;
import com.cinggl.cinggl.adapters.CingleOutAdapter;
import com.cinggl.cinggl.adapters.ProfileCinglesAdapter;
import com.cinggl.cinggl.adapters.ProfileCinglesViewHolder;
import com.cinggl.cinggl.home.CingleSettingsDialog;
import com.cinggl.cinggl.home.CommentsActivity;
import com.cinggl.cinggl.home.LikesActivity;
import com.cinggl.cinggl.ifair.WalletActivity;
import com.cinggl.cinggl.models.Balance;
import com.cinggl.cinggl.models.Cingle;
import com.cinggl.cinggl.models.Cingulan;
import com.cinggl.cinggl.relations.PeopleActivity;
import com.cinggl.cinggl.utils.ExpandableTextView;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
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

import butterknife.Bind;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

import static android.os.Build.VERSION_CODES.M;
import static com.cinggl.cinggl.R.id.cingleOutRecyclerView;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment implements View.OnClickListener{

    //BIND VIEWS
    @Bind(R.id.profileCinglesRecyclerView)RecyclerView mProfileCinglesRecyclerView;
    @Bind(R.id.profileImageView)CircleImageView mProifleImageView;
    @Bind(R.id.firstNameTextView)TextView mFirstNameTextView;
    @Bind(R.id.secondNameTextView)TextView  mSecondNameTextView;
    @Bind(R.id.bioTextView)TextView mBioTextView;
    @Bind(R.id.followersCountTextView) TextView mFollowersCountTextView;
    @Bind(R.id.followingCountTextView)TextView mFollowingCountTextView;
    @Bind(R.id.cinglesCountTextView)TextView mCinglesCountTextView;
    @Bind(R.id.header_cover_image)ImageView mProfileCover;
    @Bind(R.id.editProfileImageView)ImageView mEditProfileImageView;

    //DATABASE REFERENCES
    private FirebaseRecyclerAdapter firebaseRecyclerAdapter;
    private Query profileCinglesQuery;
    private Query relationsQuery;
    private DatabaseReference relationsRef;
    private DatabaseReference ifairReference;
    private DatabaseReference cingleWalletReference;
    private boolean processLikes = false;
    private static final String TAG = ProfileFragment.class.getSimpleName();
    private  static final int MAX_WIDTH = 300;
    private static final int MAX_HEIGHT = 300;
    private static final double GOLDEN_RATIO = 1.618;
    private static final double DEFAULT_PRICE = 1.5;
    private static final String EXTRA_POST_KEY = "post key";
    private int mProfileCinglesRecyclerViewPosition = 0;
    private DatabaseReference databaseReference;
    private Query cinglesQuery;
    private ChildEventListener mChildEventListener;
    private DatabaseReference usernameRef;
    private DatabaseReference likesRef;
    private FirebaseAuth firebaseAuth;
    private ProfileCinglesAdapter profileCinglesAdapter;
    private DatabaseReference sensepointRef;
    private DatabaseReference commentReference;
    private DatabaseReference profileCinglesReference;
    private LinearLayoutManager layoutManager;

    private List<Cingle> cingles = new ArrayList<>();
    private List<String> cinglesIds = new ArrayList<>();

    private int currentPage = 0;
    private static final int TOTAL_ITEM_EACH_LOAD = 10;
    private static final String KEY_LAYOUT_POSITION = "layout pooition";
    private int cingleOutRecyclerViewPosition = 0;


    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        ButterKnife.bind(this, view);

        //FIREBASE AUTH
        firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser()!= null){
            //DATABASE REFERENCE
            databaseReference = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CINGLES);
            profileCinglesQuery = databaseReference.orderByChild("uid").equalTo(firebaseAuth.getCurrentUser().getUid());
            relationsRef = FirebaseDatabase.getInstance().getReference(Constants.RELATIONS);
            usernameRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_USERS);
            relationsQuery = relationsRef.orderByChild("uid").equalTo(firebaseAuth.getCurrentUser().getUid());
            commentReference = FirebaseDatabase.getInstance().getReference(Constants.COMMENTS);
            likesRef = FirebaseDatabase.getInstance().getReference(Constants.LIKES);
            sensepointRef = FirebaseDatabase.getInstance().getReference("Sense points");
            ifairReference = FirebaseDatabase.getInstance().getReference(Constants.IFAIR);
            cingleWalletReference = FirebaseDatabase.getInstance().getReference(Constants.CINGLE_WALLET);
            profileCinglesReference =  FirebaseDatabase.getInstance().getReference(Constants.PROFILE_CINGLES)
                    .child(firebaseAuth.getCurrentUser().getUid());

            //KEEP DATABASE SYNCED
            databaseReference.keepSynced(true);
            cingleWalletReference.keepSynced(true);
            usernameRef.keepSynced(true);
            likesRef.keepSynced(true);
            commentReference.keepSynced(true);
            relationsRef.keepSynced(true);
            profileCinglesReference.keepSynced(true);

            fetchData();
//            setUpFirebaseAdapter();
            setProfileCingles(currentPage);

            //INITIALIZE CLICK LISTENERS
            mEditProfileImageView.setOnClickListener(this);
            mFollowersCountTextView.setOnClickListener(this);
            mFollowingCountTextView.setOnClickListener(this);

        }

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initializeViewsAdapter();

        if (savedInstanceState != null){
            //restore saved layout manager type
            mProfileCinglesRecyclerViewPosition = (int) savedInstanceState
                    .getSerializable(KEY_LAYOUT_POSITION);
            mProfileCinglesRecyclerView.scrollToPosition(mProfileCinglesRecyclerViewPosition);
        }
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

        if (id == R.id.action_wallet){
            Intent intent = new Intent(getActivity(), WalletActivity.class);
            startActivity(intent);
        }

        if (id == R.id.action_signout){
            firebaseAuth.signOut();
            startActivity(new Intent(getActivity(), SignInActivity.class));

        }
        return super.onOptionsItemSelected(item);
    }



    private void initializeViewsAdapter(){
        layoutManager =  new LinearLayoutManager(getContext());
        mProfileCinglesRecyclerView.setLayoutManager(layoutManager);
        mProfileCinglesRecyclerView.setHasFixedSize(true);
        profileCinglesAdapter = new ProfileCinglesAdapter(getContext());
        mProfileCinglesRecyclerView.setAdapter(profileCinglesAdapter);
        profileCinglesAdapter.notifyDataSetChanged();
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

    public void setProfileCingles(int start){
//        progressBar.setVisibility(View.VISIBLE);
        cinglesQuery = profileCinglesReference.orderByChild("number").startAt(start)
                .endAt(start + TOTAL_ITEM_EACH_LOAD);
        cinglesQuery.keepSynced(true);

        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d("Snapshot", dataSnapshot.toString());
//                progressBar.setVisibility(View.GONE);

                Cingle cingle = dataSnapshot.getValue(Cingle.class);
                cinglesIds.add(dataSnapshot.getKey());
                cingles.add(cingle);

                currentPage += 10;
                profileCinglesAdapter.setProfileCingles(cingles);
                profileCinglesAdapter.notifyItemInserted(cingles.size());
                profileCinglesAdapter.getItemCount();
                Log.d("size of all cingles", cingles.size() + "");

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Cingle cingle =  dataSnapshot.getValue(Cingle.class);

                String cingle_key = dataSnapshot.getKey();

                //exclude
                int cingle_index = cinglesIds.indexOf(cingle_key);
                if (cingle_index > - 1){

                    //replace with the new cingle
                    cingles.set(cingle_index, cingle);
                    profileCinglesAdapter.notifyItemChanged(cingle_index);
                    profileCinglesAdapter.notifyDataSetChanged();
                    profileCinglesAdapter.getItemCount();
                }else {
                    Log.w(TAG, "onChildChanged:unknown_child" + cingle_key);
                }


            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onChiledRemoved:" + dataSnapshot.getKey());

                //a cingle has changed. use the key to determine if the cingle
                // is being displayed and
                //so remove it.
                String cingle_key = dataSnapshot.getKey();
                //exclude
                int cingle_index = cinglesIds.indexOf(cingle_key);
                if (cingle_index > - 1){
                    //remove data from the list
                    cinglesIds.remove(cingle_index);
                    cingles.remove(cingle_key);
                    profileCinglesAdapter.removeAt(cingle_index);
                    profileCinglesAdapter.notifyItemRemoved(cingle_index);

                }else {
                    Log.w(TAG, "onChildRemoved:unknown_child:" + cingle_key);
                }

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                Cingle cingle = dataSnapshot.getValue(Cingle.class);
                String cingle_key = dataSnapshot.getKey();

                //...

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "load Cingles : onCancelled", databaseError.toException());
                Toast.makeText(getContext(), "Failed to load comments.", Toast.LENGTH_SHORT).show();

            }
        };
        cinglesQuery.addChildEventListener(childEventListener);
        mChildEventListener = childEventListener;
    }

    public void cleanUpListener(){
        if (mChildEventListener != null){
            cinglesQuery.removeEventListener(mChildEventListener);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        //save currently selected layout manager;
        int recyclerViewScrollPosition =  getRecyclerViewScrollPosition();
        Log.d(TAG, "Recycler view scroll position:" + recyclerViewScrollPosition);
        outState.putSerializable(KEY_LAYOUT_POSITION, recyclerViewScrollPosition);
        super.onSaveInstanceState(outState);

    }

    private int getRecyclerViewScrollPosition() {
        int scrollPosition = 0;
        // TODO: Is null check necessary?
        if (mProfileCinglesRecyclerView != null && mProfileCinglesRecyclerView.getLayoutManager() != null) {
            scrollPosition = ((LinearLayoutManager)  mProfileCinglesRecyclerView.getLayoutManager())
                    .findFirstCompletelyVisibleItemPosition();
        }
        return scrollPosition;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cleanUpListener();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
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
                        if (dataSnapshot.exists()){
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


                            //SET THE TRADE METHOD TEXT ACCORDING TO THE TRADE METHOD OF THE CINGLE
                            ifairReference.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.child("Cingle Lacing").hasChild(postKey)){
                                        viewHolder.cingleTradeMethodTextView.setText("@CingleLacing");
                                    }else if (dataSnapshot.child("Cingle Leasing").hasChild(postKey)){
                                        viewHolder.cingleTradeMethodTextView.setText("@CingleLeasing");

                                    }else if (dataSnapshot.child("Cingle Selling").hasChild(postKey)){
                                        viewHolder.cingleTradeMethodTextView.setText("@CingleSelling");
                                    }else if ( dataSnapshot.child("Cingle Backing").hasChild(postKey)){
                                        viewHolder.cingleTradeMethodTextView.setText("@CingleBacking");
                                    }else {
                                        viewHolder.cingleTradeMethodTextView.setText("@NotForTrade");
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

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

                            likesRef.child(postKey).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()){
                                        for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                                            Log.e(snapshot.getKey(), snapshot.getChildrenCount() + "likesCount");
                                        }

                                        viewHolder.likesCountTextView.setText(dataSnapshot.getChildrenCount() + " ");

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
//
                                                final double finalPoints = round( cingleWorth, 10);

                                                Log.d("final points", finalPoints + "");

                                                cingleWalletReference.child(postKey).addListenerForSingleValueEvent(new ValueEventListener() {
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
                                                            databaseReference.child(postKey).child("sensepoint").setValue(totalSenseCredits);
                                                        }else {
                                                            databaseReference.child(postKey).child("sensepoint").setValue(finalPoints);
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
                                                cingleWalletReference.child(postKey).addListenerForSingleValueEvent(new ValueEventListener() {
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
                                                            databaseReference.child(postKey).child("sensepoint").setValue(totalSenseCredits);
                                                        }else {
                                                            databaseReference.child(postKey).child("sensepoint").setValue(finalPoints);
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
                            });
                        }

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

}

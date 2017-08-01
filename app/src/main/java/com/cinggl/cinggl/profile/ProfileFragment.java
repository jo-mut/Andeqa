package com.cinggl.cinggl.profile;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cinggl.cinggl.App;
import com.cinggl.cinggl.Constants;
import com.cinggl.cinggl.R;
import com.cinggl.cinggl.adapters.ProfileCinglesViewHolder;
import com.cinggl.cinggl.home.CingleSettingsDialog;
import com.cinggl.cinggl.home.HomeActivity;
import com.cinggl.cinggl.models.Cingle;
import com.cinggl.cinggl.relations.PeopleActivity;
import com.cinggl.cinggl.services.ConnectivityReceiver;
import com.cinggl.cinggl.ui.MainActivity;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.Timer;
import java.util.TimerTask;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

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


    private DatabaseReference databaseReference;
    private FirebaseRecyclerAdapter firebaseRecyclerAdapter;
    private Query profileCinglesQuery;
    private Query relationsQuery;
    private Query profileInfoQuery;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private DatabaseReference usernameRef;
    private boolean processFollow = false;
    private DatabaseReference relationsRef;
    private FragmentManager fragmentManager;
    private static final String TAG = "ProfileFragment";
    private  static final int MAX_WIDTH = 300;
    private static final int MAX_HEIGHT = 300;
    private String mUid;
    private static final String EXTRA_USER_UID = "uid";


    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        mUid = firebaseUser.getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CINGLES);
        profileCinglesQuery = databaseReference.orderByChild("uid").equalTo(firebaseAuth.getCurrentUser().getUid());
        databaseReference = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_USERS);
        profileInfoQuery = databaseReference.orderByChild("uid").equalTo(firebaseAuth.getCurrentUser().getUid());
        usernameRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_USERS);
        relationsRef = FirebaseDatabase.getInstance().getReference(Constants.RELATIONS);
        relationsQuery = relationsRef.orderByChild("uid").equalTo(firebaseAuth.getCurrentUser().getUid());
        fragmentManager = getChildFragmentManager();

        databaseReference.keepSynced(true);
        profileCinglesQuery.keepSynced(true);
        profileInfoQuery.keepSynced(true);
        usernameRef.keepSynced(true);
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

        databaseReference.keepSynced(true);
        usernameRef.keepSynced(true);
        relationsRef.keepSynced(true);

        return view;
    }

//    @Override
//    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        super.onCreateOptionsMenu(menu, inflater);
//        inflater.inflate(R.menu.profile_menu, menu);
//    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action b item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_account_settings) {
//            launchSettings();
            Intent intent = new Intent(getActivity(), UpdateProfileActivity.class);
            startActivity(intent);
            return true;
        }

        if (id == R.id.action_home){
            Intent intent = new Intent(getActivity(), HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    /**method to launch settings activity*/
//    public void launchSettings(){
//        Intent intentSettings = new Intent(getActivity(), SettingsActivity.class);
//        startActivity(intentSettings);
//    }



    private void fetchData(){

        profileCinglesQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {


                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Log.e(snapshot.getKey(), snapshot.getChildrenCount() + "cingles Count");

                    if (dataSnapshot.hasChildren()){
                        mCinglesCountTextView.setText(dataSnapshot.getChildrenCount()+ "");
                    }else {
                        mCinglesCountTextView.setText("0");
                    }

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
                String firstName = (String) dataSnapshot.child("firstName").getValue();
                String secondName = (String) dataSnapshot.child("secondName").getValue();
                final String profileImage = (String) dataSnapshot.child("profileImage").getValue();
                String bio = (String) dataSnapshot.child("bio").getValue();

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

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setUpFirebaseAdapter(){

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Cingle, ProfileCinglesViewHolder>
                (Cingle.class, R.layout.profile_cingles_item_list, ProfileCinglesViewHolder.class, profileCinglesQuery) {
            @Override
            protected void populateViewHolder(final ProfileCinglesViewHolder viewHolder, final Cingle model, int position) {
                final String postKey = getRef(position).getKey();

                profileCinglesQuery.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChildren()){
//                            viewHolder.noCinglesRelativeLayout.setVisibility(View.GONE);
                            viewHolder.bindProfileCingle(model);
                        }else {
//                            viewHolder.noCinglesRelativeLayout.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getActivity(), CingleDetailActivity.class);
                        intent.putExtra("cingle_id", postKey);
                        startActivity(intent);
                    }
                });

            }

        };

        mProfileCinglesRecyclerView.setAdapter(firebaseRecyclerAdapter);
        mProfileCinglesRecyclerView.setHasFixedSize(false);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getContext(), 3);
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

package com.cinggl.cinggl.profile;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.cinggl.cinggl.Constants;
import com.cinggl.cinggl.R;
import com.cinggl.cinggl.adapters.FirebaseProfileCinglesViewHolder;
import com.cinggl.cinggl.adapters.ProfileInfoViewHolder;
import com.cinggl.cinggl.models.Cingle;
import com.cinggl.cinggl.models.Cingulan;
import com.cinggl.cinggl.ui.SettingsActivity;
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

import butterknife.Bind;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment implements View.OnClickListener{
    @Bind(R.id.profileCinglesRecyclerView)RecyclerView mProfileCinglesRecyclerView;
    @Bind(R.id.profileImageView)CircleImageView mProifleImageView;
    @Bind(R.id.firstNameTextView)TextView mFirstNameTextView;
    @Bind(R.id.secondNameTextView)TextView  mSecondNameTextView;
    @Bind(R.id.bioTextView)TextView mBioTextView;
    @Bind(R.id.followersCountTextView) TextView mFollowersCountTextView;
    @Bind(R.id.followingCountTextView)TextView mFollowingCountTextView;
    @Bind(R.id.cinglesCountTextView)TextView mCinglesCountTextView;
    @Bind(R.id.followButton)Button mFollowButton;


    private DatabaseReference databaseReference;
    private DatabaseReference usersRef;
    private FirebaseRecyclerAdapter firebaseRecyclerAdapter;
    private Query profileCinglesQuery;
    private Query profileInfoQuery;
    public String userKey;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private DatabaseReference usernameRef;
    private DatabaseReference relationsRef;
    private boolean processFollow = false;
    private DatabaseReference followingRef;
    private DatabaseReference followersRef;
    private static final String TAG = "ProfileFragment";


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
        profileCinglesQuery = databaseReference.orderByChild("uid").equalTo(firebaseAuth.getCurrentUser().getUid());
        databaseReference = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_USERS);
        profileInfoQuery = databaseReference.orderByChild("uid").equalTo(firebaseAuth.getCurrentUser().getUid());
        usernameRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_USERS)
                .child(firebaseAuth.getCurrentUser().getUid());
        followingRef = FirebaseDatabase.getInstance().getReference(Constants.FOLLOWERS)
                .child(firebaseAuth.getCurrentUser().getUid());
        followersRef = FirebaseDatabase.getInstance().getReference(Constants.FOLLOWERS)
                .child(firebaseAuth.getCurrentUser().getUid());


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        ButterKnife.bind(this, view);

        setUpFirebaseAdapter();

        mFollowButton.setOnClickListener(this);
        databaseReference.keepSynced(true);
        usernameRef.keepSynced(true);
        followersRef.keepSynced(true);
        followingRef.keepSynced(true);

        profileCinglesQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Log.e(snapshot.getKey(), snapshot.getChildrenCount() + "cingles Count");

                    mCinglesCountTextView.setText(dataSnapshot.getChildrenCount()+ "");

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        followersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Log.e(snapshot.getKey(), snapshot.getChildrenCount() + "followers Count");

                    mFollowersCountTextView.setText(dataSnapshot.getChildrenCount() + "");

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        followingRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.hasChild(firebaseAuth.getCurrentUser().getUid())){
                    Log.e(dataSnapshot.getKey(), dataSnapshot.getChildrenCount() + "following Count");
                    mFollowingCountTextView.setText(dataSnapshot.getChildrenCount() + "");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        usernameRef.addValueEventListener(new ValueEventListener() {
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
                        .fit()
                        .centerCrop()
                        .networkPolicy(NetworkPolicy.OFFLINE)
                        .into(mProifleImageView, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError() {
                                Picasso.with(getContext())
                                        .load(profileImage)
                                        .fit()
                                        .centerCrop()
                                        .into(mProifleImageView);

                            }
                        });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            launchSettings();
            return true;
        }

        if(id == R.id.action_search){
            return true;
        }

        if(id == R.id.action_notifications){
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void launchSettings(){
        Intent intentSettings = new Intent(getActivity(), SettingsActivity.class);
        startActivity(intentSettings);
    }


    private void setUpFirebaseAdapter(){

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Cingle, FirebaseProfileCinglesViewHolder>
                (Cingle.class, R.layout.profile_cingles_item_list, FirebaseProfileCinglesViewHolder.class, profileCinglesQuery) {
            @Override
            protected void populateViewHolder(FirebaseProfileCinglesViewHolder viewHolder, Cingle model, int position) {
                viewHolder.bindProfileCingle(model);
                final String postKey = getRef(position).getKey();

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
        if(v == mFollowButton){
            processFollow = true;
            DatabaseReference reference = usernameRef;
            final String refKey = reference.getKey();
            followingRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (processFollow){
                        if (dataSnapshot.child(refKey).hasChild(firebaseAuth.getCurrentUser().getUid())){
                            followingRef.child(refKey)
                                    .removeValue();
                            processFollow = false;
                            onFollow(false);


                        }else {
                            followingRef.child(refKey).child(firebaseAuth.getCurrentUser().getUid())
                                    .setValue(firebaseAuth.getCurrentUser().getUid());
                            processFollow = false;
                            onFollow(false);
                        }

                        followersRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                mFollowersCountTextView.setText(dataSnapshot.getChildrenCount() + "");

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

    private void onFollow(final boolean increament){
        followingRef.runTransaction(new Transaction.Handler() {
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
                Log.d(TAG, "followTransaction:onComplete" + databaseError);

            }
        });
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

}

package com.cinggl.cinggl.profile;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cinggl.cinggl.Constants;
import com.cinggl.cinggl.R;
import com.cinggl.cinggl.adapters.FirebaseProfileCinglesViewHolder;
import com.cinggl.cinggl.models.Cingle;
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
    @Bind(R.id.userProfileImageView)CircleImageView mProifleImageView;
    @Bind(R.id.firstNameTextView)TextView mFirstNameTextView;
    @Bind(R.id.secondNameTextView)TextView  mSecondNameTextView;
    @Bind(R.id.bioTextView)TextView mBioTextView;
    @Bind(R.id.followersCountTextView) TextView mFollowersCountTextView;
    @Bind(R.id.followingCountTextView)TextView mFollowingCountTextView;
    @Bind(R.id.cinglesCountTextView)TextView mCinglesCountTextView;
    @Bind(R.id.followButton)Button mFollowButton;
    @Bind(R.id.followButtonRelativeLayout)RelativeLayout mFollowButtonRelativeLayout;


    private DatabaseReference databaseReference;
    private FirebaseRecyclerAdapter firebaseRecyclerAdapter;
    private Query profileCinglesQuery;
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
        usernameRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_USERS)
                .child(firebaseAuth.getCurrentUser().getUid());
        relationsRef = FirebaseDatabase.getInstance().getReference(Constants.FOLLOWERS);
        fragmentManager = getChildFragmentManager();


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        ButterKnife.bind(this, view);

        setUpFirebaseAdapter();
        fetchData();

        mFollowButton.setOnClickListener(this);
        mFollowingCountTextView.setOnClickListener(this);
        mFollowersCountTextView.setOnClickListener(this);

        databaseReference.keepSynced(true);
        usernameRef.keepSynced(true);
        relationsRef.keepSynced(true);

        usernameRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String uid = (String) dataSnapshot.child("uid").getValue();

                if (firebaseAuth.getCurrentUser().getUid().equals(uid)){
//                    mFollowButton.setVisibility(View.INVISIBLE);
                    mFollowButtonRelativeLayout.setVisibility(View.GONE);
                }else {
//                    mFollowButton.setVisibility(View.VISIBLE);
                    mFollowButtonRelativeLayout.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

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
        if (id == R.id.action_settings) {
//            launchSettings();
            Intent intent = new Intent(getActivity(), UpdateProfileActivity.class);
            startActivity(intent);
            return true;
        }

//        if(id == R.id.action_search){
//            return true;
//        }

//        if(id == R.id.action_notifications){
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }
    /**method to launch settings activity*/
//    public void launchSettings(){
//        Intent intentSettings = new Intent(getActivity(), SettingsActivity.class);
//        startActivity(intentSettings);
//    }



    private void fetchData(){
        DatabaseReference reference = usernameRef;
        final String refKey = reference.getKey();
        Log.d(refKey, "current user reference key");
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

        relationsRef.child(firebaseAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
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

        relationsRef.child(refKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.hasChild(firebaseAuth.getCurrentUser().getUid())){
                    mFollowButton.setText("FOLLOWING");
                }else {
                    mFollowButton.setText("FOLLOW");
                }

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
            relationsRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (processFollow){
                        if (dataSnapshot.child(refKey).hasChild(firebaseAuth.getCurrentUser().getUid())){
                            relationsRef.child(refKey)
                                    .removeValue();
                            processFollow = false;
                            onFollow(false);
                            //set the text on the button to follow if the user in not yet following;
                            mFollowButton.setText("FOLLOW");

                        }else {
                            relationsRef.child(refKey).child(firebaseAuth.getCurrentUser().getUid())
                                    .child("uid").setValue(firebaseAuth.getCurrentUser().getUid());
                            processFollow = false;
                            onFollow(false);

                            //set text on the button following;
                            mFollowButton.setText("FOLLOWING");

                        }

                    }

                    mFollowingCountTextView.setText(dataSnapshot.getChildrenCount() + "");

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        if (v == mFollowingCountTextView) {
            Intent intent = new Intent(getActivity(), PeopleActivity.class);
            startActivity(intent);
        }

        if (v == mFollowersCountTextView){
            Intent intent = new Intent(getActivity(), PeopleActivity.class);
            startActivity(intent);

        }
    }

    private void onFollow(final boolean increament){
        relationsRef.runTransaction(new Transaction.Handler() {
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

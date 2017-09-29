package com.cinggl.cinggl.profile;


import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import android.widget.TextView;
import android.widget.Toast;

import com.cinggl.cinggl.Constants;
import com.cinggl.cinggl.R;
import com.cinggl.cinggl.adapters.ProfileCinglesAdapter;
import com.cinggl.cinggl.ifair.WalletActivity;
import com.cinggl.cinggl.models.Cingle;
import com.cinggl.cinggl.relations.PeopleActivity;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

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
    private Query profileInfoQuery;
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
    private Parcelable recyclerViewState;


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
            profileInfoQuery = databaseReference.orderByChild("uid").equalTo(firebaseAuth.getCurrentUser().getUid());
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
            recyclerViewState = savedInstanceState.getParcelable(KEY_LAYOUT_POSITION);
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

        profileInfoQuery.addValueEventListener(new ValueEventListener() {
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
        outState.putParcelable(KEY_LAYOUT_POSITION, layoutManager.onSaveInstanceState());

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        cleanUpListener();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (recyclerViewState != null){
            layoutManager.onRestoreInstanceState(recyclerViewState);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
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

}

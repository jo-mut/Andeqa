package com.cinggl.cinggl.profile;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.cinggl.cinggl.Constants;
import com.cinggl.cinggl.R;
import com.cinggl.cinggl.adapters.PeopleViewHolder;
import com.cinggl.cinggl.models.Cingulan;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

import butterknife.Bind;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 */
public class FollowersFragment extends Fragment {
    private DatabaseReference cingulansRef;
    private DatabaseReference followersRef;
    private DatabaseReference followingRef;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private DatabaseReference usernameRef;
    private TextView firstNameTextView;
    private TextView secondNameTextView;
    private CircleImageView profileImageView;
    private Button followButton;
    private boolean processFollow = false;
    private FirebaseRecyclerAdapter firebaseRecyclerAdapter;
    private static final String TAG = FollowersFragment.class.getSimpleName();

    @Bind(R.id.followersRecyclerView)RecyclerView mFollowersRecyclerView;

    public FollowersFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        followingRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_USERS);
        usernameRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_USERS);
        cingulansRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_USERS);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_followers, container, false);
        ButterKnife.bind(this, view);
//
//        peopleAdapter = new PeopleAdapter(getContext(), cingulansRef);
//        mFollowersRecyclerView.setAdapter(peopleAdapter);
//        mFollowersRecyclerView.setHasFixedSize(false);
//        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
//        layoutManager.setAutoMeasureEnabled(true);
//        mFollowersRecyclerView.setNestedScrollingEnabled(false);
//        mFollowersRecyclerView.setLayoutManager(layoutManager);
        retrieveFollowers();

        return view;
    }
//
    public void retrieveFollowers(){
        followersRef = FirebaseDatabase.getInstance().getReference(Constants.FOLLOWERS)
                .child(firebaseAuth.getCurrentUser().getUid());
        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Cingulan, PeopleViewHolder>
                (Cingulan.class, R.layout.followers_list, PeopleViewHolder.class, followersRef){
            @Override
            protected void populateViewHolder(final PeopleViewHolder viewHolder, final Cingulan model, int position) {
                DatabaseReference userRef = getRef(position);
                final String postKey = userRef.getKey();
                viewHolder.bindPeople(model);

                followersRef.child(postKey).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String uid = (String) dataSnapshot.child("uid").getValue();

                        try {
                            usernameRef.child(uid).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    final String profileImage = (String) dataSnapshot.child("profileImage").getValue();
                                    final String firstName = (String) dataSnapshot.child("firstName").getValue();
                                    final String secondName = (String) dataSnapshot.child("secondName").getValue();

                                    try {
                                        viewHolder.firstNameTextView.setText(firstName);
                                        viewHolder.secondNameTextView.setText(secondName);

                                        Picasso.with(getContext())
                                                .load(profileImage)
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
                                                                .load(profileImage)
                                                                .fit()
                                                                .centerCrop()
                                                                .placeholder(R.drawable.profle_image_background)
                                                                .into(viewHolder.profileImageView);


                                                    }
                                                });
                                    }catch (Exception e){

                                    }

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }catch (Exception e){

                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

//                viewHolder.followButton.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        processFollow = true;
//                        followingRef.addValueEventListener(new ValueEventListener() {
//                            @Override
//                            public void onDataChange(DataSnapshot dataSnapshot) {
//                                if (processFollow){
//                                    if (dataSnapshot.child(postKey).hasChild(firebaseAuth.getCurrentUser().getUid())){
//                                        followingRef.child(postKey)
//                                                .removeValue();
//                                        processFollow = false;
//                                        onFollow(false);
//                                        //set the text on the button to follow if the user in not yet following;
////                                        followButton.setText("FOLLOW");
//
//                                    }else {
//                                        followingRef.child(postKey).child(firebaseAuth.getCurrentUser().getUid())
//                                                .child("uid").setValue(firebaseAuth.getCurrentUser().getUid());
//                                        processFollow = false;
//                                        onFollow(false);
//
//                                        //set text on the button to unfollow onClicj to follow;
////                                        followButton.setText("UNFOLLOW");
//
//                                    }
//
//                                }
//
//                            }
//
//                            @Override
//                            public void onCancelled(DatabaseError databaseError) {
//
//                            }
//                        });
//                    }
//                });


            }
        };

        mFollowersRecyclerView.setAdapter(firebaseRecyclerAdapter);
        mFollowersRecyclerView.setHasFixedSize(false);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        layoutManager.onSaveInstanceState();
        layoutManager.setAutoMeasureEnabled(true);
        mFollowersRecyclerView.setLayoutManager(layoutManager);

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
    public void onStop() {
        super.onStop();
//        peopleAdapter.cleanUpListener();
        firebaseRecyclerAdapter.cleanup();
    }

    @Override
    public void onStart() {
        super.onStart();
    }
}


package com.cinggl.cinggl.people;


import android.content.Intent;
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
import com.cinggl.cinggl.viewholders.PeopleViewHolder;
import com.cinggl.cinggl.models.Cingulan;
import com.cinggl.cinggl.profile.PersonalProfileActivity;
import com.firebase.ui.common.ChangeEventType;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.firebase.ui.firestore.ObservableSnapshotArray;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
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
    //firestore
    private CollectionReference relationsReference;
    private CollectionReference followingReference;
    private CollectionReference followersReference;
    private CollectionReference usersReference;
    private Query realtionsQuery;
    //adapters
    private FirestoreRecyclerAdapter firestoreRecyclerAdapter;
    //firebase auth
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private TextView firstNameTextView;
    private TextView secondNameTextView;
    private CircleImageView profileImageView;
    private Button followButton;
    private boolean processFollow = false;
    private FirebaseRecyclerAdapter firebaseRecyclerAdapter;
    private static final String TAG = FollowersFragment.class.getSimpleName();
    private static final String EXTRA_USER_UID = "uid";
    private String mUid;

    @Bind(R.id.followersRecyclerView)RecyclerView mFollowersRecyclerView;

    public FollowersFragment() {
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
        View view = inflater.inflate(R.layout.fragment_followers, container, false);
        ButterKnife.bind(this, view);

        firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser()!= null){
            usersReference = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);
            followersReference = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);
            followingReference = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);

            retrieveFollowers();
        }

        return view;
    }

    public void retrieveFollowers(){
        realtionsQuery = relationsReference.document("followers")
                .collection(firebaseAuth.getCurrentUser().getUid());

        FirestoreRecyclerOptions<Cingulan> options = new FirestoreRecyclerOptions.Builder<Cingulan>()
                .setQuery(realtionsQuery, Cingulan.class)
                .build();

        firestoreRecyclerAdapter = new FirestoreRecyclerAdapter<Cingulan, PeopleViewHolder>(options) {
            @Override
            protected void onBindViewHolder(final PeopleViewHolder holder, int position, Cingulan model) {
                final String postKey = getSnapshots().get(position).getPushId();
                holder.bindPeople(model);

                relationsReference.document(postKey).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(final DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen error", e);
                            return;
                        }

                        if (documentSnapshot.exists()){
                            Cingulan cingulan_uid = documentSnapshot.toObject(Cingulan.class);
                            final String uid = cingulan_uid.getUid();

                            usersReference.document(uid).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                @Override
                                public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                                    if (e != null) {
                                        Log.w(TAG, "Listen error", e);
                                        return;
                                    }

                                    Cingulan cingulan =  documentSnapshot.toObject(Cingulan.class);
                                    final String profileImage = cingulan.getProfileImage();
                                    final String firstName = cingulan.getFirstName();
                                    final String secondName = cingulan.getSecondName();
                                    final String username = cingulan.getUsername();


                                    holder.usernameTextView.setText(username);
                                    holder.firstNameTextView.setText(firstName);
                                    holder.secondNameTextView.setText(secondName);
                                    Picasso.with(getContext())
                                            .load(profileImage)
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
                                                    Picasso.with(getContext())
                                                            .load(profileImage)
                                                            .fit()
                                                            .centerCrop()
                                                            .placeholder(R.drawable.profle_image_background)
                                                            .into(holder.profileImageView);


                                                }
                                            });
                                }
                            });

                            holder.profileImageView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    if (uid.equals(firebaseAuth.getCurrentUser().getUid())){
                                        Intent intent = new Intent(getActivity(), PersonalProfileActivity.class);
                                        startActivity(intent);
                                    }else {
                                        Intent intent = new Intent(getActivity(), FollowerProfileActivity.class);
                                        intent.putExtra(FollowersFragment.EXTRA_USER_UID, uid);
                                        startActivity(intent);
                                    }
                                }
                            });

                            holder.followButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    followersReference.addSnapshotListener(new EventListener<QuerySnapshot>() {
                                        @Override
                                        public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                                            if (e != null) {
                                                Log.w(TAG, "Listen error", e);
                                                return;
                                            }


                                        }
                                    });
                                }
                            });


//                            viewHolder.followButton.setOnClickListener(new View.OnClickListener() {
//                                @Override
//                                public void onClick(View view) {
//                                    processFollow = true;
//                                    followingRef.addValueEventListener(new ValueEventListener() {
//                                        @Override
//                                        public void onDataChange(DataSnapshot dataSnapshot) {
//                                            if (processFollow){
//                                                if (dataSnapshot.child("followers").child(postKey).hasChild(firebaseAuth.getCurrentUser().getUid())){
//
//                                                    //remove the uid from the person followed
//                                                    followingRef.child("followers").child(postKey).child(firebaseAuth.getCurrentUser().getUid())
//                                                            .removeValue();
//
//                                                    //remove the person uid is following from the uid
//                                                    followingRef.child("following").child(firebaseAuth.getCurrentUser().getUid()).child(postKey)
//                                                            .removeValue();
//
//                                                    processFollow = false;
//                                                    onFollow(false);
//                                                    //set the text on the button to follow if the user in not yet following;
//
//                                                }else {
//                                                    try {
//                                                        //add uid to the uid of the person followed
//                                                        followingRef.child("followers").child(postKey).child(firebaseAuth.getCurrentUser().getUid())
//                                                                .child("uid").setValue(firebaseAuth.getCurrentUser().getUid());
//
//                                                        //add uid of the person followed to the uid that is folowing
//                                                        followingRef.child("following").child(firebaseAuth.getCurrentUser().getUid()).child(postKey)
//                                                                .child("uid").setValue(postKey);
//
//                                                        processFollow = false;
//                                                        onFollow(false);
//
//                                                        //set text on the button to following;
//                                                        viewHolder.followButton.setText("Following");
//
//                                                    }catch (Exception e){
//
//                                                    }
//
//                                                }
//
//                                            }
//
//                                        }
//
//                                        @Override
//                                        public void onCancelled(DatabaseError databaseError) {
//
//                                        }
//                                    });
//                                }
//                            });

                        }
                    }
                });

                followersReference.document("followers").collection(firebaseAuth.getCurrentUser().getUid())
                        .addSnapshotListener(new EventListener<QuerySnapshot>() {
                            @Override
                            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                                if (e != null) {
                                    Log.w(TAG, "Listen error", e);
                                    return;
                                }

                                holder.followButton.setText("Unfollow");

                            }
                        });
            }

            @Override
            public PeopleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.poeple_list, parent, false);
                return new PeopleViewHolder(view);
            }

            @Override
            public void onError(FirebaseFirestoreException e) {
                super.onError(e);
            }

            @Override
            public void onDataChanged() {
                super.onDataChanged();
            }

            @Override
            public void onChildChanged(ChangeEventType type, DocumentSnapshot snapshot, int newIndex, int oldIndex) {
                super.onChildChanged(type, snapshot, newIndex, oldIndex);
            }

            @Override
            public int getItemCount() {
                return super.getItemCount();
            }

            @Override
            public Cingulan getItem(int position) {
                return super.getItem(position);
            }

            @Override
            public ObservableSnapshotArray<Cingulan> getSnapshots() {
                return super.getSnapshots();
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

//    private void onFollow(final boolean increament){
//        relationsRef.runTransaction(new Transaction.Handler() {
//            @Override
//            public Transaction.Result doTransaction(MutableData mutableData) {
//                if(mutableData.getValue() != null){
//                    int value = mutableData.getValue(Integer.class);
//                    if(increament){
//                        value++;
//                    }else{
//                        value--;
//                    }
//                    mutableData.setValue(value);
//                }
//                return Transaction.success(mutableData);
//            }
//
//            @Override
//            public void onComplete(DatabaseError databaseError, boolean b,
//                                   DataSnapshot dataSnapshot) {
//                Log.d(TAG, "followTransaction:onComplete" + databaseError);
//
//            }
//        });
//    }

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


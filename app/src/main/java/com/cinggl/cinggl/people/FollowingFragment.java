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
import com.cinggl.cinggl.models.Like;
import com.cinggl.cinggl.viewholders.CingleOutViewHolder;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.L;
import static com.cinggl.cinggl.R.id.usernameTextView;

/**
 * A simple {@link Fragment} subclass.
 */
public class FollowingFragment extends Fragment {
    //firestore
    private CollectionReference relationsRef;
    private CollectionReference followingRef;
    private CollectionReference usersReference;
    private com.google.firebase.firestore.Query followingQuery;
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

    @Bind(R.id.followingRecyclerView)RecyclerView mFollowingRecyclerView;

    public FollowingFragment() {
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
        View view = inflater.inflate(R.layout.fragment_following, container, false);
        ButterKnife.bind(this, view);


        firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() != null){
            usersReference = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);
            followingRef = FirebaseFirestore.getInstance().collection(Constants.RELATIONS);
            relationsRef = FirebaseFirestore.getInstance().collection(Constants.RELATIONS);

            retrieveFollowing();
        }

        return view;
    }

    public void retrieveFollowing(){
        followingQuery = followingRef.document("following").collection(firebaseAuth.getUid()
                .toString()).orderBy("uid");

        FirestoreRecyclerOptions<Cingulan> options = new FirestoreRecyclerOptions.Builder<Cingulan>()
                .setQuery(followingQuery, Cingulan.class)
                .build();

        firestoreRecyclerAdapter = new FirestoreRecyclerAdapter<Cingulan, PeopleViewHolder>(options) {
            @Override
            protected void onBindViewHolder(final PeopleViewHolder holder, int position, Cingulan model) {
                final String postKey = getSnapshots().get(position).getPushId();
                holder.bindPeople(model);

                relationsRef.document(postKey).addSnapshotListener(new EventListener<DocumentSnapshot>() {
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
                                        intent.putExtra(FollowingFragment.EXTRA_USER_UID, uid);
                                        startActivity(intent);
                                    }
                                }
                            });

                            holder.followButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    followingRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
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

                followingRef.document("following").collection(firebaseAuth.getCurrentUser().getUid())
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

            @Override
            public void onDataChanged() {
                super.onDataChanged();
            }

            @Override
            public void onError(FirebaseFirestoreException e) {
                super.onError(e);
            }
        };

        mFollowingRecyclerView.setAdapter(firestoreRecyclerAdapter);
        mFollowingRecyclerView.setHasFixedSize(false);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        layoutManager.onSaveInstanceState();
        layoutManager.setAutoMeasureEnabled(true);
        mFollowingRecyclerView.setLayoutManager(layoutManager);
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
        firestoreRecyclerAdapter.stopListening();
    }

    @Override
    public void onStart() {
        super.onStart();
        firestoreRecyclerAdapter.startListening();
    }

}

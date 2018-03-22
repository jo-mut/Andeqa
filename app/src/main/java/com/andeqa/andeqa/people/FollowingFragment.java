package com.andeqa.andeqa.people;


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

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.R;
import com.andeqa.andeqa.models.Andeqan;
import com.andeqa.andeqa.models.Relation;
import com.andeqa.andeqa.models.Timeline;
import com.andeqa.andeqa.profile.PersonalProfileActivity;
import com.firebase.ui.common.ChangeEventType;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.firebase.ui.firestore.ObservableSnapshotArray;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.Date;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class FollowingFragment extends Fragment {
    //firestore
    private CollectionReference relationsReference;
    private CollectionReference usersReference;
    private CollectionReference timelineCollection;
    private com.google.firebase.firestore.Query followingQuery;
    //firebase
    private DatabaseReference databaseReference;
    //adapters
    private FirestoreRecyclerAdapter firestoreRecyclerAdapter;
    //firebase auth
    private FirebaseAuth firebaseAuth;
    private boolean processFollow = false;
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
            relationsReference = FirebaseFirestore.getInstance().collection(Constants.RELATIONS);
            timelineCollection = FirebaseFirestore.getInstance().collection(Constants.TIMELINE);
            //firebase
            databaseReference = FirebaseDatabase.getInstance().getReference(Constants.RANDOM_PUSH_ID);
            retrieveFollowing();
            firestoreRecyclerAdapter.startListening();
        }

        return view;
    }

    private void retrieveFollowing(){
        followingQuery = relationsReference.document("following").collection(firebaseAuth.getCurrentUser().getUid())
                .orderBy("uid");

        FirestoreRecyclerOptions<Relation> options = new FirestoreRecyclerOptions.Builder<Relation>()
                .setQuery(followingQuery, Relation.class)
                .build();

        firestoreRecyclerAdapter = new FirestoreRecyclerAdapter<Relation, PeopleViewHolder>(options) {
            @Override
            protected void onBindViewHolder(final PeopleViewHolder holder, int position, Relation model) {
                //postKey is the uid of the cinggulan user is following
                final String postKey = getSnapshots().get(position).getUid();
                holder.bindPeople(model);
                Log.d("following postKey", postKey);

                usersReference.document(postKey).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen error", e);
                            return;
                        }


                        if (documentSnapshot.exists()){
                            Andeqan cinggulan =  documentSnapshot.toObject(Andeqan.class);
                            final String profileImage = cinggulan.getProfileImage();
                            final String firstName = cinggulan.getFirstName();
                            final String secondName = cinggulan.getSecondName();
                            final String username = cinggulan.getUsername();
                            final String uid = cinggulan.getUid();


                            holder.usernameTextView.setText(username);
                            holder.fullNameTextView.setText(firstName + " " + secondName);
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
                            //lauch user profile
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

                            //show if following or not
                            relationsReference.document("following").collection(postKey)
                                    .whereEqualTo("uid", firebaseAuth.getCurrentUser().getUid())
                                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                        @Override
                                        public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                                            if (e != null) {
                                                Log.w(TAG, "Listen error", e);
                                                return;
                                            }

                                            if (documentSnapshots.isEmpty()){
                                                holder.followButton.setText("Unfollow");
                                            }else {
                                                holder.followButton.setText("Following");
                                            }
                                        }
                                    });


                            //follow or unfollow
                            if (uid.equals(firebaseAuth.getCurrentUser().getUid())){
                                holder.followButton.setVisibility(View.GONE);
                            }else {
                                holder.followButton.setVisibility(View.VISIBLE);
                                holder.followButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        processFollow = true;
                                        relationsReference.document("followers")
                                                .collection(postKey)
                                                .whereEqualTo("uid", firebaseAuth.getCurrentUser().getUid())
                                                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                                    @Override
                                                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {


                                                        if (e != null) {
                                                            Log.w(TAG, "Listen error", e);
                                                            return;
                                                        }

                                                        if (processFollow){
                                                            if (documentSnapshots.isEmpty()){
                                                                //set followers and following
                                                                Relation follower = new Relation();
                                                                follower.setUid(firebaseAuth.getCurrentUser().getUid());
                                                                relationsReference.document("followers").collection(postKey)
                                                                        .document(firebaseAuth.getCurrentUser().getUid()).set(follower)
                                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                            @Override
                                                                            public void onSuccess(Void aVoid) {
                                                                                Timeline timeline = new Timeline();
                                                                                final long time = new Date().getTime();
                                                                                timelineCollection.document(postKey).collection("timeline").document(postKey)
                                                                                        .set(timeline);
                                                                                final String postid =  databaseReference.push().getKey();
                                                                                timeline.setPushId(postKey);
                                                                                timeline.setTime(time);
                                                                                timeline.setUid(firebaseAuth.getCurrentUser().getUid());
                                                                                timeline.setType("followers");
                                                                                timeline.setPostId(postid);
                                                                                timeline.setStatus("unRead");

                                                                                timelineCollection.document(postKey).collection("timeline")
                                                                                        .document(firebaseAuth.getCurrentUser().getUid())
                                                                                        .set(timeline);
                                                                            }
                                                                        });
                                                                final Relation following = new Relation();
                                                                following.setUid(postKey);
                                                                relationsReference.document("following").collection(firebaseAuth.getCurrentUser().getUid())
                                                                        .document(postKey).set(following);
                                                                processFollow = false;
                                                                holder.followButton.setText("Following");
                                                            }else {
                                                                relationsReference.document("followers").collection(postKey)
                                                                        .document(firebaseAuth.getCurrentUser().getUid()).delete();
                                                                relationsReference.document("following").collection(firebaseAuth.getCurrentUser().getUid())
                                                                        .document(postKey).delete();
                                                                processFollow = false;
                                                                holder.followButton.setText("Unfollow");
                                                            }
                                                        }
                                                    }
                                                });

                                    }
                                });
                            }
                        }


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
            public Relation getItem(int position) {
                return super.getItem(position);
            }

            @Override
            public ObservableSnapshotArray<Relation> getSnapshots() {
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

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        firestoreRecyclerAdapter.stopListening();
    }

}

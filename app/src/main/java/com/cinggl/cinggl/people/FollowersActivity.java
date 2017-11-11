package com.cinggl.cinggl.people;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.cinggl.cinggl.Constants;
import com.cinggl.cinggl.R;
import com.cinggl.cinggl.models.Relation;
import com.cinggl.cinggl.viewholders.PeopleViewHolder;
import com.cinggl.cinggl.models.Cingulan;
import com.cinggl.cinggl.profile.PersonalProfileActivity;
import com.firebase.ui.common.ChangeEventType;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.firebase.ui.firestore.ObservableSnapshotArray;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.google.firebase.firestore.DocumentReference;
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

import static com.cinggl.cinggl.R.id.followButton;

public class FollowersActivity extends AppCompatActivity {
    //firestore references
    private CollectionReference relationsReference;
    private CollectionReference usersReference;
    private Query followersQuery;
    //adapters
    private FirestoreRecyclerAdapter firestoreRecyclerAdapter;
    private FirebaseAuth firebaseAuth;
    private boolean processFollow = false;
    private static final String TAG = FollowersFragment.class.getSimpleName();
    private static final String EXTRA_USER_UID = "uid";
    private String mUid;

    @Bind(R.id.followersRecyclerView)RecyclerView mFollowersRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_followers);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //ON NAVIGATING BACK
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() != null){
            mUid = getIntent().getStringExtra(EXTRA_USER_UID);
            if(mUid == null){
                throw new IllegalArgumentException("pass an EXTRA_USER_UID");
            }

            usersReference= FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);
            relationsReference = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);
            followersQuery = relationsReference.document("followers").collection(mUid);

            retrieveFollowers();
            firestoreRecyclerAdapter.startListening();
        }
    }



    private void retrieveFollowers(){
        followersQuery.orderBy("uid");
        FirestoreRecyclerOptions<Relation> options = new FirestoreRecyclerOptions.Builder<Relation>()
                .setQuery(followersQuery, Relation.class)
                .build();

        firestoreRecyclerAdapter = new FirestoreRecyclerAdapter<Relation, PeopleViewHolder>(options) {
            @Override
            protected void onBindViewHolder(final PeopleViewHolder holder, int position, Relation model) {
                holder.bindPeople(model);
                final String postKey = getSnapshots().get(position).getUid();
                Log.d("follower postKey", postKey);

                //postkey is same as uid
                usersReference.document(postKey).addSnapshotListener(new EventListener<DocumentSnapshot>() {
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
                        final String uid = cingulan.getUid();


                        holder.usernameTextView.setText(username);
                        holder.fullNameTextView.setText(firstName + " " + secondName);
                        Picasso.with(FollowersActivity.this)
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
                                        Picasso.with(FollowersActivity.this)
                                                .load(profileImage)
                                                .fit()
                                                .centerCrop()
                                                .placeholder(R.drawable.profle_image_background)
                                                .into(holder.profileImageView);


                                    }
                                });

                        holder.profileImageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (uid.equals(firebaseAuth.getCurrentUser().getUid())){
                                    Intent intent = new Intent(FollowersActivity.this, PersonalProfileActivity.class);
                                    startActivity(intent);
                                }else {
                                    Intent intent = new Intent(FollowersActivity.this, FollowerProfileActivity.class);
                                    intent.putExtra(FollowersActivity.EXTRA_USER_UID, uid);
                                    startActivity(intent);
                                }
                            }
                        });

                        //show if following or not
                        relationsReference.document("followers").collection(mUid)
                                .whereEqualTo("uid", firebaseAuth.getCurrentUser().getUid())
                                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                    @Override
                                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                                        if (documentSnapshots.isEmpty()){
                                            holder.followButton.setText("Follow");
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
                                            .collection(postKey).whereEqualTo("uid", firebaseAuth.getCurrentUser().getUid())
                                            .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                                @Override
                                                public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                                                    if (processFollow){
                                                        if (documentSnapshots.isEmpty()){
                                                            Relation follower = new Relation();
                                                            follower.setUid(firebaseAuth.getCurrentUser().getUid());
                                                            relationsReference.document("followers").collection(postKey)
                                                                    .document(firebaseAuth.getCurrentUser().getUid()).set(follower)
                                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                        @Override
                                                                        public void onSuccess(Void aVoid) {
                                                                            Relation following = new Relation();
                                                                            following.setUid(postKey);
                                                                            relationsReference.document("following").collection(firebaseAuth
                                                                                    .getCurrentUser().getUid()).document(postKey).set(following);
                                                                        }
                                                                    });
                                                            processFollow = false;
                                                            holder.followButton.setText("Following");
                                                        }else {
                                                            relationsReference.document("followers").collection(postKey)
                                                                    .document(firebaseAuth.getCurrentUser().getUid()).delete()
                                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                        @Override
                                                                        public void onSuccess(Void aVoid) {
                                                                            relationsReference.document("following").collection(firebaseAuth.getCurrentUser()
                                                                                    .getUid()).document(postKey).delete();
                                                                        }
                                                                    });
                                                            processFollow = false;
                                                            holder.followButton.setText("Follow");
                                                        }
                                                    }
                                                }
                                            });

                                }
                            });

                        }

                    }
                });



            }

            @Override
            public PeopleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.poeple_list, parent, false);
                return new PeopleViewHolder(view);
            }

            @Override
            public void onDataChanged() {
                super.onDataChanged();
            }

            @Override
            public void onError(FirebaseFirestoreException e) {
                super.onError(e);
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
        };

        mFollowersRecyclerView.setAdapter(firestoreRecyclerAdapter);
        mFollowersRecyclerView.setHasFixedSize(false);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        layoutManager.onSaveInstanceState();
        layoutManager.setAutoMeasureEnabled(true);
        mFollowersRecyclerView.setLayoutManager(layoutManager);

    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        firestoreRecyclerAdapter.stopListening();
    }
}

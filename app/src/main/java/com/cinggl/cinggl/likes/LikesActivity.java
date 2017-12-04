package com.cinggl.cinggl.likes;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.cinggl.cinggl.Constants;
import com.cinggl.cinggl.R;
import com.cinggl.cinggl.models.Cinggulan;
import com.cinggl.cinggl.models.Relation;
import com.cinggl.cinggl.viewholders.LikesViewHolder;
import com.cinggl.cinggl.models.Like;
import com.cinggl.cinggl.people.FollowerProfileActivity;
import com.cinggl.cinggl.profile.PersonalProfileActivity;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
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

public class LikesActivity extends AppCompatActivity {
    @Bind(R.id.recentLikesRecyclerView)RecyclerView mRecentLikesRecyclerView;
    @Bind(R.id.emptyLikesRelativeLayout)RelativeLayout mEmptyRelativelayout;

    //firestore
    private CollectionReference relationsReference;
    private CollectionReference usersReference;

    private DatabaseReference likesRef;
    private DatabaseReference usernameRef;
    private DatabaseReference relationsRef;
    private FirebaseAuth firebaseAuth;
    private CircleImageView profileImageView;
    private String mPostKey;
    private FirebaseRecyclerAdapter firebaseRecyclerAdapter;
    private Button followButton;
    private boolean processFollow = false;
    private static final String TAG = LikesActivity.class.getSimpleName();
    private static final String EXTRA_USER_UID = "uid";
    private static final String EXTRA_POST_KEY = "post key";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_likes);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() != null){
            mPostKey = getIntent().getStringExtra(EXTRA_POST_KEY);
            if(mPostKey == null){
                throw new IllegalArgumentException("pass an EXTRA_POST_KEY");
            }
            likesRef = FirebaseDatabase.getInstance().getReference(Constants.LIKES).child(mPostKey);
            usernameRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_USERS);
            relationsRef = FirebaseDatabase.getInstance().getReference(Constants.RELATIONS);
            //firestore
            relationsReference = FirebaseFirestore.getInstance().collection(Constants.RELATIONS);
            usersReference = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);


            likesRef.keepSynced(true);
            setUpFirebaseLikes();
        }

    }

    @Override
    protected void onStart() {
        super.onStart();

    }


    @Override
    public void onStop(){
        super.onStop();
        firebaseRecyclerAdapter.cleanup();
    }


    public void setUpFirebaseLikes(){
        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Like, LikesViewHolder>
                (Like.class, R.layout.likes_list_layout ,LikesViewHolder.class, likesRef) {
            @Override
            protected void populateViewHolder(final LikesViewHolder viewHolder,final Like model, int position) {
                DatabaseReference userRef = getRef(position);
                final String postKey = userRef.getKey();
                Log.d("like post key", postKey);

                if (likesRef.child(postKey) != null){
                    mEmptyRelativelayout.setVisibility(View.GONE);
                    viewHolder.bindLikes(model);
                }else {
                    mEmptyRelativelayout.setVisibility(View.VISIBLE);
                }

                likesRef.child(postKey).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(final DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            final Like like = dataSnapshot.getValue(Like.class);
                            final String uid = like.getUid();
                            Log.d("user uid", uid);

                            //get the profile of the user wh just commented
                            usersReference.document(uid).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                @Override
                                public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                                    if (e != null) {
                                        Log.w(TAG, "Listen error", e);
                                        return;
                                    }

                                    if (documentSnapshot.exists()){
                                        final Cinggulan cinggulan = documentSnapshot.toObject(Cinggulan.class);
                                        final String profileImage = cinggulan.getProfileImage();
                                        final String username = cinggulan.getUsername();
                                        final String firstName = cinggulan.getFirstName();
                                        final String secondName = cinggulan.getSecondName();


                                        viewHolder.usernameTextView.setText(username);
                                        viewHolder.fullNameTextView.setText(firstName + " " + secondName);

                                        Picasso.with(LikesActivity.this)
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
                                                        Picasso.with(LikesActivity.this)
                                                                .load(profileImage)
                                                                .fit()
                                                                .centerCrop()
                                                                .placeholder(R.drawable.profle_image_background)
                                                                .into(viewHolder.profileImageView);


                                                    }
                                                });

                                    }
                                }
                            });

                            viewHolder.profileImageView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    if (uid.equals(firebaseAuth.getCurrentUser().getUid())){
                                        Intent intent = new Intent(LikesActivity.this, PersonalProfileActivity.class);
                                        startActivity(intent);
                                    }else {
                                        Intent intent = new Intent(LikesActivity.this, FollowerProfileActivity.class);
                                        intent.putExtra(LikesActivity.EXTRA_USER_UID, uid);
                                        startActivity(intent);
                                    }
                                }
                            });

                            relationsReference.document("following").collection(firebaseAuth.getCurrentUser().getUid())
                                    .whereEqualTo("uid", uid).addSnapshotListener(new EventListener<QuerySnapshot>() {
                                @Override
                                public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                                    if (e != null) {
                                        Log.w(TAG, "Listen error", e);
                                        return;
                                    }

                                    if (documentSnapshots.isEmpty()){
                                        viewHolder.followButton.setText("Follow");
                                    }else {
                                        viewHolder.followButton.setText("Following");
                                    }
                                }
                            });

                            if (uid.equals(firebaseAuth.getCurrentUser().getUid())){
                                viewHolder.followButton.setVisibility(View.GONE);
                            }else {
                                viewHolder.followButton.setVisibility(View.VISIBLE);
                                viewHolder.followButton.setOnClickListener(new View.OnClickListener() {
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
                                                                viewHolder.followButton.setText("Following");
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
                                                                viewHolder.followButton.setText("Follow");
                                                            }
                                                        }
                                                    }
                                                });

                                    }
                                });
                            }

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        };

        mRecentLikesRecyclerView.setAdapter(firebaseRecyclerAdapter);
        mRecentLikesRecyclerView.setHasFixedSize(false);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setAutoMeasureEnabled(true);
        mRecentLikesRecyclerView.setLayoutManager(layoutManager);
    }

}

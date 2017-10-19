package com.cinggl.cinggl.home;

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
import android.widget.RelativeLayout;

import com.cinggl.cinggl.Constants;
import com.cinggl.cinggl.R;
import com.cinggl.cinggl.models.Cingulan;
import com.cinggl.cinggl.viewholders.LikesViewHolder;
import com.cinggl.cinggl.models.Like;
import com.cinggl.cinggl.people.FollowerProfileActivity;
import com.cinggl.cinggl.profile.PersonalProfileActivity;
import com.firebase.ui.common.ChangeEventType;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.firebase.ui.firestore.ObservableSnapshotArray;
import com.google.firebase.auth.FirebaseAuth;
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

import static com.cinggl.cinggl.R.id.firstNameTextView;
import static com.cinggl.cinggl.R.id.secondNameTextView;
import static com.cinggl.cinggl.R.id.usernameTextView;

public class LikesActivity extends AppCompatActivity {
    @Bind(R.id.recentLikesRecyclerView)RecyclerView mRecentLikesRecyclerView;
    @Bind(R.id.emptyLikesRelativeLayout)RelativeLayout mEmptyRelativelayout;
    //firestore
    private CollectionReference relationsReference;
    private CollectionReference usersReference;
    private CollectionReference likesReference;
    private Query likesQuery;
    //adapters
    private FirestoreRecyclerOptions firestoreRecyclerOptions;
    private FirestoreRecyclerAdapter firestoreRecyclerAdapter;
    //firebase auth
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
            usersReference = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);
            relationsReference = FirebaseFirestore.getInstance().collection(Constants.RELATIONS);
            likesReference = FirebaseFirestore.getInstance().collection(Constants.LIKES);

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
        likesQuery = likesReference.document("Cingle Likes").collection("Likes").orderBy("uid");
        FirestoreRecyclerOptions<Like> options = new FirestoreRecyclerOptions.Builder<Like>()
                .setQuery(likesQuery, Like.class)
                .build();

        firestoreRecyclerAdapter = new FirestoreRecyclerAdapter<Like, LikesViewHolder>(options) {
            @Override
            protected void onBindViewHolder(final LikesViewHolder holder, int position, Like model) {
                holder.bindLikes(model);
                final String likeKey = getSnapshots().get(position).getPushId();
                final String uid = getSnapshots().get(position).getUid();

                if (likesReference.document(likeKey)!= null){
                    mEmptyRelativelayout.setVisibility(View.GONE);
                    holder.bindLikes(model);
                }else {
                    mEmptyRelativelayout.setVisibility(View.VISIBLE);
                }

                holder.profileImageView.setOnClickListener(new View.OnClickListener() {
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

                holder.profileImageView.setOnClickListener(new View.OnClickListener() {
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


                usersReference.document(uid).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                        if (e != null) {
                            Log.w(TAG, "Listen error", e);
                            return;
                        }

                        Cingulan cingulan = documentSnapshot.toObject(Cingulan.class);
                        final String profileImage = cingulan.getProfileImage();
                        final String firstName = cingulan.getFirstName();
                        final String secondName = cingulan.getSecondName();
                        final String username = cingulan.getUsername();

                        holder.usernameTextView.setText(username);
                        holder.firstNameTextView.setText(firstName);
                        holder.secondNameTextView.setText(secondName);

                        Picasso.with(LikesActivity.this)
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
                                        Picasso.with(LikesActivity.this)
                                                .load(profileImage)
                                                .fit()
                                                .centerCrop()
                                                .placeholder(R.drawable.profle_image_background)
                                                .into(holder.profileImageView);


                                    }
                                });
                    }
                });

                relationsReference.document("following").collection(firebaseAuth.getCurrentUser().getUid())
                        .addSnapshotListener(new EventListener<QuerySnapshot>() {
                            @Override
                            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                                if (e != null) {
                                    Log.w(TAG, "Listen error", e);
                                    return;
                                }

                                if (documentSnapshots.getDocuments().contains(uid)){
                                    holder.followButton.setText("Following");
                                }else {
                                    holder.followButton.setText("Following");
                                }
                            }
                        });



            }

            @Override
            public LikesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.likes_list_layout, parent, false);
                return new LikesViewHolder(view);
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
            public Like getItem(int position) {
                return super.getItem(position);
            }

            @Override
            public ObservableSnapshotArray<Like> getSnapshots() {
                return super.getSnapshots();
            }
        };

        mRecentLikesRecyclerView.setAdapter(firestoreRecyclerAdapter);
        mRecentLikesRecyclerView.setHasFixedSize(false);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setAutoMeasureEnabled(true);
        mRecentLikesRecyclerView.setLayoutManager(layoutManager);
    }
//
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

}

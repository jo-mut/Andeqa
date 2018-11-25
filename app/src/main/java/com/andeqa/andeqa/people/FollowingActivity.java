package com.andeqa.andeqa.people;

import android.arch.paging.PagedList;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.R;
import com.andeqa.andeqa.chatting.ChatActivity;
import com.andeqa.andeqa.models.Andeqan;
import com.andeqa.andeqa.models.Relation;
import com.andeqa.andeqa.models.Room;
import com.andeqa.andeqa.models.Timeline;
import com.andeqa.andeqa.profile.ProfileActivity;
import com.andeqa.andeqa.utils.ItemOffsetDecoration;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.firebase.ui.firestore.paging.LoadingState;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Date;

import javax.annotation.Nullable;

import butterknife.Bind;
import butterknife.ButterKnife;

public class FollowingActivity extends AppCompatActivity {
    @Bind(R.id.followingRecyclerView)RecyclerView mFollowingRecyclerView;
    @Bind(R.id.toolbar)Toolbar toolbar;
    @Bind(R.id.progressBar) ProgressBar mProgressBar;
    @Bind(R.id.progressRelativeLayout)RelativeLayout mProgressRelativeLayout;
    //firestore
    private CollectionReference usersCollection;
    private CollectionReference followersCollection;
    private CollectionReference timelineCollection;
    private CollectionReference roomsCollection;
    private Query usersQuery;
    private Query followeringQuery;
    private DatabaseReference databaseReference;
    //firebase auth
    private FirebaseAuth firebaseAuth;
    //boolean
    private boolean processFollow = false;
    private boolean processRoom = false;
    //strings
    private static final String TAG = FollowersActivity.class.getSimpleName();
    private static final String EXTRA_USER_UID = "uid";
    private static final String EXTRA_ROOM_ID = "roomId";
    private String roomId;
    private String mUid;
    //layouts
    private LinearLayoutManager layoutManager;
    private ItemOffsetDecoration itemOffsetDecoration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_following);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        // set up action bar
        toolbar.setNavigationIcon(R.drawable.ic_arrow);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        //get intents
        getIntents();
        // initialize firebase references
        initReferences();
        //set up the adapter
        setUpAdapter();
    }


    @Override
    public void onStart() {
        super.onStart();
        mFollowingRecyclerView.addItemDecoration(itemOffsetDecoration);

    }

    @Override
    public void onStop() {
        super.onStop();
        mFollowingRecyclerView.removeItemDecoration(itemOffsetDecoration);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void getIntents(){
        mUid = getIntent().getStringExtra(EXTRA_USER_UID);
    }

    private void initReferences(){
        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance();
        // init firestore references
        usersCollection = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);
        usersQuery = usersCollection;
        followersCollection = FirebaseFirestore.getInstance().collection(Constants.PEOPLE_RELATIONS);
        followeringQuery = followersCollection.document("following")
                .collection(mUid);
        timelineCollection = FirebaseFirestore.getInstance().collection(Constants.TIMELINE);
        timelineCollection = FirebaseFirestore.getInstance().collection(Constants.TIMELINE);
        databaseReference = FirebaseDatabase.getInstance().getReference(Constants.RANDOM_PUSH_ID);
        followersCollection = FirebaseFirestore.getInstance().collection(Constants.PEOPLE_RELATIONS);
        roomsCollection = FirebaseFirestore.getInstance().collection(Constants.MESSAGES);
    }

    private void setUpAdapter() {
        Query query = followeringQuery.orderBy("time", Query.Direction.DESCENDING);
        PagedList.Config config = new PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPrefetchDistance(10)
                .setPageSize(20)
                .build();

        FirestorePagingOptions<Relation> options = new FirestorePagingOptions.Builder<Relation>()
                .setLifecycleOwner(this)
                .setQuery(query, config, Relation.class)
                .build();

        FirestorePagingAdapter<Relation, PeopleRelationsViewHolder> pagingAdapter
                = new FirestorePagingAdapter<Relation, PeopleRelationsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final PeopleRelationsViewHolder holder, int position, @NonNull Relation model) {
                Relation relation = model;
                final String userId = relation.getFollowing_id();

                usersCollection.document(userId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen error", e);
                            return;
                        }


                        if (documentSnapshot.exists()){
                            Andeqan andeqan =  documentSnapshot.toObject(Andeqan.class);
                            final String profileImage = andeqan.getProfile_image();
                            final String firstName = andeqan.getFirst_name();
                            final String secondName = andeqan.getSecond_name();
                            final String username = andeqan.getUsername();
                            final String uid = andeqan.getUser_id();

                            holder.mUsernameTextView.setText(username);
                            holder.mFullNameTextView.setText(firstName + " " + secondName);
                            Glide.with(getApplicationContext())
                                    .load(profileImage)
                                    .apply(new RequestOptions()
                                            .placeholder(R.drawable.ic_user)
                                            .diskCacheStrategy(DiskCacheStrategy.DATA))
                                    .into(holder.mProfileImageView);

                            //lauch user profile
                            holder.mProfileImageView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent intent = new Intent(FollowingActivity.this, ProfileActivity.class);
                                    intent.putExtra(FollowingActivity.EXTRA_USER_UID, userId);
                                    startActivity(intent);
                                }
                            });


                            //show if following or not
                            followersCollection.document("followers").collection(userId)
                                    .document(firebaseAuth.getCurrentUser().getUid())
                                    .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                        @Override
                                        public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                                            if (e != null) {
                                                Log.w(TAG, "Listen error", e);
                                                return;
                                            }

                                            if (documentSnapshot.exists()){
                                                if (!uid.equals(firebaseAuth.getCurrentUser().getUid())){
                                                    holder.mFollowButton.setVisibility(View.VISIBLE);
                                                    holder.mFollowButton.setText("Follow");
                                                }
                                            }else {
                                                if (!uid.equals(firebaseAuth.getCurrentUser().getUid())){
                                                    holder.mFollowButton.setVisibility(View.VISIBLE);
                                                    holder.mFollowButton.setText("Following");
                                                }
                                            }
                                        }
                                    });


                            if (uid.equals(firebaseAuth.getCurrentUser().getUid())){
                                holder.mSendMessageImageView.setVisibility(View.GONE);
                            }else {
                                holder.mSendMessageImageView.setVisibility(View.VISIBLE);
                                holder.mSendMessageImageView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        processRoom = true;
                                        roomsCollection.document(userId).collection("last message")
                                                .document(firebaseAuth.getCurrentUser().getUid())
                                                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                                    @Override
                                                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                                                        if (e != null) {
                                                            Log.w(TAG, "Listen error", e);
                                                            return;
                                                        }

                                                        if (processRoom){
                                                            if (documentSnapshot.exists()){
                                                                Room room = documentSnapshot.toObject(Room.class);
                                                                roomId = room.getRoom_id();
                                                                Intent intent = new Intent(FollowingActivity.this, ChatActivity.class);
                                                                intent.putExtra(FollowingActivity.EXTRA_ROOM_ID, roomId);
                                                                intent.putExtra(FollowingActivity.EXTRA_USER_UID, userId);
                                                                startActivity(intent);

                                                                processRoom = false;
                                                            }else {
                                                                roomsCollection.document(firebaseAuth.getCurrentUser().getUid())
                                                                        .collection("last message")
                                                                        .document(userId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                                                    @Override
                                                                    public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                                                                        if (e != null) {
                                                                            Log.w(TAG, "Listen error", e);
                                                                            return;
                                                                        }

                                                                        if (processRoom){
                                                                            if (documentSnapshot.exists()){
                                                                                Room room = documentSnapshot.toObject(Room.class);
                                                                                roomId = room.getRoom_id();
                                                                                Intent intent = new Intent(FollowingActivity.this, ChatActivity.class);
                                                                                intent.putExtra(FollowingActivity.EXTRA_ROOM_ID, roomId);
                                                                                intent.putExtra(FollowingActivity.EXTRA_USER_UID, userId);
                                                                                startActivity(intent);

                                                                                processRoom = false;

                                                                            }else {
                                                                                //start a chat with mUid since they have no chatting history
                                                                                roomId = databaseReference.push().getKey();
                                                                                Intent intent = new Intent(FollowingActivity.this, ChatActivity.class);
                                                                                intent.putExtra(FollowingActivity.EXTRA_ROOM_ID, roomId);
                                                                                intent.putExtra(FollowingActivity.EXTRA_USER_UID, userId);
                                                                                startActivity(intent);

                                                                                processRoom = false;
                                                                            }
                                                                        }
                                                                    }
                                                                });
                                                            }
                                                        }

                                                    }
                                                });

                                    }
                                });

                            }

                            //follow or unfollow
                            if (uid.equals(firebaseAuth.getCurrentUser().getUid())){
                                holder.mFollowButton.setVisibility(View.GONE);
                            }else {
                                holder.mFollowButton.setVisibility(View.VISIBLE);
                                holder.mFollowButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        processFollow = true;
                                        followersCollection.document("followers")
                                                .collection(userId).whereEqualTo("following_id",
                                                firebaseAuth.getCurrentUser().getUid())
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
                                                                follower.setFollowing_id(firebaseAuth.getCurrentUser().getUid());
                                                                follower.setFollowed_id(userId);
                                                                follower.setType("followed_user");
                                                                follower.setTime(System.currentTimeMillis());
                                                                followersCollection.document("followers").collection(userId)
                                                                        .document(firebaseAuth.getCurrentUser().getUid()).set(follower)
                                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                            @Override
                                                                            public void onSuccess(Void aVoid) {
                                                                                Timeline timeline = new Timeline();
                                                                                final long time = new Date().getTime();
                                                                                final String postid =  databaseReference.push().getKey();
                                                                                timeline.setPost_id(userId);
                                                                                timeline.setTime(time);
                                                                                timeline.setUser_id(firebaseAuth.getCurrentUser().getUid());
                                                                                timeline.setType("followers");
                                                                                timeline.setActivity_id(postid);
                                                                                timeline.setStatus("un_read");

                                                                                timelineCollection.document(userId).collection("activities")
                                                                                        .document(firebaseAuth.getCurrentUser().getUid())
                                                                                        .set(timeline);
                                                                            }
                                                                        });
                                                                final Relation following = new Relation();
                                                                follower.setFollowing_id(firebaseAuth.getCurrentUser().getUid());
                                                                following.setFollowed_id(userId);
                                                                following.setType("following_user");;
                                                                following.setTime(System.currentTimeMillis());
                                                                followersCollection.document("following").collection(firebaseAuth.getCurrentUser().getUid())
                                                                        .document(userId).set(following);
                                                                holder.mFollowButton.setText("Following");

                                                                processFollow = false;
                                                            }else {
                                                                followersCollection.document("followers").collection(userId)
                                                                        .document(firebaseAuth.getCurrentUser().getUid()).delete();
                                                                followersCollection.document("following").collection(firebaseAuth.getCurrentUser().getUid())
                                                                        .document(userId).delete();
                                                                holder.mFollowButton.setText("Follow");
                                                                processFollow = false;
                                                            }
                                                        }
                                                    }
                                                });

                                    }
                                });
                            }
                        }else {
                            followersCollection.document("followers").collection(userId)
                                    .document(firebaseAuth.getCurrentUser().getUid()).delete();
                        }


                    }
                });

            }

            @NonNull
            @Override
            public PeopleRelationsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.layout_explore_posts, parent, false);
                return new PeopleRelationsViewHolder(view);
            }


            @Override
            public int getItemViewType(int position) {
                return super.getItemViewType(position);
            }

            @Override
            public void setHasStableIds(boolean hasStableIds) {
                super.setHasStableIds(hasStableIds);
            }

            @Override
            public long getItemId(int position) {
                return super.getItemId(position);
            }

            @Override
            protected void onLoadingStateChanged(@NonNull LoadingState state) {
                switch (state) {
                    case LOADING_INITIAL:
                    case LOADING_MORE:
                        mProgressBar.setVisibility(View.VISIBLE);
                        mProgressRelativeLayout.setVisibility(View.VISIBLE);
                        break;
                    case LOADED:
                        mProgressBar.setVisibility(View.GONE);
                        break;
                    case FINISHED:
                        mProgressBar.setVisibility(View.GONE);
                        showToast("Reached end of data set.");
                        break;
                    case ERROR:
                        showToast("An error occurred.");
                        retry();
                        break;
                }
            }
        };

        pagingAdapter.setHasStableIds(true);
        mFollowingRecyclerView.setHasFixedSize(false);
        mFollowingRecyclerView.setAdapter(pagingAdapter);
        layoutManager = new LinearLayoutManager(this);
        itemOffsetDecoration = new ItemOffsetDecoration(this, R.dimen.item_off_set);
        mFollowingRecyclerView.setLayoutManager(layoutManager);

    }

    private void showToast(@NonNull String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}

package com.andeqa.andeqa.profile;

import android.content.Intent;
import android.os.Parcelable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.R;
import com.andeqa.andeqa.message.MessagesAccountActivity;
import com.andeqa.andeqa.models.Andeqan;
import com.andeqa.andeqa.models.Relation;
import com.andeqa.andeqa.models.Room;
import com.andeqa.andeqa.models.Single;
import com.andeqa.andeqa.models.Timeline;
import com.andeqa.andeqa.people.FollowersActivity;
import com.andeqa.andeqa.people.FollowingActivity;
import com.andeqa.andeqa.utils.EndlessRecyclerOnScrollListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener{
    //BIND VIEWS
    @Bind(R.id.collectionsRecyclerView)RecyclerView mCollectionsRecyclerView;
    @Bind(R.id.profileImageView)CircleImageView mProifleImageView;
    @Bind(R.id.fullNameTextView)TextView mFullNameTextView;
    @Bind(R.id.bioTextView)TextView mBioTextView;
    @Bind(R.id.followersCountTextView) TextView mFollowersCountTextView;
    @Bind(R.id.followingCountTextView)TextView mFollowingCountTextView;
    @Bind(R.id.postsCountTextView)TextView mPostsCountTextView;
    @Bind(R.id.profileCoverImageView)ImageView mProfileCover;
    @Bind(R.id.collapsing_toolbar)CollapsingToolbarLayout collapsingToolbarLayout;
    @Bind(R.id.sendMessageImageView)ImageView mSendMessageImageView;
    @Bind(R.id.followButton)Button mFollowButton;

    private static final String TAG = ProfileActivity.class.getSimpleName();
    //firestore reference
    private CollectionReference collectionsCollection;
    private CollectionReference relationsCollections;
    private CollectionReference usersCollections;
    private CollectionReference postsCollection;
    private CollectionReference timelineCollection;

    private Query postCountQuery;
    private Query profileCollectionsQuery;
    private Query nextCollectionsQuery;
    private CollectionReference roomsCollection;
    //firebase
    private DatabaseReference databaseReference;


    //firebase auth
    private FirebaseAuth firebaseAuth;
    //firestore adapters
    private ProfileCollectionsAdapter profileCollectionsAdapter;
    private static final String KEY_LAYOUT_POSITION = "layout pooition";
    private Parcelable recyclerViewState;
    private  static final int MAX_WIDTH = 200;
    private static final int MAX_HEIGHT = 200;
    //singles meber variables
    private List<Single> singles = new ArrayList<>();
    private List<String> cinglesIds = new ArrayList<>();
    private DocumentSnapshot lastVisible;
    private LinearLayoutManager layoutManager;
    private static final String EXTRA_USER_UID = "uid";
    private static final int TOTAL_ITEMS = 20;

    private static final String EXTRA_ROOM_UID = "roomId";
    private String mUid;
    private List<String> collectionsIds = new ArrayList<>();
    private List<DocumentSnapshot> documentSnapshots = new ArrayList<>();

    private boolean processFollow = false;
    private String roomId;
    private boolean processRoom = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        ButterKnife.bind(this);

        //FIREBASE AUTH
        firebaseAuth = FirebaseAuth.getInstance();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


        if (firebaseAuth.getCurrentUser()!= null){

            mUid = getIntent().getStringExtra(EXTRA_USER_UID);
            if(mUid == null){
                throw new IllegalArgumentException("pass an EXTRA_UID");
            }

            usersCollections = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);
            relationsCollections = FirebaseFirestore.getInstance().collection(Constants.RELATIONS);
            collectionsCollection = FirebaseFirestore.getInstance().collection(Constants.COLLECTIONS);
            profileCollectionsQuery = collectionsCollection.orderBy("time", Query.Direction.DESCENDING)
                    .whereEqualTo("uid", mUid)
                    .limit(TOTAL_ITEMS);
            timelineCollection = FirebaseFirestore.getInstance().collection(Constants.TIMELINE);
            roomsCollection = FirebaseFirestore.getInstance().collection(Constants.MESSAGES);
            postsCollection = FirebaseFirestore.getInstance().collection(Constants.POSTS);
            postCountQuery = postsCollection.orderBy("time").whereEqualTo("uid", mUid);

            //firebase
            databaseReference = FirebaseDatabase.getInstance().getReference(Constants.RANDOM_PUSH_ID);


            fetchData();
            recyclerView();
            setCollections();

            if (savedInstanceState != null){
                recyclerViewState = savedInstanceState.getParcelable(KEY_LAYOUT_POSITION);
                Log.d("Profile saved Instance", "Instance is not null");
            }else {
                Log.d("Saved Instance", "Instance is completely null");
            }

            //show hidden views
            if (!firebaseAuth.getCurrentUser().getUid().equals(mUid)){
                mSendMessageImageView.setVisibility(View.VISIBLE);
                mFollowButton.setVisibility(View.VISIBLE);
            }


            usersCollections.document(mUid).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                    if (e != null) {
                        Log.w(TAG, "Listen error", e);
                        return;
                    }

                    if (documentSnapshot.exists()){
                        Andeqan andeqan = documentSnapshot.toObject(Andeqan.class);
                        if (firebaseAuth.getCurrentUser().getUid().equals(mUid)){
                            collapsingToolbarLayout.setTitle("Profile");
                        }else {
                            collapsingToolbarLayout.setTitle(andeqan.getUsername());
                        }
                    }

                }
            });

            mCollectionsRecyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener() {
                @Override
                public void onLoadMore() {
                    setNextCollections();
                }
            });

            //INITIALIZE CLICK LISTENERS
            mFollowersCountTextView.setOnClickListener(this);
            mFollowingCountTextView.setOnClickListener(this);
            mSendMessageImageView.setOnClickListener(this);
            mFollowButton.setOnClickListener(this);

        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.profile_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action b item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_wallet){
            Intent intent = new Intent(ProfileActivity.this, WalletActivity.class);
            startActivity(intent);
        }

        if (id == R.id.action_signout){
            FragmentManager fragmentManager = getSupportFragmentManager();
            DialogConfirmSingOutFragment dialogConfirmSingOutFragment = DialogConfirmSingOutFragment.newInstance("sing out");
            dialogConfirmSingOutFragment.show(fragmentManager, "delete account fragment");
        }

        if (id == R.id.action_account_settings){
            Intent intent = new Intent(ProfileActivity.this, UpdateProfileActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    private void fetchData(){

        postCountQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (!documentSnapshots.isEmpty()){
                    final int postsCount = documentSnapshots.size();
                    mPostsCountTextView.setText(postsCount + "");
                }else {
                    mPostsCountTextView.setText("0");
                }
            }
        });

        //get followers count
        relationsCollections.document("followers")
                .collection(mUid)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                        if (e != null) {
                            Log.w(TAG, "Listen error", e);
                            return;
                        }


                        if (documentSnapshots.isEmpty()){
                            mFollowersCountTextView.setText("0");
                        }else {
                            mFollowersCountTextView.setText(documentSnapshots.size() + "");
                        }
                    }
                });


        relationsCollections.document("followers").collection(mUid)
                .whereEqualTo("uid", firebaseAuth.getCurrentUser().getUid())
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen error", e);
                            return;
                        }

                        if (documentSnapshots.isEmpty()){
                            mFollowButton.setText("FOLLOW");
                        }else {
                            mFollowButton.setText("FOLLOWING");
                        }
                    }
                });



        //get following count
        relationsCollections.document("following")
                .collection(mUid)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                        if (e != null) {
                            Log.w(TAG, "Listen error", e);
                            return;
                        }


                        if (documentSnapshots.isEmpty()){
                            mFollowingCountTextView.setText("0");
                        }else {
                            mFollowingCountTextView.setText(documentSnapshots.size() + "");
                        }
                    }
                });


        usersCollections.document(mUid)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                        if (e != null) {
                            Log.w(TAG, "Listen error", e);
                            return;
                        }

                        if (documentSnapshot.exists()){
                            final Andeqan cinggulan = documentSnapshot.toObject(Andeqan.class);
                            String firstName = cinggulan.getFirstName();
                            String secondName = cinggulan.getSecondName();
                            final String profileImage = cinggulan.getProfileImage();
                            String bio = cinggulan.getBio();
                            final String profileCover = cinggulan.getProfileCover();

                            mFullNameTextView.setText(firstName + " " + secondName);
                            mBioTextView.setText(bio);
                            Picasso.with(ProfileActivity.this)
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
                                            Picasso.with(ProfileActivity.this)
                                                    .load(profileImage)
                                                    .resize(MAX_WIDTH, MAX_HEIGHT)
                                                    .onlyScaleDown()
                                                    .centerCrop()
                                                    .placeholder(R.drawable.profle_image_background)
                                                    .into(mProifleImageView);

                                        }
                                    });

                            Picasso.with(ProfileActivity.this)
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
                                            Picasso.with(ProfileActivity.this)
                                                    .load(profileCover)
                                                    .fit()
                                                    .centerCrop()
                                                    .into(mProfileCover, new Callback() {
                                                        @Override
                                                        public void onSuccess() {
                                                            Log.d("profile cover", "profile cover found");
                                                        }

                                                        @Override
                                                        public void onError() {
                                                            Log.d("prifle cover", "profile cover not found");
                                                        }
                                                    });


                                        }
                                    });
                        }
                    }
                });
    }

    private void recyclerView(){
        profileCollectionsAdapter = new ProfileCollectionsAdapter(ProfileActivity.this);
        layoutManager = new GridLayoutManager(ProfileActivity.this, 2);
        mCollectionsRecyclerView.setLayoutManager(layoutManager);
        mCollectionsRecyclerView.setAdapter(profileCollectionsAdapter);
        mCollectionsRecyclerView.setHasFixedSize(false);
        mCollectionsRecyclerView.setNestedScrollingEnabled(false);
    }

    private void setCollections(){
        profileCollectionsQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (!documentSnapshots.isEmpty()){
                    //retrieve the first bacth of documentSnapshots
                    for (final DocumentChange change : documentSnapshots.getDocumentChanges()) {
                        switch (change.getType()) {
                            case ADDED:
                                onDocumentAdded(change);
                                break;
                            case MODIFIED:
                                onDocumentModified(change);
                                break;
                            case REMOVED:
                                onDocumentRemoved(change);
                                break;
                        }
                    }
                }

            }
        });
    }

    private void setNextCollections(){
        // Get the last visible document
        final int snapshotSize = profileCollectionsAdapter.getItemCount();
        DocumentSnapshot lastVisible = profileCollectionsAdapter.getSnapshot(snapshotSize - 1);

        //retrieve the first bacth of documentSnapshots
        nextCollectionsQuery = collectionsCollection.orderBy("time", Query.Direction.DESCENDING)
                .whereEqualTo("uid", mUid)
                .startAfter(lastVisible)
                .limit(TOTAL_ITEMS);

        nextCollectionsQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (!documentSnapshots.isEmpty()){
                    //retrieve the first bacth of documentSnapshots
                    for (final DocumentChange change : documentSnapshots.getDocumentChanges()) {
                        switch (change.getType()) {
                            case ADDED:
                                onDocumentAdded(change);
                                break;
                            case MODIFIED:
                                onDocumentModified(change);
                                break;
                            case REMOVED:
                                onDocumentRemoved(change);
                                break;
                        }
                    }
                }
            }
        });
    }

    protected void onDocumentAdded(DocumentChange change) {
        collectionsIds.add(change.getDocument().getId());
        documentSnapshots.add(change.getDocument());
        profileCollectionsAdapter.setProfileCollections(documentSnapshots);
        profileCollectionsAdapter.notifyItemInserted(documentSnapshots.size() -1);
        profileCollectionsAdapter.getItemCount();

    }

    protected void onDocumentModified(DocumentChange change) {
        if (change.getOldIndex() == change.getNewIndex()) {
            // Item changed but remained in same position
            documentSnapshots.set(change.getOldIndex(), change.getDocument());
            profileCollectionsAdapter.notifyItemChanged(change.getOldIndex());
        } else {
            // Item changed and changed position
            documentSnapshots.remove(change.getOldIndex());
            documentSnapshots.add(change.getNewIndex(), change.getDocument());
            profileCollectionsAdapter.notifyItemRangeChanged(0, documentSnapshots.size());
        }
    }

    protected void onDocumentRemoved(DocumentChange change) {
        documentSnapshots.remove(change.getOldIndex());
        profileCollectionsAdapter.notifyItemRemoved(change.getOldIndex());
        profileCollectionsAdapter.notifyItemRangeChanged(0, documentSnapshots.size());
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
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
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }


    @Override
    public void onClick(View v){
        if (v == mFollowingCountTextView) {
            Intent intent = new Intent(ProfileActivity.this, FollowingActivity.class);
            intent.putExtra(ProfileActivity.EXTRA_USER_UID, mUid);
            startActivity(intent);
        }

        if (v == mFollowersCountTextView){
            Intent intent = new Intent(ProfileActivity.this, FollowersActivity.class);
            intent.putExtra(ProfileActivity.EXTRA_USER_UID, mUid);
            startActivity(intent);
        }

        if (v == mSendMessageImageView){
            processRoom = true;
            roomsCollection.document("rooms")
                    .collection(mUid).document(firebaseAuth.getCurrentUser().getUid())
                    .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                            if (e != null) {
                                Log.w(TAG, "Listen error", e);
                                return;
                            }

                            if (processRoom){
                                if (documentSnapshot.exists()){
                                    Room room = documentSnapshot.toObject(Room.class);
                                    roomId = room.getRoomId();
                                    Intent intent = new Intent(ProfileActivity.this, MessagesAccountActivity.class);
                                    intent.putExtra(ProfileActivity.EXTRA_ROOM_UID, roomId);
                                    intent.putExtra(ProfileActivity.EXTRA_USER_UID, mUid);
                                    startActivity(intent);

                                    processRoom = false;

                                }else {
                                    roomId = databaseReference.push().getKey();
                                    Intent intent = new Intent(ProfileActivity.this, MessagesAccountActivity.class);
                                    intent.putExtra(ProfileActivity.EXTRA_ROOM_UID, roomId);
                                    intent.putExtra(ProfileActivity.EXTRA_USER_UID, mUid);
                                    startActivity(intent);

                                    processRoom = false;
                                }
                            }
                        }
                    });
        }

        if (v == mFollowButton){
            processFollow = true;
            relationsCollections.document("followers")
                    .collection(mUid)
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
                                    relationsCollections.document("followers").collection(mUid)
                                            .document(firebaseAuth.getCurrentUser().getUid()).set(follower)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Timeline timeline = new Timeline();
                                                    final long time = new Date().getTime();

                                                    final String postid = databaseReference.push().getKey();
                                                    timeline.setPushId(mUid);
                                                    timeline.setTime(time);
                                                    timeline.setUid(firebaseAuth.getCurrentUser().getUid());
                                                    timeline.setType("followers");
                                                    timeline.setPostId(postid);
                                                    timeline.setStatus("unRead");

                                                    timelineCollection.document(mUid).collection("timeline")
                                                            .document(firebaseAuth.getCurrentUser().getUid())
                                                            .set(timeline);
                                                }
                                            });
                                    final Relation following = new Relation();
                                    following.setUid(mUid);
                                    relationsCollections.document("following").collection(firebaseAuth.getCurrentUser().getUid())
                                            .document(mUid).set(following);
                                    processFollow = false;
                                    mFollowButton.setText("Following");
                                }else {
                                    relationsCollections.document("followers").collection(mUid)
                                            .document(firebaseAuth.getCurrentUser().getUid()).delete();
                                    relationsCollections.document("following").collection(firebaseAuth.getCurrentUser().getUid())
                                            .document(mUid).delete();
                                    processFollow = false;
                                    mFollowButton.setText("Follow");
                                }
                            }
                        }
                    });
        }

    }

}

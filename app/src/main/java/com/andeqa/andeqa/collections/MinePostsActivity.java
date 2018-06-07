package com.andeqa.andeqa.collections;

import android.content.Intent;
import android.os.Parcelable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.R;
import com.andeqa.andeqa.creation.CreatePostActivity;
import com.andeqa.andeqa.models.Collection;
import com.andeqa.andeqa.models.TransactionDetails;
import com.andeqa.andeqa.settings.CollectionSettingsActivity;
import com.andeqa.andeqa.utils.EndlessRecyclerOnScrollListener;
import com.google.firebase.auth.FirebaseAuth;
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
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MinePostsActivity extends AppCompatActivity implements View.OnClickListener {
    @Bind(R.id.collectionsPostsRecyclerView)RecyclerView mCollectionsPostsRecyclerView;
    @Bind(R.id.createPostButton)FloatingActionButton mCreatePostButton;
    @Bind(R.id.collectionCoverImageView)ImageView mCollectionCoverImageView;
    @Bind(R.id.collectionNoteTextView)TextView mCollectionNoteTextView;
    @Bind(R.id.collapsing_toolbar)CollapsingToolbarLayout collapsingToolbarLayout;
    @Bind(R.id.collectionNameTextView)TextView mCollectionNameTextView;
    private static final String TAG = CollectionPostsActivity.class.getSimpleName();

    //firestore reference
    private CollectionReference collectionsPosts;
    private CollectionReference collectionCollection;
    private CollectionReference usersCollection;
    private CollectionReference collectionOwnersCollection;

    private Query collectionPostsQuery;
    //firebase auth
    private FirebaseAuth firebaseAuth;
    //firestore adapters
    private MinePostsAdapters minePostsAdapters;
    private static final String KEY_LAYOUT_POSITION = "layout pooition";
    private Parcelable recyclerViewState;
    private  static final int MAX_WIDTH = 400;
    private static final int MAX_HEIGHT = 400;
    private int TOTAL_ITEMS = 10;
    private LinearLayoutManager layoutManager;
    private static final String EXTRA_USER_UID = "uid";

    private String collectionId;
    private String mUid;
    private String mSource;
    private static final String COLLECTION_ID = "collection id";
    private List<String> mSnapshotsIds = new ArrayList<>();
    private List<DocumentSnapshot> mSnapshots = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mine_posts);
        ButterKnife.bind(this);
        //initialize click listener
        mCreatePostButton.setOnClickListener(this);

        //FIREBASE AUTH
        firebaseAuth = FirebaseAuth.getInstance();

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationIcon(R.drawable.ic_arrow);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        collapsingToolbarLayout.setTitle("Posts");


        if (firebaseAuth.getCurrentUser()!= null){

            collectionId = getIntent().getStringExtra(COLLECTION_ID);
            mUid = getIntent().getStringExtra(EXTRA_USER_UID);

            collectionsPosts = FirebaseFirestore.getInstance().collection(Constants.COLLECTIONS_POSTS)
                    .document("collections").collection(collectionId);
            collectionCollection = FirebaseFirestore.getInstance().collection(Constants.USER_COLLECTIONS);
            collectionPostsQuery = collectionsPosts.orderBy("time", Query.Direction.DESCENDING);
            usersCollection = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);
            collectionOwnersCollection = FirebaseFirestore.getInstance().collection(Constants.COLLECTION_OWNERS);

            if (savedInstanceState != null){
                recyclerViewState = savedInstanceState.getParcelable(KEY_LAYOUT_POSITION);
                Log.d("Profile saved Instance", "Instance is not null");
            }else {
                Log.d("Saved Instance", "Instance is completely null");
            }

            //add appropriate title to the action bar
            mCollectionsPostsRecyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener() {
                @Override
                public void onLoadMore() {
                    setNextCollectionPosts();
                }
            });

        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mSnapshots.clear();
        setCollectionPosts();
        setRecyclerView();
        setCollectionsInfo();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.colletion_settings, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_collections_settings){
            Intent intent = new Intent(this,    CollectionSettingsActivity.class);
            intent.putExtra(MinePostsActivity.COLLECTION_ID, collectionId);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        collectionOwnersCollection.document(collectionId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (documentSnapshot.exists()){
                    TransactionDetails transactionDetails = documentSnapshot.toObject(TransactionDetails.class);
                    final String ownerUid = transactionDetails.getUser_id();
                    Log.d("owner uid", ownerUid);

                    if (!firebaseAuth.getCurrentUser().getUid().equals(ownerUid)){
                        menu.clear();
                    }
                }
            }
        });


        return super.onPrepareOptionsMenu(menu);
    }



    private void setRecyclerView(){
        // RecyclerView
        minePostsAdapters = new MinePostsAdapters(MinePostsActivity.this);
        mCollectionsPostsRecyclerView.setAdapter(minePostsAdapters);
        mCollectionsPostsRecyclerView.setHasFixedSize(false);
        layoutManager = new LinearLayoutManager(MinePostsActivity.this);
        layoutManager.setReverseLayout(true);
        mCollectionsPostsRecyclerView.setLayoutManager(layoutManager);
        mCollectionsPostsRecyclerView.setNestedScrollingEnabled(false);
    }

    private void setCollectionsInfo(){

        collectionOwnersCollection.document(collectionId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (documentSnapshot.exists()){
                    TransactionDetails transactionDetails = documentSnapshot.toObject(TransactionDetails.class);
                    final String ownerUid = transactionDetails.getUser_id();
                    Log.d("owner uid", ownerUid);

                    if (firebaseAuth.getCurrentUser().getUid().equals(ownerUid)){
                        mCreatePostButton.setVisibility(View.VISIBLE);
                    }
                }
            }
        });

        collectionCollection.document(collectionId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (documentSnapshot.exists()){
                    Collection collection = documentSnapshot.toObject(Collection.class);
                    final String name = collection.getName();
                    final String note = collection.getNote();
                    final String cover = collection.getImage();

                    mCollectionNameTextView.setText(name);
                    mCollectionNoteTextView.setText(note);
                    Picasso.with(MinePostsActivity.this)
                            .load(cover)
                            .resize(MAX_WIDTH, MAX_HEIGHT)
                            .onlyScaleDown()
                            .centerCrop()
                            .networkPolicy(NetworkPolicy.OFFLINE)
                            .into(mCollectionCoverImageView, new Callback() {
                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onError() {
                                    Picasso.with(MinePostsActivity.this)
                                            .load(cover)
                                            .resize(MAX_WIDTH, MAX_HEIGHT)
                                            .onlyScaleDown()
                                            .centerCrop()
                                            .into(mCollectionCoverImageView);
                                }
                            });

                }
            }
        });
    }

    private void setCollectionPosts(){
        collectionPostsQuery.limit(TOTAL_ITEMS)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                        if (e != null) {
                            Log.w(TAG, "Listen error", e);
                            return;
                        }


                        if (!documentSnapshots.isEmpty()){
                            //retrieve the first bacth of mSnapshots
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


    private void setNextCollectionPosts(){
        // Get the last visible document
        final int snapshotSize = minePostsAdapters.getItemCount();

        DocumentSnapshot lastVisible = minePostsAdapters.getSnapshot(snapshotSize - 1);

        //retrieve the first bacth of mSnapshots
        Query nextCollectionPostsQuery = collectionsPosts.orderBy("time", Query.Direction.DESCENDING)
                .startAfter(lastVisible)
                .limit(TOTAL_ITEMS);

        nextCollectionPostsQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (!documentSnapshots.isEmpty()){
                    //retrieve the first bacth of mSnapshots
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
        mSnapshotsIds.add(change.getDocument().getId());
        mSnapshots.add(change.getDocument());
        minePostsAdapters.setCollectionPosts(mSnapshots);
        minePostsAdapters.notifyItemInserted(mSnapshots.size() -1);
        minePostsAdapters.getItemCount();

    }

    protected void onDocumentModified(DocumentChange change) {
        try {
            if (change.getOldIndex() == change.getNewIndex()) {
                // Item changed but remained in same position
                mSnapshots.set(change.getOldIndex(), change.getDocument());
                minePostsAdapters.notifyItemChanged(change.getOldIndex());
            } else {
                // Item changed and changed position
                mSnapshots.remove(change.getOldIndex());
                mSnapshots.add(change.getNewIndex(), change.getDocument());
                minePostsAdapters.notifyItemRangeChanged(0, mSnapshots.size());
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    protected void onDocumentRemoved(DocumentChange change) {
       try {
           mSnapshots.remove(change.getOldIndex());
           minePostsAdapters.notifyItemRemoved(change.getOldIndex());
           minePostsAdapters.notifyItemRangeChanged(0, mSnapshots.size());
       }catch (Exception e){
           e.printStackTrace();
       }
    }


    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onClick(View v){
        if (v == mCreatePostButton){
            Intent intent = new Intent(MinePostsActivity.this, CreatePostActivity.class);
            intent.putExtra(MinePostsActivity.COLLECTION_ID, collectionId);
            startActivity(intent);
        }

    }

}
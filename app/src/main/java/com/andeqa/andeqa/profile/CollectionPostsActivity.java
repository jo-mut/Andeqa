package com.andeqa.andeqa.profile;

import android.content.Intent;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.R;
import com.andeqa.andeqa.creation.CreatePostActivity;
import com.andeqa.andeqa.models.Andeqan;
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

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class CollectionPostsActivity extends AppCompatActivity implements View.OnClickListener {
    @Bind(R.id.collectionsPostsRecyclerView)RecyclerView mCollectionsPostsRecyclerView;
    @Bind(R.id.createPostImageView)ImageView mCreatePostImageView;
    private static final String TAG = CollectionPostsActivity.class.getSimpleName();
    //firestore reference
    private CollectionReference collectionsCollection;
    private CollectionReference usersCollection;
    private Query collectionPostsQuery;
    //firebase auth
    private FirebaseAuth firebaseAuth;
    //firestore adapters
    private CollectionPostsAdapter collectionPostsAdapter;
    private static final String KEY_LAYOUT_POSITION = "layout pooition";
    private Parcelable recyclerViewState;
    private  static final int MAX_WIDTH = 200;
    private static final int MAX_HEIGHT = 200;
    private int TOTAL_ITEMS = 10;
    private DocumentSnapshot lastVisible;
    private LinearLayoutManager layoutManager;
    private static final String EXTRA_USER_UID = "uid";

    private String collectionId;
    private String mUid;
    private static final String COLLECTION_ID = "collection id";
    private List<String> mSnapshotsIds = new ArrayList<>();
    private List<DocumentSnapshot> mSnapshots = new ArrayList<>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection_posts);
        ButterKnife.bind(this);
        //initialize click listener
        mCreatePostImageView.setOnClickListener(this);

        //FIREBASE AUTH
        firebaseAuth = FirebaseAuth.getInstance();

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        if (firebaseAuth.getCurrentUser()!= null){

            collectionId = getIntent().getStringExtra(COLLECTION_ID);
            if(collectionId == null){
                throw new IllegalArgumentException("pass an collection id");
            }

            mUid = getIntent().getStringExtra(EXTRA_USER_UID);
            if(mUid == null){
                throw new IllegalArgumentException("pass an EXTRA_UID");
            }


            collectionsCollection = FirebaseFirestore.getInstance().collection(Constants.COLLECTIONS)
                    .document("collection_posts").collection(collectionId);
            collectionPostsQuery = collectionsCollection.orderBy("time", Query.Direction.DESCENDING)
                    .whereEqualTo("uid", mUid);
            usersCollection = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);

            setCollectionPosts();
            setRecyclerView();
            if (savedInstanceState != null){
                recyclerViewState = savedInstanceState.getParcelable(KEY_LAYOUT_POSITION);
                Log.d("Profile saved Instance", "Instance is not null");
            }else {
                Log.d("Saved Instance", "Instance is completely null");
            }

            //add appropriate title to the action bar
            if (mUid.equals(firebaseAuth.getCurrentUser().getUid())){
                mCreatePostImageView.setVisibility(View.VISIBLE);
                getSupportActionBar().setTitle("Add to collection");
            }else {
                usersCollection.document(mUid).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                        if (e != null) {
                            Log.w(TAG, "Listen error", e);
                            return;
                        }

                        if (documentSnapshot.exists()){
                            final Andeqan cinggulan = documentSnapshot.toObject(Andeqan.class);
                            final String username = cinggulan.getUsername();
                            getSupportActionBar().setTitle(username + "'s" + " collection");
                        }
                    }
                });
            }

            mCollectionsPostsRecyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener() {
                @Override
                public void onLoadMore() {
                    setNextCollectionPosts();
                }
            });

        }
    }

    private void setRecyclerView(){
        // RecyclerView
        collectionPostsAdapter = new CollectionPostsAdapter(CollectionPostsActivity.this);
        mCollectionsPostsRecyclerView.setAdapter(collectionPostsAdapter);
        mCollectionsPostsRecyclerView.setHasFixedSize(false);
        layoutManager = new LinearLayoutManager(CollectionPostsActivity.this);
        mCollectionsPostsRecyclerView.setLayoutManager(layoutManager);
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
        final int snapshotSize = collectionPostsAdapter.getItemCount();
        DocumentSnapshot lastVisible = collectionPostsAdapter.getSnapshot(snapshotSize - 1);

        //retrieve the first bacth of mSnapshots
        Query nextCollectionPostsQuery = collectionsCollection.orderBy("time", Query.Direction.DESCENDING)
                .whereEqualTo("uid", firebaseAuth.getCurrentUser().getUid())
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
        collectionPostsAdapter.setCollectionPosts(mSnapshots);
        collectionPostsAdapter.notifyItemInserted(mSnapshots.size() -1);
        collectionPostsAdapter.getItemCount();

    }

    protected void onDocumentModified(DocumentChange change) {
        if (change.getOldIndex() == change.getNewIndex()) {
            // Item changed but remained in same position
            mSnapshots.set(change.getOldIndex(), change.getDocument());
            collectionPostsAdapter.notifyItemChanged(change.getOldIndex());
        } else {
            // Item changed and changed position
            mSnapshots.remove(change.getOldIndex());
            mSnapshots.add(change.getNewIndex(), change.getDocument());
            collectionPostsAdapter.notifyItemRangeChanged(0, mSnapshots.size());
        }
    }

    protected void onDocumentRemoved(DocumentChange change) {
        mSnapshots.remove(change.getOldIndex());
        collectionPostsAdapter.notifyItemRemoved(change.getOldIndex());
        collectionPostsAdapter.notifyItemRangeChanged(0, mSnapshots.size());
    }


    @Override
    public void onResume() {
        super.onResume();
        if (recyclerViewState != null){
            layoutManager.onRestoreInstanceState(recyclerViewState);
        }
    }

    @Override
    public void onClick(View v){
        if (v == mCreatePostImageView){
            Intent intent = new Intent(CollectionPostsActivity.this, CreatePostActivity.class);
            intent.putExtra(CollectionPostsActivity.COLLECTION_ID, collectionId);
            startActivity(intent);
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


}

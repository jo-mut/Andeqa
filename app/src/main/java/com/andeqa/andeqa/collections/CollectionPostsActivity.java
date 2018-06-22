package com.andeqa.andeqa.collections;

import android.content.Intent;
import android.os.Parcelable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
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
import com.andeqa.andeqa.models.Transaction;
import com.andeqa.andeqa.settings.CollectionSettingsActivity;
import com.andeqa.andeqa.utils.EndlessRecyclerOnScrollListener;
import com.andeqa.andeqa.utils.ItemOffsetDecoration;
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

public class CollectionPostsActivity extends AppCompatActivity
        implements View.OnClickListener{
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
    private CollectionPostsAdapter collectionPostsAdapter;
    private static final String KEY_LAYOUT_POSITION = "layout pooition";
    private Parcelable recyclerViewState;
    private  static final int MAX_WIDTH = 400;
    private static final int MAX_HEIGHT = 400;
    private int TOTAL_ITEMS = 10;
    private StaggeredGridLayoutManager layoutManager;
    private static final String EXTRA_USER_UID = "uid";

    private String collectionId;
    private String mUid;
    private String mSource;
    private static final String COLLECTION_ID = "collection id";
    private List<String> mSnapshotsIds = new ArrayList<>();
    private List<DocumentSnapshot> mSnapshots = new ArrayList<>();
    int spanCount = 2; //
    int spacing = 10; //
    boolean includeEdge = false;
    private ItemOffsetDecoration itemOffsetDecoration;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection_posts);
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
            collectionPostsQuery = collectionsPosts.orderBy("time", Query.Direction.ASCENDING);
            usersCollection = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);
            collectionOwnersCollection = FirebaseFirestore.getInstance().collection(Constants.COLLECTION_OWNERS);

            mCollectionsPostsRecyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener() {
                @Override
                public void onLoadMore() {
                    setNextCollections();
                }
            });

        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStart() {
        super.onStart();
        mSnapshots.clear();
        setRecyclerView();
        setCollectionPosts();
        setCollectionsInfo();
    }

    @Override
    public void onStop() {
        super.onStop();
        mSnapshots.clear();
        mCollectionsPostsRecyclerView.removeItemDecoration(itemOffsetDecoration);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
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
            Intent intent = new Intent(this, CollectionSettingsActivity.class);
            intent.putExtra(CollectionPostsActivity.COLLECTION_ID, collectionId);
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
                    android.util.Log.w(TAG, "Listen error", e);
                    return;
                }

                if (documentSnapshot.exists()){
                    Transaction transaction = documentSnapshot.toObject(Transaction.class);
                    final String ownerUid = transaction.getUser_id();
                    android.util.Log.d("owner uid", ownerUid);

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
        collectionPostsAdapter = new CollectionPostsAdapter(CollectionPostsActivity.this);
        mCollectionsPostsRecyclerView.setAdapter(collectionPostsAdapter);
        mCollectionsPostsRecyclerView.setHasFixedSize(false);
        layoutManager = new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL);
        itemOffsetDecoration = new ItemOffsetDecoration(this, R.dimen.item_off_set);
        mCollectionsPostsRecyclerView.addItemDecoration(itemOffsetDecoration);
        mCollectionsPostsRecyclerView.setLayoutManager(layoutManager);
        ViewCompat.setNestedScrollingEnabled(mCollectionsPostsRecyclerView,false);
    }

    private void setCollectionsInfo(){

        collectionOwnersCollection.document(collectionId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                if (e != null) {
                    android.util.Log.w(TAG, "Listen error", e);
                    return;
                }

                if (documentSnapshot.exists()){
                    Transaction transaction = documentSnapshot.toObject(Transaction.class);
                    final String ownerUid = transaction.getUser_id();
                    android.util.Log.d("owner uid", ownerUid);

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
                    android.util.Log.w(TAG, "Listen error", e);
                    return;
                }

                if (documentSnapshot.exists()){
                    Collection collection = documentSnapshot.toObject(Collection.class);
                    final String name = collection.getName();
                    final String note = collection.getNote();
                    final String cover = collection.getImage();

                    mCollectionNameTextView.setText(name);
                    mCollectionNoteTextView.setText(note);
                    Picasso.with(CollectionPostsActivity.this)
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
                                    Picasso.with(CollectionPostsActivity.this)
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
                            android.util.Log.w(TAG, "Listen error", e);
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

    private void setNextCollections(){
        // Get the last visible document
        final int snapshotSize = collectionPostsAdapter.getItemCount();
        if (snapshotSize == 0){

        }else {
            DocumentSnapshot lastVisible = collectionPostsAdapter.getSnapshot(snapshotSize - 1);

            //retrieve the first bacth of mSnapshots
            Query nextCollectionPostsQuery = collectionsPosts.orderBy("time", Query.Direction.ASCENDING)
                    .startAfter(lastVisible)
                    .limit(TOTAL_ITEMS);

            nextCollectionPostsQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                    if (e != null) {
                        android.util.Log.w(TAG, "Listen error", e);
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
        try{
            mSnapshots.remove(change.getOldIndex());
            collectionPostsAdapter.notifyItemRemoved(change.getOldIndex());
            collectionPostsAdapter.notifyItemRangeChanged(0, mSnapshots.size());
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
            Intent intent = new Intent(CollectionPostsActivity.this, CreatePostActivity.class);
            intent.putExtra(CollectionPostsActivity.COLLECTION_ID, collectionId);
            startActivity(intent);
        }

    }

}

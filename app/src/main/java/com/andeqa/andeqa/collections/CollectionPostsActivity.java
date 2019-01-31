package com.andeqa.andeqa.collections;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.support.constraint.ConstraintSet;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.R;
import com.andeqa.andeqa.camera.CameraActivity;
import com.andeqa.andeqa.home.PostsAdapter;
import com.andeqa.andeqa.models.Collection;
import com.andeqa.andeqa.settings.CollectionSettingsActivity;
import com.andeqa.andeqa.utils.BottomReachedListener;
import com.andeqa.andeqa.utils.EndlessStaggeredScrollListener;
import com.andeqa.andeqa.utils.ItemOffsetDecoration;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
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
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class CollectionPostsActivity extends AppCompatActivity
        implements View.OnClickListener{
    @Bind(R.id.collectionsPostsRecyclerView)RecyclerView mCollectionsPostsRecyclerView;
    @Bind(R.id.addRelativeLayout)RelativeLayout mAddRelativeLayout;
    @Bind(R.id.collectionCoverImageView)ImageView mCollectionCoverImageView;
    @Bind(R.id.collectionNoteTextView)TextView mCollectionNoteTextView;
    @Bind(R.id.collapsingToolbar)CollapsingToolbarLayout collapsingToolbarLayout;
    @Bind(R.id.collectionNameTextView)TextView mCollectionNameTextView;
    @Bind(R.id.collectionSettingsRelativeLayout)RelativeLayout mCollectionSettingsRelativeLayout;
    @Bind(R.id.toolbar)Toolbar toolbar;

    private static final String TAG = CollectionPostsActivity.class.getSimpleName();
    //firestore reference
    private CollectionReference postsCollectionReference;
     private DatabaseReference impressionReference;
    private CollectionReference collectionCollection;
    //firebase auth
    private FirebaseAuth firebaseAuth;
    //lists
    private List<DocumentSnapshot> snapshots = new ArrayList<>();
    //layouts
    private ItemOffsetDecoration itemOffsetDecoration;
    private StaggeredGridLayoutManager layoutManager;
    //strings
    private String mCollectionId;
    private String mUid;
    private static final String COLLECTION_ID = "collection id";
    private static final String EXTRA_USER_UID =  "uid";
    private static final String IMAGE = "image";
    private static final String VIDEO = "video";
    private static final String TITLE = "title";
    private static final String DESCRIPTION = "description";
    //int
    private static final int TOTAL_ITEMS = 10;
    private static final int INITIAL_ITEMS = 20;
    //boolean
    private ProgressDialog progressDialog;
    //adapter
    private PostsAdapter postsAdapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection_posts);
        ButterKnife.bind(this);
        //initialize click listener
        mAddRelativeLayout.setOnClickListener(this);
        mCollectionSettingsRelativeLayout.setOnClickListener(this);

        setSupportActionBar(toolbar);
        collapsingToolbarLayout.setTitle("Posts");
        toolbar.setNavigationIcon(R.drawable.ic_arrow);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        //get intent extras
        getIntents();
        //initialize firebase references
        initReferences();
        //shows the collections settings icon only to the creators of the collection
        if (mUid.equals(firebaseAuth.getCurrentUser().getUid())){
            mCollectionSettingsRelativeLayout.setVisibility(View.VISIBLE);
        }

        //set up information about the collection
        setCollectionsInfo();
        //show progress dialog when adding new post
        createPostProgressDialog();
        //set the recycler view adapter
        setRecyclerView();
        mCollectionsPostsRecyclerView.addItemDecoration(itemOffsetDecoration);
        //get the remote data
        getCollectionPosts();

        mCollectionsPostsRecyclerView.addOnScrollListener(new EndlessStaggeredScrollListener() {
            @Override
            public void onLoadMore() {
                getNextPosts();

            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStart() {
        super.onStart();
        //set up the collections  posts
        mCollectionsPostsRecyclerView.addItemDecoration(itemOffsetDecoration);

    }

    @Override
    public void onStop() {
        super.onStop();
        mCollectionsPostsRecyclerView.removeItemDecoration(itemOffsetDecoration);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void createPostProgressDialog(){
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onClick(View v){
        if (v == mAddRelativeLayout){
            Intent intent = new Intent(CollectionPostsActivity.this, CameraActivity.class);
            intent.putExtra(CollectionPostsActivity.COLLECTION_ID, mCollectionId);
            startActivity(intent);
            finish();
        }

        if (v == mCollectionSettingsRelativeLayout){
            Intent intent = new Intent(this, CollectionSettingsActivity.class);
            intent.putExtra(CollectionPostsActivity.COLLECTION_ID, mCollectionId);
            startActivity(intent);
        }

    }

    private void getIntents(){
        if (getIntent().getExtras() != null){
            mCollectionId = getIntent().getStringExtra(COLLECTION_ID);
            mUid = getIntent().getStringExtra(EXTRA_USER_UID);
        }

    }

    private void initReferences(){
        //firestore
        firebaseAuth = FirebaseAuth.getInstance();
        //firestore references
        collectionCollection = FirebaseFirestore.getInstance().collection(Constants.COLLECTIONS);
        postsCollectionReference = FirebaseFirestore.getInstance().collection(Constants.POSTS);
        //firebase database references
        impressionReference = FirebaseDatabase.getInstance().getReference(Constants.VIEWS);
        impressionReference.keepSynced(true);
    }


    private void setCollectionsInfo(){

        collectionCollection.document(mCollectionId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
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
                    final String userId = collection.getUser_id();

                    mCollectionNameTextView.setText(name);

                    if (!TextUtils.isEmpty(note)){
                        mCollectionNoteTextView.setVisibility(View.VISIBLE);
                        mCollectionNoteTextView.setText(note);
                    }

                    Glide.with(getApplicationContext())
                            .load(cover)
                            .apply(new RequestOptions()
                            .diskCacheStrategy(DiskCacheStrategy.DATA))
                            .into(mCollectionCoverImageView);

                }
            }
        });
    }

    private void getCollectionPosts(){
        Query query = postsCollectionReference
                .orderBy("time", Query.Direction.DESCENDING)
                .whereEqualTo("collection_id", mCollectionId)
                .limit(INITIAL_ITEMS);

        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable QuerySnapshot documentSnapshots,
                                @javax.annotation.Nullable FirebaseFirestoreException e) {

                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (!documentSnapshots.isEmpty()){
                    for (DocumentSnapshot snapshot: documentSnapshots){
                        snapshots.add(snapshot);
                        postsAdapter.notifyItemInserted(snapshots.size() - 1);
                    }

                }
            }
        });

    }

    private void getNextPosts(){

        DocumentSnapshot last = snapshots.get(snapshots.size() - 1);

        Query query = postsCollectionReference
                .orderBy("time", Query.Direction.DESCENDING)
                .whereEqualTo("collection_id", mCollectionId)
                .startAfter(last).limit(TOTAL_ITEMS);

        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(final QuerySnapshot documentSnapshots) {

                if (!documentSnapshots.isEmpty()){

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            for (DocumentSnapshot snapshot: documentSnapshots){
                                snapshots.add(snapshot);
                                postsAdapter.notifyItemInserted(snapshots.size() - 1);
                            }
                        }
                    }, 4000);

                }
            }
        });

    }

    private void setRecyclerView(){
        postsAdapter = new PostsAdapter(this, snapshots);
        postsAdapter.setHasStableIds(true);
        mCollectionsPostsRecyclerView.setHasFixedSize(false);
        mCollectionsPostsRecyclerView.setAdapter(postsAdapter);
        layoutManager = new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL);
        itemOffsetDecoration = new ItemOffsetDecoration(this, R.dimen.item_off_set);
        mCollectionsPostsRecyclerView.setLayoutManager(layoutManager);
        ViewCompat.setNestedScrollingEnabled(mCollectionsPostsRecyclerView, false);

    }



}

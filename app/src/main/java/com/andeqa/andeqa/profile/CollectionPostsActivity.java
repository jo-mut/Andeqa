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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

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
    private int TOTAL_ITEMS = 4;
    private DocumentSnapshot lastVisible;
    private LinearLayoutManager layoutManager;
    private static final String EXTRA_USER_UID = "uid";

    private String collectionId;
    private String mUid;
    private static final String COLLECTION_ID = "collection id";



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
            if (savedInstanceState != null){
                recyclerViewState = savedInstanceState.getParcelable(KEY_LAYOUT_POSITION);
                Log.d("Profile saved Instance", "Instance is not null");
            }else {
                Log.d("Saved Instance", "Instance is completely null");
            }

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
                            final Andeqan andeqan = documentSnapshot.toObject(Andeqan.class);
                            final String username = andeqan.getUsername();
                            getSupportActionBar().setTitle(username + "'s" + " collection");
                        }
                    }
                });
            }



        }
    }

    private void recyclerViewScrolling(){
        mCollectionsPostsRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (!recyclerView.canScrollVertically(-1)) {
                    onScrolledToTop();
                } else if (!recyclerView.canScrollVertically(1)) {
                    onScrolledToBottom();
                } else if (dy < 0) {
                    onScrolledUp();
                } else if (dy > 0) {
                    onScrolledDown();
                }
            }
        });
    }

    public void onScrolledUp() {}

    public void onScrolledDown() {}

    public void onScrolledToTop() {

    }

    public void onScrolledToBottom() {

    }


    private void setCollectionPosts(){
        collectionPostsQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                        if (e != null) {
                            Log.w(TAG, "Listen error", e);
                            return;
                        }


                        if (!documentSnapshots.isEmpty()){
                            // RecyclerView
                            collectionPostsAdapter = new CollectionPostsAdapter(collectionPostsQuery,
                                    CollectionPostsActivity.this);
                            collectionPostsAdapter.startListening();
                            mCollectionsPostsRecyclerView.setAdapter(collectionPostsAdapter);
                            mCollectionsPostsRecyclerView.setHasFixedSize(false);
                            layoutManager = new LinearLayoutManager(CollectionPostsActivity.this);
                            mCollectionsPostsRecyclerView.setLayoutManager(layoutManager);
                            Log.d("data is present",documentSnapshots.toString());
                        }else {
                            Log.d("data not present",documentSnapshots.toString());
                        }

                    }
                });

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

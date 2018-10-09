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
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.R;
import com.andeqa.andeqa.camera.PicturesActivity;
import com.andeqa.andeqa.models.Collection;
import com.andeqa.andeqa.models.Post;
import com.andeqa.andeqa.models.Relation;
import com.andeqa.andeqa.settings.CollectionSettingsActivity;
import com.andeqa.andeqa.utils.EndlesssStaggeredRecyclerOnScrollListener;
import com.andeqa.andeqa.utils.ItemOffsetDecoration;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnSuccessListener;
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

import javax.annotation.Nullable;

import butterknife.Bind;
import butterknife.ButterKnife;

public class CollectionPostsActivity extends AppCompatActivity
        implements View.OnClickListener{
    @Bind(R.id.collectionsPostsRecyclerView)RecyclerView mCollectionsPostsRecyclerView;
    @Bind(R.id.createPostButton)FloatingActionButton mCreatePostButton;
    @Bind(R.id.collectionCoverImageView)ImageView mCollectionCoverImageView;
    @Bind(R.id.collectionNoteTextView)TextView mCollectionNoteTextView;
    @Bind(R.id.collapsingToolbar)CollapsingToolbarLayout collapsingToolbarLayout;
    @Bind(R.id.collectionNameTextView)TextView mCollectionNameTextView;
    @Bind(R.id.collectionSettingsRelativeLayout)RelativeLayout mCollectionSettingsRelativeLayout;
    @Bind(R.id.followersCountTextView) TextView mFollowersCountTextView;
    @Bind(R.id.followersTextView)TextView mFollowersTextView;
    @Bind(R.id.postsTextView)TextView mPostsTextView;
    @Bind(R.id.postsCountTextView)TextView mPostsCountTextView;
    @Bind(R.id.followTextView)TextView mFollowTextView;

    private static final String TAG = CollectionPostsActivity.class.getSimpleName();
    //firestore reference
    private CollectionReference postsCollection;
    private CollectionReference collectionCollection;
    private CollectionReference usersCollection;
    private CollectionReference collectionOwnersCollection;
    private CollectionReference collectionsRelations;
    private Query postsQuery;
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
    private boolean processFollow = false;
    private ItemOffsetDecoration itemOffsetDecoration;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection_posts);
        ButterKnife.bind(this);
        //initialize click listener
        mCreatePostButton.setOnClickListener(this);
        mCollectionSettingsRelativeLayout.setOnClickListener(this);

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

        collectionId = getIntent().getStringExtra(COLLECTION_ID);
        mUid = getIntent().getStringExtra(EXTRA_USER_UID);

        if (mUid.equals(firebaseAuth.getCurrentUser().getUid())){
            mCollectionSettingsRelativeLayout.setVisibility(View.VISIBLE);
        }

        collectionsRelations = FirebaseFirestore.getInstance().collection(Constants.COLLECTION_RELATIONS);
        collectionCollection = FirebaseFirestore.getInstance().collection(Constants.COLLECTIONS);
        postsCollection = FirebaseFirestore.getInstance().collection(Constants.POSTS);
        postsQuery = postsCollection.orderBy("time", Query.Direction.ASCENDING);
        usersCollection = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);
        collectionOwnersCollection = FirebaseFirestore.getInstance().collection(Constants.COLLECTION_OWNERS);

        mCollectionsPostsRecyclerView.addOnScrollListener(new EndlesssStaggeredRecyclerOnScrollListener() {
            @Override
            public void onLoadMore() {
                setNextCollections();
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
        mSnapshots.clear();
        setRecyclerView();
        setCollectionPosts();
        setCollectionsInfo();
        followCollection();
        getCountOfCollectionFollowers();
        getCountOfCollectionsPosts();
        findIfUserIsFollowing();
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


//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.collection_settings, menu);
//        return super.onCreateOptionsMenu(menu);
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        int id = item.getItemId();
//
//
//        return super.onOptionsItemSelected(item);
//    }

//    @Override
//    public boolean onPrepareOptionsMenu(final Menu menu) {
//
//
//        return super.onPrepareOptionsMenu(menu);
//    }



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

    private void findIfUserIsFollowing(){
        /**show the number of peopl following collection**/
        collectionsRelations.document("following")
                .collection(collectionId)
                .whereEqualTo("following_id", firebaseAuth.getCurrentUser().getUid())
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot documentSnapshots,
                                        @Nullable FirebaseFirestoreException e) {

                        if (e != null) {
                            Log.w(TAG, "Listen error", e);
                            return;
                        }

                        if (!documentSnapshots.isEmpty()){
                            mFollowTextView.setText("FOLLOWING");
                            mCreatePostButton.setVisibility(View.VISIBLE);
                        }else {
                            mFollowTextView.setText("FOLLOW");
                            if (mUid.equals(firebaseAuth.getCurrentUser().getUid())){
                                mCreatePostButton.setVisibility(View.VISIBLE);
                            }else {
                                mCreatePostButton.setVisibility(View.GONE);
                            }
                        }

                    }
                });

    }

    private void getCountOfCollectionFollowers(){
        /**show the number of peopl following collection**/
        collectionsRelations.document("following")
                .collection(collectionId).addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot documentSnapshots,
                                        @Nullable FirebaseFirestoreException e) {

                        if (e != null) {
                            Log.w(TAG, "Listen error", e);
                            return;
                        }

                        if (!documentSnapshots.isEmpty()){
                            int following = documentSnapshots.size();
                            mFollowersCountTextView.setText(following + "");
                            mFollowersTextView.setText("Following");
                        }else {
                            mFollowersCountTextView.setText("0");
                            mFollowersTextView.setText("Following");
                        }

                    }
                });
    }

    private void getCountOfCollectionsPosts(){
        postsQuery.whereEqualTo("collection_id", collectionId)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                        if (e != null) {
                            android.util.Log.w(TAG, "Listen error", e);
                            return;
                        }


                        if (!documentSnapshots.isEmpty()){
                            final int count = documentSnapshots.size();
                            mPostsCountTextView.setText(count + "");
                            mPostsTextView.setText("Posts");
                        }else {
                            mPostsCountTextView.setText("0");
                            mPostsTextView.setText("Posts");
                        }

                    }
                });

    }

    private void followCollection(){
        /**follow or un follow collection*/
        if (mUid.equals(firebaseAuth.getCurrentUser().getUid())){
            mFollowTextView.setVisibility(View.GONE);
        }else {
            mFollowTextView.setVisibility(View.VISIBLE);
            mFollowTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    processFollow = true;
                    collectionsRelations.document("following")
                            .collection(collectionId).whereEqualTo("following_id",
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
                                            final Relation following = new Relation();
                                            following.setFollowing_id(firebaseAuth.getCurrentUser().getUid());
                                            following.setFollowed_id(collectionId);
                                            following.setType("followed_collection");
                                            following.setTime(System.currentTimeMillis());
                                            collectionsRelations.document("following").collection(collectionId)
                                                    .document(firebaseAuth.getCurrentUser().getUid())
                                                    .set(following);

                                            mFollowTextView.setText("FOLLOWING");
                                            processFollow = false;
                                        }else {
                                            collectionsRelations.document("following").collection(collectionId)
                                                    .document(firebaseAuth.getCurrentUser().getUid()).delete();
                                            mFollowTextView.setText("FOLLOW");
                                            processFollow = false;
                                        }
                                    }
                                }
                            });
                }
            });
        }

    }

    private void setCollectionsInfo(){

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

    private void setCollectionPosts(){
        postsQuery.whereEqualTo("collection_id", collectionId).limit(TOTAL_ITEMS)
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
                                        Post post = change.getDocument().toObject(Post.class);
                                        final String type = post.getType();
                                        if (!type.equals("collection_video_post") || !type.equals("single_video_post")){
                                            onDocumentAdded(change);

                                        }
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
        if (snapshotSize !=0){
            DocumentSnapshot lastVisible = collectionPostsAdapter.getSnapshot(snapshotSize - 1);
            //retrieve the first bacth of mSnapshots
            Query nextCollectionPostsQuery = postsCollection.orderBy("time", Query.Direction.ASCENDING)
                    .whereEqualTo("collection_id", collectionId).startAfter(lastVisible)
                    .limit(TOTAL_ITEMS);

            nextCollectionPostsQuery.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot documentSnapshots) {
                    if (!documentSnapshots.isEmpty()){
                        //retrieve the first bacth of mSnapshots
                        for (final DocumentChange change : documentSnapshots.getDocumentChanges()) {
                            switch (change.getType()) {
                                case ADDED:
                                    Post post = change.getDocument().toObject(Post.class);
                                    final String type = post.getType();
                                    if (!type.equals("collection_video_post") || !type.equals("single_video_post")){
                                    onDocumentAdded(change);

                                    }
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
        collectionPostsAdapter.setCollectionsPosts(mSnapshots);
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
            Intent intent = new Intent(CollectionPostsActivity.this, PicturesActivity.class);
            intent.putExtra(CollectionPostsActivity.COLLECTION_ID, collectionId);
            startActivity(intent);
            finish();
        }

        if (v == mCollectionSettingsRelativeLayout){
            Intent intent = new Intent(this, CollectionSettingsActivity.class);
            intent.putExtra(CollectionPostsActivity.COLLECTION_ID, collectionId);
            startActivity(intent);
        }

    }

}

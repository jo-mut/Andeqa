package com.andeqa.andeqa.collections;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
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
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.R;
import com.andeqa.andeqa.creation.CreateActivity;
import com.andeqa.andeqa.models.Collection;
import com.andeqa.andeqa.models.Post;
import com.andeqa.andeqa.models.QueryOptions;
import com.andeqa.andeqa.models.Relation;
import com.andeqa.andeqa.settings.CollectionSettingsActivity;
import com.andeqa.andeqa.utils.BottomReachedListener;
import com.andeqa.andeqa.utils.ItemOffsetDecoration;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import butterknife.Bind;
import butterknife.ButterKnife;

public class CollectionPostsActivity extends AppCompatActivity
        implements View.OnClickListener{
    @Bind(R.id.collectionsPostsRecyclerView)RecyclerView mCollectionsPostsRecyclerView;
    @Bind(R.id.addRelativeLayout)RelativeLayout addToCollectionRelativeLayout;
    @Bind(R.id.collectionCoverImageView)ImageView mCollectionCoverImageView;
    @Bind(R.id.collectionNoteTextView)TextView mCollectionNoteTextView;
    @Bind(R.id.collapsingToolbar)CollapsingToolbarLayout collapsingToolbarLayout;
    @Bind(R.id.collectionNameTextView)TextView mCollectionNameTextView;
    @Bind(R.id.collectionSettingsRelativeLayout)RelativeLayout mCollectionSettingsRelativeLayout;
    @Bind(R.id.followersCountTextView) TextView mFollowersCountTextView;
    @Bind(R.id.postsCountTextView)TextView mPostsCountTextView;
    @Bind(R.id.followTextView)TextView mFollowTextView;

    private static final String TAG = CollectionPostsActivity.class.getSimpleName();
    //firestore reference
    private CollectionReference postsCollection;
    private CollectionReference collectionCollection;
    private CollectionReference collectionsRelations;
    private Query postsQuery;
    private StorageReference storageReference;
    private DatabaseReference databaseReference;
    private CollectionReference queryOptionsReference;
    //firebase auth
    private FirebaseAuth firebaseAuth;
    //firestore adapters
    private CollectionPostsAdapter mCollectionPostsAdapter;
    private int TOTAL_ITEMS = 10;
    private StaggeredGridLayoutManager layoutManager;
    private String mCollectionId;
    private String mUid;
    private String title;
    private String description;
    private String height;
    private String width;
    private String mPostId;
    private String postId;
    private String image;
    private String video;
    private Uri uri;
    private static final String EXTRA_USER_UID = "uid";
    private static final String COLLECTION_ID = "collection id";
    private static final String IMAGE = "image";
    private static final String VIDEO = "video";
    private static final String POST_HEIGHT = "height";
    private static final String POST_WIDTH = "width";
    private static final String TITLE = "title";
    private static final String DESCRIPTION = "description";
    private List<String> mSnapshotsIds = new ArrayList<>();
    private List<DocumentSnapshot> mSnapshots = new ArrayList<>();
    private boolean processFollow = false;
    private ItemOffsetDecoration itemOffsetDecoration;
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection_posts);
        ButterKnife.bind(this);
        //initialize click listener
        addToCollectionRelativeLayout.setOnClickListener(this);
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

        //get intent extras
        if (getIntent().getExtras() != null){
            mCollectionId = getIntent().getStringExtra(COLLECTION_ID);
            mUid = getIntent().getStringExtra(EXTRA_USER_UID);
            title = getIntent().getStringExtra(TITLE);
            description = getIntent().getStringExtra(DESCRIPTION);
            image = getIntent().getStringExtra(IMAGE);
            video = getIntent().getStringExtra(VIDEO);
            height = getIntent().getStringExtra(POST_HEIGHT);
            width = getIntent().getStringExtra(POST_WIDTH);

            if (image != null){
                uri = Uri.fromFile(new File(image));
            }

            if (video != null){
                uri = Uri.fromFile(new File(video));
            }

        }

        if (mUid.equals(firebaseAuth.getCurrentUser().getUid())){
            mCollectionSettingsRelativeLayout.setVisibility(View.VISIBLE);
        }

        //firestore references
        collectionsRelations = FirebaseFirestore.getInstance().collection(Constants.COLLECTION_RELATIONS);
        collectionCollection = FirebaseFirestore.getInstance().collection(Constants.COLLECTIONS);
        postsCollection = FirebaseFirestore.getInstance().collection(Constants.POSTS);
        postsQuery = postsCollection.orderBy("time", Query.Direction.ASCENDING);
        databaseReference = FirebaseDatabase.getInstance().getReference(Constants.RANDOM_PUSH_ID);
        queryOptionsReference = FirebaseFirestore.getInstance().collection(Constants.QUERY_OPTIONS);


        //add the new post to collection
        if (image != null){
            addCollectionImagePost();
        }

        //show progress dialog when adding new post
        createPostProgressDialog();

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

        mCollectionPostsAdapter.setBottomReachedListener(new BottomReachedListener() {
            @Override
            public void onBottomReached(int position) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        setNextCollections();
                    }
                },1000);
            }
        });

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

    private void createPostProgressDialog(){
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
    }

    private void setRecyclerView(){
        // RecyclerView
        mCollectionPostsAdapter = new CollectionPostsAdapter(CollectionPostsActivity.this);
        mCollectionsPostsRecyclerView.setAdapter(mCollectionPostsAdapter);
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
                .collection(mCollectionId)
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
                            addToCollectionRelativeLayout.setVisibility(View.VISIBLE);
                        }else {
                            mFollowTextView.setText("FOLLOW");
                            if (mUid.equals(firebaseAuth.getCurrentUser().getUid())){
                                addToCollectionRelativeLayout.setVisibility(View.VISIBLE);
                            }else {
                                addToCollectionRelativeLayout.setVisibility(View.GONE);
                            }
                        }

                    }
                });

    }

    private void getCountOfCollectionFollowers(){
        /**show the number of peopl following collection**/
        collectionsRelations.document("following")
                .collection(mCollectionId).addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot documentSnapshots,
                                        @Nullable FirebaseFirestoreException e) {

                        if (e != null) {
                            Log.w(TAG, "Listen error", e);
                            return;
                        }

                        if (!documentSnapshots.isEmpty()){
                            int following = documentSnapshots.size();
                            mFollowersCountTextView.setText(following + " following");
                        }else {
                            mFollowersCountTextView.setText("0 following");
                        }

                    }
                });
    }

    private void getCountOfCollectionsPosts(){
        postsQuery.whereEqualTo("collection_id", mCollectionId)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                        if (e != null) {
                            android.util.Log.w(TAG, "Listen error", e);
                            return;
                        }


                        if (!documentSnapshots.isEmpty()){
                            final int count = documentSnapshots.size();
                            mPostsCountTextView.setText(count + " posts");
                        }else {
                            mPostsCountTextView.setText("0 posts");
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
                            .collection(mCollectionId).whereEqualTo("following_id",
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
                                            following.setFollowed_id(mCollectionId);
                                            following.setType("followed_collection");
                                            following.setTime(System.currentTimeMillis());
                                            collectionsRelations.document("following").collection(mCollectionId)
                                                    .document(firebaseAuth.getCurrentUser().getUid())
                                                    .set(following);

                                            mFollowTextView.setText("FOLLOWING");
                                            processFollow = false;
                                        }else {
                                            collectionsRelations.document("following").collection(mCollectionId)
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

    private void setCollectionPosts(){
        postsQuery.whereEqualTo("collection_id", mCollectionId).limit(TOTAL_ITEMS)
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
        final int snapshotSize = mCollectionPostsAdapter.getItemCount();
        if (snapshotSize !=0){
            DocumentSnapshot lastVisible = mCollectionPostsAdapter.getSnapshot(snapshotSize - 1);
            //retrieve the first bacth of mSnapshots
            Query nextCollectionPostsQuery = postsCollection.orderBy("time", Query.Direction.ASCENDING)
                    .whereEqualTo("collection_id", mCollectionId).startAfter(lastVisible)
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
        mCollectionPostsAdapter.setCollectionsPosts(mSnapshots);
        mCollectionPostsAdapter.notifyItemInserted(mSnapshots.size() -1);
        mCollectionPostsAdapter.getItemCount();

    }

    protected void onDocumentModified(DocumentChange change) {
        if (change.getOldIndex() == change.getNewIndex()) {
            // Item changed but remained in same position
            mSnapshots.set(change.getOldIndex(), change.getDocument());
            mCollectionPostsAdapter.notifyItemChanged(change.getOldIndex());
        } else {
            // Item changed and changed position
            mSnapshots.remove(change.getOldIndex());
            mSnapshots.add(change.getNewIndex(), change.getDocument());
            mCollectionPostsAdapter.notifyItemRangeChanged(0, mSnapshots.size());
        }
    }

    protected void onDocumentRemoved(DocumentChange change) {
        try{
            mSnapshots.remove(change.getOldIndex());
            mCollectionPostsAdapter.notifyItemRemoved(change.getOldIndex());
            mCollectionPostsAdapter.notifyItemRangeChanged(0, mSnapshots.size());
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
        if (v == addToCollectionRelativeLayout){
            Intent intent = new Intent(CollectionPostsActivity.this, CreateActivity.class);
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

    private void addCollectionImagePost(){
        progressDialog.show();
        final Uri uri = Uri.fromFile(new File(image));
        //current time
        final long timeStamp = new Date().getTime();
        //push id to organise the posts according to time
        final DatabaseReference reference = databaseReference.push();
        final String pushId = reference.getKey();

        storageReference = FirebaseStorage
                .getInstance().getReference()
                .child(Constants.COLLECTIONS)
                .child(Constants.IMAGES)
                .child(pushId);

        UploadTask uploadTask = storageReference.putFile(uri);
        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }

                // Continue with the task to get the download URL
                return storageReference.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    final Uri downloadUri = task.getResult();
                    Post post = new Post();
                    postsCollection.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot snapshots) {
                            final int size = snapshots.size();
                            final int number = size + 1;
                            final double random = new Random().nextDouble();

                            Post post = new Post();
                            post.setCollection_id(mCollectionId);
                            post.setType("collection_image_post");
                            post.setPost_id(pushId);
                            post.setUser_id(firebaseAuth.getCurrentUser().getUid());
                            post.setRandom_number(random);
                            post.setNumber(number);
                            post.setTime(timeStamp);
                            post.setDeeplink("");
                            post.setHeight(height);
                            post.setWidth(width);
                            post.setTitle(title);
                            post.setDescription(description);
                            post.setUrl(downloadUri.toString());


                            final String titleToLowercase [] = title.toLowerCase().split(" ");
                            final String descriptionToLowercase [] = description.toLowerCase().split(" ");

                            QueryOptions queryOptions = new QueryOptions();
                            queryOptions.setOption_id(pushId);
                            queryOptions.setUser_id(firebaseAuth.getCurrentUser().getUid());
                            queryOptions.setType("post");
                            queryOptions.setOne(Arrays.asList(titleToLowercase));
                            queryOptions.setTwo(Arrays.asList(descriptionToLowercase));
                            queryOptionsReference.document(pushId).set(queryOptions);
                            postsCollection.document(pushId).set(post);

                        }
                    });



                } else {
                    // Handle failures
                    // ...
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(CollectionPostsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                double progression = (100.0 * taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                int progress = (int) progression;
                progressDialog.setMessage("Adding your post " + progress + "%");
                if (progress == 100){
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.dismiss();
                        }
                    }, 100);
                }

            }
        });
    }

    private void collectionVideoPost(){
        if (image != null){
            //current time
            final long timeStamp = new Date().getTime();
            //push id to organise the posts according to time
            final DatabaseReference reference = databaseReference.push();
            final String pushId = reference.getKey();

            storageReference = FirebaseStorage
                    .getInstance().getReference()
                    .child(Constants.COLLECTIONS)
                    .child("collection_videos")
                    .child(pushId);

            if (uri != null){
                UploadTask uploadTask = storageReference.putFile(uri);
                Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }

                        // Continue with the task to get the download URL
                        return storageReference.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            final Uri downloadUri = task.getResult();

                            CollectionReference cl = collectionCollection;
                            cl.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                @Override
                                public void onSuccess(QuerySnapshot documentSnapshots) {
                                    final Post post = new Post();
                                    final int size = documentSnapshots.size();
                                    final int number = size + 1;
                                    final double random = new Random().nextDouble();

                                    postsCollection.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                        @Override
                                        public void onSuccess(QuerySnapshot snapshots) {
                                            final int size = snapshots.size();
                                            final int number = size + 1;
                                            final double random = new Random().nextDouble();

                                            post.setCollection_id(mCollectionId);
                                            post.setType("collection_video_post");
                                            post.setPost_id(pushId);
                                            post.setUser_id(firebaseAuth.getCurrentUser().getUid());
                                            post.setRandom_number(random);
                                            post.setNumber(number);
                                            post.setTime(timeStamp);
                                            post.setDeeplink("");
                                            post.setTitle(title);
                                            post.setDescription(description);
                                            post.setUrl(downloadUri.toString());

                                            final String titleToLowercase [] = title.toLowerCase().split(" ");
                                            final String descriptionToLowercase [] = description.toLowerCase().split(" ");

                                            QueryOptions queryOptions = new QueryOptions();
                                            queryOptions.setOption_id(pushId);
                                            queryOptions.setUser_id(firebaseAuth.getCurrentUser().getUid());
                                            queryOptions.setType("post");
                                            queryOptions.setOne(Arrays.asList(titleToLowercase));
                                            queryOptions.setTwo(Arrays.asList(descriptionToLowercase));
                                            queryOptionsReference.document(pushId).set(queryOptions);

                                            postsCollection.document(pushId).set(post);

                                            //launch the collections activity
                                            Intent intent = new Intent(CollectionPostsActivity.this, CollectionPostsActivity.class);
                                            intent.putExtra(CollectionPostsActivity.COLLECTION_ID, mCollectionId);
                                            intent.putExtra(CollectionPostsActivity.EXTRA_USER_UID, firebaseAuth.getCurrentUser().getUid());
                                            startActivity(intent);
                                            finish();
                                        }
                                    });


                                }
                            });


                        } else {
                            // Handle failures
                            // ...
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(CollectionPostsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

                uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                    }
                });

            }

        }
    }

}

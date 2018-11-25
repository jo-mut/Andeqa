package com.andeqa.andeqa.collections;

import android.app.ProgressDialog;
import android.arch.paging.PagedList;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.R;
import com.andeqa.andeqa.comments.CommentsActivity;
import com.andeqa.andeqa.creation.CreateActivity;
import com.andeqa.andeqa.home.HomeFragment;
import com.andeqa.andeqa.home.PhotoPostViewHolder;
import com.andeqa.andeqa.models.Andeqan;
import com.andeqa.andeqa.models.Collection;
import com.andeqa.andeqa.models.CollectionPost;
import com.andeqa.andeqa.models.Post;
import com.andeqa.andeqa.models.QueryOptions;
import com.andeqa.andeqa.models.Relation;
import com.andeqa.andeqa.post_detail.PostDetailActivity;
import com.andeqa.andeqa.profile.ProfileActivity;
import com.andeqa.andeqa.profile.ProfilePostsActivity;
import com.andeqa.andeqa.settings.CollectionSettingsActivity;
import com.andeqa.andeqa.utils.BottomReachedListener;
import com.andeqa.andeqa.utils.ItemOffsetDecoration;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.firebase.ui.firestore.paging.LoadingState;
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
    @Bind(R.id.toolbar)Toolbar toolbar;

    private static final String TAG = CollectionPostsActivity.class.getSimpleName();
    //firestore reference
    private CollectionReference postsCollectionReference;
    private CollectionReference usersReference;
    private CollectionReference collectionsPosts;
    private CollectionReference commentsReference;
    private DatabaseReference impressionReference;
    private CollectionReference collectionCollection;
    private CollectionReference collectionsRelations;
    private Query postsQuery;
    private StorageReference storageReference;
    private DatabaseReference databaseReference;
    private CollectionReference queryOptionsReference;
    //firebase auth
    private FirebaseAuth firebaseAuth;
    //lists
    private List<DocumentSnapshot> snapshots = new ArrayList<>();
    //layouts
    private ConstraintSet constraintSet;
    private ItemOffsetDecoration itemOffsetDecoration;
    private StaggeredGridLayoutManager layoutManager;
    //strings
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
    private static final String EXTRA_POST_ID = "post id";
    private static final String COLLECTION_ID = "collection id";
    private static final String EXTRA_USER_UID =  "uid";
    private static final String TYPE = "type";
    private static final String POST_HEIGHT = "height";
    private static final String POST_WIDTH = "width";
    private static final String IMAGE = "image";
    private static final String VIDEO = "video";
    private static final String TITLE = "title";
    private static final String DESCRIPTION = "description";
    //boolean
    private boolean processFollow = false;
    private ProgressDialog progressDialog;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection_posts);
        ButterKnife.bind(this);
        //initialize click listener
        addToCollectionRelativeLayout.setOnClickListener(this);
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
        //follow collections
        followCollection();
        //find if the current user is following the collections
        findIfUserIsFollowing();
        //get the number of users following the collection
        getCountOfCollectionFollowers();
        //get the amount of post in a collections
        getCountOfCollectionsPosts();
        //set up the collections  posts
        setUpAdapter();


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

    private void getIntents(){
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

    }

    private void initReferences(){
        //firestore
        firebaseAuth = FirebaseAuth.getInstance();
        //firestore references
        collectionsRelations = FirebaseFirestore.getInstance().collection(Constants.COLLECTION_RELATIONS);
        collectionCollection = FirebaseFirestore.getInstance().collection(Constants.COLLECTIONS);
        postsCollectionReference = FirebaseFirestore.getInstance().collection(Constants.POSTS);
        postsQuery = postsCollectionReference.orderBy("time", Query.Direction.DESCENDING);
        databaseReference = FirebaseDatabase.getInstance().getReference(Constants.RANDOM_PUSH_ID);
        queryOptionsReference = FirebaseFirestore.getInstance().collection(Constants.QUERY_OPTIONS);
        usersReference = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);
        //firebase
        //document reference
        commentsReference  = FirebaseFirestore.getInstance().collection(Constants.COMMENTS);
        //firebase database references
        impressionReference = FirebaseDatabase.getInstance().getReference(Constants.VIEWS);
        impressionReference.keepSynced(true);
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
                    postsCollectionReference.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
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
                            postsCollectionReference.document(pushId).set(post);

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

                                    postsCollectionReference.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
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
                                            postsCollectionReference.document(pushId).set(post);

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


    private void setUpAdapter() {
        postsQuery.whereEqualTo("collection_id", mCollectionId);
        PagedList.Config config = new PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPrefetchDistance(10)
                .setPageSize(20)
                .build();

        FirestorePagingOptions<Post> options = new FirestorePagingOptions.Builder<Post>()
                .setLifecycleOwner(this)
                .setQuery(postsQuery, config, Post.class)
                .build();

        FirestorePagingAdapter<Post, PhotoPostViewHolder> pagingAdapter
                = new FirestorePagingAdapter<Post, PhotoPostViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final PhotoPostViewHolder holder, int position, @NonNull Post model) {
                final Post post = model;
                final String postId = post.getPost_id();
                final String uid = post.getUser_id();
                final String collectionId = post.getCollection_id();
                final String type = post.getType();

                if (post.getHeight() != null && post.getWidth() != null){
                    final float width = (float) Integer.parseInt(post.getWidth());
                    final float height = (float) Integer.parseInt(post.getHeight());
                    float ratio = height/width;

                    constraintSet = new ConstraintSet();
                    constraintSet.clone(holder.postConstraintLayout);
                    constraintSet.setDimensionRatio(holder.postImageView.getId(), "H," + ratio);
                    holder.postImageView.setImageResource(R.drawable.post_placeholder);
                    constraintSet.applyTo(holder.postConstraintLayout);

                }else {
                    constraintSet = new ConstraintSet();
                    constraintSet.clone(holder.postConstraintLayout);
                    constraintSet.setDimensionRatio(holder.postImageView.getId(), "H," + 1);
                    holder.postImageView.setImageResource(R.drawable.post_placeholder);
                    constraintSet.applyTo(holder.postConstraintLayout);

                }


                if (post.getUrl() == null){
                    //firebase firestore references
                    if (type.equals("single")|| type.equals("single_image_post")){
                        collectionsPosts = FirebaseFirestore.getInstance().collection(Constants.COLLECTIONS_OF_POSTS)
                                .document("singles").collection(collectionId);
                    }else{
                        collectionsPosts = FirebaseFirestore.getInstance().collection(Constants.COLLECTIONS_OF_POSTS)
                                .document("collections").collection(collectionId);
                    }
                    collectionsPosts.document(postId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                            if (e != null) {
                                Log.w(TAG, "Listen error", e);
                                return;
                            }

                            if (documentSnapshot.exists()){
                                final CollectionPost collectionPost = documentSnapshot.toObject(CollectionPost.class);
                                //set the image on the image view
                                Glide.with(getApplicationContext())
                                        .load(collectionPost.getImage())
                                        .apply(new RequestOptions()
                                                .placeholder(R.drawable.post_placeholder)
                                                .diskCacheStrategy(DiskCacheStrategy.DATA))
                                        .into(holder.postImageView);

                                if (!TextUtils.isEmpty(collectionPost.getTitle())){
                                    holder.captionLinearLayout.setVisibility(View.VISIBLE);
                                    holder.titleTextView.setText(collectionPost.getTitle());
                                    holder.titleRelativeLayout.setVisibility(View.VISIBLE);
                                }else {
                                    holder.titleRelativeLayout.setVisibility(View.GONE);
                                }

                                if (!TextUtils.isEmpty(collectionPost.getDescription())){
                                    //prevent collection note from overlapping other layouts
                                    final String [] strings = collectionPost.getDescription().split("");
                                    final int size = strings.length;
                                    if (size <= 50){
                                        holder.captionLinearLayout.setVisibility(View.VISIBLE);
                                        holder.descriptionRelativeLayout.setVisibility(View.VISIBLE);
                                        holder.descriptionTextView.setText(collectionPost.getDescription());
                                    }else{
                                        holder.captionLinearLayout.setVisibility(View.VISIBLE);
                                        holder.descriptionRelativeLayout.setVisibility(View.VISIBLE);
                                        final String boldMore = "...";
                                        String normalText = collectionPost.getDescription().substring(0, 49);
                                        holder.descriptionTextView.setText(normalText + boldMore);
                                    }
                                }else {
                                    holder.captionLinearLayout.setVisibility(View.GONE);
                                }
                            }else {
                                //post does not exist
                            }
                        }
                    });
                }else {
                    Glide.with(getApplicationContext())
                            .load(post.getUrl())
                            .apply(new RequestOptions()
                                    .placeholder(R.drawable.post_placeholder)
                                    .diskCacheStrategy(DiskCacheStrategy.DATA))
                            .into(holder.postImageView);

                    if (!TextUtils.isEmpty(post.getTitle())){
                        holder.captionLinearLayout.setVisibility(View.VISIBLE);
                        holder.titleTextView.setText(post.getTitle());
                        holder.titleRelativeLayout.setVisibility(View.VISIBLE);
                    }else {
                        holder.titleRelativeLayout.setVisibility(View.GONE);
                    }

                    if (!TextUtils.isEmpty(post.getDescription())){
                        //prevent collection note from overlapping other layouts
                        final String [] strings = post.getDescription().split("");
                        final int size = strings.length;
                        if (size <= 50){
                            holder.captionLinearLayout.setVisibility(View.VISIBLE);
                            holder.descriptionRelativeLayout.setVisibility(View.VISIBLE);
                            holder.descriptionTextView.setText(post.getDescription());
                        }else{
                            holder. captionLinearLayout.setVisibility(View.VISIBLE);
                            holder.descriptionRelativeLayout.setVisibility(View.VISIBLE);
                            final String boldMore = "...";
                            String normalText = post.getDescription().substring(0, 49);
                            holder.descriptionTextView.setText(normalText + boldMore);
                        }
                    }else {
                        holder.captionLinearLayout.setVisibility(View.GONE);
                    }
                }


                if (post.getWidth() != null && post.getHeight() != null){
                    holder.postImageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent =  new Intent(CollectionPostsActivity.this, PostDetailActivity.class);
                            intent.putExtra(CollectionPostsActivity.EXTRA_POST_ID, postId);
                            intent.putExtra(CollectionPostsActivity.COLLECTION_ID, collectionId);
                            intent.putExtra(CollectionPostsActivity.EXTRA_USER_UID, uid);
                            intent.putExtra(CollectionPostsActivity.TYPE, type);
                            intent.putExtra(CollectionPostsActivity.POST_HEIGHT, post.getHeight());
                            intent.putExtra(CollectionPostsActivity.POST_WIDTH, post.getWidth());
                            startActivity(intent);
                        }
                    });
                }else {
                    holder.postImageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent =  new Intent(CollectionPostsActivity.this, PostDetailActivity.class);
                            intent.putExtra(CollectionPostsActivity.EXTRA_POST_ID, postId);
                            intent.putExtra(CollectionPostsActivity.COLLECTION_ID, collectionId);
                            intent.putExtra(CollectionPostsActivity.EXTRA_USER_UID, uid);
                            intent.putExtra(CollectionPostsActivity.TYPE, type);
                            startActivity(intent);
                        }
                    });

                }

                holder.profileImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(CollectionPostsActivity.this, ProfileActivity.class);
                        intent.putExtra(CollectionPostsActivity.EXTRA_USER_UID, uid);
                        startActivity(intent);
                    }
                });

                holder.commentsImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(CollectionPostsActivity.this, CommentsActivity.class);
                        intent.putExtra(CollectionPostsActivity.EXTRA_POST_ID, postId);
                        startActivity(intent);
                    }
                });

                postsCollectionReference.document(collectionId)
                        .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                            @Override
                            public void onEvent(@javax.annotation.Nullable DocumentSnapshot documentSnapshot,
                                                @javax.annotation.Nullable FirebaseFirestoreException e) {
                                if (e != null) {
                                    Log.w(TAG, "Listen error", e);
                                    return;
                                }

                                if (documentSnapshot.exists()){
                                    Collection collection = documentSnapshot.toObject(Collection.class);
                                    final String name = collection.getName();
                                    final String creatorUid = collection.getUser_id();
                                    holder.collectionNameTextView.setText("@" + name);
                                    holder.collectionNameTextView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Intent intent = new Intent(CollectionPostsActivity.this, CollectionPostsActivity.class);
                                            intent.putExtra(CollectionPostsActivity.COLLECTION_ID, collectionId);
                                            intent.putExtra(CollectionPostsActivity.EXTRA_USER_UID, creatorUid);
                                            startActivity(intent);
                                        }
                                    });
                                }else {
                                    holder.collectionNameTextView.setText("");
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

                        if (documentSnapshot.exists()){
                            final Andeqan andeqan = documentSnapshot.toObject(Andeqan.class);
                            holder.usernameTextView.setText(andeqan.getUsername());

                            Glide.with(getApplicationContext())
                                    .load(andeqan.getProfile_image())
                                    .apply(new RequestOptions()
                                            .placeholder(R.drawable.ic_user)
                                            .diskCacheStrategy(DiskCacheStrategy.DATA))
                                    .into(holder.profileImageView);
                        }
                    }
                });

                //get the number of commments in a single
                commentsReference.document("post_ids").collection(postId)
                        .orderBy("comment_id").whereEqualTo("post_id", postId)
                        .addSnapshotListener(new EventListener<QuerySnapshot>() {
                            @Override
                            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                                if (e != null) {
                                    Log.w(TAG, "Listen error", e);
                                    return;
                                }

                                if (!documentSnapshots.isEmpty()){
                                    final int commentsCount = documentSnapshots.size();
                                    holder.commentsCountTextView.setText(commentsCount + "");
                                }else {
                                    holder.commentsCountTextView.setText("0");
                                }
                            }
                        });

            }

            @NonNull
            @Override
            public PhotoPostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.layout_explore_posts, parent, false);
                return new PhotoPostViewHolder(view);
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
                        break;
                    case LOADED:
                        break;
                    case FINISHED:
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
        mCollectionsPostsRecyclerView.setAdapter(pagingAdapter);
        mCollectionsPostsRecyclerView.setHasFixedSize(false);
        layoutManager = new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL);
        itemOffsetDecoration = new ItemOffsetDecoration(this, R.dimen.item_off_set);
        mCollectionsPostsRecyclerView.setLayoutManager(layoutManager);
        ViewCompat.setNestedScrollingEnabled(mCollectionsPostsRecyclerView,false);

    }

    private void showToast(@NonNull String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

}

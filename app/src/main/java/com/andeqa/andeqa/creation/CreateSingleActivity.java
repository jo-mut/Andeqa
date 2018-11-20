package com.andeqa.andeqa.creation;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.R;
import com.andeqa.andeqa.collections.CollectionPostsActivity;
import com.andeqa.andeqa.main.HomeActivity;
import com.andeqa.andeqa.models.Collection;
import com.andeqa.andeqa.models.Post;
import com.andeqa.andeqa.models.QueryOptions;
import com.andeqa.andeqa.post_detail.PostDetailActivity;
import com.andeqa.andeqa.profile.ProfileActivity;
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
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import butterknife.Bind;
import butterknife.ButterKnife;

public class CreateSingleActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String CAMERA_PATH = "camera image";
    private static final String GALLERY_PATH ="gallery image";
    private static final String IMAGE = "image";
    private static final String VIDEO = "video";
    private static final String EXTRA_USER_UID = "uid";
    private static final String CAMERA_VIDEO = "camera video";
    private static final String GALLERY_VIDEO = "gallery video";
    private static final String PHOTO_URI = "photo uri";
    private static final String HEIGHT = "height";
    private static final String EXTRA_POST_ID = "post id";
    private static final String COLLECTION_ID = "collection id";
    private static final String POST_HEIGHT = "height";
    private static final String POST_WIDTH = "width";
    private static final String TYPE = "type";
    private static final String TITLE = "title";
    private static final String DESCRIPTION = "description";
    private String postId;
    private static final String WIDTH = "width";
    private String height;
    private String width;
    private String video;
    private String photoUri;
    private String image;
    private Uri uri;
    private int progress = 0;
    private Handler handler = new Handler();
    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;

    private static final int DEFAULT_TITLE_LENGTH_LIMIT = 100;

    //FIRESTORE
    private FirebaseFirestore firebaseFirestore;
    private CollectionReference postsCollection;
    private CollectionReference postOwnersCollection;
    private DatabaseReference randomReference;
    private StorageReference storageReference;
    private CollectionReference collectionsReference;
    private CollectionReference queryOptionsReference;
    private List<DocumentSnapshot> documentSnapshots = new ArrayList<>();
    private List<String> mSnapshotsIds = new ArrayList<>();
    private ItemOffsetDecoration itemOffsetDecoration;
    private CreateToCollectionsAdapter createToCollectionsAdapter;
    private static final int TOTAL_ITEMS = 20;
    private static final String TAG = CreateSingleActivity.class.getSimpleName();

    @Bind(R.id.postImageView)ImageView mPostImageView;
    @Bind(R.id.titleEditText)EditText mTitleEditText;
    @Bind(R.id.descriptionEditText)EditText mDescriptionEditText;
    @Bind(R.id.postPostImageView)ImageView mPostPostImageView;
    @Bind(R.id.descriptionCountTextView)TextView mDescriptionCountTextView;
    @Bind(R.id.titleCountTextView)TextView mTitleCountTextView;
    @Bind(R.id.postRelativeLayout)RelativeLayout mPostRelativeLayout;
    @Bind(R.id.progressBar)ProgressBar progressBar;
    @Bind(R.id.progressRelativeLayout)RelativeLayout progressRelativeLayout;
    @Bind(R.id.toolbar)Toolbar toolbar;
    @Bind(R.id.collectionsRecyclerView)RecyclerView mCollectionsRecyclerView;
    @Bind(R.id.createToCollectionsLinearLayout)LinearLayout mCreateToCollectionsLinearLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_single);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_black);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        firebaseAuth = FirebaseAuth.getInstance();
        mPostPostImageView.setOnClickListener(this);

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        Resources res = getResources();
        Drawable drawable = res.getDrawable(R.drawable.progress_drawable);
        progressBar.setProgress(0);
        progressBar.setSecondaryProgress(100);
        progressBar.setMax(100);
        progressBar.setProgressDrawable(drawable);

        //initialize firestore
        firebaseFirestore =  FirebaseFirestore.getInstance();
        //get the reference to posts(collection reference)
        postsCollection = FirebaseFirestore.getInstance().collection(Constants.POSTS);
        postOwnersCollection = firebaseFirestore.collection(Constants.POST_OWNERS);
        randomReference = FirebaseDatabase.getInstance().getReference(Constants.RANDOM_PUSH_ID);
        postOwnersCollection = FirebaseFirestore.getInstance().collection(Constants.POST_WALLET);
        queryOptionsReference = FirebaseFirestore.getInstance().collection(Constants.QUERY_OPTIONS);
        collectionsReference = FirebaseFirestore.getInstance().collection(Constants.COLLECTIONS);

        mTitleEditText.setFilters(new InputFilter[]{new InputFilter
                .LengthFilter(DEFAULT_TITLE_LENGTH_LIMIT)});

        if (getIntent().getExtras() != null){
            if (getIntent().getStringExtra(GALLERY_PATH) != null) {
                mPostRelativeLayout.setVisibility(View.VISIBLE);
                image = getIntent().getStringExtra(GALLERY_PATH);
                uri = Uri.fromFile(new File(image));
                Glide.with(CreateSingleActivity.this)
                        .asBitmap()
                        .load(uri)
                        .into(mPostImageView);
            }
            if(getIntent().getStringExtra(CAMERA_PATH) != null){
                mPostRelativeLayout.setVisibility(View.VISIBLE);
                image = getIntent().getStringExtra(CAMERA_PATH);
                uri = Uri.fromFile(new File(image));
                Glide.with(CreateSingleActivity.this)
                        .asBitmap()
                        .load(uri)
                        .into(mPostImageView);
            }

            if (getIntent().getStringExtra(PHOTO_URI) != null){
                mPostRelativeLayout.setVisibility(View.VISIBLE);
                photoUri = getIntent().getStringExtra(PHOTO_URI);
                uri = Uri.parse(photoUri);
                Glide.with(CreateSingleActivity.this)
                        .asBitmap()
                        .load(uri)
                        .into(mPostImageView);
            }

            if (getIntent().getStringExtra(CAMERA_VIDEO) != null) {
                mPostRelativeLayout.setVisibility(View.VISIBLE);
                video = getIntent().getStringExtra(CAMERA_VIDEO);
                uri = Uri.parse(video);
                Glide.with(CreateSingleActivity.this)
                        .asBitmap()
                        .load(uri)
                        .into(mPostImageView);
            }

            if (getIntent().getStringExtra(GALLERY_VIDEO) != null){
                mPostRelativeLayout.setVisibility(View.VISIBLE);
                video = getIntent().getStringExtra(CAMERA_VIDEO);
                uri = Uri.parse(video);
                Glide.with(CreateSingleActivity.this)
                        .asBitmap()
                        .load(uri)
                        .into(mPostImageView);
            }

            if (getIntent().getStringExtra(HEIGHT)!=null){
                width = getIntent().getStringExtra(WIDTH);
                height = getIntent().getStringExtra(HEIGHT);
            }

            if (getIntent().getStringExtra(EXTRA_POST_ID) != null){
                postId = getIntent().getStringExtra(EXTRA_POST_ID);
            }
        }

        textWatchers();
        uploadingToFirebaseDialog();
    }


    @Override
    protected void onPause(){
        super.onPause();
        if(isFinishing()){
            Picasso.with(this).cancelRequest(mPostImageView);
        }
    }

    private void textWatchers(){
        //TITLE TEXT WATCHER
        mTitleEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                int count = DEFAULT_TITLE_LENGTH_LIMIT - mTitleEditText.getText().length();
                mTitleCountTextView.setText(Integer.toString(count));

                if (count < 0){
                }else if (count < 100){
                    mTitleCountTextView.setTextColor(Color.GRAY);
                }else {
                    mTitleCountTextView.setTextColor(Color.BLACK);
                }

            }
        });

    }


    @Override
    protected void onStart() {
        super.onStart();
        if (postId == null){
            loadData();
            mCreateToCollectionsLinearLayout.setVisibility(View.VISIBLE);
            setRecyclerView();
            mCollectionsRecyclerView.addItemDecoration(itemOffsetDecoration);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mCollectionsRecyclerView.removeItemDecoration(itemOffsetDecoration);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void loadData(){
        documentSnapshots.clear();
        setCreateToColections();
    }

    @Override
    public void onResume() {
        super.onResume();

    }


    @Override
    public void onClick(View v){
        if(v == mPostPostImageView){
            progressRelativeLayout.setVisibility(View.VISIBLE);

            if (postId != null){
                if (image != null){
                    addToPost();
                }
            }else {
                if (image != null){
                    imagePost();
                }

                if (video != null){
                    videoPost();
                }
            }

        }

    }


    public void uploadingToFirebaseDialog(){
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Adding your post");
        progressDialog.setCancelable(false);
    }

    private void addToPost(){
        progressBar.setVisibility(View.VISIBLE);
        final Uri uri = Uri.fromFile(new File(image));
        //current time
        final long timeStamp = new Date().getTime();
        //push id to organise the posts according to time
        final DatabaseReference reference = randomReference.push();
        final String pushId = reference.getKey();
        if (uri != null){
            final String title = mTitleEditText.getText().toString();
            final String description = mDescriptionEditText.getText().toString();
            storageReference = FirebaseStorage
                    .getInstance().getReference()
                    .child(Constants.COLLECTIONS)
                    .child(Constants.IMAGES)
                    .child(postId);

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
                                post.setCollection_id(postId);
                                post.setType("video");
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

                                //reset input fields
                                mTitleEditText.setText("");
                                mDescriptionEditText.setText("");
                                mPostImageView.setImageBitmap(null);


                                postsCollection.document(postId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                    @Override
                                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {

                                        if (e != null) {
                                            Log.w(TAG, "Listen error", e);
                                            return;
                                        }

                                        if (documentSnapshot.exists()){
                                            final Post p = documentSnapshot.toObject(Post.class);
                                            if (p.getWidth() != null && p.getHeight() != null){
                                                Intent intent =  new Intent(CreateSingleActivity.this, PostDetailActivity.class);
                                                intent.putExtra(CreateSingleActivity.EXTRA_POST_ID, p.getPost_id());
                                                intent.putExtra(CreateSingleActivity.COLLECTION_ID, p.getCollection_id());
                                                intent.putExtra(CreateSingleActivity.EXTRA_USER_UID, p.getUser_id());
                                                intent.putExtra(CreateSingleActivity.TYPE, p.getType());
                                                intent.putExtra(CreateSingleActivity.POST_HEIGHT, p.getHeight());
                                                intent.putExtra(CreateSingleActivity.POST_WIDTH, p.getWidth());
                                                startActivity(intent);
                                                finish();
                                            }else {
                                                Intent intent =  new Intent(CreateSingleActivity.this, PostDetailActivity.class);
                                                intent.putExtra(CreateSingleActivity.EXTRA_POST_ID, p.getPost_id());
                                                intent.putExtra(CreateSingleActivity.COLLECTION_ID, p.getCollection_id());
                                                intent.putExtra(CreateSingleActivity.EXTRA_USER_UID, p.getUser_id());
                                                intent.putExtra(CreateSingleActivity.TYPE, p.getType());
                                                startActivity(intent);
                                                finish();
                                            }

                                        }

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
                    Toast.makeText(CreateSingleActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

            uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progression = (100.0 * taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                    progress = (int) progression;
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            while (progress < 100){
                                progress += 1;
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        progressBar.setProgress(progress);
                                    }
                                });

                                if (progress == 100){
                                    progressBar.setIndeterminate(true);
                                }

                                try {
                                    Thread.sleep(100);
                                }catch (InterruptedException ex){
                                    ex.printStackTrace();
                                }
                            }
                        }
                    }).start();
                }
            });
        }
    }

    private void imagePost(){
        progressBar.setVisibility(View.VISIBLE);
        //current time
        final long timeStamp = new Date().getTime();
        //this push id is to put all the singles together under the same reference
        final DatabaseReference collectiveRef = randomReference.push();
        final String pushId = collectiveRef.getKey();
        //this push id is to add new single to the same reference
        final DatabaseReference singleRef = randomReference.push();
        final String post_id = singleRef.getKey();

        if (uri != null){
            final String title = mTitleEditText.getText().toString();
            final String description = mDescriptionEditText.getText().toString();
            storageReference = FirebaseStorage
                    .getInstance().getReference()
                    .child(Constants.COLLECTIONS)
                    .child(Constants.IMAGES)
                    .child(post_id);

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
                                Log.d("posts count", snapshots.size() + "");
                                final int size = snapshots.size();
                                final int number = size + 1;
                                final double random = new Random().nextDouble();

                                Post post = new Post();
                                post.setCollection_id(pushId);
                                post.setType("image");
                                post.setPost_id(post_id);
                                post.setUser_id(firebaseAuth.getCurrentUser().getUid());
                                post.setRandom_number(random);
                                post.setNumber(number);
                                post.setDeeplink("");
                                post.setTime(timeStamp);
                                post.setWidth(width);
                                post.setHeight(height);
                                post.setTitle(title);
                                post.setDescription(description);
                                post.setUrl(downloadUri.toString());


                                final String titleToLowercase [] = title.toLowerCase().split(" ");
                                final String descriptionToLowercase [] = description.toLowerCase().split(" ");

                                QueryOptions queryOptions = new QueryOptions();
                                queryOptions.setOption_id(post_id);
                                queryOptions.setUser_id(firebaseAuth.getCurrentUser().getUid());
                                queryOptions.setType("post");
                                queryOptions.setOne(Arrays.asList(titleToLowercase));
                                queryOptions.setTwo(Arrays.asList(descriptionToLowercase));
                                queryOptionsReference.document(post_id).set(queryOptions);

                                postsCollection.document(post_id).set(post);

                                //reset input fields
                                mTitleEditText.setText("");
                                mDescriptionEditText.setText("");
                                mPostImageView.setImageBitmap(null);

                                //launch the collections activity
                                Intent intent = new Intent(CreateSingleActivity.this, HomeActivity.class);
                                intent.putExtra(CreateSingleActivity.EXTRA_USER_UID, ProfileActivity.class);
                                intent.putExtra(CreateSingleActivity.EXTRA_USER_UID, firebaseAuth.getCurrentUser().getUid());
                                startActivity(intent);
                                finish();

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
                    Toast.makeText(CreateSingleActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

            uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    final double progression = (100 * taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                    progress = (int) progression;
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            while (progress < 100){
                                progress += 1;
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        progressBar.setProgress(progress);
                                    }
                                });

                                if (progress == 100){
                                    progressBar.setIndeterminate(true);
                                }


                                try {
                                    Thread.sleep(100);
                                }catch (InterruptedException ex){
                                    ex.printStackTrace();
                                }
                            }
                        }
                    }).start();

                }
            });

        }
    }

    private void videoPost(){
        if (video != null){
            //current time
            final long timeStamp = new Date().getTime();
            //this push id is to put all the singles together under the same reference
            final DatabaseReference collectiveRef = randomReference.push();
            final String pushId = collectiveRef.getKey();
            //this push id is to add new single to the same reference
            final DatabaseReference singleRef = randomReference.push();
            final String post_id = singleRef.getKey();

            storageReference = FirebaseStorage
                    .getInstance().getReference()
                    .child(Constants.COLLECTIONS)
                    .child(Constants.VIDEOS)
                    .child(post_id);

            if (uri != null){
                final String title = mTitleEditText.getText().toString();
                final String description = mDescriptionEditText.getText().toString();
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
                                    post.setCollection_id(pushId);
                                    post.setType("video");
                                    post.setPost_id(post_id);
                                    post.setUser_id(firebaseAuth.getCurrentUser().getUid());
                                    post.setRandom_number(random);
                                    post.setNumber(number);
                                    post.setDeeplink("");
                                    post.setTime(timeStamp);
                                    post.setWidth(width);
                                    post.setHeight(height);
                                    post.setTitle(title);
                                    post.setDescription(description);
                                    post.setUrl(downloadUri.toString());


                                    final String titleToLowercase [] = title.toLowerCase().split(" ");
                                    final String descriptionToLowercase [] = description.toLowerCase().split(" ");

                                    QueryOptions queryOptions = new QueryOptions();
                                    queryOptions.setOption_id(post_id);
                                    queryOptions.setUser_id(firebaseAuth.getCurrentUser().getUid());
                                    queryOptions.setType("post");
                                    queryOptions.setOne(Arrays.asList(titleToLowercase));
                                    queryOptions.setTwo(Arrays.asList(descriptionToLowercase));
                                    queryOptionsReference.document(post_id).set(queryOptions);
//
                                    postsCollection.document(post_id).set(post);

                                    //reset input fields
                                    mTitleEditText.setText("");
                                    mDescriptionEditText.setText("");
                                    mPostImageView.setImageBitmap(null);

                                    //launch the collections activity
                                    Intent intent = new Intent(CreateSingleActivity.this, HomeActivity.class);
                                    intent.putExtra(CreateSingleActivity.EXTRA_USER_UID, ProfileActivity.class);
                                    intent.putExtra(CreateSingleActivity.EXTRA_USER_UID, firebaseAuth.getCurrentUser().getUid());
                                    startActivity(intent);
                                    finish();
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
                        Toast.makeText(CreateSingleActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

                uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        final double progression = (100 * taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                        progress = (int) progression;
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                while (progress < 100){
                                    progress += 1;
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            progressBar.setProgress(progress);
                                        }
                                    });

                                    if (progress == 100){
                                        progressBar.setIndeterminate(true);
                                    }


                                    try {
                                        Thread.sleep(100);
                                    }catch (InterruptedException ex){
                                        ex.printStackTrace();
                                    }
                                }
                            }
                        }).start();

                    }
                });

            }

        }
    }

    private void setRecyclerView(){
        // RecyclerView
        createToCollectionsAdapter = new CreateToCollectionsAdapter(this);
        mCollectionsRecyclerView.setAdapter(createToCollectionsAdapter);
        mCollectionsRecyclerView.setHasFixedSize(false);
        createToCollectionsAdapter.setCreateToCollections(documentSnapshots);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        itemOffsetDecoration = new ItemOffsetDecoration(this, R.dimen.item_off_set);
        mCollectionsRecyclerView.setLayoutManager(layoutManager);
        ViewCompat.setNestedScrollingEnabled(mCollectionsRecyclerView, false);
    }

//    private void setCollectionsToCreateTo(){
//            Query query = collectionsReference.orderBy("collection_id");
//
//            FirestoreRecyclerOptions<Collection> options = new FirestoreRecyclerOptions.Builder<Collection>()
//                    .setQuery(query, Collection.class)
//                    .build();
//
//            firestoreRecyclerAdapter = new FirestoreRecyclerAdapter<Collection, ExploreCollectionViewHolder>(options) {
//                @Override
//                protected void onBindViewHolder(@NonNull ExploreCollectionViewHolder holder, int position, @NonNull Collection model) {
//                    model = getSnapshots().get(position);
//                    final String collectionId = model.getCollection_id();
//                    final String userId = model.getUser_id();
//
//                    firebaseAuth = FirebaseAuth.getInstance();
//                    Glide.with(CreateSingleActivity.this)
//                            .asBitmap()
//                            .load(model.getImage())
//                            .apply(new RequestOptions()
//                                    .placeholder(R.drawable.post_placeholder)
//                                    .diskCacheStrategy(DiskCacheStrategy.DATA))
//                            .into(holder.mCollectionCoverImageView);
//
//                    if (!TextUtils.isEmpty(model.getName())){
//                        holder.mCollectionNameTextView.setText(model.getName());
//                    }else {
//                        holder.mCollectionNameTextView.setVisibility(View.GONE);
//                    }
//
//                    if (!TextUtils.isEmpty(model.getNote())){
//                        holder.mCollectionsNoteTextView.setVisibility(View.VISIBLE);
//                        //prevent collection note from overlapping other layouts
//                        final String [] strings = model.getNote().split("");
//
//                        final int size = strings.length;
//
//                        if (size <= 60){
//                            //setence will not have read more
//                            holder.mCollectionsNoteTextView.setText(model.getNote());
//                        }else {
//                            holder.mCollectionsNoteTextView.setText(model.getNote().substring(0, 59) + "...");
//                        }
//                    }else {
//                        holder.mCollectionsNoteTextView.setVisibility(View.GONE);
//                    }
//
//                    holder.itemView.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            Intent intent = new Intent(CreateSingleActivity.this, CreateCollectionPostActivity.class);
//                            intent.putExtra(CreateSingleActivity.COLLECTION_ID, collectionId);
//                            startActivity(intent);
//                        }
//                    });
//
//                }
//
//                @NonNull
//                @Override
//                public ExploreCollectionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//                    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_collection_create_to,
//                            parent, false);
//                    return new ExploreCollectionViewHolder(view);
//                }
//            };
//
//    }


    public class CreateToCollectionsAdapter extends RecyclerView.Adapter<CreateToCollectionViewHolder> {
        private Context mContext;
        //firebase auth
        private FirebaseAuth firebaseAuth;
        private static final String COLLECTION_ID = "collection id";
        private List<DocumentSnapshot> featuredCollections = new ArrayList<>();
//        private BottomReachedListener bottomReachedListener;

        public CreateToCollectionsAdapter(Context mContext) {
            this.mContext = mContext;
        }

        public void setCreateToCollections(List<DocumentSnapshot> mSnapshots) {
            this.featuredCollections = mSnapshots;
        }

        @Override
        public int getItemCount() {
            return featuredCollections.size();
        }

        public DocumentSnapshot getSnapshot(int index) {
            return featuredCollections.get(index);
        }


        @NonNull
        @Override
        public CreateToCollectionViewHolder onCreateViewHolder(final @NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_collection_create_to,
                    parent, false);
            return new CreateToCollectionViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final @NonNull CreateToCollectionViewHolder holder, int position) {
            final Collection collection = getSnapshot(position).toObject(Collection.class);
            final String collectionId = collection.getCollection_id();
            final String userId = collection.getUser_id();

            firebaseAuth = FirebaseAuth.getInstance();
            Glide.with(mContext.getApplicationContext())
                    .asBitmap()
                    .load(collection.getImage())
                    .apply(new RequestOptions()
                            .placeholder(R.drawable.post_placeholder)
                            .diskCacheStrategy(DiskCacheStrategy.DATA))
                    .into(holder.mCollectionCoverImageView);

            if (!TextUtils.isEmpty(collection.getName())) {
                holder.mCollectionNameTextView.setText(collection.getName());
            } else {
                holder.mCollectionNameTextView.setVisibility(View.GONE);
            }

            if (!TextUtils.isEmpty(collection.getNote())) {
                holder.mCollectionsNoteTextView.setVisibility(View.VISIBLE);
                //prevent collection note from overlapping other layouts
                final String[] strings = collection.getNote().split("");

                final int size = strings.length;

                if (size <= 60) {
                    //setence will not have read more
                    holder.mCollectionsNoteTextView.setText(collection.getNote());
                } else {
                    holder.mCollectionsNoteTextView.setText(collection.getNote().substring(0, 59) + "...");
                }
            } else {
                holder.mCollectionsNoteTextView.setVisibility(View.GONE);
            }

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                   if (image != null){
                       Intent intent = new Intent(mContext, CollectionPostsActivity.class);
                       intent.putExtra(CreateToCollectionsAdapter.COLLECTION_ID, collectionId);
                       intent.putExtra(CreateSingleActivity.EXTRA_USER_UID, firebaseAuth.getCurrentUser().getUid());
                       intent.putExtra(CreateSingleActivity.TITLE, mTitleEditText.getText().toString());
                       intent.putExtra(CreateSingleActivity.DESCRIPTION, mDescriptionEditText.getText().toString());
                       intent.putExtra(CreateSingleActivity.IMAGE, image);
                       intent.putExtra(CreateSingleActivity.POST_HEIGHT, height);
                       intent.putExtra(CreateSingleActivity.POST_WIDTH, width);
                       mContext.startActivity(intent);
                   }

                   if (video != null){
                       Intent intent = new Intent(mContext, CollectionPostsActivity.class);
                       intent.putExtra(CreateToCollectionsAdapter.COLLECTION_ID, collectionId);
                       intent.putExtra(CreateSingleActivity.EXTRA_USER_UID, firebaseAuth.getCurrentUser().getUid());
                       intent.putExtra(CreateSingleActivity.TITLE, mTitleEditText.getText().toString());
                       intent.putExtra(CreateSingleActivity.DESCRIPTION, mDescriptionEditText.getText().toString());
                       intent.putExtra(CreateSingleActivity.VIDEO, video);
                       mContext.startActivity(intent);
                   }
                }
            });
        }
    }

    private void setCreateToColections(){
        collectionsReference.orderBy("time").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (!documentSnapshots.isEmpty()) {
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

        DocumentSnapshot lastVisible = documentSnapshots.get(documentSnapshots.size() - 1);

        //retrieve the first bacth of posts
        Query nextSinglesQuery = collectionsReference.orderBy("time", Query.Direction.DESCENDING)
                .startAfter(lastVisible).limit(TOTAL_ITEMS);

        nextSinglesQuery.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot documentSnapshots) {
                if (!documentSnapshots.isEmpty()){
                    //retrieve the first bacth of posts
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
        documentSnapshots.add(change.getDocument());
        createToCollectionsAdapter.notifyItemInserted(documentSnapshots.size() -1);
        createToCollectionsAdapter.getItemCount();

    }

    protected void onDocumentModified(DocumentChange change) {
        if (change.getOldIndex() == change.getNewIndex()) {
            // Item changed but remained in same position
            documentSnapshots.set(change.getOldIndex(), change.getDocument());
            createToCollectionsAdapter.notifyItemChanged(change.getOldIndex());
        } else {
            // Item changed and changed position
            documentSnapshots.remove(change.getOldIndex());
            documentSnapshots.add(change.getNewIndex(), change.getDocument());
            createToCollectionsAdapter.notifyItemRangeChanged(0, documentSnapshots.size());
        }
    }

    protected void onDocumentRemoved(DocumentChange change) {
        try{
            documentSnapshots.remove(change.getOldIndex());
            createToCollectionsAdapter.notifyItemRemoved(change.getOldIndex());
            createToCollectionsAdapter.notifyItemRangeChanged(0, documentSnapshots.size());
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}

package com.andeqa.andeqa.creation;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.R;
import com.andeqa.andeqa.post_detail.PostDetailActivity;
import com.andeqa.andeqa.models.Post;
import com.andeqa.andeqa.collections.CollectionPostsActivity;
import com.andeqa.andeqa.models.QueryOptions;
import com.andeqa.andeqa.utils.ItemOffsetDecoration;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import butterknife.Bind;
import butterknife.ButterKnife;

public class CreateCollectionPostActivity extends AppCompatActivity implements View.OnClickListener{
    @Bind(R.id.titleEditText)EditText mTitleEditText;
    @Bind(R.id.descriptionEditText)EditText mDescriptionEditText;
    @Bind(R.id.postImageView)ImageView mPostImageView;
    @Bind(R.id.doneImageView)ImageView mDoneImageView;
    @Bind(R.id.descriptionCountTextView)TextView mDescriptionCountTextView;
    @Bind(R.id.titleCountTextView)TextView mTitleCountTextView;
    @Bind(R.id.postRelativeLayout)RelativeLayout mPostRelativeLayout;
    @Bind(R.id.progressBar)ProgressBar progressBar;
    @Bind(R.id.collectionsRecyclerView)RecyclerView mCollectionsRecyclerView;

    private static final String TAG = CreateCollectionPostActivity.class.getSimpleName();
    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;
    private Context mContext;

    private static final String COLLECTION_POST = CreateCollectionPostActivity.class.getSimpleName();
    private static final int DEFAULT_TITLE_LENGTH_LIMIT = 100;
    private static final int IMAGE_GALLERY_REQUEST = 112;
    private static final String GALLERY_PATH ="gallery image";
    private static final String CAMERA_VIDEO = "camera video";
    private static final String COLLECTION_ID = "collection id";
    private static final String EXTRA_USER_UID = "uid";
    private static final String EXTRA_POST_ID = "post id";
    private static final String POST_HEIGHT = "height";
    private static final String POST_WIDTH = "width";
    private static final String TYPE = "type";
    private static final int TOTAL_ITEMS = 20;
    private String height;
    private String width;
    private String collectionId;
    private String postId;
    private String image;
    private String video;
    private int progress = 0;
    private Handler handler = new Handler();
    //FIRESTORE
    private FirebaseFirestore firebaseFirestore;
    private CollectionReference postsCollection;
    private DatabaseReference randomReference;
    private CollectionReference collectionsCollection;
    private StorageReference storageReference;
    private CollectionReference queryOptionsReference;
    private List<DocumentSnapshot> mSnapshots = new ArrayList<>();
    private ItemOffsetDecoration itemOffsetDecoration;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_collection_post);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_black);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        mDoneImageView.setOnClickListener(this);

        Resources res = getResources();
        Drawable drawable = res.getDrawable(R.drawable.progress_drawable);
        progressBar.setProgress(0);
        progressBar.setSecondaryProgress(100);
        progressBar.setMax(100);
        progressBar.setProgressDrawable(drawable);

        firebaseAuth = FirebaseAuth.getInstance();

        if (getIntent().getExtras() != null){
            collectionId = getIntent().getStringExtra(COLLECTION_ID);
            postId = getIntent().getStringExtra(EXTRA_POST_ID);
            image = getIntent().getStringExtra(GALLERY_PATH);
            video = getIntent().getStringExtra(CAMERA_VIDEO);
        }

        //initialize firestore
        firebaseFirestore =  FirebaseFirestore.getInstance();
        //get the reference to posts(collection reference)
        postsCollection = FirebaseFirestore.getInstance().collection(Constants.POSTS);
        randomReference = FirebaseDatabase.getInstance().getReference(Constants.RANDOM_PUSH_ID);
        collectionsCollection = FirebaseFirestore.getInstance().collection(Constants.COLLECTIONS_OF_POSTS);
        queryOptionsReference = FirebaseFirestore.getInstance().collection(Constants.QUERY_OPTIONS);
        collectionsCollection = FirebaseFirestore.getInstance().collection(Constants.COLLECTIONS);

        mTitleEditText.setFilters(new InputFilter[]{new InputFilter
                .LengthFilter(DEFAULT_TITLE_LENGTH_LIMIT)});

        textWatchers();
        loadPostImage();

    }

    private void loadPostImage(){
        if (image != null){
            mPostRelativeLayout.setVisibility(View.VISIBLE);
            mPostImageView.setVisibility(View.VISIBLE);
            Glide.with(this)
                    .asBitmap()
                    .load(new File(image))
                    .into(mPostImageView);
        }

        if (video != null){
            mPostRelativeLayout.setVisibility(View.VISIBLE);
            Glide.with(this)
                    .asBitmap()
                    .load(new File(video))
                    .into(mPostImageView);
        }

        if (getIntent().getStringExtra(POST_HEIGHT)!=null){
            width = getIntent().getStringExtra(POST_WIDTH);
            height = getIntent().getStringExtra(POST_HEIGHT);
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
    protected void onPause(){
        super.onPause();

    }

    @Override
    public void onClick(View v){
        if(v == mDoneImageView){
           if (video != null){
               collectionVideoPost();
           }else if (postId != null){
               addToPost();
           }else {
               collectionImagePost();
           }
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        loadData();
        mCollectionsRecyclerView.addItemDecoration(itemOffsetDecoration);

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
        mSnapshots.clear();

    }

    @Override
    public void onResume() {
        super.onResume();

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
                                post.setType("image");
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
                                                Intent intent =  new Intent(mContext, PostDetailActivity.class);
                                                intent.putExtra(CreateCollectionPostActivity.EXTRA_POST_ID, p.getPost_id());
                                                intent.putExtra(CreateCollectionPostActivity.COLLECTION_ID, p.getCollection_id());
                                                intent.putExtra(CreateCollectionPostActivity.EXTRA_USER_UID, p.getUser_id());
                                                intent.putExtra(CreateCollectionPostActivity.TYPE, p.getType());
                                                intent.putExtra(CreateCollectionPostActivity.POST_HEIGHT, p.getHeight());
                                                intent.putExtra(CreateCollectionPostActivity.POST_WIDTH, p.getWidth());
                                                startActivity(intent);
                                                finish();
                                            }else {
                                                Intent intent =  new Intent(mContext, PostDetailActivity.class);
                                                intent.putExtra(CreateCollectionPostActivity.EXTRA_POST_ID, p.getPost_id());
                                                intent.putExtra(CreateCollectionPostActivity.COLLECTION_ID, p.getCollection_id());
                                                intent.putExtra(CreateCollectionPostActivity.EXTRA_USER_UID, p.getUser_id());
                                                intent.putExtra(CreateCollectionPostActivity.TYPE, p.getType());
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
                    Toast.makeText(CreateCollectionPostActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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

    private void collectionImagePost(){
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
                                post.setCollection_id(collectionId);
                                post.setType("image");
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



                                //launch the collections activity
                                Intent intent = new Intent(CreateCollectionPostActivity.this, CollectionPostsActivity.class);
                                intent.putExtra(CreateCollectionPostActivity.COLLECTION_ID, collectionId);
                                intent.putExtra(CreateCollectionPostActivity.EXTRA_USER_UID, firebaseAuth.getCurrentUser().getUid());
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
                    Toast.makeText(CreateCollectionPostActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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

    private void collectionVideoPost(){
        if (image != null){
            progressBar.setVisibility(View.VISIBLE);
            //get the data from the imageview as bytes
            mPostImageView.setDrawingCacheEnabled(true);
            Bitmap bitmap = ((BitmapDrawable) mPostImageView.getDrawable()).getBitmap();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            final byte[] data = baos.toByteArray();

            //current time
            final long timeStamp = new Date().getTime();
            //push id to organise the posts according to time
            final DatabaseReference reference = randomReference.push();
            final String pushId = reference.getKey();

            storageReference = FirebaseStorage
                    .getInstance().getReference()
                    .child(Constants.COLLECTIONS)
                    .child(Constants.VIDEOS)
                    .child(pushId);

            if (data != null){
                final String title = mTitleEditText.getText().toString();
                final String description = mDescriptionEditText.getText().toString();
                UploadTask uploadTask = storageReference.putBytes(data);
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

                            final Post post = new Post();
                            postsCollection.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                @Override
                                public void onSuccess(QuerySnapshot snapshots) {
                                    final int size = snapshots.size();
                                    final int number = size + 1;
                                    final double random = new Random().nextDouble();

                                    post.setCollection_id(collectionId);
                                    post.setType("video");
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

                                    //reset input fields
                                    mTitleEditText.setText("");
                                    mDescriptionEditText.setText("");
                                    mPostImageView.setImageBitmap(null);



                                    //launch the collections activity
                                    Intent intent = new Intent(CreateCollectionPostActivity.this, CollectionPostsActivity.class);
                                    intent.putExtra(CreateCollectionPostActivity.COLLECTION_ID, collectionId);
                                    intent.putExtra(CreateCollectionPostActivity.EXTRA_USER_UID, firebaseAuth.getCurrentUser().getUid());
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
                        Toast.makeText(CreateCollectionPostActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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
    }


}

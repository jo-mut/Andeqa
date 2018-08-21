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
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.R;
import com.andeqa.andeqa.camera.PicturesActivity;
import com.andeqa.andeqa.models.CollectionPost;
import com.andeqa.andeqa.models.Post;
import com.andeqa.andeqa.collections.CollectionPostsActivity;
import com.andeqa.andeqa.models.VideoPost;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Date;
import java.util.Random;

import butterknife.Bind;
import butterknife.ButterKnife;

public class CreateCollectionPostActivity extends AppCompatActivity implements View.OnClickListener{
    @Bind(R.id.titleEditText)EditText mCingleTitleEditText;
    @Bind(R.id.descriptionEditText)EditText mCingleDescriptionEditText;
    @Bind(R.id.postImageView)ImageView mPostImageView;
    @Bind(R.id.postPostImageView)ImageView mPostPostImageView;
    @Bind(R.id.descriptionCountTextView)TextView mDescriptionCountTextView;
    @Bind(R.id.titleCountTextView)TextView mTitleCountTextView;
    @Bind(R.id.postRelativeLayout)RelativeLayout mPostRelativeLayout;
    @Bind(R.id.addRelativeLayout)RelativeLayout mAddRelativeLayout;
    @Bind(R.id.progressBar)ProgressBar progressBar;
    @Bind(R.id.progressTextView) TextView progressTextView;
    @Bind(R.id.progressRelativeLayout)RelativeLayout progressRelativeLayout;

    private static final String TAG = CreateCollectionPostActivity.class.getSimpleName();
    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;
    private Context mContext;

    private static final String POST_TAG = CreateCollectionPostActivity.class.getSimpleName();
    private static final int DEFAULT_TITLE_LENGTH_LIMIT = 100;
    private static final int IMAGE_GALLERY_REQUEST = 112;
    private static final String GALLERY_PATH ="gallery image";
    private static final String CAMERA_VIDEO = "camera video";
    private static final String COLLECTION_ID = "collection id";
    private static final String EXTRA_USER_UID = "uid";
    private static final String HEIGHT = "height";
    private static final String WIDTH = "width";
    private String height;
    private String width;
    private String collectionId;
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


        mPostPostImageView.setOnClickListener(this);
        mAddRelativeLayout.setOnClickListener(this);

        Resources res = getResources();
        Drawable drawable = res.getDrawable(R.drawable.progress_drawable);
        progressBar.setProgress(0);
        progressBar.setSecondaryProgress(100);
        progressBar.setMax(100);
        progressBar.setProgressDrawable(drawable);

        firebaseAuth = FirebaseAuth.getInstance();

        collectionId = getIntent().getStringExtra(COLLECTION_ID);
        image = getIntent().getStringExtra(GALLERY_PATH);
        video = getIntent().getStringExtra(CAMERA_VIDEO);

        //initialize firestore
        firebaseFirestore =  FirebaseFirestore.getInstance();
        //get the reference to posts(collection reference)
        postsCollection = FirebaseFirestore.getInstance().collection(Constants.POSTS);
        randomReference = FirebaseDatabase.getInstance().getReference(Constants.RANDOM_PUSH_ID);
        collectionsCollection = FirebaseFirestore.getInstance().collection(Constants.COLLECTIONS_POSTS);

        mCingleTitleEditText.setFilters(new InputFilter[]{new InputFilter
                .LengthFilter(DEFAULT_TITLE_LENGTH_LIMIT)});

        textWatchers();
        uploadingToFirebaseDialog();
        loadPostImage();

    }

    private void loadPostImage(){
        if (image != null){
            mAddRelativeLayout.setVisibility(View.GONE);
            mPostRelativeLayout.setVisibility(View.VISIBLE);
            mPostImageView.setVisibility(View.VISIBLE);
            Glide.with(this)
                    .asBitmap()
                    .load(new File(image))
                    .into(mPostImageView);
        }

        if (video != null){
            mAddRelativeLayout.setVisibility(View.GONE);
            mPostRelativeLayout.setVisibility(View.VISIBLE);
            Glide.with(this)
                    .asBitmap()
                    .load(new File(video))
                    .into(mPostImageView);
        }

        if (getIntent().getStringExtra(HEIGHT)!=null){
            width = getIntent().getStringExtra(WIDTH);
            height = getIntent().getStringExtra(HEIGHT);
        }
    }

    private void textWatchers(){
        //TITLE TEXT WATCHER
        mCingleTitleEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                int count = DEFAULT_TITLE_LENGTH_LIMIT - editable.length();
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
        if(v == mPostPostImageView){
           if (video != null){
               videoPost();
           }else {
               imagePost();
           }
        }

        if (v == mAddRelativeLayout){
            Intent intent = new Intent(CreateCollectionPostActivity.this, PicturesActivity.class);
            intent.putExtra(CreateCollectionPostActivity.POST_TAG, CreateCollectionPostActivity.class.getSimpleName());
            intent.putExtra(CreateCollectionPostActivity.COLLECTION_ID, collectionId);
            startActivity(intent);
            finish();

        }

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    public void uploadingToFirebaseDialog(){
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Adding your post");
        progressDialog.setCancelable(false);
    }


    private void imagePost(){
        progressRelativeLayout.setVisibility(View.VISIBLE);
        final Uri uri = Uri.fromFile(new File(image));
        //current time
        final long timeStamp = new Date().getTime();
        //push id to organise the posts according to time
        final DatabaseReference reference = randomReference.push();
        final String pushId = reference.getKey();

        if (uri != null){
            storageReference = FirebaseStorage
                    .getInstance().getReference()
                    .child(Constants.USER_COLLECTIONS)
                    .child("collection_images")
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

                        CollectionReference cl = collectionsCollection;
                        cl.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot documentSnapshots) {
                                final CollectionPost collectionPost = new CollectionPost();
                                final int size = documentSnapshots.size();
                                final int number = size + 1;
                                final double random = new Random().nextDouble();

                                Task<ShortDynamicLink> shortLinkTask = FirebaseDynamicLinks.getInstance().createDynamicLink()
                                        .setLink(Uri.parse("https://andeqa.com/"))
                                        .setDynamicLinkDomain("andeqa.page.link")
                                        .setSocialMetaTagParameters(new DynamicLink.SocialMetaTagParameters.Builder()
                                                .setTitle(collectionPost.getTitle())
                                                .setDescription(collectionPost.getDescription())
                                                .setImageUrl(downloadUri)
                                                .build())
                                        // Set parameters
                                        .buildShortDynamicLink()
                                        .addOnCompleteListener(CreateCollectionPostActivity.this, new OnCompleteListener<ShortDynamicLink>() {
                                            @Override
                                            public void onComplete(@NonNull Task<ShortDynamicLink> task) {
                                                if (task.isSuccessful()) {
                                                    // Short link created
                                                    final Uri shortLink = task.getResult().getShortLink();
                                                    final Uri flowchartLink = task.getResult().getPreviewLink();
                                                    //record all the collectionPost data
                                                    collectionPost.setNumber(number);
                                                    collectionPost.setRandom_number(random);
                                                    collectionPost.setTime(timeStamp);
                                                    collectionPost.setUser_id(firebaseAuth.getCurrentUser().getUid());
                                                    collectionPost.setTitle(mCingleTitleEditText.getText().toString());
                                                    collectionPost.setDescription(mCingleDescriptionEditText.getText().toString());
                                                    collectionPost.setImage(downloadUri.toString());
                                                    collectionPost.setPost_id(pushId);
                                                    collectionPost.setUser_id(firebaseAuth.getCurrentUser().getUid());
                                                    collectionPost.setType("collection_post");
                                                    collectionPost.setCollection_id(collectionId);
                                                    collectionPost.setHeight(height);
                                                    collectionPost.setWidth(width);
                                                    collectionPost.setDeeplink(shortLink.toString());
                                                    collectionsCollection.document("collections").collection(collectionId)
                                                            .document(pushId).set(collectionPost)
                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {
                                                                    postsCollection.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                                        @Override
                                                                        public void onSuccess(QuerySnapshot snapshots) {
                                                                            final int size = snapshots.size();
                                                                            final int number = size + 1;
                                                                            final double random = new Random().nextDouble();

                                                                            Post post = new Post();
                                                                            post.setCollection_id(collectionId);
                                                                            post.setType("collection_image_post");
                                                                            post.setPost_id(pushId);
                                                                            post.setUser_id(firebaseAuth.getCurrentUser().getUid());
                                                                            post.setRandom_number(random);
                                                                            post.setNumber(number);
                                                                            post.setTime(timeStamp);
                                                                            post.setDeeplink(shortLink.toString());
                                                                            post.setHeight(height);
                                                                            post.setWidth(width);

                                                                            postsCollection.document(pushId)
                                                                                    .set(post)
                                                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                        @Override
                                                                                        public void onSuccess(Void aVoid) {

                                                                                            //launch the collections activity
                                                                                            Intent intent = new Intent(CreateCollectionPostActivity.this, CollectionPostsActivity.class);
                                                                                            intent.putExtra(CreateCollectionPostActivity.COLLECTION_ID, collectionId);
                                                                                            intent.putExtra(CreateCollectionPostActivity.EXTRA_USER_UID, firebaseAuth.getCurrentUser().getUid());
                                                                                            startActivity(intent);
                                                                                            finish();
                                                                                        }
                                                                                    });
                                                                        }
                                                                    });
                                                                }
                                                            });
                                                } else {
                                                    // Error
                                                    // ...
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
                                        progressTextView.setText(progress + " %");
                                    }
                                });
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
        if (image != null){
            progressRelativeLayout.setVisibility(View.VISIBLE);
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
                    .child(Constants.USER_COLLECTIONS)
                    .child("collections_videos")
                    .child(pushId);

            if (data != null){
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

                            CollectionReference cl = collectionsCollection;
                            cl.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                @Override
                                public void onSuccess(QuerySnapshot documentSnapshots) {
                                    final VideoPost videoPost = new VideoPost();
                                    final int size = documentSnapshots.size();
                                    final int number = size + 1;
                                    final double random = new Random().nextDouble();

                                    Task<ShortDynamicLink> shortLinkTask = FirebaseDynamicLinks.getInstance().createDynamicLink()
                                            .setLink(Uri.parse("https://andeqa.com/"))
                                            .setDynamicLinkDomain("andeqa.page.link")
                                            .setSocialMetaTagParameters(new DynamicLink.SocialMetaTagParameters.Builder()
                                                    .setTitle(videoPost.getTitle())
                                                    .setDescription(videoPost.getDescription())
                                                    .setImageUrl(downloadUri)
                                                    .build())
                                            // Set parameters
                                            .buildShortDynamicLink()
                                            .addOnCompleteListener(CreateCollectionPostActivity.this, new OnCompleteListener<ShortDynamicLink>() {
                                                @Override
                                                public void onComplete(@NonNull Task<ShortDynamicLink> task) {
                                                    if (task.isSuccessful()) {
                                                        // Short link created
                                                        final Uri shortLink = task.getResult().getShortLink();
                                                        final Uri flowchartLink = task.getResult().getPreviewLink();
                                                        //record all the collectionPost data
                                                        videoPost.setNumber(number);
                                                        videoPost.setRandom_number(random);
                                                        videoPost.setTime(timeStamp);
                                                        videoPost.setUser_id(firebaseAuth.getCurrentUser().getUid());
                                                        videoPost.setTitle(mCingleTitleEditText.getText().toString());
                                                        videoPost.setDescription(mCingleDescriptionEditText.getText().toString());
                                                        videoPost.setVideo(downloadUri.toString());
                                                        videoPost.setPost_id(pushId);
                                                        videoPost.setUser_id(firebaseAuth.getCurrentUser().getUid());
                                                        videoPost.setType("collection_post");
                                                        videoPost.setCollection_id(collectionId);
                                                        videoPost.setDeeplink(shortLink.toString());
                                                        collectionsCollection.document("collections").collection(collectionId)
                                                                .document(pushId).set(videoPost)
                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void aVoid) {
                                                                        postsCollection.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                                            @Override
                                                                            public void onSuccess(QuerySnapshot snapshots) {
                                                                                final int size = snapshots.size();
                                                                                final int number = size + 1;
                                                                                final double random = new Random().nextDouble();

                                                                                Post post = new Post();
                                                                                post.setCollection_id(collectionId);
                                                                                post.setType("collection_video_post");
                                                                                post.setPost_id(pushId);
                                                                                post.setUser_id(firebaseAuth.getCurrentUser().getUid());
                                                                                post.setRandom_number(random);
                                                                                post.setNumber(number);
                                                                                post.setTime(timeStamp);
                                                                                post.setDeeplink(shortLink.toString());

                                                                                postsCollection.document(pushId)
                                                                                        .set(post)
                                                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                            @Override
                                                                                            public void onSuccess(Void aVoid) {

                                                                                                //launch the collections activity
                                                                                                Intent intent = new Intent(CreateCollectionPostActivity.this, CollectionPostsActivity.class);
                                                                                                intent.putExtra(CreateCollectionPostActivity.COLLECTION_ID, collectionId);
                                                                                                intent.putExtra(CreateCollectionPostActivity.EXTRA_USER_UID, firebaseAuth.getCurrentUser().getUid());
                                                                                                startActivity(intent);
                                                                                                finish();
                                                                                            }
                                                                                        });
                                                                            }
                                                                        });
                                                                    }
                                                                });
                                                    } else {
                                                        // Error
                                                        // ...
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
                                            progressTextView.setText(progress + " %");
                                        }
                                    });
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

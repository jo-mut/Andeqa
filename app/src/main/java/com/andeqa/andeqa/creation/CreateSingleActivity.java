package com.andeqa.andeqa.creation;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
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

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.R;
import com.andeqa.andeqa.collections.CollectionPostsActivity;
import com.andeqa.andeqa.main.HomeActivity;
import com.andeqa.andeqa.models.CollectionPost;
import com.andeqa.andeqa.models.Post;
import com.andeqa.andeqa.profile.ProfileActivity;
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
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.Date;
import java.util.Random;

import butterknife.Bind;
import butterknife.ButterKnife;

public class CreateSingleActivity extends AppCompatActivity implements View.OnClickListener {
    private String image;
    private static final String CAMERA_PATH = "camera image";
    private static final String GALLERY_PATH ="gallery image";

    private static final String KEY_IMAGE = "image";
    private static final String EXTRA_USER_UID = "uid";
    private static final String CAMERA_VIDEO = "camera video";
    private static final String GALLERY_VIDEO = "gallery video";
    private static final String HEIGHT = "height";
    private static final String WIDTH = "width";
    private String height;
    private String width;
    private String video;
    private Uri photoUri;
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
    private CollectionReference collectionsCollection;


    @Bind(R.id.postImageView)ImageView mPostImageView;
    @Bind(R.id.titleEditText)EditText mTitleEditText;
    @Bind(R.id.descriptionEditText)EditText mCingleDescriptionEditText;
    @Bind(R.id.postPostImageView)ImageView mPostPostImageView;
    @Bind(R.id.descriptionCountTextView)TextView mDescriptionCountTextView;
    @Bind(R.id.titleCountTextView)TextView mTitleCountTextView;
    @Bind(R.id.postRelativeLayout)RelativeLayout mPostRelativeLayout;
    @Bind(R.id.progressBar)ProgressBar progressBar;
    @Bind(R.id.progressTextView) TextView progressTextView;
    @Bind(R.id.toolbar)Toolbar toolbar;


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

        Resources res = getResources();
        Drawable drawable = res.getDrawable(R.drawable.progress_drawable);
        progressBar.setProgress(0);
        progressBar.setSecondaryProgress(100);
        progressBar.setMax(100);
        progressBar.setProgressDrawable(drawable);

        init();


        if (firebaseAuth.getCurrentUser() != null){
            //initialize firestore
            firebaseFirestore =  FirebaseFirestore.getInstance();
            //get the reference to posts(collection reference)
            postsCollection = FirebaseFirestore.getInstance().collection(Constants.POSTS);
            postOwnersCollection = firebaseFirestore.collection(Constants.POST_OWNERS);
            randomReference = FirebaseDatabase.getInstance().getReference(Constants.RANDOM_PUSH_ID);
            collectionsCollection = FirebaseFirestore.getInstance().collection(Constants.COLLECTIONS_POSTS);
            postOwnersCollection = FirebaseFirestore.getInstance().collection(Constants.POST_WALLET);

            mTitleEditText.setFilters(new InputFilter[]{new InputFilter
                    .LengthFilter(DEFAULT_TITLE_LENGTH_LIMIT)});

            textWatchers();
            uploadingToFirebaseDialog();

        }
    }


    @Override
    protected void onPause(){
        super.onPause();
        if(isFinishing()){
            Picasso.with(this).cancelRequest(mPostImageView);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putString(KEY_IMAGE, image);
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




    private void init() {

        if (getIntent().getExtras() != null){
            if (getIntent().getStringExtra(GALLERY_PATH) != null) {
                mPostRelativeLayout.setVisibility(View.VISIBLE);
                image = getIntent().getStringExtra(GALLERY_PATH);
            }
             if(getIntent().getStringExtra(CAMERA_PATH) != null){
                 mPostRelativeLayout.setVisibility(View.VISIBLE);
                 image = getIntent().getStringExtra(CAMERA_PATH);
            }

            if (getIntent().getStringExtra(CAMERA_VIDEO) != null) {
                mPostRelativeLayout.setVisibility(View.VISIBLE);
                video = getIntent().getStringExtra(CAMERA_VIDEO);
            }

            if (getIntent().getStringExtra(GALLERY_VIDEO) != null){
                mPostRelativeLayout.setVisibility(View.VISIBLE);
                video = getIntent().getStringExtra(CAMERA_VIDEO);
            }

            if (getIntent().getStringExtra(HEIGHT)!=null){
                width = getIntent().getStringExtra(WIDTH);
                height = getIntent().getStringExtra(HEIGHT);
            }
        }

        if (image != null){
            Glide.with(CreateSingleActivity.this)
                    .asBitmap()
                    .load(new File(image))
                    .into(mPostImageView);
        }

        if (video != null){
            Glide.with(CreateSingleActivity.this)
                    .asBitmap()
                    .load(new File(video))
                    .into(mPostImageView);
        }

    }


    @Override
    public void onClick(View v){
        if(v == mPostPostImageView){
            if (image != null){
                imagePost();
            }else if (video != null){
                videoPost();
            }else {
                imagePost();
            }
        }

    }


    public void uploadingToFirebaseDialog(){
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Adding your post");
        progressDialog.setCancelable(false);
    }

    private void imagePost(){
        progressBar.setVisibility(View.VISIBLE);
        final Uri uri = Uri.fromFile(new File(image));
        //current time
        final long timeStamp = new Date().getTime();
        //this push id is to put all the singles together under the same reference
        final DatabaseReference collectiveRef = randomReference.push();
        final String pushId = collectiveRef.getKey();
        //this push id is to add new single to the same reference
        final DatabaseReference singleRef = randomReference.push();
        final String post_id = singleRef.getKey();

        if (uri != null){
            storageReference = FirebaseStorage
                    .getInstance().getReference()
                    .child(Constants.USER_COLLECTIONS)
                    .child("single_images")
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
                        Task<ShortDynamicLink> shortLinkTask = FirebaseDynamicLinks.getInstance().createDynamicLink()
                                .setLink(Uri.parse("https://andeqa.com/"))
                                .setDynamicLinkDomain("andeqa.page.link")
                                .setSocialMetaTagParameters(new DynamicLink.SocialMetaTagParameters.Builder()
                                        .setTitle(post.getTitle())
                                        .setDescription(post.getDescription())
                                        .setImageUrl(downloadUri)
                                        .build())
                                // Set parameters
                                .buildShortDynamicLink()
                                .addOnCompleteListener(CreateSingleActivity.this, new OnCompleteListener<ShortDynamicLink>() {
                                    @Override
                                    public void onComplete(@NonNull Task<ShortDynamicLink> task) {
                                        if (task.isSuccessful()) {
                                            // Short link created
                                            final Uri shortLink = task.getResult().getShortLink();
                                            final Uri flowchartLink = task.getResult().getPreviewLink();

                                            postsCollection.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                @Override
                                                public void onSuccess(QuerySnapshot snapshots) {
                                                    final int size = snapshots.size();
                                                    final int number = size + 1;
                                                    final double random = new Random().nextDouble();

                                                    postsCollection.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                        @Override
                                                        public void onSuccess(QuerySnapshot snapshots) {
                                                            final int size = snapshots.size();
                                                            final int number = size + 1;
                                                            final double random = new Random().nextDouble();

                                                            Post post = new Post();
                                                            post.setCollection_id(pushId);
                                                            post.setType("single_image_post");
                                                            post.setPost_id(post_id);
                                                            post.setUser_id(firebaseAuth.getCurrentUser().getUid());
                                                            post.setRandom_number(random);
                                                            post.setNumber(number);
                                                            post.setDeeplink(shortLink.toString());
                                                            post.setTime(timeStamp);
                                                            post.setWidth(width);
                                                            post.setHeight(height);
                                                            post.setTitle(mTitleEditText.getText().toString());
                                                            post.setDescription(mCingleDescriptionEditText.getText().toString());
                                                            post.setUrl(downloadUri.toString());



                                                            postsCollection.document(post_id).set(post)
                                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                        @Override
                                                                        public void onSuccess(Void aVoid) {
                                                                            //reset input fields
                                                                            mTitleEditText.setText("");
                                                                            mCingleDescriptionEditText.setText("");
                                                                            mPostImageView.setImageBitmap(null);

                                                                            //launch the collections activity
                                                                            Intent intent = new Intent(CreateSingleActivity.this, HomeActivity.class);
                                                                            intent.putExtra(CreateSingleActivity.EXTRA_USER_UID, ProfileActivity.class);
                                                                            intent.putExtra(CreateSingleActivity.EXTRA_USER_UID, firebaseAuth.getCurrentUser().getUid());
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
        if (video != null){
            final Uri videoUri = Uri.fromFile(new File(video));
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
                    .child(Constants.USER_COLLECTIONS)
                    .child("single_videos")
                    .child(post_id);

            if (videoUri != null){

                UploadTask uploadTask = storageReference.putFile(videoUri);
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
                            Task<ShortDynamicLink> shortLinkTask = FirebaseDynamicLinks.getInstance().createDynamicLink()
                                    .setLink(Uri.parse("https://andeqa.com/"))
                                    .setDynamicLinkDomain("andeqa.page.link")
                                    .setSocialMetaTagParameters(new DynamicLink.SocialMetaTagParameters.Builder()
                                            .setTitle(post.getTitle())
                                            .setDescription(post.getDescription())
                                            .setImageUrl(downloadUri)
                                            .build())
                                    // Set parameters
                                    .buildShortDynamicLink()
                                    .addOnCompleteListener(CreateSingleActivity.this, new OnCompleteListener<ShortDynamicLink>() {
                                        @Override
                                        public void onComplete(@NonNull Task<ShortDynamicLink> task) {
                                            if (task.isSuccessful()) {
                                                // Short link created
                                                final Uri shortLink = task.getResult().getShortLink();
                                                final Uri flowchartLink = task.getResult().getPreviewLink();

                                                postsCollection.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                    @Override
                                                    public void onSuccess(QuerySnapshot snapshots) {
                                                        final int size = snapshots.size();
                                                        final int number = size + 1;
                                                        final double random = new Random().nextDouble();

                                                        postsCollection.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                            @Override
                                                            public void onSuccess(QuerySnapshot snapshots) {
                                                                final int size = snapshots.size();
                                                                final int number = size + 1;
                                                                final double random = new Random().nextDouble();

                                                                Post post = new Post();
                                                                post.setCollection_id(pushId);
                                                                post.setType("single_video_post");
                                                                post.setPost_id(post_id);
                                                                post.setUser_id(firebaseAuth.getCurrentUser().getUid());
                                                                post.setRandom_number(random);
                                                                post.setNumber(number);
                                                                post.setDeeplink(shortLink.toString());
                                                                post.setTime(timeStamp);
                                                                post.setWidth(width);
                                                                post.setHeight(height);
                                                                post.setTitle(mTitleEditText.getText().toString());
                                                                post.setDescription(mCingleDescriptionEditText.getText().toString());
                                                                post.setUrl(downloadUri.toString());


                                                                postsCollection.document(post_id).set(post)
                                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                            @Override
                                                                            public void onSuccess(Void aVoid) {
                                                                                //reset input fields
                                                                                mTitleEditText.setText("");
                                                                                mCingleDescriptionEditText.setText("");
                                                                                mPostImageView.setImageBitmap(null);

                                                                                //launch the collections activity
                                                                                Intent intent = new Intent(CreateSingleActivity.this, HomeActivity.class);
                                                                                intent.putExtra(CreateSingleActivity.EXTRA_USER_UID, ProfileActivity.class);
                                                                                intent.putExtra(CreateSingleActivity.EXTRA_USER_UID, firebaseAuth.getCurrentUser().getUid());
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

package com.andeqa.andeqa.camera;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.provider.MediaStore;
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
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.R;
import com.andeqa.andeqa.models.CollectionPost;
import com.andeqa.andeqa.models.Post;
import com.andeqa.andeqa.models.TransactionDetails;
import com.andeqa.andeqa.profile.ProfileActivity;
import com.andeqa.andeqa.utils.ProportionalImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Random;

import butterknife.Bind;
import butterknife.ButterKnife;

public class PreviewActivity extends AppCompatActivity implements View.OnClickListener {
    private String image;
    private String video;
    private static final String CAMERA_PATH = "camera image";
    private static final String GALLERY_PATH ="gallery image";
    private static final String GALLERY_VIDEO ="camera_video";
    private static final String KEY_IMAGE = "image";
    private static final String EXTRA_USER_UID = "uid";

    private Uri photoUri;
    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;

    private static final int DEFAULT_TITLE_LENGTH_LIMIT = 100;

    //FIRESTORE
    private FirebaseFirestore firebaseFirestore;
    private CollectionReference postsCollection;
    private CollectionReference singlesCollection;
    private CollectionReference postOwnersCollection;
    private CollectionReference usersReference;
    private DatabaseReference randomReference;
    private CollectionReference collectionsCollection;


    @Bind(R.id.postImageView)ProportionalImageView mPostImageView;
    @Bind(R.id.titleEditText)EditText mCingleTitleEditText;
    @Bind(R.id.descriptionEditText)EditText mCingleDescriptionEditText;
    @Bind(R.id.postPostImageView)ImageView mPostPostImageView;
    @Bind(R.id.descriptionCountTextView)TextView mDescriptionCountTextView;
    @Bind(R.id.titleCountTextView)TextView mTitleCountTextView;


//    @Bind(R.id.videoShowVideoView)VideoView mVideShowVideoView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);
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


        firebaseAuth = FirebaseAuth.getInstance();
        mPostPostImageView.setOnClickListener(this);

        init();


        if (firebaseAuth.getCurrentUser() != null){

            //initialize firestore
            firebaseFirestore =  FirebaseFirestore.getInstance();
            //get the reference to posts(collection reference)
            postsCollection = FirebaseFirestore.getInstance().collection(Constants.POSTS);
            postOwnersCollection = firebaseFirestore.collection(Constants.POST_OWNERS);
            usersReference = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);
            randomReference = FirebaseDatabase.getInstance().getReference(Constants.RANDOM_PUSH_ID);
            collectionsCollection = FirebaseFirestore.getInstance().collection(Constants.COLLECTIONS_POSTS);

            mCingleTitleEditText.setFilters(new InputFilter[]{new InputFilter
                    .LengthFilter(DEFAULT_TITLE_LENGTH_LIMIT)});

            textWatchers();
            uploadingToFirebaseDialog();
            getCinglesIntent();

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
        if(isFinishing()){
            Picasso.with(this).cancelRequest(mPostImageView);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putString(KEY_IMAGE, image);
    }

    private void getCinglesIntent(){
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        String action = intent.getAction();

        //if this is from share action
        if(intent.ACTION_SEND.equals(action)){
            if (bundle.containsKey(Intent.EXTRA_STREAM)){
                //get the resource path
                handleIntentData(intent);
            }
        }
    }

    public void handleIntentData(Intent data){
        Uri imageSelected = data.getParcelableExtra(Intent.EXTRA_STREAM);
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageSelected);
            Picasso.with(this)
                    .load(imageSelected)
                    .into(mPostImageView, new Callback.EmptyCallback(){
                        @Override
                        public void onSuccess(){
                            /**index 0 is the mChosenImageView*/
                        }
                    });
        }catch (IOException e){
            e.printStackTrace();
        }
    }



    VideoView videoView;
    private void init() {

        if (getIntent().getExtras() != null){
            if (getIntent().getStringExtra(GALLERY_PATH) != null){
                image = getIntent().getStringExtra(GALLERY_PATH);
            }else if (getIntent().getStringExtra(CAMERA_PATH) != null){
                image = getIntent().getStringExtra(CAMERA_PATH);
            }else {
                video = getIntent().getStringExtra(GALLERY_VIDEO);
            }
        }

        if(image != null){
            Glide.with(PreviewActivity.this)
                    .load(image)
                    .listener(new RequestListener<String, GlideDrawable>() {
                        @Override
                        public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {

                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                            return false;
                        }
                    })
                    .into(mPostImageView);
        }

        if (image != null){
            Picasso.with(PreviewActivity.this)
                    .load(new File(image))
                    .into(mPostImageView);
        }

        if (video != null){
            //            videoView.setVisibility(View.VISIBLE);
//            try {
//                videoView.setMediaController(null);
//                videoView.setVideoURI(Uri.parse(getIntent().getStringExtra("PATH")));
//            } catch (Exception e){
//                e.printStackTrace();
//            }
//            videoView.requestFocus();
//            //videoView.setZOrderOnTop(true);
//            videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
//                public void onPrepared(MediaPlayer mp) {
//
//                    videoView.start();
//                }
//            });
//            videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//                @Override
//                public void onCompletion(MediaPlayer mp) {
//                    videoView.start();
//                }
//            });
        }

    }


    @Override
    public void onClick(View v){
        if(v == mPostPostImageView){
            create();

        }

    }


    public void uploadingToFirebaseDialog(){
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Adding your post");
        progressDialog.setCancelable(false);
    }

    private void create(){
        if (image != null){
            progressDialog.show();

            //get the data from the imageview as bytes
            mPostImageView.setDrawingCacheEnabled(true);
            Bitmap bitmap = ((BitmapDrawable) mPostImageView.getDrawable()).getBitmap();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();

            //current time
            final long timeStamp = new Date().getTime();
            //this push id is to put all the singles together under the same reference
            final DatabaseReference collectiveRef = randomReference.push();
            final String pushId = collectiveRef.getKey();
            //this push id is to add new single to the same reference
            final DatabaseReference singleRef = randomReference.push();
            final String post_id = singleRef.getKey();

            final StorageReference storageReference = FirebaseStorage
                    .getInstance().getReference()
                    .child(Constants.USER_COLLECTIONS)
                    .child(post_id);

            if (data != null){
                UploadTask uploadTask = storageReference.putBytes(data);
                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        final Uri downloadUrl = taskSnapshot.getDownloadUrl();

                        CollectionReference cl = collectionsCollection;
                        cl.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot documentSnapshots) {
                                final int index = documentSnapshots.getDocuments().size();
                                CollectionPost collectionPost = new CollectionPost();
                                final int size = documentSnapshots.size();
                                final int number = size + 1;
                                final double random = new Random().nextDouble();

                                //record all the collectionPost data
                                collectionPost.setNumber(number);
                                collectionPost.setRandom_number(random);
                                collectionPost.setTime(timeStamp);
                                collectionPost.setUser_id(firebaseAuth.getCurrentUser().getUid());
                                collectionPost.setTitle(mCingleTitleEditText.getText().toString());
                                collectionPost.setDescription(mCingleDescriptionEditText.getText().toString());
                                collectionPost.setImage(downloadUrl.toString());
                                collectionPost.setPost_id(post_id);
                                collectionPost.setUser_id(firebaseAuth.getCurrentUser().getUid());
                                collectionPost.setType("single");
                                collectionPost.setCollection_id(pushId);
                                collectionsCollection.document("singles").collection(pushId)
                                        .document(post_id).set(collectionPost).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        postsCollection.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                            @Override
                                            public void onSuccess(QuerySnapshot snapshots) {
                                                final int size = snapshots.size();
                                                final int number = size + 1;
                                                final double random = new Random().nextDouble();

                                                Post post = new Post();
                                                post.setCollection_id(pushId);
                                                post.setType("single");
                                                post.setPost_id(post_id);
                                                post.setUser_id(firebaseAuth.getCurrentUser().getUid());
                                                post.setRandom_number(random);
                                                post.setNumber(number);
                                                post.setTime(timeStamp);

                                                postsCollection.document(post_id).set(post)
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                //set the collectionPost ownership
                                                                TransactionDetails transactionDetails = new TransactionDetails();
                                                                transactionDetails.setPost_id(post_id);
                                                                transactionDetails.setUser_id(firebaseAuth.getCurrentUser().getUid());
                                                                transactionDetails.setTime(timeStamp);
                                                                transactionDetails.setType("owner");
                                                                transactionDetails.setAmount(0.0);
                                                                transactionDetails.setWallet_balance(0.0);

                                                                postOwnersCollection.document(post_id).set(transactionDetails)
                                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                            @Override
                                                                            public void onSuccess(Void aVoid) {
                                                                                progressDialog.dismiss();
                                                                                //reset input fields
                                                                                mCingleTitleEditText.setText("");
                                                                                mCingleDescriptionEditText.setText("");
                                                                                mPostImageView.setImageBitmap(null);

                                                                                //launch the collections activity
                                                                                Intent intent = new Intent(PreviewActivity.this, ProfileActivity.class);
                                                                                intent.putExtra(PreviewActivity.EXTRA_USER_UID, ProfileActivity.class);
                                                                                startActivity(intent);
                                                                                finish();
                                                                            }
                                                                        });
                                                            }
                                                        });
                                            }
                                        });
                                    }
                                });


                            }
                        });


                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(PreviewActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        double progress = (100.0 * taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                        progressDialog.setMessage("Adding your post" + " " + ((int) progress) + "%...");
                        if (progress == 100.0){

                        }
                    }
                });

            }

        }
    }


}

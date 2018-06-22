package com.andeqa.andeqa.creation;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.R;
import com.andeqa.andeqa.camera.GalleryActivity;
import com.andeqa.andeqa.models.Wallet;
import com.andeqa.andeqa.models.CollectionPost;
import com.andeqa.andeqa.models.Post;
import com.andeqa.andeqa.models.Transaction;
import com.andeqa.andeqa.collections.CollectionPostsActivity;
import com.andeqa.andeqa.utils.ProportionalImageView;
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
import java.util.Date;
import java.util.Random;

import butterknife.Bind;
import butterknife.ButterKnife;

public class CreatePostActivity extends AppCompatActivity implements View.OnClickListener{
    @Bind(R.id.titleEditText)EditText mCingleTitleEditText;
    @Bind(R.id.descriptionEditText)EditText mCingleDescriptionEditText;
    @Bind(R.id.postImageView)ProportionalImageView mPostImageView;
    @Bind(R.id.postPostImageView)ImageView mPostPostImageView;
    @Bind(R.id.descriptionCountTextView)TextView mDescriptionCountTextView;
    @Bind(R.id.titleCountTextView)TextView mTitleCountTextView;
    @Bind(R.id.postImageRelativeLayout)RelativeLayout mPostImageRelativeLayout;
    @Bind(R.id.addRelativeLayout)RelativeLayout mAddRelativeLayout;

    private static final String TAG = CreatePostActivity.class.getSimpleName();
    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;

    private static final int DEFAULT_TITLE_LENGTH_LIMIT = 100;
    private static final int IMAGE_GALLERY_REQUEST = 112;
    private static final String COLLECTION_ID = "collection id";
    private static final String EXTRA_USER_UID = "uid";
    private String collectionId;
    private String postIntent;
    private static final String POST_TAG = CreatePostActivity.class.getSimpleName();
    private static final String GALLERY_PATH ="gallery image";




    //FIRESTORE
    private FirebaseFirestore firebaseFirestore;
    private CollectionReference postsCollection;
    private CollectionReference postWalletCollection;
    private DatabaseReference randomReference;
    private CollectionReference collectionsCollection;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);
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

        firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() != null){

            collectionId = getIntent().getStringExtra(COLLECTION_ID);
            postIntent = getIntent().getStringExtra(GALLERY_PATH);


            //initialize firestore
            firebaseFirestore =  FirebaseFirestore.getInstance();
            //get the reference to posts(collection reference)
            postsCollection = FirebaseFirestore.getInstance().collection(Constants.POSTS);
            randomReference = FirebaseDatabase.getInstance().getReference(Constants.RANDOM_PUSH_ID);
            collectionsCollection = FirebaseFirestore.getInstance().collection(Constants.COLLECTIONS_POSTS);
            postWalletCollection =    FirebaseFirestore.getInstance().collection(Constants.POST_WALLET);

            mCingleTitleEditText.setFilters(new InputFilter[]{new InputFilter
                    .LengthFilter(DEFAULT_TITLE_LENGTH_LIMIT)});

            textWatchers();
            uploadingToFirebaseDialog();
            loadPostImage();

        }

    }

    private void loadPostImage(){
        if (postIntent != null){
            mAddRelativeLayout.setVisibility(View.GONE);
            mPostImageRelativeLayout.setVisibility(View.VISIBLE);
            Picasso.with(this)
                    .load(new File(postIntent))
                    .into(mPostImageView,
                            new Callback.EmptyCallback(){
                                @Override
                                public void onSuccess(){

                                }
                            });
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
            create();
        }

        if (v == mAddRelativeLayout){
            Intent intent = new Intent(CreatePostActivity.this, GalleryActivity.class);
            intent.putExtra(CreatePostActivity.POST_TAG, CreatePostActivity.class.getSimpleName());
            intent.putExtra(CreatePostActivity.COLLECTION_ID, collectionId);
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


    private void create(){
        if (postIntent != null){
            progressDialog.show();
            //get the data from the imageview as bytes
            mPostImageView.setDrawingCacheEnabled(true);
            Bitmap bitmap = ((BitmapDrawable) mPostImageView.getDrawable()).getBitmap();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();

            //current time
            final long timeStamp = new Date().getTime();
            //push id to organise the posts according to time
            final DatabaseReference reference = randomReference.push();
            final String pushId = reference.getKey();
            final StorageReference storageReference = FirebaseStorage
                    .getInstance().getReference()
                    .child(Constants.USER_COLLECTIONS)
                    .child(pushId);

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
                                collectionPost.setPost_id(pushId);
                                collectionPost.setUser_id(firebaseAuth.getCurrentUser().getUid());
                                collectionPost.setType("collection_post");
                                collectionPost.setCollection_id(collectionId);
                                collectionsCollection.document("collections").collection(collectionId)
                                        .document(pushId).set(collectionPost).addOnSuccessListener(new OnSuccessListener<Void>() {
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
                                                post.setType("post");
                                                post.setPost_id(pushId);
                                                post.setUser_id(firebaseAuth.getCurrentUser().getUid());
                                                post.setRandom_number(random);
                                                post.setNumber(number);
                                                post.setTime(timeStamp);

                                                postsCollection.document(pushId).set(post)
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                //create a new post wallet
                                                                Wallet wallet = new Wallet();
                                                                wallet.setTime(timeStamp);
                                                                wallet.setBalance(0.0);
                                                                wallet.setDeposited(0.0);
                                                                wallet.setRedeemed(0.0);
                                                                wallet.setAddress(pushId);
                                                                wallet.setUser_id(firebaseAuth.getCurrentUser().getUid());
                                                                postWalletCollection.document(pushId).set(wallet);

                                                                //launch the collections activity
                                                                Intent intent = new Intent(CreatePostActivity.this, CollectionPostsActivity.class);
                                                                intent.putExtra(CreatePostActivity.COLLECTION_ID, collectionId);
                                                                intent.putExtra(CreatePostActivity.EXTRA_USER_UID, firebaseAuth.getCurrentUser().getUid());
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
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(CreatePostActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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

            }else {
                Toast.makeText(CreatePostActivity.this, "You have chosent a photo", Toast.LENGTH_LONG).show();
            }

        }
    }

}

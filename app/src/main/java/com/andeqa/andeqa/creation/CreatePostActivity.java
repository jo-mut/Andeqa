package com.andeqa.andeqa.creation;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.R;
import com.andeqa.andeqa.models.Andeqan;
import com.andeqa.andeqa.models.CollectionPost;
import com.andeqa.andeqa.models.Post;
import com.andeqa.andeqa.models.TransactionDetails;
import com.andeqa.andeqa.profile.CollectionsPostsActivity;
import com.andeqa.andeqa.utils.DialogProgressFragment;
import com.andeqa.andeqa.utils.ProportionalImageView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.Date;
import java.util.Random;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class CreatePostActivity extends AppCompatActivity implements View.OnClickListener{
    @Bind(R.id.titleEditText)EditText mCingleTitleEditText;
    @Bind(R.id.descriptionEditText)EditText mCingleDescriptionEditText;
    @Bind(R.id.profileImageView)CircleImageView mProfileImageView;
    @Bind(R.id.postImageView)ProportionalImageView mPostImageView;
    @Bind(R.id.backImageView)ImageView mBackImageView;
    @Bind(R.id.postTextView)TextView mPostTextView;
    @Bind(R.id.descriptionCountTextView)TextView mDescriptionCountTextView;
    @Bind(R.id.titleCountTextView)TextView mTitleCountTextView;


    private String image;
    private Uri photoUri;
    private static final String KEY_IMAGE = "IMAGE FROM GALLERY";
    private static final String TAG = "CreatePostActivity";
    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;

    private static final int DEFAULT_TITLE_LENGTH_LIMIT = 100;
    private static final int IMAGE_GALLERY_REQUEST = 112;
    private static final String COLLECTION_ID = "collection id";
    private static final String EXTRA_USER_UID = "uid";
    private String collectionId;


    //FIRESTORE
    private FirebaseFirestore firebaseFirestore;
    private CollectionReference postsCollection;
    private CollectionReference singlesCollection;
    private CollectionReference postOwnersCollection;
    private CollectionReference usersReference;
    private DatabaseReference randomReference;
    private CollectionReference collectionsCollection;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);
        ButterKnife.bind(this);

        mPostTextView.setOnClickListener(this);
        mBackImageView.setOnClickListener(this);
        mPostImageView.setOnClickListener(this);

        firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() != null){

            collectionId = getIntent().getStringExtra(COLLECTION_ID);
            if(collectionId == null){
                throw new IllegalArgumentException("pass an EXTRA_UID");
            }

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
            fetchUserData();
            uploadingToFirebaseDialog();

        }

        getCinglesIntent();

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

    public void fetchUserData(){
        usersReference.document(firebaseAuth.getCurrentUser().getUid())
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (documentSnapshot.exists()) {
                    final Andeqan cinggulan = documentSnapshot.toObject(Andeqan.class);
                    final String profileImage = cinggulan.getProfileImage();

                    Picasso.with(CreatePostActivity.this)
                            .load(profileImage)
                            .fit()
                            .centerCrop()
                            .networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.profle_image_background)
                            .into(mProfileImageView, new Callback() {
                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onError() {
                                    Picasso.with(CreatePostActivity.this)
                                            .load(profileImage)
                                            .fit()
                                            .centerCrop()
                                            .placeholder(R.drawable.profle_image_background)
                                            .into(mProfileImageView);
                                }
                            });
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            if(requestCode == IMAGE_GALLERY_REQUEST && data != null){
                photoUri = data.getData();
                if (photoUri != null){
                    Picasso.with(this)
                            .load(photoUri)
                            .into(mPostImageView,
                                    new Callback.EmptyCallback(){
                                        @Override
                                        public void onSuccess(){

                                        }
                                    });

                }
            }else {
                super.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    @Override
    public void onClick(View v){
        if(v == mPostTextView){
            if (firebaseAuth.getCurrentUser() != null){
                create();
            }else {
                FragmentManager fragmentManager = getSupportFragmentManager();
                DialogProgressFragment dialogProgressFragment = DialogProgressFragment.newInstance("progress dialog");
                dialogProgressFragment.show(fragmentManager, "progress dialog");
            }
        }

        if (v == mBackImageView){
            finish();
        }

        if (v == mPostImageView){
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, IMAGE_GALLERY_REQUEST);
        }

    }


    public void uploadingToFirebaseDialog(){
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Adding your post");
        progressDialog.setCancelable(true);
    }


    private void create(){
        if (photoUri != null){
            progressDialog.show();

            //current time
            final long timeStamp = new Date().getTime();
            //push id to organise the posts according to time
            final DatabaseReference reference = randomReference.push();
            final String pushId = reference.getKey();
            final StorageReference storageReference = FirebaseStorage
                    .getInstance().getReference()
                    .child(Constants.COLLECTIONS)
                    .child(pushId);

            UploadTask uploadTask = storageReference.putFile(photoUri);
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
                            collectionPost.setRandomNumber(random);
                            collectionPost.setTime(timeStamp);
                            collectionPost.setUserId(firebaseAuth.getCurrentUser().getUid());
                            collectionPost.setPostId(pushId);
                            collectionPost.setTitle(mCingleTitleEditText.getText().toString());
                            collectionPost.setDescription(mCingleDescriptionEditText.getText().toString());
                            collectionPost.setImage(downloadUrl.toString());
                            collectionPost.setPostId(pushId);
                            collectionPost.setUserId(firebaseAuth.getCurrentUser().getUid());
                            collectionPost.setType("collection_post");
                            collectionPost.setCollectionId(collectionId);
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
                                            post.setCollectionId(collectionId);
                                            post.setType("post");
                                            post.setPostId(pushId);
                                            post.setUserId(firebaseAuth.getCurrentUser().getUid());
                                            post.setRandomNumber(random);
                                            post.setNumber(number);
                                            post.setTime(timeStamp);

                                            postsCollection.document(pushId).set(post)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    //set the collectionPost ownership
                                                    TransactionDetails transactionDetails = new TransactionDetails();
                                                    transactionDetails.setPostId(pushId);
                                                    transactionDetails.setUserId(firebaseAuth.getCurrentUser().getUid());
                                                    transactionDetails.setTime(timeStamp);
                                                    transactionDetails.setType("owner");
                                                    transactionDetails.setAmount(0.0);
                                                    transactionDetails.setWalletBalance(0.0);

                                                    postOwnersCollection.document(pushId).set(transactionDetails)
                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            progressDialog.dismiss();
                                                            //reset input fields
                                                            mCingleTitleEditText.setText("");
                                                            mCingleDescriptionEditText.setText("");
                                                            mPostImageView.setImageBitmap(null);

                                                            //launch the collections activity
                                                            Intent intent = new Intent(CreatePostActivity.this, CollectionsPostsActivity.class);
                                                            intent.putExtra(CreatePostActivity.COLLECTION_ID, collectionId);
                                                            intent.putExtra(CreatePostActivity.EXTRA_USER_UID, firebaseAuth.getCurrentUser().getUid());
                                                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
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


        }
    }

}

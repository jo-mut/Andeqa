package com.andeqa.andeqa.creation;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.R;
import com.andeqa.andeqa.collections.CollectionPostsActivity;
import com.andeqa.andeqa.models.Collection;
import com.andeqa.andeqa.models.QueryOptions;
import com.andeqa.andeqa.utils.ProportionalImageView;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Arrays;
import java.util.Date;

import butterknife.Bind;
import butterknife.ButterKnife;

public class CreateCollectionActivity extends AppCompatActivity implements View.OnClickListener{
    @Bind(R.id.collectionNameEditText)EditText mCollectionNameEditText;
    @Bind(R.id.collectionNoteEditText)EditText mCollectionNoteEditText;
    @Bind(R.id.doneImageView)ImageView mDoneImageView;
    @Bind(R.id.noteCountTextView)TextView mNoteCountTextView;
    @Bind(R.id.nameCountTextView)TextView mNameCountTextView;
    @Bind(R.id.collectionCoverImageView)ProportionalImageView mCollectionCoverImageView;
    @Bind(R.id.collectionLinearLayout)RelativeLayout mCollectionRelativeLayout;
    @Bind(R.id.addRelativeLayout)RelativeLayout mAddRelativeLayout;

    private Uri photoUri;
    private static final String KEY_IMAGE = "IMAGE FROM GALLERY";
    private static final String TAG = CreateCollectionActivity.class.getSimpleName();

    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;

    private static final int DEFAULT_TITLE_LENGTH_LIMIT = 25;
    private static final int DEFAULT_DESCRIPTION_LENGTH_LIMIT = 100;
    private static final int IMAGE_GALLERY_REQUEST = 112;

    //FIRESTORE
    private FirebaseFirestore firebaseFirestore;
    private CollectionReference postsReference;
    private DatabaseReference postReference;
    private CollectionReference collectionReference;
    private CollectionReference queryOptionsReference;

    //intent extras
    private static final String COLLECTION_ID = "collection id";
    private static final String EXTRA_USER_UID = "uid";
    private static final String GALLERY_PATH ="gallery image";
    private static final String COLLECTION_TAG = CreateCollectionActivity.class.getSimpleName();
    private String image;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_collection);
        ButterKnife.bind(this);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationIcon(R.drawable.ic_arrow_black);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        mDoneImageView.setOnClickListener(this);
        mAddRelativeLayout.setOnClickListener(this);

        firebaseAuth = FirebaseAuth.getInstance();

        if (getIntent().getExtras() != null){
            if (getIntent().getStringExtra(GALLERY_PATH) != null){
                image = getIntent().getStringExtra(GALLERY_PATH);

            }
        }


        if (firebaseAuth.getCurrentUser() != null){
            //initialize firestore
            firebaseFirestore =  FirebaseFirestore.getInstance();
            //get the reference to posts(collection reference)
            postsReference = firebaseFirestore.collection(Constants.COLLECTIONS);
            queryOptionsReference = FirebaseFirestore.getInstance().collection(Constants.QUERY_OPTIONS);
            collectionReference = FirebaseFirestore.getInstance().collection(Constants.COLLECTIONS);
            //firebase
            postReference = FirebaseDatabase.getInstance().getReference(Constants.COLLECTIONS);
            //textwatchers
            mCollectionNameEditText.setFilters(new InputFilter[]{new InputFilter
                    .LengthFilter(DEFAULT_TITLE_LENGTH_LIMIT)});
            mCollectionNoteEditText.setFilters(new  InputFilter[]{new InputFilter
                    .LengthFilter(DEFAULT_DESCRIPTION_LENGTH_LIMIT)});
            textWatchers();
            uploadingToFirebaseDialog();
            loadCoverPhoto();

            //permission
           int version = Build.VERSION.SDK_INT;
           if (version > Build.VERSION_CODES.LOLLIPOP_MR1){
               if (!checkIfAlreadyHavePermission()){
                   requestForSpecificPermission();
               }
           }

        }
    }

    private void loadCoverPhoto(){
        if (image != null){
            mAddRelativeLayout.setVisibility(View.GONE);
            mCollectionRelativeLayout.setVisibility(View.VISIBLE);
            Glide.with(CreateCollectionActivity.this)
                    .asBitmap()
                    .load(new File(image))
                    .into(mCollectionCoverImageView);
        }

    }

    private void textWatchers(){
        //TITLE TEXT WATCHER
        mCollectionNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                int count = DEFAULT_TITLE_LENGTH_LIMIT - editable.length();
                mNameCountTextView.setText(Integer.toString(count));

                if (count == 0){
                    mNameCountTextView.setTextColor(Color.RED);
                }else if (count <= 25){
                    mNameCountTextView.setTextColor(Color.BLACK);
                }else{
                    //do nothing
                }

            }
        });

        //DESCRIPTION TEXT WATCHER
        mCollectionNoteEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                int count = DEFAULT_DESCRIPTION_LENGTH_LIMIT- editable.length();
                mNoteCountTextView.setText(Integer.toString(count));

                if (count == 0){
                    mNoteCountTextView.setTextColor(Color.RED);
                }else if (count <= 100){
                    mNoteCountTextView.setTextColor(Color.BLACK);
                }else{
                    //do nothing
                }

            }
        });

    }

    private boolean checkIfAlreadyHavePermission(){
        int result = ContextCompat.checkSelfPermission(this,  Manifest.permission.GET_ACCOUNTS);
        if (result == PackageManager.PERMISSION_GRANTED){
            return true;
        }else {
            return false;
        }
    }

    private void requestForSpecificPermission(){
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        }, 101);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 101:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    //granted
                }else {
                    // not granted
                }
                break;
                default:
                    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public void uploadingToFirebaseDialog(){
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Creating your collection");
        progressDialog.setCancelable(false);
    }

    private void  createCollection(){
        final DatabaseReference collectionRef= postReference.push();
        final String collectionId = collectionRef.getKey();
        final String name = mCollectionNameEditText.getText().toString();
        final String note = mCollectionNoteEditText.getText().toString();

        if (TextUtils.isEmpty(name) && TextUtils.isEmpty(note)){
            Toast.makeText(CreateCollectionActivity.this, "Your collection must have a name and a description note",
                    Toast.LENGTH_SHORT).show();
        }else if (image == null){
            Toast.makeText(CreateCollectionActivity.this, "Your collection must have a cover photo",
                    Toast.LENGTH_SHORT).show();
        }else {
            progressDialog.show();
            //get the data from the imageview as bytes
            mCollectionCoverImageView.setDrawingCacheEnabled(true);
            Bitmap bitmap = ((BitmapDrawable) mCollectionCoverImageView.getDrawable()).getBitmap();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            final byte[] data = baos.toByteArray();
            collectionReference.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot documentSnapshots) {
                    final Collection collection = new Collection();
                    final long timeStamp = new Date().getTime();

                    if (data != null){
                        final StorageReference storageReference = FirebaseStorage
                                .getInstance().getReference()
                                .child(Constants.COLLECTIONS)
                                .child("collection_covers")
                                .child(collectionId);

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

                                    CollectionReference cl = postsReference;
                                    cl.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                        @Override
                                        public void onSuccess(QuerySnapshot documentSnapshots) {

                                            final int count = documentSnapshots.getDocuments().size();
                                            //save the collection
                                            collection.setType("collection");
                                            collection.setName(name);
                                            collection.setNote(note);
                                            collection.setNumber(count + 1);
                                            collection.setUser_id(firebaseAuth.getCurrentUser().getUid());
                                            collection.setCollection_id(collectionId);
                                            collection.setTime(timeStamp);
                                            collection.setImage(downloadUri.toString());
                                            collectionReference.document(collectionId).set(collection);

                                            mCollectionNameEditText.setText("");
                                            mCollectionNoteEditText.setText("");

                                            final String nameToLowercase [] = name.toLowerCase().split(" ");
                                            final String noteToLowercase [] = note.toLowerCase().split(" ");

                                            QueryOptions queryOptions = new QueryOptions();
                                            queryOptions.setOption_id(collectionId);
                                            queryOptions.setUser_id(firebaseAuth.getCurrentUser().getUid());
                                            queryOptions.setType("collection");
                                            queryOptions.setOne(Arrays.asList(nameToLowercase));
                                            queryOptions.setTwo(Arrays.asList(noteToLowercase));
                                            queryOptionsReference.document(collectionId).set(queryOptions);

                                            Intent intent = new Intent(CreateCollectionActivity.this, CollectionPostsActivity.class);
                                            intent.putExtra(CreateCollectionActivity.COLLECTION_ID, collectionId);
                                            intent.putExtra(CreateCollectionActivity.EXTRA_USER_UID, firebaseAuth.getCurrentUser().getUid());
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
                                progressDialog.dismiss();
                                Toast.makeText(CreateCollectionActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });

                        uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                double progress = (100.0 * taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                                progressDialog.setMessage("Creating your collection" + " " + ((int) progress) + "%");
                                if (progress == 100.0){
                                    progressDialog.dismiss();
                                    //reset input fields
                                }
                            }
                        });

                    }else {
                        final int count = documentSnapshots.getDocuments().size();
                        //save the collection
                        collection.setType("collection");
                        collection.setName(mCollectionNameEditText.getText().toString().trim());
                        collection.setNote(mCollectionNoteEditText.getText().toString().trim());
                        collection.setNumber(count + 1);
                        collection.setUser_id(firebaseAuth.getCurrentUser().getUid());
                        collection.setCollection_id(collectionId);
                        collection.setTime(timeStamp);
                        collectionReference.document(collectionId).set(collection);

                        progressDialog.dismiss();
                        mCollectionNameEditText.setText("");
                        mCollectionNoteEditText.setText("");


                        Intent intent = new Intent(CreateCollectionActivity.this, CollectionPostsActivity.class);
                        intent.putExtra(CreateCollectionActivity.COLLECTION_ID, collectionId);
                        intent.putExtra(CreateCollectionActivity.EXTRA_USER_UID, firebaseAuth.getCurrentUser().getUid());
                        startActivity(intent);
                        finish();
                    }

                }
            });

        }

    }


    @Override
    public void onClick(View v){

        if (v == mDoneImageView){
            createCollection();
        }

        if (v == mAddRelativeLayout){
            mAddRelativeLayout.setVisibility(View.GONE);
            mCollectionRelativeLayout.setVisibility(View.VISIBLE);
            Intent intent = new Intent(CreateCollectionActivity.this, CreateActivity.class);
            intent.putExtra(CreateCollectionActivity.COLLECTION_TAG, CreateCollectionActivity.class.getSimpleName());
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
}



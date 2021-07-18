package com.andeka.andeka.settings;

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
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.andeka.andeka.Constants;
import com.andeka.andeka.R;
import com.andeka.andeka.models.Collection;
import com.andeka.andeka.models.QueryOptions;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Arrays;

import butterknife.Bind;
import butterknife.ButterKnife;

public class CollectionSettingsActivity extends AppCompatActivity implements View.OnClickListener {
    //bind views
    @Bind(R.id.changeNoteRelativeLayout)LinearLayout mChangeNoteRelativeLayout;
    @Bind(R.id.changeNameRelativeLayout)LinearLayout  mChangeNameRelativeLayout;
    @Bind(R.id.changeCoverRelativeLayout)LinearLayout  mChangeCoverRelativeLayout;
    @Bind(R.id.collectionCoverImageView)ImageView mCollectionCoverImageView;
    @Bind(R.id.collectionNoteTextView)TextView mCollectionNoteTextView;
    @Bind(R.id.collectionNameTextView)TextView mCollectionNameTextView;
    @Bind(R.id.collectionNameEditText)EditText mCollectionNameEditText;
    @Bind(R.id.collectionNoteEditText)EditText mCollectionNoteEditText;
    @Bind(R.id.noteCountTextView)TextView mNoteCountTextView;
    @Bind(R.id.nameCountTextView)TextView mNameCountTextView;
    @Bind(R.id.toolbar)Toolbar toolbar;
    @Bind(R.id.collectionNoteRelativeLayout)RelativeLayout mCollectionNoteRelativeLayout;
    @Bind(R.id.collectionNameRelativeLayout)RelativeLayout mCollectionNameRelativelayout;
    @Bind(R.id.doneRelativeLayout)RelativeLayout mDoneRelativeLayout;

    private static final String TAG = CollectionSettingsActivity.class.getSimpleName();
    //firestore reference
    private CollectionReference collectionCollection;
    private CollectionReference queryOptionsReference;
    private Query collectionsQuery;
    //firebase auth
    private FirebaseAuth firebaseAuth;
    private Uri photoUri;
    private ProgressDialog progressDialog;


    private static final int DEFAULT_TITLE_LENGTH_LIMIT = 25;
    private static final int DEFAULT_DESCRIPTION_LENGTH_LIMIT = 100;
    private  static final int MAX_WIDTH = 400;
    private static final int MAX_HEIGHT = 400;
    private static  final int GALLERY_PROFILE_PHOTO_REQUEST = 111;


    //intent extras
    private static final String COLLECTION_ID = "collection id";
    private static final String EXTRA_USER_UID = "uid";
    private String collectionId;
    private static final String COLLECTION_SETTINGS_COVER = CollectionSettingsActivity.class.getSimpleName();
    private static final String GALLERY_PATH ="gallery image";
    private String mUid;
    private String collectionSettingsIntent;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection_settings);
        ButterKnife.bind(this);

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

        mChangeCoverRelativeLayout.setOnClickListener(this);
        mChangeNameRelativeLayout.setOnClickListener(this);
        mChangeNoteRelativeLayout.setOnClickListener(this);
        mDoneRelativeLayout.setOnClickListener(this);

        if (firebaseAuth.getCurrentUser() != null){
            collectionId = getIntent().getStringExtra(COLLECTION_ID);
            collectionCollection = FirebaseFirestore.getInstance().collection(Constants.COLLECTIONS);
            queryOptionsReference = FirebaseFirestore.getInstance().collection(Constants.QUERY_OPTIONS);

            //textwatchers
            mCollectionNameEditText.setFilters(new InputFilter[]{new InputFilter
                    .LengthFilter(DEFAULT_TITLE_LENGTH_LIMIT)});
            mCollectionNoteEditText.setFilters(new  InputFilter[]{new InputFilter
                    .LengthFilter(DEFAULT_DESCRIPTION_LENGTH_LIMIT)});
            textWatchers();
            uploadingToFirebaseDialog();
            colectionInfo();
            loadCoverImage();
        }

    }

    private void loadCoverImage(){
        collectionSettingsIntent = getIntent().getStringExtra(GALLERY_PATH);
        if (collectionSettingsIntent != null){
            Glide.with(this)
                    .asBitmap()
                    .load(new File(collectionSettingsIntent))
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
                if (mDoneRelativeLayout.getVisibility() == View.GONE){
                    mDoneRelativeLayout.setVisibility(View.VISIBLE);
                }
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
                if (mDoneRelativeLayout.getVisibility() == View.GONE){
                    mDoneRelativeLayout.setVisibility(View.VISIBLE);
                }
                int count = DEFAULT_DESCRIPTION_LENGTH_LIMIT- editable.length();
                mNoteCountTextView.setText(Integer.toString(count));

                if (count == 0){
                    mNameCountTextView.setTextColor(Color.RED);
                }else if (count <= 100){
                    mNoteCountTextView.setTextColor(Color.BLACK);
                }else{
                    //do nothing
                }

            }
        });

    }

    public void uploadingToFirebaseDialog(){
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(true);
        progressDialog.setMessage("Updating your collection");
    }

    public void colectionInfo(){
        collectionCollection.document(collectionId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }


                if (documentSnapshot.exists()){
                    Collection collection = documentSnapshot.toObject(Collection.class);
                    final String name = collection.getName();
                    final String note = collection.getNote();

                    mCollectionNameTextView.setText(name);
                    mCollectionNoteTextView.setText(note);

                }
            }
        });
    }

    /**update collection cover*/
    private void updateCollectionCover(){
        progressDialog.show();
        //get the data from the imageview as bytes
       if (collectionSettingsIntent != null){
           mCollectionCoverImageView.setDrawingCacheEnabled(true);
           Bitmap bitmap = ((BitmapDrawable) mCollectionCoverImageView.getDrawable()).getBitmap();
           ByteArrayOutputStream baos = new ByteArrayOutputStream();
           bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
           byte[] data = baos.toByteArray();
           final StorageReference storageReference = FirebaseStorage
                   .getInstance().getReference()
                   .child(Constants.COLLECTIONS)
                   .child("collection_covers")
                   .child(collectionId);


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

                           collectionCollection.document(collectionId)
                                   .update("image", downloadUri.toString());
                           progressDialog.dismiss();

                       } else {
                           // Handle failures
                           // ...
                       }
                   }
               });

              uploadTask.addOnFailureListener(new OnFailureListener() {
                   @Override
                   public void onFailure(@NonNull Exception e) {
                       progressDialog.dismiss();
                       Toast.makeText(CollectionSettingsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                   }
               }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                   @Override
                   public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                       double progress = (100.0 * taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                       progressDialog.setMessage("Updating your collection" + " " + ((int) progress) + "%...");
                       if (progress == 100.0){
                           progressDialog.dismiss();
                           //reset input fields
                       }
                   }
               });
           }
       }
    }


    private void updateNote(){
        final String note = mCollectionNoteEditText.getText().toString();
        if (!TextUtils.isEmpty(note)){
            collectionCollection.document(collectionId)
                    .update("note", note);
            final String noteToLowercase [] = note.toLowerCase().split(" ");

            QueryOptions queryOptions = new QueryOptions();
            queryOptions.setOption_id(collectionId);
            queryOptions.setUser_id(firebaseAuth.getCurrentUser().getUid());
            queryOptions.setType("collection");
            queryOptions.setTwo(Arrays.asList(noteToLowercase));
            queryOptionsReference.document(collectionId).set(queryOptions);
            mCollectionNoteEditText.setText("");

        }
    }

    private void updaterName(){
        final String name = mCollectionNameEditText.getText().toString();
        if (!TextUtils.isEmpty(name)){
            collectionCollection.document(collectionId)
                    .update("name", name);

            final String nameToLowercase [] = name.toLowerCase().split(" ");

            QueryOptions queryOptions = new QueryOptions();
            queryOptions.setOption_id(collectionId);
            queryOptions.setUser_id(firebaseAuth.getCurrentUser().getUid());
            queryOptions.setType("collection");
            queryOptions.setOne(Arrays.asList(nameToLowercase));
            queryOptionsReference.document(collectionId).set(queryOptions);

            mCollectionNameEditText.setText("");
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            if(requestCode == GALLERY_PROFILE_PHOTO_REQUEST && data != null){
                photoUri = data.getData();
                if (photoUri != null){
                    Glide.with(this)
                            .asBitmap()
                            .load(photoUri)
                            .into(mCollectionCoverImageView);

                    updateCollectionCover();
                }
            }else {
                super.onActivityResult(requestCode, resultCode, data);
            }
        }
    }


    @Override
    public void onClick(View v){
        if (v == mChangeCoverRelativeLayout){
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, GALLERY_PROFILE_PHOTO_REQUEST);

        }

        if (v == mChangeNameRelativeLayout){
            mCollectionNameRelativelayout.setVisibility(View.VISIBLE);
        }

        if (v == mChangeNoteRelativeLayout){
            mCollectionNoteRelativeLayout.setVisibility(View.VISIBLE);
        }

        if (v == mDoneRelativeLayout){
            updateNote();
            updaterName();
        }

    }
}

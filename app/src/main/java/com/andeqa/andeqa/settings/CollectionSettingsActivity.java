package com.andeqa.andeqa.settings;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
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
import com.andeqa.andeqa.profile.CollectionsPostsActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;

public class CollectionSettingsActivity extends AppCompatActivity implements View.OnClickListener {
    //bind views
    @Bind(R.id.changeNoteRelativeLayout)RelativeLayout mChangeNoteRelativeLayout;
    @Bind(R.id.changeNameRelativeLayout)RelativeLayout mChangeNameRelativeLayout;
    @Bind(R.id.changeCoverRelativeLayout)RelativeLayout mChangeCoverRelativeLayout;
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
    @Bind(R.id.doneEditingImageView)ImageView mDoneEditingImageView;


    private static final String TAG = CollectionSettingsActivity.class.getSimpleName();
    //firestore reference
    private CollectionReference collectionCollection;
    private Query collectionsQuery;
    //firebase auth
    private FirebaseAuth firebaseAuth;
    private String collectionId;
    private String mUid;
    private Uri photoUri;
    private ProgressDialog progressDialog;


    private static final int DEFAULT_TITLE_LENGTH_LIMIT = 25;
    private static final int DEFAULT_DESCRIPTION_LENGTH_LIMIT = 100;
    private static final int IMAGE_GALLERY_REQUEST = 112;

    private static final String COLLECTION_ID = "collection id";
    private static final String EXTRA_USER_UID = "uid";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection_settings);
        ButterKnife.bind(this);

        firebaseAuth = FirebaseAuth.getInstance();

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        mChangeCoverRelativeLayout.setOnClickListener(this);
        mChangeNameRelativeLayout.setOnClickListener(this);
        mChangeNoteRelativeLayout.setOnClickListener(this);
        mDoneEditingImageView.setOnClickListener(this);

        if (firebaseAuth.getCurrentUser() != null){
            collectionId = getIntent().getStringExtra(COLLECTION_ID);
            if(collectionId == null){
                throw new IllegalArgumentException("pass an collection id");
            }

            collectionCollection = FirebaseFirestore.getInstance().collection(Constants.COLLECTIONS);

            //textwatchers
            mCollectionNameEditText.setFilters(new InputFilter[]{new InputFilter
                    .LengthFilter(DEFAULT_TITLE_LENGTH_LIMIT)});
            mCollectionNoteEditText.setFilters(new  InputFilter[]{new InputFilter
                    .LengthFilter(DEFAULT_DESCRIPTION_LENGTH_LIMIT)});
            textWatchers();
            uploadingToFirebaseDialog();
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
    }

    private void collectionSettings(){
        if (photoUri != null){
            progressDialog.show();
            StorageReference storageReference = FirebaseStorage
                    .getInstance().getReference()
                    .child(Constants.COLLECTIONS)
                    .child(collectionId);

            UploadTask uploadTask = storageReference.putFile(photoUri);
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    final Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    collectionCollection.document(collectionId)
                            .update("image", downloadUrl.toString());

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(CollectionSettingsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                    progressDialog.setMessage("Creating your collection" + " " + ((int) progress) + "%...");
                    if (progress == 100.0){
                        progressDialog.dismiss();
                        //reset input fields
                        Intent intent = new Intent(CollectionSettingsActivity.this, CollectionsPostsActivity.class);
                        intent.putExtra(CollectionSettingsActivity.COLLECTION_ID, collectionId);
                        intent.putExtra(CollectionSettingsActivity.EXTRA_USER_UID, firebaseAuth.getCurrentUser().getUid());
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }
                }
            });

        }


    }

    private void collectionNoteAndName(){
        if (!TextUtils.isEmpty(mCollectionNameEditText.getText())){
            collectionCollection.document(collectionId)
                    .update("name", mCollectionNameEditText.getText().toString());
            mCollectionNameEditText.setText("");
        }

        if (!TextUtils.isEmpty(mCollectionNoteEditText.getText())){
            collectionCollection.document(collectionId)
                    .update("note", mCollectionNoteEditText.getText().toString());
            mCollectionNoteEditText.setText("");
        }

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
                            .into(mCollectionCoverImageView,
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
        if (v == mChangeCoverRelativeLayout){
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, IMAGE_GALLERY_REQUEST);

        }

        if (v == mChangeNameRelativeLayout){
            mCollectionNameRelativelayout.setVisibility(View.VISIBLE);
        }

        if (v == mChangeNoteRelativeLayout){
            mCollectionNoteRelativeLayout.setVisibility(View.VISIBLE);
        }

        if (v == mDoneEditingImageView){
            collectionSettings();
            collectionNoteAndName();
        }
    }
}

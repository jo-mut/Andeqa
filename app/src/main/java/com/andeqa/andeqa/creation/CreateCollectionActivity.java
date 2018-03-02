package com.andeqa.andeqa.creation;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
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
import android.widget.TextView;
import android.widget.Toast;

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.R;
import com.andeqa.andeqa.home.NavigationDrawerActivity;
import com.andeqa.andeqa.models.Cinggulan;
import com.andeqa.andeqa.models.Collection;
import com.andeqa.andeqa.models.Post;
import com.andeqa.andeqa.models.TransactionDetails;
import com.andeqa.andeqa.profile.PersonalProfileActivity;
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

import java.util.Date;
import java.util.Random;

import butterknife.Bind;
import butterknife.ButterKnife;

import static android.R.attr.editable;
import static android.os.Build.VERSION_CODES.N;

public class CreateCollectionActivity extends AppCompatActivity implements View.OnClickListener{
    @Bind(R.id.collectionNameEditText)EditText mCollectionNameEditText;
    @Bind(R.id.collectionNoteEditText)EditText mCollectionNoteEditText;
    @Bind(R.id.doneTextView)TextView mDoneTextView;
    @Bind(R.id.noteCountTextView)TextView mNoteCountTextView;
    @Bind(R.id.nameCountTextView)TextView mNameCountTextView;
    @Bind(R.id.collectionCoverImageView)ImageView mCollectionCoverImageView;
    @Bind(R.id.collectionCoverTextView)TextView mCollectionCoverTextView;

    private String image;
    private Uri photoUri;
    private static final String KEY_IMAGE = "IMAGE FROM GALLERY";
    private static final String TAG = "CreatePostActivity";
    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;

    private static final int DEFAULT_TITLE_LENGTH_LIMIT = 25;
    private static final int DEFAULT_DESCRIPTION_LENGTH_LIMIT = 100;
    private static final int IMAGE_GALLERY_REQUEST = 112;
    private Toolbar toolbar;

    //FIRESTORE
    private FirebaseFirestore firebaseFirestore;
    private CollectionReference postsCollection;
    private CollectionReference ownersReference;
    private CollectionReference usersReference;
    private DatabaseReference postReference;
    private CollectionReference collectionCollection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_collection);
        ButterKnife.bind(this);

        mDoneTextView.setOnClickListener(this);
        mCollectionCoverTextView.setOnClickListener(this);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


        firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() != null){

            //initialize firestore
            firebaseFirestore =  FirebaseFirestore.getInstance();
            //get the reference to posts(collection reference)
            postsCollection = firebaseFirestore.collection(Constants.POSTS);
            ownersReference = firebaseFirestore.collection(Constants.POST_OWNERS);
            usersReference = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);
            collectionCollection = FirebaseFirestore.getInstance().collection(Constants.COLLECTION);

            //firebase
            postReference = FirebaseDatabase.getInstance().getReference(Constants.POSTS);

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

    private void  createCollection(){
        progressDialog.show();
        final DatabaseReference collectionRef= postReference.push();
        final String collectionId = collectionRef.getKey();

        //firebase push id to organise post according to time
        final DatabaseReference reference = postReference.push();
        final String pushId = reference.getKey();

        collectionCollection.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot documentSnapshots) {
                Collection collection = new Collection();
                final long timeStamp = new Date().getTime();

                final int count = documentSnapshots.getDocuments().size();
                //save the collection
                collection.setType("collection");
                collection.setName(mCollectionNameEditText.getText().toString().trim());
                collection.setNote(mCollectionNoteEditText.getText().toString().trim());
                collection.setNumber(count + 1);
                collection.setUid(firebaseAuth.getCurrentUser().getUid());
                collection.setPushId(collectionId);
                collection.setTime(timeStamp);
                collectionCollection.document(collectionId).set(collection);


                //set the single ownership
                TransactionDetails transactionDetails = new TransactionDetails();
                transactionDetails.setPushId(collectionId);
                transactionDetails.setUid(firebaseAuth.getCurrentUser().getUid());
                transactionDetails.setTime(timeStamp);
                transactionDetails.setType("owner");
                transactionDetails.setAmount(0.0);
                transactionDetails.setWalletBalance(0.0);
                ownersReference.document(collectionId).set(transactionDetails);

                if (photoUri != null){
                    StorageReference storageReference = FirebaseStorage
                            .getInstance().getReference()
                            .child(Constants.POSTS)
                            .child(pushId);

                    UploadTask uploadTask = storageReference.putFile(photoUri);
                    uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            final Uri downloadUrl = taskSnapshot.getDownloadUrl();

                            CollectionReference cl = postsCollection;
                            cl.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                @Override
                                public void onSuccess(QuerySnapshot documentSnapshots) {

                                    mCollectionNameEditText.setText("");
                                    mCollectionNoteEditText.setText("");

                                    Intent intent = new Intent(CreateCollectionActivity.this, PersonalProfileActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    finish();
                                }
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(CreateCollectionActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                            progressDialog.setMessage("Creating your collection" + " " + ((int) progress) + "%...");
                            if (progress == 100.0){
                                progressDialog.dismiss();
                                //reset input fields


                            }
                        }
                    });

                }else {
                    progressDialog.dismiss();
                    mCollectionNameEditText.setText("");
                    mCollectionNoteEditText.setText("");
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
                    mCollectionCoverTextView.setVisibility(View.GONE);
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

        if (v == mDoneTextView){
            createCollection();
        }

        if (v == mCollectionCoverTextView){
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, IMAGE_GALLERY_REQUEST);        }
    }

}



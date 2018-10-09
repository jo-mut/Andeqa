package com.andeqa.andeqa.profile;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewAnimator;

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.R;
import com.andeqa.andeqa.camera.PicturesActivity;
import com.andeqa.andeqa.models.Andeqan;
import com.andeqa.andeqa.registration.CreateProfileActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;

import javax.annotation.Nullable;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;


public class UpdateProfileActivity extends AppCompatActivity implements
        View.OnClickListener {
    @Bind(R.id.profileImageView)CircleImageView mProfilePictureImageView;
    @Bind(R.id.usernameEditText) EditText mUsernameEditText;
    @Bind(R.id.bioEditText)EditText mBioEditText;
    @Bind(R.id.fisrtNameEditText)EditText mFirstNameEditText;
    @Bind(R.id.secondNameEditText)EditText mSecondNameEditText;
    @Bind(R.id.animator)ViewAnimator viewAnimator;
    @Bind(R.id.profileCoverImageView)ImageView mProfileCoverImageView;
    @Bind(R.id.profilePhotoImageButton)ImageButton mUpdateProfileImageButton;
    @Bind(R.id.updateCoverTextView)TextView mUpdateCoverTextView;
    @Bind(R.id.doneEditingImageView)ImageView mDoneEditingImageView;

    private static  final int GALLERY_PROFILE_PHOTO_REQUEST = 111;
    private static final int GALLERY_PROFILE_COVER_PHOTO = 222;
    private static final String TAG = UpdateProfileActivity.class.getSimpleName();
    //firebase auth
    private FirebaseUser firebaseUser;
    private FirebaseAuth firebaseAuth;
    //firestore
    private CollectionReference usersReference;
    private ProgressDialog progressDialog;
    private static final int MAX_WIDTH = 200;
    private static final int MAX_HEIGHT = 200;
    private static final int MAX_COVER_WIDTH = 400;
    private static final int MAX_COVER_HEIGHT = 400;
    private static final int DEFAULT_TITLE_LENGTH_LIMIT = 250;

    //intent extras
    private String profileCoverIntent;
    private String profilePhotoIntent;
    private static final String PROFILE_PHOTO_PATH = "profile photo path";
    private static final String PROFILE_COVER_PATH = "profile cover path";
    private static final String GALLERY_PATH ="gallery image";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);
        ButterKnife.bind(this);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationIcon(R.drawable.ic_arrow);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        usersReference = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);
        updateProfileProgessDialog();
        mBioEditText.setFilters(new InputFilter[]{new InputFilter
                .LengthFilter(DEFAULT_TITLE_LENGTH_LIMIT)});
        loadProfileCover();
        loadProfileImage();
        mUsernameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(final Editable editable) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        uniqueUsernameName(editable.toString());
                    }
                }, 500);
            }
        });
        mUpdateProfileImageButton.setOnClickListener(this);
        mUpdateCoverTextView.setOnClickListener(this);
        mDoneEditingImageView.setOnClickListener(this);

    }

    private void loadProfileImage(){
        profilePhotoIntent = getIntent().getStringExtra(PROFILE_PHOTO_PATH);
        if (profilePhotoIntent != null){
            Glide.with(this)
                    .asBitmap()
                    .load(new File(profilePhotoIntent))
                    .into(mProfilePictureImageView);
        }
    }

    private void loadProfileCover(){
        profileCoverIntent = getIntent().getStringExtra(PROFILE_COVER_PATH);
        if (profileCoverIntent != null){
            Glide.with(this)
                    .asBitmap()
                    .load(new File(profileCoverIntent))
                    .into(mProfileCoverImageView);
        }
    }


    private void uniqueUsernameName(final String name){
        usersReference.whereEqualTo("username", name)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable final QuerySnapshot documentSnapshots,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen error", e);
                            return;
                        }

                        if (!documentSnapshots.isEmpty()){
                            for (DocumentChange change : documentSnapshots.getDocumentChanges()){
                                Andeqan andeqan = change.getDocument().toObject(Andeqan.class);
                                if (andeqan.getUser_id().equals(name) && andeqan.getUser_id()
                                        .equals(firebaseAuth.getCurrentUser().getUid())){
                                    Toast toast = Toast.makeText(UpdateProfileActivity.this,"Username available",
                                            Toast.LENGTH_SHORT);
                                    toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 0);
                                    toast.show();
                                }else {
                                    Toast toast = Toast.makeText(UpdateProfileActivity.this,"Username has been taken",
                                            Toast.LENGTH_SHORT);
                                    toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 0);
                                    toast.show();
                                }
                            }

                        }else {
                            Toast toast = Toast.makeText(UpdateProfileActivity.this,"Username available",
                                    Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 0);
                            toast.show();
                        }
                    }
                });

    }

    @Override
    public void onClick(View v){
        if(v == mUpdateProfileImageButton){
            Intent intent = new Intent(UpdateProfileActivity.this, PicturesActivity.class);
            intent.putExtra(UpdateProfileActivity.PROFILE_PHOTO_PATH, UpdateProfileActivity.class.getSimpleName());
            startActivity(intent);
            finish();
        }

        if (v ==  mDoneEditingImageView){
            updateUsernameAndBio();

            if (profilePhotoIntent != null){
                updateProfilePhoto();
            }

            if (profileCoverIntent != null){
                updateCoverPhoto();
            }

        }


        if (v == mUpdateCoverTextView){
            Intent intent = new Intent(UpdateProfileActivity.this, PicturesActivity.class);
            intent.putExtra(UpdateProfileActivity.PROFILE_COVER_PATH, UpdateProfileActivity.class.getSimpleName());
            startActivity(intent);
            finish();
        }
    }

    public void updateProfileProgessDialog(){
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Updating your profile...");
        progressDialog.setCancelable(true);
    }


    /**update profile photo*/
    public void updateProfilePhoto(){
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final String uid = user.getUid();

        if ( profilePhotoIntent!= null){
            progressDialog.show();
            mProfilePictureImageView.setDrawingCacheEnabled(true);
            Bitmap bitmap = ((BitmapDrawable) mProfilePictureImageView.getDrawable()).getBitmap();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();

            if (data != null){

                final StorageReference storageReference = FirebaseStorage
                        .getInstance().getReference()
                        .child("profile images")
                        .child(uid);

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

                            final String profileImage = (downloadUri.toString());

                            DocumentReference imageRef = usersReference.document(firebaseUser.getUid());
                            imageRef.update("profile_image", profileImage);
                            mProfilePictureImageView.setImageBitmap(null);
                            progressDialog.dismiss();


                        } else {
                            // Handle failures
                            // ...
                        }
                    }
                });

            }
        }

    }

    /**update profile cover photo*/
    public void updateCoverPhoto(){
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final String uid = user.getUid();

        if (profileCoverIntent != null){
            progressDialog.show();
            mProfileCoverImageView.setDrawingCacheEnabled(true);
            Bitmap bitmap = ((BitmapDrawable) mProfileCoverImageView.getDrawable()).getBitmap();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();


            if (data != null){

                final StorageReference storageReference = FirebaseStorage
                        .getInstance().getReference()
                        .child("profile cover")
                        .child(uid);

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

                            final String profileCoverPhoto = (downloadUri.toString());

                            DocumentReference imageRef = usersReference.document(firebaseUser.getUid());
                            imageRef.update("profile_cover", profileCoverPhoto);
                            mProfilePictureImageView.setImageBitmap(null);
                            progressDialog.dismiss();


                        } else {
                            // Handle failures
                            // ...
                        }
                    }
                });

            }

        }
    }

    /**update profile edit text fields*/
    private void updateUsernameAndBio(){
        final String username = (mUsernameEditText.getText().toString().toLowerCase());
        final String bio = (mBioEditText.getText().toString());
        final String firstName = (mFirstNameEditText.getText().toString());
        final String secondName = (mSecondNameEditText.getText().toString());

        if (!TextUtils.isEmpty(username)){
            usersReference.whereEqualTo("username", username)
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot documentSnapshots,
                                    @Nullable FirebaseFirestoreException e) {
                    if (e != null) {
                        Log.w(TAG, "Listen error", e);
                        return;
                    }

                    if (!documentSnapshots.isEmpty()){
                        for (DocumentChange change : documentSnapshots.getDocumentChanges()){
                            Andeqan andeqan = change.getDocument().toObject(Andeqan.class);
                            if (!andeqan.getUser_id().equals(username) && andeqan.getUser_id()
                                    .equals(firebaseAuth.getCurrentUser().getUid())){
                                DocumentReference usernameRef = usersReference.document(firebaseUser.getUid());
                                usernameRef.update("username", username);
                            }
                        }

                    }else {
                        DocumentReference usernameRef = usersReference.document(firebaseUser.getUid());
                        usernameRef.update("username", username);
                        mUsernameEditText.setText("");
                        Toast.makeText(UpdateProfileActivity.this,
                                "Successfully updated", Toast.LENGTH_SHORT).show();
                    }

                }
            });

        }
        if(!TextUtils.isEmpty(bio)){
            DocumentReference bioRef = usersReference.document(firebaseUser.getUid());
            bioRef.update("bio", bio);
            mBioEditText.setText("");
            Toast.makeText(UpdateProfileActivity.this,
                    "Successfully updated", Toast.LENGTH_SHORT).show();
        }

        if (!TextUtils.isEmpty(firstName)){
            DocumentReference firstNameRef = usersReference.document(firebaseUser.getUid());
            firstNameRef.update("first_name", firstName);
            mFirstNameEditText.setText("");
            Toast.makeText(UpdateProfileActivity.this,
                    "Successfully updated", Toast.LENGTH_SHORT).show();
        }

        if (!TextUtils.isEmpty(secondName)){
            DocumentReference secondNameRef = usersReference.document(firebaseUser.getUid());
            secondNameRef.update("second_name", secondName);
            mSecondNameEditText.setText("");
            Toast.makeText(UpdateProfileActivity.this,
                    "Successfully updated", Toast.LENGTH_SHORT).show();
        }
    }

//
//     private void updateEmailAndPassword(){
//         final String password = (mChangePasswordEditText.getText().toString());
//         final String email = (mChangeEmailEditText.getText().toString());
//        if (!TextUtils.isEmpty(password)){
//            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//
//            user.updatePassword(mChangePasswordEditText.getText().toString().trim())
//                    .addOnCompleteListener(new OnCompleteListener<Void>() {
//                        @Override
//                        public void onComplete(@NonNull Task<Void> task) {
//                            if (task.isSuccessful()) {
//                                Toast.makeText(UpdateProfileActivity.this, "Password is updated!", Toast.LENGTH_SHORT).show();
//                            } else {
//                                Toast.makeText(UpdateProfileActivity.this, "Failed to update password!", Toast.LENGTH_SHORT).show();
//
//                            }
//                        }
//                    });
//
//        }
//
//        if (!TextUtils.isEmpty(email)){
//            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//
//            user.updateEmail(mChangeEmailEditText.getText().toString().trim())
//                    .addOnCompleteListener(new OnCompleteListener<Void>() {
//                        @Override
//                        public void onComplete(@NonNull Task<Void> task) {
//                            if (task.isSuccessful()) {
//                                Toast.makeText(UpdateProfileActivity.this, "Email address is updated.", Toast.LENGTH_LONG).show();
//                            } else {
//                                Toast.makeText(UpdateProfileActivity.this, "Failed to update email!", Toast.LENGTH_LONG).show();
//                            }
//                        }
//                    });
//        }
//    }

}

package com.cinggl.cinggl.profile;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewAnimator;

import com.cinggl.cinggl.Constants;
import com.cinggl.cinggl.R;
import com.cinggl.cinggl.home.HomeActivity;
import com.cinggl.cinggl.home.NavigationDrawerActivity;
import com.cinggl.cinggl.models.Cingulan;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;



public class SetupPorifleInfo extends AppCompatActivity implements View.OnClickListener{

    //DATABASE REFERENCES
    private DatabaseReference usersRef;
    //BIND VIEWS
    @Bind(R.id.fisrtNameEditText)EditText mFirstNameEditText;
    @Bind(R.id.secondNameEditText)EditText mSecondNameEditText;
    @Bind(R.id.usernameEditText) EditText mUsernameEditText;
    @Bind(R.id.bioEditText)EditText mBioEditText;
    @Bind(R.id.createInfoButton)Button mCreateInfoButton;
    @Bind(R.id.profilePictureImageView)ImageView mProfilePictureImageView;
    @Bind(R.id.updateProfilePictureImageButton)ImageButton mUpdateProfileImageButton;

    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    private Uri imageUri;
    private static final int MAX_WIDTH = 400;
    private static final int MAX_HEIGHT = 400;
    private static  final int GALLERY_PROFILE_PHOTO_REQUEST = 111;
    private ViewAnimator viewAnimator;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_porifle_info);
        ButterKnife.bind(this);

        mCreateInfoButton.setOnClickListener(this);
        mUpdateProfileImageButton.setOnClickListener(this);

        usersRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_USERS);

        firebaseAuth = FirebaseAuth.getInstance();
        profileProgressDialog();

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK) {
            //update the profile photo
            if (requestCode == GALLERY_PROFILE_PHOTO_REQUEST) {
                imageUri = data.getData();

                Picasso.with(SetupPorifleInfo.this)
                        .load(imageUri)
                        .resize(MAX_WIDTH, MAX_HEIGHT)
                        .onlyScaleDown()
                        .centerCrop()
                        .into(mProfilePictureImageView, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError() {
                                Picasso.with(SetupPorifleInfo.this)
                                        .load(imageUri)
                                        .resize(MAX_WIDTH, MAX_HEIGHT)
                                        .onlyScaleDown()
                                        .centerCrop()
                                        .into(mProfilePictureImageView);

                            }
                        });


            }

        }
    }

    private boolean isValidName(String name) {
        if (name.equals("")) {
            mUsernameEditText.setError("Please enter a valid name!");
            return false;
        }
        return true;
    }

    private boolean isValidFirstName(String firstName) {
        if (firstName.equals("")) {
            mFirstNameEditText.setError("Please enter a valid name!");
            return false;
        }
        return true;
    }

    private boolean isValidSecondName(String secondName) {
        if (secondName.equals("")) {
            mSecondNameEditText.setError("Please enter a valid name!");
            return false;
        }
        return true;
    }


    private void profileProgressDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Loading...");
        progressDialog.setMessage("Creating your profile...");
        progressDialog.setCancelable(false);
    }

    public void updateProfilePhoto(){
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final String uid = user.getUid();

        usersRef= FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_USERS)
                .child(uid);

//        progressDialog.show();

        final String username = mUsernameEditText.getText().toString().toLowerCase().trim();
        final String firstName = mFirstNameEditText.getText().toString().trim();
        final String secondName = mSecondNameEditText.getText().toString().trim();
        final String bio = mBioEditText.getText().toString().trim();


        if (imageUri != null){
            StorageReference storageReference = FirebaseStorage
                    .getInstance().getReference()
                    .child("profile images")
                    .child(uid);

            StorageReference filePath = storageReference.child(imageUri.getLastPathSegment());
            filePath.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    String downloadUrl = taskSnapshot.getDownloadUrl().toString();

                    final String profileImage = (downloadUrl.toString());

                    mProfilePictureImageView.setImageBitmap(null);

                    Cingulan cingulan = new Cingulan();
                    cingulan.setFirstName(firstName);
                    cingulan.setSecondName(secondName);
                    cingulan.setUsername(username);
                    cingulan.setBio(bio);
                    cingulan.setUid(uid);
                    cingulan.setProfileImage(profileImage);

                    DatabaseReference pushRef = usersRef;
                    String pushId = pushRef.getKey();
                    pushRef.setValue(cingulan).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
//                                progressDialog.dismiss();
                            }

                        }
                    });

                }
            });

        }

    }


    @Override
    public void onClick(View v){
        if (v == mCreateInfoButton){

            boolean validName = isValidName(mUsernameEditText.getText().toString());
            boolean validFirstName = isValidFirstName(mFirstNameEditText.getText().toString());
            boolean validSecondName = isValidSecondName(mSecondNameEditText.getText().toString());

            if (!validName|| !validFirstName || !validSecondName) return;

            updateProfilePhoto();

            Toast.makeText(SetupPorifleInfo.this, "Your profile info has been saved", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(SetupPorifleInfo.this, NavigationDrawerActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }

        if (v == mUpdateProfileImageButton){
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, GALLERY_PROFILE_PHOTO_REQUEST);
        }
    }

}

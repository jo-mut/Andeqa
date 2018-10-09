package com.andeqa.andeqa.registration;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.R;
import com.andeqa.andeqa.main.HomeActivity;
import com.andeqa.andeqa.models.Andeqan;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Date;

import javax.annotation.Nullable;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class SaveGoogleProfileActivity extends AppCompatActivity implements View.OnClickListener{
    @Bind(R.id.fisrtNameEditText)EditText mFirstNameEditText;
    @Bind(R.id.secondNameEditText)EditText mSecondNameEditText;
    @Bind(R.id.usernameEditText) EditText mUsernameEditText;
    @Bind(R.id.profileImageView)CircleImageView mProfileImageView;
    @Bind(R.id.profilePhotoImageButton)ImageButton mUpdateProfilePictureImageButton;
    @Bind(R.id.submitUserInfoButton)Button mSubmitUserInfoButton;


    private static final String TAG = CreateProfileActivity.class.getSimpleName();
    private CollectionReference usersReference;
    private CollectionReference walletReference;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog mAuthProgressDialog;
    private ProgressDialog verifyingProgressDialog;
    private ProgressDialog createProfileProgressDialog;
    private static  final int GALLERY_PROFILE_PHOTO_REQUEST = 111;
    private static final int GALLERY_PROFILE_COVER_PHOTO = 222;
    private static final int MAX_WIDTH = 200;
    private static final int MAX_HEIGHT = 200;
    private static final int MAX_COVER_WIDTH = 400;
    private static final int MAX_COVER_HEIGHT = 400;
    private static final String EMAIL = "email";
    private Uri profileUri;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save_google_profile);
        ButterKnife.bind(this);

        firebaseAuth = FirebaseAuth.getInstance();


        if (firebaseAuth != null){
            usersReference = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);
            walletReference = FirebaseFirestore.getInstance().collection(Constants.WALLET);

            createAuthProgressDialog();
            verifyingYourEmailDialog();
            createProfileDialog();

            //permission
            int version = Build.VERSION.SDK_INT;
            if (version > Build.VERSION_CODES.LOLLIPOP_MR1){
                if (!checkIfAlreadyHavePermission()){
                    requestForSpecificPermission();
                }
            }

            mUpdateProfilePictureImageButton.setOnClickListener(this);
            mSubmitUserInfoButton.setOnClickListener(this);

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
        }
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



    private void uniqueUsernameName(final String name){
        usersReference.whereEqualTo("username", name)
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
                                if (andeqan.getUser_id().equals(name) && andeqan.getUser_id()
                                        .equals(firebaseAuth.getCurrentUser().getUid())){
                                    Toast toast = Toast.makeText(SaveGoogleProfileActivity.this,"Username available",
                                            Toast.LENGTH_SHORT);
                                    toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 0);
                                    toast.show();
                                }else {
                                    Toast toast = Toast.makeText(SaveGoogleProfileActivity.this,"Username has been taken",
                                            Toast.LENGTH_SHORT);
                                    toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 0);
                                    toast.show();
                                }
                            }
                        }else {
                            Toast toast = Toast.makeText(SaveGoogleProfileActivity.this,"Username available",
                                    Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 0);
                            toast.show();
                        }
                    }
                });

    }


    private void createAuthProgressDialog() {
        mAuthProgressDialog = new ProgressDialog(this);
        mAuthProgressDialog.setMessage("Signing in");
        mAuthProgressDialog.setCancelable(false);
    }

    private void verifyingYourEmailDialog() {
        verifyingProgressDialog = new ProgressDialog(this);
        verifyingProgressDialog.setMessage("Authenticating your sign in details");
        verifyingProgressDialog.setCancelable(false);
    }

    private void createProfileDialog() {
        createProfileProgressDialog = new ProgressDialog(this);
        createProfileProgressDialog.setMessage("Updating your profile info");
        createProfileProgressDialog.setCancelable(false);
    }


    //get user input and submit info
    public void createProfile(){
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final String uid = user.getUid();
        final String email = user.getEmail();

        final String deviceId = FirebaseInstanceId.getInstance().getToken();


        final String username = mUsernameEditText.getText().toString().toLowerCase().trim();
        final String firstName = mFirstNameEditText.getText().toString().trim();
        final String secondName = mSecondNameEditText.getText().toString().trim();

        boolean validName = isValidName(mUsernameEditText.getText().toString());
        boolean validFirstName = isValidFirstName(mFirstNameEditText.getText().toString());
        boolean validSecondName = isValidSecondName(mSecondNameEditText.getText().toString());

        if (!validName|| !validFirstName || !validSecondName) return;

        Andeqan andeqan = new Andeqan();
        andeqan.setFirst_name(firstName);
        andeqan.setSecond_name(secondName);
        andeqan.setUsername(username);
        andeqan.setUser_id(uid);
        andeqan.setEmail(email);
        andeqan.setDevice_id(deviceId);

        createProfileProgressDialog.dismiss();
        usersReference.document(uid).set(andeqan).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                //move to next actvity even if the user doesnt submit a profile image
                if (profileUri != null){
                    StorageReference storageRef = FirebaseStorage
                            .getInstance().getReference()
                            .child("profile images")
                            .child(uid);

                    final StorageReference path = storageRef.child(profileUri.getLastPathSegment());
                    UploadTask uploadTask = storageRef.putFile(profileUri);
                    Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                        @Override
                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                            if (!task.isSuccessful()) {
                                throw task.getException();
                            }

                            // Continue with the task to get the download URL
                            return path.getDownloadUrl();
                        }
                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()) {
                                final Uri downloadUri = task.getResult();

                                final String profileImage = (downloadUri.toString());
                                Log.d("profile image", profileImage);
                                usersReference.document(uid).update("profile_image", profileImage);
                                Intent intent = new Intent(SaveGoogleProfileActivity.this, HomeActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();

                            } else {
                                // Handle failures
                                // ...
                            }
                        }
                    });

                }

            }
        });


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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK) {
            //update the profile photo
            if (requestCode == GALLERY_PROFILE_PHOTO_REQUEST) {
                profileUri = data.getData();
//                mProfileImageView.setImageURI(imageUri);
                Glide.with(this)
                        .asBitmap()
                        .load(profileUri)
                        .into(mProfileImageView);

            }

        }
    }


    @Override
    public void onClick(View v){

        if (v == mUpdateProfilePictureImageButton){
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, GALLERY_PROFILE_PHOTO_REQUEST);
        }

        if (v == mSubmitUserInfoButton){
            if (firebaseAuth.getCurrentUser() != null){
                usersReference.whereEqualTo("username", mUsernameEditText.getText().toString())
                        .addSnapshotListener(new EventListener<QuerySnapshot>() {
                            @Override
                            public void onEvent(@Nullable QuerySnapshot documentSnapshots, @Nullable FirebaseFirestoreException e) {


                                if (!documentSnapshots.isEmpty()){
                                    Toast.makeText(SaveGoogleProfileActivity.this,
                                            "Please chooser a username that has not been taken",
                                            Toast.LENGTH_SHORT).show();
                                }else {
                                    createProfile();
                                }

                            }
                        });
            }
        }

    }


}

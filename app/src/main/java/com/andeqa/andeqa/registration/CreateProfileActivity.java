package com.andeqa.andeqa.registration;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.R;
import com.andeqa.andeqa.home.NavigationDrawerActivity;
import com.andeqa.andeqa.models.Andeqan;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class CreateProfileActivity extends AppCompatActivity implements View.OnClickListener{
    @Bind(R.id.fisrtNameEditText)EditText mFirstNameEditText;
    @Bind(R.id.secondNameEditText)EditText mSecondNameEditText;
    @Bind(R.id.usernameEditText) EditText mUsernameEditText;
    @Bind(R.id.profileImageView)CircleImageView mProfilePictureImageView;
    @Bind(R.id.profilePhotoImageButton)ImageButton mUpdateProfilePictureImageButton;
    @Bind(R.id.submitUserInfoButton)Button mSubmitUserInfoButton;
    @Bind(R.id.errorRelativeLayout)RelativeLayout mErrorRelativeLayout;
    @Bind(R.id.errorTextView)TextView mErrorTextView;
    @Bind(R.id.resendLinkRelativeLayout)RelativeLayout mResendLinkRelativeLayout;

    private static final String TAG = CreateProfileActivity.class.getSimpleName();
    private CollectionReference usersReference;
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
    private Uri profileUri;
    private Uri imageUri;
    private Boolean flag;
    private String email;
    private String password;
    private String code;
    private static final String PASSWORD = "password";
    private static final String EMAIL = "email";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_profile);
        ButterKnife.bind(this);

        firebaseAuth = FirebaseAuth.getInstance();


        if (firebaseAuth != null){
            usersReference = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);

            email = getIntent().getStringExtra(EMAIL);
            password = getIntent().getStringExtra(PASSWORD);

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
            mResendLinkRelativeLayout.setOnClickListener(this);
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

    private void loginWithPassword() {
         if (!TextUtils.isEmpty(mFirstNameEditText.getText().toString()) &&
              !TextUtils.isEmpty(mSecondNameEditText.getText().toString()) &&
              !TextUtils.isEmpty(mUsernameEditText.getText().toString())){

              mAuthProgressDialog.show();
              firebaseAuth.signInWithEmailAndPassword(email, password)
                  .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                      @Override
                      public void onComplete(@NonNull Task<AuthResult> task) {
                          Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());
                          mAuthProgressDialog.dismiss();
                          if (!task.isSuccessful()) {
                              Log.w(TAG, "signInWithEmail", task.getException());

                              mErrorRelativeLayout.setVisibility(View.VISIBLE);
                              mErrorTextView.setText("Please confirm that your email and password match you are connected to the internet");

                              mErrorRelativeLayout.postDelayed(new Runnable() {
                                  public void run() {
                                      mErrorRelativeLayout.setVisibility(View.GONE);
                                  }
                              }, 5000);


                          }else {
                              checkIfImailVerified();
                          }
                      }
                  });

        }
    }



    public void checkIfImailVerified(){
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser.isEmailVerified()){
            createProfileProgressDialog.show();
            createProfile();
            //user is verified sp you can finish this activity or send user to activity you want

        }else {
            //email is not verified so just prompt the massge to the user and restart this activity
            FirebaseAuth.getInstance().signOut();
            mErrorRelativeLayout.setVisibility(View.VISIBLE);
            mErrorTextView.setText("Check that you have confirmed your email");

            mErrorRelativeLayout.postDelayed(new Runnable() {
                public void run() {
                    mErrorRelativeLayout.setVisibility(View.GONE);
                }
            }, 5000);

            mResendLinkRelativeLayout.setVisibility(View.VISIBLE);

        }
    }

    private void sendVerificationEmail(){
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        firebaseUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    //email sent
                    Toast.makeText(CreateProfileActivity.this, "Confirm your email! Verification email successfully sent to" + " " +
                            firebaseUser.getEmail(), Toast.LENGTH_LONG).show();
                    FirebaseAuth.getInstance().signOut();

                }else {
                    //email not sent, so display a message and restart the activity and restart this activity
                    overridePendingTransition(0,0);
                    finish();
                    overridePendingTransition(0,0);
                    startActivity(getIntent());
                    new AlertDialog.Builder(CreateProfileActivity.this)
                            .setMessage("Andeqa could not send verification email, please confirm that you " +
                                    "entered the right email and check your internet connection")
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            }).setIcon(android.R.drawable.ic_dialog_alert).show();
                }
            }
        });
    }

    //get user input and submit info
    public void createProfile(){
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final String uid = user.getUid();

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
        usersReference.document(uid).set(andeqan)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                //move to next actvity even if the user doesnt submit a profile image
                if (profileUri != null){
                    StorageReference storageRef = FirebaseStorage
                            .getInstance().getReference()
                            .child("profile images")
                            .child(uid);

                    StorageReference path = storageRef.child(profileUri.getLastPathSegment());
                    path.putFile(profileUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            String downloadUrl = taskSnapshot.getDownloadUrl().toString();

                            final String profileImage = (downloadUrl.toString());
                            Log.d("profile image", profileImage);
                            usersReference.document(uid).update("profile_image", profileImage);

                        }
                    });
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(CreateProfileActivity.this, "Profile successfully updated",
                        Toast.LENGTH_SHORT).show();
            }
        });

        //the user is already logged in so create profile and move to next activity
        Intent intent = new Intent(CreateProfileActivity.this, NavigationDrawerActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();

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

    private boolean isValidPicture(){
        if (profileUri == null){
            mErrorRelativeLayout.setVisibility(View.VISIBLE);
            mErrorTextView.setText("Please add your profile picture so other Cinggulans can see you."
                    + " " + "You can change your profile picture later");
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
//                mProfilePictureImageView.setImageURI(imageUri);

                Picasso.with(this)
                        .load(profileUri).resize(MAX_WIDTH, MAX_HEIGHT).onlyScaleDown()
                        .centerCrop()
                        .placeholder(R.drawable.ic_user)
                        .networkPolicy(NetworkPolicy.OFFLINE)
                        .into(mProfilePictureImageView, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError() {
                                Picasso.with(CreateProfileActivity.this)
                                        .load(profileUri).resize(MAX_WIDTH, MAX_HEIGHT)
                                        .onlyScaleDown().centerCrop()
                                        .placeholder(R.drawable.ic_user)
                                        .into(mProfilePictureImageView);

                            }
                        });

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
               checkIfImailVerified();
           }else {
               mAuthProgressDialog.show();
               loginWithPassword();
           }
        }

        if (v == mResendLinkRelativeLayout){
            firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());
                            mAuthProgressDialog.dismiss();
                            if (task.isSuccessful()) {
                                Log.w(TAG, "signInWithEmail", task.getException());
                                sendVerificationEmail();
                            }else {
                                mErrorRelativeLayout.setVisibility(View.VISIBLE);
                                mErrorTextView.setText("Check that you are connected to the internet");

                                mErrorRelativeLayout.postDelayed(new Runnable() {
                                    public void run() {
                                        mErrorRelativeLayout.setVisibility(View.GONE);
                                    }
                                }, 5000);

                            }
                        }
                    });

        }

    }



}

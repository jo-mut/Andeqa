package com.cinggl.cinggl.profile;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cinggl.cinggl.Constants;
import com.cinggl.cinggl.R;
import com.cinggl.cinggl.home.MainActivity;
import com.cinggl.cinggl.models.Cingulan;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
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
    @Bind(R.id.profilePictureImageView)CircleImageView mProfilePictureImageView;
    @Bind(R.id.profileCoverImageView)ImageView mProfileCoverImageView;
    @Bind(R.id.updateCoverTextView)TextView mUpdateCoverTextView;
    @Bind(R.id.updateProfilePictureImageButton)ImageButton mUpdateProfilePictureImageButton;
    @Bind(R.id.submitUserInfoButton)Button mSubmitUserInfoButton;

    private static final String TAG = CreateProfileActivity.class.getSimpleName();
    private CollectionReference usersReference;
    private FirebaseAuth mAuth;
    private ProgressDialog mAuthProgressDialog;
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
    private static final String PASSWORD = "password";
    private static final String EMAIL = "email";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_profile);
        ButterKnife.bind(this);

        mAuth = FirebaseAuth.getInstance();

       if (mAuth != null){
           usersReference = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);

           email = getIntent().getStringExtra(EMAIL);
           if(email == null){
               throw new IllegalArgumentException("pass an EXTRA_POST_KEY");
           }

           password = getIntent().getStringExtra(PASSWORD);
           if(password == null){
               throw new IllegalArgumentException("pass an EXTRA_POST_KEY");
           }

           createAuthProgressDialog();

           mUpdateCoverTextView.setOnClickListener(this);
           mUpdateProfilePictureImageButton.setOnClickListener(this);
           mSubmitUserInfoButton.setOnClickListener(this);
       }
    }


    private void createAuthProgressDialog() {
        mAuthProgressDialog = new ProgressDialog(this);
        mAuthProgressDialog.setTitle("Loading...");
        mAuthProgressDialog.setMessage("Updating your profile info");
        mAuthProgressDialog.setCancelable(false);
    }

    private void loginWithPassword() {
      if (!TextUtils.isEmpty(mFirstNameEditText.getText().toString()) &&
              !TextUtils.isEmpty(mSecondNameEditText.getText().toString()) &&
              !TextUtils.isEmpty(mUsernameEditText.getText().toString())){

          mAuth.signInWithEmailAndPassword(email, password)
                  .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                      @Override
                      public void onComplete(@NonNull Task<AuthResult> task) {
                          Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());
                          mAuthProgressDialog.dismiss();
                          if (!task.isSuccessful()) {
                              Log.w(TAG, "signInWithEmail", task.getException());
                              Toast.makeText(CreateProfileActivity.this, "Please confirm that your email and password match",
                                      Toast.LENGTH_SHORT).show();
                          }else {
                              checkIfImailVerified();
                          }
                      }
                  });

      }else {
          overridePendingTransition(0,0);
          finish();
          overridePendingTransition(0,0);
          startActivity(getIntent());

          Toast.makeText(CreateProfileActivity.this, "Submit your profile info",
                  Toast.LENGTH_LONG).show();
      }
    }



    public void checkIfImailVerified(){
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser.isEmailVerified()){

            createProfile();
            //user is verified sp you can finish this activity or send user to activity you want
            Toast.makeText(CreateProfileActivity.this, "You have Successfully signed in",
                    Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(CreateProfileActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();

        }else {
            //email is not verified so just prompt the massge to the user and restart this activity
            FirebaseAuth.getInstance().signOut();
            //restart this activity

            Toast.makeText(CreateProfileActivity.this, "Please check that you have confirmed your email",
                    Toast.LENGTH_LONG).show();

            overridePendingTransition(0,0);
            finish();
            overridePendingTransition(0,0);
            startActivity(getIntent());
        }
    }

    //get user input and submit info
    public void createProfile(){
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final String uid = user.getUid();

        final String username = mUsernameEditText.getText().toString().toLowerCase().trim();
        final String firstName = mFirstNameEditText.getText().toString().trim();
        final String secondName = mSecondNameEditText.getText().toString().trim();

        boolean validName = isValidName(mUsernameEditText.getText().toString());
        boolean validFirstName = isValidFirstName(mFirstNameEditText.getText().toString());
        boolean validSecondName = isValidSecondName(mSecondNameEditText.getText().toString());

        if (!validName|| !validFirstName || !validSecondName) return;

        mAuthProgressDialog.show();

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

                Cingulan cingulan = new Cingulan();
                cingulan.setFirstName(firstName);
                cingulan.setSecondName(secondName);
                cingulan.setUsername(username);
                cingulan.setUid(uid);
                cingulan.setProfileImage(profileImage);

                DocumentReference pushRef = usersReference.document(uid);
                String pushId = pushRef.getId();
                cingulan.setPushId(pushId);
                pushRef.set(cingulan).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("user profile created", "firstime");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(CreateProfileActivity.this, "Profile successfully updated", Toast.LENGTH_SHORT).show();
                    }
                });


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
            //update the profile cover photo
            if (requestCode == GALLERY_PROFILE_COVER_PHOTO){
                imageUri = data.getData();

                Picasso.with(this).load(imageUri).resize(MAX_COVER_WIDTH, MAX_COVER_HEIGHT)
                        .onlyScaleDown().centerCrop().placeholder(R.drawable.gradient_color)
                        .networkPolicy(NetworkPolicy.OFFLINE)
                        .into(mProfileCoverImageView, new Callback() {
                            @Override
                            public void onSuccess() {

                            }
                            @Override
                            public void onError() {
                                Picasso.with(CreateProfileActivity.this).load(imageUri)
                                        .resize(MAX_COVER_WIDTH, MAX_COVER_HEIGHT)
                                        .onlyScaleDown().centerCrop()
                                        .placeholder(R.drawable.profle_image_background)
                                        .into(mProfileCoverImageView);

                            }
                        });

            }

            //update the profile photo
            if (requestCode == GALLERY_PROFILE_PHOTO_REQUEST) {
                profileUri = data.getData();
//                mProfilePictureImageView.setImageURI(imageUri);

                Picasso.with(this)
                        .load(profileUri).resize(MAX_WIDTH, MAX_HEIGHT).onlyScaleDown()
                        .centerCrop().placeholder(R.drawable.profle_image_background)
                        .networkPolicy(NetworkPolicy.OFFLINE)
                        .into(mProfilePictureImageView, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError() {
                                Picasso.with(CreateProfileActivity.this)
                                        .load(profileUri).resize(MAX_WIDTH, MAX_HEIGHT)
                                        .onlyScaleDown().centerCrop().placeholder(R.drawable.profle_image_background)
                                        .into(mProfilePictureImageView);

                            }
                        });

            }

        }
    }


    @Override
    public void onClick(View v){
        if (v == mUpdateCoverTextView){
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, GALLERY_PROFILE_COVER_PHOTO);
        }

        if (v == mUpdateProfilePictureImageButton){
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, GALLERY_PROFILE_PHOTO_REQUEST);
        }

        if (v == mSubmitUserInfoButton){
           if (mAuth.getCurrentUser() != null){
               Intent intent = new Intent(CreateProfileActivity.this, MainActivity.class);
               intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
               startActivity(intent);
               finish();
           }else {
               loginWithPassword();
           }
        }
    }
}

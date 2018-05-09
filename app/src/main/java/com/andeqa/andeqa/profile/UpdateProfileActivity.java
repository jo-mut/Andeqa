package com.andeqa.andeqa.profile;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
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
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewAnimator;

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.R;
import com.andeqa.andeqa.models.Andeqan;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

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
    @Bind(R.id.updateProfilePictureImageButton)ImageButton mUpdateProfilePictureImageButton;
    @Bind(R.id.updateCoverTextView)TextView mUpdateCoverTextView;
    @Bind(R.id.statusCountTextView)TextView mStatusCountTextView;
    @Bind(R.id.doneEditingImageView)ImageView mDoneEditingImageView;

    private static  final int GALLERY_PROFILE_PHOTO_REQUEST = 111;
    private static final int GALLERY_PROFILE_COVER_PHOTO = 222;
    private static final String TAG = UpdateProfileActivity.class.getSimpleName();
    private Uri imageUri;
    private Uri profileUri;
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        if (firebaseAuth.getCurrentUser() != null){
            usersReference = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);

            updateProfileProgessDialog();
            mBioEditText.setFilters(new InputFilter[]{new InputFilter
                    .LengthFilter(DEFAULT_TITLE_LENGTH_LIMIT)});
            textWatchers();

            //permission
            int version = Build.VERSION.SDK_INT;
            if (version > Build.VERSION_CODES.LOLLIPOP_MR1){
                if (!checkIfAlreadyHavePermission()){
                    requestForSpecificPermission();
                }
            }

            mUpdateProfilePictureImageButton.setOnClickListener(this);
            mUpdateCoverTextView.setOnClickListener(this);
            mDoneEditingImageView.setOnClickListener(this);
        }

    }


    private void textWatchers(){
        //TITLE TEXT WATCHER
        mBioEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                int count = DEFAULT_TITLE_LENGTH_LIMIT - editable.length();
                mStatusCountTextView.setText(Integer.toString(count));

                if (count == 0){
                    mStatusCountTextView.setTextColor(Color.RED);
                }else if (count <= 250){
                    mStatusCountTextView.setTextColor(Color.BLACK);
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
                Manifest.permission.GET_ACCOUNTS, Manifest.permission.RECEIVE_SMS,
                Manifest.permission.READ_SMS, Manifest.permission.READ_EXTERNAL_STORAGE,
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



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.update_profile_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();



        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onClick(View v){

        if(v == mUpdateProfilePictureImageButton){
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, GALLERY_PROFILE_PHOTO_REQUEST);
        }

        if (v ==  mDoneEditingImageView){
            updateUsernameAndBio();

            Toast.makeText(UpdateProfileActivity.this, "Successfully updated", Toast.LENGTH_SHORT).show();
        }


        if (v == mUpdateCoverTextView){
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, GALLERY_PROFILE_COVER_PHOTO);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK) {
            //update the profile cover photo
            if (requestCode == GALLERY_PROFILE_COVER_PHOTO){
                imageUri = data.getData();

                Picasso.with(this).load(imageUri)
                        .resize(MAX_COVER_WIDTH, MAX_COVER_HEIGHT)
                        .onlyScaleDown().centerCrop()
                        .placeholder(R.drawable.default_gradient_color)
                        .networkPolicy(NetworkPolicy.OFFLINE)
                        .into(mProfileCoverImageView, new Callback() {
                            @Override
                            public void onSuccess() {

                            }
                            @Override
                            public void onError() {
                                Picasso.with(UpdateProfileActivity.this).load(imageUri)
                                        .resize(MAX_COVER_WIDTH, MAX_COVER_HEIGHT)
                                        .onlyScaleDown().centerCrop()
                                        .placeholder(R.drawable.profle_image_background)
                                        .into(mProfileCoverImageView);

                            }
                        });
                updateCoverPhoto();
            }

            //update the profile photo
            if (requestCode == GALLERY_PROFILE_PHOTO_REQUEST) {
                profileUri = data.getData();

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
                                Picasso.with(UpdateProfileActivity.this)
                                        .load(profileUri).resize(MAX_WIDTH, MAX_HEIGHT)
                                        .onlyScaleDown().centerCrop().placeholder(R.drawable.profle_image_background)
                                        .into(mProfilePictureImageView);

                            }
                        });
                updateProfilePhoto();
            }

        }
    }

    public void updateProfileProgessDialog(){
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Updating your profile...");
        progressDialog.setCancelable(true);
        progressDialog.getWindow().setLayout(100, 150);
    }


    public void updateProfilePhoto(){
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final String uid = user.getUid();
        progressDialog.show();

        if (profileUri != null){
            StorageReference storageReference = FirebaseStorage
                    .getInstance().getReference()
                    .child("profile images")
                    .child(uid);

            StorageReference filePath = storageReference.child(profileUri.getLastPathSegment());
            filePath.putFile(profileUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    String downloadUrl = taskSnapshot.getDownloadUrl().toString();

                    final String profileImage = (downloadUrl.toString());
                    Log.d("profile image", profileImage);

                    DocumentReference imageRef = usersReference.document(firebaseUser.getUid());
                    imageRef.update("profileImage", profileImage);
                    mProfilePictureImageView.setImageBitmap(null);

                    //progress dialog to show the retrieval of the update profile picture
//                    viewAnimator.setDisplayedChild(1);

                    usersReference.document(firebaseUser.getUid()).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                            if (documentSnapshot.exists()){
                                final Andeqan cinggulan = documentSnapshot.toObject(Andeqan.class);
                                final String profileImage = cinggulan.getProfile_image();
                                Log.d("profile image", profileImage);

                                Picasso.with(UpdateProfileActivity.this)
                                        .load(profileImage)
                                        .resize(MAX_WIDTH, MAX_HEIGHT)
                                        .onlyScaleDown()
                                        .centerCrop()
                                        .placeholder(R.drawable.profle_image_background)
                                        .networkPolicy(NetworkPolicy.OFFLINE)
                                        .into(mProfilePictureImageView, new Callback() {
                                            @Override
                                            public void onSuccess() {
//                                            viewAnimator.setDisplayedChild(0);
                                            }

                                            @Override
                                            public void onError() {
                                                Picasso.with(UpdateProfileActivity.this)
                                                        .load(profileImage)
                                                        .resize(MAX_WIDTH, MAX_HEIGHT)
                                                        .onlyScaleDown()
                                                        .centerCrop()
                                                        .placeholder(R.drawable.profle_image_background)
                                                        .into(mProfilePictureImageView);

                                            }
                                        });
                                progressDialog.dismiss();

                            }

                        }
                    });

                }
            });

        }

    }

    public void updateCoverPhoto(){
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final String uid = user.getUid();
        progressDialog.show();

        if (imageUri != null){
            StorageReference storageReference = FirebaseStorage
                    .getInstance().getReference()
                    .child("profile cover")
                    .child(uid);

            StorageReference filePath = storageReference.child(imageUri.getLastPathSegment());
            filePath.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    String downloadUrl = taskSnapshot.getDownloadUrl().toString();

                    final String profileCoverPhoto = (downloadUrl.toString());

                    DocumentReference imageRef = usersReference.document(firebaseUser.getUid());
                    imageRef.update("profileCover", profileCoverPhoto);
                    mProfilePictureImageView.setImageBitmap(null);

                    //progress dialog to show the retrieval of the update profile picture
                    usersReference.document(firebaseUser.getUid()).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                            if (documentSnapshot.exists()){
                                final Andeqan cinggulan = documentSnapshot.toObject(Andeqan.class);
                                final String profileCover= cinggulan.getProfile_cover();
                                Log.d("profile image", profileCover);

                                Picasso.with(UpdateProfileActivity.this)
                                        .load(profileCover).resize(MAX_WIDTH, MAX_HEIGHT)
                                        .onlyScaleDown().centerCrop().placeholder(R.drawable.default_gradient_color)
                                        .networkPolicy(NetworkPolicy.OFFLINE)
                                        .into(mProfileCoverImageView, new Callback() {
                                            @Override
                                            public void onSuccess() {
//                                            viewAnimator.setDisplayedChild(0);
                                            }

                                            @Override
                                            public void onError() {
                                                Picasso.with(UpdateProfileActivity.this)
                                                        .load(profileCover).resize(MAX_WIDTH, MAX_HEIGHT)
                                                        .onlyScaleDown().centerCrop()
                                                        .placeholder(R.drawable.default_gradient_color)
                                                        .into(mProfileCoverImageView);

                                            }
                                        });
                                progressDialog.dismiss();
                            }

                        }
                    });

                }
            });

        }

    }


    private void updateUsernameAndBio(){
        final String username = (mUsernameEditText.getText().toString());
        final String bio = (mBioEditText.getText().toString());
        final String firstName = (mFirstNameEditText.getText().toString());
        final String secondName = (mSecondNameEditText.getText().toString());

        if (!TextUtils.isEmpty(username)){
            DocumentReference usernameRef = usersReference.document(firebaseUser.getUid());
            usernameRef.update("username", username);
            mUsernameEditText.setText("");
        }
        if(!TextUtils.isEmpty(bio)){
            DocumentReference bioRef = usersReference.document(firebaseUser.getUid());
            bioRef.update("bio", bio);
            mBioEditText.setText("");
        }

        if (!TextUtils.isEmpty(firstName)){
            DocumentReference firstNameRef = usersReference.document(firebaseUser.getUid());
            firstNameRef.update("first_name", firstName);
            mFirstNameEditText.setText("");
        }

        if (!TextUtils.isEmpty(secondName)){
            DocumentReference secondNameRef = usersReference.document(firebaseUser.getUid());
            secondNameRef.update("second_name", secondName);
            mSecondNameEditText.setText("");
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

package com.cinggl.cinggl.profile;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.ViewAnimator;

import com.cinggl.cinggl.Constants;
import com.cinggl.cinggl.R;
import com.cinggl.cinggl.home.DeleteAccountDialog;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
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
    @Bind(R.id.profilePictureImageView)CircleImageView mProfilePictureImageView;
    @Bind(R.id.usernameEditText) EditText mUsernameEditText;
    @Bind(R.id.bioEditText)EditText mBioEditText;
    @Bind(R.id.fisrtNameEditText)EditText mFirstNameEditText;
    @Bind(R.id.secondNameEditText)EditText mSecondNameEditText;
    @Bind(R.id.deleteAccountRelativeLayout)RelativeLayout mDeleteAccountRelativeLayout;
    @Bind(R.id.animator)ViewAnimator viewAnimator;
    @Bind(R.id.profileCoverImageView)ImageView mProfileCoverImageView;
    @Bind(R.id.updateProfilePictureImageButton)ImageButton mUpdateProfilePictureImageButton;
    @Bind(R.id.updateProfileCoverImageButton)ImageView mUpdateProfileCoverImageView;

    private static  final int GALLERY_PROFILE_PHOTO_REQUEST = 111;
    private static final int GALLERY_PROFILE_COVER_PHOTO = 222;
    private static final String TAG = UpdateProfileActivity.class.getSimpleName();
    private Uri imageUri;
    private Uri profileUri;
    private FirebaseUser firebaseUser;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    private DatabaseReference usersRef;
    private ProgressDialog progressDialog;
    private static final int MAX_WIDTH = 300;
    private static final int MAX_HEIGHT = 300;
    private static final int MAX_COVER_WIDTH = 400;
    private static final int MAX_COVER_HEIGHT = 400;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDeleteAccountRelativeLayout.setOnClickListener(this);
        mUpdateProfileCoverImageView.setOnClickListener(this);
        mUpdateProfilePictureImageButton.setOnClickListener(this);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        usersRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_USERS)
                .child(firebaseAuth.getCurrentUser().getUid());

        updateProfileProgessDialog();

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

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

        if (id == R.id.action_done){
            updateUsernameAndBio();

            Toast.makeText(UpdateProfileActivity.this, "Successfully updated", Toast.LENGTH_SHORT).show();

        }

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

        if (v == mUpdateProfileCoverImageView){
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, GALLERY_PROFILE_COVER_PHOTO);
        }

        if (v == mDeleteAccountRelativeLayout){
            //delete your account permanently
            FragmentManager fragmenManager = getSupportFragmentManager();
            DeleteAccountDialog deleteAccountDialog = DeleteAccountDialog.newInstance("create your cingle");
            deleteAccountDialog.show(fragmenManager, "new post fragment");

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK) {
            //update the profile cover photo
            if (requestCode == GALLERY_PROFILE_COVER_PHOTO){
                imageUri = data.getData();

                Picasso.with(this)
                        .load(imageUri)
                        .resize(MAX_COVER_WIDTH, MAX_COVER_HEIGHT)
                        .onlyScaleDown()
                        .centerCrop()
                        .placeholder(R.drawable.profle_image_background)
                        .networkPolicy(NetworkPolicy.OFFLINE)
                        .into(mProfileCoverImageView, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError() {
                                Picasso.with(UpdateProfileActivity.this)
                                        .load(imageUri)
                                        .resize(MAX_COVER_WIDTH, MAX_COVER_HEIGHT)
                                        .onlyScaleDown()
                                        .centerCrop()
                                        .placeholder(R.drawable.profle_image_background)
                                        .into(mProfileCoverImageView);

                            }
                        });
                updateCoverPhoto();
                Toast.makeText(UpdateProfileActivity.this, "Successfully updated", Toast.LENGTH_SHORT).show();

            }

            //update the profile photo
            if (requestCode == GALLERY_PROFILE_PHOTO_REQUEST) {
                profileUri = data.getData();
//                mProfilePictureImageView.setImageURI(imageUri);

                Picasso.with(this)
                        .load(profileUri)
                        .resize(MAX_WIDTH, MAX_HEIGHT)
                        .onlyScaleDown()
                        .centerCrop()
                        .placeholder(R.drawable.profle_image_background)
                        .networkPolicy(NetworkPolicy.OFFLINE)
                        .into(mProfilePictureImageView, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError() {
                                Picasso.with(UpdateProfileActivity.this)
                                        .load(profileUri)
                                        .resize(MAX_WIDTH, MAX_HEIGHT)
                                        .onlyScaleDown()
                                        .centerCrop()
                                        .placeholder(R.drawable.profle_image_background)
                                        .into(mProfilePictureImageView);

                            }
                        });
                updateProfilePhoto();
                Toast.makeText(UpdateProfileActivity.this, "Successfully updated", Toast.LENGTH_SHORT).show();

            }



        }
    }

    public void updateProfileProgessDialog(){
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Updating your profile ...");
        progressDialog.setCancelable(true);
    }

    public void updateProfilePhoto(){
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final String uid = user.getUid();
        final String profileImage = (imageUri.toString());

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

                    usersRef.child("profileImage").setValue(profileImage);
                    mProfilePictureImageView.setImageBitmap(null);

                    //progress dialog to show the retrieval of the update profile picture
//                    viewAnimator.setDisplayedChild(1);

                    usersRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            final String profileImage = (String) dataSnapshot.child("profileImage").getValue();

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
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });


                }
            });

        }

    }

    public void updateCoverPhoto(){
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final String uid = user.getUid();

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

                    final String profileCover = (downloadUrl.toString());

                    usersRef.child("profileCover").setValue(profileCover);
                    mProfileCoverImageView.setImageBitmap(null);

                    //progress dialog to show the retrieval of the update profile picture
//                    viewAnimator.setDisplayedChild(1);

                    usersRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            final String profileImage = (String) dataSnapshot.child("profileCover").getValue();

                            Picasso.with(UpdateProfileActivity.this)
                                    .load(profileImage)
                                    .resize(MAX_WIDTH, MAX_HEIGHT)
                                    .onlyScaleDown()
                                    .centerCrop()
                                    .placeholder(R.drawable.gradient_color)
                                    .networkPolicy(NetworkPolicy.OFFLINE)
                                    .into(mProfileCoverImageView, new Callback() {
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
                                                    .placeholder(R.drawable.gradient_color)
                                                    .into(mProfileCoverImageView);

                                        }
                                    });
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

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
            usersRef.child("username").setValue(username);
            mUsernameEditText.setText("");
        }
        if(!TextUtils.isEmpty(bio)){
            usersRef.child("bio").setValue(bio);
            mBioEditText.setText("");
        }

        if (!TextUtils.isEmpty(firstName)){
            usersRef.child("firstName").setValue(firstName);
            mFirstNameEditText.setText("");
        }

        if (!TextUtils.isEmpty(secondName)){
            usersRef.child("secondName").setValue(secondName);
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

package com.cinggl.cinggl.profile;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.cinggl.cinggl.Constants;
import com.cinggl.cinggl.R;
import com.cinggl.cinggl.models.Cingulan;
import com.cinggl.cinggl.utils.FirebaseUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
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


public class UpdateProfileActivity extends AppCompatActivity implements View.OnClickListener {
    @Bind(R.id.profilePictureImageView)CircleImageView mProfilePictureImageView;
    @Bind(R.id.usernameEditText) EditText mUsernameEditText;
    @Bind(R.id.bioEditText)EditText mBioEditText;
    @Bind(R.id.doneEditButton)Button mDoneEditButton;

    public static  final int GALLERY_REQUEST = 1;
    public static final String TAG = UpdateProfileActivity.class.getSimpleName();
    private Uri imageUri;
    private FirebaseUser firebaseUser;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    private DatabaseReference usersRef;
    private ProgressDialog progressDialog;
    private static final int MAX_WIDTH = 300;
    private static final int MAX_HEIGHT = 300;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDoneEditButton.setOnClickListener(this);
        mProfilePictureImageView.setOnClickListener(this);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        usersRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_USERS)
                .child(firebaseAuth.getCurrentUser().getUid());

        updateProfileProgessDialog();
    }


//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.menu_layout, menu);
//        return super.onCreateOptionsMenu(menu);
//    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onClick(View v){
        if(v == mDoneEditButton){
            updateUserProfile();
        }

        if(v == mProfilePictureImageView){
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, GALLERY_REQUEST);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK){
            if(requestCode == GALLERY_REQUEST){
                imageUri = data.getData();
                mProfilePictureImageView.setImageURI(imageUri);

                Picasso.with(this)
                        .load(imageUri)
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
                                        .load(imageUri)
                                        .resize(MAX_WIDTH, MAX_HEIGHT)
                                        .onlyScaleDown()
                                        .centerCrop()
                                        .placeholder(R.drawable.profle_image_background)
                                        .into(mProfilePictureImageView);

                            }
                        });


            }
        }
    }

    public void updateProfileProgessDialog(){
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Updating your profile ...");
        progressDialog.setCancelable(true);
    }


    public void updateUserProfile(){
        progressDialog.show();
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final String uid = user.getUid();

        StorageReference storageReference = FirebaseStorage
                .getInstance().getReference()
                .child("profile images")
                .child(uid);

        StorageReference filePath = storageReference.child(imageUri.getLastPathSegment());
        filePath.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                String downloadUrl = taskSnapshot.getDownloadUrl().toString();

                final String username = (mUsernameEditText.getText().toString());
                final String profileImage = (downloadUrl.toString());
                final String bio = (mBioEditText.getText().toString());

                if (!TextUtils.isEmpty(username)){
                    usersRef.child("username").setValue(username);
                    mUsernameEditText.setText("");
                }

                if (!TextUtils.isEmpty(bio)){
                    usersRef.child("bio").setValue(bio);
                    mBioEditText.setText("");
                }

                if (imageUri!= null){
                    usersRef.child("profileImage").setValue(profileImage);
                    mProfilePictureImageView.setImageBitmap(null);
                }
                progressDialog.dismiss();

                Toast.makeText(UpdateProfileActivity.this, "Your profile is updated!", Toast.LENGTH_SHORT).show();

            }
        });
    }
}

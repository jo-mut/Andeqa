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
import android.widget.ImageView;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import butterknife.Bind;
import butterknife.ButterKnife;


public class UpdateProfileActivity extends AppCompatActivity implements View.OnClickListener {
    @Bind(R.id.profilePictureImageView)ImageView mProfilePictureImageView;
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



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);
        ButterKnife.bind(this);

        mDoneEditButton.setOnClickListener(this);
        mProfilePictureImageView.setOnClickListener(this);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        usersRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_USERS)
                .child(firebaseAuth.getCurrentUser().getUid());

        updateProfileProgessDialog();
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
        final String name = mUsernameEditText.getText().toString().trim();

        StorageReference storageReference = FirebaseStorage
                .getInstance().getReference()
                .child("profile images")
                .child(uid);

        if(!TextUtils.isEmpty(name) && imageUri != null){
            StorageReference filePath = storageReference.child(imageUri.getLastPathSegment());
            filePath.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    String downloadUrl = taskSnapshot.getDownloadUrl().toString();
                    final Cingulan cingulan = new Cingulan();
                    cingulan.setUsername(mUsernameEditText.getText().toString());
                    cingulan.setProfileImage(downloadUrl.toString());
                    cingulan.setBio(mBioEditText.getText().toString());
                    cingulan.setUid(uid);

                    DatabaseReference pushRef = usersRef;
                    String pushId = pushRef.getKey();
                    cingulan.setPushId(pushId);
                    pushRef.setValue(cingulan);

                    progressDialog.dismiss();

                    Toast.makeText(UpdateProfileActivity.this, "Your profile is updated!", Toast.LENGTH_SHORT).show();

                }
            });
        }
    }

}

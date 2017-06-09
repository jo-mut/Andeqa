package com.cinggl.cinggl.profile;

import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.cinggl.cinggl.Constants;
import com.cinggl.cinggl.R;
import com.cinggl.cinggl.home.HomeActivity;
import com.cinggl.cinggl.models.Cingulan;
import com.cinggl.cinggl.ui.MainActivity;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener{
    @Bind(R.id.profileImageView)ImageView mProfileImageView;
    @Bind(R.id.setUpAccountButton)Button mSetUpAccountButton;
    @Bind(R.id.userNameEditText)EditText mUserNameEditText;


    public static  final int GALLERY_REQUEST = 1;
    private Uri imageUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        ButterKnife.bind(this);

        mProfileImageView.setOnClickListener(this);
        mSetUpAccountButton.setOnClickListener(this);

    }

    @Override
    public void onClick(View v){
        if(v == mProfileImageView){
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, GALLERY_REQUEST);
        }

        if(v == mSetUpAccountButton){
            setupProfilePicture();
        }
    }

    public void setupProfilePicture(){
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final String uid = user.getUid();
        final String name = mUserNameEditText.getText().toString().trim();

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
                    Cingulan cingulan = new Cingulan();
                    cingulan.setUsername(mUserNameEditText.getText().toString().trim());
                    cingulan.setProfileImage(downloadUrl.toString());
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                            .getReference("Users")
                            .child(uid);
                }
            });
        }

        Intent intent = new Intent(ProfileActivity.this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK){
            if(requestCode == GALLERY_REQUEST){
                imageUri = data.getData();
                mProfileImageView.setImageURI(imageUri);

            }
        }
    }


}

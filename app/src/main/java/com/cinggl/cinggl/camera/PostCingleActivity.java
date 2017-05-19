package com.cinggl.cinggl.camera;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.cinggl.cinggl.Constants;
import com.cinggl.cinggl.R;
import com.cinggl.cinggl.models.Cingle;
import com.cinggl.cinggl.ui.HomeActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

import butterknife.Bind;
import butterknife.ButterKnife;


public class PostCingleActivity extends AppCompatActivity implements View.OnClickListener{
    @Bind(R.id.cingleTitleEditText)EditText mCingleTitleEditText;
    @Bind(R.id.cingleDescriptionEditText)EditText mCingleDescriptionEditText;
    @Bind(R.id.postCingleImageView)ImageView mPostCingleImageView;
    @Bind(R.id.chosenImageView)ImageView mChosenImageView;
    @Bind(R.id.laceTextView)TextView mLaceTextView;

    private Cingle cingle;
    private Bitmap bitmap;
    private ProgressDialog progressDialog;
    private StorageReference storageReference;
    private  DatabaseReference databaseReference;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_cingle);
        ButterKnife.bind(this);

        mPostCingleImageView.setOnClickListener(this);
        postProgressDialog();

        storageReference = FirebaseStorage.getInstance().getReference();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        if(getIntent().hasExtra("byteArray")){
            bitmap = BitmapFactory.decodeByteArray(getIntent()
                    .getByteArrayExtra("byteArray"), 0, getIntent()
                    .getByteArrayExtra("byteArray").length);
            mChosenImageView.setImageBitmap(bitmap);
        }
    }

    @Override
    public void onClick(View v){
        if(v == mPostCingleImageView){
            saveToFirebase();
        }
    }

    private void postProgressDialog(){
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Adding your Cingle...");
        progressDialog.setCancelable(true);
    }



    public void saveToFirebase(){
        progressDialog.show();
        //CREATE A NEW CINGLE OBJECT AND GET THE INPUTTED TEXT

        mChosenImageView.setDrawingCacheEnabled(true);
        mChosenImageView.buildDrawingCache();
        bitmap = mChosenImageView.getDrawingCache();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] data = baos.toByteArray();

        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("CINGLE_IMAGES");
        UploadTask uploadTask = storageRef.putBytes(data);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                            @Override
                                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                Uri downloadUrl = taskSnapshot.getDownloadUrl();

                                                cingle = new Cingle();
                                                cingle.setTitle(mCingleTitleEditText.getText().toString().trim());
                                                cingle.setDescription(mCingleDescriptionEditText.getText().toString().trim());
                                                if( downloadUrl != null){
                                                    cingle.setCingleImageUrl(downloadUrl.toString());
                                                }

                                                FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                                                DatabaseReference databaseReference = firebaseDatabase.getReference();
                                                databaseReference.child(Constants.FIREBASE_CINGLES).push().setValue(cingle);


                                                progressDialog.dismiss();

                                                Intent intent = new Intent(PostCingleActivity.this, HomeActivity.class);
                                                startActivity(intent);

                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //HANDLE UNSUCCESSFUL UPLOADS
            }
        });

//        progressDialog.show();
//
//        //GET CURRENT SIGNED IN USER
//        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//        String uid = user.getUid();
//
//        //SAVE DATA TO FIREBASE
//        databaseReference = FirebaseDatabase.getInstance()
//                .getReference(Constants.FIREBASE_CINGLES)
//                .child(uid);
//        DatabaseReference pushRef = databaseReference.push();
//        String pushId = pushRef.getKey();
//        cingle.setPushId(pushId);
//        pushRef.setValue(cingle);
//
//
//        mCingleTitleEditText.setText("");
//        mCingleDescriptionEditText.setText("");
//        mChosenImageView.setImageResource(0);
//
//        progressDialog.dismiss();


    }

}

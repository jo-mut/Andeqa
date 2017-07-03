package com.cinggl.cinggl.camera;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewAnimator;

import com.cinggl.cinggl.Constants;
import com.cinggl.cinggl.R;
import com.cinggl.cinggl.models.Cingle;
import com.cinggl.cinggl.utils.ProportionalImageView;
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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import butterknife.Bind;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class CreateCingleActivity extends AppCompatActivity implements View.OnClickListener{
    @Bind(R.id.cingleTitleEditText)EditText mCingleTitleEditText;
    @Bind(R.id.cingleDescriptionEditText)EditText mCingleDescriptionEditText;
    @Bind(R.id.postCingleImageView)ImageView mPostCingleImageView;
//    @Bind(R.id.chosenImageView)ImageView mChosenImageView;
    @Bind(R.id.cameraImageView)ImageView mCameraImageView;
    @Bind(R.id.galleryImageView)ImageView mGalleryImageView;
    @Bind(R.id.profileImageView)CircleImageView mProfileImageView;
    @Bind(R.id.accountUsernameTextView)TextView mAccountUsernameTextView;
    @Bind(R.id.img)ProportionalImageView mProportionalImageView;

    private String ImageFileLocation = "";
    private String GALLERY_LOCATION = "Cingles";
    private File mGalleryFolder;
    private String image;
    private String photoUri;
    private static final String KEY_IMAGE = "IMAGE FROM GALLERY";
    private static final int CAMERA_REQUEST_CODE = 111;
    private static final int IMAGE_GALLERY_REQUEST = 112;
    private static final String TAG = "CreateCingleActivity";
    private static final int MAX_WIDTH = 400;
    private static final int MAX_HEIGHT = 400;
    private Uri imageUri;
    private Bitmap photoReducedSizeBitmap = null;
    private Cingle cingle;
    private StorageReference storageReference;
    private DatabaseReference databaseReference;
    private ProgressDialog progressDialog;
    private FirebaseUser firebaseUser;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference usernameRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_cingle);
        ButterKnife.bind(this);

        createImageGallery();

        mGalleryImageView.setOnClickListener(this);
        mCameraImageView.setOnClickListener(this);

        mGalleryImageView.setOnClickListener(this);
        mCameraImageView.setOnClickListener(this);
        mPostCingleImageView.setOnClickListener(this);
        uploadingToFirebaseDialog();

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        usernameRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_USERS);

        usernameRef.child(firebaseAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final String profileImage = (String) dataSnapshot.child("profileImage").getValue();
                String username = (String) dataSnapshot.child("username").getValue();

                mAccountUsernameTextView.setText(username);

                Picasso.with(CreateCingleActivity.this)
                        .load(profileImage)
                        .fit()
                        .centerCrop()
                        .networkPolicy(NetworkPolicy.OFFLINE)
                        .into(mProfileImageView, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError() {
                                Picasso.with(CreateCingleActivity.this)
                                        .load(profileImage)
                                        .fit()
                                        .centerCrop()
                                        .into(mProfileImageView);
                            }
                        });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    @Override
    protected void onPause(){
        super.onPause();
        if(isFinishing()){
            Picasso.with(this).cancelRequest(mProportionalImageView);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putString(KEY_IMAGE, image);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            if(requestCode == CAMERA_REQUEST_CODE){
                Picasso.with(this)
                        .load(photoUri)
                        .fit()
                        .centerCrop()
                        .into(mProportionalImageView);

            }

            if(requestCode == IMAGE_GALLERY_REQUEST && data != null){
                image = data.getData().toString();
                loadImage();
            }else {
                super.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    @Override
    public void onClick(View v){

        if(v == mCameraImageView){
            Intent cameraIntent =new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            File photoFile = null;
            try{
                photoFile = createImageFile();
            }catch (IOException e) {
                e.printStackTrace();
            }cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
            startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);

        }


        if(v == mGalleryImageView){
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, IMAGE_GALLERY_REQUEST);

        }

        if(v == mPostCingleImageView){
            saveToFirebase();
        }


//        if(v == mNextTextView){
//            Cingle cingle = new Cingle();
//            Intent intent = new Intent(CreateCingleActivity.this, CustomGelleryActivity.class);
//            intent.putExtra(Constants.CINGLE_IMAGE, Parcels.wrap(image));
//            startActivity(intent);

//            InputStream inputStream;
//            try{
//                inputStream = getContentResolver().openInputStream(imageUri
//                );
//
//                photoReducedSizeBitmap =
//                        BitmapFactory.decodeStream(inputStream);
//
//
//                mChosenImageView.setImageBitmap(photoReducedSizeBitmap);
//
//            }catch (FileNotFoundException e){
//                e.printStackTrace();
//                Toast.makeText(this, "Unable to open image", Toast.LENGTH_LONG).show();
//            }
//            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//            photoReducedSizeBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
//            intent.putExtra("byteArray", baos.toByteArray());
//            startActivity(intent);
//
//        }
    }

    public void loadImage(){
        /**index 1 is the progress bar and it show as the image is loading*/

        Picasso.with(this)
                .load(image)
                .fit()
                .centerInside()
                .into(mProportionalImageView, new Callback.EmptyCallback(){
                    @Override
                    public void onSuccess(){
                        /**index 0 is the mChosenImageView*/
                    }
                });

    }

    private void createImageGallery(){
        File storageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        mGalleryFolder = new File(storageDirectory, GALLERY_LOCATION);
        if(!mGalleryFolder.exists()){
            mGalleryFolder.mkdirs();
        }
    }

    public File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "IMAGE_" + timeStamp + "_";
        File tempFile = File.createTempFile(imageFileName, ".jpg", mGalleryFolder);
        ImageFileLocation = tempFile.getAbsolutePath();
        return  tempFile;

    }

//    public void setReducedImageSize(){
//        int targetImageViewWidth = mChosenImageView.getWidth();
//        int targetImageViewHeight = mChosenImageView.getHeight();
//
//
//        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
//        bmOptions.inJustDecodeBounds = true;
//        BitmapFactory.decodeFile(ImageFileLocation, bmOptions);
//        int cameraImageWidth = bmOptions.outWidth;
//        int cameraImageHeight = bmOptions.outHeight;
//
//        int scaleFactor = Math.min
//                (cameraImageWidth/targetImageViewWidth, cameraImageHeight/targetImageViewHeight);
//        bmOptions.inSampleSize = scaleFactor;
//        bmOptions.inJustDecodeBounds = false;
//
//
//        photoReducedSizeBitmap = BitmapFactory.decodeFile(ImageFileLocation, bmOptions);
//        mChosenImageView.setImageBitmap(photoReducedSizeBitmap);
//    }

    public void uploadingToFirebaseDialog(){
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Adding your Cingle...");
        progressDialog.setCancelable(true);
    }


    public void saveToFirebase(){
        progressDialog.show();
        mProportionalImageView.setDrawingCacheEnabled(true);
        mProportionalImageView.buildDrawingCache();
        photoReducedSizeBitmap = mProportionalImageView.getDrawingCache();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        photoReducedSizeBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte [] data = baos.toByteArray();


        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final String uid = user.getUid();

        final Long timeStamp = System.currentTimeMillis();
        StorageReference storageReference = FirebaseStorage
                .getInstance().getReference()
                .child(Constants.FIREBASE_CINGLES)
                .child(uid)
                .child(timeStamp.toString());

        UploadTask uploadTask = storageReference.putBytes(data);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                final Uri downloadUrl = taskSnapshot.getDownloadUrl();

                usernameRef.child(firebaseAuth.getCurrentUser().getUid())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                String username = (String) dataSnapshot.child("username").getValue();
                                String uid = (String) dataSnapshot.child("uid").getValue();
                                String profileImage = (String) dataSnapshot.child("profileImage").getValue();

                                Cingle cingle = new Cingle();
                                cingle.setTitle(mCingleTitleEditText.getText().toString());
                                cingle.setDescription(mCingleDescriptionEditText.getText().toString());
                                cingle.setTimeStamp(timeStamp.toString());
                                cingle.setUid(uid);
                                cingle.setAccountUserName(username);
                                cingle.setProfileImageUrl(profileImage);

                                if(photoReducedSizeBitmap != null){
                                    cingle.setCingleImageUrl(downloadUrl.toString());
                                }

                /*Getting the current logged in user*/
                                DatabaseReference databaseReference = FirebaseDatabase
                                        .getInstance()
                                        .getReference(Constants.FIREBASE_CINGLES);


                /*Pushing the same cingle to a reference from where Cingles posted by the
                 user will be retrieved and displayed on their profile*/
                                DatabaseReference userRef = databaseReference.push();
                                String pushId = userRef.getKey();
                                cingle.setPushId(pushId);
                                userRef.setValue(cingle);

                                mCingleTitleEditText.setText("");
                                mCingleDescriptionEditText.setText("");
                                mProportionalImageView.setImageBitmap(null);

                                progressDialog.dismiss();

                                Toast.makeText(CreateCingleActivity.this, "Your Cingle has successfully been posted", Toast.LENGTH_LONG).show();

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });



            }
        });
    }

}

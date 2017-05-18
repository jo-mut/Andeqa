package com.cinggl.cinggl.camera;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cinggl.cinggl.Constants;
import com.cinggl.cinggl.R;
import com.cinggl.cinggl.models.Cingle;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import butterknife.Bind;
import butterknife.ButterKnife;

public class CreateCingleActivity extends AppCompatActivity implements View.OnClickListener{
    @Bind(R.id.nextTextView)TextView mNextTextView;
    @Bind(R.id.cameraImageView)ImageView mCameraImageView;
    @Bind(R.id.takenImageView)ImageView mTakenImageView;
    @Bind(R.id.galleryImageView)ImageView mGalleryImageView;
    @Bind(R.id.recyclerView) RecyclerView mRecyclerView;


    private static final int CAMERA_REQUEST_CODE = 111;
    private static final int IMAGE_GALLERY_REQUEST = 112;
    private static final int MAX_WIDTH = 400;
    private static final int MAX_HEIGHT = 400;
    private String ImageFileLocation = "Image Gallery";
    private String GALLERY_LOCATION = "Cingles";
    private File GalleryFolder;
    private Uri imageUri = null;
    private Bitmap photoReducedSizeBitmap = null;

    private StorageReference storageReference;
    private  DatabaseReference databaseReference;
    private Cingle cingle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_cingle);
        ButterKnife.bind(this);

        createImageGallery();

        storageReference = FirebaseStorage.getInstance().getReference();

        mGalleryImageView.setOnClickListener(this);
        mCameraImageView.setOnClickListener(this);
        mNextTextView.setOnClickListener(this);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            if(requestCode == CAMERA_REQUEST_CODE){
                setReducedImageSize();
            }

            if(requestCode == IMAGE_GALLERY_REQUEST){
                imageUri = data.getData();

                InputStream inputStream;
                try{
                    inputStream = getContentResolver().openInputStream(imageUri);

                    photoReducedSizeBitmap = BitmapFactory.decodeStream(inputStream);

                    mTakenImageView.setImageBitmap(photoReducedSizeBitmap);
//                    encodeBitmapAndSaveToFirebase(photoReducedSizeBitmap);

                }catch (FileNotFoundException e){
                    e.printStackTrace();
                    Toast.makeText(this, "Unable to open image", Toast.LENGTH_LONG).show();
                }
            }
        }
    }


    private void createImageGallery(){
        File storageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        GalleryFolder = new File(storageDirectory, GALLERY_LOCATION);
        if(!GalleryFolder.exists()){
            GalleryFolder.mkdirs();
        }
    }

    File createImageFile() throws IOException{
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "IMAGE_" + timeStamp + "_";
        File image = File.createTempFile(imageFileName, ".jpg", GalleryFolder);
        ImageFileLocation = image.getAbsolutePath();
        return  image;

    }

    public void setReducedImageSize(){
        int targetImageViewWidth = mTakenImageView.getWidth();
        int targetImageViewHeight = mTakenImageView.getHeight();

        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(ImageFileLocation, bmOptions);
        int cameraImageWidth = bmOptions.outWidth;
        int cameraImageHeight = bmOptions.outHeight;

        int scaleFactor = Math.min(cameraImageWidth/targetImageViewWidth, cameraImageHeight/targetImageViewHeight);
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inJustDecodeBounds = false;

        photoReducedSizeBitmap = BitmapFactory.decodeFile(ImageFileLocation, bmOptions);
        mTakenImageView.setImageBitmap(photoReducedSizeBitmap);
//        encodeBitmapAndSaveToFirebase(photoReducedSizeBitmap);
    }

//    public void encodeBitmapAndSaveToFirebase(Bitmap bitmap) {
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
//        String imageEncoded = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
//        DatabaseReference ref = FirebaseDatabase.getInstance()
//                .getReference(Constants.FIREBASE_CINGLES)
//                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
//                .child(cingle.getPushId())
//                .child("imageUrl");
//        ref.setValue(imageEncoded);
//    }


    @Override
    public void onClick(View v) {
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
            Intent intent = new Intent(Intent.ACTION_PICK);

            File pictureDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            String pictureDirectoryPath = pictureDirectory.getPath();

            Uri data = Uri.parse(pictureDirectoryPath);
            intent.setDataAndType(data, "image/*");

            startActivityForResult(intent, IMAGE_GALLERY_REQUEST);
        }

        if(v == mNextTextView){
            Intent intent = new Intent(CreateCingleActivity.this, PostCingleActivity.class);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            photoReducedSizeBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            intent.putExtra("byteArray", baos.toByteArray());
            startActivity(intent);

        }

    }

}

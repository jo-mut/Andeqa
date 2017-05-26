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
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewAnimator;

import com.cinggl.cinggl.Constants;
import com.cinggl.cinggl.R;
import com.cinggl.cinggl.models.Cingle;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.parceler.Parcels;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import butterknife.Bind;
import butterknife.ButterKnife;

import static android.content.Intent.ACTION_PICK;
import static android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

public class CreateCingleActivity extends AppCompatActivity implements View.OnClickListener{
    @Bind(R.id.nextTextView)TextView mNextTextView;
    @Bind(R.id.cameraImageView)ImageView mCameraImageView;
    @Bind(R.id.chosenImageView)ImageView mChosenImageView;
    @Bind(R.id.galleryImageView)ImageView mGalleryImageView;
    @Bind(R.id.viewAnimator)ViewAnimator mViewAnimator;

    private static final int CAMERA_REQUEST_CODE = 111;
    private static final int IMAGE_GALLERY_REQUEST = 112;
    private static final int MAX_WIDTH = 400;
    private static final int MAX_HEIGHT = 400;
    private String ImageFileLocation = "";
    private String GALLERY_LOCATION = "Cingles";
    private File mGalleryFolder;
    private Uri imageUri = null;
    private Bitmap photoReducedSizeBitmap = null;
    private String image;
    private String photoUri;
    private static final String KEY_IMAGE = "IMAGE FROM GALLERY";
    private Cingle cingle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_cingle);
        ButterKnife.bind(this);

        createImageGallery();

        mGalleryImageView.setOnClickListener(this);
        mCameraImageView.setOnClickListener(this);
        mNextTextView.setOnClickListener(this);

        findViewById(R.id.galleryImageView).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(ACTION_PICK, EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, IMAGE_GALLERY_REQUEST);

            }

        });

        if(savedInstanceState != null){
            image = savedInstanceState.getString(KEY_IMAGE);
            if(image != null){
                loadImage();
            }
        }
    }


    @Override
    protected void onPause(){
        super.onPause();
        if(isFinishing()){
            Picasso.with(this).cancelRequest(mChosenImageView);
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
                        .into(mChosenImageView);
//                mChosenImageView.setImageURI(Uri.fromFile(new File(photoUri)));

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


        if(v == mCameraImageView){
            Intent cameraIntent =new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            File photoFile = null;
            try{
                photoFile = createImageFile();
            }catch (IOException e) {
                e.printStackTrace();
            }cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
            photoUri = photoFile.getAbsolutePath();
            startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);

        }

        if(v == mNextTextView){
            Cingle cingle = new Cingle();
            Intent intent = new Intent(CreateCingleActivity.this, CustomGelleryActivity.class);
            intent.putExtra(Constants.CINGLE_IMAGE, Parcels.wrap(image));
            startActivity(intent);

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

        }
    }

    public void loadImage(){
        /**index 1 is the progress bar and it show as the image is loading*/
        mViewAnimator.setDisplayedChild(1);

        Picasso.with(this)
                .load(image)
                .fit()
                .centerInside()
                .into(mChosenImageView, new Callback.EmptyCallback(){
                    @Override
                    public void onSuccess(){
                        /**index 0 is the mChosenImageView*/
                        mViewAnimator.setDisplayedChild(0);
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

    public void setReducedImageSize(){
        int targetImageViewWidth = mChosenImageView.getWidth();
        int targetImageViewHeight = mChosenImageView.getHeight();


        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(ImageFileLocation, bmOptions);
        int cameraImageWidth = bmOptions.outWidth;
        int cameraImageHeight = bmOptions.outHeight;

        int scaleFactor = Math.min
                (cameraImageWidth/targetImageViewWidth, cameraImageHeight/targetImageViewHeight);
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inJustDecodeBounds = false;


        photoReducedSizeBitmap = BitmapFactory.decodeFile(ImageFileLocation, bmOptions);
        mChosenImageView.setImageBitmap(photoReducedSizeBitmap);
    }

}

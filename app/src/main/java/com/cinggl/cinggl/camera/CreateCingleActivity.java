package com.cinggl.cinggl.camera;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.cinggl.cinggl.Constants;
import com.cinggl.cinggl.R;
import com.cinggl.cinggl.models.Cingle;
import com.cinggl.cinggl.services.ConnectivityReceiver;
import com.cinggl.cinggl.utils.App;
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

public class CreateCingleActivity extends AppCompatActivity implements View.OnClickListener,
        ConnectivityReceiver.ConnectivityReceiverListener{
    @Bind(R.id.cingleTitleEditText)EditText mCingleTitleEditText;
    @Bind(R.id.cingleDescriptionEditText)EditText mCingleDescriptionEditText;
    @Bind(R.id.postCingleImageView)ImageView mPostCingleImageView;
//    @Bind(R.id.chosenImageView)ImageView mChosenImageView;
    @Bind(R.id.cameraImageView)ImageView mCameraImageView;
    @Bind(R.id.galleryImageView)ImageView mGalleryImageView;
    @Bind(R.id.userProfileImageView)CircleImageView mProfileImageView;
    @Bind(R.id.accountUsernameTextView)TextView mAccountUsernameTextView;
    @Bind(R.id.img)ProportionalImageView mProportionalImageView;
//    @Bind(R.id.cingleImageView)ImageView mProportionalImageView;

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
    private Bitmap photoReducedSizeBitmap = null;
    private Cingle cingle;
    private File photoFile;
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

                setPic();

//                setReducedImageSize();

//               Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
//                mProportionalImageView.setImageBitmap(imageBitmap);


//                Bitmap bitmap = createScaledBitmap(getImagePath(data, getApplicationContext()),
//                        mProportionalImageView.getWidth() ,mProportionalImageView.getHeight());
//                mProportionalImageView.setImageBitmap(bitmap);
//
//                Picasso.with(CreateCingleActivity.this)
//                        .load(photoFile)
//                        .resize(MAX_WIDTH, MAX_HEIGHT)
//                        .onlyScaleDown()
//                        .into(mProportionalImageView);
//                Glide.with(this)
//                        .load(photoFile)
//                        .into(mProportionalImageView);
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
//
//        if(v == mCameraImageView){
//            Intent cameraIntent =new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
////            photoFile = null;
////            try{
////                photoFile = createImageFile();
////            }catch (IOException e) {
////                e.printStackTrace();
////            }cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
//            startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
//        }

        if (v ==  mCameraImageView){
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            photoFile = null;
            try {
                photoFile = createImageFile();
            }catch (IOException e){
                e.printStackTrace();
            }intent.putExtra(MediaStore.EXTRA_OUTPUT, photoFile);
            startActivityForResult(intent, CAMERA_REQUEST_CODE);
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
                .into(mProportionalImageView, new Callback.EmptyCallback(){
                    @Override
                    public void onSuccess(){
                        /**index 0 is the mChosenImageView*/
                    }
                });

    }

    private void setPic() {
        // Get the dimensions of the View
        int targetW = mProportionalImageView.getWidth();
        int targetH = mProportionalImageView.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(ImageFileLocation, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;

        Bitmap bitmap = BitmapFactory.decodeFile(ImageFileLocation, bmOptions);
        mProportionalImageView.setImageBitmap(bitmap);
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
        int targetImageViewWidth = mProportionalImageView.getWidth();
        int targetImageViewHeight = mProportionalImageView.getHeight();


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
        mProportionalImageView.setImageBitmap(photoReducedSizeBitmap);
    }

    public void uploadingToFirebaseDialog(){
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Adding your Cingle...");
        progressDialog.setCancelable(true);
    }

    // Function to get image path from ImagePicker
    public static String getImagePath(Intent data, Context context) {
        Uri selectedImage = data.getData();
        String[] filePathColumn = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(selectedImage, filePathColumn, null, null, null);
        cursor.moveToFirst();
        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String picturePath = cursor.getString(columnIndex);
        cursor.close();
        return picturePath;
    }


    public Bitmap createScaledBitmap(String pathName, int width, int height) {
        final BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(ImageFileLocation, opt);
        opt.inSampleSize = calculateBmpSampleSize(opt, width, height);
        opt.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(ImageFileLocation, opt);
    }

    public int calculateBmpSampleSize(BitmapFactory.Options opt, int width, int height) {
        final int outHeight = opt.outHeight;
        final int outWidth = opt.outWidth;
        int sampleSize = 1;
        if (outHeight > height || outWidth > width) {
            final int heightRatio = Math.round((float) outHeight / (float) height);
            final int widthRatio = Math.round((float) outWidth / (float) width);
            sampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        return sampleSize;
    }


    public void saveToFirebase(){
        if (image != null){
            try {
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

                                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("d");
                                        String date = simpleDateFormat.format(new Date());

                                        if (date.endsWith("1") && !date.endsWith("11"))
                                            simpleDateFormat = new SimpleDateFormat("d'st' MMM yyyy");
                                        else if (date.endsWith("2") && !date.endsWith("12"))
                                            simpleDateFormat = new SimpleDateFormat("d'nd' MMM yyyy");
                                        else if (date.endsWith("3") && !date.endsWith("13"))
                                            simpleDateFormat = new SimpleDateFormat("d'rd' MMM yyyy");
                                        else
                                            simpleDateFormat = new SimpleDateFormat("d'th' MMM yyyy");
                                        String currentDate = simpleDateFormat.format(new Date());

                                        cingle.setTitle(mCingleTitleEditText.getText().toString());
                                        cingle.setDescription(mCingleDescriptionEditText.getText().toString());
                                        cingle.setTimeStamp(timeStamp.toString());
                                        cingle.setUid(uid);
                                        cingle.setAccountUserName(username);
                                        cingle.setProfileImageUrl(profileImage);
                                        cingle.setDatePosted(currentDate);
                                        cingle.setCingleImageUrl(downloadUrl.toString());

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


            }catch (Exception e){

            }
        }else {
            Toast.makeText(this, "Cingle must be an image", Toast.LENGTH_LONG).show();
        }
    }

    // Method to manually check connection status
    private void checkConnection() {
        boolean isConnected = ConnectivityReceiver.isConnected();
        showConnection(isConnected);
    }

    //Showing the status in Snackbar
    private void showConnection(boolean isConnected) {
        String message;
        if (isConnected) {
            message = "Connected to the internet";
        } else {
            message = "You are disconnected from the internet";
        }

        Toast.makeText(this, message, Toast.LENGTH_LONG).show();

    }

    @Override
    protected void onResume() {
        super.onResume();

        // register connection status listener
        App.getInstance().setConnectivityListener(this);
        checkConnection();

    }

    /**
     * Callback will be triggered when there is change in
     * network connection
     */
    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        showConnection(isConnected);
    }



}

package com.cinggl.cinggl.creation;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cinggl.cinggl.Constants;
import com.cinggl.cinggl.R;
import com.cinggl.cinggl.home.NavigationDrawerActivity;
import com.cinggl.cinggl.models.Cingle;
import com.cinggl.cinggl.ProportionalImageView;
import com.cinggl.cinggl.models.TransactionDetails;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

import static android.R.attr.editable;

public class CreateCingleActivity extends AppCompatActivity implements View.OnClickListener{
    @Bind(R.id.cingleTitleEditText)EditText mCingleTitleEditText;
    @Bind(R.id.cingleDescriptionEditText)EditText mCingleDescriptionEditText;
    @Bind(R.id.postCingleImageView)ImageView mPostCingleImageView;
    @Bind(R.id.cameraImageView)ImageView mCameraImageView;
//    @Bind(R.id.galleryImageView)ImageView mGalleryImageView;
    @Bind(R.id.profileImageView)CircleImageView mProfileImageView;
    @Bind(R.id.usernameTextView)TextView mAccountUsernameTextView;
    @Bind(R.id.img)ProportionalImageView mProportionalImageView;
    @Bind(R.id.descriptionCountTextView)TextView mDescriptionCountTextView;
    @Bind(R.id.titleCountTextView)TextView mTitleCountTextView;

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
    private Bitmap bitmap;
    private Cingle cingle;
    private File file;
    private StorageReference storageReference;
    private DatabaseReference databaseReference;
    private DatabaseReference profileCinglesReference;
    private ProgressDialog progressDialog;
    private FirebaseUser firebaseUser;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference cingleOwnersReference;
    private DatabaseReference usernameRef;
    private List<Cingle> cingles = new ArrayList<>();

    private static final int DEFAULT_TITLE_LENGTH_LIMIT = 100;
    private static final int DEFAULT_DESCRIPTION_LENGTH_LIMIT = 500;
//

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_cingle);
        ButterKnife.bind(this);

        mCameraImageView.setOnClickListener(this);
        mPostCingleImageView.setOnClickListener(this);

        firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() != null){
            usernameRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_USERS);
            databaseReference = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CINGLES);
            profileCinglesReference = FirebaseDatabase.getInstance().getReference(Constants.PROFILE_CINGLES);
            cingleOwnersReference = FirebaseDatabase.getInstance().getReference(Constants.CINGLE_ONWERS);

            fetchUserData();
            uploadingToFirebaseDialog();
            createImageGallery();
            mCingleTitleEditText.setFilters(new InputFilter[]{new InputFilter
                    .LengthFilter(DEFAULT_TITLE_LENGTH_LIMIT)});
            mCingleDescriptionEditText.setFilters(new  InputFilter[]{new InputFilter
                    .LengthFilter(DEFAULT_DESCRIPTION_LENGTH_LIMIT)});
            textWatchers();
        }

    }



    private void textWatchers(){
        //TITLE TEXT WATCHER
        mCingleTitleEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                int count = DEFAULT_TITLE_LENGTH_LIMIT - editable.length();
                mTitleCountTextView.setText(Integer.toString(count));

                if (count < 0){
                }else if (count < 100){
                    mTitleCountTextView.setTextColor(Color.GRAY);
                }else {
                    mTitleCountTextView.setTextColor(Color.BLACK);
                }

            }
        });

        //DESCRIPTION TEXT WATCHER
        mCingleDescriptionEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                int count = DEFAULT_DESCRIPTION_LENGTH_LIMIT- editable.length();
                mDescriptionCountTextView.setText(Integer.toString(count));

                if (count < 0){
                }else if (count < 100){
                    mDescriptionCountTextView.setTextColor(Color.GRAY);
                }else if (count < 200){
                    mDescriptionCountTextView.setTextColor(Color.RED);
                }else if (count < 300){
                    mDescriptionCountTextView.setTextColor(Color.BLUE);
                }else if (count < 400){
                    mDescriptionCountTextView.setTextColor(Color.GREEN);
                }else {
                    mDescriptionCountTextView.setTextColor(Color.BLACK);
                }

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

                /**diaabled camera intent*/
//                file = new File(Environment.getExternalStorageDirectory().toString());
//                for (File temp : file.listFiles()){
//                    if (temp.getName().equals("temp.jpg")){
//                        file = temp;
//                    }
//                }
//                try {
//
//                    BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
//
//                    bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), bitmapOptions);
//
//                    bitmap = ThumbnailUtils.extractThumbnail(bitmap, 400,400);
//                    // NOTE incredibly useful trick for cropping/resizing square
//
//                    Matrix matrix = new Matrix();
//                    matrix.postRotate(neededRotation(file));
//
//                    mProportionalImageView.setImageBitmap(bitmap);


//                }catch (Exception e){
//                    e.printStackTrace();
//                }

            }

            if(requestCode == IMAGE_GALLERY_REQUEST && data != null){
                image = data.getData().toString();
                loadImage();
            }else {
                super.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    public static int neededRotation(File ff) {
        try
        {
            ExifInterface exif = new ExifInterface(ff.getAbsolutePath());
            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            if (orientation == ExifInterface.ORIENTATION_ROTATE_270)
            { return 270; }
            if (orientation == ExifInterface.ORIENTATION_ROTATE_180)
            { return 180; }
            if (orientation == ExifInterface.ORIENTATION_ROTATE_90)
            { return 90; }
            return 0;

        } catch (FileNotFoundException e)
        {
            e.printStackTrace();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        return 0;
    }

    public void fetchUserData(){
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
    public void onClick(View v){

//        if ( v == mCameraImageView){
//            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//            file = new File(android.os.Environment.getExternalStorageDirectory(), "temp.jpg");
//            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
//            startActivityForResult(intent, CAMERA_REQUEST_CODE);
//        }

        if(v == mCameraImageView){
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, IMAGE_GALLERY_REQUEST);
        }

//        if(v == mGalleryImageView){
//            Intent intent = new Intent();
//            intent.setAction(Intent.ACTION_GET_CONTENT);
//            intent.setType("image/*");
//            startActivityForResult(intent, IMAGE_GALLERY_REQUEST);
//
//        }

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

    private void createImageGallery(){
        File storageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        mGalleryFolder = new File(storageDirectory, GALLERY_LOCATION);
        if(!mGalleryFolder.exists()){
            mGalleryFolder.mkdirs();
        }
    }

    public void uploadingToFirebaseDialog(){
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(true);
    }

    public void saveToFirebase(){
        if (image != null || bitmap != null){
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
                                        final String uid = (String) dataSnapshot.child("uid").getValue();

                                        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                usernameRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(final DataSnapshot dataSnapshot) {
                                                        if (dataSnapshot.hasChild(firebaseAuth.getCurrentUser().getUid())){
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

                                                            long index = dataSnapshot.getChildrenCount();

                                                            final long currentIdex = index + 1;

                                                            cingle.setCingleIndex("Cingle number" + " " + currentIdex);
                                                            cingle.setNumber(currentIdex);
                                                            cingle.setRandomNumber((double) new Random().nextDouble());
                                                            cingle.setTitle(mCingleTitleEditText.getText().toString());
                                                            cingle.setDescription(mCingleDescriptionEditText.getText().toString());
                                                            cingle.setTimeStamp(timeStamp);
                                                            cingle.setUid(uid);

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

                                                            Cingle profileCingle = new Cingle();
                                                            profileCingle.setPushId(pushId);
                                                            profileCingle.setNumber(currentIdex);

                                                            profileCinglesReference.child(firebaseAuth.getCurrentUser().getUid())
                                                                    .child(pushId)
                                                                    .setValue(profileCingle);

                                                            TransactionDetails transactionDetails = new TransactionDetails();
                                                            transactionDetails.setPushId(pushId);
                                                            transactionDetails.setUid(firebaseAuth.getCurrentUser().getUid());
                                                            transactionDetails.setDate(currentDate);

                                                            //save the cingle owner
                                                            cingleOwnersReference.child(pushId).child("owner").setValue(transactionDetails);
                                                            //save the cingle ownership history
                                                            cingleOwnersReference.child(pushId).child("onwership history").setValue(transactionDetails);
                                                            //once cingle has been bought remove it from cingle selling
                                                            mCingleTitleEditText.setText("");
                                                            mCingleDescriptionEditText.setText("");
                                                            mProportionalImageView.setImageBitmap(null);

                                                            progressDialog.dismiss();

                                                            Toast.makeText(CreateCingleActivity.this, "Your Cingle has successfully been posted", Toast.LENGTH_LONG).show();

                                                            Intent intent = new Intent(CreateCingleActivity.this, NavigationDrawerActivity.class);
                                                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                            startActivity(intent);
                                                            finish();
                                                        }else {
                                                            usernameRef.child(firebaseAuth.getCurrentUser().getUid()).child("uid")
                                                                    .setValue(firebaseAuth.getCurrentUser().getUid()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if (task.isSuccessful()){
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

                                                                        long index = dataSnapshot.getChildrenCount();

                                                                        final long currentIdex = index + 1;

                                                                        cingle.setCingleIndex("Cingle number" + " " + currentIdex);
                                                                        cingle.setNumber(currentIdex);
                                                                        cingle.setRandomNumber((double) new Random().nextDouble());
                                                                        cingle.setTitle(mCingleTitleEditText.getText().toString());
                                                                        cingle.setDescription(mCingleDescriptionEditText.getText().toString());
                                                                        cingle.setTimeStamp(timeStamp);
                                                                        cingle.setUid(uid);

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

                                                                        Cingle profileCingle = new Cingle();
                                                                        profileCingle.setPushId(pushId);
                                                                        profileCingle.setNumber(currentIdex);

                                                                        profileCinglesReference.child(firebaseAuth.getCurrentUser().getUid())
                                                                                .child(pushId)
                                                                                .setValue(profileCingle);

                                                                        TransactionDetails transactionDetails = new TransactionDetails();
                                                                        transactionDetails.setPushId(pushId);
                                                                        transactionDetails.setUid(firebaseAuth.getCurrentUser().getUid());
                                                                        transactionDetails.setDate(currentDate);

                                                                        //save the cingle owner
                                                                        cingleOwnersReference.child(pushId).child("owner").setValue(transactionDetails);
                                                                        //save the cingle ownership history
                                                                        cingleOwnersReference.child(pushId).child("onwership history").setValue(transactionDetails);
                                                                        //once cingle has been bought remove it from cingle selling
                                                                        mCingleTitleEditText.setText("");
                                                                        mCingleDescriptionEditText.setText("");
                                                                        mProportionalImageView.setImageBitmap(null);

                                                                        progressDialog.dismiss();

                                                                        Toast.makeText(CreateCingleActivity.this, "Your Cingle has successfully been posted", Toast.LENGTH_LONG).show();

                                                                        Intent intent = new Intent(CreateCingleActivity.this, NavigationDrawerActivity.class);
                                                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                                        startActivity(intent);
                                                                        finish();
                                                                    }
                                                                }
                                                            });
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(DatabaseError databaseError) {

                                                    }
                                                });

                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });

                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });



                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(CreateCingleActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                      double progress = (100.0 * taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                        progressDialog.setMessage("Adding your Cingle" + " " + ((int) progress) + "%...");
                    }
                });


            }catch (Exception e){

            }
        }else {
            Toast.makeText(this, "Cingle must be an image", Toast.LENGTH_LONG).show();
        }
    }

}

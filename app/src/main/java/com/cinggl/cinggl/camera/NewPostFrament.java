package com.cinggl.cinggl.camera;


;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewAnimator;

import com.cinggl.cinggl.Constants;
import com.cinggl.cinggl.R;
import com.cinggl.cinggl.models.Cingle;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
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

import static android.app.Activity.RESULT_OK;
import static android.content.Intent.ACTION_PICK;
import static android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

/**
 * A simple {@link Fragment} subclass.
 */
public class NewPostFrament extends DialogFragment implements View.OnClickListener {
    @Bind(R.id.cingleTitleEditText)EditText mCingleTitleEditText;
    @Bind(R.id.cingleDescriptionEditText)EditText mCingleDescriptionEditText;
    @Bind(R.id.postCingleTextView)TextView mPostCingleTextView;
    @Bind(R.id.chosenImageView)ImageView mChosenImageView;
    @Bind(R.id.cameraImageView)ImageView mCameraImageView;
    @Bind(R.id.galleryImageView)ImageView mGalleryImageView;

    private static final int CAMERA_REQUEST_CODE = 111;
    private static final int IMAGE_GALLERY_REQUEST = 112;
    private static final int MAX_WIDTH = 400;
    private static final int MAX_HEIGHT = 400;
    private Uri imageUri = null;
    private Bitmap photoReducedSizeBitmap = null;
    private Cingle cingle;
    private StorageReference storageReference;
    private DatabaseReference databaseReference;
    private ProgressDialog progressDialog;


    public NewPostFrament() {
        // Required empty public constructor
    }

    public static NewPostFrament newInstance(String title){
        NewPostFrament newPostFrament = new NewPostFrament();
        Bundle args = new Bundle();
        args.putString("title", title);
        newPostFrament.setArguments(args);
        return newPostFrament;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_new_post_frament, container, false);
        ButterKnife.bind(this, view);

        mGalleryImageView.setOnClickListener(this);
        mCameraImageView.setOnClickListener(this);
        mPostCingleTextView.setOnClickListener(this);
        uploadingToFirebaseDialog();
//        createImageGallery();

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        String title = getArguments().getString("title", "create your cingle");
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            if(requestCode == CAMERA_REQUEST_CODE){
                // we are hearing back from the camera.
                photoReducedSizeBitmap = (Bitmap) data.getExtras().get("data");
                // at this point, we have the image from the camera.
                mChosenImageView.setImageBitmap(photoReducedSizeBitmap);

            }

            if(requestCode == IMAGE_GALLERY_REQUEST){
                imageUri = data.getData();

                InputStream inputStream;

                try{
                    inputStream = getContext().getContentResolver().openInputStream(imageUri);

                    photoReducedSizeBitmap = BitmapFactory.decodeStream(inputStream);
                    mChosenImageView.setImageBitmap(photoReducedSizeBitmap);

                }catch (FileNotFoundException e){
                    e.printStackTrace();
                    Toast.makeText(getContext(), "Unable to open image", Toast.LENGTH_LONG).show();
                }
            }

        }

    }

    private String getPictureName() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String timestamp = sdf.format(new Date());
        return "Cingle" + timestamp + ".jpg";

    }

    public void uploadingToFirebaseDialog(){
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Adding your Cingle...");
        progressDialog.setCancelable(true);
    }

    @Override
    public void onClick(View v){
        if(v == mCameraImageView){
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            File pictureDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            String pictureName = getPictureName();
            File imageFile = new File(pictureDirectory, pictureName);
            imageUri = Uri.fromFile(imageFile);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, CAMERA_REQUEST_CODE);
            startActivity(cameraIntent);
        }

        if(v == mGalleryImageView){
            Intent galleryIntent = new Intent(Intent.ACTION_PICK);
            File photo = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            String photoPath = photo.getPath();
            imageUri = Uri.parse(photoPath);
            galleryIntent.setDataAndType(imageUri, "image/*");
            startActivityForResult(galleryIntent, IMAGE_GALLERY_REQUEST);
        }

        if(v == mPostCingleTextView){
            saveToFirebase();
        }

    }

    public void saveToFirebase(){
        progressDialog.show();
        mChosenImageView.setDrawingCacheEnabled(true);
        mChosenImageView.buildDrawingCache();
        photoReducedSizeBitmap = mChosenImageView.getDrawingCache();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        photoReducedSizeBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte [] data = baos.toByteArray();

        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getUid();

        StorageReference storageReference = FirebaseStorage
                .getInstance().getReference()
                .child(Constants.FIREBASE_CINGLES)
                .child(uid);
        UploadTask uploadTask = storageReference.putBytes(data);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Uri downloadUrl = taskSnapshot.getDownloadUrl();

                Cingle cingle = new Cingle();

                cingle.setTitle(mCingleTitleEditText.getText().toString());
                cingle.setDescription(mCingleDescriptionEditText.getText().toString());
                if(photoReducedSizeBitmap != null){
                    cingle.setCingleImageUrl(downloadUrl.toString());
                }

                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                String uid = firebaseUser.getUid();
                DatabaseReference databaseReference = FirebaseDatabase
                        .getInstance()
                        .getReference(Constants.FIREBASE_CINGLES)
                        .child(uid);

                DatabaseReference pushRef = databaseReference.push();
                String pushId = pushRef.getKey();
                cingle.setPushId(pushId);
                pushRef.setValue(cingle);

                progressDialog.dismiss();

                Toast.makeText(getContext(), "Your Cingle has successfully been posted", Toast.LENGTH_LONG).show();

            }
        });
    }


}

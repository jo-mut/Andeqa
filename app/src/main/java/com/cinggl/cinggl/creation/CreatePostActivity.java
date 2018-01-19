package com.cinggl.cinggl.creation;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cinggl.cinggl.Constants;
import com.cinggl.cinggl.R;
import com.cinggl.cinggl.home.NavigationDrawerActivity;
import com.cinggl.cinggl.utils.ProportionalImageView;
import com.cinggl.cinggl.models.Post;
import com.cinggl.cinggl.models.Cinggulan;
import com.cinggl.cinggl.models.TransactionDetails;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class CreatePostActivity extends AppCompatActivity implements View.OnClickListener{
    @Bind(R.id.cingleTitleEditText)EditText mCingleTitleEditText;
    @Bind(R.id.cingleDescriptionEditText)EditText mCingleDescriptionEditText;
    @Bind(R.id.sharePostImageView)ImageView mPostCingleImageView;
    @Bind(R.id.profileImageView)CircleImageView mProfileImageView;
    @Bind(R.id.usernameTextView)TextView mAccountUsernameTextView;
    @Bind(R.id.postImageView)ProportionalImageView mPostImageView;
    @Bind(R.id.descriptionCountTextView)TextView mDescriptionCountTextView;
    @Bind(R.id.titleCountTextView)TextView mTitleCountTextView;


    private String image;
    private Uri photoUri;
    private static final String KEY_IMAGE = "IMAGE FROM GALLERY";
    private static final String TAG = "CreatePostActivity";
    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;

    private static final int DEFAULT_TITLE_LENGTH_LIMIT = 100;
    private static final int DEFAULT_DESCRIPTION_LENGTH_LIMIT = 500;
    //FIRESTORE
    private FirebaseFirestore firebaseFirestore;
    private CollectionReference postsReference;
    private CollectionReference ownersReference;
    private CollectionReference usersReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);
        ButterKnife.bind(this);

        mPostCingleImageView.setOnClickListener(this);

        firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() != null){

            if (getIntent().getExtras() != null){
                photoUri = Uri.parse(getIntent().getStringExtra("photoUri"));
                loadImage();
            }

            //initialize firestore
            firebaseFirestore =  FirebaseFirestore.getInstance();
            //get the reference to posts(collection reference)
            postsReference = firebaseFirestore.collection(Constants.POSTS);
            ownersReference = firebaseFirestore.collection(Constants.CINGLE_ONWERS);
            usersReference = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);
 
            fetchUserData();
            uploadingToFirebaseDialog();
            mCingleTitleEditText.setFilters(new InputFilter[]{new InputFilter
                    .LengthFilter(DEFAULT_TITLE_LENGTH_LIMIT)});
            mCingleDescriptionEditText.setFilters(new  InputFilter[]{new InputFilter
                    .LengthFilter(DEFAULT_DESCRIPTION_LENGTH_LIMIT)});
            textWatchers();
            getCinglesIntent();
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
            Picasso.with(this).cancelRequest(mPostImageView);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putString(KEY_IMAGE, image);
    }

    private void getCinglesIntent(){
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        String action = intent.getAction();

        //if this is from share action
        if(intent.ACTION_SEND.equals(action)){
            if (bundle.containsKey(Intent.EXTRA_STREAM)){
                //get the resource path
                handleIntentData(intent);
            }
        }
    }

    public void handleIntentData(Intent data){
        Uri imageSelected = data.getParcelableExtra(Intent.EXTRA_STREAM);
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageSelected);
//            mPostImageView.setImageBitmap(bitmap);
            Picasso.with(this)
                    .load(imageSelected)
                    .into(mPostImageView, new Callback.EmptyCallback(){
                        @Override
                        public void onSuccess(){
                            /**index 0 is the mChosenImageView*/
                        }
                    });
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public void fetchUserData(){
        usersReference.document(firebaseAuth.getCurrentUser().getUid())
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (documentSnapshot.exists()) {
                    final Cinggulan cinggulan = documentSnapshot.toObject(Cinggulan.class);
                    final String username = cinggulan.getUsername();
                    final String profileImage = cinggulan.getProfileImage();

                    mAccountUsernameTextView.setText(username);

                    Picasso.with(CreatePostActivity.this)
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
                                    Picasso.with(CreatePostActivity.this)
                                            .load(profileImage)
                                            .fit()
                                            .centerCrop()
                                            .into(mProfileImageView);
                                }
                            });
                }
            }
        });
    }

    @Override
    public void onClick(View v){
        if(v == mPostCingleImageView){
            savingDataToFirebase();
        }

    }

    public void loadImage(){
        /**index 1 is the progress bar and it show as the image is loading*/

        Picasso.with(this)
                .load(photoUri)
                .into(mPostImageView, new Callback.EmptyCallback(){
                    @Override
                    public void onSuccess(){
                        /**index 0 is the mChosenImageView*/
                    }
                });

    }

    public void uploadingToFirebaseDialog(){
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(true);
    }

    private void savingDataToFirebase(){
        if (photoUri != null){
            progressDialog.show();
            mPostCingleImageView.setEnabled(false);
            mPostCingleImageView.setColorFilter(Color.GRAY);
            final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            final String uid = user.getUid();

            final DocumentReference cingleRef = postsReference.document();
            final String pushId = cingleRef.getId();
            StorageReference storageReference = FirebaseStorage
                    .getInstance().getReference()
                    .child(Constants.POSTS)
                    .child(pushId);

            UploadTask uploadTask = storageReference.putFile(photoUri);
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    final Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    CollectionReference cl = postsReference;
                    cl.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot documentSnapshots) {
                            final int index = documentSnapshots.getDocuments().size();
                            Post post = new Post();
                            final Long timeStamp = System.currentTimeMillis();

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

                            final long currentIdex = index + 1;

                            Log.d("downloadUrl post image", downloadUrl.toString());

                            //record all the post data
                            post.setNumber(currentIdex);
                            post.setRandomNumber((double) new Random().nextDouble());
                            post.setTimeStamp(timeStamp);
                            post.setUid(firebaseAuth.getCurrentUser().getUid());
                            post.setPushId(pushId);
                            post.setTitle(mCingleTitleEditText.getText().toString());
                            post.setDescription(mCingleDescriptionEditText.getText().toString());
                            post.setImage(downloadUrl.toString());
                            post.setPushId(pushId);
                            post.setUid(firebaseAuth.getCurrentUser().getUid());
                            post.setCreatorUid(firebaseAuth.getCurrentUser().getUid());
                            post.setDatePosted(currentDate);
                            post.setCingleIndex("Post number" + " " + currentIdex);
                            cingleRef.set(post);

                            //reset input fields
                            mCingleTitleEditText.setText("");
                            mCingleDescriptionEditText.setText("");
                            mPostImageView.setImageBitmap(null);

                            //set the post ownership
                            TransactionDetails transactionDetails = new TransactionDetails();
                            transactionDetails.setPushId(pushId);
                            transactionDetails.setUid(firebaseAuth.getCurrentUser().getUid());
                            transactionDetails.setDate(currentDate);

                            DocumentReference ownerRef = ownersReference.document(pushId);
                            ownerRef.set(transactionDetails);

                            progressDialog.dismiss();


                            Intent intent = new Intent(CreatePostActivity.this, NavigationDrawerActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(CreatePostActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                    progressDialog.setMessage("Adding your Post" + " " + ((int) progress) + "%...");
                    if (progress == 100.0){
                        progressDialog.dismiss();
                    }
                }
            });

        }
    }

}

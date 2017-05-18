package com.cinggl.cinggl.camera;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Parcel;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.cinggl.cinggl.Constants;
import com.cinggl.cinggl.R;
import com.cinggl.cinggl.models.Cingle;
import com.cinggl.cinggl.ui.HomeActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import org.parceler.Parcels;

import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;


public class PostCingleActivity extends AppCompatActivity implements View.OnClickListener{
    @Bind(R.id.cingleTitleEditText)EditText mCingleTitleEditText;
    @Bind(R.id.cingleDescriptionEditText)EditText mCingleDescriptionEditText;
    @Bind(R.id.postCingleImageView)ImageView mPostCingleImageView;
    @Bind(R.id.chosenImageView)ImageView mChosenImageView;
    @Bind(R.id.laceTextView)TextView mLaceTextView;

    private Cingle cingle;
    private DatabaseReference databaseReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_cingle);
        ButterKnife.bind(this);

        mPostCingleImageView.setOnClickListener(this);

        if(getIntent().hasExtra("byteArray")){
            Bitmap bitmap = BitmapFactory.decodeByteArray(getIntent()
                    .getByteArrayExtra("byteArray"), 0, getIntent()
                    .getByteArrayExtra("byteArray").length);
            mChosenImageView.setImageBitmap(bitmap);
        }

//        //CONFIRM THAT THE IMAGE BE RETRIEVED IS THE SAME IMAGE THAT WAS ENCODED AND SAVED
//        if(!cingle.getCingleImageUrl().contains("http")){
//
//            try {
//                Bitmap bitmapImage = decodeFromFirebaseBase64(cingle.getCingleImageUrl());
//                mChosenImageView.setImageBitmap(bitmapImage);
//            }catch (IOException e){
//                e.printStackTrace();
//            }
//
//        }else {
//            Picasso.with(getApplicationContext())
//                    .load(cingle.getCingleImageUrl())
//                    .fit()
//                    .centerCrop()
//                    .into(mChosenImageView);
//        }
    }

    @Override
    public void onClick(View v){
        if(v == mPostCingleImageView){
            saveToFirebase();
        }
    }

    public static Bitmap decodeFromFirebaseBase64(String image) throws IOException {
        byte[] decodedByteArray = android.util.Base64.decode(image, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedByteArray, 0, decodedByteArray.length);
    }

    public void saveToFirebase(){
        //CREATE A NEW CINGLE OBJECT AND GET THE INPUTTED TEXT
        cingle = new Cingle();
        cingle.setTitle(mCingleTitleEditText.getText().toString().trim());
        cingle.setDescription(mCingleDescriptionEditText.getText().toString().trim());

        //GET CURRENT SIGNED IN USER
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getUid();

        //SAVE DATA TO FIREBASE
        databaseReference = FirebaseDatabase.getInstance()
                .getReference(Constants.FIREBASE_CINGLES)
                .child(uid);
        DatabaseReference pushRef = databaseReference.push();
        String pushId = pushRef.getKey();
        cingle.setPushId(pushId);
        pushRef.setValue(cingle);


        mCingleTitleEditText.setText("");
        mCingleDescriptionEditText.setText("");

        Intent intent = new Intent(PostCingleActivity.this, HomeActivity.class);
        startActivity(intent);

    }

}

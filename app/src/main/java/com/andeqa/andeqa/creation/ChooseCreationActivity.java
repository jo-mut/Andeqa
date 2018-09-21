package com.andeqa.andeqa.creation;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import com.andeqa.andeqa.R;
import com.andeqa.andeqa.camera.CameraActivity;
import com.andeqa.andeqa.camera.PicturesActivity;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ChooseCreationActivity extends AppCompatActivity implements View.OnClickListener{
    @Bind(R.id.galleryLinearLayout)LinearLayout mGalleryLinearLayout;
    @Bind(R.id.collectionLinearLayout)LinearLayout mCollectionLinearLayout;
    @Bind(R.id.cameraLinearLayout)LinearLayout mCameraLinearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_creation);
        ButterKnife.bind(this);

        mCameraLinearLayout.setOnClickListener(this);
        mGalleryLinearLayout.setOnClickListener(this);
        mCollectionLinearLayout.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == mCameraLinearLayout){
            Intent intent = new Intent(this, CameraActivity.class);
            startActivity(intent);
        }

        if (v == mGalleryLinearLayout){
            Intent intent = new Intent(this, PicturesActivity.class);
            startActivity(intent);
        }

        if (v == mCollectionLinearLayout){
            Intent intent = new Intent(this, CreateCollectionActivity.class);
            startActivity(intent);
        }

    }
}

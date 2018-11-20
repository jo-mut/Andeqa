package com.andeqa.andeqa.creation;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;

import com.andeqa.andeqa.R;
import com.andeqa.andeqa.utils.ProportionalImageView;
import com.bumptech.glide.Glide;

import java.io.File;

import butterknife.Bind;
import butterknife.ButterKnife;

public class PreviewImagePostActivity extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG = PreviewImagePostActivity.class.getSimpleName();
    @Bind(R.id.postImageView)ProportionalImageView postImageView;
    @Bind(R.id.toolbar)Toolbar mToolbar;
    @Bind(R.id.nextImageView)ImageView mNextImageView;

    private static final String COLLECTION_ID = "collection id";
    private static final String CAMERA_PATH = "camera image";
    private static final String PHOTO_URI = "photo uri";
    private static final String GALLERY_PATH ="gallery image";
    private static final String HEIGHT = "height";
    private static final String WIDTH = "width";
    private static final String EXTRA_POST_ID = "post id";
    private String mCollectionId;
    private String galleryImage;
    private String cameraImage;
    private String postId;
    private String video;
    private int height;
    private int width;
    private Uri photoUri;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_image);
        ButterKnife.bind(this);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Photo");
        toolbar.setNavigationIcon(R.drawable.ic_arrow);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        mNextImageView.setOnClickListener(this);

        getShareIntent();

        mCollectionId = getIntent().getStringExtra(COLLECTION_ID);
        galleryImage = getIntent().getStringExtra(GALLERY_PATH);
        cameraImage = getIntent().getStringExtra(CAMERA_PATH);
        postId = getIntent().getStringExtra(EXTRA_POST_ID);

        if (cameraImage != null){
            Glide.with(PreviewImagePostActivity.this)
                    .asBitmap()
                    .load(new File(cameraImage))
                    .into(postImageView);
        }

        if (galleryImage != null){
            Glide.with(PreviewImagePostActivity.this)
                    .asBitmap()
                    .load(new File(galleryImage))
                    .into(postImageView);
        }

        if (mCollectionId != null){
            Glide.with(PreviewImagePostActivity.this)
                    .asBitmap()
                    .load(new File(galleryImage))
                    .into(postImageView);
        }

    }

    private void getShareIntent(){
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
        photoUri = data.getParcelableExtra(Intent.EXTRA_STREAM);
        if (photoUri != null){
            postImageView.setVisibility(View.VISIBLE);
            Glide.with(this)
                    .asBitmap()
                    .load(photoUri)
                    .into(postImageView);
        }

    }

    @Override
    public void onClick(View v){
        if (v == mNextImageView){
            height = postImageView.getDrawable().getIntrinsicHeight();
            width = postImageView.getDrawable().getIntrinsicWidth();

            if (postId != null){
                if (cameraImage != null){
                    Intent intent = new Intent(PreviewImagePostActivity.this, CreateSingleActivity.class);
                    intent.putExtra(PreviewImagePostActivity.GALLERY_PATH, cameraImage);
                    intent.putExtra(PreviewImagePostActivity.HEIGHT, height + "");
                    intent.putExtra(PreviewImagePostActivity.WIDTH, width + "");
                    startActivity(intent);
                    finish();
                }

                if (video != null){
                    Intent intent = new Intent(PreviewImagePostActivity.this, CreateSingleActivity.class);
                    intent.putExtra(PreviewImagePostActivity.PHOTO_URI, video);
                    intent.putExtra(PreviewImagePostActivity.HEIGHT, height + "");
                    intent.putExtra(PreviewImagePostActivity.WIDTH, width + "");
                    startActivity(intent);
                    finish();
                }

                if (photoUri != null){
                    Intent intent = new Intent(PreviewImagePostActivity.this, CreateSingleActivity.class);
                    intent.putExtra(PreviewImagePostActivity.PHOTO_URI, photoUri.toString());
                    intent.putExtra(PreviewImagePostActivity.HEIGHT, height + "");
                    intent.putExtra(PreviewImagePostActivity.WIDTH, width + "");
                    startActivity(intent);
                    finish();
                }

            }else {
                if (cameraImage != null){
                    Intent intent = new Intent(PreviewImagePostActivity.this, CreateSingleActivity.class);
                    intent.putExtra(PreviewImagePostActivity.GALLERY_PATH, cameraImage);
                    intent.putExtra(PreviewImagePostActivity.HEIGHT, height + "");
                    intent.putExtra(PreviewImagePostActivity.WIDTH, width + "");
                    startActivity(intent);
                    finish();
                }

                if (video != null){
                    Intent intent = new Intent(PreviewImagePostActivity.this, CreateSingleActivity.class);
                    intent.putExtra(PreviewImagePostActivity.PHOTO_URI, video);
                    intent.putExtra(PreviewImagePostActivity.HEIGHT, height + "");
                    intent.putExtra(PreviewImagePostActivity.WIDTH, width + "");
                    startActivity(intent);
                    finish();
                }

                if (photoUri != null){
                    Intent intent = new Intent(PreviewImagePostActivity.this, CreateSingleActivity.class);
                    intent.putExtra(PreviewImagePostActivity.PHOTO_URI, photoUri.toString());
                    intent.putExtra(PreviewImagePostActivity.HEIGHT, height + "");
                    intent.putExtra(PreviewImagePostActivity.WIDTH, width + "");
                    startActivity(intent);
                    finish();
                }
            }

            if (mCollectionId != null){
                Intent intent = new Intent(PreviewImagePostActivity.this, CreateCollectionPostActivity.class);
                intent.putExtra(PreviewImagePostActivity.GALLERY_PATH, galleryImage);
                intent.putExtra(PreviewImagePostActivity.COLLECTION_ID, mCollectionId);
                intent.putExtra(PreviewImagePostActivity.HEIGHT, height + "");
                intent.putExtra(PreviewImagePostActivity.WIDTH, width + "");
                startActivity(intent);
                finish();
            }


        }
    }
}

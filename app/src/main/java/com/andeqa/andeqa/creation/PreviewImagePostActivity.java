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

    private static final String HEIGHT = "height";
    private static final String WIDTH = "width";
    private static final String IMAGE_PATH ="image path";
    private static final String COLLECTION_ID = "collection id";
    private static final String POST_ID = "post id";
    private String mCollectionId;
    private String postId;
    private String image;
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
        image = getIntent().getStringExtra(IMAGE_PATH);
        mCollectionId = getIntent().getStringExtra(COLLECTION_ID);
        postId = getIntent().getStringExtra(POST_ID);

    }


    @Override
    protected void onStart() {
        super.onStart();
        if (image != null){
            Glide.with(PreviewImagePostActivity.this)
                    .asBitmap()
                    .load(new File(image))
                    .into(postImageView);
        }
    }


    @Override
    public void onClick(View v){
        if (v == mNextImageView){
            height = postImageView.getDrawable().getIntrinsicHeight();
            width = postImageView.getDrawable().getIntrinsicWidth();

            if (image != null && mCollectionId != null){
                Intent intent = new Intent(this, CreatePostActivity.class);
                intent.putExtra(PreviewImagePostActivity.IMAGE_PATH, image);
                intent.putExtra(PreviewImagePostActivity.HEIGHT, height + "");
                intent.putExtra(PreviewImagePostActivity.WIDTH, width + "");
                intent.putExtra(PreviewImagePostActivity.COLLECTION_ID, mCollectionId);
                startActivity(intent);
                finish();
            }else if (image != null && postId != null){
                Intent intent = new Intent(this, CreatePostActivity.class);
                intent.putExtra(PreviewImagePostActivity.IMAGE_PATH, image);
                intent.putExtra(PreviewImagePostActivity.HEIGHT, height + "");
                intent.putExtra(PreviewImagePostActivity.WIDTH, width + "");
                intent.putExtra(PreviewImagePostActivity.POST_ID, postId);
                startActivity(intent);
                finish();
            }else {
                Intent intent = new Intent(this, CreatePostActivity.class);
                intent.putExtra(PreviewImagePostActivity.IMAGE_PATH, image);
                intent.putExtra(PreviewImagePostActivity.HEIGHT, height + "");
                intent.putExtra(PreviewImagePostActivity.WIDTH, width + "");
                startActivity(intent);
                finish();
            }

        }
    }

    private void getShareIntent(){
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        String action = intent.getAction();

        //if this is from share action
        if(intent.ACTION_SEND.equals(action)){
            if (bundle.containsKey(Intent.EXTRA_STREAM)){
                //get the resource image
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
}

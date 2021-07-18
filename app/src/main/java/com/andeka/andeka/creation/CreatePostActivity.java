package com.andeka.andeka.creation;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.andeka.andeka.R;
import com.bumptech.glide.Glide;
import com.squareup.picasso.Picasso;

import java.io.File;

import butterknife.Bind;
import butterknife.ButterKnife;

public class CreatePostActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String IMAGE_PATH ="image path";
    private static final String VIDEO_PATH = "video path";
    private static final String HEIGHT = "height";
    private static final String TITLE = "title";
    private static final String DESCRIPTION = "description";
    private static final String WIDTH = "width";
    private static final String COLLECTION_ID = "collection id";
    private static final String POST_ID = "post id";
    private String mCollectionId;
    private String postId;
    private String height;
    private String width;
    private String image;
    private String video;

    private static final int DEFAULT_TITLE_LENGTH_LIMIT = 100;

    @Bind(R.id.postImageView)ImageView mPostImageView;
    @Bind(R.id.titleEditText)EditText mTitleEditText;
    @Bind(R.id.descriptionEditText)EditText mDescriptionEditText;
    @Bind(R.id.postPostImageView)ImageView mPostPostImageView;
    @Bind(R.id.descriptionCountTextView)TextView mDescriptionCountTextView;
    @Bind(R.id.titleCountTextView)TextView mTitleCountTextView;
    @Bind(R.id.postCardView)RelativeLayout mPostRelativeLayout;
    @Bind(R.id.toolbar)Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_black);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        mPostPostImageView.setOnClickListener(this);

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);


        mTitleEditText.setFilters(new InputFilter[]{new InputFilter
                .LengthFilter(DEFAULT_TITLE_LENGTH_LIMIT)});

        textWatchers();


        image = getIntent().getStringExtra(IMAGE_PATH);

        video = getIntent().getStringExtra(VIDEO_PATH);
        mCollectionId = getIntent().getStringExtra(COLLECTION_ID);
        postId = getIntent().getStringExtra(POST_ID);

        width = getIntent().getStringExtra(WIDTH);
        height = getIntent().getStringExtra(HEIGHT);

    }


    @Override
    protected void onPause(){
        super.onPause();
        if(isFinishing()){
            Picasso.with(this).cancelRequest(mPostImageView);
        }
    }

    private void textWatchers(){
        //TITLE TEXT WATCHER
        mTitleEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                int count = DEFAULT_TITLE_LENGTH_LIMIT - mTitleEditText.getText().length();
                mTitleCountTextView.setText(Integer.toString(count));

                if (count < 0){
                }else if (count < 100){
                    mTitleCountTextView.setTextColor(Color.GRAY);
                }else {
                    mTitleCountTextView.setTextColor(Color.BLACK);
                }

            }
        });

    }


    @Override
    protected void onStart() {
        super.onStart();
        if (image != null){
            Glide.with(CreatePostActivity.this)
                    .asBitmap()
                    .load(new File(image))
                    .into(mPostImageView);
        }

        if (video != null) {
            Glide.with(CreatePostActivity.this)
                    .asBitmap()
                    .load(new File(video))
                    .into(mPostImageView);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onClick(View v){
        if(v == mPostPostImageView){
           if (image != null){
               if (mCollectionId != null){
                   Bundle bundle = new Bundle();
                   bundle.putString(CreatePostActivity.TITLE, mTitleEditText.getText().toString());
                   bundle.putString(CreatePostActivity.DESCRIPTION, mDescriptionEditText.getText().toString());
                   bundle.putString(CreatePostActivity.WIDTH, width);
                   bundle.putString(CreatePostActivity.HEIGHT, height);
                   bundle.putString(CreatePostActivity.IMAGE_PATH, image);
                   bundle.putString(CreatePostActivity.COLLECTION_ID, mCollectionId);
                   DialogProgressFragment fragment = new DialogProgressFragment();
                   fragment.setArguments(bundle);
                   fragment.show(getSupportFragmentManager(), "new post");
               }else if (postId != null){
                   Bundle bundle = new Bundle();
                   bundle.putString(CreatePostActivity.TITLE, mTitleEditText.getText().toString());
                   bundle.putString(CreatePostActivity.DESCRIPTION, mDescriptionEditText.getText().toString());
                   bundle.putString(CreatePostActivity.WIDTH, width);
                   bundle.putString(CreatePostActivity.HEIGHT, height);
                   bundle.putString(CreatePostActivity.IMAGE_PATH, image);
                   bundle.putString(CreatePostActivity.POST_ID, postId);
                   DialogProgressFragment fragment = new DialogProgressFragment();
                   fragment.setArguments(bundle);
                   fragment.show(getSupportFragmentManager(), "new post");
               }else {
                   Bundle bundle = new Bundle();
                   bundle.putString(CreatePostActivity.TITLE, mTitleEditText.getText().toString());
                   bundle.putString(CreatePostActivity.DESCRIPTION, mDescriptionEditText.getText().toString());
                   bundle.putString(CreatePostActivity.WIDTH, width);
                   bundle.putString(CreatePostActivity.HEIGHT, height);
                   bundle.putString(CreatePostActivity.IMAGE_PATH, image);
                   bundle.putString(CreatePostActivity.COLLECTION_ID, mCollectionId);
                   DialogProgressFragment fragment = new DialogProgressFragment();
                   fragment.setArguments(bundle);
                   fragment.show(getSupportFragmentManager(), "new post");
               }
           }

           if (video != null) {
               if (mCollectionId != null){
                   Bundle bundle = new Bundle();
                   bundle.putString(CreatePostActivity.TITLE, mTitleEditText.getText().toString());
                   bundle.putString(CreatePostActivity.DESCRIPTION, mDescriptionEditText.getText().toString());
                   bundle.putString(CreatePostActivity.VIDEO_PATH, video);
                   bundle.putString(CreatePostActivity.COLLECTION_ID, mCollectionId);
                   DialogProgressFragment fragment = new DialogProgressFragment();
                   fragment.setArguments(bundle);
                   fragment.show(getSupportFragmentManager(), "new post");
               }else if (postId != null){
                   Bundle bundle = new Bundle();
                   bundle.putString(CreatePostActivity.TITLE, mTitleEditText.getText().toString());
                   bundle.putString(CreatePostActivity.DESCRIPTION, mDescriptionEditText.getText().toString());
                   bundle.putString(CreatePostActivity.VIDEO_PATH, video);
                   bundle.putString(CreatePostActivity.POST_ID, postId);
                   DialogProgressFragment fragment = new DialogProgressFragment();
                   fragment.setArguments(bundle);
                   fragment.show(getSupportFragmentManager(), "new post");
               }else {
                   Bundle bundle = new Bundle();
                   bundle.putString(CreatePostActivity.TITLE, mTitleEditText.getText().toString());
                   bundle.putString(CreatePostActivity.DESCRIPTION, mDescriptionEditText.getText().toString());
                   bundle.putString(CreatePostActivity.VIDEO_PATH, video);
                   bundle.putString(CreatePostActivity.COLLECTION_ID, mCollectionId);
                   DialogProgressFragment fragment = new DialogProgressFragment();
                   fragment.setArguments(bundle);
                   fragment.show(getSupportFragmentManager(), "new post");
               }
           }

        }

    }

}

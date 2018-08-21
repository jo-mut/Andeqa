package com.andeqa.andeqa.camera;

import android.Manifest;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.andeqa.andeqa.R;
import com.andeqa.andeqa.creation.CreateCollectionActivity;
import com.andeqa.andeqa.creation.CreateCollectionPostActivity;
import com.andeqa.andeqa.settings.CollectionSettingsActivity;

import butterknife.ButterKnife;

public class PicturesActivity extends AppCompatActivity {
    private static final String TAG = PicturesActivity.class.getSimpleName();
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private GalleryPagerAdapter galleryPagerAdapter;
    private static final String EXTRA_USER_UID = "uid";
    private static final String GALLERY_PATH ="gallery image";
    private static final String POST_TAG = CreateCollectionPostActivity.class.getSimpleName();
    private static final String COLLECTION_TAG = CreateCollectionActivity.class.getSimpleName();
    private static final String COLLECTION_SETTINGS_COVER = CollectionSettingsActivity.class.getSimpleName();
    private static final String PROFILE_PHOTO_PATH = "profile photo path";
    private static final String PROFILE_COVER_PATH = "profile cover path";
    private static final String COLLECTION_ID = "collection id";
    private static final String EXTRA_ROOM_ID = "roomId";

    private String mUid;
    private String mRoomId;
    private String postIntent;
    private String collectionIntent;
    private String collectionId;
    private String profileCoverIntent;
    private String profilePhotoIntent;
    private String collectionSettingsIntent;

    private RunTimePermissions runTimePermission;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        ButterKnife.bind(this);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationIcon(R.drawable.ic_arrow);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (runTimePermission != null) {
            runTimePermission.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }


    @Override
    protected void onStart() {
        super.onStart();
        runTimePermission = new RunTimePermissions(this);
        runTimePermission.requestPermission(new String[]{Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        }, new RunTimePermissions.RunTimePermissionListener() {

            @Override
            public void permissionGranted() {
                // First we need to check availability of play services
              initializeGallery();
            }

            @Override
            public void permissionDenied() {

            }
        });
    }

    private void initializeGallery(){
        postIntent = getIntent().getStringExtra(POST_TAG);
        collectionId = getIntent().getStringExtra(COLLECTION_ID);
        collectionIntent = getIntent().getStringExtra(COLLECTION_TAG);
        collectionSettingsIntent  = getIntent().getStringExtra(COLLECTION_SETTINGS_COVER);
        profileCoverIntent = getIntent().getStringExtra(PROFILE_COVER_PATH);
        profilePhotoIntent = getIntent().getStringExtra(PROFILE_PHOTO_PATH);
        mUid = getIntent().getStringExtra(EXTRA_USER_UID);
        mRoomId = getIntent().getStringExtra(EXTRA_ROOM_ID);

        galleryPagerAdapter = new GalleryPagerAdapter(getSupportFragmentManager());
        tabLayout = (TabLayout)findViewById(R.id.tabs);
        viewPager = (ViewPager)findViewById(R.id.container);
        viewPager.setAdapter(galleryPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);

        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(viewPager));
    }
}


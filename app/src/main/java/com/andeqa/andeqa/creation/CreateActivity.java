package com.andeqa.andeqa.creation;

import android.Manifest;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.andeqa.andeqa.R;
import com.andeqa.andeqa.gallery.GalleryPagerAdapter;
import com.andeqa.andeqa.utils.RunTimePermissions;
import com.andeqa.andeqa.settings.CollectionSettingsActivity;

import butterknife.ButterKnife;

public class CreateActivity extends AppCompatActivity {
    private static final String TAG = CreateActivity.class.getSimpleName();
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private GalleryPagerAdapter galleryPagerAdapter;
    private int [] tabIcons = {
            R.drawable.ic_camera,
            R.drawable.ic_photo,
            R.drawable.ic_collection};

    private RunTimePermissions runTimePermission;

    private static final String GALLERY_PATH ="gallery image";
    private static final String GALLERY_VIDEO ="gallery video";
    private static final String COLLECTION_TAG = CreateCollectionActivity.class.getSimpleName();
    private static final String COLLECTION_SETTINGS_COVER = CollectionSettingsActivity.class.getSimpleName();
    private static final String COLLECTION_POST = CreateCollectionPostActivity.class.getSimpleName();
    private static final String PROFILE_PHOTO_PATH = "profile photo path";
    private static final String PROFILE_COVER_PATH = "profile cover path";
    private static final String COLLECTION_ID = "collection id";
    private static final String EXTRA_ROOM_ID = "roomId";
    private static final String EXTRA_USER_UID = "uid";
    private static final String EXTRA_POST_ID = "post id";
    private static final String CAMERA_PATH = "camera image";
    private static final String CAMERA_VIDEO = "camera video";
    private static final String CAMERA_THUMB = "thumb";
    private static final String PREVIEW_POST = "preview post";

    private String mUid;
    private String mRoomId;
    private String collection_post;
    private String createCollection;
    private String collectionId;
    private String profileCoverIntent;
    private String profilePhotoIntent;
    private String collectionSettingsIntent;
    private String  album_name;
    private String postId;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);
        ButterKnife.bind(this);

        if (getIntent().getExtras() != null){
            album_name = getIntent().getStringExtra("name");
            collection_post = getIntent().getStringExtra(COLLECTION_POST);
            collectionId = getIntent().getStringExtra(COLLECTION_ID);
            createCollection = getIntent().getStringExtra(COLLECTION_TAG);
            collectionSettingsIntent  = getIntent().getStringExtra(COLLECTION_SETTINGS_COVER);
            profileCoverIntent = getIntent().getStringExtra(PROFILE_COVER_PATH);
            profilePhotoIntent = getIntent().getStringExtra(PROFILE_PHOTO_PATH);
            mUid = getIntent().getStringExtra(EXTRA_USER_UID);
            mRoomId = getIntent().getStringExtra(EXTRA_ROOM_ID);
            postId = getIntent().getStringExtra(EXTRA_POST_ID);
            Log.d("post id", postId);

        }


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
        galleryPagerAdapter = new GalleryPagerAdapter(getSupportFragmentManager());
        tabLayout = (TabLayout)findViewById(R.id.tabs);
        viewPager = (ViewPager)findViewById(R.id.container);
        viewPager.setAdapter(galleryPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
//        setUpTabIcons();

        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(viewPager));
    }

    private void setUpTabIcons(){
        tabLayout.getTabAt(0).setIcon(tabIcons[0]);
        tabLayout.getTabAt(1).setIcon(tabIcons[1]);
        tabLayout.getTabAt(2).setIcon(tabIcons[2]);
    }
}


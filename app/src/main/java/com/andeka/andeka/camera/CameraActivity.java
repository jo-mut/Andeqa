package com.andeka.andeka.camera;

import android.Manifest;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.andeka.andeka.R;
import com.andeka.andeka.gallery.GalleryPagerAdapter;
import com.andeka.andeka.utils.RunTimePermissions;

import butterknife.ButterKnife;

public class CameraActivity extends AppCompatActivity {
    private static final String TAG = CameraActivity.class.getSimpleName();
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private GalleryPagerAdapter galleryPagerAdapter;
    private int [] tabIcons = {
            R.drawable.ic_camera,
            R.drawable.ic_photo,
            R.drawable.ic_collection};

    private RunTimePermissions runTimePermission;

    private static final String COLLECTION_ID = "collection id";
    private static final String POST_ID = "post id";
    private String mCollectionId;
    private String postId;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        ButterKnife.bind(this);

        if (getIntent().getExtras() != null){
            mCollectionId = getIntent().getStringExtra(COLLECTION_ID);
            postId = getIntent().getStringExtra(POST_ID);

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
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
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


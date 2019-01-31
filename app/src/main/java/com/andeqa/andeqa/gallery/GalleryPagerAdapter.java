package com.andeqa.andeqa.gallery;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.andeqa.andeqa.camera.CameraFragment;

public class GalleryPagerAdapter extends FragmentPagerAdapter {
    private static final String TAG = GalleryPagerAdapter.class.getSimpleName();

    public GalleryPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                CameraFragment cameraFragment = new CameraFragment();
                return cameraFragment;
            case 1:
                AlbumFragment albumFragment = new AlbumFragment();
                return albumFragment;
        }
        return null;
    }

    @Override
    public int getCount() {
        // Show 3 total pages.
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Camera";
            case 1:
                return "Gallery";
        }
        return null;
    }
}

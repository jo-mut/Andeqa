package com.andeqa.andeqa.camera;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.andeqa.andeqa.gallery.PicturesFragment;

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
                PicturesFragment picturesFragment = new PicturesFragment();
                return picturesFragment;
        }
        return null;
    }

    @Override
    public int getCount() {
        // Show 2 total pages.
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Camera";
            case 1:
                return "Photos";
        }
        return null;
    }
}

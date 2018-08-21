package com.andeqa.andeqa.camera;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class GalleryPagerAdapter extends FragmentPagerAdapter {
    private static final String TAG = GalleryPagerAdapter.class.getSimpleName();

    public GalleryPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                PicturesFragment picturesFragment = new PicturesFragment();
                return picturesFragment;
            case 1:
                AlbumFragment albumFragment = new AlbumFragment();
                return albumFragment;
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
                return "Pictures";
            case 1:
                return "Albums";
        }
        return null;
    }
}

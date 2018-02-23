package com.andeqa.andeqa.creation;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by J.EL on 2/23/2018.
 */

public class CreationPagerAdapter extends FragmentPagerAdapter {

    public CreationPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                CreationGalleryFragment creationGalleryFragment = new CreationGalleryFragment();
                return creationGalleryFragment;
            case 1:
                CreationCameraFragment creationCameraFragment = new CreationCameraFragment();
                return creationCameraFragment;
            case 2:
                CreationArticleFragment creationArticleFragment = new CreationArticleFragment();
                return creationArticleFragment;
        }
        return null;
    }

    @Override
    public int getCount() {
        // Show 3 total pages.
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "GALLERY";
            case 1:
                return "CAMERA";
            case 2:
                return "ARTICLE";
        }
        return null;
    }

}
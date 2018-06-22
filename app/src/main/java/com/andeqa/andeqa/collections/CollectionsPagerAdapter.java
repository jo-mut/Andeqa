package com.andeqa.andeqa.collections;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class CollectionsPagerAdapter extends FragmentPagerAdapter {
    private static final String TAG = CollectionsPagerAdapter.class.getSimpleName();

    public CollectionsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                FeaturedCollectionsFragment featuredCollectionsFragment = new FeaturedCollectionsFragment();
                return featuredCollectionsFragment;
            case 1:
                MineCollectionFragment mineCollectionFragment = new MineCollectionFragment();
                return mineCollectionFragment;
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
                return "Featured";
            case 1:
                return "Mine";

        }
        return null;
    }
}

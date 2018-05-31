package com.andeqa.andeqa.explore;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.andeqa.andeqa.collections.CollectionsPagerAdapter;
import com.andeqa.andeqa.collections.FeaturedCollectionFragment;
import com.andeqa.andeqa.collections.MineCollectionsFragment;

public class ExplorePagerAdapter extends FragmentPagerAdapter {
    private static final String TAG = FragmentPagerAdapter.class.getSimpleName();

    public ExplorePagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                BestPostsFragment bestPostsFragment = new BestPostsFragment();
                return bestPostsFragment;
            case 1:
                SellingFragment sellingFragment = new SellingFragment();
                return sellingFragment;

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
                return "Best 100";
            case 1:
                return "Selling";

        }
        return null;
    }
}

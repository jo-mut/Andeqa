package com.cinggl.cinggl.home;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * Created by J.EL on 5/29/2017.
 */


/**
 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */

public class HomePagerAdapter extends FragmentStatePagerAdapter {
    private static final String TAG = HomePagerAdapter.class.getSimpleName();

    public HomePagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                SingleOutFragment singleOutFragment = new SingleOutFragment();
                return singleOutFragment;
            case 1:
                BestPostsFragment bestPostsFragment = new BestPostsFragment();
                return bestPostsFragment;
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
                return "Single Out";
            case 1:
                return "Top";
        }
        return null;
    }

}


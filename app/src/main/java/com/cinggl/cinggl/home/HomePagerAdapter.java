package com.cinggl.cinggl.home;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by J.EL on 5/29/2017.
 */


/**
 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */

public class HomePagerAdapter extends FragmentPagerAdapter {
    private static final String TAG = HomePagerAdapter.class.getSimpleName();

    public HomePagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                CingleOutFragment cingleOutFragment = new CingleOutFragment();
                return cingleOutFragment;
            case 1:
                BestCinglesFragment bestCinglesFragment = new BestCinglesFragment();
                return bestCinglesFragment;
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
                return "Cingle Out";
            case 1:
                return "Cingles";
        }
        return null;
    }
}


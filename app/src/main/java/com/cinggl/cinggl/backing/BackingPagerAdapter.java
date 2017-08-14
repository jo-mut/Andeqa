package com.cinggl.cinggl.backing;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.cinggl.cinggl.home.BestCinglesFragment;
import com.cinggl.cinggl.home.CingleOutFragment;
import com.cinggl.cinggl.home.HomePagerAdapter;

/**
 * Created by J.EL on 8/14/2017.
 */

public class BackingPagerAdapter extends FragmentPagerAdapter {
    private static final String TAG = BackingPagerAdapter.class.getSimpleName();

    public BackingPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                BackingOverallFragment backingOverallFragment = new BackingOverallFragment();
                return backingOverallFragment;
            case 1:
                BackingMineFragment backingMineFragment =  new BackingMineFragment();
                return backingMineFragment;
            case 2:
                BackingOursFragment backingOursFragment = new BackingOursFragment();
                return backingOursFragment;
        }
        return null;
    }

    @Override
    public int getCount() {
        // Show 2 total pages.
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Overall";
            case 1:
                return "Mine";
            case 2:
                return "Ours";
        }
        return null;
    }

}

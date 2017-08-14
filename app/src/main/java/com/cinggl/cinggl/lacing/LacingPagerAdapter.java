package com.cinggl.cinggl.lacing;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.cinggl.cinggl.backing.BackingMineFragment;
import com.cinggl.cinggl.backing.BackingOursFragment;
import com.cinggl.cinggl.backing.BackingOverallFragment;
import com.cinggl.cinggl.backing.BackingPagerAdapter;

/**
 * Created by J.EL on 8/14/2017.
 */

public class LacingPagerAdapter extends FragmentPagerAdapter {

    private static final String TAG = LacingPagerAdapter.class.getSimpleName();

    public LacingPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                LacingOverallFragment lacingOverallFragment =  new LacingOverallFragment();
                return lacingOverallFragment;
            case 1:
                LacingMineFragment lacingMineFragment = new LacingMineFragment();
                return  lacingMineFragment;
            case 2:
                LacingOursFragment lacingOursFragment = new LacingOursFragment();
                return  lacingOursFragment;
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

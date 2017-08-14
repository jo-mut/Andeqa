package com.cinggl.cinggl.leasing;

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

public class LeasingPagerAdapter extends FragmentPagerAdapter {
    private static final String TAG = LeasingPagerAdapter.class.getSimpleName();

    public LeasingPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                LeasingOverallFragment leasingOverallFragment = new LeasingOverallFragment();
                return  leasingOverallFragment;
            case 1:
                LeasingMineFragment leasingMineFragment = new LeasingMineFragment();
                return  leasingMineFragment;
            case 2:
                LeasingOursFragment leasingOursFragment = new LeasingOursFragment();
                return  leasingOursFragment;
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

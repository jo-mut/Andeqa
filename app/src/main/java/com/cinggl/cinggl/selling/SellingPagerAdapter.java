package com.cinggl.cinggl.selling;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.cinggl.cinggl.leasing.LeasingMineFragment;
import com.cinggl.cinggl.leasing.LeasingOursFragment;
import com.cinggl.cinggl.leasing.LeasingOverallFragment;
import com.cinggl.cinggl.leasing.LeasingPagerAdapter;

/**
 * Created by J.EL on 8/14/2017.
 */

public class SellingPagerAdapter extends FragmentPagerAdapter {

    private static final String TAG = SellingPagerAdapter.class.getSimpleName();

    public SellingPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                SellingOverallFragment sellingOverallFragment = new SellingOverallFragment();
                return sellingOverallFragment;
            case 1:
                SellingMineFragment sellingMineFragment = new SellingMineFragment();
                return sellingMineFragment;
            case 2:
                SellingOursFragment sellingOursFragment = new SellingOursFragment();
                return  sellingOursFragment;
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

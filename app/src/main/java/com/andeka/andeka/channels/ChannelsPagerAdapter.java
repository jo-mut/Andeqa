package com.andeka.andeka.channels;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.andeka.andeka.more.MoreFragment;

public class ChannelsPagerAdapter extends FragmentPagerAdapter {

    private static final String TAG = ChannelsPagerAdapter.class.getSimpleName();

    public ChannelsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                VideosFragment videosFragment = new VideosFragment();
                return videosFragment;
            case 1:
                MoreFragment moreFragment = new MoreFragment();
                return moreFragment;
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
                return "Videos";
            case 1:
                return "More";
        }
        return null;
    }

}

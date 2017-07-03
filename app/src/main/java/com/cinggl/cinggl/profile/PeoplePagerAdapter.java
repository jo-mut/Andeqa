package com.cinggl.cinggl.profile;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by J.EL on 7/2/2017.
 */

public class PeoplePagerAdapter extends FragmentPagerAdapter{
    private static final String TAG = PeoplePagerAdapter.class.getSimpleName();

    public PeoplePagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                FollowersFragment followersFragment = new FollowersFragment();
                return followersFragment;
            case 1:
                FollowingFragment followingFragment = new FollowingFragment();
                return followingFragment;
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
                return "Followers";
            case 1:
                return "Following";

        }
        return null;
    }
}

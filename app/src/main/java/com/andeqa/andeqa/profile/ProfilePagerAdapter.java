package com.andeqa.andeqa.profile;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.andeqa.andeqa.collections.MineCollectionsFragment;

public class ProfilePagerAdapter extends FragmentStatePagerAdapter {

    private static final String TAG = ProfilePagerAdapter.class.getSimpleName();

    public ProfilePagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                SinglesFragment singlesFragment = new SinglesFragment();
                return singlesFragment;
            case 1:
                ProfileCollectionsFragment profileCollectionsFragment = new ProfileCollectionsFragment();
                return  profileCollectionsFragment;
            case 2:
                PostsFragment postsFragment = new PostsFragment();
                return postsFragment;

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
                return "Singles";
            case 1:
                return "Collections";
            case 2:
                return "Posts";

        }
        return null;
    }
}

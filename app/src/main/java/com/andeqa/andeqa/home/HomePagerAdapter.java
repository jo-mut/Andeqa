package com.andeqa.andeqa.home;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.andeqa.andeqa.collections.CollectionFragment;
import com.andeqa.andeqa.market.MarketFragment;
import com.andeqa.andeqa.timeline.TimelineFragment;

public class HomePagerAdapter extends FragmentStatePagerAdapter {

    public HomePagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                HomeFragment homeFragment = new HomeFragment();
                return homeFragment;
            case 1:
                CollectionFragment collectionsFragment = new CollectionFragment();
                return collectionsFragment;
            case 2:
                MarketFragment marketFragment = new MarketFragment();
                return marketFragment;
            case 3:
                TimelineFragment timelineFragment = new TimelineFragment();
                return timelineFragment;
        }
        return null;
    }

    @Override
    public int getCount() {
        // Show 3 total pages.
        return 4;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Home";
            case 1:
                return "Collections";
            case 2:
                return "Market";
            case 3:
                return "Timeline";

        }
        return null;
    }

}

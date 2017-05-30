package com.cinggl.cinggl.timeline;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.cinggl.cinggl.home.BestCinglesFragment;
import com.cinggl.cinggl.home.CingleOutFragment;

/**
 * Created by J.EL on 5/30/2017.
 */

public class TimelinePagerAdapter extends FragmentPagerAdapter {

    public TimelinePagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                TimelineCingles timelineCingles = new TimelineCingles();
                return timelineCingles;
            case 1:
                TimelineActivities timelineActivities = new TimelineActivities();
                return timelineActivities;
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
                return "Cingles";
            case 1:
                return "Activities";

        }
        return null;
    }
}

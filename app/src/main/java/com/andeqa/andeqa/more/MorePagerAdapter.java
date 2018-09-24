package com.andeqa.andeqa.more;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.andeqa.andeqa.chat_rooms.ChatsFragment;

public class MorePagerAdapter extends FragmentPagerAdapter {
    private static final String TAG = MorePagerAdapter.class.getSimpleName();

    public MorePagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                ActivitiesFragment activitiesFragment = new ActivitiesFragment();
                return activitiesFragment;
            case 1:
                ChatsFragment chatsFragment = new ChatsFragment();
                return chatsFragment;

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
                return "Activities";
            case 1:
                return "Chats";

        }
        return null;
    }
}
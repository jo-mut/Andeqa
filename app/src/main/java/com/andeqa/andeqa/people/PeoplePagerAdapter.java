package com.andeqa.andeqa.people;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.andeqa.andeqa.more.MorePagerAdapter;

public class PeoplePagerAdapter extends FragmentPagerAdapter {
    private static final String TAG = MorePagerAdapter.class.getSimpleName();

    public PeoplePagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                SuggestedPeopleFragment suggestedPeopleFragment = new SuggestedPeopleFragment();
                return suggestedPeopleFragment;
            case 1:
                ContactsFragment contactsFragment = new ContactsFragment();
                return contactsFragment;
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
                return "Suggested";
            case 1:
                return "Contacts";
        }
        return null;
    }
}

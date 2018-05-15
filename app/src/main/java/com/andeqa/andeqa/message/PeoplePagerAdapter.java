package com.andeqa.andeqa.message;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class PeoplePagerAdapter extends FragmentStatePagerAdapter {

    public PeoplePagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                ChatsFragments chatsFragments = new ChatsFragments();
                return chatsFragments;
            case 1:
                ContactsFragments contactsFragments = new ContactsFragments();
                return contactsFragments;

        }
        return null;
    }

    @Override
    public int getCount() {
        // Show 3 total pages.
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Chats";
            case 1:
                return "Contacts";

        }
        return null;
    }

}

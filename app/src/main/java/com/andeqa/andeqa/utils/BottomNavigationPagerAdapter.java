package com.andeqa.andeqa.utils;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import java.util.ArrayList;
import java.util.List;

public class BottomNavigationPagerAdapter extends SmartBottomFragmentStateAdapter {
    private final List<Fragment> fragments = new ArrayList<>();

    public BottomNavigationPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    public void addFragments(Fragment fragment){
        fragments.add(fragment);
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }


}

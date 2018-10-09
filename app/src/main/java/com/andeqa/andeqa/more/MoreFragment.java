package com.andeqa.andeqa.more;


import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.andeqa.andeqa.R;
import com.andeqa.andeqa.collections.CollectionsFragment;

/**
 * A simple {@link Fragment} subclass.
 */
public class MoreFragment extends Fragment {

    private static final String TAG = MoreFragment.class.getSimpleName();
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private MorePagerAdapter collectionsPagerAdapter;



    public static CollectionsFragment newInstance(int sectionNumber) {
        CollectionsFragment fragment = new CollectionsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }


    public MoreFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_more, container, false);

        collectionsPagerAdapter = new MorePagerAdapter(getChildFragmentManager());
        tabLayout = (TabLayout)view.findViewById(R.id.tabs);
        viewPager = (ViewPager)view.findViewById(R.id.container);
        viewPager.setAdapter(collectionsPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(viewPager));

        return view;
    }

}

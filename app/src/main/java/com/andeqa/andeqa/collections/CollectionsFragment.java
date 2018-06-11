package com.andeqa.andeqa.collections;


import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.andeqa.andeqa.R;
import com.google.firebase.firestore.CollectionReference;

/**
 * A simple {@link Fragment} subclass.
 */
public class CollectionsFragment extends Fragment {

    private static final String TAG = CollectionsFragment.class.getSimpleName();
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private CollectionsPagerAdapter collectionsPagerAdapter;



    public static CollectionsFragment newInstance(int sectionNumber) {
        CollectionsFragment fragment = new CollectionsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }


    public CollectionsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_collections, container, false);

        collectionsPagerAdapter = new CollectionsPagerAdapter(getChildFragmentManager());
        tabLayout = (TabLayout)view.findViewById(R.id.tabs);
        viewPager = (ViewPager)view.findViewById(R.id.container);
        viewPager.setAdapter(collectionsPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);

        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(viewPager));



        return view;
    }

}

package com.cinggl.cinggl.timeline;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.cinggl.cinggl.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class TimelineFragment extends Fragment {
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private TimelinePagerAdapter timelinePagerAdapter;

    private static final String ARG_SECTION_NUMBER = "section_number";

    public TimelineFragment() {
        // Required empty public constructor
    }

    public static TimelineFragment newInstance(int sectionNumber){
        TimelineFragment fragment = new TimelineFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_timeline, container, false);
        timelinePagerAdapter = new TimelinePagerAdapter(getChildFragmentManager());
        tabLayout = (TabLayout)view.findViewById(R.id.tabs);
        viewPager = (ViewPager)view.findViewById(R.id.container);
        viewPager.setAdapter(timelinePagerAdapter);
//        tabLayout.setupWithViewPager(viewPager);
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_layout, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement

//        if(id == R.id.action_search){
//            return true;
//        }

//        if(id == R.id.action_notifications){
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }
}

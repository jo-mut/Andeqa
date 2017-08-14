package com.cinggl.cinggl.backing;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.cinggl.cinggl.R;
import com.cinggl.cinggl.home.HomeFragment;
import com.cinggl.cinggl.home.HomePagerAdapter;
import com.cinggl.cinggl.profile.PersonalProfileActivity;
import com.cinggl.cinggl.profile.UpdateProfileActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import butterknife.ButterKnife;

import static android.R.attr.fragment;
import static com.cinggl.cinggl.R.id.container;

/**
 * A simple {@link Fragment} subclass.
 */
public class BackingFragment extends Fragment {
    private static final String TAG = HomeFragment.class.getSimpleName();
    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String  EXTRA_USER_UID = "uid";
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private BackingPagerAdapter backingPagerAdapter;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private String mUid;


    public static BackingFragment newInstance(int sectionNumber) {
        BackingFragment fragment = new BackingFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }



    public BackingFragment() {
     // Required empty public constructor

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_backing, container, false);
        ButterKnife.bind(this, view);

        backingPagerAdapter = new BackingPagerAdapter(getChildFragmentManager());
        tabLayout = (TabLayout)view.findViewById(R.id.tabs);
        viewPager = (ViewPager)view.findViewById(R.id.container);
        viewPager.setAdapter(backingPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
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


        return super.onOptionsItemSelected(item);
    }
}

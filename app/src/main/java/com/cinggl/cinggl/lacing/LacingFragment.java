package com.cinggl.cinggl.lacing;


import android.os.Bundle;
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
import com.cinggl.cinggl.backing.BackingFragment;
import com.cinggl.cinggl.backing.BackingPagerAdapter;
import com.cinggl.cinggl.home.HomeFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class LacingFragment extends Fragment {
    private static final String TAG = HomeFragment.class.getSimpleName();
    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String  EXTRA_USER_UID = "uid";
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private LacingPagerAdapter lacingPagerAdapter;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private String mUid;

    public static LacingFragment newInstance(int sectionNumber) {
        LacingFragment fragment = new LacingFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }


    public LacingFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_lacing, container, false);
        ButterKnife.bind(this, view);

        lacingPagerAdapter = new LacingPagerAdapter(getChildFragmentManager());
        tabLayout = (TabLayout)view.findViewById(R.id.tabs);
        viewPager = (ViewPager)view.findViewById(R.id.container);
        viewPager.setAdapter(lacingPagerAdapter);
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

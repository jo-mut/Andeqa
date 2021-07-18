package com.andeka.andeka.channels;


import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.andeka.andeka.R;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChannelsFragment extends Fragment {
    @Bind(R.id.container) ViewPager mViewPager;
    @Bind(R.id.tabs)TabLayout mTabLayout;

    private ChannelsPagerAdapter channelsPagerAdapter;

    public ChannelsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_channels, container, false);
        ButterKnife.bind(this, view);

        channelsPagerAdapter = new ChannelsPagerAdapter(getChildFragmentManager());
        mViewPager.setAdapter(channelsPagerAdapter);
        mTabLayout.setupWithViewPager(mViewPager);

        return view;
    }

}

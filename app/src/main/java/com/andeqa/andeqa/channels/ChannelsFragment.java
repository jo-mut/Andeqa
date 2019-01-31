package com.andeqa.andeqa.channels;


import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.andeqa.andeqa.R;
import com.andeqa.andeqa.home.HomeFragment;
import com.andeqa.andeqa.models.Post;
import com.andeqa.andeqa.utils.FirebaseUtil;
import com.andeqa.andeqa.utils.VerticalViewPager;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

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

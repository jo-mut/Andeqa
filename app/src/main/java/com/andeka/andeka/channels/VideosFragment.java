package com.andeka.andeka.channels;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.andeka.andeka.R;
import com.andeka.andeka.utils.FirebaseUtil;
import com.andeka.andeka.utils.VerticalViewPager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import butterknife.Bind;
import butterknife.ButterKnife;


public class VideosFragment extends Fragment {
    @Bind(R.id.verticalViewPager) VerticalViewPager mVerticalViewPager;
    private static final String TAG = VideosFragment.class.getSimpleName();
    private VideosPagerAdapter mPagerAdapter;
    private FirebaseUtil mFirebaseUtil;
    private List<DocumentSnapshot> mSnapshots = new ArrayList<>();

    public VideosFragment() {
        // Required empty public constructor
    }

    public static VideosFragment newInstance() {
        VideosFragment fragment = new VideosFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_videos, container, false);
        ButterKnife.bind(this, view);

        mFirebaseUtil = new FirebaseUtil(getContext());
        mPagerAdapter = new VideosPagerAdapter(getContext());
        channelVideos();

        return view;
    }


    private void channelVideos() {
        mFirebaseUtil.postsPath().whereEqualTo("type","video")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot documentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (!documentSnapshots.isEmpty()){
                    for (DocumentSnapshot snapshot : documentSnapshots) {
                       mSnapshots.add(snapshot);
                       mPagerAdapter.notifyDataSetChanged();
                    }
                    Log.d("fragments videos", documentSnapshots.size() + "");
                    mPagerAdapter.addVideos(mSnapshots);
                }
            }
        });
    }


}

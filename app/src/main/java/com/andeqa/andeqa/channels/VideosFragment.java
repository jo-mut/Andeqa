package com.andeqa.andeqa.channels;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.andeqa.andeqa.R;
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
import de.hdodenhof.circleimageview.CircleImageView;


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

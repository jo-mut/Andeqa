package com.andeqa.andeqa.channels;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.andeqa.andeqa.R;
import com.andeqa.andeqa.models.Post;
import com.andeqa.andeqa.player.Player;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.List;

public class VideosPagerAdapter extends PagerAdapter {
    private Context mContext;
    private List<DocumentSnapshot> documentSnapshots;


    public VideosPagerAdapter(Context context) {
        this.mContext = context;
    }

    public void addVideos( List<DocumentSnapshot> snapshots){
        this.documentSnapshots = snapshots;
        Log.d("channel videos", documentSnapshots.size() + "");

    }

    @Override
    public int getCount() {
        return documentSnapshots.size();
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.layout_channel_videos, container, false);
        container.addView(layout);
        Post post = documentSnapshots.get(position).toObject(Post.class);
//        SimpleExoPlayerView exoPlayerView = (SimpleExoPlayerView) layout.findViewById(R.id.exoPlayerView);
//        player = new Player(mContext, exoPlayerView);
//        player.addMedia(post.getUrl());
//        if (exoPlayerView.getPlayer() != null) {
//            exoPlayerView.getPlayer().setPlayWhenReady(true);
//        }
        Log.d("video post url", post.getUrl());


        return layout;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }
}

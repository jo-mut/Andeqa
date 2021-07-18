package com.andeka.andeka.creation;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;

import com.andeka.andeka.R;
import com.andeka.andeka.player.Player;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;

import java.io.File;

import butterknife.Bind;
import butterknife.ButterKnife;

public class PreviewVideoPostActivity extends AppCompatActivity
        implements View.OnClickListener {
    @Bind(R.id.exoPlayerView)SimpleExoPlayerView exoPlayerView;
    @Bind(R.id.toolbar)Toolbar toolbar;
    @Bind(R.id.nextImageView)ImageView nextImageView;

    private static final String IMAGE_PATH ="image path";
    private static final String VIDEO_PATH = "video path";
    private static final String COLLECTION_ID = "collection id";
    private static final String POST_ID = "post id";
    private String mCollectionId;
    private String postId;
    private String video;
    private String thumb;
    private Player player;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_video_post);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               finish();
            }
        });

        nextImageView.setOnClickListener(this);
        if (getIntent().getExtras() != null){
            if (getIntent().getStringExtra(VIDEO_PATH) != null){
                thumb = getIntent().getStringExtra(IMAGE_PATH);
                video = getIntent().getStringExtra(VIDEO_PATH);
                player = new Player(getApplicationContext(), exoPlayerView);
                player.addMedia(video);
            }

            if (getIntent().getStringExtra(VIDEO_PATH) != null){
                video = getIntent().getStringExtra(VIDEO_PATH);
                player = new Player(getApplicationContext(), exoPlayerView);
                player.addMedia(video);
            }

        }

    }



    @Override
    protected void onStart() {
        super.onStart();
        exoPlayerView.getPlayer().setPlayWhenReady(true);

    }

    @Override
    protected void onStop() {
        super.onStop();
       player.releasePlayer();

    }

    @Override
    protected void onPause() {
        super.onPause();
        player.releasePlayer();

    }

    @Override
    protected void onResume() {
        super.onResume();
        exoPlayerView.getPlayer().setPlayWhenReady(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        exoPlayerView.setPlayer(null);
    }

    @Override
    public void onClick(View v){
        if (v == nextImageView){
            File file = new File(video);
            long fileBytes = file.length();
            long fileKiloBytes =  fileBytes / 1024;
            long fileMegaBytes = fileKiloBytes / 1024;

            if (video != null){
                Intent intent = new Intent(PreviewVideoPostActivity.this, CreatePostActivity.class);
                intent.putExtra(PreviewVideoPostActivity.VIDEO_PATH, video);
                startActivity(intent);
            }

        }
    }

}

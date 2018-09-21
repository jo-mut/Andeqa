package com.andeqa.andeqa.creation;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.andeqa.andeqa.R;
import com.andeqa.andeqa.player.Player;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;

import java.io.File;

import butterknife.Bind;
import butterknife.ButterKnife;

public class PreviewVideoPostActivity extends AppCompatActivity
        implements View.OnClickListener {
    @Bind(R.id.simpleExoPlayerView)SimpleExoPlayerView exoPlayerView;
    @Bind(R.id.toolbar)Toolbar toolbar;
    @Bind(R.id.nextImageView)ImageView nextImageView;

    private static final String CAMERA_VIDEO = "camera video";
    private static final String CAMERA_THUMB = "thumb";
    private static final String GALLERY_VIDEO = "gallery video";
    private static final String COLLECTION_ID = "collection id";
    private String video;
    private String thumb;
    private String mCollectionId;
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
            if (getIntent().getStringExtra(CAMERA_VIDEO) != null){
                thumb = getIntent().getStringExtra(CAMERA_THUMB);
                video = getIntent().getStringExtra(CAMERA_VIDEO);
                player = new Player(getApplicationContext(), exoPlayerView);
                player.addMedia(video);
            }

            if (getIntent().getStringExtra(GALLERY_VIDEO) != null){
                video = getIntent().getStringExtra(GALLERY_VIDEO);
                player = new Player(getApplicationContext(), exoPlayerView);
                player.addMedia(video);
            }

            mCollectionId = getIntent().getStringExtra(COLLECTION_ID);
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

            if (fileMegaBytes <= 6){
                if (mCollectionId != null){
                    if (video != null){
                        Intent intent = new Intent(PreviewVideoPostActivity.this, CreateSingleActivity.class);
                        intent.putExtra(PreviewVideoPostActivity.CAMERA_VIDEO, video);
                        intent.putExtra(PreviewVideoPostActivity.CAMERA_THUMB, thumb);
                        intent.putExtra(PreviewVideoPostActivity.COLLECTION_ID, mCollectionId);
                        startActivity(intent);
                    }
                }else {
                    if (video != null){
                        Intent intent = new Intent(PreviewVideoPostActivity.this, CreateSingleActivity.class);
                        intent.putExtra(PreviewVideoPostActivity.CAMERA_VIDEO, video);
                        intent.putExtra(PreviewVideoPostActivity.CAMERA_THUMB, thumb);
                        startActivity(intent);
                    }
                }
            }else {
                Toast.makeText(PreviewVideoPostActivity.this, "Video must be less that 6Mbs", Toast.LENGTH_SHORT).show();
            }

        }
    }

}

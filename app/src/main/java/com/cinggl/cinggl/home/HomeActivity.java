package com.cinggl.cinggl.home;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.cinggl.cinggl.R;
import com.cinggl.cinggl.camera.NewPostFrament;
import com.cinggl.cinggl.chat.ChatFragment;
import com.cinggl.cinggl.profile.ProfileFragment;
import com.cinggl.cinggl.timeline.TimelineFragment;
import com.cinggl.cinggl.ui.SettingsActivity;

import butterknife.Bind;
import butterknife.ButterKnife;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener{
    @Bind(R.id.fab)FloatingActionButton mFloatingActionButton;
    @Bind(R.id.bottomNavigationView)BottomNavigationView mBottomNavigationView;

    final FragmentManager fragmentManager = getSupportFragmentManager();
    final Fragment timelineFragment = new TimelineFragment();
    final Fragment chatFragment = new ChatFragment();
    final Fragment profileFragment = new ProfileFragment();
    final Fragment homeFragment = new HomeFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mFloatingActionButton.setOnClickListener(this);
        launchHomeOnStart();

        mBottomNavigationView.setOnNavigationItemSelectedListener
                (new BottomNavigationView.OnNavigationItemSelectedListener(){
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_home:
                        launchHomeOnStart();
                        break;
                    case R.id.action_timeline:
                        FragmentTransaction timelineTransaction = fragmentManager.beginTransaction();
                        timelineTransaction.replace(R.id.home_container, timelineFragment).commit();
                        break;
                    case R.id.action_chat:
                        FragmentTransaction chatTransaction = fragmentManager.beginTransaction();
                        chatTransaction.replace(R.id.home_container, chatFragment).commit();
                        break;
                    case R.id.action_profile:
                        FragmentTransaction profileTransaction = fragmentManager.beginTransaction();
                        profileTransaction.replace(R.id.home_container, profileFragment).commit();
                        break;
                }
                return true;
            }
        });
    }

    private void launchHomeOnStart(){
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.replace(R.id.home_container, homeFragment);
        ft.commit();
    }

    private void showNewPostFragment(){
        FragmentManager fragmentManager = getSupportFragmentManager();
        NewPostFrament newPostFrament = NewPostFrament.newInstance("create your cingle");
        newPostFrament.show(fragmentManager, "new post fragment");
    }

    @Override
    public void onClick(View v){

        if(v == mFloatingActionButton){
            showNewPostFragment();
        }
    }

}
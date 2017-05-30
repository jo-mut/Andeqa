package com.cinggl.cinggl.home;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
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

    private HomePagerAdapter mHomePagerAdapter;
    private static final String SELECTED_ITEM = "selected item";
    private static final String ARG_SECTION_NUMBER = "section_number";
    private int mSelectedItem;

    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);

        mFloatingActionButton.setOnClickListener(this);

        final FragmentManager fragmentManager = getSupportFragmentManager();

        final Fragment homeFragment = new HomeFragment();
        final Fragment timelineFragment = new TimelineFragment();
        final Fragment chatFragment = new ChatFragment();
        final Fragment profileFragment = new ProfileFragment();

        mBottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener(){

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_home:
                        FragmentTransaction homeTransaction = fragmentManager.beginTransaction();
                        homeTransaction.replace(R.id.home_container, homeFragment).commit();
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

    private void showNewPostFragment(){
        FragmentManager fragmentManager = getSupportFragmentManager();
        NewPostFrament newPostFrament = NewPostFrament.newInstance("create your cingle");
        newPostFrament.show(fragmentManager, "new post fragment");
    }

    public void launchSettings(){
        Intent intentSettings = new Intent(this, SettingsActivity.class);
        startActivity(intentSettings);
    }


    @Override
    public void onClick(View v){

        if(v == mFloatingActionButton){
            showNewPostFragment();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            launchSettings();
            return true;
        }

        if(id == R.id.action_search){
            return true;
        }

        if(id == R.id.action_notifications){
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
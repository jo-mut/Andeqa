package com.cinggl.cinggl.home;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.cinggl.cinggl.R;
import com.cinggl.cinggl.camera.CreateCingleActivity;
import com.cinggl.cinggl.profile.ProfileFragment;
import com.cinggl.cinggl.timeline.TimelineFragment;

import butterknife.Bind;
import butterknife.ButterKnife;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener{
    @Bind(R.id.fab)FloatingActionButton mFloatingActionButton;
    @Bind(R.id.bottomNavigationView)BottomNavigationView mBottomNavigationView;

    final FragmentManager fragmentManager = getSupportFragmentManager();
    final Fragment timelineFragment = new TimelineFragment();
    final Fragment profileFragment = new ProfileFragment();
    final Fragment homeFragment = new HomeFragment();
    private int mSelectedItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mFloatingActionButton.setOnClickListener(this);
        mBottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                selectFragment(item);

                return true;
            }
        });

        MenuItem selectedItem;
        selectedItem = mBottomNavigationView.getMenu().getItem(0);
        selectFragment(selectedItem);

    }

    @Override
    public void onClick(View v){
        if(v == mFloatingActionButton){
//            showNewPostFragment();
            Intent intent = new Intent(HomeActivity.this, CreateCingleActivity.class);
            startActivity(intent);

        }
    }


    @Override
    public void onBackPressed() {
        MenuItem defaulItem = mBottomNavigationView.getMenu().getItem(0);
        if(mSelectedItem != defaulItem.getItemId()){
            selectFragment(defaulItem);
        }else {
            super.onBackPressed();
        }
    }

    private void selectFragment(MenuItem item){
        //initialize each corresponding fragment
        switch (item.getItemId()){
            case R.id.action_home:
                FragmentTransaction ft = fragmentManager.beginTransaction();
                ft.replace(R.id.home_container, homeFragment);
                ft.commit();
                break;
            case R.id.action_timeline:
                FragmentTransaction timelineTransaction = fragmentManager.beginTransaction();
                timelineTransaction.replace(R.id.home_container, timelineFragment).commit();
                break;

            case R.id.action_profile:
                FragmentTransaction profileTransaction = fragmentManager.beginTransaction();
                profileTransaction.replace(R.id.home_container, profileFragment).commit();
                break;
        }

        //update selected item
        mSelectedItem = item.getItemId();

//        updateToolbarText(item.getTitle());

        //uncheck the other items
        for(int i = 0; i < mBottomNavigationView.getMenu().size(); i++){
            MenuItem menuItem = mBottomNavigationView.getMenu().getItem(i);
            menuItem.setChecked(menuItem.getItemId() ==item.getItemId());
        }
    }

    private void updateToolbarText(CharSequence text){
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setTitle(text);
        }
    }

    private void showNewPostFragment(){
        FragmentManager fragmentManager = getSupportFragmentManager();
        CingleSettingsDialogFragment cingleSettingsDialogFragment = CingleSettingsDialogFragment.newInstance("create your cingle");
        cingleSettingsDialogFragment.show(fragmentManager, "new post fragment");
    }

}
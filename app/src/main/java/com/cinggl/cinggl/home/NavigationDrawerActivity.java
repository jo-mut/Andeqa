package com.cinggl.cinggl.home;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.ContentFrameLayout;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;
import com.cinggl.cinggl.R;
import com.cinggl.cinggl.camera.CreateCingleActivity;
import com.cinggl.cinggl.profile.ProfileFragment;
import com.cinggl.cinggl.timeline.TimelineFragment;
import com.cinggl.cinggl.utils.BottomNavigationViewHelper;

import butterknife.Bind;
import butterknife.ButterKnife;

public class NavigationDrawerActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    @Bind(R.id.fab)FloatingActionButton mFloatingActionButton;
    @Bind(R.id.bottomNavigationView)BottomNavigationView mBottomNavigationView;

    final FragmentManager fragmentManager = getSupportFragmentManager();
    final Fragment timelineFragment = new TimelineFragment();
    final Fragment profileFragment = new ProfileFragment();
    final Fragment homeFragment = new HomeFragment();
    private int mSelectedItem;
    private int orientation;
    private ContentFrameLayout mContent;
    private ProgressDialog mProgressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_drawer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ButterKnife.bind(this);

        mFloatingActionButton.setOnClickListener(this);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        if (savedInstanceState == null){
            launchHomeFragment();
        }else {

        }

        BottomNavigationViewHelper.disableShiftMode(mBottomNavigationView);

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
    public void onBackPressed() {
        MenuItem defaulItem = mBottomNavigationView.getMenu().getItem(0);
        if(mSelectedItem != defaulItem.getItemId()){
            selectFragment(defaulItem);
        }else {
            super.onBackPressed();
        }
    }

    private void updateToolbarText(CharSequence text){
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setTitle(text);
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

        updateToolbarText(item.getTitle());

        //uncheck the other items
        for(int i = 0; i < mBottomNavigationView.getMenu().size(); i++){
            MenuItem menuItem = mBottomNavigationView.getMenu().getItem(i);
            menuItem.setChecked(menuItem.getItemId() ==item.getItemId());
        }
    }


    private void launchHomeFragment(){
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.replace(R.id.home_container, homeFragment);
        ft.commit();
    }


//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.navigation_drawer, menu);
//        return true;
//    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
//
//        if (id == R.id.nav_camera) {
//            // Handle the camera action
//        } else if (id == R.id.nav_gallery) {
//
//        } else if (id == R.id.nav_slideshow) {
//
//        } else if (id == R.id.nav_manage) {
//
//        } else if (id == R.id.nav_share) {
//
//        } else if (id == R.id.nav_send) {
//
//        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onClick(View v){
        if(v == mFloatingActionButton){
//            showNewPostFragment();
            Intent intent = new Intent(NavigationDrawerActivity.this, CreateCingleActivity.class);
            startActivity(intent);

        }
    }

}

package com.cinggl.cinggl.home;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.support.v7.widget.ContentFrameLayout;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.cinggl.cinggl.App;
import com.cinggl.cinggl.R;
import com.cinggl.cinggl.camera.CreateCingleActivity;
import com.cinggl.cinggl.profile.ProfileFragment;
import com.cinggl.cinggl.services.ConnectivityReceiver;
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
    private int orientation;
    private ContentFrameLayout mContent;
    private ProgressDialog mProgressDialog;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (isNetworkConnected()) {
//            mProgressDialog = new ProgressDialog(this);
//            mProgressDialog.setMessage("Please wait...");
//            mProgressDialog.setCancelable(false);
//            mProgressDialog.show();


        } else {

//            new AlertDialog.Builder(this)
//                    .setTitle("No Internet Connection")
//                    .setMessage("It looks like your internet connection is off. Please turn it " +
//                            "on and try again")
//                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int which) {
//                        }
//                    }).setIcon(android.R.drawable.ic_dialog_alert).show();
        }
//
//        launchHomeFragment();

        if (savedInstanceState == null){
            launchHomeFragment();
        }else {

        }

        mFloatingActionButton.setOnClickListener(this);
//
//        mBottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
//            @Override
//            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
//
//                selectFragment(item);
//
//                return true;
//            }
//        });
//
//        MenuItem selectedItem;
//        selectedItem = mBottomNavigationView.getMenu().getItem(0);
//        selectFragment(selectedItem);



    }

    @Override
    public void onClick(View v){
        if(v == mFloatingActionButton){
//            showNewPostFragment();
            Intent intent = new Intent(HomeActivity.this, CreateCingleActivity.class);
            startActivity(intent);

        }
    }

//
//    @Override
//    public void onBackPressed() {
//        MenuItem defaulItem = mBottomNavigationView.getMenu().getItem(0);
//        if(mSelectedItem != defaulItem.getItemId()){
//            selectFragment(defaulItem);
//        }else {
//            super.onBackPressed();
//        }
//    }
//
//    private void selectFragment(MenuItem item){
//        //initialize each corresponding fragment
//        switch (item.getItemId()){
//            case R.id.action_home:
//                FragmentTransaction ft = fragmentManager.beginTransaction();
//                ft.replace(R.id.home_container, homeFragment);
//                ft.commit();
//                break;
//            case R.id.action_timeline:
//                FragmentTransaction timelineTransaction = fragmentManager.beginTransaction();
//                timelineTransaction.replace(R.id.home_container, timelineFragment).commit();
//                break;
//
//            case R.id.action_profile:
//                FragmentTransaction profileTransaction = fragmentManager.beginTransaction();
//                profileTransaction.replace(R.id.home_container, profileFragment).commit();
//                break;
//        }
//
//        //update selected item
//        mSelectedItem = item.getItemId();
//
////        updateToolbarText(item.getTitle());
//
//        //uncheck the other items
//        for(int i = 0; i < mBottomNavigationView.getMenu().size(); i++){
//            MenuItem menuItem = mBottomNavigationView.getMenu().getItem(i);
//            menuItem.setChecked(menuItem.getItemId() ==item.getItemId());
//        }
//    }

    private void updateToolbarText(CharSequence text){
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setTitle(text);
        }
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_layout, menu);
//        return true;
//    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//
//        }

        return super.onOptionsItemSelected(item);
    }

    private void launchHomeFragment(){
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.replace(R.id.home_container, homeFragment);
        ft.commit();
    }

    private boolean isNetworkConnected() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE); // 1
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo(); // 2
        return networkInfo != null && networkInfo.isConnected(); // 3
    }

}

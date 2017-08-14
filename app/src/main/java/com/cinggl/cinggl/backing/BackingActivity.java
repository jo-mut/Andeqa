package com.cinggl.cinggl.backing;

import android.app.ProgressDialog;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ContentFrameLayout;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.cinggl.cinggl.R;
import com.cinggl.cinggl.home.HomeFragment;
import com.cinggl.cinggl.profile.ProfileFragment;
import com.cinggl.cinggl.timeline.TimelineFragment;

public class BackingActivity extends AppCompatActivity {

    final FragmentManager fragmentManager = getSupportFragmentManager();
    final Fragment backingFragment = new BackingFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backing);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        //launch Backing fragment in onCreate
        if (savedInstanceState == null){
            launchBackingFragment();
        }else {

        }

    }


    private void launchBackingFragment(){
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.replace(R.id.backing_container, backingFragment);
        ft.commit();
    }
}

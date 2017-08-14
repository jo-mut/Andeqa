package com.cinggl.cinggl.leasing;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.cinggl.cinggl.R;
import com.cinggl.cinggl.backing.BackingFragment;

public class LeasingActivity extends AppCompatActivity {
    final FragmentManager fragmentManager = getSupportFragmentManager();
    final Fragment leasingFragment = new LeasingFragment();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leasing);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        //launch Backing fragment in onCreate
        if (savedInstanceState == null) {
            launchLeasingFragment();
        } else {

        }
    }

    private void launchLeasingFragment(){
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.replace(R.id.leasing_container, leasingFragment);
        ft.commit();
    }


}

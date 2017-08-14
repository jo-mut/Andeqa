package com.cinggl.cinggl.lacing;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.cinggl.cinggl.R;
import com.cinggl.cinggl.backing.BackingFragment;

public class LacingActivity extends AppCompatActivity {

    final FragmentManager fragmentManager = getSupportFragmentManager();
    final Fragment lacingFragment = new LacingFragment();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lacing);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        //launch Backing fragment in onCreate
        if (savedInstanceState == null){
            launchLacingFragment();
        }else {

        }
    }

    private void launchLacingFragment(){
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.replace(R.id.lacing_container, lacingFragment);
        ft.commit();
    }
}

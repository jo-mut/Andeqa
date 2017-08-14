package com.cinggl.cinggl.selling;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.cinggl.cinggl.R;
import com.cinggl.cinggl.lacing.LacingFragment;

public class SellingActivity extends AppCompatActivity {
    final FragmentManager fragmentManager = getSupportFragmentManager();
    final Fragment sellingFragment = new SellingFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selling);

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
        ft.replace(R.id.selling_container, sellingFragment);
        ft.commit();
    }
}

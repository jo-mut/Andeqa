package com.cinggl.cinggl.ifair;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.cinggl.cinggl.R;


import butterknife.Bind;
import butterknife.ButterKnife;


public class IfairMainActivity extends AppCompatActivity implements View.OnClickListener {
    //BIND VIEWS
    @Bind(R.id.launchBackingActivity)TextView mLaunchBackingAActivity;
    @Bind(R.id.launchLacingActivity)TextView mLaunchLacingActivity;
    @Bind(R.id.launchLeasingActivity)TextView mLaunchLeasingActivity;
//    @Bind(R.id.launchMarketActivity)TextView mLaunchMarketActivity;
    @Bind(R.id.launchSellingActivity)TextView mLaunchSellingActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ifair_main);
        ButterKnife.bind(this);

        //SET CLICKLISTENER OF VIEWS
        mLaunchBackingAActivity.setOnClickListener(this);
        mLaunchLacingActivity.setOnClickListener(this);
        mLaunchLeasingActivity.setOnClickListener(this);
//        mLaunchMarketActivity.setOnClickListener(this);
        mLaunchSellingActivity.setOnClickListener(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //BACK NAVIGATION
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    @Override
    public void onClick(View v){


    }
}

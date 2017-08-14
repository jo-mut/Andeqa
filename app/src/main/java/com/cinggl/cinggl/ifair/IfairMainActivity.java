package com.cinggl.cinggl.ifair;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.cinggl.cinggl.R;
import com.cinggl.cinggl.backing.BackingActivity;
import com.cinggl.cinggl.lacing.LacingActivity;
import com.cinggl.cinggl.leasing.LeasingActivity;
import com.cinggl.cinggl.market.MarketActivity;
import com.cinggl.cinggl.selling.SellingActivity;
import com.cinggl.cinggl.trading.TradingActivity;

import butterknife.Bind;
import butterknife.ButterKnife;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;

public class IfairMainActivity extends AppCompatActivity implements View.OnClickListener {
    //BIND VIEWS
    @Bind(R.id.launchBackingActivity)TextView mLaunchBackingAActivity;
    @Bind(R.id.launchLacingActivity)TextView mLaunchLacingActivity;
    @Bind(R.id.launchLeasingActivity)TextView mLaunchLeasingActivity;
    @Bind(R.id.launchMarketActivity)TextView mLaunchMarketActivity;
    @Bind(R.id.launchSellingActivity)TextView mLaunchSellingActivity;
    @Bind(R.id.launchTradingActivity)TextView mLaunchTradingActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ifair_main);
        ButterKnife.bind(this);

        //SET CLICKLISTENER OF VIEWS
        mLaunchBackingAActivity.setOnClickListener(this);
        mLaunchLacingActivity.setOnClickListener(this);
        mLaunchLeasingActivity.setOnClickListener(this);
        mLaunchMarketActivity.setOnClickListener(this);
        mLaunchTradingActivity.setOnClickListener(this);
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
        if (v == mLaunchBackingAActivity){
            //LAUNCH CINGLE BACKING ACTIVITY
            Intent intent = new Intent(IfairMainActivity.this, BackingActivity.class);
            startActivity(intent);
        }

        if (v == mLaunchLacingActivity){
            //LAUNCH CINGLE LACING ACTIVITY
            Intent intent = new Intent(IfairMainActivity.this, LacingActivity.class);
            startActivity(intent);
        }

        if (v == mLaunchLeasingActivity){
            //LAUNCH CINGLE LEASING ACTIVITY
            Intent intent = new Intent(IfairMainActivity.this, LeasingActivity.class);
            startActivity(intent);
        }

        if (v == mLaunchSellingActivity){
            //LAUNCH CINGLE SELLING ACTIVITY
            Intent intent = new Intent(IfairMainActivity.this, SellingActivity.class);
            startActivity(intent);
        }

        if (v == mLaunchTradingActivity){
            //LAUNCH ASSET TRADING ACTIVITY
            Intent intent = new Intent(IfairMainActivity.this, TradingActivity.class);
            startActivity(intent);
        }

        if (v == mLaunchMarketActivity){
            //LAUNCH MAIN MARKET ACTIVITY
            Intent intent = new Intent(IfairMainActivity.this, MarketActivity.class);
            startActivity(intent);
        }
    }
}

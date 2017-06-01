package com.cinggl.cinggl.ui;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.cinggl.cinggl.R;
import com.cinggl.cinggl.home.HomeActivity;

import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity{
    protected int _splashTime = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
                Intent intent = new Intent(MainActivity.this, AppActivities.class);
                startActivity(intent);
            }
        },_splashTime);

    }

}
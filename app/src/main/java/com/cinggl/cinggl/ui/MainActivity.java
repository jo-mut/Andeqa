package com.cinggl.cinggl.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.cinggl.cinggl.R;
import com.cinggl.cinggl.home.HomeActivity;
import com.cinggl.cinggl.profile.SetupPorifleInfo;

import butterknife.ButterKnife;

import static junit.runner.BaseTestRunner.savePreferences;

public class MainActivity extends AppCompatActivity{
    protected int _splashTime = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
                Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                startActivity(intent);
            }
        },_splashTime);

    }

}
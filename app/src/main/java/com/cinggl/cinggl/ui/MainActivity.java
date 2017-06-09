package com.cinggl.cinggl.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.cinggl.cinggl.R;
import com.cinggl.cinggl.home.HomeActivity;
import com.cinggl.cinggl.profile.ProfileActivity;

import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity{
    protected int _splashTime = 3000;
    private Boolean flag = false;

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
                loadSavedPreferences();
                if(flag){
                   Intent intent = new Intent(MainActivity.this,  ProfileActivity.class);
                    savePreferences();
                    startActivity(intent);
                }else{
                   Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                    startActivity(intent);
                }
//                Intent intent = new Intent(MainActivity.this, HomeActivity.class);
//                startActivity(intent);
            }
        },_splashTime);

    }

    private void loadSavedPreferences(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        flag = sharedPreferences.getBoolean("FirstLaunch", true);
    }

    private void savePreferences(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("FirstLaunch", false);
        editor.commit();
    }

}
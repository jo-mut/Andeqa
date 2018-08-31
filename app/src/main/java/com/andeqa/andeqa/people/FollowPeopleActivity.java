package com.andeqa.andeqa.people;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.andeqa.andeqa.R;

public class FollowPeopleActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follow_people);

        showUsersToFollow();
    }

    private void showUsersToFollow(){
        try {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FollowFragment followFragment = new FollowFragment();
            FragmentTransaction ft = fragmentManager.beginTransaction();
            ft.replace(R.id.follow_container, followFragment);
            ft.commit();
        }catch (Exception e1){

        }
    }
}

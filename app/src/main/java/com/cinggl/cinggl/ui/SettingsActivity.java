package com.cinggl.cinggl.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cinggl.cinggl.R;
import com.cinggl.cinggl.profile.UpdateProfileActivity;

import butterknife.Bind;
import butterknife.ButterKnife;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener{
    @Bind(R.id.editProifleLayout)RelativeLayout mEditProfileLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);

        mEditProfileLayout.setOnClickListener(this);
    }

    @Override
    public void onClick(View v){
        if(v == mEditProfileLayout){
            Intent intent = new Intent(SettingsActivity.this, UpdateProfileActivity.class);
            startActivity(intent);
        }
    }
}

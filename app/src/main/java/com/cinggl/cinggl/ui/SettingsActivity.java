package com.cinggl.cinggl.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.cinggl.cinggl.R;
import com.cinggl.cinggl.profile.UpdateProfileActivity;

import butterknife.Bind;
import butterknife.ButterKnife;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener{
    @Bind(R.id.editProfileTextView)TextView mEditProfileTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);

        mEditProfileTextView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v){
        if(v == mEditProfileTextView){
            Intent intent = new Intent(SettingsActivity.this, UpdateProfileActivity.class);
            startActivity(intent);
        }
    }
}

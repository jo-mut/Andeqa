package com.cinggl.cinggl.camera;

import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.cinggl.cinggl.R;

public class CingleCreationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT > 20) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorAccent));
        }

        setContentView(R.layout.activity_main);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        try {
            //noinspection ConstantConditions
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        } catch (Exception ignored) {}
//
//        if (savedInstanceState == null) {
//            getFragmentManager().beginTransaction()
//                    .add(R.id.container, new CingleCreateFragment())
//                    .commit();
//        }
    }

//    @Override
//    public void onBackPressed() {
//        CingleCreateFragment fragment = (CingleCreateFragment) getFragmentManager().findFragmentById(R.id.container);
//        fragment.back();
//    }
}

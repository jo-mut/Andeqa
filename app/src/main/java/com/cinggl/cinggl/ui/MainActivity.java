package com.cinggl.cinggl.ui;

import android.content.Intent;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TimePicker;

import com.cinggl.cinggl.R;

import butterknife.Bind;
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
                Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                startActivity(intent);
            }
        },_splashTime);

    }

}

//    public CingleOut() {
//        // Required empty public constructor
//    }
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        // Inflate the layout for this fragment
//        View view = inflater.inflate(R.layout.fragment_cingle_out, container, false);
//        return view;
//    }
//
////    private void setUpFirebaseAdapter() {
////
////        mRecyclerView.setHasFixedSize(true);
////        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
////        mRecyclerView.setLayoutManager(layoutManager);
////        mRecyclerView.setAdapter(mFirebaseRecyclerAdapter);
////
////    }
//

//public class SectionsPagerAdapter extends FragmentPagerAdapter {
//
//    public SectionsPagerAdapter(FragmentManager fm) {
//        super(fm);
//    }
//
//    @Override
//    public Fragment getItem(int position) {
//        switch (position){
//            case 0:
//                CingleOut cingleOut = new CingleOut();
//                return cingleOut;
//            case 1:
//                Cingles cingles = new Cingles();
//                return cingles;
//        }
//        return null;
//    }
//
//    @Override
//    public int getCount() {
//        // Show 3 total pages.
//        return 2;
//    }
//
//    @Override
//    public CharSequence getPageTitle(int position) {
//        switch (position) {
//            case 0:
//                return "Cingle Out";
//            case 1:
//                return "Cingles";
//
//        }
//        return null;
//    }
//}

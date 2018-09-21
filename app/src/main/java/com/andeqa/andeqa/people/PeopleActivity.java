package com.andeqa.andeqa.people;

import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.andeqa.andeqa.R;

public class PeopleActivity extends AppCompatActivity {

    private static final String TAG = PeopleActivity.class.getSimpleName();
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private PeoplePagerAdapter peoplePagerAdapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_people);

        peoplePagerAdapter = new PeoplePagerAdapter(getSupportFragmentManager());
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        viewPager = (ViewPager) findViewById(R.id.container);
        viewPager.setAdapter(peoplePagerAdapter);
        tabLayout.setupWithViewPager(viewPager);

        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(viewPager));

    }
}

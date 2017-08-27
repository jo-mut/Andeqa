package com.cinggl.cinggl.home;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.ContentFrameLayout;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.cinggl.cinggl.Constants;
import com.cinggl.cinggl.R;
import com.cinggl.cinggl.creation.CreateCingleActivity;
import com.cinggl.cinggl.ifair.IfairMainActivity;
import com.cinggl.cinggl.preferences.SettingsActivity;
import com.cinggl.cinggl.profile.ProfileFragment;
import com.cinggl.cinggl.timeline.TimelineFragment;
import com.cinggl.cinggl.utils.BottomNavigationViewHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class NavigationDrawerActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    @Bind(R.id.fab)FloatingActionButton mFloatingActionButton;
    @Bind(R.id.bottomNavigationView)BottomNavigationView mBottomNavigationView;


    private static final int MAX_WIDTH = 300;
    private static final int MAX_HEIGHT = 300;
    final FragmentManager fragmentManager = getSupportFragmentManager();
    final Fragment timelineFragment = new TimelineFragment();
    final Fragment profileFragment = new ProfileFragment();
    final Fragment homeFragment = new HomeFragment();
    private int mSelectedItem;
    private int orientation;
    private ContentFrameLayout mContent;
    private ProgressDialog mProgressDialog;
    private DatabaseReference usersRef;
    private FirebaseAuth firebaseAuth;
    private ImageView mProfileCover;
    private CircleImageView mProfileImageView;
    private TextView mFirstNameTextView;
    private TextView mSecondNameTextView;
    private TextView mEmailTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_drawer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ButterKnife.bind(this);

        firebaseAuth = FirebaseAuth.getInstance();

        mFloatingActionButton.setOnClickListener(this);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();



        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View header = navigationView.getHeaderView(0);
        mProfileCover = (ImageView) header.findViewById(R.id.header_cover_image);
        mProfileImageView = (CircleImageView) header.findViewById(R.id.profileImageView);
        mFirstNameTextView = (TextView) header.findViewById(R.id.firstNameTextView);
        mSecondNameTextView = (TextView) header.findViewById(R.id.secondNameTextView);
        mEmailTextView = (TextView) header.findViewById(R.id.emailTextView);


        if (savedInstanceState == null){
            launchHomeFragment();
        }else {

        }

        fetchData();
        fetchUserEmail();

        //bottom navigation
        BottomNavigationViewHelper.disableShiftMode(mBottomNavigationView);
        mBottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                selectFragment(item);
                return true;
            }
        });

        MenuItem selectedItem;
        selectedItem = mBottomNavigationView.getMenu().getItem(0);
        selectFragment(selectedItem);

    }


    @Override
    public void onBackPressed() {
        MenuItem defaulItem = mBottomNavigationView.getMenu().getItem(0);
        if(mSelectedItem != defaulItem.getItemId()){
            selectFragment(defaulItem);
        }else {
            super.onBackPressed();
        }
    }

    private void updateToolbarText(CharSequence text){
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setTitle(text);
        }
    }

    private void selectFragment(MenuItem item){
        //initialize each corresponding fragment
        switch (item.getItemId()){
            case R.id.action_home:
                FragmentTransaction ft = fragmentManager.beginTransaction();
                ft.replace(R.id.home_container, homeFragment);
                ft.commit();
                break;
            case R.id.action_timeline:
                FragmentTransaction timelineTransaction = fragmentManager.beginTransaction();
                timelineTransaction.replace(R.id.home_container, timelineFragment).commit();
                break;

            case R.id.action_profile:
                FragmentTransaction profileTransaction = fragmentManager.beginTransaction();
                profileTransaction.replace(R.id.home_container, profileFragment).commit();
                break;
        }

        //update selected item
        mSelectedItem = item.getItemId();

        updateToolbarText(item.getTitle());

        //uncheck the other items
        for(int i = 0; i < mBottomNavigationView.getMenu().size(); i++){
            MenuItem menuItem = mBottomNavigationView.getMenu().getItem(i);
            menuItem.setChecked(menuItem.getItemId() ==item.getItemId());
        }
    }


    private void launchHomeFragment(){
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.replace(R.id.home_container, homeFragment);
        ft.commit();
    }

    private void fetchUserEmail(){
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        String uid = firebaseUser.getUid();

        mEmailTextView.setText(firebaseUser.getEmail());
    }

    private void fetchData(){
        //database references
        usersRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_USERS);
        usersRef.keepSynced(true);
        usersRef.child(firebaseAuth.getCurrentUser().getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            String firstName = (String) dataSnapshot.child("firstName").getValue();
                            String secondName = (String) dataSnapshot.child("secondName").getValue();
                            final String profileImage = (String) dataSnapshot.child("profileImage").getValue();
                            final String profileCover = (String) dataSnapshot.child("profileCover").getValue();

                            mFirstNameTextView.setText(firstName);
                            mSecondNameTextView.setText(secondName);

                            Picasso.with(NavigationDrawerActivity.this)
                                    .load(profileImage)
                                    .resize(MAX_WIDTH, MAX_HEIGHT)
                                    .onlyScaleDown()
                                    .centerCrop()
                                    .placeholder(R.drawable.profle_image_background)
                                    .networkPolicy(NetworkPolicy.OFFLINE)
                                    .into(mProfileImageView, new Callback() {
                                        @Override
                                        public void onSuccess() {

                                        }

                                        @Override
                                        public void onError() {
                                            Picasso.with(NavigationDrawerActivity.this)
                                                    .load(profileImage)
                                                    .resize(MAX_WIDTH, MAX_HEIGHT)
                                                    .onlyScaleDown()
                                                    .centerCrop()
                                                    .placeholder(R.drawable.profle_image_background)
                                                    .into(mProfileImageView);

                                        }
                                    });

                            Picasso.with(NavigationDrawerActivity.this)
                                    .load(profileCover)
                                    .fit()
                                    .centerCrop()
                                    .networkPolicy(NetworkPolicy.OFFLINE)
                                    .into(mProfileCover, new Callback() {
                                        @Override
                                        public void onSuccess() {

                                        }

                                        @Override
                                        public void onError() {
                                            Picasso.with(NavigationDrawerActivity.this)
                                                    .load(profileCover)
                                                    .fit()
                                                    .centerCrop()
                                                    .into(mProfileCover);


                                        }
                                    });
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.navigation_drawer, menu);
//        return true;
//    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
//
//        if (id == R.id.nav_camera) {
//            // Handle the camera action
//        } else if (id == R.id.nav_gallery) {
//
//        } else if (id == R.id.nav_slideshow) {
//
//        } else if (id == R.id.nav_manage) {
//
//        } else if (id == R.id.nav_share) {
//
//        } else if (id == R.id.nav_send) {
//
//        }

        if (id == R.id.action_ifair){
            Intent intent = new Intent(NavigationDrawerActivity.this, IfairMainActivity.class);
            startActivity(intent);
        }

        if (id == R.id.action_about){
            Intent intent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://www.androidhive.info/privacy-policy"));
            startActivity(intent);
        }

        if (id == R.id.action_preferences) {
            startActivity(new Intent(NavigationDrawerActivity.this, SettingsActivity.class));
            return true;
        }

        if (id == R.id.action_help){
            Intent intent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://www.androidhive.info/privacy-policy"));
            startActivity(intent);
        }


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onClick(View v){
        if(v == mFloatingActionButton){
            Intent intent = new Intent(NavigationDrawerActivity.this, CreateCingleActivity.class);
            startActivity(intent);
        }
    }

}

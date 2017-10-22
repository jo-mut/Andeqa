package com.cinggl.cinggl.home;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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
import com.cinggl.cinggl.ifair.IfairCinglesActivity;
import com.cinggl.cinggl.profile.PersonalProfileActivity;
import com.cinggl.cinggl.profile.ProfileFragment;
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

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    @Bind(R.id.fab)FloatingActionButton mFloatingActionButton;
//    @Bind(R.id.bottomNavigationView)BottomNavigationView mBottomNavigationView;


    private static final int MAX_WIDTH = 200;
    private static final int MAX_HEIGHT = 200;
    final FragmentManager fragmentManager = getSupportFragmentManager();
    private static final String TAG = MainActivity.class.getSimpleName();
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

    private Fragment savedState = null;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
        mProfileImageView = (CircleImageView) header.findViewById(R.id.creatorImageView);
        mFirstNameTextView = (TextView) header.findViewById(R.id.firstNameTextView);
        mSecondNameTextView = (TextView) header.findViewById(R.id.secondNameTextView);
        mEmailTextView = (TextView) header.findViewById(R.id.emailTextView);

        if (firebaseAuth.getCurrentUser() != null){
            fetchData();
            fetchUserEmail();
        }

        if (savedInstanceState != null){
//            savedState = getSupportFragmentManager().getFragment(savedInstanceState, HomeFragment.class.getName());
        }else {
            launchHomeFragment();
        }

//
//        //bottom navigation
//        BottomNavigationViewHelper.disableShiftMode(mBottomNavigationView);
//        mBottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView
//                .OnNavigationItemSelectedListener() {
//            @Override
//            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
//                selectFragment(item);
//                return true;
//            }
//        });
//
//        CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams)
//                mBottomNavigationView.getLayoutParams();
//        layoutParams.setBehavior(new BottomNavigationViewBehavior());
//
//        MenuItem selectedItem;
//        selectedItem = mBottomNavigationView.getMenu().getItem(0);
//        selectFragment(selectedItem);

    }

//
//    @Override
//    public void onBackPressed() {
//        MenuItem defaulItem = mBottomNavigationView.getMenu().getItem(0);
//        if(mSelectedItem != defaulItem.getItemId()){
//            selectFragment(defaulItem);
//        }else {
//            super.onBackPressed();
//        }
//    }
//
//    private void updateToolbarText(CharSequence text){
//        ActionBar actionBar = getSupportActionBar();
//        if(actionBar != null){
//            actionBar.setTitle(text);
//        }
//    }

//    private void selectFragment(MenuItem item){
//        //initialize each corresponding fragment
//        switch (item.getItemId()){
//            case R.id.action_home:
//                FragmentTransaction ft = fragmentManager.beginTransaction();
//                ft.replace(R.id.home_container, homeFragment);
//                ft.commit();
//                break;
//            case R.id.action_timeline:
//                FragmentTransaction timelineTransaction = fragmentManager.beginTransaction();
//                timelineTransaction.replace(R.id.home_container, timelineFragment).commit();
//                break;
//
//            case R.id.action_profile:
//                FragmentTransaction profileTransaction = fragmentManager.beginTransaction();
//                profileTransaction.replace(R.id.home_container, profileFragment).commit();
//                break;
//        }
//
//        //update selected item
//        mSelectedItem = item.getItemId();
//
//        updateToolbarText(item.getTitle());
//
//        //uncheck the other items
//        for(int i = 0; i < mBottomNavigationView.getMenu().size(); i++){
//            MenuItem menuItem = mBottomNavigationView.getMenu().getItem(i);
//            menuItem.setChecked(menuItem.getItemId() ==item.getItemId());
//        }
//    }


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

                            Picasso.with(MainActivity.this)
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
                                            Picasso.with(MainActivity.this)
                                                    .load(profileImage)
                                                    .resize(MAX_WIDTH, MAX_HEIGHT)
                                                    .onlyScaleDown()
                                                    .centerCrop()
                                                    .placeholder(R.drawable.profle_image_background)
                                                    .into(mProfileImageView);

                                        }
                                    });

                            Picasso.with(MainActivity.this)
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
                                            Picasso.with(MainActivity.this)
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.action_home){
            Intent intent = new Intent(MainActivity.this, MainActivity.class);
        }

        if (id == R.id.action_profile){
            Intent intent = new Intent(MainActivity.this, PersonalProfileActivity.class);
            startActivity(intent);
        }

        if (id == R.id.action_ifair){
            Intent intent = new Intent(MainActivity.this, IfairCinglesActivity.class);
            startActivity(intent);
        }

        if (id == R.id.action_about){
            Intent intent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://johnmutuku628.wixsite.com/cinggl"));
            startActivity(intent);
        }


        if (id == R.id.action_send_feedback){
            String body = null;
            try {
                body = this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName;
                body = "\n\n-----------------------------\nPlease don't remove this information\n Device OS: Android \n Device OS version: " +
                        Build.VERSION.RELEASE + "\n App Version: " + body + "\n Device Brand: " + Build.BRAND +
                        "\n Device Model: " + Build.MODEL + "\n Device Manufacturer: " + Build.MANUFACTURER;
            } catch (PackageManager.NameNotFoundException e) {
            }
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("message/rfc822");
            intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"johnngei2@gmail.com"});
            intent.putExtra(Intent.EXTRA_SUBJECT, "Query from android app");
            intent.putExtra(Intent.EXTRA_TEXT, body);
            this.startActivity(Intent.createChooser(intent, this.getString(R.string.choose_email_client)));
        }


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onClick(View v){
        if(v == mFloatingActionButton){
            Intent intent = new Intent(MainActivity.this, CreateCingleActivity.class);
            startActivity(intent);
        }
    }

}

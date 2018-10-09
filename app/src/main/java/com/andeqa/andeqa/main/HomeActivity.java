package com.andeqa.andeqa.main;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.ColorRes;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.R;
import com.andeqa.andeqa.chat_rooms.ChatsFragment;
import com.andeqa.andeqa.collections.CollectionsFragment;
import com.andeqa.andeqa.home.ExploreFragment;
import com.andeqa.andeqa.home.HomeFragment;

import com.andeqa.andeqa.models.Andeqan;
import com.andeqa.andeqa.models.Collection;
import com.andeqa.andeqa.more.ActivitiesFragment;
import com.andeqa.andeqa.more.MoreFragment;
import com.andeqa.andeqa.profile.ProfileActivity;
import com.andeqa.andeqa.utils.BottomNavigationPagerAdapter;
import com.andeqa.andeqa.utils.BottomNavigationViewBehavior;
import com.andeqa.andeqa.utils.ForceUpdateChecker;
import com.andeqa.andeqa.utils.NoSwipePager;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;
import com.aurelhubert.ahbottomnavigation.notification.AHNotification;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener,
        ForceUpdateChecker.OnUpdateNeededListener  {
    @Bind(R.id.bottomNavigationView)AHBottomNavigation mBottomNavigationView;
//    @Bind(R.id.fab)FloatingActionButton mFloatingActionButton;
    @Bind(R.id.toolbar)Toolbar toolbar;
    @Bind(R.id.titleTextView)TextView titleTextView;
    @Bind(R.id.profileImageView)
    CircleImageView mProfileImageView;
    @Bind(R.id.noSwipeViewPager)NoSwipePager noSwipePager;

    private Uri photoUri;
    private static final String TAG = HomeActivity.class.getSimpleName();
    private static final String KEY_STATE = "state";
    private int mSelectedItem;

    private static final String EXTRA_USER_UID = "uid";
    private CollectionReference timelineCollection;
    private CollectionReference collectionsCollection;
    private CollectionReference usersReference;
    private Query timelineQuery;
    private FirebaseAuth firebaseAuth;

    //bottom navigation view
    final FragmentManager fragmentManager = getSupportFragmentManager();
    final Fragment homeFragment = new HomeFragment();
    final Fragment exploreFragment = new ExploreFragment();
    final Fragment collectionFragment = new CollectionsFragment();
    final Fragment moreFragment = new MoreFragment();
    final Fragment activitiesFragment = new ActivitiesFragment();
    final Fragment chatsFragment = new ChatsFragment();
    private BottomNavigationPagerAdapter navigationPagerAdapter;
    private boolean notificationVisible = false;

    private static final int SECONDS_TO_FORCED_UPDATE = 1296000;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        /**initialize force update checker***/
        ForceUpdateChecker.with(this).onUpdateNeeded(this).check();
        /***initialize firebase authentication**/
        firebaseAuth = FirebaseAuth.getInstance();
        /****click listeners*/
//        mFloatingActionButton.setOnClickListener(this);
        mProfileImageView.setOnClickListener(this);
        /***firestore references***/
        timelineCollection = FirebaseFirestore.getInstance().collection(Constants.TIMELINE);
        timelineCollection = FirebaseFirestore.getInstance().collection(Constants.TIMELINE);
        timelineQuery = timelineCollection.document(firebaseAuth.getCurrentUser().getUid())
                .collection("activities").whereEqualTo("status", "un_read");
        collectionsCollection = FirebaseFirestore.getInstance().collection(Constants.COLLECTIONS);
        usersReference = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);
        initializeCollections();
        setProfile();
        setUpWithViewPager();
        addFragmentsToBottomNavigation();
        setUpBottomNavigationStyle();
        mBottomNavigationView.setCurrentItem(0);
        titleTextView.setText("Andeqa");
        timelineNotifications();
        bottomNavigationListener();
        /**hide bottom navigation when user scrolls the home actvity***/
        CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams)
                mBottomNavigationView.getLayoutParams();
        layoutParams.setBehavior(new BottomNavigationViewBehavior());

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onUpdateNeeded(final String updateUrl) {
        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("New version available")
                .setMessage("Please update app to Andeqa to the newer version to experience new features")
                .setPositiveButton("Update",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                redirectStore(updateUrl);
                            }
                        }).setNegativeButton("No, thanks",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).create();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                dialog.show();
                dialog.setCancelable(false);
            }
        }, 30000);

    }

    private void redirectStore(String updateUrl) {
        final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(updateUrl));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void addFragmentsToBottomNavigation(){
        AHBottomNavigationItem home = new AHBottomNavigationItem(R.string.home,
                R.drawable.ic_home, R.color.grey_700);
        AHBottomNavigationItem collection = new AHBottomNavigationItem(R.string.collections,
                R.drawable.ic_collection_bottom_nv, R.color.grey_700);
        AHBottomNavigationItem explore = new AHBottomNavigationItem(R.string.explore,
                R.drawable.ic_explore, R.color.grey_700);
        AHBottomNavigationItem more = new AHBottomNavigationItem(R.string.more,
                R.drawable.ic_more_bottom_nav, R.color.grey_700);
        AHBottomNavigationItem activities = new AHBottomNavigationItem(R.string.activitites,
                R.drawable.ic_stopwatch, R.color.grey_700);
        AHBottomNavigationItem chats = new AHBottomNavigationItem(R.string.chats,
                R.drawable.ic_chats, R.color.grey_700);
        mBottomNavigationView.addItem(home);
        mBottomNavigationView.addItem(collection);
        mBottomNavigationView.addItem(activities);
        mBottomNavigationView.addItem(chats);
    }

    private void bottomNavigationListener(){
        mBottomNavigationView.setOnTabSelectedListener(new AHBottomNavigation.OnTabSelectedListener() {
            @Override
            public boolean onTabSelected(int position, boolean wasSelected) {
                if (!wasSelected)
                    selectFragment(position);
                    noSwipePager.setCurrentItem(position);
                    int lastPosition = mBottomNavigationView.getItemsCount()-1;
                    if (notificationVisible && position == lastPosition){
                        mBottomNavigationView.setNotification(new AHNotification(), lastPosition);
                    }

                return true;
            }
        });
    }

    private void setUpWithViewPager(){
        noSwipePager.setPagingEnabled(false);
        navigationPagerAdapter = new BottomNavigationPagerAdapter(getSupportFragmentManager());
        navigationPagerAdapter.addFragments(homeFragment);
        navigationPagerAdapter.addFragments(collectionFragment);
        navigationPagerAdapter.addFragments(activitiesFragment);
        navigationPagerAdapter.addFragments(chatsFragment);
        noSwipePager.setAdapter(navigationPagerAdapter);

    }

    private void setUpBottomNavigationStyle(){
        mBottomNavigationView.setDefaultBackgroundColor(Color.WHITE);
        mBottomNavigationView.setAccentColor(fetchColor(R.color.colorPrimary));
        mBottomNavigationView.setInactiveColor(fetchColor(R.color.grey_700));
        mBottomNavigationView.setTitleState(AHBottomNavigation.TitleState.ALWAYS_SHOW);
    }

    private void initializeCollections(){
        collectionsCollection.orderBy("time").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot documentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (documentSnapshots.isEmpty()){
                    List<Collection> collections = new ArrayList<>();

                }

            }
        });
    }

    private void setProfile(){
        usersReference.document(firebaseAuth.getCurrentUser().getUid())
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                        if (e != null) {
                            android.util.Log.w(TAG, "Listen error", e);
                            return;
                        }

                        if (documentSnapshot.exists()){
                            final Andeqan cinggulan = documentSnapshot.toObject(Andeqan.class);
                            final String profileImage = cinggulan.getProfile_image();
                            Glide.with(HomeActivity.this)
                                    .load(profileImage)
                                    .apply(new RequestOptions()
                                            .placeholder(R.drawable.ic_user_white)
                                            .diskCacheStrategy(DiskCacheStrategy.DATA))
                                    .into(mProfileImageView);
                        }
                    }
                });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }


    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    private void timelineNotifications(){
        timelineQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (!queryDocumentSnapshots.isEmpty()){
                    final int count = queryDocumentSnapshots.size();
                    AHNotification notification = new AHNotification.Builder()
                            .setText(count + "")
                            .setBackgroundColor(fetchColor(R.color.red_900))
                            .setTextColor(fetchColor(R.color.grey_1000))
                            .build();
                    mBottomNavigationView.setNotification(notification, mBottomNavigationView.getItemsCount()-1);
                            notificationVisible = true;
                }
            }
        });

    }



    private void selectFragment(int position){
        //initialize each corresponding fragment
        switch (position){
            case 0:
                titleTextView.setText("Andeqa");
                FragmentTransaction homeTransaction = fragmentManager.beginTransaction();
                homeTransaction.replace(R.id.noSwipeViewPager, homeFragment).commit();
                break;
            case 1:
                titleTextView.setText("Andeqa");
                FragmentTransaction collectionTransaction = fragmentManager.beginTransaction();
                collectionTransaction.replace(R.id.noSwipeViewPager, collectionFragment).commit();
                break;
            case 2:
                titleTextView.setText("Andeqa");
                FragmentTransaction activitiesTransaction = fragmentManager.beginTransaction();
                activitiesTransaction.replace(R.id.noSwipeViewPager, activitiesFragment).commit();
                break;
            case 3:
                titleTextView.setText("Andeqa");
                FragmentTransaction chatsTransaction = fragmentManager.beginTransaction();
                chatsTransaction.replace(R.id.noSwipeViewPager, chatsFragment).commit();
                break;
        }

    }



    @Override
    public void onClick(View v){

        if (v == mProfileImageView){
            Intent intent = new Intent(this, ProfileActivity.class);
            intent.putExtra(HomeActivity.EXTRA_USER_UID, firebaseAuth.getCurrentUser().getUid());
            startActivity(intent);
        }

    }


    private void displayPopupWindow() {
//        final BottomNavigationMenuView menuView = (BottomNavigationMenuView)
//                mBottomNavigationView.getMenu().getItem(3);

//        int [] loc_int = new int[2];
//        try {
//            menuView.getLocationOnScreen(loc_int);
//        }catch (Exception e){
//            return;
//        }
//        final Rect location = new Rect();
//        location.left = loc_int[0];
//        location.top = loc_int[1];
//        location.right = location.left + menuView.getWidth();
//        location.bottom = location.top + menuView.getHeight();
//        timelineQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
//            @Override
//            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
//                if (e != null) {
//                    Log.w(TAG, "Listen error", e);
//                    return;
//                }
//
//                if (!queryDocumentSnapshots.isEmpty()){
//                    final int count = queryDocumentSnapshots.size();
//                    PopupWindow popup = new PopupWindow(HomeActivity.this);
//                    View layout = getLayoutInflater().inflate(R.layout.popup_layout, null);
//
//                    TextView textView = (TextView) layout.findViewById(R.id.popupTextView);
//                    textView.setBackground(getResources().getDrawable(R.drawable.ic_pop_bubble));
//                    textView.setTextColor(Color.BLACK);
//                    textView.setText(count + "");
//
//                    popup.setContentView(layout);
//                    // Set content width and height
//                    popup.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
//                    popup.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
//                    // Closes the popup window when touch outside of it - when looses focus
//                    popup.setOutsideTouchable(true);
//                    popup.setFocusable(true);
//                    // Show anchored to button
//                    popup.setBackgroundDrawable(new BitmapDrawable());
//                    popup.showAtLocation(mBottomNavigationView, Gravity.CENTER, 0, location.top);
//                }
//            }
//        });

    }

    private int fetchColor(@ColorRes int color){
        return ContextCompat.getColor(this, color);
    }

}

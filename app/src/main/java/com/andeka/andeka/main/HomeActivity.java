package com.andeka.andeka.main;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.ColorRes;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;

import com.andeka.andeka.Constants;
import com.andeka.andeka.R;
import com.andeka.andeka.channels.VideosFragment;
import com.andeka.andeka.chatting.InboxFragment;
import com.andeka.andeka.collections.CollectionsFragment;
import com.andeka.andeka.home.HomeFragment;

import com.andeka.andeka.home.NoSwipePager;
import com.andeka.andeka.models.Andeqan;
import com.andeka.andeka.notifications.NotificationsFragment;
import com.andeka.andeka.profile.ProfileActivity;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;
import com.aurelhubert.ahbottomnavigation.notification.AHNotification;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import javax.annotation.Nullable;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener,
        ForceUpdateChecker.OnUpdateNeededListener{
    @Bind(R.id.profileImageView) CircleImageView mProfileImageView;
    @Bind(R.id.toolbar)Toolbar mToolBar;
    @Bind(R.id.bottomNavigationView)AHBottomNavigation mBottomNavigationView;
    @Bind(R.id.noSwipeViewPager)NoSwipePager noSwipePager;
    @Bind(R.id.appbar)AppBarLayout mAppBarLayout;

    private static final String TAG = HomeActivity.class.getSimpleName();
    private static final String EXTRA_USER_UID = "uid";
    private CollectionReference usersReference;
    private FirebaseAuth firebaseAuth;

    //firebase references
    private CollectionReference timelineCollection;
    private CollectionReference collectionsCollection;
    private CollectionReference roomsCollections;
    private Query roomsQuery;
    private Query timelineQuery;
    private CollectionReference postsCollectionReference;
    private DatabaseReference impressionReference;
    private Query collectionsQuery;
    private int TOTAL_ITEMS = 20;

    //bottom navigation view
    final Fragment homeFragment = new HomeFragment();
    //    final Fragment moreFragment = new MoreFragment();
    final Fragment collectionFragment = new CollectionsFragment();
    final Fragment activitiesFragment = new NotificationsFragment();
    final Fragment chatsFragment = new InboxFragment();
    final Fragment channelsFragment = new VideosFragment();
    private Fragment active = homeFragment;
    private BottomNavigationPagerAdapter navigationPagerAdapter;
    private boolean notificationVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        // initialize force update checker
        ForceUpdateChecker.with(this).onUpdateNeeded(this).check();
        //click listeners
        mProfileImageView.setOnClickListener(this);
        // firestore references
        initReferences();
        //get firebase data
        setProfile();
        // set up the bottom navigation fragments
        setUpWithViewPager();
        addFragmentsToBottomNavigation();
        setUpBottomNavigationStyle();
        mBottomNavigationView.setCurrentItem(0);
        mBottomNavigationView.setVisibility(View.VISIBLE);
        timelineNotifications();
        bottomNavigationListener();
        // hide bottom navigation when user scrolls the home actvity***/
        CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams)
                mBottomNavigationView.getLayoutParams();
        layoutParams.setBehavior(new BottomNavigationViewBehavior());


    }


    @Override
    public void onUpdateNeeded(final String updateUrl) {
        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("New version available")
                .setMessage("Update Andeqa to the latest version")
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

    @Override
    public void onClick(View v){

        if (v == mProfileImageView){
            Intent intent = new Intent(this, ProfileActivity.class);
            intent.putExtra(HomeActivity.EXTRA_USER_UID, firebaseAuth.getCurrentUser().getUid());
            startActivity(intent);
        }

    }


    private void redirectStore(String updateUrl) {
        final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(updateUrl));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
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

    private void initReferences(){
        firebaseAuth = FirebaseAuth.getInstance();
        usersReference = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);
        collectionsCollection = FirebaseFirestore.getInstance().collection(Constants.COLLECTIONS);
        postsCollectionReference = FirebaseFirestore.getInstance().collection(Constants.POSTS);
        impressionReference = FirebaseDatabase.getInstance().getReference(Constants.VIEWS);
        roomsCollections = FirebaseFirestore.getInstance().collection(Constants.MESSAGES);
        roomsQuery = roomsCollections.document("last messages")
                .collection(firebaseAuth.getCurrentUser().getUid());
        timelineCollection = FirebaseFirestore.getInstance().collection(Constants.TIMELINE);
        timelineQuery = timelineCollection.document(firebaseAuth.getCurrentUser().getUid())
                .collection("activities");
        collectionsCollection = FirebaseFirestore.getInstance().collection(Constants.COLLECTIONS);
        collectionsQuery = collectionsCollection.orderBy("name", Query.Direction.ASCENDING);
        impressionReference.keepSynced(true);
    }

    private void addFragmentsToBottomNavigation(){
        AHBottomNavigationItem home = new AHBottomNavigationItem(R.string.home,
                R.drawable.ic_home, R.color.grey_700);
        AHBottomNavigationItem collection = new AHBottomNavigationItem(R.string.collections,
                R.drawable.ic_collection, R.color.grey_700);
        AHBottomNavigationItem channels = new AHBottomNavigationItem(R.string.channels,
                R.drawable.ic_play, R.color.grey_700);
        AHBottomNavigationItem notifications = new AHBottomNavigationItem(R.string.notifications,
                R.drawable.ic_notifications, R.color.grey_700);
        AHBottomNavigationItem chats = new AHBottomNavigationItem(R.string.inbox,
                R.drawable.ic_inbox, R.color.grey_700);
        mBottomNavigationView.addItem(home);
        mBottomNavigationView.addItem(collection);
        mBottomNavigationView.addItem(channels);
        mBottomNavigationView.addItem(notifications);
        mBottomNavigationView.addItem(chats);
    }

    private void bottomNavigationListener(){
        mBottomNavigationView.setOnTabSelectedListener(new AHBottomNavigation.OnTabSelectedListener() {
            @Override
            public boolean onTabSelected(int position, boolean wasSelected) {

                if (!wasSelected){
                    setBackground(position);
                    noSwipePager.setCurrentItem(position);
                    int lastPosition = mBottomNavigationView.getItemsCount()-1;
                    if (notificationVisible && position == lastPosition){
                        mBottomNavigationView.setNotification(new AHNotification(), lastPosition);
                    }

                }
                return true;
            }
        });
    }

    private void setUpWithViewPager(){
        noSwipePager.setPagingEnabled(false);
        navigationPagerAdapter = new BottomNavigationPagerAdapter(this.getSupportFragmentManager());
        navigationPagerAdapter.addFragments(homeFragment);
        navigationPagerAdapter.addFragments(collectionFragment);
        navigationPagerAdapter.addFragments(channelsFragment);
        navigationPagerAdapter.addFragments(activitiesFragment);
        navigationPagerAdapter.addFragments(chatsFragment);
        noSwipePager.setAdapter(navigationPagerAdapter);
        noSwipePager.setOffscreenPageLimit(5);

    }

    private void setUpBottomNavigationStyle(){
        mBottomNavigationView.setForceTint(false);
        mBottomNavigationView.setDefaultBackgroundColor(Color.WHITE);
        mBottomNavigationView.setAccentColor(fetchColor(R.color.colorPrimary));
        mBottomNavigationView.setInactiveColor(fetchColor(R.color.grey_700));
        mBottomNavigationView.setTitleState(AHBottomNavigation.TitleState.ALWAYS_HIDE);
    }

    private void setBackground(int position) {
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) noSwipePager.getLayoutParams();
        if (position == 2) {
            mAppBarLayout.setVisibility(View.GONE);
            mBottomNavigationView.setDefaultBackgroundColor(Color.TRANSPARENT);
            params.setBehavior(null);
            noSwipePager.requestLayout();
        }else {
            params.setBehavior(new AppBarLayout.ScrollingViewBehavior());
            noSwipePager.requestLayout();
            mAppBarLayout.setVisibility(View.VISIBLE);
            mBottomNavigationView.setDefaultBackgroundColor(Color.WHITE);
        }
    }

    private int fetchColor(@ColorRes int color){
        return ContextCompat.getColor(this, color);
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

}

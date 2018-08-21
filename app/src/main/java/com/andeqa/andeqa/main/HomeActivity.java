package com.andeqa.andeqa.main;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.R;
import com.andeqa.andeqa.collections.CollectionsFragment;
import com.andeqa.andeqa.creation.ChooseCreationFragment;
import com.andeqa.andeqa.explore.ExploreFragment;
import com.andeqa.andeqa.home.HomeFragment;
import com.andeqa.andeqa.message.MessagesFragment;
import com.andeqa.andeqa.notifications.NotificationsFragment;
import com.andeqa.andeqa.settings.HomeSettingsFragment;
import com.andeqa.andeqa.utils.BottomNavigationViewBehavior;
import com.andeqa.andeqa.utils.BottomNavigationViewHelper;
import com.andeqa.andeqa.utils.CountDrawable;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import javax.annotation.Nullable;

import butterknife.Bind;
import butterknife.ButterKnife;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener,
        ForceUpdateChecker.OnUpdateNeededListener  {
    @Bind(R.id.bottomNavigationView)BottomNavigationView mBottomNavigationView;
    @Bind(R.id.fab)FloatingActionButton mFloatingActionButton;
    @Bind(R.id.appLogoImageView)ImageView appLogoImageView;


    private Uri photoUri;
    private static final String TAG = HomeActivity.class.getSimpleName();
    private static final String KEY_STATE = "state";
    private int mSelectedItem;

    private CollectionReference timelineCollection;
    private CollectionReference collectionsCollection;
    private Query timelineQuery;
    private Query roomsQuery;
    private CollectionReference messagingCollection;
    private FirebaseAuth firebaseAuth;

    //bottom navigation view
    final FragmentManager fragmentManager = getSupportFragmentManager();
    final Fragment homeFragment = new HomeFragment();
    final Fragment notificationsFragment = new NotificationsFragment();
    final Fragment collectionFragment = new CollectionsFragment();
    final Fragment messagesFragment = new MessagesFragment();
    final Fragment exploreFragment = new ExploreFragment();
    private int count;
    private Menu bottomMenu;
    private static final int SECONDS_TO_FORCED_UPDATE = 1296000;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        ButterKnife.bind(this);
        //initialize force update checker
        ForceUpdateChecker.with(this).onUpdateNeeded(this).check();
        //initialize firebase authentication
        firebaseAuth = FirebaseAuth.getInstance();
        mFloatingActionButton.setOnClickListener(this);
        appLogoImageView.setOnClickListener(this);
        //firestore references
        timelineCollection = FirebaseFirestore.getInstance().collection(Constants.TIMELINE);
        timelineCollection = FirebaseFirestore.getInstance().collection(Constants.TIMELINE);
        timelineQuery = timelineCollection.document(firebaseAuth.getCurrentUser().getUid())
                .collection("activities").whereEqualTo("status", "un_read");
        messagingCollection = FirebaseFirestore.getInstance().collection(Constants.MESSAGES);
        roomsQuery = messagingCollection.document("last messages")
                .collection(firebaseAuth.getCurrentUser().getUid()).whereEqualTo("status", "un_read");
        collectionsCollection = FirebaseFirestore.getInstance().collection(Constants.USER_COLLECTIONS);
        initializeCollections();
        //bottom navigation
        BottomNavigationViewHelper.disableShiftMode(mBottomNavigationView);
        mBottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView
                .OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                selectFragment(item);
                return true;
            }
        });

        CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams)
                mBottomNavigationView.getLayoutParams();
        layoutParams.setBehavior(new BottomNavigationViewBehavior());
        MenuItem selectedItem;
        selectedItem = mBottomNavigationView.getMenu().getItem(0);
        selectFragment(selectedItem);

    }

    @Override
    public void onUpdateNeeded(final String updateUrl) {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("New version available")
                .setMessage("Please update app to Andeqa the newer version to continue using the service")
                .setPositiveButton("Update",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                redirectStore(updateUrl);
                            }
                        }).create();
//        dialog.show();
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

                }

            }
        });
    }

    private void redirectStore(String updateUrl) {
        final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(updateUrl));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
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
        timelineNotifications();
        messageNotifications();
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

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.main_menu, menu);
//
//
//        return true;
//    }
//
//    @Override
//    public boolean onPrepareOptionsMenu(Menu menu) {
//        return super.onPrepareOptionsMenu(menu);
//    }
//
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        int id = item.getItemId();
//
//        if (id == R.id.action_search){
//            Intent intent = new Intent(HomeActivity.this, SearchActivity.class);
//            startActivity(intent);
//        }
//
//        return super.onOptionsItemSelected(item);
//    }



    private void timelineNotifications(){
        MenuItem menuItem = mBottomNavigationView.getMenu().findItem(R.id.action_timeline);
        final LayerDrawable icon = (LayerDrawable) menuItem.getIcon();
        menuItem.setIcon(icon);

        CountDrawable badge;
        badge = new CountDrawable(HomeActivity.this);
        badge.setCount(count + "");
        icon.mutate();
        icon.setDrawableByLayerId(R.id.ic_group_count, badge);

        timelineQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (!queryDocumentSnapshots.isEmpty()){
                    final int count = queryDocumentSnapshots.size();
                    CountDrawable badge;
                    badge = new CountDrawable(HomeActivity.this);
                    badge.setCount(count + "");
                    icon.mutate();
                    icon.setDrawableByLayerId(R.id.ic_group_count, badge);
                }
            }
        });

    }

    private void messageNotifications(){
        MenuItem menuItem = mBottomNavigationView.getMenu().findItem(R.id.action_timeline);
        final LayerDrawable icon = (LayerDrawable) menuItem.getIcon();
        menuItem.setIcon(icon);

        CountDrawable badge;
        badge = new CountDrawable(HomeActivity.this);
        badge.setCount(count + "");
        icon.mutate();
        icon.setDrawableByLayerId(R.id.ic_group_count, badge);

        roomsQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (!queryDocumentSnapshots.isEmpty()){
                    final int count = queryDocumentSnapshots.size();
                    CountDrawable badge;
                    badge = new CountDrawable(HomeActivity.this);
                    badge.setCount(count + "");
                    icon.mutate();
                    icon.setDrawableByLayerId(R.id.ic_group_count, badge);
                }
            }
        });

    }


    private void selectFragment(MenuItem item){
        //initialize each corresponding fragment
        switch (item.getItemId()){
            case R.id.action_home:
                FragmentTransaction ft = fragmentManager.beginTransaction();
                ft.replace(R.id.container, homeFragment);
                ft.commit();
                break;
            case R.id.action_collection:
                FragmentTransaction collectionTransaction = fragmentManager.beginTransaction();
                 collectionTransaction.replace(R.id.container, collectionFragment).commit();
                break;
            case R.id.action_explore:
                FragmentTransaction exploreTransaction = fragmentManager.beginTransaction();
                exploreTransaction.replace(R.id.container, exploreFragment).commit();
                break;
            case R.id.action_chats:
                FragmentTransaction timelineTransaction = fragmentManager.beginTransaction();
                timelineTransaction.replace(R.id.container, messagesFragment).commit();
                break;
            case R.id.action_timeline:
                FragmentTransaction profileTransaction = fragmentManager.beginTransaction();
                profileTransaction.replace(R.id.container, notificationsFragment).commit();
                break;

        }

        //update selected item
        mSelectedItem = item.getItemId();
        //uncheck the other items
        for(int i = 0; i < mBottomNavigationView.getMenu().size(); i++){
            MenuItem menuItem = mBottomNavigationView.getMenu().getItem(i);
            menuItem.setChecked(menuItem.getItemId() ==item.getItemId());
        }
    }


    @Override
    public void onClick(View v){

        if (v == mFloatingActionButton){
            ChooseCreationFragment chooseCreationFragment = ChooseCreationFragment.newInstance();
            chooseCreationFragment.show(getSupportFragmentManager(), "create bottom fragment");
        }

        if (v == appLogoImageView){
            HomeSettingsFragment homeSettingsFragment = HomeSettingsFragment.newInstance();
            homeSettingsFragment.show(getSupportFragmentManager(), "share bottom fragment");
        }
    }

}

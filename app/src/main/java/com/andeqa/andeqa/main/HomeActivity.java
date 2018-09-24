package com.andeqa.andeqa.main;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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
import com.andeqa.andeqa.collections.CollectionsFragment;
import com.andeqa.andeqa.home.HomeFragment;

import com.andeqa.andeqa.models.Andeqan;
import com.andeqa.andeqa.more.ActivitiesFragment;
import com.andeqa.andeqa.chat_rooms.ChatsFragment;
import com.andeqa.andeqa.profile.ProfileActivity;
import com.andeqa.andeqa.utils.BottomNavigationViewBehavior;
import com.andeqa.andeqa.utils.BottomNavigationViewHelper;
import com.andeqa.andeqa.utils.CountDrawable;
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

import javax.annotation.Nullable;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener,
        ForceUpdateChecker.OnUpdateNeededListener  {
    @Bind(R.id.bottomNavigationView)BottomNavigationView mBottomNavigationView;
//    @Bind(R.id.fab)FloatingActionButton mFloatingActionButton;
    @Bind(R.id.toolbar)Toolbar toolbar;
    @Bind(R.id.titleTextView)TextView titleTextView;
    @Bind(R.id.profileImageView)
    CircleImageView mProfileImageView;

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
    final Fragment collectionFragment = new CollectionsFragment();
    final Fragment timelineFragment = new ActivitiesFragment();
    final Fragment chatsFragment = new ChatsFragment();

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
        collectionsCollection = FirebaseFirestore.getInstance().collection(Constants.USER_COLLECTIONS);
        usersReference = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);
        initializeCollections();
//        displayPopupWindow();
        setProfile();
        /***disable bottom navigation shifting mode**/
        BottomNavigationViewHelper.disableShiftMode(mBottomNavigationView);
        /***bottom navigation click listeners**/
        mBottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView
                .OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                selectFragment(item);
                return true;
            }
        });

//        /***hide bottom navigation when user scrolls the home actvity***/
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
        MenuItem selectedItem;
        selectedItem = mBottomNavigationView.getMenu().getItem(0);
        selectFragment(selectedItem);
        timelineNotifications();
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
        MenuItem menuItem = mBottomNavigationView.getMenu().findItem(R.id.action_chat);
        final LayerDrawable icon = (LayerDrawable) menuItem.getIcon();
        menuItem.setIcon(icon);

        int count = 0;
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


    private void selectFragment(MenuItem item){
        //initialize each corresponding fragment
        switch (item.getItemId()){
            case R.id.action_home:
                titleTextView.setText("Andeqa");
                FragmentTransaction homeTransaction = fragmentManager.beginTransaction();
                homeTransaction.replace(R.id.container, homeFragment).commit();
                break;
            case R.id.action_collection:
                titleTextView.setText("Andeqa");
                FragmentTransaction collectionTransaction = fragmentManager.beginTransaction();
                collectionTransaction.replace(R.id.container, collectionFragment).commit();
                break;
            case R.id.action_timeline:
                titleTextView.setText("Andeqa");
                FragmentTransaction timelineTransaction = fragmentManager.beginTransaction();
                timelineTransaction.replace(R.id.container, timelineFragment).commit();
                break;
            case R.id.action_chat:
                titleTextView.setText("Andeqa");
                FragmentTransaction exploreTransaction = fragmentManager.beginTransaction();
                exploreTransaction.replace(R.id.container, chatsFragment).commit();
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

//        if (v == mFloatingActionButton){
//            ChooseCreationFragment chooseCreationFragment = ChooseCreationFragment.newInstance();
//            chooseCreationFragment.show(getSupportFragmentManager(), "create bottom fragment");
//        }

        if (v == mProfileImageView){
            Intent intent = new Intent(this, ProfileActivity.class);
            intent.putExtra(HomeActivity.EXTRA_USER_UID, firebaseAuth.getCurrentUser().getUid());
            startActivity(intent);
        }

    }


    private void displayPopupWindow() {
        final BottomNavigationMenuView menuView = (BottomNavigationMenuView)
                mBottomNavigationView.getMenu().getItem(3);

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

}

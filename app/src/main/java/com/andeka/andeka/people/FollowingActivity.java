package com.andeka.andeka.people;

import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.andeka.andeka.Constants;
import com.andeka.andeka.R;
import com.andeka.andeka.utils.EndlessLinearScrollListener;
import com.andeka.andeka.utils.ItemOffsetDecoration;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class FollowingActivity extends AppCompatActivity {
    @Bind(R.id.followingRecyclerView)RecyclerView mFollowingRecyclerView;
    @Bind(R.id.toolbar)Toolbar toolbar;
    @Bind(R.id.progressBar) ProgressBar mProgressBar;
    @Bind(R.id.progressRelativeLayout)RelativeLayout mProgressRelativeLayout;
    //firestore
    private CollectionReference usersCollection;
    private CollectionReference peopleCollection;
    //strings
    private static final String TAG = FollowersActivity.class.getSimpleName();
    private static final String EXTRA_USER_UID = "uid";
    private String mUid;
    //layouts
    private LinearLayoutManager layoutManager;
    private ItemOffsetDecoration itemOffsetDecoration;
    //lists
    private int INITIAL_ITEMS = 50;
    private int TOTAL_ITEMS = 20;
    private List<DocumentSnapshot> snapshots = new ArrayList<>();
    //adapters
    private PeopleAdapter followingAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_following);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        // set up action bar
        toolbar.setNavigationIcon(R.drawable.ic_arrow);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        //get intents
        getIntents();
        // initialize firebase references
        initReferences();

    }


    @Override
    public void onStart() {
        super.onStart();
        snapshots.clear();
        //set up the adapter
        setRecyclerView();
        getFollowing();
        mFollowingRecyclerView.addItemDecoration(itemOffsetDecoration);
        mFollowingRecyclerView.addOnScrollListener(new EndlessLinearScrollListener() {
            @Override
            public void onLoadMore() {
               getNextFollowing();
            }
        });

    }

    @Override
    public void onStop() {
        super.onStop();
        mFollowingRecyclerView.removeItemDecoration(itemOffsetDecoration);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void getIntents(){
        mUid = getIntent().getStringExtra(EXTRA_USER_UID);
    }

    private void initReferences(){
        // init firestore references
        usersCollection = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);
        peopleCollection = FirebaseFirestore.getInstance().collection(Constants.PEOPLE_RELATIONS);

    }

    private void setRecyclerView() {
        followingAdapter = new PeopleAdapter(this, snapshots);
        mFollowingRecyclerView.setHasFixedSize(false);
        mFollowingRecyclerView.setAdapter(followingAdapter);
        layoutManager = new LinearLayoutManager(this);
        itemOffsetDecoration = new ItemOffsetDecoration(this, R.dimen.item_off_set);
        mFollowingRecyclerView.setLayoutManager(layoutManager);
    }

    private void getFollowing(){
        Query query = peopleCollection.document("following")
                .collection(mUid).orderBy("following_id")
                .limit(INITIAL_ITEMS);

        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable QuerySnapshot documentSnapshots,
                                @javax.annotation.Nullable FirebaseFirestoreException e) {

                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (!documentSnapshots.isEmpty()){
                    for (DocumentSnapshot snapshot: documentSnapshots){
                        snapshots.add(snapshot);
                        followingAdapter.notifyItemInserted(snapshots.size() - 1);
                    }

                }
            }
        });

    }

    private void getNextFollowing(){
        DocumentSnapshot last = snapshots.get(snapshots.size() - 1);
        Query query = peopleCollection.document("following")
                .collection(mUid).orderBy("following_id")
                .startAfter(last).limit(TOTAL_ITEMS);

        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(final QuerySnapshot documentSnapshots) {

                if (!documentSnapshots.isEmpty()){
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            for (DocumentSnapshot snapshot: documentSnapshots){
                                snapshots.add(snapshot);
                                followingAdapter.notifyItemInserted(snapshots.size() - 1);
                            }
                        }
                    }, 4000);

                }
            }
        });

    }


    private void showToast(@NonNull String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}

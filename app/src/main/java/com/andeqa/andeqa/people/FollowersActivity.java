package com.andeqa.andeqa.people;

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

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.R;
import com.andeqa.andeqa.utils.EndlessLinearScrollListener;
import com.andeqa.andeqa.utils.ItemOffsetDecoration;
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

public class FollowersActivity extends AppCompatActivity {
    @Bind(R.id.followersRecyclerView)RecyclerView followersRecyclerView;
    @Bind(R.id.toolbar)Toolbar toolbar;
    @Bind(R.id.progressBar) ProgressBar mProgressBar;
    @Bind(R.id.progressRelativeLayout)RelativeLayout mProgressRelativeLayout;
    //firestore
    private CollectionReference peopleCollection;
    //strings
    private static final String TAG = FollowersActivity.class.getSimpleName();
    private static final String EXTRA_USER_UID = "uid";
    private static final String EXTRA_ROOM_ID = "roomId";
    private String roomId;
    private String mUid;
    //layouts
    private LinearLayoutManager layoutManager;
    private ItemOffsetDecoration itemOffsetDecoration;
    //lists
    private int INITIAL_ITEMS = 50;
    private int TOTAL_ITEMS = 20;
    private List<DocumentSnapshot> snapshots = new ArrayList<>();
    //adapters
    private PeopleAdapter peopleAdapter;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_followers);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        toolbar.setNavigationIcon(R.drawable.ic_arrow);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        getIntents();
        initReferences();


    }


    @Override
    public void onStart() {
        super.onStart();
        snapshots.clear();
        //set up the adapter
        getFollowers();
        setRecyclerView();

        followersRecyclerView.addOnScrollListener(new EndlessLinearScrollListener() {
            @Override
            public void onLoadMore() {
               getNextFollowers();
            }
        });

    }

    @Override
    public void onStop() {
        super.onStop();
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
        peopleCollection = FirebaseFirestore.getInstance().collection(Constants.PEOPLE_RELATIONS);
    }

    private void setRecyclerView() {
        peopleAdapter = new PeopleAdapter(this, snapshots);
        followersRecyclerView.setHasFixedSize(false);
        followersRecyclerView.setAdapter(peopleAdapter);
        layoutManager = new LinearLayoutManager(this);
        itemOffsetDecoration = new ItemOffsetDecoration(this, R.dimen.item_off_set);
        followersRecyclerView.setLayoutManager(layoutManager);
    }

    private void getFollowers() {
        Query query = peopleCollection.document("followers")
                .collection(mUid).orderBy("followed_id")
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
                        peopleAdapter.notifyItemInserted(snapshots.size() - 1);
                    }

                }
            }
        });

    }

    private void getNextFollowers(){
        DocumentSnapshot last = snapshots.get(snapshots.size() - 1);
        Query query = peopleCollection.document("followers")
                .collection(mUid).orderBy("followed_id")
                .startAfter(last).limit(TOTAL_ITEMS);

        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(final QuerySnapshot documentSnapshots) {

                if (!documentSnapshots.isEmpty()){
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            getNextFollowers();
                            for (DocumentSnapshot snapshot: documentSnapshots){
                                snapshots.add(snapshot);
                                peopleAdapter.notifyItemInserted(snapshots.size() - 1);
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

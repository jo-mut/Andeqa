package com.andeka.andeka.profile;

import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.andeka.andeka.Constants;
import com.andeka.andeka.R;
import com.andeka.andeka.collections.CollectionsAdapter;
import com.andeka.andeka.models.Andeqan;
import com.andeka.andeka.utils.EndlessStaggeredScrollListener;
import com.andeka.andeka.utils.ItemOffsetDecoration;
import com.google.android.gms.tasks.OnSuccessListener;
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

public class ProfileCollectionsActivity extends AppCompatActivity{
    @Bind(R.id.collectionsRecyclerView)RecyclerView mCollectionsRecyclerView;
    @Bind(R.id.toolbar)Toolbar mToolbar;
    private static final String TAG = ProfileCollectionsActivity.class.getSimpleName();

    //firestore reference
    private CollectionReference collectionsCollection;
    private CollectionReference usersCollection;
    private Query collectionsQuery;
    //firebase auth
    private FirebaseAuth firebaseAuth;
    //layout
    private StaggeredGridLayoutManager layoutManager;
    private ItemOffsetDecoration itemOffsetDecoration;
    private CollectionsAdapter collectionsAdapter;
    // strings
    private static final String COLLECTION_ID = "collection id";
    private static final String EXTRA_USER_UID = "uid";
    private String mUid;
    private int INITIAL_ITEMS = 20;
    private int TOTAL_ITEMS = 10;
    //lists
    private List<DocumentSnapshot> snapshots = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_collections);
        ButterKnife.bind(this);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationIcon(R.drawable.ic_arrow);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        //get intents extras
        getIntents();
        // init firebase references and auth
        initFirebase();


    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStart() {
        super.onStart();
        snapshots.clear();
        getProfileCollections();
        mCollectionsRecyclerView.addItemDecoration(itemOffsetDecoration);

        mCollectionsRecyclerView.addOnScrollListener(new EndlessStaggeredScrollListener() {
            @Override
            public void onLoadMore() {
                getNextCollections();
            }
        });

    }

    @Override
    public void onStop() {
        super.onStop();
        mCollectionsRecyclerView.removeItemDecoration(itemOffsetDecoration);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void initFirebase() {
        firebaseAuth = FirebaseAuth.getInstance();
        collectionsCollection = FirebaseFirestore.getInstance().collection(Constants.COLLECTIONS);
        collectionsQuery = collectionsCollection.orderBy("time", Query.Direction.DESCENDING);
        usersCollection = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);
        usersCollection.document(mUid).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {

                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (documentSnapshot.exists()){
                    Andeqan andeqan = documentSnapshot.toObject(Andeqan.class);
                    final String username = andeqan.getUsername();
                    mToolbar.setTitle(username + "'s" + " collections");

                }
            }
        });
    }

    private void getIntents() {
        mUid = getIntent().getStringExtra(EXTRA_USER_UID);
    }


    @Override
    public void onResume() {
        super.onResume();
    }


    private void getProfileCollections(){

        collectionsQuery.whereEqualTo("user_id", mUid)
                .limit(INITIAL_ITEMS);
        collectionsQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
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
                        collectionsAdapter.notifyItemInserted(snapshots.size() - 1);
                    }

                    Log.d("total posts,", snapshots.size() + "");
                }
            }
        });

        collectionsAdapter = new CollectionsAdapter(ProfileCollectionsActivity.this, snapshots);
        collectionsAdapter.setHasStableIds(true);
        mCollectionsRecyclerView.setAdapter(collectionsAdapter);
        mCollectionsRecyclerView.setHasFixedSize(false);
        layoutManager = new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL);
        itemOffsetDecoration = new ItemOffsetDecoration(this, R.dimen.item_off_set);
        mCollectionsRecyclerView.setLayoutManager(layoutManager);
        ViewCompat.setNestedScrollingEnabled(mCollectionsRecyclerView,false);

    }

    private void getNextCollections(){
        DocumentSnapshot last = snapshots.get(snapshots.size() - 1);
        collectionsQuery.whereEqualTo("user_id", mUid)
                .startAfter(last).limit(INITIAL_ITEMS);

        collectionsQuery.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(final QuerySnapshot documentSnapshots) {

                if (!documentSnapshots.isEmpty()){
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            for (DocumentSnapshot snapshot: documentSnapshots){
                                snapshots.add(snapshot);
                                collectionsAdapter.notifyItemInserted(snapshots.size() - 1);
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

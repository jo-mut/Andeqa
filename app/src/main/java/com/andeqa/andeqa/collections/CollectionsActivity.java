package com.andeqa.andeqa.collections;

import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.utils.BottomReachedListener;
import com.andeqa.andeqa.utils.EndlessLinearScrollListener;
import com.andeqa.andeqa.utils.EndlessStaggeredScrollListener;
import com.andeqa.andeqa.utils.ItemOffsetDecoration;
import com.andeqa.andeqa.R;
import com.andeqa.andeqa.search.SearchedCollectionsActivity;
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

import butterknife.Bind;
import butterknife.ButterKnife;

public class CollectionsActivity extends AppCompatActivity {
    @Bind(R.id.exploreCollectionsRecyclerView)RecyclerView mExploreCollectionsRecyclerView;
    @Bind(R.id.toolbar) Toolbar mToolbar;
    @Bind(R.id.progressRelativeLayout)RelativeLayout mProgressRelativeLayout;

    private static final String TAG = CollectionsFragment.class.getSimpleName();
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
    private static final int TOTAL_ITEMS = 20;
    private List<DocumentSnapshot> snapshots = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explore_collections);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        setTitle("Collections");
        setSupportActionBar(mToolbar);

        mToolbar.setNavigationIcon(R.drawable.ic_arrow);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        initFirebase();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        menu.clear();
        inflater.inflate(R.menu.explore_menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_search){
            Intent intent =  new Intent(this, SearchedCollectionsActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        snapshots.clear();
        setDocuments();
        mExploreCollectionsRecyclerView.addItemDecoration(itemOffsetDecoration);

        mExploreCollectionsRecyclerView.addOnScrollListener(new EndlessStaggeredScrollListener() {
            @Override
            public void onLoadMore() {
               getNextDocuments();
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        mExploreCollectionsRecyclerView.removeItemDecoration(itemOffsetDecoration);
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

    private void initFirebase(){
        firebaseAuth = FirebaseAuth.getInstance();
        //firestore references
        usersCollection = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);
        collectionsCollection = FirebaseFirestore.getInstance().collection(Constants.COLLECTIONS);
        collectionsQuery = collectionsCollection.orderBy("time", Query.Direction.DESCENDING);
    }


    private void setDocuments(){
        collectionsQuery.limit(TOTAL_ITEMS).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (!documentSnapshots.isEmpty()) {
                    for (DocumentSnapshot collection: documentSnapshots){
                        snapshots.add(collection);
                        collectionsAdapter.notifyItemInserted(snapshots.size() - 1);
                    }
                }

            }
        });

        collectionsAdapter = new CollectionsAdapter(this, snapshots);
        collectionsAdapter.setHasStableIds(true);
        mExploreCollectionsRecyclerView.setAdapter(collectionsAdapter);
        mExploreCollectionsRecyclerView.setHasFixedSize(false);
        layoutManager = new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL);
        itemOffsetDecoration = new ItemOffsetDecoration(this, R.dimen.item_off_set);
        mExploreCollectionsRecyclerView.setLayoutManager(layoutManager);
        ViewCompat.setNestedScrollingEnabled(mExploreCollectionsRecyclerView,false);

    }


    private void getNextDocuments(){
        DocumentSnapshot lastVisible = snapshots.get(snapshots.size() - 1);

        //retrieve the first bacth of posts
        Query nextSinglesQuery = collectionsCollection.orderBy("time", Query.Direction.ASCENDING)
                .startAfter(lastVisible).limit(TOTAL_ITEMS);

        nextSinglesQuery.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(final QuerySnapshot documentSnapshots) {
                if (!documentSnapshots.isEmpty()){
                    mProgressRelativeLayout.setVisibility(View.VISIBLE);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            for (DocumentSnapshot collection: documentSnapshots){
                                snapshots.add(collection);
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

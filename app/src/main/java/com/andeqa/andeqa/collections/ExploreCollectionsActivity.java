package com.andeqa.andeqa.collections;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.utils.BottomReachedListener;
import com.andeqa.andeqa.utils.ItemOffsetDecoration;
import com.andeqa.andeqa.R;
import com.andeqa.andeqa.search.SearchedCollectionsActivity;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
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

public class ExploreCollectionsActivity extends AppCompatActivity {
    @Bind(R.id.exploreCollectionsRecyclerView)RecyclerView mExploreCollectionsRecyclerView;
    @Bind(R.id.toolbar) Toolbar mToolbar;

    private static final String TAG = CollectionsFragment.class.getSimpleName();
    //firestore reference
    private CollectionReference collectionsCollection;
    private CollectionReference usersReference;
    private Query collectionsQuery;
    //firebase auth
    private FirebaseAuth firebaseAuth;
    //firestore adapters
    private ExploreCollectionsAdapter mExploreCollectionsAdapter;
    private int TOTAL_ITEMS = 10;
    private StaggeredGridLayoutManager layoutManager;
    private static final String EXTRA_USER_UID = "uid";
    private SearchView searchView;
    private List<String> mSnapshotsIds = new ArrayList<>();
    private List<DocumentSnapshot> documentSnapshots = new ArrayList<>();
    private ItemOffsetDecoration itemOffsetDecoration;

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

        //initialize click listener
        //FIREBASE AUTH
        firebaseAuth = FirebaseAuth.getInstance();

        usersReference = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);
        collectionsCollection = FirebaseFirestore.getInstance().collection(Constants.COLLECTIONS);
        collectionsQuery = collectionsCollection.orderBy("time", Query.Direction.DESCENDING);

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
        documentSnapshots.clear();
        setRecyclerView();
        setColections();
        mExploreCollectionsRecyclerView.addItemDecoration(itemOffsetDecoration);

        mExploreCollectionsAdapter.setBottomReachedListener(new BottomReachedListener() {
            @Override
            public void onBottomReached(int position) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        setNextCollections();
                    }
                },1000);
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

    private void setRecyclerView(){
        // RecyclerView
        mExploreCollectionsAdapter = new ExploreCollectionsAdapter(this);
        mExploreCollectionsRecyclerView.setAdapter(mExploreCollectionsAdapter);
        mExploreCollectionsAdapter.setCollections(documentSnapshots);
        mExploreCollectionsRecyclerView.setHasFixedSize(false);
        layoutManager = new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL);
        itemOffsetDecoration = new ItemOffsetDecoration(this, R.dimen.item_off_set);
        mExploreCollectionsRecyclerView.setLayoutManager(layoutManager);

    }


    private void setColections(){
        collectionsQuery.limit(TOTAL_ITEMS).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (!documentSnapshots.isEmpty()) {
                    for (final DocumentChange change : documentSnapshots.getDocumentChanges()) {
                        switch (change.getType()) {
                            case ADDED:
                                onDocumentAdded(change);
                                break;
                            case MODIFIED:
                                onDocumentModified(change);
                                break;
                            case REMOVED:
                                onDocumentRemoved(change);
                                break;
                        }
                    }
                }

            }
        });

    }


    private void setNextCollections(){
        // Get the last visible document
        final int snapshotSize = mExploreCollectionsAdapter.getItemCount();

        if (snapshotSize!= 0){
            DocumentSnapshot lastVisible = mExploreCollectionsAdapter.getSnapshot(snapshotSize - 1);

            //retrieve the first bacth of posts
            Query nextSinglesQuery = collectionsCollection.orderBy("time", Query.Direction.DESCENDING)
                    .startAfter(lastVisible).limit(TOTAL_ITEMS);

            nextSinglesQuery.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot documentSnapshots) {
                    if (!documentSnapshots.isEmpty()){
                        //retrieve the first bacth of posts
                        for (final DocumentChange change : documentSnapshots.getDocumentChanges()) {
                            switch (change.getType()) {
                                case ADDED:
                                    onDocumentAdded(change);
                                    break;
                                case MODIFIED:
                                    onDocumentModified(change);
                                    break;
                                case REMOVED:
                                    onDocumentRemoved(change);
                                    break;
                            }
                        }
                    }
                }
            });
        }

    }



    protected void onDocumentAdded(DocumentChange change) {
        mSnapshotsIds.add(change.getDocument().getId());
        documentSnapshots.add(change.getDocument());
        mExploreCollectionsAdapter.notifyItemInserted(documentSnapshots.size() -1);
        mExploreCollectionsAdapter.getItemCount();

    }

    protected void onDocumentModified(DocumentChange change) {
        if (change.getOldIndex() == change.getNewIndex()) {
            // Item changed but remained in same position
            documentSnapshots.set(change.getOldIndex(), change.getDocument());
            mExploreCollectionsAdapter.notifyItemChanged(change.getOldIndex());
        } else {
            // Item changed and changed position
            documentSnapshots.remove(change.getOldIndex());
            documentSnapshots.add(change.getNewIndex(), change.getDocument());
            mExploreCollectionsAdapter.notifyItemRangeChanged(0, documentSnapshots.size());
        }
    }

    protected void onDocumentRemoved(DocumentChange change) {
        try{
            documentSnapshots.remove(change.getOldIndex());
            mExploreCollectionsAdapter.notifyItemRemoved(change.getOldIndex());
            mExploreCollectionsAdapter.notifyItemRangeChanged(0, documentSnapshots.size());
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}

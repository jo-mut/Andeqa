package com.andeqa.andeqa.collections;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.R;
import com.andeqa.andeqa.utils.EndlesssStaggeredRecyclerOnScrollListener;
import com.andeqa.andeqa.utils.ItemOffsetDecoration;
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

public class ChooseCollectionActivity extends AppCompatActivity {
    @Bind(R.id.collectionsRecyclerView)RecyclerView mCollectionsRecyclerView;
    @Bind(R.id.toolbar)Toolbar toolbar;
    private static final String TAG = CollectionsFragment.class.getSimpleName();
    //firestore reference
    private CollectionReference collectionsCollection;
    private Query collectionsQuery;
    //firebase auth
    private FirebaseAuth firebaseAuth;
    //firestore adapters
    private FeaturedCollectionsAdapter featuredCollectionsAdapter;
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
        setContentView(R.layout.activity_choose_collection);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        //FIREBASE AUTH
        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser()!= null){
            collectionsCollection = FirebaseFirestore.getInstance().collection(Constants.COLLECTIONS);
            collectionsQuery = collectionsCollection.orderBy("time", Query.Direction.ASCENDING)
                    .limit(TOTAL_ITEMS);
            mCollectionsRecyclerView.addOnScrollListener(new EndlesssStaggeredRecyclerOnScrollListener() {
                @Override
                public void onLoadMore() {
                    setNextCollections();
                }
            });

        }

    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStart() {
        super.onStart();
        documentSnapshots.clear();
        setRecyclerView();
        mCollectionsRecyclerView.addItemDecoration(itemOffsetDecoration);
        setColections();

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


    private void setRecyclerView(){
        // RecyclerView
        featuredCollectionsAdapter = new FeaturedCollectionsAdapter(this);
        mCollectionsRecyclerView.setAdapter(featuredCollectionsAdapter);
        mCollectionsRecyclerView.setHasFixedSize(false);
        layoutManager = new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL);
        itemOffsetDecoration = new ItemOffsetDecoration(this, R.dimen.item_off_set);
        mCollectionsRecyclerView.setLayoutManager(layoutManager);
//        ViewCompat.setNestedScrollingEnabled(mCollectionsRecyclerView,false);

    }

    private void setColections(){
        collectionsQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
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
        final int snapshotSize = featuredCollectionsAdapter.getItemCount();

        if (snapshotSize == 0){
        }else {
            DocumentSnapshot lastVisible = featuredCollectionsAdapter.getSnapshot(snapshotSize - 1);

            //retrieve the first bacth of posts
            Query nextSinglesQuery = collectionsCollection.orderBy("time", Query.Direction.ASCENDING)
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
        featuredCollectionsAdapter.setFeaturedCollections(documentSnapshots);
        featuredCollectionsAdapter.notifyItemInserted(documentSnapshots.size() -1);
        featuredCollectionsAdapter.getItemCount();

    }

    protected void onDocumentModified(DocumentChange change) {
        if (change.getOldIndex() == change.getNewIndex()) {
            // Item changed but remained in same position
            documentSnapshots.set(change.getOldIndex(), change.getDocument());
            featuredCollectionsAdapter.notifyItemChanged(change.getOldIndex());
        } else {
            // Item changed and changed position
            documentSnapshots.remove(change.getOldIndex());
            documentSnapshots.add(change.getNewIndex(), change.getDocument());
            featuredCollectionsAdapter.notifyItemRangeChanged(0, documentSnapshots.size());
        }
    }

    protected void onDocumentRemoved(DocumentChange change) {
        try{
            documentSnapshots.remove(change.getOldIndex());
            featuredCollectionsAdapter.notifyItemRemoved(change.getOldIndex());
            featuredCollectionsAdapter.notifyItemRangeChanged(0, documentSnapshots.size());
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    @Override
    public void onResume() {
        super.onResume();

    }

}

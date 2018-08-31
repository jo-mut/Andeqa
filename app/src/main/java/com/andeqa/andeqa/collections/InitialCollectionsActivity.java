package com.andeqa.andeqa.collections;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.R;
import com.andeqa.andeqa.main.HomeActivity;
import com.andeqa.andeqa.models.Collection;
import com.andeqa.andeqa.models.QueryOptions;
import com.andeqa.andeqa.models.Relation;
import com.andeqa.andeqa.utils.EndlessRecyclerOnScrollListener;
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

public class InitialCollectionsActivity extends AppCompatActivity implements View.OnClickListener{
    @Bind(R.id.collectionsRecyclerView)RecyclerView mCollectionsRecyclerView;
    @Bind(R.id.doneButton)Button doneButton;

    private static final String TAG = InitialCollectionsActivity.class.getSimpleName();
    private static final String EXTRA_POST_ID = "post id";
    private static final String COLLECTION_ID = "collection id";
    private static final String TYPE = "type";
    private static final String EXTRA_USER_UID =  "uid";
    //firestore reference
    private CollectionReference queryParametersCollection;
    private CollectionReference collectionsCollection;
    private CollectionReference relationsCollection;
    private Query collectionsQuery;
    //firebase auth
    private FirebaseAuth firebaseAuth;
    //firestore adapters
    private FollowCollectionsAdapter followCollectionsAdapter;
    private int TOTAL_ITEMS = 10;
    private StaggeredGridLayoutManager layoutManager;
    private List<String> mSnapshotsIds = new ArrayList<>();
    private List<DocumentSnapshot> mSnapshots = new ArrayList<>();
    private ItemOffsetDecoration itemOffsetDecoration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial_collections);
        ButterKnife.bind(this);
        doneButton.setOnClickListener(this);

        //FIREBASE AUTH
        firebaseAuth = FirebaseAuth.getInstance();
        collectionsCollection = FirebaseFirestore.getInstance().collection(Constants.USER_COLLECTIONS);
        queryParametersCollection = FirebaseFirestore.getInstance().collection(Constants.QUERY_OPTIONS);
        relationsCollection = FirebaseFirestore.getInstance().collection(Constants.COLLECTION_RELATIONS);
        mCollectionsRecyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener() {
            @Override
            public void onLoadMore() {
                setNextCollections();
            }
        });

        loadData();

    }

    public InitialCollectionsActivity() {
        super();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mCollectionsRecyclerView.addItemDecoration(itemOffsetDecoration);

    }

    @Override
    protected void onStop() {
        super.onStop();
        mCollectionsRecyclerView.removeItemDecoration(itemOffsetDecoration);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void loadData(){
        mSnapshots.clear();
        setRecyclerView();
        setColections();
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onClick(View v){
        if (v == doneButton){
            followedCollections();
        }
    }

    private void setRecyclerView(){
        // RecyclerView
        followCollectionsAdapter = new FollowCollectionsAdapter(this);
        mCollectionsRecyclerView.setAdapter(followCollectionsAdapter);
        mCollectionsRecyclerView.setHasFixedSize(false);
        layoutManager = new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL);
        itemOffsetDecoration = new ItemOffsetDecoration(this, R.dimen.item_off_set);
        mCollectionsRecyclerView.setLayoutManager(layoutManager);
    }

    private void setColections(){
        collectionsCollection.limit(TOTAL_ITEMS).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(final QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (!documentSnapshots.isEmpty()) {
                    for (final DocumentChange change : documentSnapshots.getDocumentChanges()) {
                        switch (change.getType()) {
                            case ADDED:
                                Collection collection = change.getDocument().toObject(Collection.class);
                                final String userId = collection.getUser_id();
                                if (userId.equals(firebaseAuth.getCurrentUser().getUid())){
                                    //do not add collection to recycler view adapter
                                }else {
                                    onDocumentAdded(change);
                                }
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

    private void followedCollections(){
        queryParametersCollection.document("options")
                .collection(firebaseAuth.getCurrentUser().getUid())
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                        if (e != null) {
                            Log.w(TAG, "Listen error", e);
                            return;
                        }

                        if (!documentSnapshots.isEmpty()){
                            Intent intent = new Intent(InitialCollectionsActivity.this, HomeActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        }
                    }
                });
    }

    private void setNextCollections(){
        // Get the last visible document
        final int snapshotSize = followCollectionsAdapter.getItemCount();

        if (snapshotSize > 0){
            DocumentSnapshot lastVisible = followCollectionsAdapter.getSnapshot(snapshotSize - 1);

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
                                    Collection collection = change.getDocument().toObject(Collection.class);
                                    final String userId = collection.getUser_id();
                                    if (userId.equals(firebaseAuth.getCurrentUser().getUid())){
                                        //do not add collection to recycler view adapter
                                    }else {
                                        onDocumentAdded(change);
                                    }
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
        mSnapshots.add(change.getDocument());
        followCollectionsAdapter.setFeaturedCollections(mSnapshots);
        followCollectionsAdapter.notifyItemInserted(mSnapshots.size() -1);
        followCollectionsAdapter.getItemCount();

    }

    protected void onDocumentModified(DocumentChange change) {
        if (change.getOldIndex() == change.getNewIndex()) {
            // Item changed but remained in same position
            mSnapshots.set(change.getOldIndex(), change.getDocument());
            followCollectionsAdapter.notifyItemChanged(change.getOldIndex());
        } else {
            // Item changed and changed position
            mSnapshots.remove(change.getOldIndex());
            mSnapshots.add(change.getNewIndex(), change.getDocument());
            followCollectionsAdapter.notifyItemRangeChanged(0, mSnapshots.size());
        }
    }

    protected void onDocumentRemoved(DocumentChange change) {
        try{
            mSnapshots.remove(change.getOldIndex());
            followCollectionsAdapter.notifyItemRemoved(change.getOldIndex());
            followCollectionsAdapter.notifyItemRangeChanged(0, mSnapshots.size());
        }catch (Exception e){
            e.printStackTrace();
        }
    }


}

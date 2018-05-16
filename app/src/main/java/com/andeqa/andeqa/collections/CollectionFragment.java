package com.andeqa.andeqa.collections;


import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.R;
import com.andeqa.andeqa.models.Andeqan;
import com.andeqa.andeqa.profile.ProfileActivity;
import com.andeqa.andeqa.profile.ProfileCollectionsAdapter;
import com.andeqa.andeqa.utils.EndlessRecyclerOnScrollListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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

/**
 * A simple {@link Fragment} subclass.
 */
public class CollectionFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    @Bind(R.id.collectionsRecyclerView)RecyclerView mCollectionsRecyclerView;
    @Bind(R.id.swipeRefreshLayout)SwipeRefreshLayout mSwipeRefreshLayout;

    private static final String TAG = ProfileActivity.class.getSimpleName();
    //firestore reference
    private CollectionReference collectionCollection;
    private CollectionReference relationsCollections;
    private CollectionReference usersCollections;
    private CollectionReference postsCollection;
    private CollectionReference timelineCollection;
    private Query collectionsQuery;

    private Query postCountQuery;

    //firebase
    private DatabaseReference databaseReference;


    //firebase auth
    private FirebaseAuth firebaseAuth;
    //firestore adapters
    private CollectionsAdapter collectionsAdapter;
    private  static final int MAX_WIDTH = 200;
    private static final int MAX_HEIGHT = 200;
    //singles meber variables
    private DocumentSnapshot lastVisible;
    private LinearLayoutManager layoutManager;
    private static final String EXTRA_USER_UID = "uid";
    private static final int TOTAL_ITEMS = 20;
    private static final String EXTRA_ROOM_UID = "roomId";
    private String mUid;
    private List<String> collectionsIds = new ArrayList<>();
    private List<DocumentSnapshot> documentSnapshots = new ArrayList<>();



    public CollectionFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_collection, container, false);
        ButterKnife.bind(this, view);

        //FIREBASE AUTH
        firebaseAuth = FirebaseAuth.getInstance();
        mSwipeRefreshLayout.setOnRefreshListener(this);


        if (firebaseAuth.getCurrentUser()!= null){


            usersCollections = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);
            relationsCollections = FirebaseFirestore.getInstance().collection(Constants.RELATIONS);
            collectionCollection = FirebaseFirestore.getInstance().collection(Constants.USER_COLLECTIONS);
            collectionsQuery = collectionCollection.orderBy("collection_id", Query.Direction.ASCENDING)
                    .limit(TOTAL_ITEMS);

            timelineCollection = FirebaseFirestore.getInstance().collection(Constants.TIMELINE);
            postsCollection = FirebaseFirestore.getInstance().collection(Constants.POSTS);

            //firebase
            databaseReference = FirebaseDatabase.getInstance().getReference(Constants.RANDOM_PUSH_ID);

            setCollections();
            recyclerView();


        }

        return view;
    }

    @Override
    public void onRefresh() {
        setNextCollections();
    }

    private void recyclerView(){
        collectionsAdapter = new CollectionsAdapter(getContext());
        layoutManager = new LinearLayoutManager(getContext());
        mCollectionsRecyclerView.setLayoutManager(layoutManager);
        mCollectionsRecyclerView.setAdapter(collectionsAdapter);
        mCollectionsRecyclerView.setHasFixedSize(false);
        mCollectionsRecyclerView.setNestedScrollingEnabled(false);
    }

    private void setCollections(){
        collectionsQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (!documentSnapshots.isEmpty()){
                    //retrieve the first bacth of documentSnapshots
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

                    Log.d("name of collection", "data is present");

                }else {
                    Log.d("name of collection", "data is absent");

                }

            }
        });
    }

    private void setNextCollections(){
        mSwipeRefreshLayout.setRefreshing(true);

        // Get the last visible document
        final int snapshotSize = collectionsAdapter.getItemCount();
        if (snapshotSize == 0){

        }else {
            DocumentSnapshot lastVisible = collectionsAdapter.getSnapshot(snapshotSize - 1);

            //retrieve the first bacth of documentSnapshots
            Query  nextCollectionsQuery = collectionCollection.orderBy("time", Query.Direction.ASCENDING)
                    .whereEqualTo("uid", mUid).startAfter(lastVisible)
                    .limit(TOTAL_ITEMS);

            nextCollectionsQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                    if (e != null) {
                        Log.w(TAG, "Listen error", e);
                        return;
                    }

                    if (!documentSnapshots.isEmpty()){
                        //retrieve the first bacth of documentSnapshots
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
                        mSwipeRefreshLayout.setRefreshing(false);
                    }else {
                        mSwipeRefreshLayout.setRefreshing(false);

                    }
                }
            });
        }

    }


    protected void onDocumentAdded(DocumentChange change) {
        collectionsIds.add(change.getDocument().getId());
        documentSnapshots.add(change.getDocument());
        collectionsAdapter.setFeaturedCollections(documentSnapshots);
        collectionsAdapter.notifyItemInserted(documentSnapshots.size() -1);
        collectionsAdapter.getItemCount();

    }

    protected void onDocumentModified(DocumentChange change) {
        if (change.getOldIndex() == change.getNewIndex()) {
            // Item changed but remained in same position
            documentSnapshots.set(change.getOldIndex(), change.getDocument());
            collectionsAdapter.notifyItemChanged(change.getOldIndex());
        } else {
            // Item changed and changed position
            documentSnapshots.remove(change.getOldIndex());
            documentSnapshots.add(change.getNewIndex(), change.getDocument());
            collectionsAdapter.notifyItemRangeChanged(0, documentSnapshots.size());
        }
    }

    protected void onDocumentRemoved(DocumentChange change) {
        documentSnapshots.remove(change.getOldIndex());
        collectionsAdapter.notifyItemRemoved(change.getOldIndex());
        collectionsAdapter.notifyItemRangeChanged(0, documentSnapshots.size());
    }


}
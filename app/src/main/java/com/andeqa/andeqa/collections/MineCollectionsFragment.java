package com.andeqa.andeqa.collections;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.R;
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

/**
 * A simple {@link Fragment} subclass.
 */
public class MineCollectionsFragment extends Fragment implements
        SwipeRefreshLayout.OnRefreshListener {

    @Bind(R.id.collectionsRecyclerView)RecyclerView mCollectionsRecyclerView;
    @Bind(R.id.swipeRefreshLayout)SwipeRefreshLayout mSwipeRefreshLayout;

    private static final String TAG = FeaturedCollectionFragment.class.getSimpleName();
    //firestore reference
    private CollectionReference collectionCollection;
    private Query collectionsQuery;

    //firebase auth
    private FirebaseAuth firebaseAuth;
    //firestore adapters
    private ProfileCollectionsAdapter profileCollectionsAdapter;
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

    public MineCollectionsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_mine_collections, container, false);
        ButterKnife.bind(this, view);

        //FIREBASE AUTH
        firebaseAuth = FirebaseAuth.getInstance();
        mSwipeRefreshLayout.setOnRefreshListener(this);


        if (firebaseAuth.getCurrentUser()!= null){

            collectionCollection = FirebaseFirestore.getInstance().collection(Constants.USER_COLLECTIONS);
            collectionsQuery = collectionCollection.orderBy("time", Query.Direction.DESCENDING)
                    .whereEqualTo("user_id", firebaseAuth.getCurrentUser().getUid())
                    .limit(TOTAL_ITEMS);

        }
        return view;

    }

    @Override
    public void onStart() {
        super.onStart();
        documentSnapshots.clear();
        setCollections();
        recyclerView();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onRefresh() {
        setNextCollections();
    }

    private void recyclerView(){
        profileCollectionsAdapter = new ProfileCollectionsAdapter(getContext());
        layoutManager = new LinearLayoutManager(getContext());
        mCollectionsRecyclerView.setLayoutManager(layoutManager);
        mCollectionsRecyclerView.setAdapter(profileCollectionsAdapter);
        mCollectionsRecyclerView.setHasFixedSize(false);
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
        final int snapshotSize = profileCollectionsAdapter.getItemCount();
        if (snapshotSize == 0){

        }else {
            DocumentSnapshot lastVisible = profileCollectionsAdapter.getSnapshot(snapshotSize - 1);

            //retrieve the first bacth of documentSnapshots
            Query  nextCollectionsQuery = collectionCollection.orderBy("time", Query.Direction.DESCENDING)
                    .whereEqualTo("user_id", firebaseAuth.getCurrentUser().getUid())
                    .startAfter(lastVisible).limit(TOTAL_ITEMS);

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
        profileCollectionsAdapter.setProfileCollections(documentSnapshots);
        profileCollectionsAdapter.notifyItemInserted(documentSnapshots.size() -1);
        profileCollectionsAdapter.getItemCount();

    }

    protected void onDocumentModified(DocumentChange change) {
        try {
            if (change.getOldIndex() == change.getNewIndex()) {
                // Item changed but remained in same position
                documentSnapshots.set(change.getOldIndex(), change.getDocument());
                profileCollectionsAdapter.notifyItemChanged(change.getOldIndex());
            } else {
                // Item changed and changed position
                documentSnapshots.remove(change.getOldIndex());
                documentSnapshots.add(change.getNewIndex(), change.getDocument());
                profileCollectionsAdapter.notifyItemRangeChanged(0, documentSnapshots.size());
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    protected void onDocumentRemoved(DocumentChange change) {
        try {
            documentSnapshots.remove(change.getOldIndex());
            profileCollectionsAdapter.notifyItemRemoved(change.getOldIndex());
            profileCollectionsAdapter.notifyItemRangeChanged(0, documentSnapshots.size());
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        documentSnapshots.clear();
    }


}

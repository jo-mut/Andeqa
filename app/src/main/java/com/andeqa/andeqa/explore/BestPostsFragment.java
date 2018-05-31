package com.andeqa.andeqa.explore;


import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
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
public class BestPostsFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    @Bind(R.id.bestPostsRecyclerView)RecyclerView bestPostRecyclerView;
    @Bind(R.id.swipeRefreshLayout)SwipeRefreshLayout mSwipeRefreshLayout;

    private static final String TAG = "SingleOutFragment";
    private GridLayoutManager layoutManager;
    //firestore
    private CollectionReference creditCollection;
    private Query creditQuery;
    //adapters
    private BestPostAdapter bestPostAdapter;
    private FirebaseAuth firebaseAuth;
    private int TOTAL_ITEMS = 20;
    private DocumentSnapshot lastVisible;
    private List<String> snapshotsIds = new ArrayList<>();
    private List<DocumentSnapshot> documentSnapshots = new ArrayList<>();


    public BestPostsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_best_posts, container, false);
        ButterKnife.bind(this, view);

        mSwipeRefreshLayout.setOnRefreshListener(this);

        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser()!= null){
            //firestore
            creditCollection = FirebaseFirestore.getInstance().collection(Constants.CREDITS);
            creditQuery = creditCollection.orderBy("amount", Query.Direction.DESCENDING)
                    .limit(TOTAL_ITEMS);

        }


        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        documentSnapshots.clear();
        setRecyclerView();
        setCollections();
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
    public void onDestroyView() {
        super.onDestroyView();
        documentSnapshots.clear();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onRefresh() {
        setNextCollections();
    }

    private void setRecyclerView(){
        bestPostAdapter = new BestPostAdapter(getContext());
        bestPostRecyclerView.setAdapter(bestPostAdapter);
        bestPostRecyclerView.setHasFixedSize(false);
        layoutManager = new GridLayoutManager(getContext(), 3);
        bestPostRecyclerView.setLayoutManager(layoutManager);
    }

    private void setCollections(){
        creditQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (!documentSnapshots.isEmpty()){
                    Log.d("snapshot is empty", documentSnapshots.size() + "");
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
                }

            }
        });
    }

    private void setNextCollections(){
        mSwipeRefreshLayout.setRefreshing(true);
        // Get the last visible document
        final int snapshotSize = bestPostAdapter.getItemCount();

        if (snapshotSize == 0){
            mSwipeRefreshLayout.setRefreshing(false);
        }else {
            DocumentSnapshot lastVisible = bestPostAdapter.getSnapshot(snapshotSize - 1);

            //retrieve the first bacth of documentSnapshots
            Query nextSellingQuery = creditCollection.orderBy("amount", Query.Direction.DESCENDING)
                    .startAfter(lastVisible)
                    .limit(TOTAL_ITEMS);

            nextSellingQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
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
        snapshotsIds.add(change.getDocument().getId());
        documentSnapshots.add(change.getDocument());
        bestPostAdapter.setBestPosts(documentSnapshots);
        bestPostAdapter.notifyItemInserted(documentSnapshots.size() -1);
        bestPostAdapter.getItemCount();

    }

    protected void onDocumentModified(DocumentChange change) {
        try{
            if (change.getOldIndex() == change.getNewIndex()) {
                // Item changed but remained in same position
                documentSnapshots.set(change.getOldIndex(), change.getDocument());
                bestPostAdapter.notifyItemChanged(change.getOldIndex());
            } else {
                // Item changed and changed position
                documentSnapshots.remove(change.getOldIndex());
                documentSnapshots.add(change.getNewIndex(), change.getDocument());
                bestPostAdapter.notifyItemRangeChanged(0, documentSnapshots.size());
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    protected void onDocumentRemoved(DocumentChange change) {
        try {
            documentSnapshots.remove(change.getOldIndex());
            bestPostAdapter.notifyItemRemoved(change.getOldIndex());
            bestPostAdapter.notifyItemRangeChanged(0, documentSnapshots.size());
        }catch (Exception e){
            e.printStackTrace();
        }
    }


}

package com.cinggl.cinggl.home;


import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cinggl.cinggl.Constants;
import com.cinggl.cinggl.R;
import com.cinggl.cinggl.adapters.TopPostsAdapter;
import com.cinggl.cinggl.models.Credit;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;


/**
 * A simple {@link Fragment} subclass.
 */
public class BestPostsFragment extends Fragment implements
        SwipeRefreshLayout.OnRefreshListener{
    @Bind(R.id.bestPostsRecyclerView)RecyclerView bestCinglesRecyclerView;
    @Bind(R.id.swipeToRefreshLayout)SwipeRefreshLayout swipeRefreshLayout;
    private static final String TAG = "BestCingleFragment";
    private static final String KEY_LAYOUT_POSITION = "layout position";
    private LinearLayoutManager layoutManager;
    private Parcelable recyclerViewState;
    //firebase auth
    private FirebaseAuth firebaseAuth;
    //adapters
    private TopPostsAdapter topPostsAdapter;
    //firestore
    private CollectionReference senseCreditReference;
    private Query bestCinglesQuery;
    //cingles member variables
    private List<Credit> credits = new ArrayList<>();
    private List<String> creditIds = new ArrayList<>();
    private int TOTAL_ITEMS = 4;
    private DocumentSnapshot lastVisible;


    public BestPostsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() != null){
            senseCreditReference = FirebaseFirestore.getInstance().collection(Constants.SENSECREDITS);
            bestCinglesQuery = senseCreditReference.orderBy("amount", Query.Direction.ASCENDING)
                    .limit(TOTAL_ITEMS);

        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_best_cingles, container, false);
        ButterKnife.bind(this, view);
        swipeRefreshLayout.setOnRefreshListener(this);
        setCurrentDate();

        return view;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setTheFirstBacthBestCingles();
        if (savedInstanceState != null){
            recyclerViewState = savedInstanceState.getParcelable(KEY_LAYOUT_POSITION);
            Log.d("Best saved Instance", "Instance is not");
        }else {
            Log.d("Saved Instance", "Instance is completely null");
        }
    }

    private void setCurrentDate(){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("d");
        String date = simpleDateFormat.format(new Date());

        if (date.endsWith("1") && !date.endsWith("11"))
            simpleDateFormat = new SimpleDateFormat("d'st' MMM yyyy");
        else if (date.endsWith("2") && !date.endsWith("12"))
            simpleDateFormat = new SimpleDateFormat("d'nd' MMM yyyy");
        else if (date.endsWith("3") && !date.endsWith("13"))
            simpleDateFormat = new SimpleDateFormat("d'rd' MMM yyyy");
        else
            simpleDateFormat = new SimpleDateFormat("d'th' MMM yyyy");
        String currentDate = simpleDateFormat.format(new Date());

    }


    private void setTheFirstBacthBestCingles(){
        bestCinglesQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                Log.d("all best cingles", documentSnapshots.size() + "");

                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }


                for (DocumentChange change : documentSnapshots.getDocumentChanges()) {
                    switch (change.getType()) {
                        case ADDED:
                            onDocumentAdded(change);
                            break;
                        case MODIFIED:
//                            onDocumentModified(change);
                            break;
                        case REMOVED:
//                            onDocumentRemoved(change);
                            break;
                    }
                    onDataChanged();
                }

            }
        });

        // RecyclerView
        topPostsAdapter = new TopPostsAdapter(getContext());
        bestCinglesRecyclerView.setAdapter(topPostsAdapter);
        bestCinglesRecyclerView.setHasFixedSize(false);
        layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setAutoMeasureEnabled(true);
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        bestCinglesRecyclerView.setLayoutManager(layoutManager);

    }

    private void onDocumentAdded(DocumentChange change) {
        Credit credit = change.getDocument().toObject(Credit.class);
        if (credit.getAmount() > 0.00){
            creditIds.add(change.getDocument().getId());
            credits.add(credit);
            topPostsAdapter.setBestCingles(credits);
            topPostsAdapter.getItemCount();
            topPostsAdapter.notifyItemInserted(credits.size());
        }

    }

    private void onDocumentModified(DocumentChange change) {
        Credit credit = change.getDocument().toObject(Credit.class);
        if (change.getOldIndex() == change.getNewIndex()) {
            // Item changed but remained in same position
            creditIds.add(change.getDocument().getId());
            credits.set(change.getNewIndex(), credit);
            topPostsAdapter.notifyItemChanged(change.getOldIndex());

        } else {
            // Item changed and changed position
            credits.remove(change.getOldIndex());
            credits.add(change.getNewIndex(), credit);
            topPostsAdapter.notifyItemMoved(change.getOldIndex(), change.getNewIndex());
        }

    }

    private void onDocumentRemoved(DocumentChange change) {
        String credit_key = change.getDocument().getId();
        int credit_index = creditIds.indexOf(credit_key);
        if (credit_index > -1){
            //remove data from the list
            creditIds.remove(change.getDocument().getId());
            topPostsAdapter.removeAt(change.getOldIndex());
            topPostsAdapter.notifyItemRemoved(change.getOldIndex());
            topPostsAdapter.getItemCount();
        }else {
            Log.v(TAG, "onDocumentRemoved:" + credit_key);
        }


    }

    private void onError(FirebaseFirestoreException e) {};

    private void onDataChanged() {}



    @Override
    public void onRefresh() {
        swipeRefreshLayout.setRefreshing(true);
        setNextBestCingles();
    }


    private void setNextBestCingles(){
        senseCreditReference.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(final QuerySnapshot creditsSnapshots, FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (creditsSnapshots.isEmpty()){
                    swipeRefreshLayout.setRefreshing(false);
                }else {
                    bestCinglesQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(final QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                            if (e != null) {
                                Log.w(TAG, "Listen error", e);
                                return;
                            }

                            if (!documentSnapshots.isEmpty()){
                                //get the last visible document(cingle)
                                lastVisible = documentSnapshots.getDocuments()
                                        .get(documentSnapshots.size() - 1);

                                //query starting from last retrived cingle
                                final Query nextBestCinglesQuery = senseCreditReference.orderBy("amount")
                                        .startAfter(lastVisible);
                                //retrive more cingles if present
                                nextBestCinglesQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
                                    @Override
                                    public void onEvent(final QuerySnapshot snapshots, FirebaseFirestoreException e) {
                                        if (e != null) {
                                            Log.w(TAG, "Listen error", e);
                                            return;
                                        }

                                        //retrieve cingles depending on the remaining size of the list
                                        if (!snapshots.isEmpty()){
                                            final long lastSize = snapshots.size();
                                            if (lastSize < TOTAL_ITEMS){
                                                nextBestCinglesQuery.limit(lastSize);
                                            }else {
                                                nextBestCinglesQuery.limit(TOTAL_ITEMS);
                                            }

                                            //make sure that the size of snapshot equals item count
                                            if (topPostsAdapter.getItemCount() == creditsSnapshots.size()){
                                                swipeRefreshLayout.setRefreshing(false);
                                            }else if (topPostsAdapter.getItemCount() < creditsSnapshots.size()){
                                                for (DocumentChange change : snapshots.getDocumentChanges()) {
                                                    switch (change.getType()) {
                                                        case ADDED:
                                                            onDocumentAdded(change);
                                                            break;

                                                    }
                                                    onDataChanged();
                                                }
                                                swipeRefreshLayout.setRefreshing(false);
                                            }else {
                                                swipeRefreshLayout.setRefreshing(false);
                                            }


                                        }


                                    }
                                });
                            }

                        }
                    });
                }

            }
        });

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }


}

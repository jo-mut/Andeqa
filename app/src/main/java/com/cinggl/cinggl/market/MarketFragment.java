package com.cinggl.cinggl.market;


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
import com.cinggl.cinggl.adapters.SellingAdapter;
import com.cinggl.cinggl.models.PostSale;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
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

import static com.cinggl.cinggl.R.id.swipeToRefreshLayout;

/**
 * A simple {@link Fragment} subclass.
 */
public class MarketFragment extends Fragment implements
        SwipeRefreshLayout.OnRefreshListener{
    @Bind(R.id.ifairCinglesRecyclerView)RecyclerView mIfairCingleRecyclerView;
    @Bind(swipeToRefreshLayout)SwipeRefreshLayout swipeRefreshLayout;

    private static final String TAG = "SingleOutFragment";
    private int currentPage = 0;
    private LinearLayoutManager layoutManager;
    private static final String KEY_LAYOUT_POSITION = "layout pooition";
    private static final String EXTRA_POST_KEY = "post key";
    private static final String EXTRA_USER_UID = "uid";
    private static final int MAX_WIDTH = 200;
    private static final int MAX_HEIGHT = 200;
    private Parcelable recyclerViewState;
    //firestore
    private CollectionReference ifairReference;
    private com.google.firebase.firestore.Query sellingQuery;
    //adapters
    private FirestoreRecyclerAdapter firestoreRecyclerAdapter;
    private SellingAdapter sellingAdapter;
    private FirebaseAuth firebaseAuth;
    private List<PostSale> postSales = new ArrayList<>();
    private List<String> cingleSaleIds = new ArrayList<>();
    private int TOTAL_ITEMS = 4;
    private DocumentSnapshot lastVisible;


    public MarketFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_ifair_cingles, container, false);
        ButterKnife.bind(this, view);
        swipeRefreshLayout.setOnRefreshListener(this);
        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseFirestore.setLoggingEnabled(true);

        if (firebaseAuth.getCurrentUser()!= null){
            //firestore
            ifairReference = FirebaseFirestore.getInstance().collection(Constants.IFAIR);
            sellingQuery = ifairReference.orderBy("timeStamp").limit(TOTAL_ITEMS);

        }

        return  view;
    }




    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setTheFirstBacthSellingCingles();
        if (savedInstanceState != null){
            recyclerViewState = savedInstanceState.getParcelable(KEY_LAYOUT_POSITION);
            Log.d("Saved Instance", "Instance is not null");
        }else {
            Log.d("Saved Instance", "Instance is completely null");
        }
    }

    private void setTheFirstBacthSellingCingles(){
        sellingQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

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
        sellingAdapter = new SellingAdapter(getContext());
        mIfairCingleRecyclerView.setAdapter(sellingAdapter);
        mIfairCingleRecyclerView.setHasFixedSize(false);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setAutoMeasureEnabled(true);
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        mIfairCingleRecyclerView.setLayoutManager(layoutManager);

    }

    private void onDocumentAdded(DocumentChange change) {
        PostSale postSale = change.getDocument().toObject(PostSale.class);
        cingleSaleIds.add(change.getDocument().getId());
        postSales.add(postSale);
        sellingAdapter.setSellingCingles(postSales);
        sellingAdapter.getItemCount();
        sellingAdapter.notifyItemInserted(postSales.size());

    }

    private void onDocumentModified(DocumentChange change) {
        PostSale postSale = change.getDocument().toObject(PostSale.class);
        if (change.getOldIndex() == change.getNewIndex()) {
            // Item changed but remained in same position
            cingleSaleIds.add(change.getDocument().getId());
            postSales.set(change.getNewIndex(), postSale);
            sellingAdapter.notifyItemChanged(change.getOldIndex());

        } else {
            // Item changed and changed position
            postSales.remove(change.getOldIndex());
            postSales.add(change.getNewIndex(), postSale);
            sellingAdapter.notifyItemMoved(change.getOldIndex(), change.getNewIndex());
        }
    }

    private void onDocumentRemoved(DocumentChange change) {
        String cingleSale_key = change.getDocument().getId();
        int cingle_index = cingleSaleIds.indexOf(cingleSale_key);
        if (cingle_index > -1){
            //remove data from the list
            cingleSaleIds.remove(change.getDocument().getId());
            sellingAdapter.removeAt(change.getOldIndex());
            sellingAdapter.notifyItemRemoved(change.getOldIndex());
            sellingAdapter.getItemCount();
        }else {
            Log.v(TAG, "onDocumentRemoved:" + cingleSale_key);
        }

    }

    private void onError(FirebaseFirestoreException e) {};

    private void onDataChanged() {}



    @Override
    public void onRefresh() {
        swipeRefreshLayout.setRefreshing(true);
        setNextProfileCingles();
    }


    private void setNextProfileCingles(){
        ifairReference.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(final QuerySnapshot sellingSnapshots, FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (sellingSnapshots.isEmpty()){
                    swipeRefreshLayout.setRefreshing(false);
                }else {
                    sellingQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(final QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                            if (e != null) {
                                Log.w(TAG, "Listen error", e);
                                return;
                            }

                            //get the last visible document(cingle)
                            lastVisible = documentSnapshots.getDocuments()
                                    .get(documentSnapshots.size() - 1);

                            //query starting from last retrived cingle
                            Query nextBestCinglesQuery = ifairReference.orderBy("randomNumber")
                                    .startAfter(lastVisible).limit(TOTAL_ITEMS);
                            //retrive more cingles if present
                            nextBestCinglesQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
                                @Override
                                public void onEvent(final QuerySnapshot snapshots, FirebaseFirestoreException e) {
                                    if (e != null) {
                                        Log.w(TAG, "Listen error", e);
                                        return;
                                    }

                                    if (sellingAdapter.getItemCount() == sellingSnapshots.size()){
                                        swipeRefreshLayout.setRefreshing(false);
                                    }else {
                                        for (DocumentChange change : snapshots.getDocumentChanges()) {
                                            switch (change.getType()) {
                                                case ADDED:
                                                    onDocumentAdded(change);
                                                    break;

                                            }
                                            onDataChanged();
                                        }
                                        swipeRefreshLayout.setRefreshing(false);
                                    }


                                }
                            });

                        }
                    });
                }


            }
        });

    }



    @Override
    public void onSaveInstanceState(Bundle outState) {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onStart() {
        super.onStart();
    }
}

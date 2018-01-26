package com.cinggl.cinggl.market;


import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.cinggl.cinggl.Constants;
import com.cinggl.cinggl.R;
import com.cinggl.cinggl.models.PostSale;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;


/**
 * A simple {@link Fragment} subclass.
 */
public class MarketFragment extends Fragment{
    @Bind(R.id.marketRecyclerView)RecyclerView mIfairCingleRecyclerView;
    @Bind(R.id.placeHolderRelativeLayout)RelativeLayout mPlaceHolderRelativeLayout;

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
        View view = inflater.inflate(R.layout.fragment_market, container, false);
        ButterKnife.bind(this, view);
        firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser()!= null){
            //firestore
            ifairReference = FirebaseFirestore.getInstance().collection(Constants.SELLING);
            sellingQuery = ifairReference.orderBy("randomNumber").limit(TOTAL_ITEMS);

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


                if (!documentSnapshots.isEmpty()){
                    // RecyclerView
                    sellingAdapter = new SellingAdapter(sellingQuery, getContext());
                    sellingAdapter.startListening();
                    mIfairCingleRecyclerView.setAdapter(sellingAdapter);
                    mIfairCingleRecyclerView.setHasFixedSize(false);
                    LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
                    mIfairCingleRecyclerView.setLayoutManager(layoutManager);
                }else {
                    mPlaceHolderRelativeLayout.setVisibility(View.VISIBLE);
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

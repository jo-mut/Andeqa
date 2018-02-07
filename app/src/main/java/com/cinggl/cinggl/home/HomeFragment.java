package com.cinggl.cinggl.home;


import android.os.Bundle;
import android.os.Parcelable;
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
import com.cinggl.cinggl.Trace;
import com.cinggl.cinggl.models.Post;
import com.cinggl.cinggl.models.TraceData;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment implements Trace.TracingListener{
    @Bind(R.id.singleOutRecyclerView)RecyclerView singleOutRecyclerView;
    @Bind(R.id.placeHolderRelativeLayout)RelativeLayout mPlaceHolderRelativeLayout;


    private static final String TAG = HomeFragment.class.getSimpleName();
    private static final String KEY_LAYOUT_POSITION = "layout position";
    private Parcelable recyclerViewState;
    //firestore reference
    private CollectionReference cinglesReference;
    private Query randomPostsQuery;
    private DocumentSnapshot lastVisible;

    //firebase auth
    private FirebaseAuth firebaseAuth;
    //adapters
    private MainPostsAdapter mainPostsAdapter;
    private LinearLayoutManager layoutManager;
    private int TOTAL_ITEMS = 50;
    private Trace trace;




    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        ButterKnife.bind(this, view);

        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null){
            //firestore
            cinglesReference = FirebaseFirestore.getInstance().collection(Constants.POSTS);
            randomPostsQuery = cinglesReference.orderBy("timeStamp", Query.Direction.DESCENDING)
                    .limit(TOTAL_ITEMS);

            refresh();

        }

//        trace = new Trace.Builder()
//                .setRecyclerView(singleOutRecyclerView)
//                .setMinimumViewingTimeThreshold(2000)
//                .setMinimumVisibleHeightThreshold(60)
//                .setTracingListener(this)
//                .setDataDumpInterval(1000)
//                .dumpDataAfterInterval(true)
//                .build();


        return view;
    }

    private void refresh(){
        randomPostsQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }


                if (!documentSnapshots.isEmpty()){
                    // Get the last visible document
                    lastVisible = documentSnapshots.getDocuments()
                            .get(documentSnapshots.size() -1);
                    mainPostsAdapter = new MainPostsAdapter(randomPostsQuery, getContext());
                    mainPostsAdapter.startListening();
                    singleOutRecyclerView.setAdapter(mainPostsAdapter);
                    singleOutRecyclerView.setHasFixedSize(false);
                    layoutManager = new LinearLayoutManager(getContext());
                    singleOutRecyclerView.setLayoutManager(layoutManager);
                }else {
                    mPlaceHolderRelativeLayout.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void nextPosts(){
//        mSwipeRefreshLayout.setRefreshing(true);
        // Construct a new query starting at this document,
        // get the next 30 documents.
       final Query next = cinglesReference.orderBy("timeStamp", Query.Direction.DESCENDING)
                .startAfter(lastVisible).limit(TOTAL_ITEMS);

        next.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (!documentSnapshots.isEmpty()){
                    mainPostsAdapter = new MainPostsAdapter(next, getContext());
                    mainPostsAdapter.startListening();
                    singleOutRecyclerView.setAdapter(mainPostsAdapter);
                    singleOutRecyclerView.setHasFixedSize(false);
                    layoutManager = new LinearLayoutManager(getContext());
                    singleOutRecyclerView.setLayoutManager(layoutManager);
//                    mSwipeRefreshLayout.setRefreshing(false);
                }
            }
        });

    }


    @Override
    public void traceDataDump(ArrayList<TraceData> data) {
        if(data != null) {
            // Do something with the data.
            for(int i = 0 ; i < data.size(); ++i)
                Log.i("Data dump", data.get(i).getViewId());
        }

    }

//    @Override
//    public void onStop() {
//        super.onStop();
//    }
//
//    @Override
//    public void onResume() {
//        super.onResume();
//        trace.getTraceData(true);
//
//    }
//
//    @Override
//    public void onStart() {
//        super.onStart();
//    }
//
//    @Override
//    public void onPause() {
//        super.onPause();
//        trace.startTracing();
//    }
}

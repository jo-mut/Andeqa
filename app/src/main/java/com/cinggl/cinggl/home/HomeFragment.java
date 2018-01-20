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
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

import static android.R.attr.data;

/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment implements Trace.TracingListener {
    @Bind(R.id.singleOutRecyclerView)RecyclerView singleOutRecyclerView;

    private static final String TAG = HomeFragment.class.getSimpleName();
    private static final String KEY_LAYOUT_POSITION = "layout position";
    private Parcelable recyclerViewState;
    //firestore reference
    private CollectionReference cinglesReference;
    private Query randomPostsQuery;
    //firebase auth
    private FirebaseAuth firebaseAuth;
    //adapters
    private MainPostsAdapter mainPostsAdapter;
    private LinearLayoutManager layoutManager;
    private int TOTAL_ITEMS = 4;
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
            randomPostsQuery = cinglesReference;

            latestPosts();

        }

        trace = new Trace.Builder()
                .setRecyclerView(singleOutRecyclerView)
                .setMinimumViewingTimeThreshold(2000)
                .setMinimumVisibleHeightThreshold(60)
                .setTracingListener(this)
                .setDataDumpInterval(1000)
                .dumpDataAfterInterval(true)
                .build();


        return view;
    }

    private void latestPosts(){
        randomPostsQuery.orderBy("timeStamp", Query.Direction.DESCENDING)
                .limit(50).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }


                if (!documentSnapshots.isEmpty()){
                    mainPostsAdapter = new MainPostsAdapter(randomPostsQuery, getContext());
                    mainPostsAdapter.startListening();
                    singleOutRecyclerView.setAdapter(mainPostsAdapter);
                    singleOutRecyclerView.setHasFixedSize(false);
                    layoutManager = new LinearLayoutManager(getContext());
                    singleOutRecyclerView.setLayoutManager(layoutManager);
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

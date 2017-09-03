package com.cinggl.cinggl.home;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.cinggl.cinggl.Constants;
import com.cinggl.cinggl.R;
import com.cinggl.cinggl.models.Cingle;
import com.cinggl.cinggl.models.Ifair;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class IfairCinglesFragment extends Fragment {
//    @Bind(R.id.tradingRecyclerView)RecyclerView mTradingRecyclerView;
//
//    private ChildEventListener mChildEventListener;
//    private DatabaseReference likesRef;
//    private static final String TAG = "CingleOutFragment";
//    private LinearLayoutManager layoutManager;
//    private List<Ifair> ifairCingles = new ArrayList<>();
//    private List<String> ifairCinglesIds = new ArrayList<>();
//    private int currentPage = 0;
//    private static final int TOTAL_ITEM_EACH_LOAD = 10;
//    private static final String KEY_LAYOUT_POSITION = "layout pooition";
//    private int cingleOutRecyclerViewPosition = 0;
//    private DatabaseReference tradingCinglesReference;
//    private DatabaseReference cinglesReference;
//    private DatabaseReference usersRef;
//    private DatabaseReference commentsRef;
//    private DatabaseReference cingleWalletReference;
//    private static final String EXTRA_POST_KEY = "post key";
//    private Query tradingCinglesQuery;
//

    public IfairCinglesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_ifair_cingles, container, false);
        ButterKnife.bind(this, view);

//        //intialize database references;
//        tradingCinglesReference = FirebaseDatabase.getInstance().getReference(Constants.IFAIR);
//        cinglesReference = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CINGLES);
//        commentsRef = FirebaseDatabase.getInstance().getReference(Constants.COMMENTS);
//        likesRef = FirebaseDatabase.getInstance().getReference(Constants.LIKES);
//        usersRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_USERS);
//        cingleWalletReference = FirebaseDatabase.getInstance().getReference(Constants.CINGLE_WALLET);
//
//        tradingCinglesReference.keepSynced(true);
//        usersRef.keepSynced(true);
//        likesRef.keepSynced(true);
//        commentsRef.keepSynced(true);
//        cinglesReference.keepSynced(true);
//        cingleWalletReference.keepSynced(true);
//
//
//        initializeViewsAdapter();
//        mTradingRecyclerView.addOnScrollListener(mOnScollListener);
//        setIfairCingles(currentPage);

        return view;
    }

//    @Override
//    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
//        super.onActivityCreated(savedInstanceState);
//
//        if (savedInstanceState != null){
//            //restore saved layout manager type
//            cingleOutRecyclerViewPosition = (int) savedInstanceState
//                    .getSerializable(KEY_LAYOUT_POSITION);
//            mTradingRecyclerView.scrollToPosition(cingleOutRecyclerViewPosition);
//        }
//    }
//
//    @Override
//    public void onStart() {
//        super.onStart();
//
//    }
//
//    @Override
//    public void onStop() {
//        super.onStop();
//    }
//
//
//    private void initializeViewsAdapter(){
//        layoutManager =  new LinearLayoutManager(getContext());
//        mTradingRecyclerView.setLayoutManager(layoutManager);
//        layoutManager.setAutoMeasureEnabled(true);
//        mTradingRecyclerView.setHasFixedSize(true);
//        ifairCinglesFragmentAdapter = new IfairCinglesFragmentAdapter(getContext());
//        mTradingRecyclerView.setAdapter(ifairCinglesFragmentAdapter);
//        ifairCinglesFragmentAdapter.notifyDataSetChanged();
//    }
//
//    private RecyclerView.OnScrollListener mOnScollListener = new RecyclerView.OnScrollListener(){
//        private int lastVisibileItem;
//
//        @Override
//        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
//            super.onScrolled(recyclerView, dx, dy);
//            lastVisibileItem = layoutManager.findLastVisibleItemPosition();
//        }
//
//        @Override
//        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
//            super.onScrollStateChanged(recyclerView, newState);
//            if (newState == RecyclerView.SCROLL_STATE_IDLE
//                    && lastVisibileItem + 1 == ifairCinglesFragmentAdapter.getItemCount()){
////                progressBar.setVisibility(View.VISIBLE);
//                setIfairCingles(currentPage + 1);
//            }
//        }
//    };
//
//    public void setIfairCingles(int start){
////        progressBar.setVisibility(View.VISIBLE);
//        tradingCinglesQuery = tradingCinglesReference.child("Cingle Selling")
//                .orderByChild("pushId");
//        tradingCinglesQuery.keepSynced(true);
//
//        ChildEventListener childEventListener = new ChildEventListener() {
//            @Override
//            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
//                Log.d("Snapshot", dataSnapshot.toString());
////                progressBar.setVisibility(View.GONE);
//
//                Ifair ifair = dataSnapshot.getValue(Ifair.class);
//                ifairCinglesIds.add(dataSnapshot.getKey());
//                ifairCingles.add(ifair);
//
//                currentPage += 10;
//                ifairCinglesFragmentAdapter.setIfairCingles(ifairCingles);
//                ifairCinglesFragmentAdapter.notifyItemInserted(ifairCingles.size());
//                ifairCinglesFragmentAdapter.getItemCount();
//                Log.d("size of cingles list", ifairCinglesIds.size() + "");
//
//            }
//
//            @Override
//            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
//                Ifair ifair =  dataSnapshot.getValue(Ifair.class);
//
//                String cingle_key = dataSnapshot.getKey();
//
//                //exclude
//                int cingle_index = ifairCinglesIds.indexOf(cingle_key);
//                if (cingle_index > - 1){
//
//                    //replace with the new cingle
//                    ifairCingles.set(cingle_index, ifair);
//                    ifairCinglesFragmentAdapter.notifyItemChanged(cingle_index);
//                    ifairCinglesFragmentAdapter.notifyDataSetChanged();
//                    ifairCinglesFragmentAdapter.getItemCount();
//                }else {
//                    Log.w(TAG, "onChildChanged:unknown_child" + cingle_key);
//                }
//
//
//            }
//
//            @Override
//            public void onChildRemoved(DataSnapshot dataSnapshot) {
//                Log.d(TAG, "onChiledRemoved:" + dataSnapshot.getKey());
//
//                //a cingle has changed. use the key to determine if the cingle
//                // is being displayed and
//                //so remove it.
//                String cingle_key = dataSnapshot.getKey();
//                //exclude
//                int cingle_index = ifairCinglesIds.indexOf(cingle_key);
//                if (cingle_index > - 1){
//                    //remove data from the list
//                    ifairCinglesIds.remove(cingle_index);
//                    ifairCingles.remove(cingle_key);
//                    ifairCinglesFragmentAdapter.removeAt(cingle_index);
//                    ifairCinglesFragmentAdapter.notifyItemRemoved(cingle_index);
//
//                }else {
//                    Log.w(TAG, "onChildRemoved:unknown_child:" + cingle_key);
//                }
//
//            }
//
//            @Override
//            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
//                Cingle cingle = dataSnapshot.getValue(Cingle.class);
//                String cingle_key = dataSnapshot.getKey();
//
//                //...
//
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//                Log.w(TAG, "load Cingles : onCancelled", databaseError.toException());
//                Toast.makeText(getContext(), "Failed to load comments.", Toast.LENGTH_SHORT).show();
//
//            }
//        };
//        tradingCinglesQuery.addChildEventListener(childEventListener);
//        mChildEventListener = childEventListener;
//    }
//
//    public void cleanUpListener(){
//        if (mChildEventListener != null){
//            tradingCinglesQuery.removeEventListener(mChildEventListener);
//        }
//    }
//
//    @Override
//    public void onSaveInstanceState(Bundle outState) {
//        //save currently selected layout manager;
//        int recyclerViewScrollPosition =  getRecyclerViewScrollPosition();
//        Log.d(TAG, "Recycler view scroll position:" + recyclerViewScrollPosition);
//        outState.putSerializable(KEY_LAYOUT_POSITION, recyclerViewScrollPosition);
//        super.onSaveInstanceState(outState);
//
//    }
//
//    private int getRecyclerViewScrollPosition() {
//        int scrollPosition = 0;
//        // TODO: Is null check necessary?
//        if (mTradingRecyclerView != null && mTradingRecyclerView.getLayoutManager() != null) {
//            scrollPosition = ((LinearLayoutManager) mTradingRecyclerView.getLayoutManager())
//                    .findFirstCompletelyVisibleItemPosition();
//        }
//        return scrollPosition;
//    }
//
//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        cleanUpListener();
//    }

}

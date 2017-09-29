package com.cinggl.cinggl.ifair;


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
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.cinggl.cinggl.Constants;
import com.cinggl.cinggl.R;
import com.cinggl.cinggl.adapters.IfairCingleAdapter;
import com.cinggl.cinggl.models.CingleSale;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class IfairCinglesFragment extends Fragment {
    @Bind(R.id.ifairCinglesRecyclerView)RecyclerView mIfairCingleRecyclerView;
    @Bind(R.id.ifairCinglesProgressbar)ProgressBar progressBar;

    private DatabaseReference cinglesReference;
    private DatabaseReference ifairReference;
    private Query ifairCinglesQuery;
    private ChildEventListener mChildEventListener;
    private DatabaseReference usernameRef;
    private FirebaseAuth firebaseAuth;
    private IfairCingleAdapter ifairCingleAdapter;
    private DatabaseReference senseCreditsReference;
    private static final String TAG = "CingleOutFragment";

    private List<CingleSale> ifairCingles = new ArrayList<>();
    private List<String> ifairCinglesIds = new ArrayList<>();

    private int currentPage = 0;
    private static final int TOTAL_ITEM_EACH_LOAD = 10;
    private LinearLayoutManager layoutManager;
    private static final String KEY_LAYOUT_POSITION = "layout pooition";
    private Parcelable recyclerViewState;


    public IfairCinglesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_ifair_cingles, container, false);
        ButterKnife.bind(this, view);

        firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser()!= null){
            senseCreditsReference = FirebaseDatabase.getInstance().getReference("Sense points");
            usernameRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_USERS);
            cinglesReference = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CINGLES);
            ifairReference = FirebaseDatabase.getInstance().getReference(Constants.IFAIR);

            usernameRef.keepSynced(true);

            setCinglesOnIfair(currentPage);
        }

        return  view;
    }

    private void initializeViewsAdapter(){
        layoutManager =  new LinearLayoutManager(getContext());
        mIfairCingleRecyclerView.setLayoutManager(layoutManager);
        mIfairCingleRecyclerView.setHasFixedSize(true);
        ifairCingleAdapter = new IfairCingleAdapter(getContext());
        mIfairCingleRecyclerView.setAdapter(ifairCingleAdapter);
        ifairCingleAdapter.notifyDataSetChanged();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initializeViewsAdapter();

        if (savedInstanceState != null){
            recyclerViewState = savedInstanceState.getParcelable(KEY_LAYOUT_POSITION);
        }
    }

    public void setCinglesOnIfair(int start){
//        progressBar.setVisibility(View.VISIBLE);
        ifairCinglesQuery = ifairReference.child("Cingle Selling").orderByChild("randomNumber")
                .startAt(start).endAt(start + TOTAL_ITEM_EACH_LOAD);
        ifairCinglesQuery.keepSynced(true);

        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.exists()){
                    Log.d("Snapshot", dataSnapshot.toString());
                    progressBar.setVisibility(View.GONE);
                    CingleSale cingleSale = dataSnapshot.getValue(CingleSale.class);
                    ifairCinglesIds.add(dataSnapshot.getKey());
                    ifairCingles.add(cingleSale);

                    currentPage += 10;
                    ifairCingleAdapter.setIfairCingles(ifairCingles);
                    ifairCingleAdapter.notifyItemInserted(ifairCingles.size());
                    ifairCingleAdapter.getItemCount();
                    Log.d("size of cingles list", ifairCingles.size() + "");
                }else {
                    progressBar.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                CingleSale cingleSale =  dataSnapshot.getValue(CingleSale.class);

                String cingle_key = dataSnapshot.getKey();

                //exclude
                int cingle_index = ifairCinglesIds.indexOf(cingle_key);
                if (cingle_index > - 1){

                    //replace with the new cingle
                    ifairCingles.set(cingle_index, cingleSale);
                    ifairCingleAdapter.notifyItemChanged(cingle_index);
                    ifairCingleAdapter.notifyDataSetChanged();
                    ifairCingleAdapter.getItemCount();
                }else {
                    Log.w(TAG, "onChildChanged:unknown_child" + cingle_key);
                }


            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onChiledRemoved:" + dataSnapshot.getKey());

                //a cingle has changed. use the key to determine if the cingle
                // is being displayed and
                //so remove it.
                String cingle_key = dataSnapshot.getKey();
                //exclude
                int cingle_index = ifairCinglesIds.indexOf(cingle_key);
                if (cingle_index > - 1){
                    //remove data from the list
                    ifairCinglesIds.remove(cingle_index);
                    ifairCingles.remove(cingle_key);
                    ifairCingleAdapter.removeAt(cingle_index);
                    ifairCingleAdapter.notifyItemRemoved(cingle_index);

                }else {
                    Log.w(TAG, "onChildRemoved:unknown_child:" + cingle_key);
                }

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                CingleSale cingleSale = dataSnapshot.getValue(CingleSale.class);
                String cingle_key = dataSnapshot.getKey();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "load Cingles : onCancelled", databaseError.toException());
                Toast.makeText(getContext(), "Failed to load comments.", Toast.LENGTH_SHORT).show();

            }
        };
        ifairCinglesQuery.addChildEventListener(childEventListener);
        mChildEventListener = childEventListener;
    }

    public void cleanUpListener(){
        if (mChildEventListener != null){
            ifairCinglesQuery.removeEventListener(mChildEventListener);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(KEY_LAYOUT_POSITION, layoutManager.onSaveInstanceState());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cleanUpListener();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (recyclerViewState != null){
            layoutManager.onRestoreInstanceState(recyclerViewState);
        }
    }



}

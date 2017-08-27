package com.cinggl.cinggl.lacing;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cinggl.cinggl.Constants;
import com.cinggl.cinggl.R;
import com.cinggl.cinggl.ifair.TradeCinglesViewHolder;
import com.cinggl.cinggl.models.Ifair;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
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
public class LacingOverallFragment extends Fragment {
    //bind views
    @Bind(R.id.overallLacedCinglesRecyclerView)RecyclerView mOverallLacedCinglesRecyclerView;

    private DatabaseReference lacingOverallReferences;
    private DatabaseReference cinglesReference;
    private FirebaseRecyclerAdapter firebaseRecyclerAdapter;
    private Query overallQuery;
    private int TOTAL_ITEM_EACH_LOAD = 10;
    private int currentPage = 0;
    private List<Ifair> lacedCingles = new ArrayList<>();
    private List<String> lacedCinglesIds = new ArrayList<>();
    private LinearLayoutManager layoutManager;

    private OverallLacedAdapter overallLacedAdapter;



    public LacingOverallFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_lacing_overall, container, false);
        ButterKnife.bind(this, view);

        //intialize database references;
        lacingOverallReferences = FirebaseDatabase.getInstance().getReference(Constants.IFAIR);
        overallQuery = lacingOverallReferences.child("Cingle Lacing").orderByChild("pushId");
        lacingOverallReferences.keepSynced(true);

        //retrieve all the laced cingles
//        retrieveAllLacedCingles(currentPage);
//        load data on the recycler view
//        initializeOverallLacedCinglesRecyclerView();
        setUpLacedCingles();
        return view;

    }

    private void initializeOverallLacedCinglesRecyclerView(){
        layoutManager =  new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        mOverallLacedCinglesRecyclerView.setLayoutManager(layoutManager);
        mOverallLacedCinglesRecyclerView.setHasFixedSize(true);
        overallLacedAdapter = new OverallLacedAdapter(getContext());
        mOverallLacedCinglesRecyclerView.setAdapter(overallLacedAdapter);
    }


    private void retrieveAllLacedCingles(int start){
//        overallQuery = lacingOverallReferences.child("Cingle Lacing")
//                .orderByChild("pushId").startAt(start)
//                .endAt(start + TOTAL_ITEM_EACH_LOAD);
        overallQuery.startAt(start).endAt(start + TOTAL_ITEM_EACH_LOAD)
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("Snapshot of laced", dataSnapshot.toString());

                Ifair ifair = dataSnapshot.getValue(Ifair.class);
                lacedCinglesIds.add(dataSnapshot.getKey());
                lacedCingles.add(ifair);
                currentPage += 10;
                overallLacedAdapter.setLacedCingles(lacedCingles);
                overallLacedAdapter.notifyItemInserted(lacedCingles.size());
                overallLacedAdapter.getItemCount();
                Log.d("size of laced cingles", lacedCingles.size() + "");

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void setUpLacedCingles(){
        firebaseRecyclerAdapter =  new FirebaseRecyclerAdapter<Ifair, TradeCinglesViewHolder>
                (Ifair.class, R.layout.cingle_lacing_layout, TradeCinglesViewHolder.class, overallQuery) {
            @Override
            protected void populateViewHolder(TradeCinglesViewHolder viewHolder, Ifair model, int position) {
                viewHolder.bindTradedCingles(model);

            }

        };

        layoutManager =  new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        mOverallLacedCinglesRecyclerView.setLayoutManager(layoutManager);
        mOverallLacedCinglesRecyclerView.setHasFixedSize(true);
        mOverallLacedCinglesRecyclerView.setAdapter(firebaseRecyclerAdapter);
    }

}

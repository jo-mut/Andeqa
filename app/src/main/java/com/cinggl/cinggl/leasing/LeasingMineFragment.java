package com.cinggl.cinggl.leasing;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cinggl.cinggl.Constants;
import com.cinggl.cinggl.R;
import com.cinggl.cinggl.ifair.TradeCinglesViewHolder;
import com.cinggl.cinggl.lacing.OverallLacedAdapter;
import com.cinggl.cinggl.models.Ifair;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
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
public class LeasingMineFragment extends Fragment {

    //bind views
    @Bind(R.id.mineLeasingCinglesRecyclerView)RecyclerView mMineLeasingCinglesRecyclerView;

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




    public LeasingMineFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_leasing_mine, container, false);

        ButterKnife.bind(this, view);

        //intialize database references;
        lacingOverallReferences = FirebaseDatabase.getInstance().getReference(Constants.IFAIR);
        lacingOverallReferences.keepSynced(true);

        setUpLacedCingles();
        return view;
    }

    public void setUpLacedCingles(){
        overallQuery = lacingOverallReferences.child("Cingle Leasing").orderByChild("creator");
        firebaseRecyclerAdapter =  new FirebaseRecyclerAdapter<Ifair, TradeCinglesViewHolder>
                (Ifair.class, R.layout.cingle_leasing_layout, TradeCinglesViewHolder.class, overallQuery) {
            @Override
            protected void populateViewHolder(TradeCinglesViewHolder viewHolder, Ifair model, int position) {
                viewHolder.bindTradedCingles(model);

            }

        };

        layoutManager =  new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        mMineLeasingCinglesRecyclerView.setLayoutManager(layoutManager);
        mMineLeasingCinglesRecyclerView.setHasFixedSize(true);
        mMineLeasingCinglesRecyclerView.setAdapter(firebaseRecyclerAdapter);
    }

}

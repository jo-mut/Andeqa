package com.cinggl.cinggl.home;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cinggl.cinggl.Constants;
import com.cinggl.cinggl.R;
import com.cinggl.cinggl.adapters.BestCinglesViewHolder;
import com.cinggl.cinggl.models.Cingle;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class BestCinglesFragment extends Fragment {
    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private FirebaseRecyclerAdapter firebaseRecyclerAdapter;
    private Query bestQuery;

    @Bind(R.id.bestCinglesRecyclerView)RecyclerView mBestCinglesRecyclerView;


    public BestCinglesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CINGLES);
        bestQuery = databaseReference.orderByChild("sensepoint").limitToFirst(10);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_best_cingles, container, false);
        ButterKnife.bind(this, view);

        setUpBestMomentCingles();

        return view;
    }

    private void setUpBestMomentCingles(){
        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Cingle, BestCinglesViewHolder>
                (Cingle.class, R.layout.best_cingles_list, BestCinglesViewHolder.class, bestQuery) {
            @Override
            protected void populateViewHolder(final BestCinglesViewHolder viewHolder, final Cingle model, int position) {
                viewHolder.bindBestCingle(model);
                DatabaseReference cingleRef = getRef(position);
                final String postKey = cingleRef.getKey();

                databaseReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        };

        mBestCinglesRecyclerView.setAdapter(firebaseRecyclerAdapter);
        mBestCinglesRecyclerView.setHasFixedSize(false);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        layoutManager.onSaveInstanceState();
        layoutManager.setAutoMeasureEnabled(true);
        mBestCinglesRecyclerView.setLayoutManager(layoutManager);

    }



}

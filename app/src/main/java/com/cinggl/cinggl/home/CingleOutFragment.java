package com.cinggl.cinggl.home;


import android.os.Bundle;
import android.support.constraint.solver.Cache;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ScrollView;

import com.cinggl.cinggl.Constants;
import com.cinggl.cinggl.R;
import com.cinggl.cinggl.adapters.FirebaseCingleOutViewHolder;
import com.cinggl.cinggl.models.Cingle;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class CingleOutFragment extends Fragment {
    private DatabaseReference databaseReference;
    private FirebaseRecyclerAdapter firebaseRecyclerAdapter;
    @Bind(R.id.cingleOutRecyclerView)RecyclerView cingleOutRecyclerView;
    @Bind(R.id.scrollView) ScrollView cingleOutScrollView;

    public CingleOutFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_cingle_out, container, false);
        ButterKnife.bind(this, view);
        databaseReference = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_PUBLIC_CINGLES);
        setUpFirebaseAdapter();


        return view;
    }

    private void setUpFirebaseAdapter(){
        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Cingle, FirebaseCingleOutViewHolder>
                (Cingle.class, R.layout.cingle_out_list, FirebaseCingleOutViewHolder.class, databaseReference) {
            @Override
            protected void populateViewHolder(FirebaseCingleOutViewHolder viewHolder, Cingle model, int position) {
                viewHolder.bindCingle(model);
            }
        };
        cingleOutRecyclerView.setAdapter(firebaseRecyclerAdapter);
        cingleOutRecyclerView.setHasFixedSize(false);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setAutoMeasureEnabled(true);
        cingleOutRecyclerView.setLayoutManager(layoutManager);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        firebaseRecyclerAdapter.cleanup();
    }

}

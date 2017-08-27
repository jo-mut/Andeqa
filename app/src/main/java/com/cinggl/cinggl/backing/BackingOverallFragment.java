package com.cinggl.cinggl.backing;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cinggl.cinggl.Constants;
import com.cinggl.cinggl.R;
import com.cinggl.cinggl.adapters.CingleOutAdapter;
import com.cinggl.cinggl.home.CingleSettingsDialog;
import com.cinggl.cinggl.ifair.SendCreditsDialogFragment;
import com.cinggl.cinggl.ifair.TradeCinglesViewHolder;
import com.cinggl.cinggl.lacing.OverallLacedAdapter;
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
public class BackingOverallFragment extends Fragment {
    //bind views
    @Bind(R.id.overallBackingCinglesRecyclerView)RecyclerView mOverallBackingCinglesRecyclerView;

    private DatabaseReference lacingOverallReferences;
    private DatabaseReference cinglesReference;
    private FirebaseRecyclerAdapter firebaseRecyclerAdapter;
    private static final String EXTRA_POST_KEY = "post key";
    private Query overallQuery;
    private int TOTAL_ITEM_EACH_LOAD = 10;
    private int currentPage = 0;
    private List<Ifair> backedCingles = new ArrayList<>();
    private List<String> backedCinglesIds = new ArrayList<>();
    private LinearLayoutManager layoutManager;
    private BackingOverallAdapter backingOverallAdapter;


    public BackingOverallFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_backing_overall, container, false);
        ButterKnife.bind(this, view);

        //intialize database references;
        lacingOverallReferences = FirebaseDatabase.getInstance().getReference(Constants.IFAIR);
        overallQuery = lacingOverallReferences.child("Cingle Backing").orderByChild("pushId");
        lacingOverallReferences.keepSynced(true);

        setUpLacedCingles();
//        initializeOverallBackingCinglesRecyclerView();
//        retrieveAllBackedCingles(currentPage);
        return view;

    }

    private void initializeOverallBackingCinglesRecyclerView(){
        layoutManager =  new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        mOverallBackingCinglesRecyclerView.setLayoutManager(layoutManager);
        mOverallBackingCinglesRecyclerView.setHasFixedSize(true);
        backingOverallAdapter = new BackingOverallAdapter(getContext());
        mOverallBackingCinglesRecyclerView.setAdapter(backingOverallAdapter);
    }


    private void retrieveAllBackedCingles(int start){

        overallQuery.startAt(start).endAt(start + TOTAL_ITEM_EACH_LOAD)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.d("Snapshot of laced", dataSnapshot.toString());

                        Ifair ifair = dataSnapshot.getValue(Ifair.class);

                        backedCinglesIds.add(dataSnapshot.getKey());
                        backedCingles.add(ifair);
                        currentPage += 10;
                        backingOverallAdapter.setBackedCingles(backedCingles);
                        backingOverallAdapter.notifyItemInserted(backedCingles.size());
                        backingOverallAdapter.getItemCount();
                        Log.d("size of backced cingles", backedCingles.size() + "");

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

    }

    public void setUpLacedCingles(){
        firebaseRecyclerAdapter =  new FirebaseRecyclerAdapter<Ifair, TradeCinglesViewHolder>
                (Ifair.class, R.layout.cingle_backing_layout, TradeCinglesViewHolder.class, overallQuery) {
            @Override
            protected void populateViewHolder(TradeCinglesViewHolder viewHolder, Ifair model, int position) {
                viewHolder.bindTradedCingles(model);
                final String postKey = getRef(position).getKey();

                viewHolder.mBackCingleButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Bundle bundle = new Bundle();
                        bundle.putString(BackingOverallFragment.EXTRA_POST_KEY, postKey);
                        FragmentManager fragmenManager = getChildFragmentManager();
                        SendCreditsDialogFragment sendCreditsDialogFragment = SendCreditsDialogFragment.newInstance("send credits");
                        sendCreditsDialogFragment.setArguments(bundle);
                        sendCreditsDialogFragment.show(fragmenManager, "cingle settings fragment");
                    }
                });

            }

        };

        layoutManager =  new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        mOverallBackingCinglesRecyclerView.setLayoutManager(layoutManager);
        mOverallBackingCinglesRecyclerView.setHasFixedSize(true);
        mOverallBackingCinglesRecyclerView.setAdapter(firebaseRecyclerAdapter);
    }

}

package com.cinggl.cinggl.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cinggl.cinggl.R;
import com.cinggl.cinggl.home.BestCinglesFragment;
import com.cinggl.cinggl.models.Cingle;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * Created by J.EL on 7/19/2017.
 */

public class BestCinglesAdapter extends RecyclerView.Adapter<BestCinglesViewHolder> {
    private Context mContext;
    private Query mQuery;
    private static final String TAG = BestCinglesFragment.class.getSimpleName();
    ArrayList<Cingle> bestCingles = new ArrayList<>();

    public BestCinglesAdapter(Context context, Query query) {
        mContext = context;
        mQuery = query;

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Cingle cingle = dataSnapshot.getValue(Cingle.class);
                bestCingles.add(cingle);

                notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return bestCingles.size();
    }

    @Override
    public BestCinglesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.best_cingles_list, parent, false );
        return new BestCinglesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(BestCinglesViewHolder holder, int position) {
        Cingle cingle = bestCingles.get(position);
        holder.bindBestCingle(cingle);
    }
}

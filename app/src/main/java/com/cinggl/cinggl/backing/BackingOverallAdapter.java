package com.cinggl.cinggl.backing;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cinggl.cinggl.R;
import com.cinggl.cinggl.adapters.BestCinglesViewHolder;
import com.cinggl.cinggl.ifair.TradeCinglesViewHolder;
import com.cinggl.cinggl.models.Cingle;
import com.cinggl.cinggl.models.Ifair;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by J.EL on 8/23/2017.
 */

public class BackingOverallAdapter extends RecyclerView.Adapter<TradeCinglesViewHolder> {
    private Context mContext;
    private List<Ifair> backedCingles = new ArrayList<>();

    public BackingOverallAdapter(Context mContext) {
        this.mContext = mContext;

    }

    public void setBackedCingles(List<Ifair> backedCingles) {
        this.backedCingles = backedCingles;
        notifyDataSetChanged();
    }

    @Override
    public TradeCinglesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cingle_backing_layout, parent, false );

        return new TradeCinglesViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return backedCingles.size();
    }

    @Override
    public void onBindViewHolder(TradeCinglesViewHolder holder, int position) {
        final Ifair ifair= backedCingles.get(position);
        holder.bindTradedCingles(ifair);
    }


}

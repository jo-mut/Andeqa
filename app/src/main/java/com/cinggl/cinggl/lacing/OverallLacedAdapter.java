package com.cinggl.cinggl.lacing;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.cinggl.cinggl.R;
import com.cinggl.cinggl.adapters.BestCinglesViewHolder;
import com.cinggl.cinggl.ifair.TradeCinglesViewHolder;
import com.cinggl.cinggl.models.Ifair;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by J.EL on 8/20/2017.
 */

public class OverallLacedAdapter extends RecyclerView.Adapter<TradeCinglesViewHolder> {
    private static final String TAG = OverallLacedAdapter.class.getSimpleName();
    private Context mContext;
    private static final String EXTRA_POST_KEY = "post key";
    private static final String EXTRA_USER_UID = "uid";
    private DatabaseReference commentReference;
    private DatabaseReference cinglesReferences;
    private DatabaseReference usersRef;
    private  DatabaseReference likesRef;
    private FirebaseAuth firebaseAuth;
    private FirebaseRecyclerAdapter firebaseRecyclerAdapter;
    private Query likesQuery;
    private Query likesQueryCount;
    private boolean processLikes = false;
    private static final double DEFAULT_PRICE = 1.5;
    private static final double GOLDEN_RATIO = 1.618;
    private List<Ifair> lacedCingles = new ArrayList<>();

    public OverallLacedAdapter(Context mContext) {
        this.mContext = mContext;

    }

    public void setLacedCingles(List<Ifair> lacedCingles) {
        this.lacedCingles = lacedCingles;
        notifyDataSetChanged();
    }

    public void removeAt(int position){
        lacedCingles.remove(lacedCingles.get(position));
    }


    public void animate(BestCinglesViewHolder viewHolder){
        final Animation animAnticipateOvershoot = AnimationUtils.loadAnimation(mContext, R.anim.bounce_interpolator);
        viewHolder.itemView.setAnimation(animAnticipateOvershoot);

        final Animation a = AnimationUtils.loadAnimation(mContext, R.anim.anticipate_overshoot_interpolator);
        viewHolder.itemView.setAnimation(a);
    }


    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public int getItemCount() {
        return lacedCingles.size();
    }


    @Override
    public TradeCinglesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cingle_lacing_layout, parent, false );

        return new TradeCinglesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final TradeCinglesViewHolder holder, final int position) {
        final Ifair ifair = lacedCingles.get(position);
        holder.bindTradedCingles(ifair);
    }

}

package com.cinggl.cinggl.adapters;

import android.content.Context;
import android.provider.ContactsContract;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.cinggl.cinggl.R;
import com.cinggl.cinggl.models.Cingle;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.List;

import static android.R.attr.data;


/**
 * Created by J.EL on 7/14/2017.
 */

public class CingleOutAdapter extends RecyclerView.Adapter<CingleOutViewHolder> {
    private Context mContext;
    private DatabaseReference mDatabaseReference;
    private ChildEventListener mChildEventListner;
    private static final String TAG =  CingleOutAdapter.class.getSimpleName();

    private List<String> CinglesIds = new ArrayList<>();
    private List<Cingle> cingles = new ArrayList<>();

//    public CingleOutAdapter(Context mContext, ArrayList<Cingle> cingles) {
//        this.mContext = mContext;
//        this.cingles = cingles;
//    }

    public CingleOutAdapter(DatabaseReference ref, final Context context) {
        mDatabaseReference = ref;
        mContext = context;


        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Cingle  cingle = dataSnapshot.getValue(Cingle.class);

                CinglesIds.add(dataSnapshot.getKey());
                cingles.add(cingle);
                notifyItemInserted(cingles.size() -  1);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        ref.addChildEventListener(childEventListener);

        mChildEventListner = childEventListener;

    }


    @Override
    public void onBindViewHolder(CingleOutViewHolder holder, int position) {
        Cingle cingle = cingles.get(position);
        holder.bindCingle(cingle);
        animate(holder);
    }

    @Override
    public CingleOutViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.cingle_out_list, parent, false);
        CingleOutViewHolder holder = new CingleOutViewHolder(view);
        return  holder;
    }

    @Override
    public int getItemCount() {
        return cingles.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView cingleOutRecyclerView) {
        super.onAttachedToRecyclerView(cingleOutRecyclerView);
    }

    public void animate(RecyclerView.ViewHolder viewHolder){
        final Animation animAnticipateOvershooot = AnimationUtils.loadAnimation(mContext, R.anim.bounce_interpolator);
        viewHolder.itemView.setAnimation(animAnticipateOvershooot);
    }

    public void insert(int position, Cingle cingle){
        cingles.add(position, cingle);
        notifyItemInserted(position);
    }

    public void remove(Cingle cingle){
        int position = cingles.indexOf(cingle);
        cingles.remove(position);
        notifyItemInserted(position);
    }

    public void add(Cingle cingle){
        cingles.add(cingle);
        notifyDataSetChanged();
    }

    public void clear(){
        cingles.clear();
        notifyDataSetChanged();
    }

    public void cleanUpListener(){
        if (mChildEventListner != null){
            mDatabaseReference.removeEventListener(mChildEventListner);
        }
    }
}

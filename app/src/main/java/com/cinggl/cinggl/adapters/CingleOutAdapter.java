package com.cinggl.cinggl.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.sectionedrecyclerview.SectionedRecyclerViewAdapter;
import com.cinggl.cinggl.R;
import com.cinggl.cinggl.models.Cingle;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by J.EL on 7/19/2017.
 */

public class CingleOutAdapter extends RecyclerView.Adapter<CingleOutViewHolder> {
    private Context mContext;
    private DatabaseReference mDatabaseReference;
    private ChildEventListener mChildEventListener;
    private static final String TAG =  CingleOutAdapter.class.getSimpleName();
    private List<Cingle> cingles = new ArrayList<>();
    private List<String>mCingleIds = new ArrayList<>();

//    public CingleOutAdapter(Context mContext, ArrayList<Cingle> cingles) {
//        this.cingles = cingles;
//        this.mContext = mContext;
//    }

    public CingleOutAdapter(final Context context, DatabaseReference ref) {
        mContext = context;
        mDatabaseReference = ref;

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Cingle cingle = dataSnapshot.getValue(Cingle.class);
                cingles.add(cingle);

                notifyDataSetChanged();
            }


            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("failed to read firebase", databaseError.getMessage());
            }
        });

//        ChildEventListener childEventListener = new ChildEventListener() {
//            @Override
//            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
//                Cingle cingle = dataSnapshot.getValue(Cingle.class);
//
//                mCingleIds.add(dataSnapshot.getKey());
//                cingles.add(cingle);
//                notifyItemInserted(cingles.size() - 1);
//
//            }
//
//            @Override
//            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
//
//            }
//
//            @Override
//            public void onChildRemoved(DataSnapshot dataSnapshot) {
//
//            }
//
//            @Override
//            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
//
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        };
//
//        reference.addChildEventListener(childEventListener);
//
//        mChildEventListener = childEventListener;

    }

    @Override
    public int getItemCount() {
        return cingles.size();
    }

    @Override
    public CingleOutViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.best_cingles_list, parent, false);
        return new CingleOutViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CingleOutViewHolder holder, int position) {
        Cingle cingle = cingles.get(position);
        holder.bindCingle(cingle);
    }
}

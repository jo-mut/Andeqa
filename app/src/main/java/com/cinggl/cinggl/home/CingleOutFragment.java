package com.cinggl.cinggl.home;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import com.cinggl.cinggl.Constants;
import com.cinggl.cinggl.R;
import com.cinggl.cinggl.adapters.FirebaseCingleOutViewHolder;
import com.cinggl.cinggl.ui.FirebaseUtil;
import com.cinggl.cinggl.models.Cingle;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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

    private TextView likesCountTextView;
    private boolean processLikes = false;
    private DatabaseReference likesRef;
    private FirebaseAuth firebaseAuth;

    public CingleOutFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_cingle_out, container, false);
        ButterKnife.bind(this, view);

        likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");

        firebaseAuth = FirebaseAuth.getInstance();

        likesRef.keepSynced(true);

        setUpFirebaseAdapter();
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_layout, menu);
        super.onCreateOptionsMenu(menu, inflater);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement

        return super.onOptionsItemSelected(item);
    }

    private void setUpFirebaseAdapter(){
        Cingle cingle = new Cingle();
        databaseReference = FirebaseDatabase.getInstance()
                .getReference(Constants.FIREBASE_PUBLIC_CINGLES);
        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Cingle, FirebaseCingleOutViewHolder>
                (Cingle.class, R.layout.cingle_out_list, FirebaseCingleOutViewHolder.class, databaseReference) {
            @Override
            protected void populateViewHolder(FirebaseCingleOutViewHolder viewHolder, Cingle model, int position) {
                viewHolder.bindCingle(model);

                final String postKey = getRef(position).getKey();

                viewHolder.likesCountTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        processLikes = true;

                            likesRef.addValueEventListener(new ValueEventListener() {

                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if(processLikes){
                                        if(dataSnapshot.child(postKey).hasChild(FirebaseUtil.getCurrentUserId())){
                                            likesRef.child(postKey).child(FirebaseUtil.getCurrentUserId()).removeValue();

                                            processLikes = false;

                                        }else {
                                            likesRef.child(postKey).child(firebaseAuth.getCurrentUser().getUid()).setValue("like ");
                                            processLikes = false;
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }
                });
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

    @Override
    public void onStop(){
        super.onStop();
    }
}

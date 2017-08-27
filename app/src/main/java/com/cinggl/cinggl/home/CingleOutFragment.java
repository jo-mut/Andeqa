package com.cinggl.cinggl.home;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.cinggl.cinggl.Constants;
import com.cinggl.cinggl.R;
import com.cinggl.cinggl.adapters.CingleOutAdapter;
import com.cinggl.cinggl.adapters.LikesViewHolder;
import com.cinggl.cinggl.models.Cingle;
import com.cinggl.cinggl.Trace;
import com.cinggl.cinggl.models.Like;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.L;
import static com.cinggl.cinggl.R.id.cingleSettingsImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class CingleOutFragment extends Fragment{
    @Bind(R.id.cingleOutRecyclerView)RecyclerView cingleOutRecyclerView;
    @Bind(R.id.cingleOutProgressbar)ProgressBar progressBar;

    private DatabaseReference databaseReference;
    private Query cinglesQuery;
    private ChildEventListener mChildEventListener;
    private DatabaseReference usernameRef;
    private DatabaseReference likesRef;
    private FirebaseAuth firebaseAuth;
    private CingleOutAdapter cingleOutAdapter;
    private DatabaseReference sensepointRef;
    private DatabaseReference commentReference;
    private static final String TAG = "CingleOutFragment";
    private LinearLayoutManager layoutManager;

    private List<Cingle> cingles = new ArrayList<>();
    private List<String> cinglesIds = new ArrayList<>();

    private int currentPage = 0;
    private static final int TOTAL_ITEM_EACH_LOAD = 10;
    private static final String KEY_LAYOUT_POSITION = "layout pooition";
    private int cingleOutRecyclerViewPosition = 0;


    public CingleOutFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_cingle_out, container, false);
        ButterKnife.bind(this, view);
        likesRef = FirebaseDatabase.getInstance().getReference(Constants.LIKES);
        sensepointRef = FirebaseDatabase.getInstance().getReference("Sense points");
        usernameRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_USERS);
        commentReference = FirebaseDatabase.getInstance()
                .getReference(Constants.COMMENTS);
        databaseReference = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CINGLES);


        firebaseAuth = FirebaseAuth.getInstance();
        likesRef.keepSynced(true);
        usernameRef.keepSynced(true);
        commentReference.keepSynced(true);


        initializeViewsAdapter();
        cingleOutRecyclerView.addOnScrollListener(mOnScollListener);
        setAllCingles(currentPage);


        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null){
            //restore saved layout manager type
            cingleOutRecyclerViewPosition = (int) savedInstanceState
                    .getSerializable(KEY_LAYOUT_POSITION);
            cingleOutRecyclerView.scrollToPosition(cingleOutRecyclerViewPosition);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onStop() {
        super.onStop();
    }

    private void initializeViewsAdapter(){
        layoutManager =  new LinearLayoutManager(getContext());
        cingleOutRecyclerView.setLayoutManager(layoutManager);
        cingleOutRecyclerView.setHasFixedSize(true);
        cingleOutAdapter = new CingleOutAdapter(getContext());
        cingleOutRecyclerView.setAdapter(cingleOutAdapter);
        cingleOutAdapter.notifyDataSetChanged();
    }

    private RecyclerView.OnScrollListener mOnScollListener = new RecyclerView.OnScrollListener(){
        private int lastVisibileItem;

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            lastVisibileItem = layoutManager.findLastVisibleItemPosition();
        }

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            if (newState == RecyclerView.SCROLL_STATE_IDLE
                    && lastVisibileItem + 1 == cingleOutAdapter.getItemCount()){
//                progressBar.setVisibility(View.VISIBLE);
                setAllCingles(currentPage + 1);
            }
        }
    };

    public void setAllCingles(int start){
//        progressBar.setVisibility(View.VISIBLE);
        cinglesQuery = databaseReference.orderByChild("randomNumber").startAt(start)
                .endAt(start + TOTAL_ITEM_EACH_LOAD);
        cinglesQuery.keepSynced(true);

        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d("Snapshot", dataSnapshot.toString());
//                progressBar.setVisibility(View.GONE);

                Cingle cingle = dataSnapshot.getValue(Cingle.class);
//                    cingle = snapshot.getValue(Cingle.class);
                cinglesIds.add(dataSnapshot.getKey());
                cingles.add(cingle);

                currentPage += 10;
                cingleOutAdapter.setCingles(cingles);
                cingleOutAdapter.notifyItemInserted(cingles.size());
                cingleOutAdapter.getItemCount();
                Log.d("size of cingles list", cingles.size() + "");

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Cingle cingle =  dataSnapshot.getValue(Cingle.class);

                String cingle_key = dataSnapshot.getKey();

                //exclude
                int cingle_index = cinglesIds.indexOf(cingle_key);
                if (cingle_index > - 1){

                    //replace with the new cingle
                    cingles.set(cingle_index, cingle);
                    cingleOutAdapter.notifyItemChanged(cingle_index);
                    cingleOutAdapter.notifyDataSetChanged();
                    cingleOutAdapter.getItemCount();
                }else {
                    Log.w(TAG, "onChildChanged:unknown_child" + cingle_key);
                }


            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onChiledRemoved:" + dataSnapshot.getKey());

                //a cingle has changed. use the key to determine if the cingle
                // is being displayed and
                //so remove it.
                String cingle_key = dataSnapshot.getKey();
                //exclude
                int cingle_index = cinglesIds.indexOf(cingle_key);
                if (cingle_index > - 1){
                    //remove data from the list
                    cinglesIds.remove(cingle_index);
                    cingles.remove(cingle_key);
                    cingleOutAdapter.removeAt(cingle_index);
                    cingleOutAdapter.notifyItemRemoved(cingle_index);

                }else {
                    Log.w(TAG, "onChildRemoved:unknown_child:" + cingle_key);
                }

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                Cingle cingle = dataSnapshot.getValue(Cingle.class);
                String cingle_key = dataSnapshot.getKey();

                //...

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "load Cingles : onCancelled", databaseError.toException());
                Toast.makeText(getContext(), "Failed to load comments.", Toast.LENGTH_SHORT).show();

            }
        };
        cinglesQuery.addChildEventListener(childEventListener);
        mChildEventListener = childEventListener;
    }

    public void cleanUpListener(){
        if (mChildEventListener != null){
            cinglesQuery.removeEventListener(mChildEventListener);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        //save currently selected layout manager;
        int recyclerViewScrollPosition =  getRecyclerViewScrollPosition();
        Log.d(TAG, "Recycler view scroll position:" + recyclerViewScrollPosition);
        outState.putSerializable(KEY_LAYOUT_POSITION, recyclerViewScrollPosition);
        super.onSaveInstanceState(outState);

    }

    private int getRecyclerViewScrollPosition() {
        int scrollPosition = 0;
        // TODO: Is null check necessary?
        if (cingleOutRecyclerView != null && cingleOutRecyclerView.getLayoutManager() != null) {
            scrollPosition = ((LinearLayoutManager) cingleOutRecyclerView.getLayoutManager())
                    .findFirstCompletelyVisibleItemPosition();
        }
        return scrollPosition;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cleanUpListener();
    }

}

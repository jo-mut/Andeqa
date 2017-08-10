package com.cinggl.cinggl.home;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cinggl.cinggl.Constants;
import com.cinggl.cinggl.R;
import com.cinggl.cinggl.adapters.BestCinglesAdapter;
import com.cinggl.cinggl.models.Cingle;
import com.cinggl.cinggl.utils.CinglesItemClickListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

import static android.media.CamcorderProfile.get;

/**
 * A simple {@link Fragment} subclass.
 */
public class BestCinglesFragment extends Fragment implements CinglesItemClickListener {
    private DatabaseReference databaseReference;
    private DatabaseReference usernameRef;
    private DatabaseReference likesRef;
    private DatabaseReference commentsRef;
    private static final String TAG = "BestCingleFragment";
    private static final String KEY_LAYOUT_POSITION = "layout position";
    private int bestCingleRecyclerPosition = 0;
    private List<Cingle> bestCingles = new ArrayList<>();
    private List<String> cinglesIds = new ArrayList<>();
    private BestCinglesAdapter bestCinglesAdapter;
    private LinearLayoutManager layoutManager;
    private CinglesItemClickListener cinglesItemClickListener;
    private int currentPage = 0;
    private Query bestCinglesQuery;
    private FirebaseUser firebaseUser;
    private FirebaseAuth firebaseAuth;
    private ChildEventListener mChildEventListener;
    private static final int TOTAL_ITEM_EACH_LOAD = 10;



    @Bind(R.id.bestCinglesRecyclerView)RecyclerView bestCinglesRecyclerView;
    @Bind(R.id.bestCinglesProgressbar)ProgressBar progressBar;
    @Bind(R.id.currentDateTextView)TextView mCurrentDateTextView;



    public BestCinglesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CINGLES);
        usernameRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_USERS);
        likesRef = FirebaseDatabase.getInstance().getReference(Constants.LIKES);
        commentsRef = FirebaseDatabase.getInstance().getReference(Constants.COMMENTS);

        usernameRef.keepSynced(true);
        likesRef.keepSynced(true);
        commentsRef.keepSynced(true);
        databaseReference.keepSynced(true);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_best_cingles, container, false);
        ButterKnife.bind(this, view);

        cinglesItemClickListener = this;

        initializeViewsAdapter();
        setCurrentDate();
        bestCinglesRecyclerView.addOnScrollListener(mOnScollListener);
        setBestCingles(currentPage);

        return view;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null){
            //restore saved layout manager type
            bestCingleRecyclerPosition = (int) savedInstanceState
                    .getSerializable(KEY_LAYOUT_POSITION);
            bestCinglesRecyclerView.scrollToPosition(bestCingleRecyclerPosition);
        }
    }

    private void initializeViewsAdapter(){
        layoutManager =  new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        bestCinglesRecyclerView.setLayoutManager(layoutManager);
        bestCinglesRecyclerView.setHasFixedSize(true);
        bestCinglesAdapter = new BestCinglesAdapter(getContext(), this);
        bestCinglesRecyclerView.setAdapter(bestCinglesAdapter);
    }

    private void setCurrentDate(){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("d");
        String date = simpleDateFormat.format(new Date());

        if (date.endsWith("1") && !date.endsWith("11"))
            simpleDateFormat = new SimpleDateFormat("d'st' MMM yyyy");
        else if (date.endsWith("2") && !date.endsWith("12"))
            simpleDateFormat = new SimpleDateFormat("d'nd' MMM yyyy");
        else if (date.endsWith("3") && !date.endsWith("13"))
            simpleDateFormat = new SimpleDateFormat("d'rd' MMM yyyy");
        else
            simpleDateFormat = new SimpleDateFormat("d'th' MMM yyyy");
        String currentDate = simpleDateFormat.format(new Date());

        mCurrentDateTextView.setText(currentDate);
    }

    public void setBestCingles(int start){
//        progressBar.setVisibility(View.VISIBLE);
        bestCinglesQuery = databaseReference.orderByChild("sensepoint").startAt(start)
                .endAt(start + TOTAL_ITEM_EACH_LOAD);
        bestCinglesQuery.keepSynced(true);

        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d("Snapshot", dataSnapshot.toString());
//                progressBar.setVisibility(View.GONE);

                Cingle cingle = dataSnapshot.getValue(Cingle.class);
                cinglesIds.add(dataSnapshot.getKey());
                bestCingles.add(cingle);

                currentPage += 10;
                bestCinglesAdapter.setCingles(bestCingles);
                bestCinglesAdapter.notifyItemInserted(bestCingles.size());
                bestCinglesAdapter.getItemCount();
                Log.d("size of cingles list", bestCingles.size() + "");

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Cingle cingle =  dataSnapshot.getValue(Cingle.class);

                String cingle_key = dataSnapshot.getKey();

                //exclude
                int cingle_index = cinglesIds.indexOf(cingle_key);
                if (cingle_index > - 1){

                    //replace with the new cingle
                    bestCingles.set(cingle_index, cingle);
                    bestCinglesAdapter.notifyItemChanged(cingle_index);
                    bestCinglesAdapter.getItemCount();
                }else {
                    Log.w(TAG, "onChildChanged:unknown_child" + cingle_key);
                }

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onChiledRemoved:" + dataSnapshot.getKey());

                //a cingle has changed. use the key to determine if the comment
                // is being displayed and
                //so remove it.

                String cingle_key = dataSnapshot.getKey();

                //exclude
                int cingle_index = cinglesIds.indexOf(cingle_key);
                if (cingle_index > - 1){

                    //remove data from the list
                    cinglesIds.remove(cingle_index);
                    bestCingles.remove(cingle_key);
                    bestCinglesAdapter.notifyItemRemoved(cingle_index);
                    bestCinglesAdapter.getItemCount();
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
        bestCinglesQuery.addChildEventListener(childEventListener);
        mChildEventListener = childEventListener;
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
        if (bestCinglesRecyclerView != null && bestCinglesRecyclerView.getLayoutManager() != null) {
            scrollPosition = ((LinearLayoutManager) bestCinglesRecyclerView.getLayoutManager())
                    .findFirstCompletelyVisibleItemPosition();
        }
        return scrollPosition;
    }

    @Override
    public void clickPosition(int position, int id){
        final String postKey = bestCingles.get(position).getPushId();
        if (id == R.id.likesImageView){

        }

        if (id == R.id.cingleSettingsImageView){

        }

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
                    && lastVisibileItem + 1 == bestCinglesAdapter.getItemCount()){
//                progressBar.setVisibility(View.VISIBLE);
                setBestCingles(currentPage + 1);
            }
        }
    };



}

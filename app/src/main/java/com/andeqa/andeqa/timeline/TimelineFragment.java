package com.andeqa.andeqa.timeline;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.R;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class TimelineFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    @Bind(R.id.swipeRefreshLayout)SwipeRefreshLayout mSwipeRefreshLayout;
    @Bind(R.id.timelineRecyclerView)RecyclerView mTimelineRecyclerView;
    @Bind(R.id.placeHolderRelativeLayout)RelativeLayout mPlaceHolderRelativeLayout;

    private static final String TAG = TimelineFragment.class.getSimpleName();
    private static final String EXTRA_USER_UID = "uid";
    private FirestoreRecyclerAdapter firestoreRecyclerAdapter;
    private FirebaseAuth firebaseAuth;
    private CollectionReference timelineCollection;
    private Query timelineQuery;
    private TimelineAdapter timelineAdapter;
    private int TOTAL_ITEMS = 30;


    private List<String> activitiesIds = new ArrayList<>();
    private List<DocumentSnapshot> timelineSnapshots = new ArrayList<>();

    public TimelineFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_timeline, container, false);
        ButterKnife.bind(this, view);

        mSwipeRefreshLayout.setOnRefreshListener(this);

        firebaseAuth = FirebaseAuth.getInstance();

        timelineCollection = FirebaseFirestore.getInstance().collection(Constants.TIMELINE);
        timelineQuery = timelineCollection.document(firebaseAuth.getCurrentUser().getUid())
                .collection("activities").orderBy("time", Query.Direction.ASCENDING)
                .limit(TOTAL_ITEMS);

        return  view;
    }

    @Override
    public void onStart() {
        super.onStart();
        timelineSnapshots.clear();
        setRecyclerView();
        setCollections();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onRefresh() {
        setNextCollections();
    }

    private void setRecyclerView(){
        timelineAdapter = new TimelineAdapter(getContext());
        mTimelineRecyclerView.setAdapter(timelineAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        mTimelineRecyclerView.setHasFixedSize(false);
        mTimelineRecyclerView.setLayoutManager(layoutManager);
    }




    private void setCollections(){
        timelineQuery.limit(TOTAL_ITEMS)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                        if (e != null) {
                            Log.w(TAG, "Listen error", e);
                            return;
                        }

                        if (!documentSnapshots.isEmpty()){
                            //retrieve the first bacth of timelineSnapshots
                            for (final DocumentChange change : documentSnapshots.getDocumentChanges()) {
                                switch (change.getType()) {
                                    case ADDED:
                                        onDocumentAdded(change);
                                        break;
                                    case MODIFIED:
                                        onDocumentModified(change);
                                        break;
                                    case REMOVED:
                                        onDocumentRemoved(change);
                                        break;
                                }
                            }
                        }else {
                            mPlaceHolderRelativeLayout.setVisibility(View.VISIBLE);
                        }

                    }
                });
    }

    private void setNextCollections(){
        mSwipeRefreshLayout.setRefreshing(true);
        // Get the last visible document
        final int snapshotSize = timelineAdapter.getItemCount();

        if (snapshotSize == 0){
            mSwipeRefreshLayout.setRefreshing(false);
        }else {
            DocumentSnapshot lastVisible = timelineAdapter.getSnapshot(snapshotSize - 1);

            //retrieve the first bacth of timelineSnapshots
            Query nextSellingQuery = timelineCollection.document(firebaseAuth.getCurrentUser().getUid())
                    .collection("activities").orderBy("time", Query.Direction.ASCENDING)
                    .startAfter(lastVisible)
                    .limit(TOTAL_ITEMS);

            nextSellingQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                    if (e != null) {
                        Log.w(TAG, "Listen error", e);
                        return;
                    }

                    if (!documentSnapshots.isEmpty()){
                        //retrieve the first bacth of timelineSnapshots
                        for (final DocumentChange change : documentSnapshots.getDocumentChanges()) {
                            switch (change.getType()) {
                                case ADDED:
                                    onDocumentAdded(change);
                                    break;
                                case MODIFIED:
                                    onDocumentModified(change);
                                    break;
                                case REMOVED:
                                    onDocumentRemoved(change);
                                    break;
                            }
                        }

                        mSwipeRefreshLayout.setRefreshing(false);
                    }else {
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                }
            });
        }

    }

    protected void onDocumentAdded(DocumentChange change) {
        activitiesIds.add(change.getDocument().getId());
        timelineSnapshots.add(change.getDocument());
        timelineAdapter.setTimelineActivities(timelineSnapshots);
        timelineAdapter.notifyItemInserted(timelineSnapshots.size() -1);
        timelineAdapter.getItemCount();

    }

    protected void onDocumentModified(DocumentChange change) {
       try {
           if (change.getOldIndex() == change.getNewIndex()) {
               // Item changed but remained in same position
               timelineSnapshots.set(change.getOldIndex(), change.getDocument());
               timelineAdapter.notifyItemChanged(change.getOldIndex());
           } else {
               // Item changed and changed position
               timelineSnapshots.remove(change.getOldIndex());
               timelineSnapshots.add(change.getNewIndex(), change.getDocument());
               timelineAdapter.notifyItemRangeChanged(0, timelineSnapshots.size());
           }
       }catch (Exception e){
           e.printStackTrace();
       }
    }

    protected void onDocumentRemoved(DocumentChange change) {
       try{
           timelineSnapshots.remove(change.getOldIndex());
           timelineAdapter.notifyItemRemoved(change.getOldIndex());
           timelineAdapter.notifyItemRangeChanged(0, timelineSnapshots.size());
       }catch (Exception e){
           e.printStackTrace();
       }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        timelineSnapshots.clear();
    }

    @Override
    public void onResume() {
        super.onResume();
        timelineQuery.limit(TOTAL_ITEMS)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                        if (e != null) {
                            Log.w(TAG, "Listen error", e);
                            return;
                        }

                        if (!documentSnapshots.isEmpty()){
                          //document snapshot is not empty
                        }else {
                            timelineSnapshots.clear();
                        }

                    }
                });
    }
}

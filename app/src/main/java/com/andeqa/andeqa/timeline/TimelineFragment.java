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
import com.andeqa.andeqa.message.MessagesFragment;
import com.andeqa.andeqa.message.RoomAdapter;
import com.andeqa.andeqa.utils.EndlessRecyclerOnScrollListener;
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

    private static final String TAG = MessagesFragment.class.getSimpleName();
    private static final String EXTRA_USER_UID = "uid";
    private FirestoreRecyclerAdapter firestoreRecyclerAdapter;
    private FirebaseAuth firebaseAuth;
    private CollectionReference timelineCollection;
    private Query timelineQuery;
    private TimelineAdapter timelineAdapter;
    private int TOTAL_ITEMS = 30;


    private List<String> activitiesIds = new ArrayList<>();
    private List<DocumentSnapshot> documentSnapshots = new ArrayList<>();



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
                .collection("timeline").orderBy("time", Query.Direction.DESCENDING)
                .limit(TOTAL_ITEMS);

        setRecyclerView();
        setCollections();

        return  view;
    }

    @Override
    public void onRefresh() {
        setNextCollections();
    }

    private void setRecyclerView(){
        timelineAdapter = new TimelineAdapter(getContext());
        mTimelineRecyclerView.setAdapter(timelineAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setAutoMeasureEnabled(true);
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
                            //retrieve the first bacth of documentSnapshots
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
        DocumentSnapshot lastVisible = timelineAdapter.getSnapshot(snapshotSize - 1);

        //retrieve the first bacth of documentSnapshots
        Query nextSellingQuery = timelineCollection.document(firebaseAuth.getCurrentUser().getUid())
                .collection("timeline").orderBy("time", Query.Direction.DESCENDING)
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
                    //retrieve the first bacth of documentSnapshots
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

                    mSwipeRefreshLayout.setRefreshing(true);
                }else {
                    mSwipeRefreshLayout.setRefreshing(true);
                }
            }
        });
    }

    protected void onDocumentAdded(DocumentChange change) {
        activitiesIds.add(change.getDocument().getId());
        documentSnapshots.add(change.getDocument());
        timelineAdapter.setTimelineActivities(documentSnapshots);
        timelineAdapter.notifyItemInserted(documentSnapshots.size() -1);
        timelineAdapter.getItemCount();

    }

    protected void onDocumentModified(DocumentChange change) {
        if (change.getOldIndex() == change.getNewIndex()) {
            // Item changed but remained in same position
            documentSnapshots.set(change.getOldIndex(), change.getDocument());
            timelineAdapter.notifyItemChanged(change.getOldIndex());
        } else {
            // Item changed and changed position
            documentSnapshots.remove(change.getOldIndex());
            documentSnapshots.add(change.getNewIndex(), change.getDocument());
            timelineAdapter.notifyItemRangeChanged(0, documentSnapshots.size());
        }
    }

    protected void onDocumentRemoved(DocumentChange change) {
        documentSnapshots.remove(change.getOldIndex());
        timelineAdapter.notifyItemRemoved(change.getOldIndex());
        timelineAdapter.notifyItemRangeChanged(0, documentSnapshots.size());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        documentSnapshots.clear();
    }

}

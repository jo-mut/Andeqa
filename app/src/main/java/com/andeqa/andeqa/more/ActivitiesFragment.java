package com.andeqa.andeqa.more;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.R;
import com.andeqa.andeqa.models.Timeline;
import com.andeqa.andeqa.search.SearchPeopleActivity;
import com.andeqa.andeqa.search.SearchPostsActivity;
import com.andeqa.andeqa.utils.EndlessLinearRecyclerViewOnScrollListener;
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
public class ActivitiesFragment extends Fragment {

    @Bind(R.id.timelineRecyclerView)RecyclerView mTimelineRecyclerView;
    @Bind(R.id.placeHolderRelativeLayout)RelativeLayout mPlaceHolderRelativeLayout;

    private static final String TAG = ActivitiesFragment.class.getSimpleName();
    private static final String EXTRA_USER_UID = "uid";
    private FirestoreRecyclerAdapter firestoreRecyclerAdapter;
    private FirebaseAuth firebaseAuth;
    private CollectionReference timelineCollection;
    private Query timelineQuery;
    private NotificationsAdapter notificationsAdapter;
    private int TOTAL_ITEMS = 30;

    private List<String> activitiesIds = new ArrayList<>();
    private List<DocumentSnapshot> timelineSnapshots = new ArrayList<>();

    public ActivitiesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_activities, container, false);
        ButterKnife.bind(this, view);
        firebaseAuth = FirebaseAuth.getInstance();
        timelineCollection = FirebaseFirestore.getInstance().collection(Constants.TIMELINE);
        timelineQuery = timelineCollection.document(firebaseAuth.getCurrentUser().getUid())
                .collection("activities");
        mTimelineRecyclerView.addOnScrollListener(new EndlessLinearRecyclerViewOnScrollListener() {
            @Override
            public void onLoadMore() {
                setNextCollections();
            }
        });

        return  view;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.explore_menu, menu);

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_search){
            Intent intent =  new Intent(getActivity(), SearchPostsActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        loadData();
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
    public void onDestroyView() {
        super.onDestroyView();
        timelineSnapshots.clear();
    }


    private void loadData(){
        timelineSnapshots.clear();
        setRecyclerView();
        setCollections();
    }

    private void setRecyclerView(){
        notificationsAdapter = new NotificationsAdapter(getContext());
        mTimelineRecyclerView.setAdapter(notificationsAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        mTimelineRecyclerView.setHasFixedSize(false);
        mTimelineRecyclerView.setLayoutManager(layoutManager);
        ViewCompat.setNestedScrollingEnabled(mTimelineRecyclerView,false);

    }

    private void setCollections(){
        timelineQuery.orderBy("time", Query.Direction.DESCENDING)
                .limit(TOTAL_ITEMS).addSnapshotListener(new EventListener<QuerySnapshot>() {
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
                                        Timeline timeline = change.getDocument().toObject(Timeline.class);
                                        if (timeline.getType() != null){
                                            final String type = timeline.getType();
                                            if (type.equals("comment") || type.equals("follow")){
                                                onDocumentAdded(change);
                                            }else {
                                                //do not add any other timeline activity
                                            }
                                        }
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
        // Get the last visible document
        final int snapshotSize = notificationsAdapter.getItemCount();

        if (snapshotSize > 0){
            DocumentSnapshot lastVisible = notificationsAdapter.getSnapshot(snapshotSize - 1);

            //retrieve the first bacth of timelineSnapshots
            Query nextSellingQuery = timelineCollection.document(firebaseAuth.getCurrentUser().getUid())
                    .collection("activities").orderBy("time", Query.Direction.DESCENDING)
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
                                    Timeline timeline = change.getDocument().toObject(Timeline.class);
                                    if (timeline.getType() != null){
                                        final String type = timeline.getType();
                                        if (type.equals("comment") || type.equals("follow")){
                                            onDocumentAdded(change);
                                        }else {
                                            //do not add any other timeline activity
                                        }
                                    }
                                    break;
                                case MODIFIED:
                                    onDocumentModified(change);
                                    break;
                                case REMOVED:
                                    onDocumentRemoved(change);
                                    break;
                            }
                        }

                    }
                }
            });
        }

    }

    protected void onDocumentAdded(DocumentChange change) {
        activitiesIds.add(change.getDocument().getId());
        timelineSnapshots.add(change.getDocument());
        notificationsAdapter.setTimelineActivities(timelineSnapshots);
        notificationsAdapter.notifyItemInserted(timelineSnapshots.size() -1);
        notificationsAdapter.getItemCount();

    }

    protected void onDocumentModified(DocumentChange change) {
        try {
            if (change.getOldIndex() == change.getNewIndex()) {
                // Item changed but remained in same position
                timelineSnapshots.set(change.getOldIndex(), change.getDocument());
                notificationsAdapter.notifyItemChanged(change.getOldIndex());
            } else {
                // Item changed and changed position
                timelineSnapshots.remove(change.getOldIndex());
                timelineSnapshots.add(change.getNewIndex(), change.getDocument());
                notificationsAdapter.notifyItemRangeChanged(0, timelineSnapshots.size());
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    protected void onDocumentRemoved(DocumentChange change) {
        try{
            timelineSnapshots.remove(change.getOldIndex());
            notificationsAdapter.notifyItemRemoved(change.getOldIndex());
            notificationsAdapter.notifyItemRangeChanged(0, timelineSnapshots.size());
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }


}

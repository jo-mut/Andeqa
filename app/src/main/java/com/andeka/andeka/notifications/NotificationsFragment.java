package com.andeka.andeka.notifications;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.andeka.andeka.Constants;
import com.andeka.andeka.R;
import com.andeka.andeka.main.HomeActivity;
import com.andeka.andeka.search.SearchPostsActivity;
import com.andeka.andeka.utils.BottomReachedListener;
import com.andeka.andeka.utils.EndlessStaggeredScrollListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
public class NotificationsFragment extends Fragment {

    @Bind(R.id.timelineRecyclerView)RecyclerView mTimelineRecyclerView;
    @Bind(R.id.placeHolderRelativeLayout)RelativeLayout mPlaceHolderRelativeLayout;

    private static final String TAG = NotificationsFragment.class.getSimpleName();
    private NotificationsAdapter notificationsAdapter;
    private List<DocumentSnapshot> mSnapshots = new ArrayList<>();
    private HomeActivity mHomeActivity;
    //firebase
    private CollectionReference timelineCollection;
    private Query timelineQuery;
    private FirebaseAuth firebaseAuth;
    private int TOTAL_ITEMS = 20;

    public NotificationsFragment() {
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
        View view = inflater.inflate(R.layout.fragment_activities, container, false);
        ButterKnife.bind(this, view);
        initFirebase();
        getNotification();
        setRecyclerView();

        mTimelineRecyclerView.addOnScrollListener(new EndlessStaggeredScrollListener() {
            @Override
            public void onLoadMore() {
                notificationsAdapter.setBottomReachedListener(new BottomReachedListener() {
                    @Override
                    public void onBottomReached(int position) {
                        getNextNotification();                    }
                });
            }
        });

        return  view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

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
    }

    @Override
    public void onResume() {
        super.onResume();
    }


    private void setRecyclerView(){
        notificationsAdapter = new NotificationsAdapter(getContext(), mSnapshots);
        mTimelineRecyclerView.setAdapter(notificationsAdapter);
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(1,StaggeredGridLayoutManager.VERTICAL);
        mTimelineRecyclerView.setHasFixedSize(false);
        mTimelineRecyclerView.setLayoutManager(layoutManager);
        ViewCompat.setNestedScrollingEnabled(mTimelineRecyclerView,false);
    }

    private void initFirebase(){
        firebaseAuth = FirebaseAuth.getInstance();
        timelineCollection = FirebaseFirestore.getInstance().collection(Constants.TIMELINE);
        timelineQuery = timelineCollection.document(firebaseAuth.getCurrentUser().getUid())
                .collection("activities");
    }

    public void getNotification(){
        timelineQuery.orderBy("time", Query.Direction.DESCENDING)
                .limit(TOTAL_ITEMS).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable QuerySnapshot documentSnapshots,
                                @javax.annotation.Nullable FirebaseFirestoreException e) {

                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (!documentSnapshots.isEmpty()){
                    for (final DocumentChange documentChange : documentSnapshots.getDocumentChanges()) {
                        switch (documentChange.getType()) {
                            case ADDED:
                                onDocumentAdded(documentChange);
                                break;
                            case MODIFIED:
                                onDocumentModified(documentChange);
                                break;
                            case REMOVED:
                                onDocumentRemoved(documentChange);
                                break;
                        }
                    }
                }
            }
        });

    }

    public void getNextNotification(){
        DocumentSnapshot last = mSnapshots.get(mSnapshots.size() - 1);
        Query query =  timelineCollection.document(firebaseAuth.getCurrentUser().getUid())
                .collection("activities").orderBy("time", Query.Direction.DESCENDING)
                .startAfter(last).limit(TOTAL_ITEMS);
        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(final QuerySnapshot documentSnapshots) {

                if (!documentSnapshots.isEmpty()){
                    // get remote data
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            for (final DocumentChange documentChange : documentSnapshots.getDocumentChanges()) {
                                switch (documentChange.getType()) {
                                    case ADDED:
                                        onDocumentAdded(documentChange);
                                        break;
                                    case MODIFIED:
                                        onDocumentModified(documentChange);
                                        break;
                                    case REMOVED:
                                        onDocumentRemoved(documentChange);
                                        break;
                                }
                            }
                        }
                    }, 4000);
                }else {
                    mSnapshots.add(null);
                }
            }
        });

    }

    protected void onDocumentAdded(DocumentChange change) {
        mSnapshots.add(change.getDocument());
        notificationsAdapter.notifyItemInserted(mSnapshots.size() - 1);
        notificationsAdapter.getItemCount();
    }

    protected void onDocumentModified(DocumentChange change) {
        try {
            if (change.getOldIndex() == change.getNewIndex()) {
                // Item changed but remained in same position
                mSnapshots.set(change.getOldIndex(), change.getDocument());
                notificationsAdapter.notifyItemChanged(change.getOldIndex());
            } else {
                // Item changed and changed position
                mSnapshots.remove(change.getOldIndex());
                mSnapshots.add(change.getNewIndex(), change.getDocument());
                notificationsAdapter.notifyItemRangeChanged(0, mSnapshots.size());
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    protected void onDocumentRemoved(DocumentChange change) {
        try {
            mSnapshots.remove(change.getOldIndex());
            notificationsAdapter.notifyItemRemoved(change.getOldIndex());
            notificationsAdapter.notifyItemRangeChanged(0, mSnapshots.size());
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

}

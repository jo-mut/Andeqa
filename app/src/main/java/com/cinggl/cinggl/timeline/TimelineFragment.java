package com.cinggl.cinggl.timeline;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.cinggl.cinggl.Constants;
import com.cinggl.cinggl.R;
import com.cinggl.cinggl.message.MessagesFragment;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class TimelineFragment extends Fragment {
    @Bind(R.id.timelineRecyclerView)RecyclerView mTimelineRecyclerView;
    @Bind(R.id.placeHolderRelativeLayout)RelativeLayout mPlaceHolderRelativeLayout;

    private static final String TAG = MessagesFragment.class.getSimpleName();
    private static final String EXTRA_USER_UID = "uid";
    private FirestoreRecyclerAdapter firestoreRecyclerAdapter;
    private FirebaseAuth firebaseAuth;
    private CollectionReference timelineCollection;
    private Query timelineQuery;
    private TimelineAdapter timelineAdapter;



    public TimelineFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_timeline, container, false);
        ButterKnife.bind(this, view);
        firebaseAuth = FirebaseAuth.getInstance();

        timelineCollection = FirebaseFirestore.getInstance().collection(Constants.TIMELINE);
        timelineQuery = timelineCollection.document(firebaseAuth.getCurrentUser().getUid())
                .collection("timeline").orderBy("timeStamp", Query.Direction.DESCENDING);

        getTimeline();

        return  view;
    }

    private void getTimeline(){
       timelineQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
           @Override
           public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

               if (e != null) {
                   Log.w(TAG, "Listen error", e);
                   return;
               }

               if (!documentSnapshots.isEmpty()){
                   timelineAdapter = new TimelineAdapter(timelineQuery, getContext());
                   timelineAdapter.startListening();
                   mTimelineRecyclerView.setAdapter(timelineAdapter);
                   LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
                   mTimelineRecyclerView.setLayoutManager(layoutManager);
               }else {
                   mPlaceHolderRelativeLayout.setVisibility(View.VISIBLE);
               }

           }
       });
    }


}

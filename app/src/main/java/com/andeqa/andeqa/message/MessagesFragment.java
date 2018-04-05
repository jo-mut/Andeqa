package com.andeqa.andeqa.message;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.R;
import com.andeqa.andeqa.utils.EndlessRecyclerOnScrollListener;
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
public class MessagesFragment extends Fragment {

    @Bind(R.id.roomsRecyclerView)RecyclerView mRoomsRecyclerView;
    @Bind(R.id.placeHolderRelativeLayout)RelativeLayout mPlaceHolderRelativeLayout;

    private static final String TAG = MessagesFragment.class.getSimpleName();
    private CollectionReference roomsCollections;
    private Query roomsQuery;
    private FirebaseAuth firebaseAuth;
    private RoomAdapter roomAdapter;
    private static final int TOTAL_ITEMS = 25;

    private List<String> roomIds = new ArrayList<>();
    private List<DocumentSnapshot> documentSnapshots = new ArrayList<>();

    public MessagesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_messages, container, false);
        ButterKnife.bind(this, view);

        firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() != null){
            roomsCollections = FirebaseFirestore.getInstance().collection(Constants.MESSAGES);
            roomsQuery = roomsCollections.document("rooms")
                    .collection(firebaseAuth.getCurrentUser().getUid());

            setRecyclerView();
            setCollections();

//            mRoomsRecyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener() {
//                @Override
//                public void onLoadMore() {
//                    setNextCollections();
//                }
//            });
        }

        return view;
    }

    private void setRecyclerView(){
        roomAdapter = new RoomAdapter(getContext());
        mRoomsRecyclerView.setAdapter(roomAdapter);
        roomAdapter.notifyDataSetChanged();
        mRoomsRecyclerView.setHasFixedSize(false);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setAutoMeasureEnabled(true);
        mRoomsRecyclerView.setLayoutManager(layoutManager);
    }

    private void setCollections(){
        roomsQuery.orderBy("time").limit(TOTAL_ITEMS)
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

                            Log.d("chats rooms size", documentSnapshots.size() + "");
                        }else {
                            mPlaceHolderRelativeLayout.setVisibility(View.VISIBLE);
                        }

                    }
                });
    }

    private void setNextCollections(){
        // Get the last visible document
        final int snapshotSize = roomAdapter.getItemCount();
        DocumentSnapshot lastVisible = roomAdapter.getSnapshot(snapshotSize - 1);

        //retrieve the first bacth of documentSnapshots
        Query nextSellingQuery = roomsCollections.document("rooms")
                .collection(firebaseAuth.getCurrentUser().getUid())
                .orderBy("time").startAfter(lastVisible)
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
                }
            }
        });
    }

    protected void onDocumentAdded(DocumentChange change) {
        roomIds.add(change.getDocument().getId());
        documentSnapshots.add(change.getDocument());
        roomAdapter.setChatRooms(documentSnapshots);
        roomAdapter.notifyItemInserted(documentSnapshots.size() -1);
        roomAdapter.getItemCount();

    }

    protected void onDocumentModified(DocumentChange change) {
        if (change.getOldIndex() == change.getNewIndex()) {
            // Item changed but remained in same position
            documentSnapshots.set(change.getOldIndex(), change.getDocument());
            roomAdapter.notifyItemChanged(change.getOldIndex());
        } else {
            // Item changed and changed position
            documentSnapshots.remove(change.getOldIndex());
            documentSnapshots.add(change.getNewIndex(), change.getDocument());
            roomAdapter.notifyItemRangeChanged(0, documentSnapshots.size());
        }
    }

    protected void onDocumentRemoved(DocumentChange change) {
        documentSnapshots.remove(change.getOldIndex());
        roomAdapter.notifyItemRemoved(change.getOldIndex());
        roomAdapter.notifyItemRangeChanged(0, documentSnapshots.size());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        documentSnapshots.clear();
    }
}

package com.andeqa.andeqa.message;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.R;
import com.andeqa.andeqa.utils.EndlessLinearRecyclerViewOnScrollListener;
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
public class MessagesFragment extends Fragment{
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
            roomsQuery = roomsCollections.document("last messages")
                    .collection(firebaseAuth.getCurrentUser().getUid());

            mRoomsRecyclerView.addOnScrollListener(new EndlessLinearRecyclerViewOnScrollListener() {
                @Override
                public void onLoadMore() {
                    setNextCollections();
                }
            });

        }

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        loadData();
    }

    @Override
    public void onStart() {
        super.onStart();
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
        documentSnapshots.clear();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }


    private void loadData(){
        documentSnapshots.clear();
        setRecyclerView();
        setCollections();
    }

    private void setRecyclerView(){
        roomAdapter = new RoomAdapter(getContext());
        mRoomsRecyclerView.setAdapter(roomAdapter);
        mRoomsRecyclerView.setHasFixedSize(false);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        mRoomsRecyclerView.setLayoutManager(layoutManager);
    }

    private void setCollections(){
        roomsQuery.orderBy("time", Query.Direction.DESCENDING).limit(TOTAL_ITEMS)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                        if (e != null) {
                            Log.w(TAG, "Listed    onDocumentModified(change);\n" +
                                    "n error", e);
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

        if (snapshotSize == 0){
        }else {
            DocumentSnapshot lastVisible = roomAdapter.getSnapshot(snapshotSize - 1);

            //retrieve the first bacth of documentSnapshots
            Query nextSellingQuery = roomsCollections.document("last messages")
                    .collection(firebaseAuth.getCurrentUser().getUid())
                    .orderBy("time", Query.Direction.DESCENDING).startAfter(lastVisible)
                    .limit(TOTAL_ITEMS);

           nextSellingQuery.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
               @Override
               public void onSuccess(QuerySnapshot documentSnapshots) {
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
    }

    protected void onDocumentAdded(DocumentChange change) {
        roomIds.add(change.getDocument().getId());
        documentSnapshots.add(change.getDocument());
        roomAdapter.setChatRooms(documentSnapshots);
        roomAdapter.notifyItemInserted(documentSnapshots.size() -1);
        roomAdapter.getItemCount();

    }

    protected void onDocumentModified(DocumentChange change) {
        try {
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
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    protected void onDocumentRemoved(DocumentChange change) {
        try{
            documentSnapshots.remove(change.getOldIndex());
            roomAdapter.notifyItemRemoved(change.getOldIndex());
            roomAdapter.notifyItemRangeChanged(0, documentSnapshots.size());
        }catch (Exception e){
            e.printStackTrace();
        }
    }


}

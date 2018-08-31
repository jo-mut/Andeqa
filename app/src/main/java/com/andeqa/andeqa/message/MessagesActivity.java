package com.andeqa.andeqa.message;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.R;
import com.andeqa.andeqa.search.SearchActivity;
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

public class MessagesActivity extends AppCompatActivity implements View.OnClickListener {
    @Bind(R.id.roomsRecyclerView)RecyclerView mRoomsRecyclerView;
    @Bind(R.id.placeHolderRelativeLayout)RelativeLayout mPlaceHolderRelativeLayout;
    @Bind(R.id.newChatImageView)ImageView newChatImageView;

    private static final String TAG = MessagesActivity.class.getSimpleName();
    private CollectionReference roomsCollections;
    private Query roomsQuery;
    private FirebaseAuth firebaseAuth;
    private RoomAdapter roomAdapter;
    private static final int TOTAL_ITEMS = 25;

    private List<String> roomIds = new ArrayList<>();
    private List<DocumentSnapshot> documentSnapshots = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);
        ButterKnife.bind(this);

        newChatImageView.setOnClickListener(this);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

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
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadData();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onClick(View v){
        if (v == newChatImageView){
            Intent intent = new Intent(this, SearchActivity.class);
            startActivity(intent);
        }
    }

    private void loadData(){
        documentSnapshots.clear();
        setRecyclerView();
        setCollections();
    }

    private void setRecyclerView(){
        roomAdapter = new RoomAdapter(this);
        mRoomsRecyclerView.setAdapter(roomAdapter);
        mRoomsRecyclerView.setHasFixedSize(false);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
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

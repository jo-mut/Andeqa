package com.andeqa.andeqa.collections;

import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.R;
import com.andeqa.andeqa.models.Andeqan;
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

public class ProfileCollectionsActivity extends AppCompatActivity implements
        SwipeRefreshLayout.OnRefreshListener{
    @Bind(R.id.collectionsRecyclerView)RecyclerView mCollectionsRecyclerView;
    @Bind(R.id.swipeRefreshLayout)SwipeRefreshLayout mSwipeRefreshLayout;
    private static final String TAG = ProfileCollectionsActivity.class.getSimpleName();
    //firestore reference
    private CollectionReference collectionCollection;
    private Query collectionsQuery;
    private CollectionReference usersCollections;
    private Query profileCollectionsQuery;

    //firebase auth
    private FirebaseAuth firebaseAuth;
    //firestore adapters
    private ProfileCollectionsAdapter profileCollectionsAdapter;

    //strings
    private String mUid;
    private static final String EXTRA_USER_UID = "uid";
    private static final String EXTRA_ROOM_UID = "roomId";

    private  static final int MAX_WIDTH = 200;
    private static final int MAX_HEIGHT = 200;
    private static final int TOTAL_ITEMS = 20;


    private DocumentSnapshot lastVisible;
    private LinearLayoutManager layoutManager;

    private List<String> collectionsIds = new ArrayList<>();
    private List<DocumentSnapshot> documentSnapshots = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_collections);
        ButterKnife.bind(this);
        firebaseAuth = FirebaseAuth.getInstance();

        /**set the toolbar*/
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationIcon(R.drawable.ic_arrow);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        /**initialize refresh listner*/
        mSwipeRefreshLayout.setOnRefreshListener(this);

        if (firebaseAuth.getCurrentUser() != null){

            /**get intent extras*/
            mUid = getIntent().getStringExtra(EXTRA_USER_UID);

            /**firestore refences paths*/
            usersCollections = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);
            collectionCollection = FirebaseFirestore.getInstance().collection(Constants.USER_COLLECTIONS);
            profileCollectionsQuery = collectionCollection.orderBy("time", Query.Direction.DESCENDING)
                    .whereEqualTo("user_id", mUid)
                    .limit(TOTAL_ITEMS);

        }

        usersCollections.document(mUid).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (documentSnapshot.exists()){
                    Andeqan andeqan = documentSnapshot.toObject(Andeqan.class);
                    if (firebaseAuth.getCurrentUser().getUid().equals(mUid)){
                        toolbar.setTitle("Your collections");
                    }else {
                        toolbar.setTitle(andeqan.getUsername() + " collections");
                    }
                }

            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        documentSnapshots.clear();
        recyclerView();
        setCollections();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void recyclerView(){
        profileCollectionsAdapter = new ProfileCollectionsAdapter(ProfileCollectionsActivity.this);
        layoutManager = new LinearLayoutManager(ProfileCollectionsActivity.this);
        mCollectionsRecyclerView.setLayoutManager(layoutManager);
        mCollectionsRecyclerView.setAdapter(profileCollectionsAdapter);
        mCollectionsRecyclerView.setHasFixedSize(false);
        mCollectionsRecyclerView.setNestedScrollingEnabled(false);

    }

    @Override
    public void onRefresh() {
        setNextCollections();
    }

    private void setCollections(){
        profileCollectionsQuery
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

                    Log.d("name of collection", "data is present");

                }else {
                    Log.d("name of collection", "data is absent");

                }

            }
        });
    }

    private void setNextCollections(){
        mSwipeRefreshLayout.setRefreshing(true);

        // Get the last visible document
        final int snapshotSize = profileCollectionsAdapter.getItemCount();
        if (snapshotSize == 0){

        }else {
            DocumentSnapshot lastVisible = profileCollectionsAdapter.getSnapshot(snapshotSize - 1);

            //retrieve the first bacth of documentSnapshots
            Query  nextCollectionsQuery = collectionCollection.orderBy("time", Query.Direction.DESCENDING)
                    .whereEqualTo("user_id", mUid).startAfter(lastVisible)
                    .limit(TOTAL_ITEMS);


            nextCollectionsQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
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
                        mSwipeRefreshLayout.setRefreshing(false);
                    }else {
                        mSwipeRefreshLayout.setRefreshing(false);

                    }
                }
            });
        }

    }


    protected void onDocumentAdded(DocumentChange change) {
        collectionsIds.add(change.getDocument().getId());
        documentSnapshots.add(change.getDocument());
        profileCollectionsAdapter.setProfileCollections(documentSnapshots);
        profileCollectionsAdapter.notifyItemInserted(documentSnapshots.size() -1);
        profileCollectionsAdapter.getItemCount();

    }

    protected void onDocumentModified(DocumentChange change) {
       try {
           if (change.getOldIndex() == change.getNewIndex()) {
               // Item changed but remained in same position
               documentSnapshots.set(change.getOldIndex(), change.getDocument());
               profileCollectionsAdapter.notifyItemChanged(change.getOldIndex());
           } else {
               // Item changed and changed position
               documentSnapshots.remove(change.getOldIndex());
               documentSnapshots.add(change.getNewIndex(), change.getDocument());
               profileCollectionsAdapter.notifyItemRangeChanged(0, documentSnapshots.size());
           }
       }catch (Exception e){
           e.printStackTrace();
       }
    }

    protected void onDocumentRemoved(DocumentChange change) {
        try {
            documentSnapshots.remove(change.getOldIndex());
            profileCollectionsAdapter.notifyItemRemoved(change.getOldIndex());
            profileCollectionsAdapter.notifyItemRangeChanged(0, documentSnapshots.size());
        }catch (Exception e){
            e.printStackTrace();
        }
    }



}

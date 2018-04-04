package com.andeqa.andeqa.people;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.R;
import com.andeqa.andeqa.comments.CommentsAdapter;
import com.andeqa.andeqa.models.Andeqan;
import com.andeqa.andeqa.models.Relation;
import com.andeqa.andeqa.models.Timeline;
import com.andeqa.andeqa.profile.ProfileActivity;
import com.andeqa.andeqa.utils.EndlessRecyclerOnScrollListener;
import com.firebase.ui.common.ChangeEventType;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.firebase.ui.firestore.ObservableSnapshotArray;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class FollowersActivity extends AppCompatActivity {
    //firestore references
    private CollectionReference relationsReference;
    private Query followersQuery;
    private FirebaseAuth firebaseAuth;
    private static final String TAG = FollowersActivity.class.getSimpleName();
    private static final String EXTRA_USER_UID = "uid";
    private String mUid;
    private static final int TOTAL_ITEMS = 25;
    private FollowersAdapter followersAdapter;

    private List<String> followersIds = new ArrayList<>();
    private List<DocumentSnapshot> documentSnapshots = new ArrayList<>();

    @Bind(R.id.followersRecyclerView)RecyclerView mFollowersRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_followers);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //ON NAVIGATING BACK
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() != null){
            mUid = getIntent().getStringExtra(EXTRA_USER_UID);
            if(mUid == null){
                throw new IllegalArgumentException("pass an EXTRA_USER_UID");
            }

            relationsReference = FirebaseFirestore.getInstance().collection(Constants.RELATIONS);
            followersQuery = relationsReference.document("followers").collection(mUid);

            setRecyclerView();
            setCollections();

            mFollowersRecyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener() {
                @Override
                public void onLoadMore() {
                    setNextCollections();
                }
            });

        }
    }

    private void setRecyclerView(){
        followersAdapter = new FollowersAdapter(this);
        mFollowersRecyclerView.setAdapter(followersAdapter);
        mFollowersRecyclerView.setHasFixedSize(false);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setAutoMeasureEnabled(true);
        mFollowersRecyclerView.setLayoutManager(layoutManager);
    }



    private void setCollections(){
        followersQuery.orderBy("uid").limit(TOTAL_ITEMS)
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
                        }

                    }
                });
    }

    private void setNextCollections(){
        // Get the last visible document
        final int snapshotSize = followersAdapter.getItemCount();
        DocumentSnapshot lastVisible = followersAdapter.getSnapshot(snapshotSize - 1);

        //retrieve the first bacth of documentSnapshots
        Query nextSellingQuery = relationsReference.document("followers").collection(mUid)
                .orderBy("uid")
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
                }
            }
        });
    }

    protected void onDocumentAdded(DocumentChange change) {
        followersIds.add(change.getDocument().getId());
        documentSnapshots.add(change.getDocument());
        followersAdapter.setProfileFollowers(documentSnapshots);
        followersAdapter.notifyItemInserted(documentSnapshots.size() -1);
        followersAdapter.getItemCount();

    }

    protected void onDocumentModified(DocumentChange change) {
        if (change.getOldIndex() == change.getNewIndex()) {
            // Item changed but remained in same position
            documentSnapshots.set(change.getOldIndex(), change.getDocument());
            followersAdapter.notifyItemChanged(change.getOldIndex());
        } else {
            // Item changed and changed position
            documentSnapshots.remove(change.getOldIndex());
            documentSnapshots.add(change.getNewIndex(), change.getDocument());
            followersAdapter.notifyItemRangeChanged(0, documentSnapshots.size());
        }
    }

    protected void onDocumentRemoved(DocumentChange change) {
        documentSnapshots.remove(change.getOldIndex());
        followersAdapter.notifyItemRemoved(change.getOldIndex());
        followersAdapter.notifyItemRangeChanged(0, documentSnapshots.size());
    }



    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }
}

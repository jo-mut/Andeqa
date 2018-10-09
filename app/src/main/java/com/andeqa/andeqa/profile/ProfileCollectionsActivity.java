package com.andeqa.andeqa.profile;

import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.R;
import com.andeqa.andeqa.models.Andeqan;
import com.andeqa.andeqa.utils.EndlesssStaggeredRecyclerOnScrollListener;
import com.andeqa.andeqa.utils.ItemOffsetDecoration;
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

import javax.annotation.Nullable;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ProfileCollectionsActivity extends AppCompatActivity{
    @Bind(R.id.collectionsRecyclerView)RecyclerView mCollectionsRecyclerView;
    private static final String TAG = ProfileCollectionsActivity.class.getSimpleName();

    //firestore reference
    private CollectionReference collectionsCollection;
    private Query collectionsQuery;
    private CollectionReference usersReference;

    //firebase auth
    private FirebaseAuth firebaseAuth;
    //firestore adapters
    private ProfileCollectionsAdapter profileCollectionsAdapter;
    private int TOTAL_ITEMS = 10;
    private StaggeredGridLayoutManager layoutManager;
    private static final String EXTRA_USER_UID = "uid";
    private String mUid;
    private List<String> mSnapshotsIds = new ArrayList<>();
    private List<DocumentSnapshot> mSnapshots = new ArrayList<>();
    private ItemOffsetDecoration itemOffsetDecoration;
    private FragmentManager fragmentManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_collections);
        ButterKnife.bind(this);
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
        mUid = getIntent().getStringExtra(EXTRA_USER_UID);
        collectionsCollection = FirebaseFirestore.getInstance().collection(Constants.COLLECTIONS);
        collectionsQuery = collectionsCollection.orderBy("time", Query.Direction.DESCENDING);
        mCollectionsRecyclerView.addOnScrollListener(new EndlesssStaggeredRecyclerOnScrollListener() {
            @Override
            public void onLoadMore() {
                setNextCollections();
            }
        });

        usersReference = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);
        usersReference.document(mUid).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {

                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (documentSnapshot.exists()){
                    Andeqan andeqan = documentSnapshot.toObject(Andeqan.class);
                    final String username = andeqan.getUsername();
                    toolbar.setTitle(username + "'s" + " collections");

                }
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStart() {
        super.onStart();
        loadData();
        mCollectionsRecyclerView.addItemDecoration(itemOffsetDecoration);

    }

    @Override
    public void onStop() {
        super.onStop();
        mCollectionsRecyclerView.removeItemDecoration(itemOffsetDecoration);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void loadData(){
        mSnapshots.clear();
        setRecyclerView();
        setCollections();
    }

    private void setRecyclerView(){
        // RecyclerView
        profileCollectionsAdapter = new ProfileCollectionsAdapter(this);
        mCollectionsRecyclerView.setAdapter(profileCollectionsAdapter);
        mCollectionsRecyclerView.setHasFixedSize(false);
        layoutManager = new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL);
        itemOffsetDecoration = new ItemOffsetDecoration(this, R.dimen.item_off_set);
        mCollectionsRecyclerView.setLayoutManager(layoutManager);
    }

    private void setCollections(){
        collectionsQuery.whereEqualTo("user_id", mUid)
                .limit(TOTAL_ITEMS).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (!documentSnapshots.isEmpty()) {
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
        final int snapshotSize = profileCollectionsAdapter.getItemCount();

        if (snapshotSize!= 0){
            DocumentSnapshot lastVisible = profileCollectionsAdapter.getSnapshot(snapshotSize - 1);

            //retrieve the first bacth of posts
            Query nextSinglesQuery = collectionsCollection.orderBy("time", Query.Direction.DESCENDING)
                    .whereEqualTo("user_id", mUid).startAfter(lastVisible)
                    .limit(TOTAL_ITEMS);

            nextSinglesQuery.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot documentSnapshots) {
                    if (!documentSnapshots.isEmpty()){
                        //retrieve the first bacth of posts
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
        mSnapshotsIds.add(change.getDocument().getId());
        mSnapshots.add(change.getDocument());
        profileCollectionsAdapter.setProfileCollections(mSnapshots);
        profileCollectionsAdapter.notifyItemInserted(mSnapshots.size() -1);
        profileCollectionsAdapter.getItemCount();

    }

    protected void onDocumentModified(DocumentChange change) {
        if (change.getOldIndex() == change.getNewIndex()) {
            // Item changed but remained in same position
            mSnapshots.set(change.getOldIndex(), change.getDocument());
            profileCollectionsAdapter.notifyItemChanged(change.getOldIndex());
        } else {
            // Item changed and changed position
            mSnapshots.remove(change.getOldIndex());
            mSnapshots.add(change.getNewIndex(), change.getDocument());
            profileCollectionsAdapter.notifyItemRangeChanged(0, mSnapshots.size());
        }
    }

    protected void onDocumentRemoved(DocumentChange change) {
        try{
            mSnapshots.remove(change.getOldIndex());
            profileCollectionsAdapter.notifyItemRemoved(change.getOldIndex());
            profileCollectionsAdapter.notifyItemRangeChanged(0, mSnapshots.size());
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    @Override
    public void onResume() {
        super.onResume();
    }


}

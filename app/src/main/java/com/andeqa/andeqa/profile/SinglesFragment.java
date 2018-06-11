package com.andeqa.andeqa.profile;


import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.R;
import com.andeqa.andeqa.collections.CollectionPostsActivity;
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
public class SinglesFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener{

    @Bind(R.id.singlesRecyclerView)RecyclerView mSinglesRecyclerView;
    @Bind(R.id.swipeRefreshLayout)SwipeRefreshLayout mSwipeRefreshLayout;
    private static final String TAG = CollectionPostsActivity.class.getSimpleName();


    //firestore reference
    private CollectionReference postsCollection;
    private Query profileSinglesQuery;
    //firebase auth
    private FirebaseAuth firebaseAuth;
    //firestore adapters
    private ProfilePostsAdapter profilePostsAdapter;
    private static final String KEY_LAYOUT_POSITION = "layout pooition";
    private Parcelable recyclerViewState;
    private  static final int MAX_WIDTH = 400;
    private static final int MAX_HEIGHT = 400;
    private int TOTAL_ITEMS = 10;
    private LinearLayoutManager layoutManager;
    private static final String EXTRA_USER_UID = "uid";

    private String collectionId;
    private String mUid;
    private String mSource;
    private static final String COLLECTION_ID = "collection id";
    private List<String> mSnapshotsIds = new ArrayList<>();
    private List<DocumentSnapshot> mSnapshots = new ArrayList<>();



    public SinglesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_singles, container, false);
        ButterKnife.bind(this, view);
        //initialize click listener
        //FIREBASE AUTH
        firebaseAuth = FirebaseAuth.getInstance();
        mSwipeRefreshLayout.setOnRefreshListener(this);


        if (firebaseAuth.getCurrentUser()!= null){

            mUid = getActivity().getIntent().getStringExtra(EXTRA_USER_UID);

            postsCollection = FirebaseFirestore.getInstance().collection(Constants.POSTS);
            profileSinglesQuery = postsCollection.orderBy("time", Query.Direction.DESCENDING)
                    .whereEqualTo("type", "single")
                    .whereEqualTo("user_id", mUid).limit(TOTAL_ITEMS);

        }
         return view;
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStart() {
        super.onStart();
        mSnapshots.clear();
        setRecyclerView();
        setSingles();
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
    public void onRefresh() {
        setNextCollectionPosts();
    }

    private void setRecyclerView(){
        // RecyclerView
        profilePostsAdapter = new ProfilePostsAdapter(getContext());
        mSinglesRecyclerView.setAdapter(profilePostsAdapter);
        mSinglesRecyclerView.setHasFixedSize(false);
        layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        mSinglesRecyclerView.setLayoutManager(layoutManager);
        mSinglesRecyclerView.setNestedScrollingEnabled(false);
    }

    private void setSingles(){
        profileSinglesQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
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


    private void setNextCollectionPosts(){
        // Get the last visible document
        mSwipeRefreshLayout.setRefreshing(true);
        final int snapshotSize = profilePostsAdapter.getItemCount();

        if (snapshotSize == 0){
            mSwipeRefreshLayout.setRefreshing(false);
        }else {
            DocumentSnapshot lastVisible = profilePostsAdapter.getSnapshot(snapshotSize - 1);

            //retrieve the first bacth of posts
            Query nextSinglesQuery = postsCollection.orderBy("time", Query.Direction.DESCENDING)
                    .whereEqualTo("type", "single")
                    .whereEqualTo("user_id", mUid).startAfter(lastVisible)
                    .limit(TOTAL_ITEMS);

            nextSinglesQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                    if (e != null) {
                        Log.w(TAG, "Listen error", e);
                        return;
                    }

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
                        mSwipeRefreshLayout.setRefreshing(false);
                    }else {
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                }
            });
        }

    }

    protected void onDocumentAdded(DocumentChange change) {
        mSnapshotsIds.add(change.getDocument().getId());
        mSnapshots.add(change.getDocument());
        profilePostsAdapter.setCollectionPosts(mSnapshots);
        profilePostsAdapter.notifyItemInserted(mSnapshots.size() -1);
        profilePostsAdapter.getItemCount();

    }

    protected void onDocumentModified(DocumentChange change) {
        if (change.getOldIndex() == change.getNewIndex()) {
            // Item changed but remained in same position
            mSnapshots.set(change.getOldIndex(), change.getDocument());
            profilePostsAdapter.notifyItemChanged(change.getOldIndex());
        } else {
            // Item changed and changed position
            mSnapshots.remove(change.getOldIndex());
            mSnapshots.add(change.getNewIndex(), change.getDocument());
            profilePostsAdapter.notifyItemRangeChanged(0, mSnapshots.size());
        }
    }

    protected void onDocumentRemoved(DocumentChange change) {
        try{
            mSnapshots.remove(change.getOldIndex());
            profilePostsAdapter.notifyItemRemoved(change.getOldIndex());
            profilePostsAdapter.notifyItemRangeChanged(0, mSnapshots.size());
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    @Override
    public void onResume() {
        super.onResume();

    }

}

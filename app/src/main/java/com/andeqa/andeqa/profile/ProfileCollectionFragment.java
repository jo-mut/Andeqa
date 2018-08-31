package com.andeqa.andeqa.profile;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.R;
import com.andeqa.andeqa.collections.CollectionPostsActivity;
import com.andeqa.andeqa.utils.EndlessRecyclerOnScrollListener;
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

import butterknife.Bind;
import butterknife.ButterKnife;


public class ProfileCollectionFragment extends Fragment{
    @Bind(R.id.collectionsRecyclerView)RecyclerView mCollectionsRecyclerView;

    private static final String TAG = CollectionPostsActivity.class.getSimpleName();

    //firestore reference
    private CollectionReference collectionsCollection;
    private Query collectionsQuery;

    //firebase auth
    private FirebaseAuth firebaseAuth;
    //firestore adapters
    private ProfileCollectionAdapter profileCollectionAdapter;
    private int TOTAL_ITEMS = 10;
    private StaggeredGridLayoutManager layoutManager;
    private static final String EXTRA_USER_UID = "uid";
    private String mUid;
    private List<String> mSnapshotsIds = new ArrayList<>();
    private List<DocumentSnapshot> mSnapshots = new ArrayList<>();
    private ItemOffsetDecoration itemOffsetDecoration;


    public ProfileCollectionFragment() {
        // Required empty public constructor
    }


    public static ProfileCollectionFragment newInstance(String param1, String param2) {
        ProfileCollectionFragment fragment = new ProfileCollectionFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile_collection, container, false);
        ButterKnife.bind(this, view);
        //initialize click listener
        //FIREBASE AUTH
        firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser()!= null){

            mUid = getActivity().getIntent().getStringExtra(EXTRA_USER_UID);
            Log.d("fragment uid", mUid);

            collectionsCollection = FirebaseFirestore.getInstance().collection(Constants.USER_COLLECTIONS);
            collectionsQuery = collectionsCollection.orderBy("time", Query.Direction.DESCENDING)
                    .whereEqualTo("user_id", mUid)
                    .limit(TOTAL_ITEMS);

            mCollectionsRecyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener() {
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
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStart() {
        super.onStart();
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
        profileCollectionAdapter = new ProfileCollectionAdapter(getContext());
        mCollectionsRecyclerView.setAdapter(profileCollectionAdapter);
        mCollectionsRecyclerView.setHasFixedSize(false);
        layoutManager = new StaggeredGridLayoutManager(1,StaggeredGridLayoutManager.HORIZONTAL);
        itemOffsetDecoration = new ItemOffsetDecoration(getContext(), R.dimen.item_off_set);
        mCollectionsRecyclerView.setLayoutManager(layoutManager);
    }

    private void setCollections(){
        collectionsQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
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
        final int snapshotSize = profileCollectionAdapter.getItemCount();

        if (snapshotSize != 0){
            DocumentSnapshot lastVisible = profileCollectionAdapter.getSnapshot(snapshotSize - 1);

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
        profileCollectionAdapter.setProfileCollections(mSnapshots);
        profileCollectionAdapter.notifyItemInserted(mSnapshots.size() -1);
        profileCollectionAdapter.getItemCount();

    }

    protected void onDocumentModified(DocumentChange change) {
        if (change.getOldIndex() == change.getNewIndex()) {
            // Item changed but remained in same position
            mSnapshots.set(change.getOldIndex(), change.getDocument());
            profileCollectionAdapter.notifyItemChanged(change.getOldIndex());
        } else {
            // Item changed and changed position
            mSnapshots.remove(change.getOldIndex());
            mSnapshots.add(change.getNewIndex(), change.getDocument());
            profileCollectionAdapter.notifyItemRangeChanged(0, mSnapshots.size());
        }
    }

    protected void onDocumentRemoved(DocumentChange change) {
        try{
            mSnapshots.remove(change.getOldIndex());
            profileCollectionAdapter.notifyItemRemoved(change.getOldIndex());
            profileCollectionAdapter.notifyItemRangeChanged(0, mSnapshots.size());
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    @Override
    public void onResume() {
        super.onResume();

    }


}

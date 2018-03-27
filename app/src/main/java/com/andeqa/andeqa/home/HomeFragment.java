package com.andeqa.andeqa.home;


import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
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

import static android.media.CamcorderProfile.get;

/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment{
    @Bind(R.id.postsRecyclerView)RecyclerView postsRecyclerView;
    @Bind(R.id.placeHolderRelativeLayout)RelativeLayout mPlaceHolderRelativeLayout;

    private static final String TAG = HomeFragment.class.getSimpleName();
    private static final String KEY_LAYOUT_POSITION = "layout position";
    private Parcelable recyclerViewState;
    //firestore reference
    private CollectionReference postsCollection;
    private Query randomPostsQuery;
    private Query nextRandomQuery;

    //firebase auth
    private FirebaseAuth firebaseAuth;
    //adapters
    private PostsAdapter postsAdapter;
    private LinearLayoutManager layoutManager;
    private int TOTAL_ITEMS = 10;
    private List<String> mSnapshotsIds = new ArrayList<>();
    private List<DocumentSnapshot> mSnapshots = new ArrayList<>();

    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        ButterKnife.bind(this, view);

        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null){
            //firestore
            postsCollection = FirebaseFirestore.getInstance().collection(Constants.POSTS);
            randomPostsQuery = postsCollection.orderBy("time", Query.Direction.DESCENDING)
                    .limit(TOTAL_ITEMS);

            postsRecyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener() {
                @Override
                public void onLoadMore() {
                    setNextPosts();
                }
            });

        }

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setRecyclerView();
        setPosts();

    }

    private void setRecyclerView(){
        postsAdapter = new PostsAdapter(getContext());
        postsRecyclerView.setAdapter(postsAdapter);
        postsAdapter.notifyDataSetChanged();
        postsRecyclerView.setHasFixedSize(false);
        layoutManager = new LinearLayoutManager(getContext());
        postsRecyclerView.setLayoutManager(layoutManager);

    }

    private void setPosts(){
        randomPostsQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
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

    private void setNextPosts(){
        // Get the last visible document
        final int snapshotSize = postsAdapter.getItemCount();
        DocumentSnapshot lastVisible = postsAdapter.getSnapshot(snapshotSize - 1);

        //retrieve the first bacth of mSnapshots
        nextRandomQuery = postsCollection.orderBy("time", Query.Direction.DESCENDING)
                .startAfter(lastVisible)
                .limit(TOTAL_ITEMS);

        nextRandomQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (!documentSnapshots.isEmpty()){
                    //retrieve the first bacth of mSnapshots
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
        mSnapshotsIds.add(change.getDocument().getId());
        mSnapshots.add(change.getDocument());
        postsAdapter.setRandomPosts(mSnapshots);
        postsAdapter.notifyItemInserted(mSnapshots.size() -1);
        postsAdapter.getItemCount();

    }

    protected void onDocumentModified(DocumentChange change) {
        if (change.getOldIndex() == change.getNewIndex()) {
            // Item changed but remained in same position
            mSnapshots.set(change.getOldIndex(), change.getDocument());
            postsAdapter.notifyItemChanged(change.getOldIndex());
        } else {
            // Item changed and changed position
            mSnapshots.remove(change.getOldIndex());
            mSnapshots.add(change.getNewIndex(), change.getDocument());
            postsAdapter.notifyItemRangeChanged(0, mSnapshots.size());
        }
    }

    protected void onDocumentRemoved(DocumentChange change) {
        mSnapshots.remove(change.getOldIndex());
        postsAdapter.notifyItemRemoved(change.getOldIndex());
        postsAdapter.notifyItemRangeChanged(0, mSnapshots.size());
    }

}

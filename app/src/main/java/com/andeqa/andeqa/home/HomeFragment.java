package com.andeqa.andeqa.home;


import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.R;
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
public class HomeFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener{
    @Bind(R.id.postsRecyclerView)RecyclerView postsRecyclerView;
    @Bind(R.id.swipeRefreshLayout)SwipeRefreshLayout mSwipeRefreshLayout;
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
    private List<String> postsIds = new ArrayList<>();
    private List<DocumentSnapshot> posts = new ArrayList<>();

    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        ButterKnife.bind(this, view);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null){
            //firestore
            postsCollection = FirebaseFirestore.getInstance().collection(Constants.POSTS);
            randomPostsQuery = postsCollection.orderBy("time", Query.Direction.ASCENDING)
                    .limit(TOTAL_ITEMS);

            setRecyclerView();
            setPosts();

        }

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    public void onRefresh() {
        setNextPosts();
    }

    private void setRecyclerView(){
        postsAdapter = new PostsAdapter(getContext());
        postsRecyclerView.setAdapter(postsAdapter);
        postsAdapter.notifyDataSetChanged();
        postsRecyclerView.setHasFixedSize(false);
        layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
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
        mSwipeRefreshLayout.setRefreshing(true);
        final int snapshotSize = postsAdapter.getItemCount();

        if (snapshotSize == 0){
            mSwipeRefreshLayout.setRefreshing(false);
        }else {
            DocumentSnapshot lastVisible = postsAdapter.getSnapshot(snapshotSize - 1);

            //retrieve the first bacth of posts
            nextRandomQuery = postsCollection.orderBy("time", Query.Direction.ASCENDING)
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
        postsIds.add(change.getDocument().getId());
        posts.add(change.getDocument());
        postsAdapter.setRandomPosts(posts);
        postsAdapter.notifyItemInserted(posts.size() -1);
        postsAdapter.getItemCount();

    }

    protected void onDocumentModified(DocumentChange change) {
        if (change.getOldIndex() == change.getNewIndex()) {
            // Item changed but remained in same position
            posts.set(change.getOldIndex(), change.getDocument());
            postsAdapter.notifyItemChanged(change.getOldIndex());
        } else {
            // Item changed and changed position
            posts.remove(change.getOldIndex());
            posts.add(change.getNewIndex(), change.getDocument());
            postsAdapter.notifyItemRangeChanged(0, posts.size());
        }
    }

    protected void onDocumentRemoved(DocumentChange change) {
        posts.remove(change.getOldIndex());
        postsAdapter.notifyItemRemoved(change.getOldIndex());
        postsAdapter.notifyItemRangeChanged(0, posts.size());
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        posts.clear();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        postsAdapter.removeListener();
    }
}

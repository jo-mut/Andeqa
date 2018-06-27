package com.andeqa.andeqa.home;


import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.R;
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

import static android.media.CamcorderProfile.get;

/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment{
    @Bind(R.id.postsRecyclerView)RecyclerView postsRecyclerView;
    @Bind(R.id.placeHolderRelativeLayout)RelativeLayout mPlaceHolderRelativeLayout;
    @Bind(R.id.progressBarRelativeLayout)RelativeLayout mProgressBarRelativeLayout;

    private static final String TAG = HomeFragment.class.getSimpleName();
    private static final String LAYOUT_STATE = "layout position";
    private static final String RECYCLER_VIEW_ITEMS = "items";
    private Parcelable parcelable = null;
    private Bundle savedState = null;
    private int firstVisiblePosition = RecyclerView.NO_POSITION;
    //firestore reference
    private CollectionReference postsCollection;
    private CollectionReference postCollections;
    private CollectionReference collectionReference;
    private Query randomPostsQuery;
    private Query nextRandomQuery;

    //firebase auth
    private FirebaseAuth firebaseAuth;
    //adapters
    private PostsAdapter postsAdapter;
    private StaggeredGridLayoutManager layoutManager;
    private int TOTAL_ITEMS = 10;
    private List<String> postsIds = new ArrayList<>();
    private List<DocumentSnapshot> posts = new ArrayList<>();
    private ArrayList<Parcelable> snapshots = new ArrayList<>();
    int spanCount = 2; // 3 columns
    int spacing = 5; // 50px
    boolean includeEdge = true;
    private ItemOffsetDecoration itemOffsetDecoration;


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
            randomPostsQuery = postsCollection.orderBy("random_number", Query.Direction.ASCENDING)
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


//    @Override
//    public void onSaveInstanceState(@NonNull Bundle outState) {
//        parcelable = postsRecyclerView.getLayoutManager().onSaveInstanceState();
//        outState.putParcelable(LAYOUT_STATE, parcelable);
//        outState.putParcelableArrayList(RECYCLER_VIEW_ITEMS, snapshots);
//        super.onSaveInstanceState(outState);
//    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadData();
//        if (savedInstanceState != null && savedState == null){
//            savedState = savedInstanceState.getParcelable(LAYOUT_STATE);
//            postsRecyclerView.getLayoutManager().onRestoreInstanceState(savedState);
//            Log.d("saved state", "present");
//        }
//
//        if (savedState != null) {
//            Log.d("saved state bundle", "present");
//            savedState.getParcelable(LAYOUT_STATE);
//            postsRecyclerView.getLayoutManager()
//                    .onRestoreInstanceState(savedState);
//        }
//
//        parcelable = null;
    }

//    @Override
//    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
//        super.onViewStateRestored(savedInstanceState);
//
//    }

    @Override
    public void onStart() {
        super.onStart();
        postsRecyclerView.addItemDecoration(itemOffsetDecoration);

    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
//        savedState = saveState();
    }

    @Override
    public void onStop() {
        super.onStop();
        postsRecyclerView.removeItemDecoration(itemOffsetDecoration);
    }


//    public Bundle saveState(){
//        Bundle bundle = new Bundle();
//        parcelable = postsRecyclerView.getLayoutManager().onSaveInstanceState();
//        bundle.putParcelable(LAYOUT_STATE, parcelable);
//        bundle.putParcelableArrayList(RECYCLER_VIEW_ITEMS, snapshots);
//        return bundle;
//    }

    public void loadData(){
        posts.clear();
        setRecyclerView();
        setPosts();
    }

    private void setRecyclerView(){
        postsAdapter = new PostsAdapter(getContext());
        postsRecyclerView.setAdapter(postsAdapter);
        postsRecyclerView.setHasFixedSize(false);
        layoutManager = new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL);
        itemOffsetDecoration = new ItemOffsetDecoration(getContext(), R.dimen.item_off_set);
        postsRecyclerView.setLayoutManager(layoutManager);

    }


    private void setPosts(){
        randomPostsQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot documentSnapshots,
                                @Nullable FirebaseFirestoreException e) {

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

                    Log.d("size of documents", documentSnapshots.size() + "");
                }
            }
        });
    }

    private void setNextPosts(){
        // Get the last visible document
        final int snapshotSize = postsAdapter.getItemCount();
        if (snapshotSize == 0){
            //do nothing
        }else {
            DocumentSnapshot lastVisible = postsAdapter.getSnapshot(snapshotSize - 1);
            //retrieve the first bacth of posts
            nextRandomQuery = postsCollection.orderBy("random_number", Query.Direction.ASCENDING)
                    .startAfter(lastVisible)
                    .limit(TOTAL_ITEMS);

            nextRandomQuery.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
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
        postsIds.add(change.getDocument().getId());
        posts.add(change.getDocument());
        postsAdapter.setRandomPosts(posts);
        postsAdapter.notifyItemInserted(posts.size() -1);
        postsAdapter.getItemCount();

    }

    protected void onDocumentModified(DocumentChange change) {
        try {
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
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    protected void onDocumentRemoved(DocumentChange change) {
       try {
           posts.remove(change.getOldIndex());
           postsAdapter.notifyItemRemoved(change.getOldIndex());
           postsAdapter.notifyItemRangeChanged(0, posts.size());
       }catch (Exception e) {
           e.printStackTrace();
       }
    }
    
}

package com.andeqa.andeqa.home;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.R;
import com.andeqa.andeqa.models.Collection;
import com.andeqa.andeqa.models.Post;
import com.andeqa.andeqa.models.Relation;
import com.andeqa.andeqa.search.SearchPostsActivity;
import com.andeqa.andeqa.utils.EndlesssStaggeredRecyclerOnScrollListener;
import com.andeqa.andeqa.utils.ItemOffsetDecoration;
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
public class ExploreFragment extends Fragment {
    @Bind(R.id.bestPostsRecyclerView)RecyclerView exploreRecyclerView;
    private static final String TAG = ExploreFragment.class.getSimpleName();
    //firestore reference
    private CollectionReference postsCollectionReference;
    private CollectionReference followingPeopleReference;
    private CollectionReference followingCollectionReference;
    //firebase auth
    private FirebaseAuth firebaseAuth;
    //adapters
    private PostsAdapter postsAdapter;
    private StaggeredGridLayoutManager layoutManager;
    private int TOTAL_ITEMS = 10;
    private List<String> postsIds = new ArrayList<>();
    private List<DocumentSnapshot> snapshots = new ArrayList<>();
    private ItemOffsetDecoration itemOffsetDecoration;

    public static ExploreFragment newInstance(Collection collection) {
        final ExploreFragment fragment = new ExploreFragment();
        final Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public ExploreFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_explore, container, false);
        // Inflate the layout for this fragment
        ButterKnife.bind(this, view);
        firebaseAuth = FirebaseAuth.getInstance();
        //firestore
        postsCollectionReference = FirebaseFirestore.getInstance().collection(Constants.POSTS);
        followingPeopleReference = FirebaseFirestore.getInstance().collection(Constants.PEOPLE_RELATIONS);
        followingCollectionReference = FirebaseFirestore.getInstance().collection(Constants.COLLECTION_RELATIONS);

        exploreRecyclerView.addOnScrollListener(new EndlesssStaggeredRecyclerOnScrollListener() {
            @Override
            public void onLoadMore() {
                getNextExplorePosts();
            }
        });

        return view;
    }

//    @Override
//    public void onRefresh() {
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                getNextExplorePosts();
//            }
//        }, 1000);
//    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onCreate(@android.support.annotation.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.explore_menu, menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_search){
            Intent intent =  new Intent(getActivity(), SearchPostsActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onResume() {
        super.onResume();
        snapshots.clear();
        setRecyclerView();
        getExplorePosts();
        exploreRecyclerView.addItemDecoration(itemOffsetDecoration);

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
    }

    @Override
    public void onStop() {
        super.onStop();
        exploreRecyclerView.removeItemDecoration(itemOffsetDecoration);
    }


    private void setRecyclerView(){
        postsAdapter = new PostsAdapter(getActivity());
        postsAdapter.setHasStableIds(true);
        exploreRecyclerView.setHasFixedSize(false);
        exploreRecyclerView.setAdapter(postsAdapter);
        layoutManager = new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL);
        itemOffsetDecoration = new ItemOffsetDecoration(getContext(), R.dimen.item_off_set);
        exploreRecyclerView.setLayoutManager(layoutManager);

    }


    private void getExplorePosts(){
        postsCollectionReference.limit(TOTAL_ITEMS)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot documentSnapshots,
                                @Nullable FirebaseFirestoreException e) {

                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (!documentSnapshots.isEmpty()) {
                    for (final DocumentChange documentChange : documentSnapshots.getDocumentChanges()) {
                        Post post = documentChange.getDocument().toObject(Post.class);
                        final String userId = post.getUser_id();
                        final String collectionId = post.getCollection_id();
                        switch (documentChange.getType()) {
                            case ADDED:
                                followingPeopleReference.document("following")
                                        .collection(firebaseAuth.getCurrentUser().getUid())
                                        .document(userId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                    @Override
                                    public void onEvent(@javax.annotation.Nullable DocumentSnapshot documentSnapshot,
                                                        @javax.annotation.Nullable FirebaseFirestoreException e) {
                                        if (e != null) {
                                            Log.w(TAG, "Listen error", e);
                                            return;
                                        }

                                        if (!documentSnapshot.exists()){
                                            followingCollectionReference.document("following")
                                                    .collection(collectionId)
                                                    .document(firebaseAuth.getCurrentUser().getUid())
                                                    .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                                        @Override
                                                        public void onEvent(@javax.annotation.Nullable DocumentSnapshot documentSnapshot,
                                                                            @javax.annotation.Nullable FirebaseFirestoreException e) {
                                                            if (e != null) {
                                                                Log.w(TAG, "Listen error", e);
                                                                return;
                                                            }

                                                            if (!documentSnapshot.exists()){
                                                                if (!userId.equals(firebaseAuth.getCurrentUser().getUid())){
                                                                    onDocumentAdded(documentChange);
                                                                }
                                                            }
                                                        }
                                                    });
                                        }
                                    }
                                });

                                break;
                            case MODIFIED:
                                onDocumentModified(documentChange);
                                break;
                            case REMOVED:
                                onDocumentRemoved(documentChange);
                                break;
                        }

                    }

                }
            }
        });

    }


    private void getNextExplorePosts(){
        final DocumentSnapshot lastVisible = snapshots.get(snapshots.size() - 1);
        postsCollectionReference.startAfter(lastVisible).limit(TOTAL_ITEMS)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot documentSnapshots,
                                        @Nullable FirebaseFirestoreException e) {

                        if (e != null) {
                            Log.w(TAG, "Listen error", e);
                            return;
                        }

                        if (!documentSnapshots.isEmpty()) {
                            for (final DocumentChange documentChange : documentSnapshots.getDocumentChanges()) {
                                Post post = documentChange.getDocument().toObject(Post.class);
                                final String userId = post.getUser_id();
                                final String collectionId = post.getCollection_id();
                                switch (documentChange.getType()) {
                                    case ADDED:
                                        followingPeopleReference.document("following")
                                                .collection(firebaseAuth.getCurrentUser().getUid())
                                                .document(userId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                            @Override
                                            public void onEvent(@javax.annotation.Nullable DocumentSnapshot documentSnapshot,
                                                                @javax.annotation.Nullable FirebaseFirestoreException e) {
                                                if (e != null) {
                                                    Log.w(TAG, "Listen error", e);
                                                    return;
                                                }

                                                if (!documentSnapshot.exists()){
                                                    followingCollectionReference.document("following")
                                                            .collection(collectionId)
                                                            .document(firebaseAuth.getCurrentUser().getUid())
                                                            .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                                                @Override
                                                                public void onEvent(@javax.annotation.Nullable DocumentSnapshot documentSnapshot,
                                                                                    @javax.annotation.Nullable FirebaseFirestoreException e) {
                                                                    if (e != null) {
                                                                        Log.w(TAG, "Listen error", e);
                                                                        return;
                                                                    }

                                                                    if (!documentSnapshot.exists()){
                                                                        if (!userId.equals(firebaseAuth.getCurrentUser().getUid())){
                                                                            onDocumentAdded(documentChange);
                                                                        }
                                                                    }
                                                                }
                                                            });
                                                }
                                            }
                                        });
                                        break;
                                    case MODIFIED:
                                        onDocumentModified(documentChange);
                                        break;
                                    case REMOVED:
                                        onDocumentRemoved(documentChange);
                                        break;
                                }

                            }

                        }
                    }
                });


    }

    protected void onDocumentAdded(DocumentChange change) {
        postsIds.add(change.getDocument().getId());
        snapshots.add(change.getDocument());
        postsAdapter.notifyItemInserted(snapshots.size() - 1);
        postsAdapter.getItemCount();
        postsAdapter.setRandomPosts(snapshots);
        Log.d("posts added explore", snapshots.size() + "");

    }

    protected void onDocumentModified(DocumentChange change) {
        try {
            if (change.getOldIndex() == change.getNewIndex()) {
                // Item changed but remained in same position
                snapshots.set(change.getOldIndex(), change.getDocument());
                postsAdapter.notifyItemChanged(change.getOldIndex());
            } else {
                // Item changed and changed position
                snapshots.remove(change.getOldIndex());
                snapshots.add(change.getNewIndex(), change.getDocument());
                postsAdapter.notifyItemRangeChanged(0, snapshots.size());
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    protected void onDocumentRemoved(DocumentChange change) {
        try {
            snapshots.remove(change.getOldIndex());
            postsAdapter.notifyItemRemoved(change.getOldIndex());
            postsAdapter.notifyItemRangeChanged(0, snapshots.size());
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

}

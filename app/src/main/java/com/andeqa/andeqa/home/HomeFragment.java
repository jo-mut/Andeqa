package com.andeqa.andeqa.home;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.R;
import com.andeqa.andeqa.creation.CreateActivity;
import com.andeqa.andeqa.models.Post;
import com.andeqa.andeqa.utils.BottomReachedListener;
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
public class HomeFragment extends Fragment {
    @Bind(R.id.homeRecyclerView)RecyclerView homeRecyclerView;
    @Bind(R.id.progressBar) ProgressBar progressBar;
    private static final String TAG = HomeFragment.class.getSimpleName();
    //firestore reference
    private CollectionReference postsCollectionReference;
    private CollectionReference followingPeopleReference;
    private CollectionReference followingCollectionReference;
    private CollectionReference usersReference;
    //firebase auth
    private FirebaseAuth firebaseAuth;
    //adapters
    private PostsAdapter postsAdapter;
    private StaggeredGridLayoutManager layoutManager;
    private int TOTAL_ITEMS = 20;
    private List<DocumentSnapshot> snapshots = new ArrayList<>();
    private List<DocumentSnapshot> queriedPosts = new ArrayList<>();
    private List<String> postsIds = new ArrayList<>();
    private List<DocumentSnapshot> list = new ArrayList<>();
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
        //firestore
        postsCollectionReference = FirebaseFirestore.getInstance().collection(Constants.POSTS);
        followingPeopleReference = FirebaseFirestore.getInstance().collection(Constants.PEOPLE_RELATIONS);
        followingCollectionReference = FirebaseFirestore.getInstance().collection(Constants.COLLECTION_RELATIONS);
        usersReference = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);

        return view;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }



    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.home_menu, menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_create){
            Intent intent =  new Intent(getActivity(),CreateActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onStart() {
        super.onStart();
        snapshots.clear();
        queriedPosts.clear();
        list.clear();
        setRecyclerView();
        getHomePosts();
        homeRecyclerView.addItemDecoration(itemOffsetDecoration);

        postsAdapter.setmBottomReachedListener(new BottomReachedListener() {
            @Override
            public void onBottomReached(final int position) {
                progressBar.setVisibility(View.VISIBLE);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        buildNextPostsQuery();
                    }
                },1000);
            }
        });

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
        snapshots.clear();
        queriedPosts.clear();
        list.clear();
    }

    @Override
    public void onStop() {
        super.onStop();
        homeRecyclerView.removeItemDecoration(itemOffsetDecoration);
    }


    private void setRecyclerView(){
        postsAdapter = new PostsAdapter(getActivity());
        postsAdapter.setHasStableIds(true);
        homeRecyclerView.setHasFixedSize(false);
        homeRecyclerView.setAdapter(postsAdapter);
        layoutManager = new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL);
        itemOffsetDecoration = new ItemOffsetDecoration(getContext(), R.dimen.item_off_set);
        homeRecyclerView.setLayoutManager(layoutManager);
    }

    private void getHomePosts(){
        postsCollectionReference.orderBy("time", Query.Direction.DESCENDING)
                .limit(TOTAL_ITEMS).addSnapshotListener(new EventListener<QuerySnapshot>() {
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
                        list.add(documentChange.getDocument());
                        queriedPosts.add(documentChange.getDocument());
                        switch (documentChange.getType()) {
                            case ADDED:
                                if (userId.equals(firebaseAuth.getCurrentUser().getUid())){
                                    onDocumentAdded(documentChange);
                                }else {
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

                                            if (documentSnapshot.exists()){
                                                onDocumentAdded(documentChange);
                                            }else {
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

                                                                if (documentSnapshot.exists()){
                                                                    onDocumentAdded(documentChange);
                                                                }
                                                            }
                                                        });
                                            }
                                        }
                                    });
                                }
                                break;
                            case MODIFIED:
                                onDocumentModified(documentChange);
                                break;
                            case REMOVED:
                                onDocumentRemoved(documentChange);
                                break;
                        }


                    }


                    Log.d("home posts", queriedPosts.size() + "");
                    Log.d("home list posts", list.size() + "");

                    if (list.size() < TOTAL_ITEMS){
                        final DocumentSnapshot lastVisible = queriedPosts.get(queriedPosts.size() - 1);
                        postsCollectionReference.orderBy("time", Query.Direction.DESCENDING)
                                .startAfter(lastVisible).limit(TOTAL_ITEMS)
                                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                    @Override
                                    public void onEvent(@javax.annotation.Nullable QuerySnapshot documentSnapshots,
                                                        @javax.annotation.Nullable FirebaseFirestoreException e) {

                                        if (e != null) {
                                            Log.w(TAG, "Listen error", e);
                                            return;
                                        }

                                        if (!documentSnapshots.isEmpty()){
                                            getNextHomePosts();
                                        }

                                    }
                                });
                    }else {
                        list.clear();
                    }

                }

            }
        });

    }

    private void buildNextPostsQuery(){
        list.clear();
        if (list.size() < TOTAL_ITEMS){
            final DocumentSnapshot lastVisible = queriedPosts.get(queriedPosts.size() - 1);
            postsCollectionReference.orderBy("time", Query.Direction.DESCENDING)
                    .startAfter(lastVisible)
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@javax.annotation.Nullable QuerySnapshot documentSnapshots,
                                    @javax.annotation.Nullable FirebaseFirestoreException e) {

                    if (e != null) {
                        Log.w(TAG, "Listen error", e);
                        return;
                    }

                    if (!documentSnapshots.isEmpty()){
                        getNextHomePosts();
                    }

                }
            });
        }
    }

    private void getNextHomePosts(){
        final DocumentSnapshot lastVisible = queriedPosts.get(queriedPosts.size() - 1);
        postsCollectionReference.orderBy("time", Query.Direction.DESCENDING)
                .startAfter(lastVisible).limit(TOTAL_ITEMS)
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
                                list.add(documentChange.getDocument());
                                queriedPosts.add(documentChange.getDocument());
                                Post post = documentChange.getDocument().toObject(Post.class);
                                final String userId = post.getUser_id();
                                final String collectionId = post.getCollection_id();
                                switch (documentChange.getType()) {
                                    case ADDED:
                                        if (userId.equals(firebaseAuth.getCurrentUser().getUid())){
                                            onDocumentAdded(documentChange);
                                        }else {
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

                                                    if (documentSnapshot.exists()){
                                                        onDocumentAdded(documentChange);
                                                    }else {
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

                                                                        if (documentSnapshot.exists()){
                                                                            onDocumentAdded(documentChange);
                                                                        }
                                                                    }
                                                                });
                                                    }
                                                }
                                            });
                                        }

                                        //hide the click to load more view
                                        progressBar.setVisibility(View.GONE);

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
        postsAdapter.getItemCount();
        postsAdapter.setDefaultsPosts(snapshots);

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

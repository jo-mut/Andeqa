package com.andeqa.andeqa.home;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.R;
import com.andeqa.andeqa.creation.ChooseCreationActivity;
import com.andeqa.andeqa.models.Post;
import com.andeqa.andeqa.models.QueryOptions;
import com.andeqa.andeqa.models.Relation;
import com.andeqa.andeqa.utils.EndlessRecyclerOnScrollListener;
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
public class HomeFragment extends Fragment{
    @Bind(R.id.homeRecyclerView)RecyclerView homeRecyclerView;
    @Bind(R.id.placeHolderRelativeLayout)RelativeLayout mPlaceHolderRelativeLayout;
    private static final String TAG = HomeFragment.class.getSimpleName();
    //firestore reference
    private CollectionReference postsCollection;
    private CollectionReference followingCollection;
    private CollectionReference collectionsPosts;
    private CollectionReference queryOptionsCollections;
    private Query nextRandomQuery;
    private CollectionReference usersReference;
    //firebase auth
    private FirebaseAuth firebaseAuth;
    //adapters
    private PostsAdapter postsAdapter;
    private StaggeredGridLayoutManager layoutManager;
    private int TOTAL_ITEMS = 10;
    private List<String> postsIds = new ArrayList<>();
    private List<DocumentSnapshot> documentSnapshots = new ArrayList<>();
    private List<Post> postOfFollowingCollections = new ArrayList<>();
    private List<Post> postOfFollowingPeople = new ArrayList<>();
    int spanCount = 2; // 3 columns
    int spacing = 5; // 50px
    boolean includeEdge = true;
    private static final String EXTRA_USER_UID = "uid";
    private ItemOffsetDecoration itemOffsetDecoration;
    private FragmentManager fragmentManager;
    private boolean processPosts =  false;
    private boolean processOptions = false;



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
            usersReference = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);
            followingCollection = FirebaseFirestore.getInstance().collection(Constants.PEOPLE);
            queryOptionsCollections = FirebaseFirestore.getInstance().collection(Constants.QUERY_OPTIONS);
            collectionsPosts = FirebaseFirestore.getInstance().collection(Constants.COLLECTIONS_POSTS);
            homeRecyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener() {
                @Override
                public void onLoadMore() {
                  getNextPosts();
                }
            });

        }

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
        inflater.inflate(R.menu.home_menu, menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_create){
            Intent intent =  new Intent(getActivity(), ChooseCreationActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }



    @Override
    public void onStart() {
        super.onStart();
        documentSnapshots.clear();
        setRecyclerView();
        getFirstPosts();
        homeRecyclerView.addItemDecoration(itemOffsetDecoration);

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

    private void getQueryOptionsPosts(){
        queryOptionsCollections.document("options")
                .collection(firebaseAuth.getCurrentUser().getUid())
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable QuerySnapshot documentSnapshots,
                                @javax.annotation.Nullable FirebaseFirestoreException e) {


                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (!documentSnapshots.isEmpty()){
                    for (final DocumentChange change : documentSnapshots.getDocumentChanges()) {
                        QueryOptions queryOptions = change.getDocument().toObject(QueryOptions.class);
                        final String userId = queryOptions.getUser_id();
                        final String followingId = queryOptions.getFollowed_id();
                        final String type = queryOptions.getType();

                        postsCollection.orderBy("time", Query.Direction.DESCENDING)
                                .whereEqualTo("user_id", userId)
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
                                                switch (documentChange.getType()) {
                                                    case ADDED:
                                                        onDocumentAdded(documentChange);
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
                }

            }
        });
    }


    private void getFirstPosts(){
        postsCollection.orderBy("time", Query.Direction.DESCENDING)
                .limit(TOTAL_ITEMS)
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
                                switch (documentChange.getType()) {
                                    case ADDED:
                                        onDocumentAdded(documentChange);
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


    private void getNextPosts(){
        final int snapshotSize = postsAdapter.getItemCount();
        if (snapshotSize != 0){
            final DocumentSnapshot lastVisible = postsAdapter.getSnapshot(snapshotSize - 1);
            postsCollection.orderBy("time", Query.Direction.DESCENDING)
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
                                    switch (documentChange.getType()) {
                                        case ADDED:
                                            onDocumentAdded(documentChange);
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

    }

    private void getPostsOfFollowingPoeple(){
        followingCollection.document("following")
                .collection(firebaseAuth.getCurrentUser().getUid())
                .whereEqualTo("type", "following_user")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@javax.annotation.Nullable QuerySnapshot snapshots,
                                        @javax.annotation.Nullable FirebaseFirestoreException e) {

                        if (e != null) {
                            Log.w(TAG, "Listen error", e);
                            return;
                        }

                        if (!snapshots.isEmpty()){
                            for (final DocumentChange change : snapshots.getDocumentChanges()) {
                                Relation relation = change.getDocument().toObject(Relation.class);
                                final String userId = relation.getFollowed_id();
                                postsCollection.orderBy("time", Query.Direction.DESCENDING)
                                        .whereEqualTo("user_id", userId).limit(TOTAL_ITEMS)
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
                                                switch (documentChange.getType()) {
                                                    case ADDED:
                                                        onDocumentAdded(documentChange);
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
                        }

                    }
                });
    }

    private void getNextPostsOfFollowingPoeple(){
        final int snapshotSize = postsAdapter.getItemCount();
        if (snapshotSize != 0){
            final DocumentSnapshot lastVisible = postsAdapter.getSnapshot(snapshotSize - 1);
            followingCollection.document("following")
                    .collection(firebaseAuth.getCurrentUser().getUid())
                    .whereEqualTo("type", "following_user")
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@javax.annotation.Nullable QuerySnapshot snapshots,
                                            @javax.annotation.Nullable FirebaseFirestoreException e) {

                            if (e != null) {
                                Log.w(TAG, "Listen error", e);
                                return;
                            }

                            if (!snapshots.isEmpty()){
                                for (final DocumentChange change : snapshots.getDocumentChanges()) {
                                    Relation relation = change.getDocument().toObject(Relation.class);
                                    final String userId = relation.getFollowed_id();
                                    Log.d("next user id", userId);
                                    postsCollection.orderBy("time", Query.Direction.DESCENDING)
                                            .whereEqualTo("user_id", userId)
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
                                                            switch (documentChange.getType()) {
                                                                case ADDED:
                                                                    onDocumentAdded(documentChange);
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
                            }

                        }
                    });
        }

    }


    protected void onDocumentAdded(DocumentChange change) {
        postsIds.add(change.getDocument().getId());
        documentSnapshots.add(change.getDocument());
        postsAdapter.notifyItemInserted(documentSnapshots.size() - 1);
        postsAdapter.getItemCount();
        postsAdapter.setRandomPosts(documentSnapshots);
        Log.d("document snapshots", documentSnapshots.size() + "");
    }

    protected void onDocumentModified(DocumentChange change) {
        try {
            if (change.getOldIndex() == change.getNewIndex()) {
                // Item changed but remained in same position
                documentSnapshots.set(change.getOldIndex(), change.getDocument());
                postsAdapter.notifyItemChanged(change.getOldIndex());
            } else {
                // Item changed and changed position
                documentSnapshots.remove(change.getOldIndex());
                documentSnapshots.add(change.getNewIndex(), change.getDocument());
                postsAdapter.notifyItemRangeChanged(0, documentSnapshots.size());
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    protected void onDocumentRemoved(DocumentChange change) {
       try {
           documentSnapshots.remove(change.getOldIndex());
           postsAdapter.notifyItemRemoved(change.getOldIndex());
           postsAdapter.notifyItemRangeChanged(0, documentSnapshots.size());
       }catch (Exception e) {
           e.printStackTrace();
       }
    }

}

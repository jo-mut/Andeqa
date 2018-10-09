package com.andeqa.andeqa.home;


import android.content.Intent;
import android.os.Bundle;
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
import android.widget.RelativeLayout;

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.R;
import com.andeqa.andeqa.creation.ChooseCreationActivity;
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
public class HomeFragment extends Fragment{
    @Bind(R.id.homeRecyclerView)RecyclerView homeRecyclerView;
    @Bind(R.id.placeHolderRelativeLayout)RelativeLayout mPlaceHolderRelativeLayout;
    private static final String TAG = HomeFragment.class.getSimpleName();
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
    private List<DocumentSnapshot> snapshots = new ArrayList<>();
    private List<String> postsIds = new ArrayList<>();
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
            postsCollectionReference = FirebaseFirestore.getInstance().collection(Constants.POSTS);
            followingPeopleReference = FirebaseFirestore.getInstance().collection(Constants.PEOPLE_RELATIONS);
            followingCollectionReference = FirebaseFirestore.getInstance().collection(Constants.COLLECTION_RELATIONS);
            homeRecyclerView.addOnScrollListener(new EndlesssStaggeredRecyclerOnScrollListener() {
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
        menu.clear();
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
        snapshots.clear();
        setRecyclerView();
        getHomePosts();
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

    /**filter the documents that will be added the home feed of the current user*/
    private void getHomePosts(){
        postsCollectionReference.orderBy("time", Query.Direction.DESCENDING)
                .limit(TOTAL_ITEMS)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable final QuerySnapshot documentSnapshots,
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
        final DocumentSnapshot lastVisible = snapshots.get(snapshots.size() - 1);
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

package com.andeqa.andeqa.more;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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
import android.widget.RelativeLayout;

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.R;
import com.andeqa.andeqa.collections.CollectionsFragment;
import com.andeqa.andeqa.models.Collection;
import com.andeqa.andeqa.search.SearchPostsActivity;
import com.andeqa.andeqa.utils.ItemOffsetDecoration;
import com.andeqa.andeqa.utils.BottomReachedListener;
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
public class MoreFragment extends Fragment{
    @Bind(R.id.morePostsRecyclerView)RecyclerView mMorePostsRecyclerView;
    @Bind(R.id.progressBar)ProgressBar progressBar;
    @Bind(R.id.progressRelativeLayout)RelativeLayout mProgressRelativeLayout;

    private static final String TAG = MoreFragment.class.getSimpleName();
    //firestore reference
    private CollectionReference postsCollectionReference;
    private CollectionReference followingPeopleReference;
    private CollectionReference followingCollectionReference;
    //firebase auth
    private FirebaseAuth firebaseAuth;
    //adapters
    private MorePostsAdapter morePostsAdapter;
    private StaggeredGridLayoutManager layoutManager;
    private int TOTAL_ITEMS = 20;
    private List<String> postsIds = new ArrayList<>();
    private List<DocumentSnapshot> snapshots = new ArrayList<>();
    private ItemOffsetDecoration itemOffsetDecoration;

    public static MoreFragment newInstance(Collection collection) {
        final MoreFragment fragment = new MoreFragment();
        final Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public MoreFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_more, container, false);
        // Inflate the layout for this fragment
        ButterKnife.bind(this, view);
        firebaseAuth = FirebaseAuth.getInstance();
        // initialize click listeners
        //firestore
        postsCollectionReference = FirebaseFirestore.getInstance().collection(Constants.POSTS);
        followingPeopleReference = FirebaseFirestore.getInstance().collection(Constants.PEOPLE_RELATIONS);
        followingCollectionReference = FirebaseFirestore.getInstance().collection(Constants.COLLECTION_RELATIONS);

        return view;
    }



    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FragmentManager manager = getChildFragmentManager();
        CollectionsFragment collectionsFragment = new CollectionsFragment();
        FragmentTransaction ft = manager.beginTransaction();
        ft.replace(R.id.collectionsContainerFrameLayout, collectionsFragment);
        ft.commit();
    }

    @Override
    public void onStart() {
        super.onStart();
        snapshots.clear();
        setRecyclerView();
        getExplorePosts();
        mMorePostsRecyclerView.addItemDecoration(itemOffsetDecoration);

        morePostsAdapter.setBottomReachedListener(new BottomReachedListener() {
            @Override
            public void onBottomReached(final int position) {
                progressBar.setVisibility(View.VISIBLE);
                mProgressRelativeLayout.setVisibility(View.VISIBLE);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        getNextExplorePosts();
                    }
                },1000);
            }
        });

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

    }

    @Override
    public void onStop() {
        super.onStop();
        mMorePostsRecyclerView.removeItemDecoration(itemOffsetDecoration);
    }

    private void setRecyclerView(){
        morePostsAdapter = new MorePostsAdapter(getActivity());
        morePostsAdapter.setHasStableIds(true);
        mMorePostsRecyclerView.setHasFixedSize(false);
        mMorePostsRecyclerView.setAdapter(morePostsAdapter);
        layoutManager = new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL);
        itemOffsetDecoration = new ItemOffsetDecoration(getContext(), R.dimen.item_off_set);
        mMorePostsRecyclerView.setLayoutManager(layoutManager);

    }


    private void getExplorePosts(){
        postsCollectionReference.orderBy("random_number", Query.Direction.DESCENDING)
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


    private void getNextExplorePosts(){
        final DocumentSnapshot lastVisible = snapshots.get(snapshots.size() - 1);
        postsCollectionReference.orderBy("random_number", Query.Direction.DESCENDING)
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
        morePostsAdapter.notifyItemInserted(snapshots.size() - 1);
        morePostsAdapter.getItemCount();
        morePostsAdapter.setExplorePosts(snapshots);

    }

    protected void onDocumentModified(DocumentChange change) {
        try {
            if (change.getOldIndex() == change.getNewIndex()) {
                // Item changed but remained in same position
                snapshots.set(change.getOldIndex(), change.getDocument());
                morePostsAdapter.notifyItemChanged(change.getOldIndex());
            } else {
                // Item changed and changed position
                snapshots.remove(change.getOldIndex());
                snapshots.add(change.getNewIndex(), change.getDocument());
                morePostsAdapter.notifyItemRangeChanged(0, snapshots.size());
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    protected void onDocumentRemoved(DocumentChange change) {
        try {
            snapshots.remove(change.getOldIndex());
            morePostsAdapter.notifyItemRemoved(change.getOldIndex());
            morePostsAdapter.notifyItemRangeChanged(0, snapshots.size());
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

}

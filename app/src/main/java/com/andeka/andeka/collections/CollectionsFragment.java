package com.andeka.andeka.collections;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.andeka.andeka.Constants;
import com.andeka.andeka.R;
import com.andeka.andeka.search.SearchedCollectionsActivity;
import com.andeka.andeka.utils.BottomReachedListener;
import com.andeka.andeka.utils.EndlessStaggeredScrollListener;
import com.andeka.andeka.utils.ItemOffsetDecoration;
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

/**
 * A simple {@link Fragment} subclass.
 */
public class CollectionsFragment extends Fragment implements View.OnClickListener{
    @Bind(R.id.collectionsRecyclerView)RecyclerView mCollectionsRecyclerView;
    @Bind(R.id.progressRelativeLayout)RelativeLayout mProgressRelativeLayout;

    private static final String TAG = CollectionsFragment.class.getSimpleName();
    //layouts
    private ItemOffsetDecoration itemOffsetDecoration;
    private StaggeredGridLayoutManager layoutManager;
    private SearchView searchView;
    // adapters
    private CollectionsAdapter collectionsAdapter;
    // lists
    private List<DocumentSnapshot> mSnapshots = new ArrayList<>();
    //firebase
    private CollectionReference collectionsCollection;
    private FirebaseAuth firebaseAuth;
    private int TOTAL_ITEMS = 20;



    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

    }

    public CollectionsFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.collection_menu, menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_search){
            Intent intent =  new Intent(getActivity(),SearchedCollectionsActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_collections, container, false);
        ButterKnife.bind(this, view);
        initFirebase();
        getCollections();
        setRecyclerView();

        mCollectionsRecyclerView.addOnScrollListener(new EndlessStaggeredScrollListener() {
            @Override
            public void onLoadMore() {
                collectionsAdapter.setBottomReachedListener(new BottomReachedListener() {
                    @Override
                    public void onBottomReached(int position) {
                        mProgressRelativeLayout.setVisibility(View.VISIBLE);
                        getNextCollections();
                    }
                });
            }
        });
        return view;
    }


    @Override
    public void onActivityCreated(@android.support.annotation.Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
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
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onStop() {
        super.onStop();
        mCollectionsRecyclerView.removeItemDecoration(itemOffsetDecoration);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View v){

    }

    private void initFirebase(){
        firebaseAuth = FirebaseAuth.getInstance();
        collectionsCollection = FirebaseFirestore.getInstance().collection(Constants.COLLECTIONS);
    }

    private void setRecyclerView(){
        collectionsAdapter = new CollectionsAdapter(getContext(), mSnapshots);
        collectionsAdapter.setHasStableIds(true);
        mCollectionsRecyclerView.setAdapter(collectionsAdapter);
        mCollectionsRecyclerView.setHasFixedSize(false);
        layoutManager = new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL);
        itemOffsetDecoration = new ItemOffsetDecoration(getContext(), R.dimen.item_off_set);
        mCollectionsRecyclerView.setLayoutManager(layoutManager);
    }

    public void getCollections(){
        collectionsCollection.orderBy("time", Query.Direction.ASCENDING)
                .limit(TOTAL_ITEMS).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

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

    public void getNextCollections(){
        DocumentSnapshot lastVisible = mSnapshots.get(mSnapshots.size() - 1);

        //retrieve the first bacth of posts
        Query nextQuery = collectionsCollection.orderBy("time", Query.Direction.ASCENDING)
                .startAfter(lastVisible).limit(TOTAL_ITEMS);

        nextQuery.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(final QuerySnapshot documentSnapshots) {
                if (!documentSnapshots.isEmpty()){
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
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
                    }, 4000);
                    mProgressRelativeLayout.setVisibility(View.GONE);
                }else {
                    mProgressRelativeLayout.setVisibility(View.GONE);
                }
            }
        });

    }

    protected void onDocumentAdded(DocumentChange change) {
        mSnapshots.add(change.getDocument());
        collectionsAdapter.notifyItemInserted(mSnapshots.size() - 1);
        collectionsAdapter.getItemCount();

    }

    protected void onDocumentModified(DocumentChange change) {
        try {
            if (change.getOldIndex() == change.getNewIndex()) {
                // Item changed but remained in same position
                mSnapshots.set(change.getOldIndex(), change.getDocument());
                collectionsAdapter.notifyItemChanged(change.getOldIndex());
            } else {
                // Item changed and changed position
                mSnapshots.remove(change.getOldIndex());
                mSnapshots.add(change.getNewIndex(), change.getDocument());
                collectionsAdapter.notifyItemRangeChanged(0, mSnapshots.size());
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    protected void onDocumentRemoved(DocumentChange change) {
        try {
            mSnapshots.remove(change.getOldIndex());
            collectionsAdapter.notifyItemRemoved(change.getOldIndex());
            collectionsAdapter.notifyItemRangeChanged(0, mSnapshots.size());
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

}



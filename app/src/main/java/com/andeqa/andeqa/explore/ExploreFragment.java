package com.andeqa.andeqa.explore;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

/**
 * A simple {@link Fragment} subclass.
 */
public class ExploreFragment extends Fragment {

    @Bind(R.id.bestPostsRecyclerView)RecyclerView bestPostRecyclerView;

    private StaggeredGridLayoutManager layoutManager;
    //firestore
    private CollectionReference postWalletCollection;
    private Query creditQuery;
    //adapters
    private ExplorePostAdapter explorePostAdapter;
    private FirebaseAuth firebaseAuth;
    private int TOTAL_ITEMS = 20;
    private DocumentSnapshot lastVisible;
    private List<String> snapshotsIds = new ArrayList<>();
    private List<DocumentSnapshot> documentSnapshots = new ArrayList<>();
    private ItemOffsetDecoration itemOffsetDecoration;
    private static final String TAG = ExploreFragment.class.getSimpleName();

    public static ExploreFragment newInstance(int sectionNumber) {
        ExploreFragment fragment = new ExploreFragment();
        Bundle args = new Bundle();
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
        if (firebaseAuth.getCurrentUser()!= null){
            //firestore
            postWalletCollection = FirebaseFirestore.getInstance().collection(Constants.CREDITS);
            creditQuery = postWalletCollection.orderBy("amount", Query.Direction.ASCENDING)
                    .limit(TOTAL_ITEMS);

            bestPostRecyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener() {
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
    public void onStart() {
        super.onStart();
        bestPostRecyclerView.addItemDecoration(itemOffsetDecoration);

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
    public void onStop() {
        super.onStop();
        bestPostRecyclerView.removeItemDecoration(itemOffsetDecoration);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    private void loadData(){
        documentSnapshots.clear();
        setRecyclerView();
        setCollections();
    }

    private void setRecyclerView(){
        explorePostAdapter = new ExplorePostAdapter(getContext());
        bestPostRecyclerView.setAdapter(explorePostAdapter);
        bestPostRecyclerView.setHasFixedSize(false);
        layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        itemOffsetDecoration = new ItemOffsetDecoration(getContext(), R.dimen.item_off_set);
        bestPostRecyclerView.setLayoutManager(layoutManager);
    }

    private void setCollections(){
        creditQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (!documentSnapshots.isEmpty()){
                    Log.d("snapshot is empty", documentSnapshots.size() + "");
                    //retrieve the first bacth of documentSnapshots
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
        final int snapshotSize = explorePostAdapter.getItemCount();

        if (snapshotSize == 0){
            //do nothing
        }else {
            DocumentSnapshot lastVisible = explorePostAdapter.getSnapshot(snapshotSize - 1);

            //retrieve the first bacth of documentSnapshots
            Query nextSellingQuery = postWalletCollection.orderBy("amount", Query.Direction.ASCENDING)
                    .startAfter(lastVisible)
                    .limit(TOTAL_ITEMS);

            nextSellingQuery.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot documentSnapshots) {
                    if (!documentSnapshots.isEmpty()){
                        //retrieve the first bacth of documentSnapshots
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
        snapshotsIds.add(change.getDocument().getId());
        documentSnapshots.add(change.getDocument());
        explorePostAdapter.setBestPosts(documentSnapshots);
        explorePostAdapter.notifyItemInserted(documentSnapshots.size() -1);
        explorePostAdapter.getItemCount();

    }

    protected void onDocumentModified(DocumentChange change) {
        try{
            if (change.getOldIndex() == change.getNewIndex()) {
                // Item changed but remained in same position
                documentSnapshots.set(change.getOldIndex(), change.getDocument());
                explorePostAdapter.notifyItemChanged(change.getOldIndex());
            } else {
                // Item changed and changed position
                documentSnapshots.remove(change.getOldIndex());
                documentSnapshots.add(change.getNewIndex(), change.getDocument());
                explorePostAdapter.notifyItemRangeChanged(0, documentSnapshots.size());
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    protected void onDocumentRemoved(DocumentChange change) {
        try {
            documentSnapshots.remove(change.getOldIndex());
            explorePostAdapter.notifyItemRemoved(change.getOldIndex());
            explorePostAdapter.notifyItemRangeChanged(0, documentSnapshots.size());
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}

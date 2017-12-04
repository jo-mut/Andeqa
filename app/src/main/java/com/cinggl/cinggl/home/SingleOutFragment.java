package com.cinggl.cinggl.home;


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

import com.cinggl.cinggl.Constants;
import com.cinggl.cinggl.R;
import com.cinggl.cinggl.adapters.SingleOutAdapter;
import com.cinggl.cinggl.adapters.OutAdapter;
import com.cinggl.cinggl.models.Post;
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
public class SingleOutFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener{
    @Bind(R.id.singleOutRecyclerView)RecyclerView singleOutRecyclerView;
    @Bind(R.id.swipeToRefreshLayout)SwipeRefreshLayout swipeRefreshLayout;

    private static final String TAG = SingleOutFragment.class.getSimpleName();
    private static final String KEY_LAYOUT_POSITION = "layout pooition";
    private Parcelable recyclerViewState;
    //firestore reference
    private CollectionReference cinglesReference;
    private Query randomQuery;
    //firebase auth
    private FirebaseAuth firebaseAuth;
    private SingleOutAdapter singleOutAdapter;
    private OutAdapter outAdapter;
    private DocumentSnapshot lastVisible;
    private List<Post> posts = new ArrayList<>();
    private List<String> cinglesIds = new ArrayList<>();
    private LinearLayoutManager layoutManager;
    private int TOTAL_ITEMS = 1;


    public SingleOutFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() != null){
            //firestore
            cinglesReference = FirebaseFirestore.getInstance().collection(Constants.POSTS);
            randomQuery = cinglesReference.orderBy("randomNumber", Query.Direction.ASCENDING)
                    .limit(TOTAL_ITEMS);

            randomQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                    if (!documentSnapshots.isEmpty()){
                        Log.d("posts count", documentSnapshots.size() + "");
                    }
                }
            });

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_cingle_out, container, false);
        ButterKnife.bind(this, view);
        swipeRefreshLayout.setOnRefreshListener(this);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setTheFirstBacthRandomCingles();
        if (savedInstanceState != null){
            recyclerViewState = savedInstanceState.getParcelable(KEY_LAYOUT_POSITION);
            Log.d("posts saved Instance", "Instance is not null");
                    //);
        }else {
            Log.d("Saved Instance", "Instance is completely null");
        }
    }

    private void setTheFirstBacthRandomCingles(){
        randomQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                for (DocumentChange change : documentSnapshots.getDocumentChanges()) {
                    switch (change.getType()) {
                        case ADDED:
                            onDocumentAdded(change);
                            break;
                        case MODIFIED:
//                            onDocumentModified(change);
                            break;
                        case REMOVED:
//                            onDocumentRemoved(change);
                            break;
                    }
                    onDataChanged();
                }

            }
        });

        //initilize the recycler view and set posts
        singleOutAdapter = new SingleOutAdapter(getContext());
        singleOutRecyclerView.setAdapter(singleOutAdapter);
        singleOutRecyclerView.setHasFixedSize(false);
        layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setAutoMeasureEnabled(true);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        singleOutRecyclerView.setLayoutManager(layoutManager);
    }

    private void setNextRandomCingles(){
        cinglesReference.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(final QuerySnapshot creditsSnapshots, FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (creditsSnapshots.isEmpty()){
                    swipeRefreshLayout.setRefreshing(false);
                }else {
                    randomQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(final QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                            if (e != null) {
                                Log.w(TAG, "Listen error", e);
                                return;
                            }

                            if (!documentSnapshots.isEmpty()){
                                //get the last visible document(cingle)
                                lastVisible = documentSnapshots.getDocuments()
                                        .get(documentSnapshots.size() - 1);

                                //query starting from last retrived cingle
                                final Query nextBestCinglesQuery = cinglesReference.orderBy("randomNumber")
                                        .startAfter(lastVisible);
                                //retrive more cingles if present
                                nextBestCinglesQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
                                    @Override
                                    public void onEvent(final QuerySnapshot snapshots, FirebaseFirestoreException e) {
                                        if (e != null) {
                                            Log.w(TAG, "Listen error", e);
                                            return;
                                        }

                                        Log.d("remaining posts", snapshots.size() + "");

                                        //retrieve cingles depending on the remaining size of the list
                                        if (!snapshots.isEmpty()){
                                            final long lastSize = snapshots.size();
                                            if (lastSize < TOTAL_ITEMS){
                                                nextBestCinglesQuery.limit(lastSize);
                                            }else {
                                                nextBestCinglesQuery.limit(TOTAL_ITEMS);
                                            }

                                            //make sure that the size of snapshot equals item count
                                            if (singleOutAdapter.getItemCount() == creditsSnapshots.size()){
                                                swipeRefreshLayout.setRefreshing(false);
                                            }else if (singleOutAdapter.getItemCount() < creditsSnapshots.size()){
                                                for (DocumentChange change : snapshots.getDocumentChanges()) {
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
                                                    onDataChanged();
                                                }
                                                swipeRefreshLayout.setRefreshing(false);
                                            }else {
                                                swipeRefreshLayout.setRefreshing(false);
                                            }


                                        }


                                    }
                                });
                            }

                        }
                    });
                }

            }
        });

    }

    public void onDocumentAdded(DocumentChange change) {
        Post post = change.getDocument().toObject(Post.class);
        cinglesIds.add(change.getDocument().getId());
        posts.add(post);
        singleOutAdapter.setRandomPosts(posts);
        singleOutAdapter.getItemCount();
        singleOutAdapter.notifyItemInserted(posts.size());

    }

    private void onDocumentModified(DocumentChange change) {
        Post post = change.getDocument().toObject(Post.class);
        if (change.getOldIndex() == change.getNewIndex()) {
            // Item changed but remained in same position
            cinglesIds.add(change.getDocument().getId());
            posts.set(change.getNewIndex(), post);
            singleOutAdapter.notifyItemChanged(change.getOldIndex());

        } else {
            // Item changed and changed position
            posts.remove(change.getOldIndex());
            posts.add(change.getNewIndex(), post);
            singleOutAdapter.notifyItemMoved(change.getOldIndex(), change.getNewIndex());
        }
    }

    private void onDocumentRemoved(DocumentChange change) {
        String cingle_key = change.getDocument().getId();
        int cingle_index = cinglesIds.indexOf(cingle_key);
        if (cingle_index > -1){
            //remove data from the list
            cinglesIds.remove(change.getDocument().getId());
            singleOutAdapter.notifyItemRemoved(change.getOldIndex());
            singleOutAdapter.getItemCount();
        }else {
            Log.v(TAG, "onDocumentRemoved:" + cingle_key);
        }

    }

    private void onError(FirebaseFirestoreException e) {};

    private void onDataChanged() {}




    @Override
    public void onRefresh() {
        swipeRefreshLayout.setRefreshing(true);
        setNextRandomCingles();
    }


    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onStop() {
        super.onStop();
    }

}

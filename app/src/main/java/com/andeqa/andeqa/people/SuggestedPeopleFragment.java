package com.andeqa.andeqa.people;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.R;
import com.andeqa.andeqa.models.Andeqan;
import com.andeqa.andeqa.utils.EndlessRecyclerOnScrollListener;
import com.andeqa.andeqa.utils.ItemOffsetDecoration;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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
public class SuggestedPeopleFragment extends Fragment {
    @Bind(R.id.peopleRecyclerView)RecyclerView peopleRecyclerView;
    //firestore
    private CollectionReference usersCollection;
    private CollectionReference followingCollection;
    //firebase
    private DatabaseReference databaseReference;
    //firebase auth
    private FirebaseAuth firebaseAuth;
    private boolean processFollow = false;
    private static final String TAG = SuggestedPeopleFragment.class.getSimpleName();
    private static final String EXTRA_USER_UID = "uid";
    private String mUid;
    private SuggestedPeopleAdapter suggestedPeopleAdapter;
    private static final int TOTAL_ITEMS = 30;
    private ItemOffsetDecoration itemOffsetDecoration;
    private List<String> followersIds = new ArrayList<>();
    private List<DocumentSnapshot> documentSnapshots = new ArrayList<>();


    public static SuggestedPeopleFragment newInstance(String title) {
        SuggestedPeopleFragment fragment = new SuggestedPeopleFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        fragment.setArguments(args);
        return fragment;
    }

    public SuggestedPeopleFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_suggested_people, container, false);
        ButterKnife.bind(this, view);

        firebaseAuth = FirebaseAuth.getInstance();
        usersCollection = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);
        followingCollection = FirebaseFirestore.getInstance().collection(Constants.PEOPLE);
        databaseReference = FirebaseDatabase.getInstance().getReference(Constants.RANDOM_PUSH_ID);

        peopleRecyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener() {
            @Override
            public void onLoadMore() {
                setNextFollowers();
            }
        });


        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        documentSnapshots.clear();
        getPeople();
        setRecyclerView();
        peopleRecyclerView.addItemDecoration(itemOffsetDecoration);


    }

    @Override
    public void onStop() {
        super.onStop();
        peopleRecyclerView.removeItemDecoration(itemOffsetDecoration);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    protected void onDocumentAdded(DocumentChange change) {
        followersIds.add(change.getDocument().getId());
        documentSnapshots.add(change.getDocument());
        suggestedPeopleAdapter.setPeople(documentSnapshots);
        suggestedPeopleAdapter.notifyItemInserted(documentSnapshots.size() -1);
        suggestedPeopleAdapter.getItemCount();

    }

    protected void onDocumentModified(DocumentChange change) {
        if (change.getOldIndex() == change.getNewIndex()) {
            // Item changed but remained in same position
            documentSnapshots.set(change.getOldIndex(), change.getDocument());
            suggestedPeopleAdapter.notifyItemChanged(change.getOldIndex());
        } else {
            // Item changed and changed position
            documentSnapshots.remove(change.getOldIndex());
            documentSnapshots.add(change.getNewIndex(), change.getDocument());
            suggestedPeopleAdapter.notifyItemRangeChanged(0, documentSnapshots.size());
        }
    }

    protected void onDocumentRemoved(DocumentChange change) {
        try {
            documentSnapshots.remove(change.getOldIndex());
            suggestedPeopleAdapter.notifyItemRemoved(change.getOldIndex());
            suggestedPeopleAdapter.notifyItemRangeChanged(0, documentSnapshots.size());
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    private void setRecyclerView(){
        suggestedPeopleAdapter = new SuggestedPeopleAdapter(getContext());
        peopleRecyclerView.setAdapter(suggestedPeopleAdapter);
        peopleRecyclerView.setHasFixedSize(false);
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2,
                StaggeredGridLayoutManager.VERTICAL);
        peopleRecyclerView.setLayoutManager(layoutManager);
        itemOffsetDecoration = new ItemOffsetDecoration(getContext(), R.dimen.item_off_set);

    }

    private void getPeople() {
        usersCollection.orderBy("user_id", Query.Direction.DESCENDING)
                .limit(TOTAL_ITEMS).addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@android.support.annotation.Nullable QuerySnapshot documentSnapshots,
                                        @android.support.annotation.Nullable FirebaseFirestoreException e) {

                        if (e != null) {
                            Log.w(TAG, "Listen error", e);
                            return;
                        }

                        if (!documentSnapshots.isEmpty()) {
                            for (final DocumentChange change : documentSnapshots.getDocumentChanges()) {
                                switch (change.getType()) {
                                    case ADDED:
                                        Andeqan andeqan = change.getDocument().toObject(Andeqan.class);
                                        if (andeqan.getUser_id().equals(firebaseAuth.getCurrentUser().getUid())){
                                            //do not add to the recycler view adapter
                                        }else {
                                            onDocumentAdded(change);
                                        }
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

    private void setNextFollowers(){
        // Get the last visible document
        final int snapshotSize = suggestedPeopleAdapter.getItemCount();

        if (snapshotSize > 0){
            final DocumentSnapshot lastVisible = suggestedPeopleAdapter.getSnapshot(snapshotSize - 1);
            Query nextQuery =  usersCollection.orderBy("user_id", Query.Direction.DESCENDING)
                    .startAfter(lastVisible).limit(TOTAL_ITEMS);

            nextQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@android.support.annotation.Nullable QuerySnapshot documentSnapshots,
                                            @android.support.annotation.Nullable FirebaseFirestoreException e) {

                            if (e != null) {
                                Log.w(TAG, "Listen error", e);
                                return;
                            }

                            if (!documentSnapshots.isEmpty()) {
                                for (final DocumentChange change : documentSnapshots.getDocumentChanges()) {
                                    switch (change.getType()) {
                                        case ADDED:
                                            Andeqan andeqan = change.getDocument().toObject(Andeqan.class);
                                            if (andeqan.getUser_id().equals(firebaseAuth.getCurrentUser().getUid())){
                                                //do not add to the recycler view adapter
                                            }else {
                                                onDocumentAdded(change);
                                            }
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

}

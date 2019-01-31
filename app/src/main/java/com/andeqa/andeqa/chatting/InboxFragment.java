package com.andeqa.andeqa.chatting;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.R;
import com.andeqa.andeqa.search.SearchPeopleActivity;
import com.andeqa.andeqa.utils.BottomReachedListener;
import com.andeqa.andeqa.utils.EndlessLinearScrollListener;
import com.andeqa.andeqa.utils.EndlessStaggeredScrollListener;
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

import javax.annotation.Nullable;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class InboxFragment extends Fragment {
    @Bind(R.id.roomsRecyclerView)RecyclerView mRoomsRecyclerView;
    @Bind(R.id.placeHolderRelativeLayout)RelativeLayout mPlaceHolderRelativeLayout;

    private static final String TAG = InboxFragment.class.getSimpleName();
    private List<DocumentSnapshot> mSnapshots = new ArrayList<>();
    //adapter
    private ConversationsAdapter conversationsAdapter;
    //firebase
    private FirebaseAuth firebaseAuth;
    private CollectionReference roomsCollections;
    private Query roomsQuery;
    private int TOTAL_ITEMS = 20;


    public InboxFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_inbox, container, false);
        ButterKnife.bind(this, view);
        initFirebase();
        getConversations();
        //set recycler view adapter
        setRecyclerView();
        //next query
        mRoomsRecyclerView.addOnScrollListener(new EndlessLinearScrollListener() {
            @Override
            public void onLoadMore() {
                conversationsAdapter.setBottomReachedListener(new BottomReachedListener() {
                    @Override
                    public void onBottomReached(int position) {
                        getNextConversations();
                    }
                });
            }
        });

        return view;

    }


    @Override
    public void onCreate(@android.support.annotation.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onViewCreated(@NonNull View view, @android.support.annotation.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.chats_menu, menu);

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();


        if (id == R.id.action_chat){
            Intent intent =  new Intent(getActivity(), SearchPeopleActivity.class);
            startActivity(intent);
        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onPause() {
        super.onPause();
    }


    private void setRecyclerView(){
        conversationsAdapter = new ConversationsAdapter(getContext(), mSnapshots);
        mRoomsRecyclerView.setAdapter(conversationsAdapter);
        mRoomsRecyclerView.setHasFixedSize(false);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        mRoomsRecyclerView.setLayoutManager(layoutManager);
    }

    private void initFirebase(){
        firebaseAuth = FirebaseAuth.getInstance();
        roomsCollections = FirebaseFirestore.getInstance().collection(Constants.MESSAGES);
    }

    public void getConversations(){
        roomsCollections.document("last messages")
                .collection(firebaseAuth.getCurrentUser().getUid())
                .orderBy("time", Query.Direction.DESCENDING)
                .limit(TOTAL_ITEMS).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot documentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (!documentSnapshots.isEmpty()){
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


    public void getNextConversations(){
        DocumentSnapshot last = mSnapshots.get(mSnapshots.size() - 1);
        Query nextQuery =  roomsCollections.document("last messages")
                .collection(firebaseAuth.getCurrentUser().getUid())
                .orderBy("time", Query.Direction.DESCENDING)
                .startAfter(last).limit(TOTAL_ITEMS);

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
                }
            }
        });

    }


    protected void onDocumentAdded(DocumentChange change) {
        mSnapshots.add(change.getDocument());
        conversationsAdapter.notifyItemInserted(mSnapshots.size() - 1);
        conversationsAdapter.getItemCount();

    }

    protected void onDocumentModified(DocumentChange change) {
        try {
            if (change.getOldIndex() == change.getNewIndex()) {
                // Item changed but remained in same position
                mSnapshots.set(change.getOldIndex(), change.getDocument());
                conversationsAdapter.notifyItemChanged(change.getOldIndex());
            } else {
                // Item changed and changed position
                mSnapshots.remove(change.getOldIndex());
                mSnapshots.add(change.getNewIndex(), change.getDocument());
                conversationsAdapter.notifyItemRangeChanged(0, mSnapshots.size());
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    protected void onDocumentRemoved(DocumentChange change) {
        try {
            mSnapshots.remove(change.getOldIndex());
            conversationsAdapter.notifyItemRemoved(change.getOldIndex());
            conversationsAdapter.notifyItemRangeChanged(0, mSnapshots.size());
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}

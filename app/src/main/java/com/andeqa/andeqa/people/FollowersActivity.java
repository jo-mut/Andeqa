package com.andeqa.andeqa.people;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.R;
import com.andeqa.andeqa.utils.EndlessLinearRecyclerViewOnScrollListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

import javax.annotation.Nullable;

import butterknife.Bind;
import butterknife.ButterKnife;

public class FollowersActivity extends AppCompatActivity {
    @Bind(R.id.followersRecyclerView)RecyclerView followersRecyclerView;
    @Bind(R.id.toolbar)Toolbar toolbar;
    //firestore
    private CollectionReference peopleCollection;
    private CollectionReference usersCollection;
    private CollectionReference followersCollection;
    private CollectionReference timelineCollection;
    private Query followersQuery;
    //firebase
    private DatabaseReference databaseReference;
    //firebase auth
    private FirebaseAuth firebaseAuth;
    private boolean processFollow = false;
    private static final String TAG = FollowersActivity.class.getSimpleName();
    private static final String EXTRA_USER_UID = "uid";
    private String mUid;
    private FollowersAdapter followersAdapter;
    private static final int TOTAL_ITEMS = 30;

    private List<String> followersIds = new ArrayList<>();
    private List<DocumentSnapshot> documentSnapshots = new ArrayList<>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_followers);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        toolbar.setNavigationIcon(R.drawable.ic_arrow);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        firebaseAuth = FirebaseAuth.getInstance();

        mUid = getIntent().getStringExtra(EXTRA_USER_UID);

        if (firebaseAuth.getCurrentUser()!= null){
            usersCollection = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);
            followersCollection = FirebaseFirestore.getInstance().collection(Constants.PEOPLE);
            followersQuery = followersCollection.document("followers")
                    .collection(mUid).orderBy("user_id");
            timelineCollection = FirebaseFirestore.getInstance().collection(Constants.TIMELINE);
            databaseReference = FirebaseDatabase.getInstance().getReference(Constants.RANDOM_PUSH_ID);

            followersRecyclerView.addOnScrollListener(new EndlessLinearRecyclerViewOnScrollListener() {
                @Override
                public void onLoadMore() {
                    setNextFollowers();
                }
            });
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        documentSnapshots.clear();
        getFollowers();
        setRecyclerView();

    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    protected void onDocumentAdded(DocumentChange change) {
        followersIds.add(change.getDocument().getId());
        documentSnapshots.add(change.getDocument());
        followersAdapter.setPeople(documentSnapshots);
        followersAdapter.notifyItemInserted(documentSnapshots.size() -1);
        followersAdapter.getItemCount();

    }

    protected void onDocumentModified(DocumentChange change) {
        if (change.getOldIndex() == change.getNewIndex()) {
            // Item changed but remained in same position
            documentSnapshots.set(change.getOldIndex(), change.getDocument());
            followersAdapter.notifyItemChanged(change.getOldIndex());
        } else {
            // Item changed and changed position
            documentSnapshots.remove(change.getOldIndex());
            documentSnapshots.add(change.getNewIndex(), change.getDocument());
            followersAdapter.notifyItemRangeChanged(0, documentSnapshots.size());
        }
    }

    protected void onDocumentRemoved(DocumentChange change) {
        try {
            documentSnapshots.remove(change.getOldIndex());
            followersAdapter.notifyItemRemoved(change.getOldIndex());
            followersAdapter.notifyItemRangeChanged(0, documentSnapshots.size());
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    private void setRecyclerView(){

        followersAdapter = new FollowersAdapter(this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        followersRecyclerView.setAdapter(followersAdapter);
        followersRecyclerView.setHasFixedSize(false);
        followersRecyclerView.setLayoutManager(linearLayoutManager);
    }

    private void getFollowers() {
        followersQuery.limit(TOTAL_ITEMS).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot documentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

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

    private void setNextFollowers(){
        // Get the last visible document
        final int snapshotSize = followersAdapter.getItemCount();

        if (snapshotSize == 0){
        }else {
            DocumentSnapshot lastVisible = followersAdapter.getSnapshot(snapshotSize - 1);

            //retrieve the first bacth of documentSnapshots
            Query nextSellingQuery =  followersCollection.document("followers")
                    .collection(mUid).orderBy("user_id")
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

}

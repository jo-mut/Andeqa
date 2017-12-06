package com.cinggl.cinggl.home;

import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cinggl.cinggl.Constants;
import com.cinggl.cinggl.R;
import com.cinggl.cinggl.adapters.BestPostsAdapter;
import com.cinggl.cinggl.models.Credit;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class BestPostsActivity extends AppCompatActivity {

    @Bind(R.id.bestPostsRecyclerView)RecyclerView bestCinglesRecyclerView;
    private static final String TAG = "BestCingleFragment";
    private static final String KEY_LAYOUT_POSITION = "layout position";
    private LinearLayoutManager layoutManager;
    private Parcelable recyclerViewState;
    //firebase auth
    private FirebaseAuth firebaseAuth;
    //adapters
    private BestPostsAdapter bestPostsAdapter;
    //firestore
    private CollectionReference senseCreditReference;
    private Query bestCinglesQuery;
    //cingles member variables
    private List<Credit> credits = new ArrayList<>();
    private List<String> creditIds = new ArrayList<>();
    private int TOTAL_ITEMS = 4;
    private DocumentSnapshot lastVisible;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_best_posts);
        ButterKnife.bind(this);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationIcon(R.drawable.ic_keyboard_arrow_left_white_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
            }
        });


        firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() != null){
            senseCreditReference = FirebaseFirestore.getInstance().collection(Constants.SENSECREDITS);
            bestCinglesQuery = senseCreditReference.orderBy("amount", Query.Direction.ASCENDING)
                    .limit(TOTAL_ITEMS);

            setTheFirstBacthBestCingles();
            recyclerViewScrolling();
            if (savedInstanceState != null){
                recyclerViewState = savedInstanceState.getParcelable(KEY_LAYOUT_POSITION);
                Log.d("Best saved Instance", "Instance is not");
            }else {
                Log.d("Saved Instance", "Instance is completely null");
            }

        }
    }

    private void setTheFirstBacthBestCingles(){
        bestCinglesQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                Log.d("all best cingles", documentSnapshots.size() + "");

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
                            onDocumentModified(change);
                            break;
                        case REMOVED:
                            onDocumentRemoved(change);
                            break;
                    }
                    onDataChanged();
                }

            }
        });

        // RecyclerView
        bestPostsAdapter = new BestPostsAdapter(this);
        bestCinglesRecyclerView.setAdapter(bestPostsAdapter);
        bestCinglesRecyclerView.setHasFixedSize(false);
        layoutManager = new LinearLayoutManager(this);
        bestCinglesRecyclerView.setLayoutManager(layoutManager);

    }

    private void recyclerViewScrolling(){
        bestCinglesRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (!recyclerView.canScrollVertically(-1)) {
                    onScrolledToTop();
                } else if (!recyclerView.canScrollVertically(1)) {
                    onScrolledToBottom();
                } else if (dy < 0) {
                    onScrolledUp();
                } else if (dy > 0) {
                    onScrolledDown();
                }
            }
        });
    }

    public void onScrolledUp() {}

    public void onScrolledDown() {

    }

    public void onScrolledToTop() {

    }

    public void onScrolledToBottom() {
        setNextBestCingles();
    }

    private void setNextBestCingles(){
        senseCreditReference.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(final QuerySnapshot creditsSnapshots, FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (creditsSnapshots.isEmpty()){
                }else {
                    bestCinglesQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
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
                                final Query nextBestCinglesQuery = senseCreditReference.orderBy("amount")
                                        .startAfter(lastVisible);
                                //retrive more cingles if present
                                nextBestCinglesQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
                                    @Override
                                    public void onEvent(final QuerySnapshot snapshots, FirebaseFirestoreException e) {
                                        if (e != null) {
                                            Log.w(TAG, "Listen error", e);
                                            return;
                                        }

                                        //retrieve cingles depending on the remaining size of the list
                                        if (!snapshots.isEmpty()){
                                            final long lastSize = snapshots.size();
                                            if (lastSize < TOTAL_ITEMS){
                                                nextBestCinglesQuery.limit(lastSize);
                                            }else {
                                                nextBestCinglesQuery.limit(TOTAL_ITEMS);
                                            }

                                            //make sure that the size of snapshot equals item count
                                            if (bestPostsAdapter.getItemCount() == creditsSnapshots.size()){
                                            }else if (bestPostsAdapter.getItemCount() < creditsSnapshots.size()){
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
                                            }else {
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

    private void onDocumentAdded(DocumentChange change) {
        Credit credit = change.getDocument().toObject(Credit.class);
        if (credit.getAmount() > 0.00){
            creditIds.add(change.getDocument().getId());
            credits.add(credit);
            bestPostsAdapter.setBestCingles(credits);
            bestPostsAdapter.getItemCount();
            bestPostsAdapter.notifyItemInserted(credits.size());
        }

    }

    private void onDocumentModified(DocumentChange change) {
        Credit credit = change.getDocument().toObject(Credit.class);
        if (change.getOldIndex() == change.getNewIndex()) {
            // Item changed but remained in same position
            creditIds.add(change.getDocument().getId());
            credits.set(change.getNewIndex(), credit);
            bestPostsAdapter.notifyItemChanged(change.getOldIndex());

        } else {
            // Item changed and changed position
            credits.remove(change.getOldIndex());
            credits.add(change.getNewIndex(), credit);
            bestPostsAdapter.notifyItemMoved(change.getOldIndex(), change.getNewIndex());
        }

    }

    private void onDocumentRemoved(DocumentChange change) {
        String credit_key = change.getDocument().getId();
        int credit_index = creditIds.indexOf(credit_key);
        if (credit_index > -1){
            //remove data from the list
            creditIds.remove(change.getDocument().getId());
            bestPostsAdapter.removeAt(change.getOldIndex());
            bestPostsAdapter.notifyItemRemoved(change.getOldIndex());
            bestPostsAdapter.getItemCount();
        }else {
            Log.v(TAG, "onDocumentRemoved:" + credit_key);
        }


    }

    private void onError(FirebaseFirestoreException e) {};

    private void onDataChanged() {}


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onPause() {
        super.onPause();
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

package com.cinggl.cinggl.home;

import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.cinggl.cinggl.Constants;
import com.cinggl.cinggl.R;
import com.cinggl.cinggl.adapters.BestPostsAdapter;
import com.cinggl.cinggl.models.Credit;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
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
            bestCinglesQuery = senseCreditReference.orderBy("amount", Query.Direction.DESCENDING);

            setBestCingles();
            if (savedInstanceState != null){
                recyclerViewState = savedInstanceState.getParcelable(KEY_LAYOUT_POSITION);
                Log.d("Best saved Instance", "Instance is not");
            }else {
                Log.d("Saved Instance", "Instance is completely null");
            }

        }
    }

    private void setBestCingles(){
        bestCinglesQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                Log.d("best post", documentSnapshots.size() + "");

                if (!documentSnapshots.isEmpty()){
                    bestPostsAdapter = new BestPostsAdapter(bestCinglesQuery, BestPostsActivity.this);
                    bestPostsAdapter.startListening();
                    bestCinglesRecyclerView.setAdapter(bestPostsAdapter);
                    bestCinglesRecyclerView.setHasFixedSize(false);
                    layoutManager = new LinearLayoutManager(BestPostsActivity.this);
                    bestCinglesRecyclerView.setLayoutManager(layoutManager);
                }
            }
        });


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

    }

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

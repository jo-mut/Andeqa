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
import com.cinggl.cinggl.adapters.OtherPostAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.math.BigDecimal;
import java.math.RoundingMode;

import butterknife.Bind;
import butterknife.ButterKnife;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.V;
import static android.util.Log.d;

public class BestPostsActivity extends AppCompatActivity {
    @Bind(R.id.otherPostsRecyclerView)RecyclerView otherPostsRecyclerView;
    private static final String TAG = "BestCingleFragment";
    private static final String KEY_LAYOUT_POSITION = "layout position";
    private LinearLayoutManager layoutManager;
    private Parcelable recyclerViewState;
    //cingles member variables
    private int TOTAL_ITEMS = 4;
    //firestore reference
    private CollectionReference commentsReference;
    private CollectionReference senseCreditReference;
    private Query bestCinglesQuery;
    private Query likesQuery;
    //firebase auth
    private FirebaseAuth firebaseAuth;
    //adapters
    private OtherPostAdapter otherPostAdapter;
    private FirestoreRecyclerAdapter dayPostRecyclerAdapter;

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
            commentsReference = FirebaseFirestore.getInstance().collection(Constants.COMMENTS);
            senseCreditReference = FirebaseFirestore.getInstance().collection(Constants.SENSECREDITS);


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
        bestCinglesQuery.limit(50).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                Log.d("best post", documentSnapshots.size() + "");

                if (!documentSnapshots.isEmpty()){
                    otherPostAdapter = new OtherPostAdapter(bestCinglesQuery, BestPostsActivity.this);
                    otherPostAdapter.startListening();
                    otherPostsRecyclerView.setAdapter(otherPostAdapter);
                    otherPostsRecyclerView.setHasFixedSize(false);
                    layoutManager = new LinearLayoutManager(BestPostsActivity.this);
                    otherPostsRecyclerView.setLayoutManager(layoutManager);
                }
            }
        });


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

    //region listeners
    private static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

}

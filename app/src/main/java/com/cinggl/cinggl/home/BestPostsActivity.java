package com.cinggl.cinggl.home;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Parcelable;
import android.support.v4.app.FragmentManager;
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
import com.cinggl.cinggl.comments.CommentsActivity;
import com.cinggl.cinggl.likes.LikesActivity;
import com.cinggl.cinggl.models.Balance;
import com.cinggl.cinggl.models.Cinggulan;
import com.cinggl.cinggl.models.Credit;
import com.cinggl.cinggl.models.Like;
import com.cinggl.cinggl.models.Post;
import com.cinggl.cinggl.models.PostSale;
import com.cinggl.cinggl.models.TransactionDetails;
import com.cinggl.cinggl.people.FollowerProfileActivity;
import com.cinggl.cinggl.preferences.BestPostsSettingsDialog;
import com.cinggl.cinggl.profile.PersonalProfileActivity;
import com.cinggl.cinggl.viewholders.BestPostsViewHolder;
import com.cinggl.cinggl.viewholders.WhoLikedViewHolder;
import com.facebook.drawee.view.SimpleDraweeView;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.firebase.ui.firestore.ObservableSnapshotArray;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

import static android.util.Log.d;

public class BestPostsActivity extends AppCompatActivity {
    @Bind(R.id.bestPostsRecyclerView)RecyclerView bestCinglesRecyclerView;
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
    private BestPostsAdapter bestPostsAdapter;

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

    //region listeners
    private static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

}

package com.cinggl.cinggl.registration;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.cinggl.cinggl.Constants;
import com.cinggl.cinggl.R;
import com.cinggl.cinggl.adapters.CinggulansAdapter;
import com.cinggl.cinggl.home.MainActivity;
import com.cinggl.cinggl.models.Cinggulan;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import butterknife.Bind;
import butterknife.ButterKnife;


public class FollowCinggulans extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG = FollowCinggulans.class.getSimpleName();
    @Bind(R.id.followCinggulansRecyclerView)RecyclerView followCinggulansRecyclerView;
    @Bind(R.id.doneButtonLinearLayout)LinearLayout mDoneButtonLinearLayout;
    @Bind(R.id.doneButton)Button mDoneButton;
    @Bind(R.id.progressBar)ProgressBar progressBar;
    private CollectionReference postsReference;
    private CollectionReference usersReference;
    private CollectionReference relationsReference;
    private Query cinggulansQuery;
    private FirebaseAuth firebaseAuth;
    private CinggulansAdapter cinggulansAdapter;
    private Context mContext;
    private boolean processFollow = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follow_cinggulans);
        ButterKnife.bind(this);

        firebaseAuth = FirebaseAuth.getInstance();
        mDoneButton.setOnClickListener(this);

        if (firebaseAuth.getCurrentUser() != null){
            usersReference = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);
            postsReference = FirebaseFirestore.getInstance().collection(Constants.POSTS);
            relationsReference = FirebaseFirestore.getInstance().collection(Constants.RELATIONS);
            cinggulansQuery = usersReference.orderBy("uid");
            followInitialCinggulans();
            checkIfFollowing();
        }
    }

    private void followInitialCinggulans(){
        progressBar.setVisibility(View.VISIBLE);
        cinggulansQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                if (!documentSnapshots.isEmpty()){
                    Log.d("cinggulans size", documentSnapshots.size()+"");
                    for (final DocumentChange change : documentSnapshots.getDocumentChanges()){
                        Cinggulan cinggulan = change.getDocument().toObject(Cinggulan.class);
                        final String uid = cinggulan.getUid();

                        relationsReference.document("following").collection(uid)
                                .whereEqualTo("uid", firebaseAuth.getCurrentUser().getUid()).get()
                                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot documentSnapshots) {
                                progressBar.setVisibility(View.GONE);
                                cinggulansAdapter = new CinggulansAdapter(cinggulansQuery, FollowCinggulans.this);
                                cinggulansAdapter.startListening();
                                followCinggulansRecyclerView.setAdapter(cinggulansAdapter);
                                followCinggulansRecyclerView.setHasFixedSize(false);
                                LinearLayoutManager layoutManager = new LinearLayoutManager(FollowCinggulans.this);
                                layoutManager.setAutoMeasureEnabled(true);
                                followCinggulansRecyclerView.setLayoutManager(layoutManager);

                            }
                        });

                    }
                }

            }
        });

    }


    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    private void checkIfFollowing(){
        cinggulansQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (!documentSnapshots.isEmpty()){
                    if (documentSnapshots.size() == 1){
                        mDoneButtonLinearLayout.setVisibility(View.VISIBLE);
                    }else {
                        mDoneButtonLinearLayout.setVisibility(View.GONE);
                    }

                    relationsReference.document("following").collection(firebaseAuth.getCurrentUser().getUid())
                            .orderBy("uid").addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                            if (e != null) {
                                Log.w(TAG, "Listen error", e);
                                return;
                            }

                            if (!documentSnapshots.isEmpty()){
                                mDoneButtonLinearLayout.setVisibility(View.VISIBLE);
                            }else {
                                mDoneButtonLinearLayout.setVisibility(View.GONE);
                            }
                        }
                    });
                }
            }
        });

    }


    @Override
    public void onClick(View v){
        if (v == mDoneButton){
            Intent intent = new Intent(FollowCinggulans.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }
}

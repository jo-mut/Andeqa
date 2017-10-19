package com.cinggl.cinggl.ifair;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cinggl.cinggl.Constants;
import com.cinggl.cinggl.R;
import com.cinggl.cinggl.home.CingleDetailActivity;
import com.cinggl.cinggl.viewholders.TransactionHistoryViewHolder;
import com.cinggl.cinggl.models.Balance;
import com.cinggl.cinggl.models.Cingle;
import com.cinggl.cinggl.models.TransactionDetails;
import com.firebase.ui.common.ChangeEventType;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.firebase.ui.firestore.ObservableSnapshotArray;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;

import butterknife.Bind;
import butterknife.ButterKnife;

import static android.content.ContentValues.TAG;
import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;
import static com.cinggl.cinggl.R.id.cingleImageView;

public class WalletActivity extends AppCompatActivity {
    @Bind(R.id.transactionHistoryRecyclerView)RecyclerView mTransactionHistoryRecyclerView;
    @Bind(R.id.currentWalletBalanceTextview)TextView mCurrentWalletBalanceTextView;
    @Bind(R.id.emptyRelativeLayout)RelativeLayout mEmptyRelativeLayout;
    //firestore
    private CollectionReference walletReference;
    private CollectionReference cinglesReference;
    private CollectionReference transactionReference;
    private Query transactionQuery;
    //adapters
    private FirestoreRecyclerAdapter firestoreRecyclerAdapter;
    private FirebaseAuth firebaseAuth;
    private static final String EXTRA_POST_KEY = "post key";
    private DecimalFormat formatter =  new DecimalFormat("0.00000000");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        firebaseAuth = FirebaseAuth.getInstance();

        //BACK NAVIGATION
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        if (firebaseAuth.getCurrentUser()!= null){

            cinglesReference = FirebaseFirestore.getInstance().collection(Constants.POSTS);
            walletReference = FirebaseFirestore.getInstance().collection(Constants.WALLET);
            transactionReference = FirebaseFirestore.getInstance().collection(Constants.TRANSACTION_HISTORY)
                    .document(firebaseAuth.getCurrentUser().getUid()).collection("History");

            setTransactionHistory();
            setCurrentWalletBalance();

        }
    }


    private void setTransactionHistory(){
        FirestoreRecyclerOptions<TransactionDetails> options = new FirestoreRecyclerOptions.Builder<TransactionDetails>()
                .setQuery(transactionQuery, TransactionDetails.class)
                .build();

        firestoreRecyclerAdapter = new FirestoreRecyclerAdapter<TransactionDetails, TransactionHistoryViewHolder>(options) {
            @Override
            protected void onBindViewHolder(final TransactionHistoryViewHolder holder, int position, TransactionDetails model) {
                holder.bindTransactionHistory(model);
                final String postKey = getSnapshots().get(position).getPushId();

                if (transactionReference != null){
                    mEmptyRelativeLayout.setVisibility(View.GONE);
                }else {
                    mEmptyRelativeLayout.setVisibility(View.VISIBLE);
                }

                holder.deleteHistoryImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        transactionReference.document(firebaseAuth.getCurrentUser().getUid())
                                .collection("History").document(postKey).delete();
                    }
                });

                //SET THE CINGLE IMAGE
                transactionReference.document(firebaseAuth.getCurrentUser().getUid())
                        .collection("History").document(postKey).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(final DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen error", e);
                            return;
                        }

                        if (documentSnapshot.exists()){
                            TransactionDetails transactionDetails = documentSnapshot.toObject(TransactionDetails.class);
                            final String transctionKey = transactionDetails.getPushId();

                            cinglesReference.document("Cingles").collection("Cingles").document(transctionKey)
                                    .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                        @Override
                                        public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                                            if (e != null) {
                                                Log.w(TAG, "Listen error", e);
                                                return;
                                            }

                                            if (documentSnapshot.exists()){
                                                final Cingle cingle = documentSnapshot.toObject(Cingle.class);
                                                final String uid = cingle.getUid();

                                                Picasso.with(WalletActivity.this)
                                                        .load(cingle.getCingleImageUrl())
                                                        .networkPolicy(NetworkPolicy.OFFLINE)
                                                        .into(holder.cingleImageView, new Callback() {
                                                            @Override
                                                            public void onSuccess() {

                                                            }

                                                            @Override
                                                            public void onError() {
                                                                Picasso.with(WalletActivity.this)
                                                                        .load(cingle.getCingleImageUrl())
                                                                        .into(holder.cingleImageView);


                                                            }
                                                        });

                                                holder.cingleImageView.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View view) {
                                                        Intent intent = new Intent(WalletActivity.this, CingleDetailActivity.class);
                                                        intent.putExtra(WalletActivity.EXTRA_POST_KEY, postKey);
                                                        startActivity(intent);
                                                    }
                                                });

                                            }
                                        }
                                    });
                        }
                    }
                });


                //SET THE CINGLE IMAGE
                transactionReference.document(firebaseAuth.getCurrentUser().getUid())
                        .collection("History").document(postKey).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen error", e);
                            return;
                        }

                        if (documentSnapshot.exists()){
                            TransactionDetails transactionDetails = documentSnapshot.toObject(TransactionDetails.class);
                            final String transctionKey = transactionDetails.getPushId();

                            cinglesReference.document("Cingles").collection("Cingles").document(transctionKey)
                                    .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                        @Override
                                        public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                                        }
                                    });
                        }
                    }
                });
            }

            @Override
            public TransactionHistoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                return null;
            }

            @Override
            public void onDataChanged() {
                super.onDataChanged();
            }

            @Override
            public int getItemCount() {
                return super.getItemCount();
            }

            @Override
            public void onChildChanged(ChangeEventType type, DocumentSnapshot snapshot, int newIndex, int oldIndex) {
                super.onChildChanged(type, snapshot, newIndex, oldIndex);
            }

            @Override
            public TransactionDetails getItem(int position) {
                return super.getItem(position);
            }

            @Override
            public ObservableSnapshotArray<TransactionDetails> getSnapshots() {
                return super.getSnapshots();
            }
        };


        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setAutoMeasureEnabled(true);
        mTransactionHistoryRecyclerView.setAdapter(firestoreRecyclerAdapter);
        mTransactionHistoryRecyclerView.setHasFixedSize(false);
        mTransactionHistoryRecyclerView.setLayoutManager(layoutManager);
    }

    public void setCurrentWalletBalance(){
        walletReference.document("balance").collection(firebaseAuth.getCurrentUser().getUid())
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen error", e);
                            return;
                        }

                        if (documentSnapshots.isEmpty()){

                        }
                    }
                });

        walletReference.document("balance").collection("balance")
                .document(firebaseAuth.getCurrentUser().getUid())
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (documentSnapshot.exists()){
                    final Balance balance = documentSnapshot.toObject(Balance.class);
                    final double totalBalance = balance.getTotalBalance();

                    mCurrentWalletBalanceTextView.setText("CSC" + " " + formatter.format(totalBalance));

                }
            }
        });

    }


    @Override
    protected void onStart() {
        super.onStart();
        firestoreRecyclerAdapter.stopListening();

    }


    @Override
    public void onStop(){
        super.onStop();
        firestoreRecyclerAdapter.stopListening();
    }

}

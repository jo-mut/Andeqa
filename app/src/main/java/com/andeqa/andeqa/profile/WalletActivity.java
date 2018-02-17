package com.andeqa.andeqa.profile;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.R;
import com.andeqa.andeqa.home.PostDetailActivity;
import com.andeqa.andeqa.models.Post;
import com.andeqa.andeqa.market.TransactionHistoryViewHolder;
import com.andeqa.andeqa.models.Balance;
import com.andeqa.andeqa.models.TransactionDetails;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
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

import java.text.DecimalFormat;

import butterknife.Bind;
import butterknife.ButterKnife;

public class WalletActivity extends AppCompatActivity {
    @Bind(R.id.transactionHistoryRecyclerView)RecyclerView mTransactionHistoryRecyclerView;
    @Bind(R.id.currentWalletBalanceTextview)TextView mCurrentWalletBalanceTextView;
    @Bind(R.id.emptyRelativeLayout)RelativeLayout mEmptyRelativeLayout;
    //firestore
    private CollectionReference cinglesReference;
    private Query transactionQuery;
    private CollectionReference transactionReference;
    private CollectionReference walletReference;
    //firebase
    //adapters
    private FirestoreRecyclerAdapter firestoreRecyclerAdapter;
    private FirebaseAuth firebaseAuth;
    private static final String EXTRA_POST_KEY = "post key";
    private DecimalFormat formatter =  new DecimalFormat("0.00000000");
    private static final String TAG = WalletActivity.class.getSimpleName();

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

            //firestore
            cinglesReference = FirebaseFirestore.getInstance().collection(Constants.POSTS);
            transactionReference = FirebaseFirestore.getInstance().collection(Constants.TRANSACTION_HISTORY);
            transactionQuery = transactionReference.whereEqualTo("uid", firebaseAuth.getCurrentUser().getUid());
            walletReference = FirebaseFirestore.getInstance().collection(Constants.WALLET);

            setTransactionHistory();
            setCurrentWalletBalance();
            firestoreRecyclerAdapter.startListening();

            transactionQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                    if (e != null) {
                        Log.w(TAG, "Listen error", e);
                        return;
                    }

                    if (documentSnapshots.isEmpty()){
                        mEmptyRelativeLayout.setVisibility(View.VISIBLE);
                    }else {
                        mEmptyRelativeLayout.setVisibility(View.GONE);
                    }

                }
            });

        }
    }

    public void setTransactionHistory(){
        FirestoreRecyclerOptions<TransactionDetails> options = new FirestoreRecyclerOptions.Builder<TransactionDetails>()
                .setQuery(transactionQuery, TransactionDetails.class)
                .build();

        firestoreRecyclerAdapter = new FirestoreRecyclerAdapter<TransactionDetails, TransactionHistoryViewHolder>(options) {
            @Override
            protected void onBindViewHolder(final TransactionHistoryViewHolder holder, int position, TransactionDetails model) {
                holder.bindTransactionHistory(model);
                final String postKey = getSnapshots().get(position).getPushId();


                holder.deleteHistoryImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        transactionReference.document(postKey).delete()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(WalletActivity.this, "Successfully deleted", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });

                transactionReference.document(postKey).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                        if (documentSnapshot.exists()){
                            final TransactionDetails td = documentSnapshot.toObject(TransactionDetails.class);
                            final String transactionKey = td.getPushId();

                            cinglesReference.document(transactionKey).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                @Override
                                public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                                    if (e != null) {
                                        Log.w(TAG, "Listen error", e);
                                        return;
                                    }

                                    if (documentSnapshot.exists()){
                                        final Post post = documentSnapshot.toObject(Post.class);
                                        Picasso.with(WalletActivity.this)
                                                .load(post.getImage())
                                                .networkPolicy(NetworkPolicy.OFFLINE)
                                                .into(holder.cingleImageView, new Callback() {
                                                    @Override
                                                    public void onSuccess() {

                                                    }

                                                    @Override
                                                    public void onError() {
                                                        Picasso.with(WalletActivity.this)
                                                                .load(post.getImage())
                                                                .into(holder.cingleImageView);


                                                    }
                                                });

                                        holder.cingleImageView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                Intent intent = new Intent(WalletActivity.this, PostDetailActivity.class);
                                                intent.putExtra(WalletActivity.EXTRA_POST_KEY, transactionKey);
                                                startActivity(intent);
                                            }
                                        });

                                    }
                                }
                            });

                        }
                    }
                });

            }

            @Override
            public TransactionHistoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.transaction_history_layout, parent, false);
                return  new TransactionHistoryViewHolder(view);
            }
        };

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setAutoMeasureEnabled(true);
        mTransactionHistoryRecyclerView.setAdapter(firestoreRecyclerAdapter);
        mTransactionHistoryRecyclerView.setHasFixedSize(false);
        mTransactionHistoryRecyclerView.setLayoutManager(layoutManager);
    }

    public void setCurrentWalletBalance(){
        walletReference.document(firebaseAuth.getCurrentUser().getUid()).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                if (documentSnapshot.exists()){
                    Balance balance = documentSnapshot.toObject(Balance.class);
                    final double walletBalance = balance.getTotalBalance();

                    mCurrentWalletBalanceTextView.setText("SC" + " " + formatter.format(walletBalance));
                }else {
                    mCurrentWalletBalanceTextView.setText("SC 0.00000000");
                }

            }
        });

    }


    @Override
    protected void onStart() {
        super.onStart();

    }


    @Override
    public void onStop(){
        super.onStop();
    }

}

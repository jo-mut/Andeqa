package com.andeqa.andeqa.wallet;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.R;
import com.andeqa.andeqa.models.Balance;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import butterknife.Bind;
import butterknife.ButterKnife;

public class WalletActivity extends AppCompatActivity {
    @Bind(R.id.transactionHistoryRecyclerView)RecyclerView mTransactionHistoryRecyclerView;
    @Bind(R.id.currentWalletBalanceTextview)TextView mCurrentWalletBalanceTextView;
    @Bind(R.id.emptyRelativeLayout)RelativeLayout mEmptyRelativeLayout;
    //firestore
    private CollectionReference collectionsPosts;
    private CollectionReference postsCollection;
    private Query transactionQuery;
    private CollectionReference transactionReference;
    private CollectionReference walletReference;
    private WalletAdapter walletAdapter;
    private FirebaseAuth firebaseAuth;
    private static final String EXTRA_POST_ID = "post id";
    private static final String COLLECTION_ID = "collection id";
    private DecimalFormat formatter =  new DecimalFormat("0.00000000");
    private static final String TAG = WalletActivity.class.getSimpleName();

    private List<String> transactionIds = new ArrayList<>();
    private List<DocumentSnapshot> documentSnapshots = new ArrayList<>();

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
            collectionsPosts = FirebaseFirestore.getInstance().collection(Constants.COLLECTIONS_POSTS);
            postsCollection = FirebaseFirestore.getInstance().collection(Constants.POSTS);
            transactionReference = FirebaseFirestore.getInstance().collection(Constants.TRANSACTION_HISTORY);
            transactionQuery = transactionReference.whereEqualTo("user_id", firebaseAuth.getCurrentUser().getUid());
            walletReference = FirebaseFirestore.getInstance().collection(Constants.WALLET);

            setCurrentWalletBalance();
            setRecyclerView();
            setTransactions();

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

    private void setRecyclerView(){
        walletAdapter = new WalletAdapter(this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mTransactionHistoryRecyclerView.setAdapter(walletAdapter);
        mTransactionHistoryRecyclerView.setHasFixedSize(false);
        mTransactionHistoryRecyclerView.setLayoutManager(layoutManager);
    }

    public void setCurrentWalletBalance(){
        walletReference.document(firebaseAuth.getCurrentUser().getUid())
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                if (documentSnapshot.exists()){
                    Balance balance = documentSnapshot.toObject(Balance.class);
                    final double walletBalance = balance.getTotal_balance();

                    mCurrentWalletBalanceTextView.setText("uC" + " " + formatter.format(walletBalance));
                }else {
                    mCurrentWalletBalanceTextView.setText("uC 0.00000000");
                }

            }
        });

    }

    private void setTransactions(){
        transactionQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (!queryDocumentSnapshots.isEmpty()){
                    //retrieve the first bacth of documentSnapshots
                    for (final DocumentChange change : queryDocumentSnapshots.getDocumentChanges()) {
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

    protected void onDocumentAdded(DocumentChange change) {
        transactionIds.add(change.getDocument().getId());
        documentSnapshots.add(change.getDocument());
        walletAdapter.setTransactionHistory(documentSnapshots);
        walletAdapter.notifyItemInserted(documentSnapshots.size() -1);
        walletAdapter.getItemCount();

    }

    protected void onDocumentModified(DocumentChange change) {
        if (change.getOldIndex() == change.getNewIndex()) {
            // Item changed but remained in same position
            documentSnapshots.set(change.getOldIndex(), change.getDocument());
            walletAdapter.notifyItemChanged(change.getOldIndex());
        } else {
            // Item changed and changed position
            documentSnapshots.remove(change.getOldIndex());
            documentSnapshots.add(change.getNewIndex(), change.getDocument());
            walletAdapter.notifyItemRangeChanged(0, documentSnapshots.size());
        }
    }

    protected void onDocumentRemoved(DocumentChange change) {
        documentSnapshots.remove(change.getOldIndex());
        walletAdapter.notifyItemRemoved(change.getOldIndex());
        walletAdapter.notifyItemRangeChanged(0, documentSnapshots.size());
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

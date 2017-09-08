package com.cinggl.cinggl.ifair;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cinggl.cinggl.Constants;
import com.cinggl.cinggl.R;
import com.cinggl.cinggl.adapters.TransactionHistoryViewHolder;
import com.cinggl.cinggl.models.TransactionDetails;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import butterknife.Bind;
import butterknife.ButterKnife;

public class WalletActivity extends AppCompatActivity {
    @Bind(R.id.transactionHistoryRecyclerView)RecyclerView mTransactionHistoryRecyclerView;
    @Bind(R.id.currentWalletBalanceTextview)TextView mCurrentWalletBalanceTextView;
    @Bind(R.id.emptyRelativeLayout)RelativeLayout mEmptyRelativeLayout;
    private FirebaseRecyclerAdapter firebaseRecyclerAdapter;
    private DatabaseReference transactionReference;
    private DatabaseReference walletReference;
    private FirebaseAuth firebaseAuth;

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

        transactionReference = FirebaseDatabase.getInstance().getReference(Constants.TRANSACTION_HISTORY)
                .child(firebaseAuth.getCurrentUser().getUid());
        walletReference = FirebaseDatabase.getInstance().getReference(Constants.WALLET);

        walletReference.keepSynced(true);
        transactionReference.keepSynced(true);

        setTransactionHistory();
        setCurrentWalletBalance();
    }

    public void setTransactionHistory(){
        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<TransactionDetails, TransactionHistoryViewHolder>
                (TransactionDetails.class, R.layout.transaction_history_layout, TransactionHistoryViewHolder.class, transactionReference) {
            @Override
            protected void populateViewHolder(TransactionHistoryViewHolder viewHolder, TransactionDetails model, int position) {

                if (transactionReference != null){
                    mEmptyRelativeLayout.setVisibility(View.GONE);
                    viewHolder.bindTransactionHistory(model);
                }else {
                    mEmptyRelativeLayout.setVisibility(View.VISIBLE);
                }

            }
        };


        mTransactionHistoryRecyclerView.setAdapter(firebaseRecyclerAdapter);
        mTransactionHistoryRecyclerView.setHasFixedSize(false);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setAutoMeasureEnabled(true);
        mTransactionHistoryRecyclerView.setLayoutManager(layoutManager);
    }

    public void setCurrentWalletBalance(){
        walletReference.child("balance").child(firebaseAuth.getCurrentUser().getUid()).child("total balance")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Double walletBalance = (Double) dataSnapshot.getValue();
                        mCurrentWalletBalanceTextView.setText(Double.toString(walletBalance));
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }
}

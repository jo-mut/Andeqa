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
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cinggl.cinggl.Constants;
import com.cinggl.cinggl.R;
import com.cinggl.cinggl.adapters.IfairCingleAdapter;
import com.cinggl.cinggl.adapters.LikesViewHolder;
import com.cinggl.cinggl.adapters.TransactionHistoryViewHolder;
import com.cinggl.cinggl.home.CingleDetailActivity;
import com.cinggl.cinggl.home.LikesActivity;
import com.cinggl.cinggl.models.Balance;
import com.cinggl.cinggl.models.Cingle;
import com.cinggl.cinggl.models.Like;
import com.cinggl.cinggl.models.TransactionDetails;
import com.cinggl.cinggl.profile.PersonalProfileActivity;
import com.cinggl.cinggl.relations.FollowerProfileActivity;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;

import butterknife.Bind;
import butterknife.ButterKnife;

import static com.cinggl.cinggl.R.id.cingleImageView;
import static com.cinggl.cinggl.R.id.cingleTradeMethodTextView;

public class WalletActivity extends AppCompatActivity {
    @Bind(R.id.transactionHistoryRecyclerView)RecyclerView mTransactionHistoryRecyclerView;
    @Bind(R.id.currentWalletBalanceTextview)TextView mCurrentWalletBalanceTextView;
    @Bind(R.id.emptyRelativeLayout)RelativeLayout mEmptyRelativeLayout;
    private FirebaseRecyclerAdapter firebaseRecyclerAdapter;
    private DatabaseReference transactionReference;
    private DatabaseReference walletReference;
    private DatabaseReference cinglesReference;
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
            transactionReference = FirebaseDatabase.getInstance().getReference(Constants.TRANSACTION_HISTORY)
                    .child(firebaseAuth.getCurrentUser().getUid());
            walletReference = FirebaseDatabase.getInstance().getReference(Constants.WALLET);
            cinglesReference = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CINGLES);

            walletReference.keepSynced(true);
            transactionReference.keepSynced(true);

            setTransactionHistory();
            setCurrentWalletBalance();

        }
    }

    public void setTransactionHistory(){
        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<TransactionDetails, TransactionHistoryViewHolder>
                (TransactionDetails.class, R.layout.transaction_history_layout,
                        TransactionHistoryViewHolder.class, transactionReference) {

            @Override
            public int getItemCount() {
                return super.getItemCount();
            }

            @Override
            public void onBindViewHolder(TransactionHistoryViewHolder viewHolder, int position) {
                super.onBindViewHolder(viewHolder, position);
            }

            @Override
            public TransactionHistoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                return super.onCreateViewHolder(parent, viewType);
            }

            @Override
            protected void populateViewHolder(final TransactionHistoryViewHolder viewHolder,
                                              TransactionDetails model, int position) {

                if (transactionReference != null){
                    mEmptyRelativeLayout.setVisibility(View.GONE);
                    viewHolder.bindTransactionHistory(model);
                }else {
                    mEmptyRelativeLayout.setVisibility(View.VISIBLE);
                }

                final String postKey = getRef(position).getKey();

                //SET THE CINGLE IMAGE
                transactionReference.child(postKey).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            final String postId = (String) dataSnapshot.child("postId").getValue();

                            cinglesReference.child(postId).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()){
                                        final Cingle cingle = dataSnapshot.getValue(Cingle.class);
                                        final String uid = cingle.getUid();

                                        Picasso.with(WalletActivity.this)
                                                .load(cingle.getCingleImageUrl())
                                                .networkPolicy(NetworkPolicy.OFFLINE)
                                                .into(viewHolder.cingleImageView, new Callback() {
                                                    @Override
                                                    public void onSuccess() {

                                                    }

                                                    @Override
                                                    public void onError() {
                                                        Picasso.with(WalletActivity.this)
                                                                .load(cingle.getCingleImageUrl())
                                                                .into(viewHolder.cingleImageView);


                                                    }
                                                });

                                        viewHolder.cingleImageView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                Intent intent = new Intent(WalletActivity.this, TradeDetailActivity.class);
                                                intent.putExtra(WalletActivity.EXTRA_POST_KEY, postId);
                                                startActivity(intent);
                                            }
                                        });
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                viewHolder.deleteHistoryImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        transactionReference.child(postKey).removeValue();
                    }
                });

            }
        };


        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setAutoMeasureEnabled(true);
        mTransactionHistoryRecyclerView.setAdapter(firebaseRecyclerAdapter);
        mTransactionHistoryRecyclerView.setHasFixedSize(false);
        mTransactionHistoryRecyclerView.setLayoutManager(layoutManager);
    }

    public void setCurrentWalletBalance(){
        walletReference.child("balance").child(firebaseAuth.getCurrentUser().getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                       if (dataSnapshot.exists()){
                           Balance balance = dataSnapshot.getValue(Balance.class);
                           final double walletBalance = balance.getTotalBalance();

                           mCurrentWalletBalanceTextView.setText("CSC" + " " + formatter.format(walletBalance));
                       }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

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
        firebaseRecyclerAdapter.cleanup();
    }

}

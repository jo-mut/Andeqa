package com.andeqa.andeqa.wallet;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.R;
import com.andeqa.andeqa.models.Wallet;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.text.DecimalFormat;

import butterknife.Bind;
import butterknife.ButterKnife;

public class WalletActivity extends AppCompatActivity implements View.OnClickListener{
    @Bind(R.id.walletBalanceTextView)TextView mWalletBalanceTextView;
    @Bind(R.id.transactionHistoryLinearLayout)LinearLayout mTransactionHistoryLinearLayout;
    @Bind(R.id.sendCreditLinearLayout)LinearLayout mSendCreditLinearLayout;
    @Bind(R.id.redeemCreditLinearLayout)LinearLayout mRedeemCreditLinearLayout;
//    @Bind(R.id.createAccountAddressImageView)ImageView mCreateAccountAddressImageView;
    //firestore
    private CollectionReference walletReference;
    private FirebaseAuth firebaseAuth;
    private DecimalFormat formatter =  new DecimalFormat("0.00000000");
    private static final String TAG = WalletActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet);
        ButterKnife.bind(this);


        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationIcon(R.drawable.ic_arrow);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        mRedeemCreditLinearLayout.setOnClickListener(this);
        mSendCreditLinearLayout.setOnClickListener(this);
        mTransactionHistoryLinearLayout.setOnClickListener(this);
//        mCreateAccountAddressImageView.setOnClickListener(this);

        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser()!= null){

            //firestore
            walletReference = FirebaseFirestore.getInstance().collection(Constants.WALLET);

        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        setCurrentWalletBalance();
    }


    @Override
    public void onStop(){
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View v){
        if (v == mRedeemCreditLinearLayout){
            Intent intent = new Intent(WalletActivity.this, RedeemActivity.class);
            startActivity(intent);

        }

        if (v == mSendCreditLinearLayout){
            Intent intent = new Intent(WalletActivity.this, SendCreditActivity.class);
            startActivity(intent);
        }

        if (v == mTransactionHistoryLinearLayout){
            Intent intent = new Intent(WalletActivity.this, TransactionLogActivity.class);
            startActivity(intent);
        }

//        if (v == mCreateAccountAddressImageView){
//            walletReference.document(firebaseAuth.getCurrentUser().getUid())
//                    .collection("account_addresses").document();
//            final String address = walletReference.getId();
//            final long time = new Date().getTime();
//            Wallet wallet = new Wallet();
//            wallet.setAddress(address);
//            wallet.setBalance(0.0);
//            wallet.setTime(time);
//            wallet.setUser_id(firebaseAuth.getCurrentUser().getUid());
//            walletReference.add(time);
//        }
    }

    public void setCurrentWalletBalance(){
        walletReference.document(firebaseAuth.getCurrentUser().getUid())
                .collection("account_addresses").document(firebaseAuth.getCurrentUser().getUid())
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (documentSnapshot.exists()){
                    Wallet wallet = documentSnapshot.toObject(Wallet.class);
                    final double walletBalance = wallet.getBalance();

                    mWalletBalanceTextView.setText("Credo" +" " + formatter.format(walletBalance));
                }else {
                    mWalletBalanceTextView.setText("Credo"  +" " + 0.00000000);
                }

            }
        });

    }


}

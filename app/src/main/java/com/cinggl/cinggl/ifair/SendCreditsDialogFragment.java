package com.cinggl.cinggl.ifair;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.cinggl.cinggl.Constants;
import com.cinggl.cinggl.R;
import com.cinggl.cinggl.models.Balance;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class SendCreditsDialogFragment extends DialogFragment implements View.OnClickListener{
    @Bind(R.id.amountEnteredEditText)EditText mAmountEnteredEditText;
    @Bind(R.id.sendAmountButton)Button mSendAmountButton;

    private String mPostKey;
    private static final String EXTRA_POST_KEY = "post key";
    private boolean processPay = false;
    private DatabaseReference walletReference;
    private DatabaseReference cingleWalletReference;
    private FirebaseAuth firebaseAuth;

    public static SendCreditsDialogFragment newInstance(String title){
        SendCreditsDialogFragment sendCreditsDialogFragment = new SendCreditsDialogFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        sendCreditsDialogFragment.setArguments(args);
        return sendCreditsDialogFragment;
    }

    public SendCreditsDialogFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_send_credits_dialog, container, false);
        ButterKnife.bind(this, view);

        mSendAmountButton.setOnClickListener(this);

        firebaseAuth = FirebaseAuth.getInstance();

        Bundle bundle = getArguments();
        if (bundle != null){
            mPostKey = bundle.getString(SendCreditsDialogFragment.EXTRA_POST_KEY);

            Log.d("the passed poskey", mPostKey);

        }else {
            throw new IllegalArgumentException("pass an EXTRA_POST_KEY");
        }

        walletReference = FirebaseDatabase.getInstance().getReference(Constants.WALLET);
        cingleWalletReference = FirebaseDatabase.getInstance().getReference(Constants.CINGLE_WALLET);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String title = getArguments().getString("title", "send credits");
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
    }

    @Override
    public void onClick(View v){
        if (v == mSendAmountButton){
            processPay = true;
            if (mAmountEnteredEditText != null){
                final String amountInString = mAmountEnteredEditText.getText().toString();
                final double amountEntered = Double.parseDouble(amountInString);
                Log.d("amount entered", amountEntered + "");

                walletReference.child("balance").child(firebaseAuth.getCurrentUser().getUid())
                        .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            if (processPay){
                                try {
//                                    Balance balance = dataSnapshot.child("total balance").getValue(Balance.class);
//                                    final double currentBalance = balance.getTotalBalance();
//
                                    final Double currentBalance = dataSnapshot.getValue(Double.class);
                                    Log.d("current balance", currentBalance + "");

                                    if (amountEntered > currentBalance){
                                        mAmountEnteredEditText.setError("Your wallet has insufficient balance");
                                    }else if (mAmountEnteredEditText.equals("")){
                                        mAmountEnteredEditText.setError("Amount cannot be empty");

                                    }else if(amountEntered < 0){
                                        mAmountEnteredEditText.setError("Amount cannot be zero");
                                    } else{

                                        //RECORD THE NEW BALANCE IN THE CINGLE WALLET
                                        cingleWalletReference.child(mPostKey)
                                                .child("amount deposited")
                                                .addValueEventListener(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                        final Double cingleBalance = dataSnapshot.getValue(Double.class);
                                                        Log.d("cingle balance", dataSnapshot.getValue() + "");
                                                        final double newCingleBalance = cingleBalance + amountEntered;
                                                        cingleWalletReference.child(mPostKey).child("cingle csc wallet")
                                                                .setValue(newCingleBalance).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()){
                                                                    walletReference.child("balance").child(firebaseAuth.getCurrentUser().getUid())
                                                                            .addValueEventListener(new ValueEventListener() {
                                                                        @Override
                                                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                                                            //SET THE NEW WALLET BALANCE AFTER THE MONEY HAS BEEN TRANSAFERED
                                                                            try {
                                                                                Balance b = dataSnapshot.getValue(Balance.class);
                                                                                final double currentBalance = b.getTotalBalance();
                                                                                final double newBalance = currentBalance + amountEntered;

                                                                                Log.d("new balance", newBalance + "");
                                                                                walletReference.child("balance").child(firebaseAuth.getCurrentUser()
                                                                                        .getUid()).setValue(newBalance);
                                                                            } catch (Exception e) {
                                                                                e.printStackTrace();
                                                                            }

                                                                        }

                                                                        @Override
                                                                        public void onCancelled(DatabaseError databaseError) {

                                                                        }
                                                                    });
                                                                }
                                                            }
                                                        });
                                                    }

                                                    @Override
                                                    public void onCancelled(DatabaseError databaseError) {

                                                    }
                                                });

                                        Toast.makeText(getContext(), "Transaction not successful. Please try again later",
                                                Toast.LENGTH_LONG).show();
                                    }

                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                                //FINISH THE TRANSACTION PROCESS AND READY FOR A NEW ONE
                                processPay = false;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                //RESET THE EDITTEXT
                mAmountEnteredEditText.setText("");
            }

        }

    }


//    @Override
//    public void onClick(View v){
//        if (v == mSendAmountButton){
//            processPay = true;
//            if (mAmountEnteredEditText != null){
//                final String amountInString = mAmountEnteredEditText.getText().toString();
//                final double amountEntered = Double.parseDouble(amountInString);
//
//                Log.d("amount entered", amountEntered + "");
//                walletReference.child("current balance").child(firebaseAuth.getCurrentUser().getUid())
//                        .child("total balance").addValueEventListener(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(DataSnapshot dataSnapshot) {
//                        if (dataSnapshot.exists()){
//                            if (processPay){
//                                try {
//                                    Balance balance = dataSnapshot.getValue(Balance.class);
//                                    final double currentBalance = balance.getTotalBalance();
//
//                                    Log.d("current balance", currentBalance + "");
//
//                                    if (amountEntered > currentBalance){
//                                        mAmountEnteredEditText.setError("Your wallet has insufficient balance");
//                                    }else if (mAmountEnteredEditText.equals("")){
//                                        mAmountEnteredEditText.setError("Amount cannot be empty");
//
//                                    }else if(amountEntered < 0){
//                                        mAmountEnteredEditText.setError("Amount cannot be zero");
//                                    } else{
//                                        //CREATE THE CINGLE WALLET IF ITS NOT THERE
//                                        cingleWalletReference.child(mPostKey).child("cingle wallet")
//                                                .child("cingle csc balance").setValue(amountEntered)
//                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                                    @Override
//                                                    public void onComplete(@NonNull Task<Void> task) {
//                                                        try {
//                                                            //RECORD THE NEW BALANCE IN THE CINGLE WALLET
//                                                            cingleWalletReference.child(mPostKey).child("cingle wallet")
//                                                                    .child("cingle csc balance")
//                                                                    .addValueEventListener(new ValueEventListener() {
//                                                                        @Override
//                                                                        public void onDataChange(DataSnapshot dataSnapshot) {
//                                                                            Balance cingleBalance = new Balance();
//                                                                            Log.d("cingle balance", cingleBalance + "");
//                                                                            final double newCingleBalance = cingleBalance.getTotalBalance() + amountEntered;
//                                                                            cingleWalletReference.child("cingle wallet").child("cingle csc wallet")
//                                                                                    .setValue(newCingleBalance);
//                                                                        }
//
//                                                                        @Override
//                                                                        public void onCancelled(DatabaseError databaseError) {
//
//                                                                        }
//                                                                    });
//
//                                                            //SET THE NEW WALLET BALANCE AFTER THE MPONEY HAS BEEN TRANSAFERED
//                                                            Balance walletBalance = new Balance();
//                                                            final double newWalletBalance = walletBalance.getTotalBalance() - amountEntered;
//                                                            Log.d(newWalletBalance + "", "new wallet balance");
//                                                            walletReference.child("current balance").child(firebaseAuth.getCurrentUser().getUid())
//                                                                    .child("total balance").setValue(newWalletBalance);
//
//                                                        }catch (Exception e){
//                                                            e.printStackTrace();
//                                                        }
//                                                    }
//                                                });
//
//                                        Toast.makeText(getContext(), "Transaction not successful. Please try again later",
//                                                Toast.LENGTH_LONG).show();
//                                    }
//
//                                }catch (Exception e){
//                                    e.printStackTrace();
//                                }
//                                //FINISH THE TRANSACTION PROCESS AND READY FOR A NEW ONE
//                                processPay = false;
//                            }
//                        }
//                    }
//
//                    @Override
//                    public void onCancelled(DatabaseError databaseError) {
//
//                    }
//                });
//                //RESET THE EDITTEXT
//                mAmountEnteredEditText.setText("");
//            }
//
//        }
//    }

}

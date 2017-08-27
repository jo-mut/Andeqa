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
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.cinggl.cinggl.Constants;
import com.cinggl.cinggl.R;
import com.cinggl.cinggl.models.Balance;
import com.cinggl.cinggl.models.Cingle;
import com.cinggl.cinggl.models.TransactionDetails;
import com.cinggl.cinggl.profile.UpdateProfileActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.lang.ref.WeakReference;
import java.text.DecimalFormat;

import butterknife.Bind;
import butterknife.ButterKnife;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;
import static com.cinggl.cinggl.R.id.cingleSenseCreditsTextView;

/**
 * A simple {@link Fragment} subclass.
 */
public class RedeemCreditsDialogFragment extends DialogFragment implements View.OnClickListener {
    @Bind(R.id.amountEnteredEditText)EditText mAmountEnteredEditText;
    @Bind(R.id.redeemAmountButton)Button mRedeemAmountButton;

    private String mPostKey;
    private static final String EXTRA_POST_KEY = "post key";
    private FirebaseAuth firebaseAuth;
    private DatabaseReference cinglesReference;
    private DatabaseReference walletReference;
    private boolean redeemCSC = false;

    public static RedeemCreditsDialogFragment newInstance(String title){
        RedeemCreditsDialogFragment redeemCreditsDialogFragment = new RedeemCreditsDialogFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        redeemCreditsDialogFragment.setArguments(args);
        return  redeemCreditsDialogFragment;

    }

    public RedeemCreditsDialogFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        cinglesReference = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CINGLES);
        walletReference = FirebaseDatabase.getInstance().getReference(Constants.WALLET);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_redeem_credits_dialog, container, false);
        ButterKnife.bind(this, view);

        firebaseAuth = FirebaseAuth.getInstance();

        Bundle bundle = getArguments();
        if (bundle != null){
            mPostKey = bundle.getString(RedeemCreditsDialogFragment.EXTRA_POST_KEY);

            Log.d("the passed poskey", mPostKey);

        }else {
            throw new IllegalArgumentException("pass an EXTRA_POST_KEY");
        }

        mRedeemAmountButton.setOnClickListener(this);

        return view;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String title = getArguments().getString("title", "redeem credits");
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
    }

    @Override
    public void onClick(View v) {
        if (v == mRedeemAmountButton) {
            redeemCSC = true;
            if (mAmountEnteredEditText != null) {
                final String amountInString = mAmountEnteredEditText.getText().toString();
//                            final double amountInInt = Integer.valueOf(amountInString).intValue();
                final double amountEntered = Double.parseDouble(amountInString);

                cinglesReference.child(mPostKey).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (redeemCSC) {
                            Cingle cingle = dataSnapshot.getValue(Cingle.class);
                            final double sensepoint = cingle.getSensepoint();
                            Log.d("amount of sensepoint", sensepoint + "");

                            if (mAmountEnteredEditText.getText().equals("")) {
                                mAmountEnteredEditText.setError("Amount cannot be empty");
                            } else if (amountEntered > sensepoint) {
                                mAmountEnteredEditText.setError("Your balance is insufficient");
                            } else {
                                //RECORD BALANCE AMOUNT AFTER REDEMPTION
                                final double sensecredits = sensepoint - amountEntered;

                                Log.d("amount of sensecredits", sensecredits + "");

                                cinglesReference.child(mPostKey).child("sensepoint").setValue(sensecredits);

                                //RECORD TRANSACTION HISTORY OF ALL CSC REDEEMED
                                TransactionDetails transactionDetails = new TransactionDetails();
                                transactionDetails.setAmount(amountEntered);
                                transactionDetails.setUid(firebaseAuth.getCurrentUser().getUid());
                                walletReference.child("Redeeming csc history")
                                        .child(firebaseAuth.getCurrentUser().getUid())
                                        .push().setValue(transactionDetails);

                                final Balance balance = new Balance();
                                balance.setTotalBalance(amountEntered);
                                walletReference.child("current balance").child(firebaseAuth.getCurrentUser()
                                        .getUid()).child("total balance").setValue(balance)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    walletReference.child("current balance").child(firebaseAuth.getCurrentUser().getUid())
                                                            .child("total balance").addValueEventListener(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                                            try {
                                                                Balance b = dataSnapshot.getValue(Balance.class);
                                                                final double currentBalance = b.getTotalBalance();
                                                                final double newBalance = currentBalance + amountEntered;

                                                                Log.d("new balance", newBalance + "");
                                                                walletReference.child("current balance").child(firebaseAuth.getCurrentUser()
                                                                        .getUid()).child("total balance").setValue(newBalance);
                                                            } catch (Exception e) {
                                                                e.printStackTrace();
                                                            }
                                                        }

                                                        @Override
                                                        public void onCancelled(DatabaseError databaseError) {

                                                        }
                                                    });
                                                } else {
                                                    Toast.makeText(getContext(), "Transaction not successful. Please try again later",
                                                            Toast.LENGTH_LONG).show();
                                                }
                                            }
                                        });
                            }
                            //FINISH THE TRANSACTION PROCESS AND READY FOR A NEW ONE
                            redeemCSC = false;
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
}

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
            if (mAmountEnteredEditText != null){
                final String amountInString = mAmountEnteredEditText.getText().toString();
                final double amountEntered = Double.parseDouble(amountInString);
                Log.d("amount entered", amountEntered + "");

                walletReference.child("balance").child(firebaseAuth.getCurrentUser().getUid())
                        .child("total balance")
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            final Double currentBalance = dataSnapshot.getValue(Double.class);
                            Log.d("old wallet balance", currentBalance + "");

                            if (amountEntered > currentBalance){
                                mAmountEnteredEditText.setError("Your wallet has insufficient balance");
                            }else if (mAmountEnteredEditText.equals("")){
                                mAmountEnteredEditText.setError("Amount cannot be empty");

                            }else if(amountEntered < 0){
                                mAmountEnteredEditText.setError("Amount cannot be zero");
                            } else{
                                final double newWalletBalance = currentBalance - amountEntered;
                                Log.d("new wallet balance", newWalletBalance + "");
                                walletReference.child("balance").child(firebaseAuth.getCurrentUser().getUid())
                                        .child("total balance").setValue(newWalletBalance)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()){
                                            cingleWalletReference.child(mPostKey).child("amount deposited")
                                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                                            if (dataSnapshot.exists()){
                                                                final Double cingleBalance = dataSnapshot.getValue(Double.class);
                                                                Log.d("old cingle balance", dataSnapshot.getValue() + "");
                                                                final double newCingleBalance = cingleBalance + amountEntered;
                                                                Log.d("new cingle balance", newCingleBalance + "");
                                                                cingleWalletReference.child(mPostKey).child("amount deposited")
                                                                        .setValue(newCingleBalance).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        if (task.isSuccessful()){
                                                                            Toast.makeText(getContext(), "Transaction successful",
                                                                                    Toast.LENGTH_LONG).show();
                                                                        }
                                                                    }
                                                                });
                                                            }else {
                                                                final double newCingleBalance = amountEntered;
                                                                Log.d("new cingle balance", newCingleBalance + "");
                                                                cingleWalletReference.child(mPostKey).child("amount deposited")
                                                                        .setValue(newCingleBalance).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        if (task.isSuccessful()){
                                                                            Toast.makeText(getContext(), "Transaction not successful." +
                                                                                            " Please try again later",
                                                                                    Toast.LENGTH_LONG).show();
                                                                        }
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
                                });
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

}

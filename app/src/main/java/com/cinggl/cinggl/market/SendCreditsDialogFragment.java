package com.cinggl.cinggl.market;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.method.DigitsKeyListener;
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
import com.cinggl.cinggl.models.PostSale;
import com.cinggl.cinggl.models.TransactionDetails;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

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
    //firebase
    private DatabaseReference walletReference;
    private DatabaseReference cingleWalletReference;
    //firestore
    private CollectionReference cingleOwnersReference;
    private CollectionReference ifairReference;
    private FirebaseAuth firebaseAuth;

    //REMOVE SCIENTIFIC NOATATION
    private DecimalFormat formatter =  new DecimalFormat("0.00000000");

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

        if (firebaseAuth.getCurrentUser() != null){
            Bundle bundle = getArguments();
            if (bundle != null){
                mPostKey = bundle.getString(SendCreditsDialogFragment.EXTRA_POST_KEY);

                Log.d("the passed poskey", mPostKey);

            }else {
                throw new IllegalArgumentException("pass an EXTRA_POST_KEY");
            }

            //initialize input filter
            setEditTextFilter();
            //firebase
            walletReference = FirebaseDatabase.getInstance().getReference(Constants.WALLET);
            cingleWalletReference = FirebaseDatabase.getInstance().getReference(Constants.CINGLE_WALLET);
            //firestore
            cingleOwnersReference = FirebaseFirestore.getInstance().collection(Constants.CINGLE_ONWERS);
            ifairReference = FirebaseFirestore.getInstance().collection(Constants.IFAIR);

        }
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String title = getArguments().getString("title", "send credits");
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

    }

    @Override
    public void onClick(View v){
        if (v == mSendAmountButton){
            if (mAmountEnteredEditText != null){
                final String amountInString = mAmountEnteredEditText.getText().toString();
                final double amountTransferred = Double.parseDouble(amountInString);
                Log.d("amount entered", amountTransferred + "");
                final String formattedString = formatter.format(amountTransferred);

                //get the current date
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("d");
                String date = simpleDateFormat.format(new Date());

                if (date.endsWith("1") && !date.endsWith("11"))
                    simpleDateFormat = new SimpleDateFormat("d'st' MMM yyyy");
                else if (date.endsWith("2") && !date.endsWith("12"))
                    simpleDateFormat = new SimpleDateFormat("d'nd' MMM yyyy");
                else if (date.endsWith("3") && !date.endsWith("13"))
                    simpleDateFormat = new SimpleDateFormat("d'rd' MMM yyyy");
                else
                    simpleDateFormat = new SimpleDateFormat("d'th' MMM yyyy");
                final String currentDate = simpleDateFormat.format(new Date());

                ifairReference.document(mPostKey).get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()){
                            final PostSale postSale = documentSnapshot.toObject(PostSale.class);

                            final double salePrice = postSale.getSalePrice();
                            Log.d("cingle sale price", salePrice + "");

                            if (amountTransferred < salePrice){
                                mAmountEnteredEditText.setError("Your wallet has insufficient balance");
                            }else if (mAmountEnteredEditText.equals("")){
                                mAmountEnteredEditText.setError("Amount cannot be empty");
                            }else  if (amountTransferred > salePrice){
                                mAmountEnteredEditText.setError("Amount is more than sale price");
                            } else{
                                walletReference.child("balance").child(firebaseAuth.getCurrentUser().getUid())
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                if (dataSnapshot.exists()){
                                                    Balance walletBalance = dataSnapshot.getValue(Balance.class);
                                                    final double currentBalance = walletBalance.getTotalBalance();
                                                    Log.d("old wallet balance", currentBalance + "");

                                                    final double newWalletBalance = currentBalance - amountTransferred;
                                                    Log.d("new wallet balance", newWalletBalance + "");

                                                    //record new wallet balance
                                                    final Balance balance = new Balance();
                                                    balance.setTotalBalance(newWalletBalance);

                                                    walletReference.child("balance").child(firebaseAuth.getCurrentUser()
                                                            .getUid()).setValue(balance)
                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if (task.isSuccessful()){
                                                                        cingleWalletReference.child(mPostKey).child("amount deposited")
                                                                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                    @Override
                                                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                                                        if (dataSnapshot.exists()){
                                                                                            Balance cingleWalletBalance = (Balance)dataSnapshot.getValue();
                                                                                            final double currentBalance = cingleWalletBalance.getTotalBalance();
                                                                                            Log.d("old cingle balance", currentBalance + "");
                                                                                            final double newCingleBalance = currentBalance + amountTransferred;
                                                                                            Log.d("new cingle balance", newCingleBalance + "");

                                                                                            //record new cingle wallet balance
                                                                                            balance.setAmountDeposited(newCingleBalance);

                                                                                            cingleWalletReference.child(mPostKey).child("amount deposited")
                                                                                                    .setValue(balance).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                @Override
                                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                                    if (task.isSuccessful()){
                                                                                                        Toast.makeText(getContext(), "Transaction successful",
                                                                                                                Toast.LENGTH_LONG).show();
                                                                                                    }
                                                                                                }
                                                                                            });

                                                                                            TransactionDetails transactionDetails = new TransactionDetails();
                                                                                            transactionDetails.setPushId(mPostKey);
                                                                                            transactionDetails.setUid(firebaseAuth.getCurrentUser().getUid());
                                                                                            transactionDetails.setAmount(amountTransferred);
                                                                                            transactionDetails.setWalletBalance(newWalletBalance);
                                                                                            transactionDetails.setDate(currentDate);

                                                                                            DocumentReference ownerRef = cingleOwnersReference.document("Ownership")
                                                                                                    .collection(mPostKey).document("Owner");
                                                                                            ownerRef.set(transactionDetails);
                                                                                            DocumentReference historyRef = cingleOwnersReference.document(mPostKey)
                                                                                                    .collection("Ownership history").document();
                                                                                            final String historyId = historyRef.getId();
                                                                                            transactionDetails.setCingleId(historyId);
                                                                                            historyRef.set(transactionDetails);
                                                                                            //once cingle has been bought remove it from cingle selling
                                                                                            ifairReference.document(mPostKey).delete();

                                                                                        }else {
                                                                                            final double newCingleBalance = amountTransferred;
                                                                                            Log.d("new cingle balance", newCingleBalance + "");
                                                                                            balance.setAmountDeposited(amountTransferred);
                                                                                            cingleWalletReference.child(mPostKey).child("amount deposited")
                                                                                                    .setValue(balance).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                @Override
                                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                                    if (task.isSuccessful()){
                                                                                                        Toast.makeText(getContext(), "Transaction not successful." +
                                                                                                                        " Please try again later",
                                                                                                                Toast.LENGTH_LONG).show();
                                                                                                    }
                                                                                                }
                                                                                            });

                                                                                            TransactionDetails transactionDetails = new TransactionDetails();
                                                                                            transactionDetails.setPushId(mPostKey);
                                                                                            transactionDetails.setUid(firebaseAuth.getCurrentUser().getUid());
                                                                                            transactionDetails.setAmount(amountTransferred);
                                                                                            transactionDetails.setWalletBalance(newWalletBalance);
                                                                                            transactionDetails.setDate(currentDate);


                                                                                            DocumentReference ownerRef = cingleOwnersReference.document("Ownership")
                                                                                                    .collection(mPostKey).document("Owner");
                                                                                            ownerRef.set(transactionDetails);
                                                                                            DocumentReference historyRef = cingleOwnersReference.document(mPostKey)
                                                                                                    .collection("Ownership history").document();
                                                                                            final String historyId = historyRef.getId();
                                                                                            transactionDetails.setCingleId(historyId);
                                                                                            historyRef.set(transactionDetails);

                                                                                            //once cingle has been bought remove it from cingle selling
                                                                                            ifairReference.document(mPostKey).delete();
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

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });
                            }
                        }
                    }
                });
                //RESET THE EDITTEXT
                mAmountEnteredEditText.setText("");
            }

        }

    }

    public void setEditTextFilter(){
        mAmountEnteredEditText.setFilters(new InputFilter[] {
                new DigitsKeyListener(Boolean.FALSE, Boolean.TRUE) {
                    int beforeDecimal = 6, afterDecimal = 8;

                    @Override
                    public CharSequence filter(CharSequence source, int start, int end,
                                               Spanned dest, int dstart, int dend) {
                        String temp = mAmountEnteredEditText.getText() + source.toString();

                        if (temp.equals(".")) {
                            return "0.";
                        }else if (temp.equals("0")){
                            return "0.";//if number begins with 0 return decimal place right after
                        }
                        else if (temp.toString().indexOf(".") == -1) {
                            // no decimal point placed yet
                            if (temp.length() > beforeDecimal) {
                                return "";
                            }
                        } else {
                            temp = temp.substring(temp.indexOf(".") + 1);
                            if (temp.length() > afterDecimal) {
                                return "";
                            }
                        }

                        return super.filter(source, start, end, dest, dstart, dend);
                    }
                }
        });

    }

}

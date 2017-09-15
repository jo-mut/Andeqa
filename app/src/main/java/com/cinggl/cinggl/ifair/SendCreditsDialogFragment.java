package com.cinggl.cinggl.ifair;


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
import com.cinggl.cinggl.models.CingleSale;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;

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
    private DatabaseReference cingleOwnersReference;
    private DatabaseReference cingleWalletReference;
    private DatabaseReference ifairReference;
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

            walletReference = FirebaseDatabase.getInstance().getReference(Constants.WALLET);
            cingleWalletReference = FirebaseDatabase.getInstance().getReference(Constants.CINGLE_WALLET);
            ifairReference = FirebaseDatabase.getInstance().getReference(Constants.IFAIR);
            cingleOwnersReference = FirebaseDatabase.getInstance().getReference(Constants.CINGLE_ONWERS);

        }
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
                final String formattedString = formatter.format(amountEntered);

                ifairReference.child("Cingle Selling").child(mPostKey).addListenerForSingleValueEvent
                        (new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                       if (dataSnapshot.exists()){
                           CingleSale cingleSale = dataSnapshot.getValue(CingleSale.class);
                           final double salePrice = cingleSale.getSalePrice();
                           Log.d("cingle sale price", salePrice + "");

                           if (amountEntered > salePrice){
                               mAmountEnteredEditText.setError("Your wallet has insufficient balance");
                           }else if (mAmountEnteredEditText.equals("")){
                               mAmountEnteredEditText.setError("Amount cannot be empty");
                           }else{
                               walletReference.child("balance").child(firebaseAuth.getCurrentUser().getUid())
                                       .child("total balance")
                                       .addListenerForSingleValueEvent(new ValueEventListener() {
                                           @Override
                                           public void onDataChange(DataSnapshot dataSnapshot) {
                                               if (dataSnapshot.exists()){
                                                   final Double currentBalance = dataSnapshot.getValue(Double.class);
                                                   Log.d("old wallet balance", currentBalance + "");

                                                   final double newWalletBalance = currentBalance - amountEntered;
                                                   Log.d("new wallet balance", newWalletBalance + "");
                                                   walletReference.child("balance").child(firebaseAuth.getCurrentUser()
                                                           .getUid()).child("total balance").setValue(newWalletBalance)
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

                                                                                           cingleOwnersReference.child(mPostKey).child("owner").setValue(firebaseAuth.getCurrentUser()
                                                                                                   .getUid());


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

                                                                                           cingleOwnersReference.child(mPostKey).child("owner").setValue(firebaseAuth.getCurrentUser()
                                                                                                   .getUid());

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

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

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

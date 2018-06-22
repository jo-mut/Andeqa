package com.andeqa.andeqa.wallet;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.R;
import com.andeqa.andeqa.models.Andeqan;
import com.andeqa.andeqa.models.Transaction;
import com.andeqa.andeqa.models.Wallet;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Date;

import javax.annotation.Nullable;

import butterknife.Bind;
import butterknife.ButterKnife;

public class SendCreditActivity extends AppCompatActivity implements View.OnClickListener {
    @Bind(R.id.sendCreditButton)Button mSendCreditButton;
    @Bind(R.id.recipientEditText)EditText mRecipientEditText;
    @Bind(R.id.amountEditText)EditText mAmountEditText;
    @Bind(R.id.descriptionEditText)EditText mDescriptionEditText;
    @Bind(R.id.anonymousRadioButton)RadioButton mAnonymousRadioButton;

    private CollectionReference usersCollection;
    private CollectionReference walletsCollection;
    private CollectionReference transactionsCollection;
    private FirebaseAuth firebaseAuth;
    private static final String TAG = SendCreditActivity.class.getSimpleName();
    private boolean checkedRadioButton  =  true;
    private DecimalFormat formatter =  new DecimalFormat("0.00000000");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_credit);
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

        mSendCreditButton.setOnClickListener(this);
        setEditTextFilter();

        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null){
            walletsCollection = FirebaseFirestore.getInstance().collection(Constants.WALLET);
            usersCollection = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);
            transactionsCollection = FirebaseFirestore.getInstance().collection(Constants.TRANSACTION_HISTORY);
        }

    }


    @Override
    public void onClick(View v) {
        if (v == mSendCreditButton){
            checkIfTransactionIsAnonymous();
        }

        if (v == mAnonymousRadioButton){
            if (checkedRadioButton){
                mAnonymousRadioButton.setChecked(false);
                checkedRadioButton = false;
            }else {
                mAnonymousRadioButton.setChecked(true);
                checkedRadioButton = true;
            }
        }
    }

    public void setEditTextFilter(){

        mAmountEditText.setFilters(new InputFilter[] {
                new DigitsKeyListener(Boolean.FALSE, Boolean.TRUE) {
                    int beforeDecimal = 6, afterDecimal = 8;

                    @Override
                    public CharSequence filter(CharSequence source, int start, int end,
                                               Spanned dest, int dstart, int dend) {
                        String temp = mAmountEditText.getText() + source.toString();

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

    private void processTransaction(){
        final String email = mRecipientEditText.getText().toString().trim();
        final String amount = mAmountEditText.getText().toString().trim();
        final String description  = mDescriptionEditText.getText().toString().trim();

        boolean validEmail = isValidEmail(email);
        if (!validEmail){
            return;
        }else if (!TextUtils.isEmpty(amount)){
            final double amountEntered = Double.parseDouble(amount);
            final String formattedString = formatter.format(amountEntered);
            final double amountTransferred = Double.parseDouble(formattedString);

            final long time = new Date().getTime();

            usersCollection.orderBy("email").whereEqualTo("email",email)
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                            if (e != null) {
                                Log.w(TAG, "Listen error", e);
                                return;
                            }

                            if (!queryDocumentSnapshots.isEmpty()){
                                for (final DocumentChange change : queryDocumentSnapshots.getDocumentChanges()) {
                                    Andeqan andeqan = change.getDocument().toObject(Andeqan.class);
                                    final String userId = andeqan.getUser_id();

                                    if (userId.equals(firebaseAuth.getCurrentUser().getUid())){
                                        Toast.makeText(SendCreditActivity.this,"You can not send money to yourself",
                                                Toast.LENGTH_SHORT).show();
                                    }else {
                                        walletsCollection.document(firebaseAuth.getCurrentUser().getUid())
                                                .collection("account_addresses")
                                                .document(firebaseAuth.getCurrentUser().getUid())
                                                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                            @Override
                                            public void onSuccess(DocumentSnapshot documentSnapshot) {

                                                if (documentSnapshot.exists()){
                                                    Wallet wallet = documentSnapshot.toObject(Wallet.class);
                                                    final double balance = wallet.getBalance();

                                                    if (amountTransferred > balance){
                                                        Toast.makeText(SendCreditActivity.this,"Your account has insufficient funds",
                                                                Toast.LENGTH_SHORT).show();
                                                    }else {
                                                        final double newBalance = balance - amountTransferred;
                                                        walletsCollection.document(firebaseAuth.getCurrentUser().getUid())
                                                                .collection("account_addresses")
                                                                .document(firebaseAuth.getCurrentUser().getUid()).update("balance", newBalance);

                                                        walletsCollection.document(userId).collection("account_addresses")
                                                                .document(userId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                                            @Override
                                                            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                                                                if (e != null) {
                                                                    Log.w(TAG, "Listen error", e);
                                                                    return;
                                                                }

                                                                if (documentSnapshot.exists()){
                                                                    Wallet wallet = documentSnapshot.toObject(Wallet.class);
                                                                    final double balance = wallet.getBalance();
                                                                    final double newBalance = balance + amountTransferred;
                                                                    final DocumentReference reference =   walletsCollection.document(userId)
                                                                            .collection("account_addresses").document(userId);
                                                                    reference.update("balance", newBalance);
                                                                }else {
                                                                    Wallet wallet = new Wallet();
                                                                    wallet.setRedeemed(0.0);
                                                                    wallet.setDeposited(amountTransferred);
                                                                    wallet.setBalance(amountTransferred);
                                                                    wallet.setTime(time);
                                                                    wallet.setUser_id(userId);
                                                                    wallet.setAddress(userId);

                                                                    final DocumentReference reference =   walletsCollection.document(userId)
                                                                            .collection("account_addresses").document(userId);
                                                                    reference.set(
                                                                            wallet);
                                                                }
                                                            }
                                                        });

                                                        DocumentReference reference = transactionsCollection
                                                                .document(firebaseAuth.getCurrentUser().getUid())
                                                                .collection("transactions").document();

                                                        DocumentReference ref = transactionsCollection
                                                                .document(userId)
                                                                .collection("transactions").document();

                                                        String referenceId = reference.getId();
                                                        Transaction sent = new Transaction();
                                                        sent.setAmount(amountTransferred);
                                                        sent.setPost_id(referenceId);
                                                        sent.setTransaction_id(referenceId);
                                                        sent.setTime(time);
                                                        sent.setUser_id(firebaseAuth.getCurrentUser().getUid());
                                                        sent.setType("sent");
                                                        sent.setReceiver_id(userId);
                                                        sent.setWallet_balance(newBalance);
                                                        sent.setDescription(description);

                                                        //set the push id
                                                        reference.set(sent);

                                                        String refId = ref.getId();
                                                        Transaction transaction = new Transaction();
                                                        transaction.setAmount(amountTransferred);
                                                        transaction.setPost_id(refId);
                                                        transaction.setTransaction_id(refId);
                                                        transaction.setTime(time);
                                                        transaction.setUser_id(firebaseAuth.getCurrentUser().getUid());
                                                        transaction.setType("received");
                                                        transaction.setReceiver_id(userId);
                                                        transaction.setWallet_balance(newBalance);
                                                        transaction.setDescription(description);

                                                        ref.set(transaction);

                                                        mRecipientEditText.setText("");
                                                        mAmountEditText.setText("");
                                                        mDescriptionEditText.setText("");

                                                        Toast.makeText(SendCreditActivity.this,"Transaction sent",
                                                                Toast.LENGTH_SHORT).show();
                                                    }
                                                }else {
                                                    Toast.makeText(SendCreditActivity.this,"Your account has insufficient funds",
                                                            Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    }

                                }
                            }else {
                                Toast.makeText(SendCreditActivity.this,"User with this email does not exist",
                                        Toast.LENGTH_SHORT).show();
                            }

                        }
                    });
        }else {
            mAmountEditText.setError("Amouunt cannot be empty");
        }


    }

    private void processAnonymousTransaction(){
        final String email = mRecipientEditText.getText().toString().trim();
        final String amount = mAmountEditText.getText().toString().trim();
        final String description  = mDescriptionEditText.getText().toString().trim();

        boolean validEmail = isValidEmail(email);
        if (!validEmail){
            return;
        }else if (!TextUtils.isEmpty(amount)){
            final double amountEntered = Double.parseDouble(amount);
            final double amountTransferred =roundCredits(amountEntered,8);

            final long time = new Date().getTime();

            usersCollection.orderBy("email").whereEqualTo("email",email)
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                            if (e != null) {
                                Log.w(TAG, "Listen error", e);
                                return;
                            }

                            if (!queryDocumentSnapshots.isEmpty()){
                                for (final DocumentChange change : queryDocumentSnapshots.getDocumentChanges()) {
                                    Andeqan andeqan = change.getDocument().toObject(Andeqan.class);
                                    final String userId = andeqan.getUser_id();
                                    Log.d("receiver user id", userId);

                                    if (userId.equals(firebaseAuth.getCurrentUser().getUid())){
                                        Toast.makeText(SendCreditActivity.this,"You can not send money to yourself",
                                                Toast.LENGTH_SHORT).show();
                                    }else {
                                        walletsCollection.document(firebaseAuth.getCurrentUser().getUid())
                                                .collection("account_addresses")
                                                .document(firebaseAuth.getCurrentUser().getUid())
                                                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                            @Override
                                            public void onSuccess(DocumentSnapshot documentSnapshot) {

                                                if (documentSnapshot.exists()){
                                                    Wallet wallet = documentSnapshot.toObject(Wallet.class);
                                                    final double balance = wallet.getBalance();

                                                    if (amountTransferred > balance){
                                                        Toast.makeText(SendCreditActivity.this,"Your account has insufficient funds",
                                                                Toast.LENGTH_SHORT).show();
                                                    }else {
                                                        final double newBalance = balance - amountTransferred;
                                                        walletsCollection.document(firebaseAuth.getCurrentUser().getUid()).collection("account_addresses")
                                                                .document(firebaseAuth.getCurrentUser().getUid()).update("balance", newBalance);

                                                        walletsCollection.document(userId).collection("account_addresses")
                                                                .document(userId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                                            @Override
                                                            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                                                                if (e != null) {
                                                                    Log.w(TAG, "Listen error", e);
                                                                    return;
                                                                }

                                                                if (documentSnapshot.exists()){
                                                                    Wallet wallet = documentSnapshot.toObject(Wallet.class);
                                                                    final double balance = wallet.getBalance();
                                                                    final double newBalance = balance + amountTransferred;
                                                                    final DocumentReference reference =   walletsCollection.document(userId)
                                                                            .collection("account_addresses").document(userId);
                                                                    reference.update("balance", newBalance);
                                                                }else {
                                                                    Wallet wallet = new Wallet();
                                                                    wallet.setRedeemed(0.0);
                                                                    wallet.setDeposited(amountTransferred);
                                                                    wallet.setBalance(amountTransferred);
                                                                    wallet.setTime(time);
                                                                    wallet.setUser_id(userId);
                                                                    wallet.setAddress(userId);

                                                                    final DocumentReference reference =   walletsCollection.document(userId)
                                                                            .collection("account_addresses").document(userId);
                                                                    reference.set(
                                                                            wallet);
                                                                }
                                                            }
                                                        });

                                                        DocumentReference reference = transactionsCollection
                                                                .document(firebaseAuth.getCurrentUser().getUid())
                                                                .collection("transactions").document();

                                                        DocumentReference ref = transactionsCollection
                                                                .document(userId)
                                                                .collection("transactions").document();

                                                        String referenceId = reference.getId();
                                                        Transaction sent = new Transaction();
                                                        sent.setAmount(amountTransferred);
                                                        sent.setPost_id(referenceId);
                                                        sent.setTransaction_id(referenceId);
                                                        sent.setTime(time);
                                                        sent.setUser_id("anonymous");
                                                        sent.setType("sent");
                                                        sent.setReceiver_id(userId);
                                                        sent.setWallet_balance(newBalance);
                                                        sent.setDescription(description);

                                                        //set the push id
                                                        reference.set(sent);

                                                        String refId = ref.getId();
                                                        Transaction transaction = new Transaction();
                                                        transaction.setAmount(amountTransferred);
                                                        transaction.setPost_id(refId);
                                                        transaction.setTransaction_id(refId);
                                                        transaction.setTime(time);
                                                        transaction.setUser_id("anonymous");
                                                        transaction.setType("received");
                                                        transaction.setReceiver_id(userId);
                                                        transaction.setWallet_balance(newBalance);
                                                        transaction.setDescription(description);

                                                        ref.set(transaction);

                                                        mRecipientEditText.setText("");
                                                        mAmountEditText.setText("");
                                                        mDescriptionEditText.setText("");

                                                        Toast.makeText(SendCreditActivity.this,"Transaction sent",
                                                                Toast.LENGTH_SHORT).show();
                                                    }
                                                }else {
                                                    Toast.makeText(SendCreditActivity.this,"Your account has insufficient funds",
                                                            Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    }

                                }
                            }else {
                                Toast.makeText(SendCreditActivity.this,"User with this email does not exist",
                                        Toast.LENGTH_SHORT).show();
                            }

                        }
                    });
        }else {
            mAmountEditText.setError("Amouunt cannot be empty");
        }

    }

    public void checkIfTransactionIsAnonymous(){
        if (mAnonymousRadioButton.isChecked()){
            processTransaction();
        }else {
            processAnonymousTransaction();
        }
    }

    private boolean isValidEmail(String email) {
        boolean isGoodEmail =
                (email != null &&  Patterns.EMAIL_ADDRESS.matcher(email).matches());
        if (!isGoodEmail) {
            mRecipientEditText.setError("Please enter the recipient valid email address");
            return false;
        }
        return isGoodEmail;
    }

    //region listeners
    private static double roundCredits(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }


}

package com.andeqa.andeqa.market;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.R;
import com.andeqa.andeqa.models.Andeqan;
import com.andeqa.andeqa.models.Balance;
import com.andeqa.andeqa.models.CollectionPost;
import com.andeqa.andeqa.models.Credit;
import com.andeqa.andeqa.models.TransactionDetails;
import com.andeqa.andeqa.profile.ProfileActivity;
import com.andeqa.andeqa.utils.ProportionalImageView;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class RedeemCreditsActivity extends AppCompatActivity implements View.OnClickListener{
    @Bind(R.id.amountEnteredEditText)EditText mAmountEnteredEditText;
    @Bind(R.id.redeemAmountButton)Button mRedeemAmountButton;

    @Bind(R.id.postImageView)ProportionalImageView mCingleImageView;
    @Bind(R.id.usernameTextView)TextView mAccountUsernameTextView;
    @Bind(R.id.profileImageView)CircleImageView mUserProfileImageView;

    private FirebaseAuth firebaseAuth;
    //firestore
    private CollectionReference transactionReference;
    private CollectionReference senseCreditReference;
    private CollectionReference postWalletReference;
    private CollectionReference walletReference;
    //adapters
    private FirebaseRecyclerAdapter firebaseRecyclerAdapter;
    private FirestoreRecyclerAdapter firestoreRecyclerAdapter;
    private boolean redeemCSC = false;
    private FirebaseUser firebaseUser;
    private String mPostKey;
    private static final String EXTRA_POST_KEY = "post id";
    private static final String EXTRA_USER_UID = "uid";
    private static final String COLLECTION_ID = "collection id";
    private String mCollectionId;

    private static final String TAG = RedeemCreditsActivity.class.getSimpleName();
    //firestore
    private CollectionReference collectionsCollection;
    private CollectionReference postsCollection;
    private CollectionReference usersReference;
    private CollectionReference relationsReference;
    private CollectionReference commentReference;
    private CollectionReference selllingCollection;

    private ProgressDialog progressDialog;


    //REMOVE SCIENTIFIC NOATATION
    private DecimalFormat formatter =  new DecimalFormat("0.00000000");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_redeem_credits);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


        mRedeemAmountButton.setOnClickListener(this);
        firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() != null){

            mPostKey = getIntent().getStringExtra(EXTRA_POST_KEY);
            if(mPostKey == null){
                throw new IllegalArgumentException("pass an EXTRA_POST_KEY");
            }

            mCollectionId = getIntent().getStringExtra(COLLECTION_ID);
            if (mCollectionId == null){
                throw new IllegalArgumentException("pass a collection id");
            }


            //firestore
            transactionReference = FirebaseFirestore.getInstance().collection(Constants.TRANSACTION_HISTORY);
            postWalletReference = FirebaseFirestore.getInstance().collection(Constants.POST_WALLET);
            walletReference = FirebaseFirestore.getInstance().collection(Constants.WALLET);
            senseCreditReference = FirebaseFirestore.getInstance().collection(Constants.U_CREDITS);

            collectionsCollection = FirebaseFirestore.getInstance().collection(Constants.COLLECTIONS_POSTS)
                    .document("collections").collection(mCollectionId);
            relationsReference = FirebaseFirestore.getInstance().collection(Constants.RELATIONS);
            postsCollection = FirebaseFirestore.getInstance().collection(Constants.POSTS);
            usersReference = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);
            commentReference = FirebaseFirestore.getInstance().collection(Constants.COMMENTS);
            senseCreditReference = FirebaseFirestore.getInstance().collection(Constants.U_CREDITS);
            selllingCollection = FirebaseFirestore.getInstance().collection(Constants.SELLING);


            //initialize input filters
            setEditTextFilter();
            mRedeemAmountButton.setOnClickListener(this);
            setData();
            redeeingDialog();

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

    public void setData(){
        //set the cingle image
        collectionsCollection.document(mPostKey).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (documentSnapshot.exists()){
                    final CollectionPost collectionPost = documentSnapshot.toObject(CollectionPost.class);
                    final String uid = collectionPost.getUserId();
                    final String title = collectionPost.getTitle();
                    final String image = collectionPost.getImage();

                    //set the single image
                    Picasso.with(RedeemCreditsActivity.this)
                            .load(image)
                            .networkPolicy(NetworkPolicy.OFFLINE)
                            .into(mCingleImageView, new Callback() {
                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onError() {
                                    Picasso.with(RedeemCreditsActivity.this)
                                            .load(image)
                                            .into(mCingleImageView);
                                }
                            });

                    //lauch the user profile
                    mUserProfileImageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(RedeemCreditsActivity.this, ProfileActivity.class);
                            intent.putExtra(RedeemCreditsActivity.EXTRA_USER_UID, uid);
                            startActivity(intent);
                        }
                    });

                    usersReference.document(uid).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                            if (documentSnapshot.exists()){
                                final Andeqan cinggulan = documentSnapshot.toObject(Andeqan.class);
                                final String username = cinggulan.getUsername();
                                final String profileImage = cinggulan.getProfileImage();

                                mAccountUsernameTextView.setText(username);
                                Picasso.with(RedeemCreditsActivity.this)
                                        .load(profileImage)
                                        .fit()
                                        .centerCrop()
                                        .placeholder(R.drawable.profle_image_background)
                                        .networkPolicy(NetworkPolicy.OFFLINE)
                                        .into(mUserProfileImageView, new Callback() {
                                            @Override
                                            public void onSuccess() {

                                            }

                                            @Override
                                            public void onError() {
                                                Picasso.with(RedeemCreditsActivity.this)
                                                        .load(profileImage)
                                                        .fit()
                                                        .centerCrop()
                                                        .placeholder(R.drawable.profle_image_background)
                                                        .into(mUserProfileImageView);
                                            }
                                        });

//

                            }
                        }
                    });

                }
            }
        });

    }

    public void redeeingDialog(){
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Processing transaction");
        progressDialog.setCancelable(false);
    }


    @Override
    public void onClick(View v){
        if (v == mRedeemAmountButton){
            progressDialog.show();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("d");
            final String date = simpleDateFormat.format(new Date());

            if (date.endsWith("1") && !date.endsWith("11"))
                simpleDateFormat = new SimpleDateFormat("d'st' MMM yyyy");
            else if (date.endsWith("2") && !date.endsWith("12"))
                simpleDateFormat = new SimpleDateFormat("d'nd' MMM yyyy");
            else if (date.endsWith("3") && !date.endsWith("13"))
                simpleDateFormat = new SimpleDateFormat("d'rd' MMM yyyy");
            else
                simpleDateFormat = new SimpleDateFormat("d'th' MMM yyyy");
            final String currentDate = simpleDateFormat.format(new Date());

            if (!TextUtils.isEmpty(mAmountEnteredEditText.getText())) {
                final String amountInString = mAmountEnteredEditText.getText().toString();

                final double amountEntered = Double.parseDouble(amountInString);
                final String formattedString = formatter.format(amountEntered);
                final double amountTransferred = Double.parseDouble(formattedString);

                senseCreditReference.document(mPostKey).get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if (documentSnapshot.exists()){
                                    final Credit cingleCredit = documentSnapshot.toObject(Credit.class);
                                    final double senseCredits = cingleCredit.getAmount();


                                    if (mAmountEnteredEditText.getText().equals("")) {
                                        mAmountEnteredEditText.setError("Amount cannot be empty");
                                    } else if (amountTransferred > senseCredits) {
                                        mAmountEnteredEditText.setError("Your Single has insufficient SC balance");
                                    }else if (amountTransferred <= 0.00){
                                        mAmountEnteredEditText.setError("Amount cannot be zero");
                                    }else {
                                        final double finalCredits = senseCredits - amountTransferred;

                                        senseCreditReference.document(mPostKey).update("amount", finalCredits)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()){
                                                            //get the now time
                                                            final long timeStamp = new Date().getTime();

                                                            //INCREAMENT THE AMOUNT TRANSFERED AFTER NEW TRANSFERS
                                                            postWalletReference.document(mPostKey).get()
                                                                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                                        @Override
                                                                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                                            if (documentSnapshot.exists()){
                                                                                final Balance cingleBalance = documentSnapshot.toObject(Balance.class);
                                                                                final double currentAmount = cingleBalance.getTotalBalance();
                                                                                final double newAmount = currentAmount + amountTransferred;

                                                                                final Balance balance = new Balance();
                                                                                balance.setTotalBalance(newAmount);
                                                                                balance.setAmountRedeemed(amountTransferred);
                                                                                postWalletReference.document(mPostKey).set(balance).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                        if (task.isSuccessful()){
                                                                                            //RECORD THE REDEEMED AMOUNT TRANSFERRED TO THE USE WALLET
                                                                                            walletReference.document(firebaseAuth.getCurrentUser().getUid()).get()
                                                                                                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                                                                        @Override
                                                                                                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                                                                            if (documentSnapshot.exists()){
                                                                                                                Balance walletBalance = documentSnapshot.toObject(Balance.class);
                                                                                                                final double currentBalance = walletBalance.getTotalBalance();
                                                                                                                final double newBalance = currentBalance + amountTransferred;

                                                                                                                //set transaction details
                                                                                                                final TransactionDetails transactionDetails = new TransactionDetails();
                                                                                                                transactionDetails.setAmount(amountTransferred);
                                                                                                                transactionDetails.setUserId(firebaseAuth.getCurrentUser().getUid());
                                                                                                                transactionDetails.setPostId(mPostKey);
                                                                                                                transactionDetails.setTime(timeStamp);
                                                                                                                transactionDetails.setWalletBalance(newBalance);
                                                                                                                transactionDetails.setType("redeem");
                                                                                                                //get the push id
                                                                                                                DocumentReference ref = transactionReference.document();
                                                                                                                String postId = ref.getId();
                                                                                                                //set the push id
                                                                                                                transactionDetails.setTransactionId(postId);
                                                                                                                ref.set(transactionDetails);

                                                                                                                Balance newWalletBalance = new Balance();
                                                                                                                newWalletBalance.setTotalBalance(newBalance);

                                                                                                                walletReference.document(firebaseAuth.getCurrentUser().getUid())
                                                                                                                        .set(newWalletBalance).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                    @Override
                                                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                                                        progressDialog.dismiss();
                                                                                                                        Toast.makeText(RedeemCreditsActivity.this, "Transaction successful",
                                                                                                                                Toast.LENGTH_SHORT).show();
                                                                                                                    }
                                                                                                                });
                                                                                                            }else {
                                                                                                                //set transaction details
                                                                                                                final TransactionDetails transactionDetails = new TransactionDetails();
                                                                                                                transactionDetails.setAmount(amountTransferred);
                                                                                                                transactionDetails.setUserId(firebaseAuth.getCurrentUser().getUid());
                                                                                                                transactionDetails.setPostId(mPostKey);
                                                                                                                transactionDetails.setTime(timeStamp);
                                                                                                                transactionDetails.setWalletBalance(amountTransferred);
                                                                                                                transactionDetails.setType("redeem");

                                                                                                                //get the push id
                                                                                                                DocumentReference ref = transactionReference.document();
                                                                                                                String postId = ref.getId();
                                                                                                                //set the push id
                                                                                                                transactionDetails.setTransactionId(postId);
                                                                                                                ref.set(transactionDetails);

                                                                                                                Balance newWalletBalance = new Balance();
                                                                                                                newWalletBalance.setTotalBalance(amountTransferred);

                                                                                                                walletReference.document(firebaseAuth.getCurrentUser().getUid())
                                                                                                                        .set(newWalletBalance).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                    @Override
                                                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                                                        progressDialog.dismiss();
                                                                                                                        Toast.makeText(RedeemCreditsActivity.this, "Transaction successful",
                                                                                                                                Toast.LENGTH_SHORT).show();
                                                                                                                    }
                                                                                                                });
                                                                                                            }
                                                                                                        }
                                                                                                    });
                                                                                        }
                                                                                    }
                                                                                });
                                                                            }else {
                                                                                final Balance balance = new Balance();
                                                                                balance.setAmountRedeemed(amountTransferred);
                                                                                //IF THE TRANSACTIONS IS FOR THE FIRST TIME
                                                                                postWalletReference.document(mPostKey).set(balance)
                                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                            @Override
                                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                                if (task.isSuccessful()){
                                                                                                    walletReference.document(firebaseAuth.getCurrentUser().getUid())
                                                                                                            .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                                                                        @Override
                                                                                                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                                                                            if (documentSnapshot.exists()){
                                                                                                                Balance walletBalance = documentSnapshot.toObject(Balance.class);
                                                                                                                final double currentBalance = walletBalance.getTotalBalance();
                                                                                                                final double newBalance = currentBalance + amountTransferred;

                                                                                                                final Balance newWalletBalance = new Balance();
                                                                                                                newWalletBalance.setTotalBalance(newBalance);

                                                                                                                //set transaction details
                                                                                                                final TransactionDetails transactionDetails = new TransactionDetails();
                                                                                                                transactionDetails.setAmount(amountTransferred);
                                                                                                                transactionDetails.setUserId(firebaseAuth.getCurrentUser().getUid());
                                                                                                                transactionDetails.setPostId(mPostKey);
                                                                                                                transactionDetails.setTime(timeStamp);
                                                                                                                transactionDetails.setWalletBalance(newBalance);
                                                                                                                transactionDetails.setType("redeem");

                                                                                                                //get the push id
                                                                                                                DocumentReference ref = transactionReference.document();
                                                                                                                String postId = ref.getId();
                                                                                                                //set the push id
                                                                                                                transactionDetails.setTransactionId(postId);
                                                                                                                ref.set(transactionDetails);

                                                                                                                walletReference.document(firebaseAuth.getCurrentUser().getUid())
                                                                                                                        .set(newWalletBalance).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                    @Override
                                                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                                                        progressDialog.dismiss();
                                                                                                                        Toast.makeText(RedeemCreditsActivity.this, "Transaction successful",
                                                                                                                                Toast.LENGTH_SHORT).show();
                                                                                                                    }
                                                                                                                });

                                                                                                            }else {
                                                                                                                //set transaction details
                                                                                                                final TransactionDetails transactionDetails = new TransactionDetails();
                                                                                                                transactionDetails.setAmount(amountTransferred);
                                                                                                                transactionDetails.setUserId(firebaseAuth.getCurrentUser().getUid());
                                                                                                                transactionDetails.setPostId(mPostKey);
                                                                                                                transactionDetails.setTime(timeStamp);
                                                                                                                transactionDetails.setWalletBalance(amountTransferred);
                                                                                                                transactionDetails.setType("redeem");

                                                                                                                //get the push id
                                                                                                                DocumentReference ref = transactionReference.document();
                                                                                                                String postId = ref.getId();
                                                                                                                //set the push id
                                                                                                                transactionDetails.setTransactionId(postId);
                                                                                                                ref.set(transactionDetails);

                                                                                                                final Balance newWalletBalance = new Balance();
                                                                                                                newWalletBalance.setTotalBalance(amountEntered);

                                                                                                                walletReference.document(firebaseAuth.getCurrentUser().getUid())
                                                                                                                        .set(newWalletBalance).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                    @Override
                                                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                                                        progressDialog.dismiss();
                                                                                                                        Toast.makeText(RedeemCreditsActivity.this, "Transaction successful",
                                                                                                                                Toast.LENGTH_SHORT).show();
                                                                                                                    }
                                                                                                                });
                                                                                                            }
                                                                                                        }
                                                                                                    });
                                                                                                }
                                                                                            }
                                                                                        });

                                                                            }
                                                                        }
                                                                    });

                                                        }
                                                    }
                                                });

                                    }
                                }else {
                                    mAmountEnteredEditText.setError("Your Single has insufficient SC balance");
                                }
                            }
                        });

                mAmountEnteredEditText.setText("");
            }else {
                mAmountEnteredEditText.setError("Enter amount to redeem");
            }
        }
    }
}

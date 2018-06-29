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
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.R;
import com.andeqa.andeqa.home.NavigationDrawerActivity;
import com.andeqa.andeqa.models.Andeqan;
import com.andeqa.andeqa.models.Transaction;
import com.andeqa.andeqa.models.Wallet;
import com.andeqa.andeqa.models.CollectionPost;
import com.andeqa.andeqa.models.Credit;
import com.andeqa.andeqa.profile.ProfileActivity;
import com.andeqa.andeqa.registration.SaveGoogleProfileActivity;
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

import javax.annotation.Nullable;

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
    private CollectionReference postWalletCollection;
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
    private static final String TYPE = "type";
    private String mType;
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

    private double finalCredits;
    private double newAmount;
    private double amountTransferred;


    //REMOVE SCIENTIFIC NOATATION
    private DecimalFormat formatter =  new DecimalFormat("0.00000000");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_redeem_credits);
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
        mRedeemAmountButton.setOnClickListener(this);
        firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() != null){

            mPostKey = getIntent().getStringExtra(EXTRA_POST_KEY);
            mCollectionId = getIntent().getStringExtra(COLLECTION_ID);
            mType = getIntent().getStringExtra(TYPE);


            //firestore
            if (mType.equals("single")){
                collectionsCollection = FirebaseFirestore.getInstance().collection(Constants.COLLECTIONS_POSTS)
                        .document("singles").collection(mCollectionId);
            }else{
                collectionsCollection = FirebaseFirestore.getInstance().collection(Constants.COLLECTIONS_POSTS)
                        .document("collections").collection(mCollectionId);
            }
            transactionReference = FirebaseFirestore.getInstance().collection(Constants.TRANSACTION_HISTORY);
            postWalletCollection = FirebaseFirestore.getInstance().collection(Constants.POST_WALLET);
            walletReference = FirebaseFirestore.getInstance().collection(Constants.WALLET);
            senseCreditReference = FirebaseFirestore.getInstance().collection(Constants.CREDITS);
            relationsReference = FirebaseFirestore.getInstance().collection(Constants.RELATIONS);
            postsCollection = FirebaseFirestore.getInstance().collection(Constants.POSTS);
            usersReference = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);
            commentReference = FirebaseFirestore.getInstance().collection(Constants.COMMENTS);
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
                    android.util.Log.w(TAG, "Listen error", e);
                    return;
                }

                if (documentSnapshot.exists()){
                    final CollectionPost collectionPost = documentSnapshot.toObject(CollectionPost.class);
                    final String uid = collectionPost.getUser_id();
                    final String image = collectionPost.getImage();

                    //set the single image
                    Picasso.with(RedeemCreditsActivity.this)
                            .load(image)
                            .networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.image_place_holder)
                            .into(mCingleImageView, new Callback() {
                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onError() {
                                    Picasso.with(RedeemCreditsActivity.this)
                                            .load(image)
                                            .placeholder(R.drawable.image_place_holder)
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

                            if (e != null) {
                                android.util.Log.w(TAG, "Listen error", e);
                                return;
                            }

                            if (documentSnapshot.exists()){
                                final Andeqan cinggulan = documentSnapshot.toObject(Andeqan.class);
                                final String username = cinggulan.getUsername();
                                final String profileImage = cinggulan.getProfile_image();

                                mAccountUsernameTextView.setText(username);
                                Picasso.with(RedeemCreditsActivity.this)
                                        .load(profileImage)
                                        .fit()
                                        .centerCrop()
                                        .placeholder(R.drawable.ic_user)
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
                                                        .placeholder(R.drawable.ic_user)
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
                                    final Credit credit = documentSnapshot.toObject(Credit.class);
                                    final double redeemedCredits = credit.getAmount();


                                    if (mAmountEnteredEditText.getText().equals("")) {
                                        mAmountEnteredEditText.setError("Amount cannot be empty");
                                    } else if (amountTransferred > redeemedCredits) {
                                        mAmountEnteredEditText.setError("Insufficient credo balance");
                                    }else if (amountTransferred <= 0.00){
                                        mAmountEnteredEditText.setError("Amount cannot be zero");
                                    }else {
                                        progressDialog.show();

                                        final double finalCredits = redeemedCredits - amountTransferred;

                                        senseCreditReference.document(mPostKey).update("amount", finalCredits)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()){
                                                            //get the now time
                                                            final long timeStamp = new Date().getTime();

                                                            postWalletCollection.document(mPostKey).get()
                                                                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                                @Override
                                                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                                    if (documentSnapshot.exists()){
                                                                        Wallet wallet = documentSnapshot.toObject(Wallet.class);
                                                                        final double amount  = wallet.getBalance();
                                                                        final double newAmount = amount + amountTransferred;
                                                                        postWalletCollection.document(mPostKey).update("amount", newAmount);
                                                                        postWalletCollection.document(mPostKey).update("redeemed", amountTransferred);
                                                                        senseCreditReference.document(mPostKey).update("redeemed",amountTransferred);

                                                                        walletReference.document(firebaseAuth.getCurrentUser().getUid())
                                                                                .collection("account_addresses")
                                                                                .document(firebaseAuth.getCurrentUser().getUid())
                                                                                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                                            @Override
                                                                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                                                if (documentSnapshot.exists()){
                                                                                    Wallet wallet = documentSnapshot.toObject(Wallet.class);
                                                                                    final double currentBalance = wallet.getBalance();
                                                                                    final double newBalance = currentBalance + amountTransferred;

                                                                                    //set transaction details
                                                                                    final Transaction transaction = new Transaction();
                                                                                    transaction.setAmount(amountTransferred);
                                                                                    transaction.setUser_id(firebaseAuth.getCurrentUser().getUid());
                                                                                    transaction.setPost_id(mPostKey);
                                                                                    transaction.setTime(timeStamp);
                                                                                    transaction.setWallet_balance(newBalance);
                                                                                    transaction.setReceiver_id(firebaseAuth.getCurrentUser().getUid());
                                                                                    transaction.setType("redeem");
                                                                                    //get the push id
                                                                                    DocumentReference ref = transactionReference
                                                                                            .document(firebaseAuth.getCurrentUser().getUid())
                                                                                            .collection("transactions").document();
                                                                                    String postId = ref.getId();
                                                                                    //set the push id
                                                                                    transaction.setTransaction_id(postId);
                                                                                    ref.set(transaction);

                                                                                    walletReference.document(firebaseAuth.getCurrentUser().getUid())
                                                                                            .collection("account_addresses")
                                                                                            .document(firebaseAuth.getCurrentUser().getUid())
                                                                                            .update("balance", newBalance)
                                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                        @Override
                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                            progressDialog.dismiss();
                                                                                            Toast.makeText(RedeemCreditsActivity.this, "Transaction successful",
                                                                                                    Toast.LENGTH_SHORT).show();
                                                                                        }
                                                                                    });
                                                                                }else {

                                                                                    Wallet userWallet = new Wallet();
                                                                                    userWallet.setAddress(firebaseAuth.getCurrentUser().getEmail());
                                                                                    userWallet.setUser_id(firebaseAuth.getCurrentUser().getUid());
                                                                                    userWallet.setTime(timeStamp);
                                                                                    userWallet.setDeposited(0.0);
                                                                                    userWallet.setRedeemed(amountTransferred);
                                                                                    userWallet.setBalance(amountTransferred);

                                                                                    walletReference.document(firebaseAuth.getCurrentUser().getUid())
                                                                                            .collection("account_addresses")
                                                                                            .document(firebaseAuth.getCurrentUser().getUid())
                                                                                            .set(userWallet);

                                                                                    //set transaction details
                                                                                    final Transaction transaction = new Transaction();
                                                                                    transaction.setAmount(amountTransferred);
                                                                                    transaction.setUser_id(firebaseAuth.getCurrentUser().getUid());
                                                                                    transaction.setPost_id(mPostKey);
                                                                                    transaction.setTime(timeStamp);
                                                                                    transaction.setReceiver_id(firebaseAuth.getCurrentUser().getUid());
                                                                                    transaction.setWallet_balance(amountTransferred);
                                                                                    transaction.setType("redeem");

                                                                                    //get the push id
                                                                                    DocumentReference ref = transactionReference
                                                                                            .document(firebaseAuth.getCurrentUser().getUid())
                                                                                            .collection("transactions").document();
                                                                                    String postId = ref.getId();
                                                                                    //set the push id
                                                                                    transaction.setTransaction_id(postId);
                                                                                    ref.set(transaction);

                                                                                    walletReference.document(firebaseAuth.getCurrentUser().getUid())
                                                                                            .collection("account_addresses")
                                                                                            .document(firebaseAuth.getCurrentUser().getUid())
                                                                                            .update("balance", amountTransferred)
                                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
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

                                                                    }else {
                                                                        //create a new post wallet
                                                                        Wallet wallet = new Wallet();
                                                                        wallet.setTime(timeStamp);
                                                                        wallet.setBalance(finalCredits);
                                                                        wallet.setDeposited(0.0);
                                                                        wallet.setRedeemed(amountTransferred);
                                                                        wallet.setAddress(mPostKey);
                                                                        wallet.setUser_id(firebaseAuth.getCurrentUser().getUid());
                                                                        postWalletCollection.document(mPostKey).set(wallet);

                                                                        walletReference.document(firebaseAuth.getCurrentUser().getUid())
                                                                                .collection("account_addresses")
                                                                                .document(firebaseAuth.getCurrentUser().getUid())
                                                                                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                                            @Override
                                                                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                                                if (documentSnapshot.exists()){
                                                                                    Wallet wallet = documentSnapshot.toObject(Wallet.class);
                                                                                    final double currentBalance = wallet.getBalance();
                                                                                    final double newBalance = currentBalance + amountTransferred;

                                                                                    //set transaction details
                                                                                    final Transaction transaction = new Transaction();
                                                                                    transaction.setAmount(amountTransferred);
                                                                                    transaction.setUser_id(firebaseAuth.getCurrentUser().getUid());
                                                                                    transaction.setPost_id(mPostKey);
                                                                                    transaction.setTime(timeStamp);
                                                                                    transaction.setReceiver_id(firebaseAuth.getCurrentUser().getUid());
                                                                                    transaction.setWallet_balance(newBalance);
                                                                                    transaction.setType("redeem");
                                                                                    //get the push id
                                                                                    DocumentReference ref = transactionReference
                                                                                            .document(firebaseAuth.getCurrentUser().getUid())
                                                                                            .collection("transactions").document();
                                                                                    String postId = ref.getId();
                                                                                    //set the push id
                                                                                    transaction.setTransaction_id(postId);
                                                                                    ref.set(transaction);

                                                                                    walletReference.document(firebaseAuth.getCurrentUser().getUid())
                                                                                            .collection("account_addresses").document(firebaseAuth.getCurrentUser().getUid())
                                                                                            .update("balance", newBalance).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                        @Override
                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                            progressDialog.dismiss();
                                                                                            Toast.makeText(RedeemCreditsActivity.this, "Transaction successful",
                                                                                                    Toast.LENGTH_SHORT).show();
                                                                                        }
                                                                                    });
                                                                                }else {

                                                                                    Wallet userWallet = new Wallet();
                                                                                    userWallet.setAddress(firebaseAuth.getCurrentUser().getEmail());
                                                                                    userWallet.setUser_id(firebaseAuth.getCurrentUser().getUid());
                                                                                    userWallet.setTime(timeStamp);
                                                                                    userWallet.setDeposited(0.0);
                                                                                    userWallet.setRedeemed(amountTransferred);
                                                                                    userWallet.setBalance(amountTransferred);

                                                                                    walletReference.document(firebaseAuth.getCurrentUser().getUid())
                                                                                            .collection("account_addresses")
                                                                                            .document(firebaseAuth.getCurrentUser().getUid())
                                                                                            .set(userWallet);

                                                                                    //set transaction details
                                                                                    final Transaction transaction = new Transaction();
                                                                                    transaction.setAmount(amountTransferred);
                                                                                    transaction.setUser_id(firebaseAuth.getCurrentUser().getUid());
                                                                                    transaction.setPost_id(mPostKey);
                                                                                    transaction.setTime(timeStamp);
                                                                                    transaction.setReceiver_id(firebaseAuth.getCurrentUser().getUid());
                                                                                    transaction.setWallet_balance(amountTransferred);
                                                                                    transaction.setType("redeem");

                                                                                    //get the push id
                                                                                    DocumentReference ref = transactionReference
                                                                                            .document(firebaseAuth.getCurrentUser().getUid())
                                                                                            .collection("transactions").document();
                                                                                    String postId = ref.getId();
                                                                                    //set the push id
                                                                                    transaction.setTransaction_id(postId);
                                                                                    ref.set(transaction);

                                                                                    walletReference.document(firebaseAuth.getCurrentUser().getUid())
                                                                                            .collection("account_addresses").document(firebaseAuth.getCurrentUser().getUid())
                                                                                            .update("balance", amountTransferred).addOnCompleteListener(new OnCompleteListener<Void>() {
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
                                }else {
                                    mAmountEnteredEditText.setError("Insufficient credo balance");
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



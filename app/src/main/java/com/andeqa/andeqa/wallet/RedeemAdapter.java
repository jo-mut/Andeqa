package com.andeqa.andeqa.wallet;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.DigitsKeyListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.R;
import com.andeqa.andeqa.home.PostDetailActivity;
import com.andeqa.andeqa.market.RedeemCreditsActivity;
import com.andeqa.andeqa.models.Transaction;
import com.andeqa.andeqa.models.Wallet;
import com.andeqa.andeqa.models.CollectionPost;
import com.andeqa.andeqa.models.Credit;
import com.andeqa.andeqa.models.Post;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Nullable;

public class RedeemAdapter  extends RecyclerView.Adapter<RedeemViewHolder>{

    private static final String TAG = RedeemAdapter.class.getSimpleName();
    private Context mContext;
    //firestore
    private CollectionReference collectionsPosts;
    private CollectionReference creditCollection;
    private CollectionReference postsCollection;
    private CollectionReference transactionReference;
    private CollectionReference walletReference;
    private CollectionReference postWalletReference;
    private CollectionReference usersReference;
    private CollectionReference senseCreditReference;


    //adapters
    private FirebaseAuth firebaseAuth;
    private static final String EXTRA_POST_ID = "post id";
    private static final String EXTRA_USER_UID = "uid";
    private static final String COLLECTION_ID = "collection id";
    private static final String TYPE = "type";
    private List<DocumentSnapshot> documentSnapshots = new ArrayList<>();
    private static final int MAX_WIDTH = 200;
    private static final int MAX_HEIGHT = 200;
    private ProgressDialog progressDialog;

    public RedeemAdapter(Context mContext) {
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public RedeemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.redeem_credo_layout,
                parent, false);
        return new RedeemViewHolder(view);
    }

    public void setRedeemPosts(List<DocumentSnapshot> credits){
        this.documentSnapshots = credits;
        notifyDataSetChanged();
    }

    public DocumentSnapshot getSnapshot(int index) {
        return documentSnapshots.get(index);
    }


    @Override
    public int getItemCount() {
        return documentSnapshots.size();
    }

    @Override
    public void onBindViewHolder(final @NonNull RedeemViewHolder holder, int position) {
        final Credit credit = getSnapshot(position).toObject(Credit.class);
        final String postId = credit.getPost_id();
        final String uid = credit.getUser_id();
        final double credits = credit.getAmount();

        firebaseAuth = FirebaseAuth.getInstance();
        final DecimalFormat formatter = new DecimalFormat("0.00000000");

        postsCollection = FirebaseFirestore.getInstance().collection(Constants.POSTS);
        collectionsPosts = FirebaseFirestore.getInstance().collection(Constants.COLLECTIONS_POSTS);
        creditCollection = FirebaseFirestore.getInstance().collection(Constants.CREDITS);
        transactionReference = FirebaseFirestore.getInstance().collection(Constants.TRANSACTION_HISTORY);
        postWalletReference = FirebaseFirestore.getInstance().collection(Constants.POST_WALLET);
        walletReference = FirebaseFirestore.getInstance().collection(Constants.WALLET);
        postsCollection = FirebaseFirestore.getInstance().collection(Constants.POSTS);
        usersReference = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);
        senseCreditReference = FirebaseFirestore.getInstance().collection(Constants.CREDITS);



        holder.creditsTextView.setText("Credo"  +" " + formatter.format(credits));
        postsCollection.document(postId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    android.util.Log.w(TAG, "Listen error", e);
                    return;
                }

                if (documentSnapshot.exists()){
                    Post post = documentSnapshot.toObject(Post.class);
                    final String collectionId = post.getCollection_id();
                    final String type = post.getType();

                    if (type.equals("post")){
                        collectionsPosts.document("collections").collection(collectionId)
                                .document(postId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                            @Override
                            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {

                                if (e != null) {
                                    android.util.Log.w(TAG, "Listen error", e);
                                    return;
                                }

                                if (documentSnapshot.exists()){
                                    CollectionPost collectionPost = documentSnapshot.toObject(CollectionPost.class);
                                    final String image = collectionPost.getImage();

                                    Picasso.with(mContext)
                                            .load(image)
                                            .resize(MAX_WIDTH, MAX_HEIGHT)
                                            .centerCrop()
                                            .placeholder(R.drawable.image_place_holder)
                                            .networkPolicy(NetworkPolicy.OFFLINE)
                                            .into(holder.postImageView, new Callback() {
                                                @Override
                                                public void onSuccess() {

                                                }

                                                @Override
                                                public void onError() {
                                                    Picasso.with(mContext)
                                                            .load(image)
                                                            .resize(MAX_WIDTH, MAX_HEIGHT)
                                                            .centerCrop()
                                                            .placeholder(R.drawable.image_place_holder)
                                                            .into(holder.postImageView);

                                                }
                                            });

                                    holder.postImageView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            Intent intent = new Intent(mContext, PostDetailActivity.class);
                                            intent.putExtra(RedeemAdapter.EXTRA_USER_UID, uid);
                                            intent.putExtra(RedeemAdapter.EXTRA_POST_ID, postId);
                                            intent.putExtra(RedeemAdapter.COLLECTION_ID, collectionId);
                                            intent.putExtra(RedeemAdapter.TYPE, type);
                                            mContext.startActivity(intent);
                                        }
                                    });
                                }

                            }
                        });
                    }else {
                        collectionsPosts.document("singles").collection(collectionId)
                                .document(postId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                            @Override
                            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {

                                if (e != null) {
                                    android.util.Log.w(TAG, "Listen error", e);
                                    return;
                                }

                                if (documentSnapshot.exists()){
                                    CollectionPost collectionPost = documentSnapshot.toObject(CollectionPost.class);
                                    final String image = collectionPost.getImage();

                                    Picasso.with(mContext)
                                            .load(image)
                                            .resize(MAX_WIDTH, MAX_HEIGHT)
                                            .centerCrop()
                                            .placeholder(R.drawable.image_place_holder)
                                            .networkPolicy(NetworkPolicy.OFFLINE)
                                            .into(holder.postImageView, new Callback() {
                                                @Override
                                                public void onSuccess() {

                                                }

                                                @Override
                                                public void onError() {
                                                    Picasso.with(mContext)
                                                            .load(image)
                                                            .resize(MAX_WIDTH, MAX_HEIGHT)
                                                            .centerCrop()
                                                            .placeholder(R.drawable.image_place_holder)
                                                            .into(holder.postImageView);

                                                }
                                            });

                                    holder.postImageView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            Intent intent = new Intent(mContext, PostDetailActivity.class);
                                            intent.putExtra(RedeemAdapter.EXTRA_USER_UID, uid);
                                            intent.putExtra(RedeemAdapter.EXTRA_POST_ID, postId);
                                            intent.putExtra(RedeemAdapter.COLLECTION_ID, collectionId);
                                            intent.putExtra(RedeemAdapter.TYPE, type);
                                            mContext.startActivity(intent);
                                        }
                                    });
                                }

                            }
                        });
                    }

                }
            }
        });

        holder.redeemCreditEditText.setFilters(new InputFilter[] {
                new DigitsKeyListener(Boolean.FALSE, Boolean.TRUE) {
                    int beforeDecimal = 6, afterDecimal = 8;

                    @Override
                    public CharSequence filter(CharSequence source, int start, int end,
                                               Spanned dest, int dstart, int dend) {
                        String temp = holder.redeemCreditEditText.getText() + source.toString();

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

        holder.redeemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!TextUtils.isEmpty(holder.redeemCreditEditText.getText())) {
                    final String amountInString = holder.redeemCreditEditText.getText().toString();

                    final double amountEntered = Double.parseDouble(amountInString);
                    final String formattedString = formatter.format(amountEntered);
                    final double amountTransferred = Double.parseDouble(formattedString);

                    creditCollection.document(postId).get()
                            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    if (documentSnapshot.exists()){
                                        final Credit cingleCredit = documentSnapshot.toObject(Credit.class);
                                        final double senseCredits = cingleCredit.getAmount();


                                        if (holder.redeemCreditEditText.getText().equals("")) {
                                            holder.redeemCreditEditText.setError("Amount cannot be empty");
                                        } else if (amountTransferred > senseCredits) {
                                            holder.redeemCreditEditText.setError("Insufficient credo balance");
                                        }else if (amountTransferred <= 0.00){
                                            holder.redeemCreditEditText.setError("Amount cannot be zero");
                                        }else {
                                            progressDialog = new ProgressDialog(mContext);
                                            progressDialog.setMessage("Processing transaction");
                                            progressDialog.setCancelable(false);
                                            progressDialog.show();

                                            final double finalCredits = senseCredits - amountTransferred;

                                            creditCollection.document(postId).update("amount", finalCredits)
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()){
                                                                //get the now time
                                                                final long timeStamp = new Date().getTime();

                                                                //INCREAMENT THE AMOUNT TRANSFERED AFTER NEW TRANSFERS
                                                                postWalletReference.document(postId).get()
                                                                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                                            @Override
                                                                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                                                if (documentSnapshot.exists()){
                                                                                    final Wallet cingleWallet = documentSnapshot.toObject(Wallet.class);
                                                                                    final double currentAmount = cingleWallet.getBalance();
                                                                                    final double newAmount = currentAmount + amountTransferred;

                                                                                    final Wallet wallet = new Wallet();
                                                                                    wallet.setBalance(newAmount);
                                                                                    wallet.setRedeemed(amountTransferred);
                                                                                    senseCreditReference.document(postId).update("redeemed",amountTransferred);
                                                                                    postWalletReference.document(postId).set(wallet).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                        @Override
                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                            if (task.isSuccessful()){
                                                                                                //RECORD THE REDEEMED AMOUNT TRANSFERRED TO THE USE WALLET
                                                                                                walletReference.document(firebaseAuth.getCurrentUser().getUid()).get()
                                                                                                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
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
                                                                                                                    transaction.setPost_id(postId);
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
                                                                                                                            Toast.makeText(mContext, "Transaction successful",
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
                                                                                                                    transaction.setPost_id(postId);
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
                                                                                                                            Toast.makeText(mContext, "Transaction successful",
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
                                                                                    final Wallet wallet = new Wallet();
                                                                                    wallet.setRedeemed(amountTransferred);
                                                                                    //IF THE TRANSACTIONS IS FOR THE FIRST TIME
                                                                                    postWalletReference.document(postId).set(wallet)
                                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                @Override
                                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                                    if (task.isSuccessful()){
                                                                                                        walletReference.document(firebaseAuth.getCurrentUser().getUid())
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
                                                                                                                    transaction.setPost_id(postId);
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
                                                                                                                            Toast.makeText(mContext, "Transaction successful",
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
                                                                                                                    transaction.setPost_id(postId);
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
                                                                                                                            Toast.makeText(mContext, "Transaction successful",
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
                                        holder.redeemCreditEditText.setError("Insufficient credo balance");
                                    }
                                }
                            });

                    holder.redeemCreditEditText.setText("");
                }else {
                    holder.redeemCreditEditText.setError("Enter amount to redeem");
                }
            }
        });

    }

}

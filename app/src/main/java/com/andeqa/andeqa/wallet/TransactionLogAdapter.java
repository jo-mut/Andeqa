package com.andeqa.andeqa.wallet;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.R;
import com.andeqa.andeqa.home.PostDetailActivity;
import com.andeqa.andeqa.models.Andeqan;
import com.andeqa.andeqa.models.CollectionPost;
import com.andeqa.andeqa.models.Post;
import com.andeqa.andeqa.models.Transaction;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Nullable;

public class TransactionLogAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    //context
    private Context context;

    //firestore
    private CollectionReference collectionsPosts;
    private CollectionReference postsCollection;
    private CollectionReference transactionReference;
    private CollectionReference usersReference;
    //firebase
    //adapters
    private FirebaseAuth firebaseAuth;
    private static final String EXTRA_POST_ID = "post id";
    private static final String COLLECTION_ID = "collection id";
    private static final String EXTRA_USER_UID = "uid";
    private static final String TYPE = "type";

    private DecimalFormat formatter =  new DecimalFormat("0.00000000");
    private static final String TAG = TransactionLogAdapter.class.getSimpleName();
    private List<DocumentSnapshot> documentSnapshots = new ArrayList<>();
    private static final int REDEEM_TYPE=0;
    private static final int BUY_TYPE=1;

    public TransactionLogAdapter(Context context) {
        this.context = context;
    }

    protected DocumentSnapshot getSnapshot(int index) {
        return documentSnapshots.get(index);
    }

    public void setTransactionHistory(List<DocumentSnapshot> transactions){
        this.documentSnapshots = transactions;
        notifyDataSetChanged();

    }


    @Override
    public int getItemCount() {
        return documentSnapshots.size();
    }


    @Override
    public int getItemViewType(int position) {
        Transaction transaction = getSnapshot(position).toObject(Transaction.class);
        final String type = transaction.getType();
        firebaseAuth = FirebaseAuth.getInstance();

        if (type.equals("redeem")){
            return REDEEM_TYPE;
        }else {
            return BUY_TYPE;

        }

    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
       View view;
        switch (i){
            case REDEEM_TYPE:
                view = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.history_redeeming_layout, viewGroup, false);
                return new TransactionLogViewHolder(view);
            case BUY_TYPE:
                view = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.history_transfer_layout, viewGroup, false);
                return  new TransferViewHolder(view);
        }

        return null;
    }

    @Override
    public void onBindViewHolder(final @NonNull RecyclerView.ViewHolder holder, int position) {
        Transaction transaction = getSnapshot(position).toObject(Transaction.class);
        final String type = transaction.getType();

        if (type.equals("redeem")){
            populateRedeemHistory((TransactionLogViewHolder) holder, position);
        }else if (type.equals("sent")){
            populateSentHistory((TransferViewHolder)holder, position);
        }else if (type.equals("received")){
            populateReceivedHistory((TransferViewHolder) holder, position);
        }else {

        }

    }

    private void populateSentHistory(final  @NonNull TransferViewHolder holder, int position){
        final Transaction transaction = getSnapshot(position).toObject(Transaction.class);
        final long time = transaction.getTime();
        final double balance = transaction.getWallet_balance();
        final double amount = transaction.getAmount();
        final String transactionId = transaction.getTransaction_id();
        final String receiverId = transaction.getReceiver_id();

        firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() != null){
            //firestore
            collectionsPosts = FirebaseFirestore.getInstance().collection(Constants.COLLECTIONS_POSTS);
            postsCollection = FirebaseFirestore.getInstance().collection(Constants.POSTS);
            usersReference = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);
            transactionReference = FirebaseFirestore.getInstance().collection(Constants.TRANSACTION_HISTORY);
        }

        final DecimalFormat formatter =  new DecimalFormat("0.00000000");

        usersReference.document(receiverId)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    android.util.Log.w(TAG, "Listen error", e);
                    return;
                }

                if (documentSnapshot.exists()){
                    Andeqan andeqan = documentSnapshot.toObject(Andeqan.class);
                    final String username = andeqan.getUsername();

                    holder.amountTransferredTextView.setText("You have sent " + formatter.format(amount)
                            + " credo to "  + username  + " "+ android.text.format.DateFormat.format("HH:mm",
                            transaction.getTime()) + " on " + android.text.format.DateFormat.format("dd-MMM, HH:mm",time)
                            + "." + " Your new account balance is "
                            + formatter.format(balance) );

                }
            }
        });



        holder.deleteHistoryImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                transactionReference.document(firebaseAuth.getCurrentUser().getUid())
                        .collection("transactions").document(transactionId).delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(context, "Successfully deleted", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

    }

    private void populateReceivedHistory(final  @NonNull TransferViewHolder holder, int position){
        final Transaction transaction = getSnapshot(position).toObject(Transaction.class);
        final long time = transaction.getTime();
        final double balance = transaction.getWallet_balance();
        final double amount = transaction.getAmount();
        final String transactionId = transaction.getTransaction_id();
        final String userId = transaction.getUser_id();
        final String receiverId = transaction.getReceiver_id();

        firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() != null){
            //firestore
            collectionsPosts = FirebaseFirestore.getInstance().collection(Constants.COLLECTIONS_POSTS);
            postsCollection = FirebaseFirestore.getInstance().collection(Constants.POSTS);
            usersReference = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);
            transactionReference = FirebaseFirestore.getInstance().collection(Constants.TRANSACTION_HISTORY)
                    .document(firebaseAuth.getCurrentUser().getUid())
                    .collection("transactions");
        }

        final DecimalFormat formatter =  new DecimalFormat("0.00000000");

        usersReference.document(userId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    android.util.Log.w(TAG, "Listen error", e);
                    return;
                }

                if (documentSnapshot.exists()){
                    Andeqan andeqan = documentSnapshot.toObject(Andeqan.class);
                    final String username = andeqan.getUsername();

                    holder.amountTransferredTextView.setText("You have received " + formatter.format(amount)
                            + " credo from "  + username  + " "+ android.text.format.DateFormat.format("HH:mm",
                            transaction.getTime()) + " on " + android.text.format.DateFormat.format("dd-MMM, HH:mm",time)
                            + "." + " Your new account balance is "
                            + formatter.format(balance) );

                }
            }
        });



        holder.deleteHistoryImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                transactionReference.document(receiverId)
                        .collection("transactions").document(transactionId).delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(context, "Successfully deleted", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

    }

    private void populateRedeemHistory(final  @NonNull TransactionLogViewHolder holder, int position){
        Transaction transaction = getSnapshot(position).toObject(Transaction.class);
        final long time = transaction.getTime();
        final double balance = transaction.getWallet_balance();
        final double amount = transaction.getAmount();
        final String transactionId = transaction.getTransaction_id();
        final String postId = transaction.getPost_id();

        firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() != null){
            //firestore
            collectionsPosts = FirebaseFirestore.getInstance().collection(Constants.COLLECTIONS_POSTS);
            postsCollection = FirebaseFirestore.getInstance().collection(Constants.POSTS);
            transactionReference = FirebaseFirestore.getInstance().collection(Constants.TRANSACTION_HISTORY)
                    .document(firebaseAuth.getCurrentUser().getUid())
                    .collection("transactions");
        }

        //get the current date
        DateFormat simpleDateFormat = new SimpleDateFormat("d");
        String date = simpleDateFormat.format(new Date());

        if (date.endsWith("1") && !date.endsWith("11"))
            simpleDateFormat = new SimpleDateFormat("d'st' MMM, yyyy");
        else if (date.endsWith("2") && !date.endsWith("12"))
            simpleDateFormat = new SimpleDateFormat("d'nd' MMM, yyyy");
        else if (date.endsWith("3") && !date.endsWith("13"))
            simpleDateFormat = new SimpleDateFormat("d'rd' MMM, yyyy");
        else
            simpleDateFormat = new SimpleDateFormat("d'th' MMM, yyyy");

        DecimalFormat formatter =  new DecimalFormat("0.00000000");

        holder.amountTransferredTextView.setText("Your have redeemed " + formatter.format(amount)
                + " credo from this post at " + android.text.format.DateFormat.format("HH:mm",
                transaction.getTime()) + " on " + simpleDateFormat.format(time) + "." + " Your new account balance is "
                + formatter.format(balance) );


        holder.deleteHistoryImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                transactionReference.document(transactionId).delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(context, "Successfully deleted", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        postsCollection.document(postId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                if (e != null) {
                    android.util.Log.w(TAG, "Listen error", e);
                    return;
                }

                if (documentSnapshot.exists()){
                    final Post post = documentSnapshot.toObject(Post.class);
                    final String collectionId = post.getCollection_id();
                    final String type = post.getType();

                    collectionsPosts.document("collections").collection(collectionId)
                            .document(postId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                            if (e != null) {
                                android.util.Log.w(TAG, "Listen error", e);
                                return;
                            }

                            if (documentSnapshot.exists()){
                                final CollectionPost collectionPost = documentSnapshot.toObject(CollectionPost.class);
                                final String uid = collectionPost.getUser_id();

                                Picasso.with(context)
                                        .load(collectionPost.getImage())
                                        .networkPolicy(NetworkPolicy.OFFLINE)
                                        .placeholder(R.drawable.image_place_holder)
                                        .into(holder.postImageView, new Callback() {
                                            @Override
                                            public void onSuccess() {

                                            }

                                            @Override
                                            public void onError() {
                                                Picasso.with(context)
                                                        .load(collectionPost.getImage())
                                                        .placeholder(R.drawable.image_place_holder)
                                                        .into(holder.postImageView, new Callback() {
                                                            @Override
                                                            public void onSuccess() {

                                                            }

                                                            @Override
                                                            public void onError() {
                                                                android.util.Log.v("Picasso", "Could not fetch image");
                                                            }
                                                        });


                                            }
                                        });

                                holder.postImageView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Intent intent = new Intent(context, PostDetailActivity.class);
                                        intent.putExtra(TransactionLogAdapter.EXTRA_POST_ID, postId);
                                        intent.putExtra(TransactionLogAdapter.COLLECTION_ID, collectionId);
                                        intent.putExtra(TransactionLogAdapter.EXTRA_USER_UID, uid);
                                        intent.putExtra(TransactionLogAdapter.TYPE, type);
                                        context.startActivity(intent);
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

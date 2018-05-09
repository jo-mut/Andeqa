package com.andeqa.andeqa.wallet;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.R;
import com.andeqa.andeqa.home.PostDetailActivity;
import com.andeqa.andeqa.models.CollectionPost;
import com.andeqa.andeqa.models.Post;
import com.andeqa.andeqa.models.TransactionDetails;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class WalletAdapter extends RecyclerView.Adapter<WalletViewHolder>{
    //context
    private Context context;

    //firestore
    private CollectionReference collectionsPosts;
    private CollectionReference postsCollection;
    private Query transactionQuery;
    private CollectionReference transactionReference;
    private CollectionReference walletReference;
    //firebase
    //adapters
    private FirebaseAuth firebaseAuth;
    private static final String EXTRA_POST_ID = "post id";
    private static final String COLLECTION_ID = "collection id";
    private DecimalFormat formatter =  new DecimalFormat("0.00000000");
    private static final String TAG = WalletAdapter.class.getSimpleName();
    private List<DocumentSnapshot> documentSnapshots = new ArrayList<>();

    public WalletAdapter(Context context) {
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

    @NonNull
    @Override
    public WalletViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.transaction_history_layout, viewGroup, false);
        return  new WalletViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final  @NonNull WalletViewHolder holder, int i) {
        TransactionDetails transactionDetails = getSnapshot(i).toObject(TransactionDetails.class);
        final long time = transactionDetails.getTime();
        final double balance = transactionDetails.getWallet_balance();
        final double amount = transactionDetails.getAmount();
        final String transactionId = transactionDetails.getTransaction_id();
        final String postId = transactionDetails.getPost_id();

        firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() != null){
            //firestore
            collectionsPosts = FirebaseFirestore.getInstance().collection(Constants.COLLECTIONS_POSTS);
            postsCollection = FirebaseFirestore.getInstance().collection(Constants.POSTS);
            transactionReference = FirebaseFirestore.getInstance().collection(Constants.TRANSACTION_HISTORY);
            transactionQuery = transactionReference.whereEqualTo("user_id", firebaseAuth.getCurrentUser().getUid());
            walletReference = FirebaseFirestore.getInstance().collection(Constants.WALLET);
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
                + " uCredit from this post at " + android.text.format.DateFormat.format("HH:mm",
                transactionDetails.getTime()) + " on " + simpleDateFormat.format(time) + "." + " Your new wallet balance is "
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
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (documentSnapshot.exists()){
                    final Post post = documentSnapshot.toObject(Post.class);
                    final String collectionId = post.getCollection_id();

                    collectionsPosts.document("collections").collection(collectionId)
                            .document(postId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                            if (e != null) {
                                Log.w(TAG, "Listen error", e);
                                return;
                            }

                            if (documentSnapshot.exists()){
                                final CollectionPost collectionPost = documentSnapshot.toObject(CollectionPost.class);

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
                                                                Log.v("Picasso", "Could not fetch image");
                                                            }
                                                        });


                                            }
                                        });



                            }
                        }
                    });

                    holder.postImageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(context, PostDetailActivity.class);
                            intent.putExtra(WalletAdapter.EXTRA_POST_ID, postId);
                            intent.putExtra(WalletAdapter.COLLECTION_ID, collectionId);
                            context.startActivity(intent);
                        }
                    });


                }
            }
        });

    }

}

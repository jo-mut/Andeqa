package com.andeqa.andeqa.market;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.R;
import com.andeqa.andeqa.home.PostDetailActivity;
import com.andeqa.andeqa.models.Andeqan;
import com.andeqa.andeqa.models.CollectionPost;
import com.andeqa.andeqa.models.Market;
import com.andeqa.andeqa.models.Credit;
import com.andeqa.andeqa.models.TransactionDetails;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by J.EL on 11/13/2017.
 */

public class SellingAdapter extends RecyclerView.Adapter<PostSellingViewHolder> {
    private static final String TAG = SellingAdapter.class.getSimpleName();
    private Context mContext;
    private List<Market> markets = new ArrayList<>();
    //firestore
    private CollectionReference collectionsPosts;
    private CollectionReference postsCollection;
    private CollectionReference sellingCollection;
    private CollectionReference usersReference;
    private CollectionReference creditsReference;
    private CollectionReference cingleWalletReference;
    private CollectionReference ownerReference;
    private CollectionReference senseCreditReference;
    private com.google.firebase.firestore.Query sellingQuery;
    //firebase
    private DatabaseReference cinglesRef;
    //adapters
    private FirebaseAuth firebaseAuth;
    private static final String KEY_LAYOUT_POSITION = "layout pooition";
    private static final String EXTRA_POST_ID = "post id";
    private static final String EXTRA_USER_UID = "uid";
    private static final String COLLECTION_ID = "collection id";
    private static final int MAX_WIDTH = 200;
    private static final int MAX_HEIGHT = 200;
    private List<DocumentSnapshot> documentSnapshots = new ArrayList<>();

    public SellingAdapter(Context mContext) {
        this.mContext = mContext;
    }

    protected void setPostsOnSale(List<DocumentSnapshot> mSnapshots){
        this.documentSnapshots = mSnapshots;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return documentSnapshots.size();
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    public DocumentSnapshot getSnapshot(int index) {
        return documentSnapshots.get(index);
    }


    @Override
    public PostSellingViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.posts_market_layout, parent, false);
        return new PostSellingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final PostSellingViewHolder holder, int position) {
        final Market market = getSnapshot(position).toObject(Market.class);
        holder.bindIfairCingle(market);
        final String postKey = market.getPost_id();
        final String uid = market.getUser_id();
        final double salePrice = market.getSale_price();

        Log.d("snapshot uid", uid);


        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null){
            senseCreditReference = FirebaseFirestore.getInstance().collection(Constants.CREDITS);
            postsCollection = FirebaseFirestore.getInstance().collection(Constants.POSTS);
            collectionsPosts = FirebaseFirestore.getInstance().collection(Constants.COLLECTIONS_POSTS);
            sellingCollection = FirebaseFirestore.getInstance().collection(Constants.SELLING);
            cingleWalletReference = FirebaseFirestore.getInstance().collection(Constants.POST_WALLET);
            ownerReference = FirebaseFirestore.getInstance().collection(Constants.POST_OWNERS);
            usersReference = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);
            sellingQuery = sellingCollection.orderBy("random_number").limit(10);

        }
        DecimalFormat formatter =  new DecimalFormat("0.00000000");
        holder.salePriceTextView.setText("uC" + " " + formatter.format(salePrice));

        postsCollection.document(postKey).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (documentSnapshot.exists()){
                    final CollectionPost collectionPost = documentSnapshot.toObject(CollectionPost.class);
                    final String collectionId = collectionPost.getCollection_id();

                    holder.postImageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(mContext, PostDetailActivity.class);
                            intent.putExtra(SellingAdapter.EXTRA_POST_ID, postKey);
                            intent.putExtra(SellingAdapter.COLLECTION_ID, collectionId);
                            intent.putExtra(SellingAdapter.EXTRA_USER_UID, uid);
                            mContext.startActivity(intent);
                        }
                    });


                    collectionsPosts.document("collections").collection(collectionId)
                            .document(postKey).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                            if (e != null) {
                                Log.w(TAG, "Listen error", e);
                                return;
                            }

                            if (documentSnapshot.exists()){
                                final CollectionPost collectionPost = documentSnapshot.toObject(CollectionPost.class);

                                Picasso.with(mContext)
                                        .load(collectionPost.getImage())
                                        .networkPolicy(NetworkPolicy.OFFLINE)
                                        .placeholder(R.drawable.image_place_holder)
                                        .into(holder.postImageView, new Callback() {
                                            @Override
                                            public void onSuccess() {

                                            }

                                            @Override
                                            public void onError() {
                                                Picasso.with(mContext)
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


                }
            }
        });

    }

}

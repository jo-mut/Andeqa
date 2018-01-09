package com.cinggl.cinggl.market;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cinggl.cinggl.Constants;
import com.cinggl.cinggl.R;
import com.cinggl.cinggl.firestore.FirestoreAdapter;
import com.cinggl.cinggl.home.PostDetailActivity;
import com.cinggl.cinggl.models.Post;
import com.cinggl.cinggl.models.PostSale;
import com.cinggl.cinggl.models.Cinggulan;
import com.cinggl.cinggl.models.Credit;
import com.cinggl.cinggl.models.TransactionDetails;
import com.cinggl.cinggl.people.FollowerProfileActivity;
import com.cinggl.cinggl.profile.PersonalProfileActivity;
import com.cinggl.cinggl.viewholders.PostSellingViewHolder;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by J.EL on 11/13/2017.
 */

public class SellingAdapter extends FirestoreAdapter<PostSellingViewHolder> {
    private static final String TAG = SellingAdapter.class.getSimpleName();
    private Context mContext;
    private List<PostSale> postSales = new ArrayList<>();
    //firestore
    private CollectionReference cinglesReference;
    private CollectionReference ifairReference;
    private CollectionReference usersReference;
    private CollectionReference creditsReference;
    private CollectionReference cingleWalletReference;
    private CollectionReference ownerReference;
    private CollectionReference senseCreditReference;
    private com.google.firebase.firestore.Query sellingQuery;
    //firebase
    private DatabaseReference cinglesRef;
    //adapters
    private FirestoreRecyclerAdapter firestoreRecyclerAdapter;
    private FirebaseAuth firebaseAuth;
    private static final String KEY_LAYOUT_POSITION = "layout pooition";
    private static final String EXTRA_POST_KEY = "post key";
    private static final String EXTRA_USER_UID = "uid";
    private static final int MAX_WIDTH = 200;
    private static final int MAX_HEIGHT = 200;


    public SellingAdapter(Query query, Context mContext) {
        super(query);
        this.mContext = mContext;
    }

    @Override
    public int getItemCount() {
        return super.getItemCount();
    }

    @Override
    public PostSellingViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.posts_market_layout, parent, false);
        return new PostSellingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final PostSellingViewHolder holder, int position) {
        final PostSale postSale = getSnapshot(position).toObject(PostSale.class);
        holder.bindIfairCingle(postSale);
        final String postKey = postSale.getPushId();
        final String uid = postSale.getUid();
        final double salePrice = postSale.getSalePrice();
        Log.d("cingle postkey", postKey);

        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null){
            senseCreditReference = FirebaseFirestore.getInstance().collection(Constants.SENSECREDITS);
            cinglesReference = FirebaseFirestore.getInstance().collection(Constants.POSTS);
            ifairReference = FirebaseFirestore.getInstance().collection(Constants.MARKET);
            cingleWalletReference = FirebaseFirestore.getInstance().collection(Constants.CINGLE_WALLET);
            ownerReference = FirebaseFirestore.getInstance().collection(Constants.CINGLE_ONWERS);
            usersReference = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);
            sellingQuery = ifairReference.orderBy("randomNumber").limit(10);

        }
        DecimalFormat formatter =  new DecimalFormat("0.00000000");
        holder.cingleSalePriceTextView.setText("SC" + " " + formatter.format(salePrice));

        Log.d("best cingles postKey", postKey);

        holder.cingleTradeMethodTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent =  new Intent(mContext, PostDetailActivity.class);
                intent.putExtra(SellingAdapter.EXTRA_POST_KEY, postKey);
                mContext.startActivity(intent);
            }
        });

        holder.cingleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, PostDetailActivity.class);
                intent.putExtra(SellingAdapter.EXTRA_POST_KEY, postKey);
                mContext.startActivity(intent);
            }
        });

        holder.ownerImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (uid.equals(firebaseAuth.getCurrentUser().getUid())){
                    Intent intent = new Intent(mContext, PersonalProfileActivity.class);
                    intent.putExtra(SellingAdapter.EXTRA_USER_UID, uid);
                    mContext.startActivity(intent);

                }else {
                    Intent intent = new Intent(mContext, FollowerProfileActivity.class);
                    intent.putExtra(SellingAdapter.EXTRA_USER_UID, uid);
                    mContext.startActivity(intent);
                }
            }
        });

        holder.creatorImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (uid.equals(firebaseAuth.getCurrentUser().getUid())){
                    Intent intent = new Intent(mContext, PersonalProfileActivity.class);
                    intent.putExtra(SellingAdapter.EXTRA_USER_UID, uid);
                    mContext.startActivity(intent);

                }else {
                    Intent intent = new Intent(mContext, FollowerProfileActivity.class);
                    intent.putExtra(SellingAdapter.EXTRA_USER_UID, uid);
                    mContext.startActivity(intent);
                }
            }
        });

        if (uid.equals(firebaseAuth.getCurrentUser().getUid())){
            holder.unlistPostTextView.setVisibility(View.VISIBLE);
            holder.unlistPostTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Bundle bundle = new Bundle();
                    bundle.putString(SellingAdapter.EXTRA_POST_KEY, postKey);
                    FragmentManager fragmenManager = ((AppCompatActivity)mContext).getSupportFragmentManager();
                    DialogMarketPostSettings dialogMarketPostSettings = DialogMarketPostSettings.newInstance("post settings");
                    dialogMarketPostSettings.setArguments(bundle);
                    dialogMarketPostSettings.show(fragmenManager, "market post settings fragment");
                }
            });

        }else {
            holder.unlistPostTextView.setVisibility(View.GONE);
        }


        cinglesReference.document(postKey).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (documentSnapshot.exists()){
                    final Post post = documentSnapshot.toObject(Post.class);

                    Picasso.with(mContext)
                            .load(post.getImage())
                            .networkPolicy(NetworkPolicy.OFFLINE)
                            .into(holder.cingleImageView, new Callback() {
                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onError() {
                                    Picasso.with(mContext)
                                            .load(post.getImage())
                                            .into(holder.cingleImageView, new Callback() {
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
                    holder.datePostedTextView.setText(post.getDatePosted());


                }
            }
        });

        ifairReference.document(postKey).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (documentSnapshot.exists()){
                    holder.cingleTradeMethodTextView.setText("@Selling");
                }else {
                    holder.cingleTradeMethodTextView.setText("@NotOnSale");

                }

            }
        });

        senseCreditReference.document(postKey).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (documentSnapshot.exists()){
                    final Credit credit = documentSnapshot.toObject(Credit.class);
                    DecimalFormat formatter = new DecimalFormat("0.00000000");
                    holder.cingleSenseCreditsTextView.setText("SC" + " " + formatter
                            .format(credit.getAmount()));
                }else {
                    holder.cingleSenseCreditsTextView.setText("SC 0.00000000");
                }
            }
        });

        usersReference.document(uid).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (documentSnapshot.exists()){
                    Cinggulan cinggulan = documentSnapshot.toObject(Cinggulan.class);
                    final String profileImage = cinggulan.getProfileImage();
                    final String username = cinggulan.getUsername();
                    holder.usernameTextView.setText(username);
                    Picasso.with(mContext)
                            .load(profileImage)
                            .resize(MAX_WIDTH, MAX_HEIGHT)
                            .onlyScaleDown()
                            .centerCrop()
                            .placeholder(R.drawable.profle_image_background)
                            .networkPolicy(NetworkPolicy.OFFLINE)
                            .into(holder.creatorImageView, new Callback() {
                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onError() {
                                    Picasso.with(mContext)
                                            .load(profileImage)
                                            .resize(MAX_WIDTH, MAX_HEIGHT)
                                            .onlyScaleDown()
                                            .centerCrop()
                                            .placeholder(R.drawable.profle_image_background)
                                            .into(holder.creatorImageView);
                                }
                            });
                }
            }
        });

        ownerReference.document(postKey).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (documentSnapshot.exists()){
                    TransactionDetails transactionDetails = documentSnapshot.toObject(TransactionDetails.class);
                    final String ownerUid = transactionDetails.getUid();
                    Log.d("owner uid", ownerUid);

                    usersReference.document(ownerUid).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                            if (e != null) {
                                Log.w(TAG, "Listen error", e);
                                return;
                            }

                            if (documentSnapshot.exists()){
                                Cinggulan cinggulan = documentSnapshot.toObject(Cinggulan.class);
                                final String profileImage = cinggulan.getProfileImage();
                                final String username = cinggulan.getUsername();
                                holder.cingleOwnerTextView.setText(username);
                                Picasso.with(mContext)
                                        .load(profileImage)
                                        .resize(MAX_WIDTH, MAX_HEIGHT)
                                        .onlyScaleDown()
                                        .centerCrop()
                                        .placeholder(R.drawable.profle_image_background)
                                        .networkPolicy(NetworkPolicy.OFFLINE)
                                        .into(holder.ownerImageView, new Callback() {
                                            @Override
                                            public void onSuccess() {

                                            }

                                            @Override
                                            public void onError() {
                                                Picasso.with(mContext)
                                                        .load(profileImage)
                                                        .resize(MAX_WIDTH, MAX_HEIGHT)
                                                        .onlyScaleDown()
                                                        .centerCrop()
                                                        .placeholder(R.drawable.profle_image_background)
                                                        .into(holder.ownerImageView);
                                            }
                                        });
                            }
                        }
                    });
                }
            }
        });


    }


    @Override
    protected void onDocumentRemoved(DocumentChange change) {
        super.onDocumentRemoved(change);
        removeAt(change.getOldIndex());
    }

}

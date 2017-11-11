package com.cinggl.cinggl.ifair;


import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.cinggl.cinggl.Constants;
import com.cinggl.cinggl.R;
import com.cinggl.cinggl.home.CingleDetailActivity;
import com.cinggl.cinggl.models.Cingle;
import com.cinggl.cinggl.models.CingleSale;
import com.cinggl.cinggl.models.Cingulan;
import com.cinggl.cinggl.models.Credits;
import com.cinggl.cinggl.models.TransactionDetails;
import com.cinggl.cinggl.people.FollowerProfileActivity;
import com.cinggl.cinggl.profile.PersonalProfileActivity;
import com.cinggl.cinggl.viewholders.IfairCinglesViewHolder;
import com.firebase.ui.common.ChangeEventType;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.firebase.ui.firestore.ObservableSnapshotArray;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
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

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class IfairCinglesFragment extends Fragment {
    @Bind(R.id.ifairCinglesRecyclerView)RecyclerView mIfairCingleRecyclerView;


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
    private static final String TAG = "CingleOutFragment";

    private List<CingleSale> ifairCingles = new ArrayList<>();
    private List<String> ifairCinglesIds = new ArrayList<>();

    private int currentPage = 0;
    private static final int TOTAL_ITEM_EACH_LOAD = 10;
    private LinearLayoutManager layoutManager;
    private static final String KEY_LAYOUT_POSITION = "layout pooition";
    private static final String EXTRA_POST_KEY = "post key";
    private static final String EXTRA_USER_UID = "uid";
    private static final int MAX_WIDTH = 200;
    private static final int MAX_HEIGHT = 200;
    private Parcelable recyclerViewState;


    public IfairCinglesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_ifair_cingles, container, false);
        ButterKnife.bind(this, view);

        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseFirestore.setLoggingEnabled(true);

        if (firebaseAuth.getCurrentUser()!= null){
            //firestore
            senseCreditReference = FirebaseFirestore.getInstance().collection(Constants.SENSECREDITS);
            cinglesReference = FirebaseFirestore.getInstance().collection(Constants.POSTS);
            ifairReference = FirebaseFirestore.getInstance().collection(Constants.IFAIR);
            cingleWalletReference = FirebaseFirestore.getInstance().collection(Constants.CINGLE_WALLET);
            sellingQuery = ifairReference.orderBy("randomNumber");
            ownerReference = FirebaseFirestore.getInstance().collection(Constants.CINGLE_ONWERS);
            usersReference = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);
            //firebase
            cinglesRef = FirebaseDatabase.getInstance().getReference(Constants.POSTS);
            cinglesRef.keepSynced(true);

            setCinglesOnIfair();
            firestoreRecyclerAdapter.startListening();

        }

        return  view;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null){
            recyclerViewState = savedInstanceState.getParcelable(KEY_LAYOUT_POSITION);
            Log.d("Saved Instance", "Instance is not null");
        }else {
            Log.d("Saved Instance", "Instance is completely null");
        }
    }

    public void setCinglesOnIfair(){
        FirestoreRecyclerOptions<CingleSale> options = new FirestoreRecyclerOptions.Builder<CingleSale>()
                .setQuery(sellingQuery, CingleSale.class)
                .build();
        firestoreRecyclerAdapter = new FirestoreRecyclerAdapter<CingleSale, IfairCinglesViewHolder>(options) {
            @Override
            protected void onBindViewHolder(final IfairCinglesViewHolder holder, int position, CingleSale model) {
                holder.bindIfairCingle(model);
                final String postKey = getSnapshots().get(position).getPushId();
                final String uid = getSnapshots().get(position).getUid();
                final double salePrice = getSnapshots().get(position).getSalePrice();

                DecimalFormat formatter =  new DecimalFormat("0.00000000");
                holder.cingleSalePriceTextView.setText("CSC" + " " + formatter.format(salePrice));

                Log.d("best cingles postKey", postKey);
                
                holder.cingleTradeMethodTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent =  new Intent(getContext(), CingleDetailActivity.class);
                        intent.putExtra(IfairCinglesFragment.EXTRA_POST_KEY, postKey);
                        startActivity(intent);
                    }
                });

                holder.cingleImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getContext(), CingleDetailActivity.class);
                        intent.putExtra(IfairCinglesFragment.EXTRA_POST_KEY, postKey);
                        startActivity(intent);
                    }
                });

                holder.ownerImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (uid.equals(firebaseAuth.getCurrentUser().getUid())){
                            Intent intent = new Intent(getContext(), PersonalProfileActivity.class);
                            intent.putExtra(IfairCinglesFragment.EXTRA_USER_UID, uid);
                            startActivity(intent);

                        }else {
                            Intent intent = new Intent(getContext(), FollowerProfileActivity.class);
                            intent.putExtra(IfairCinglesFragment.EXTRA_USER_UID, uid);
                            startActivity(intent);
                        }
                    }
                });

                holder.creatorImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (uid.equals(firebaseAuth.getCurrentUser().getUid())){
                            Intent intent = new Intent(getContext(), PersonalProfileActivity.class);
                            intent.putExtra(IfairCinglesFragment.EXTRA_USER_UID, uid);
                            startActivity(intent);

                        }else {
                            Intent intent = new Intent(getContext(), FollowerProfileActivity.class);
                            intent.putExtra(IfairCinglesFragment.EXTRA_USER_UID, uid);
                            startActivity(intent);
                        }
                    }
                });

                cinglesReference.document(postKey).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen error", e);
                            return;
                        }

                        if (documentSnapshot.exists()){
                            final Cingle cingle = documentSnapshot.toObject(Cingle.class);

                            Picasso.with(getContext())
                                    .load(cingle.getCingleImageUrl())
                                    .networkPolicy(NetworkPolicy.OFFLINE)
                                    .into(holder.cingleImageView, new Callback() {
                                        @Override
                                        public void onSuccess() {

                                        }

                                        @Override
                                        public void onError() {
                                            Picasso.with(getContext())
                                                    .load(cingle.getCingleImageUrl())
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

                        }
                    }
                });


                cinglesRef.child(postKey).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            final Cingle cingle = dataSnapshot.getValue(Cingle.class);
                            Picasso.with(getContext())
                                    .load(cingle.getCingleImageUrl())
                                    .networkPolicy(NetworkPolicy.OFFLINE)
                                    .into(holder.cingleImageView, new Callback() {
                                        @Override
                                        public void onSuccess() {

                                        }

                                        @Override
                                        public void onError() {
                                            Picasso.with(getContext())
                                                    .load(cingle.getCingleImageUrl())
                                                    .into(holder.cingleImageView);


                                        }
                                    });
                            holder.datePostedTextView.setText(cingle.getDatePosted());
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

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
                            final Credits credits = documentSnapshot.toObject(Credits.class);
                            DecimalFormat formatter = new DecimalFormat("0.00000000");
                            holder.cingleSenseCreditsTextView.setText("CSC" + " " + formatter
                                    .format(credits.getAmount()));
                        }else {
                            holder.cingleSenseCreditsTextView.setText("CSC 0.00000000");
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
                            Cingulan cingulan = documentSnapshot.toObject(Cingulan.class);
                            final String profileImage = cingulan.getProfileImage();
                            final String username = cingulan.getUsername();
                            holder.usernameTextView.setText(username);
                            Picasso.with(getContext())
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
                                            Picasso.with(getContext())
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

                ownerReference.document("Ownership").collection(postKey)
                        .document("Owner").addSnapshotListener(new EventListener<DocumentSnapshot>() {
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
                                        Cingulan cingulan = documentSnapshot.toObject(Cingulan.class);
                                        final String profileImage = cingulan.getProfileImage();
                                        final String username = cingulan.getUsername();
                                        holder.cingleOwnerTextView.setText(username);
                                        Picasso.with(getContext())
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
                                                        Picasso.with(getContext())
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
            public IfairCinglesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.ifair_cingles_layout, parent, false);
                return new IfairCinglesViewHolder(view);
            }

            @Override
            public CingleSale getItem(int position) {
                return super.getItem(position);
            }

            @Override
            public int getItemCount() {
                return super.getItemCount();
            }

            @Override
            public void onChildChanged(ChangeEventType type, DocumentSnapshot snapshot, int newIndex, int oldIndex) {
                super.onChildChanged(type, snapshot, newIndex, oldIndex);
            }

            @Override
            public void onDataChanged() {
                super.onDataChanged();
            }

            @Override
            public void onError(FirebaseFirestoreException e) {
                super.onError(e);
            }

            @Override
            public ObservableSnapshotArray<CingleSale> getSnapshots() {
                return super.getSnapshots();
            }
        };

        layoutManager =  new LinearLayoutManager(getContext());
        mIfairCingleRecyclerView.setLayoutManager(layoutManager);
        mIfairCingleRecyclerView.setHasFixedSize(true);
        mIfairCingleRecyclerView.setAdapter(firestoreRecyclerAdapter);

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(KEY_LAYOUT_POSITION, layoutManager.onSaveInstanceState());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void onResume() {
        super.onResume();
        if (recyclerViewState != null){
            layoutManager.onRestoreInstanceState(recyclerViewState);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onStart() {
        super.onStart();
    }
}

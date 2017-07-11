package com.cinggl.cinggl.home;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cinggl.cinggl.Constants;
import com.cinggl.cinggl.R;
import com.cinggl.cinggl.adapters.BestCinglesViewHolder;
import com.cinggl.cinggl.models.Cingle;
import com.cinggl.cinggl.models.Like;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 */
public class BestCinglesFragment extends Fragment {
    private DatabaseReference databaseReference;
    private DatabaseReference usernameRef;
    private DatabaseReference likesRef;
    private DatabaseReference commentsRef;
    private CircleImageView profileImageView;
    private static final double GOLDEN_RATIO = 1.618;
    private boolean processLikes = false;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private FirebaseRecyclerAdapter firebaseRecyclerAdapter;
    private Query bestQuery;
    private TextView currentDateTextView;
    private TextView usernameTextView;
    private static final String TAG = "BestCingleFragment";
    private static final String EXTRA_POST_KEY = "post key";


    @Bind(R.id.bestCinglesRecyclerView)RecyclerView mBestCinglesRecyclerView;


    public BestCinglesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CINGLES);
        usernameRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_USERS);
        likesRef = FirebaseDatabase.getInstance().getReference(Constants.LIKES);
        bestQuery = databaseReference.orderByChild("sensepoint").limitToFirst(10);
        commentsRef = FirebaseDatabase.getInstance().getReference(Constants.COMMENTS);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_best_cingles, container, false);
        ButterKnife.bind(this, view);

        setUpBestMomentCingles();


        return view;
    }

    private void setUpBestMomentCingles(){
        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Cingle, BestCinglesViewHolder>
                (Cingle.class, R.layout.best_cingles_list, BestCinglesViewHolder.class, bestQuery) {
            @Override
            protected void populateViewHolder(final BestCinglesViewHolder viewHolder, final Cingle model, int position) {
                viewHolder.bindBestCingle(model);
                DatabaseReference cingleRef = getRef(position);
                final String postKey = cingleRef.getKey();

                databaseReference.child(postKey).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String uid = (String) dataSnapshot.child("uid").getValue();

                        try {
                            usernameRef.child(uid).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    String username = (String) dataSnapshot.child("username").getValue();
                                    final String profileImage = (String) dataSnapshot.child("profileImage").getValue();

                                    viewHolder.usernameTextView.setText(username);


                                    Picasso.with(getContext())
                                            .load(profileImage)
                                            .fit()
                                            .centerCrop()
                                            .placeholder(R.drawable.profle_image_background)
                                            .networkPolicy(NetworkPolicy.OFFLINE)
                                            .into(viewHolder.profileImageView, new Callback() {
                                                @Override
                                                public void onSuccess() {

                                                }

                                                @Override
                                                public void onError() {
                                                    Picasso.with(getContext())
                                                            .load(profileImage)
                                                            .fit()
                                                            .centerCrop()
                                                            .placeholder(R.drawable.profle_image_background)
                                                            .into(viewHolder.profileImageView);
                                                }
                                            });

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }catch (Exception e){

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                viewHolder.likesImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        processLikes = true;
                        likesRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(final DataSnapshot dataSnapshot) {
                                if(processLikes){
                                    if(dataSnapshot.child(postKey).hasChild(firebaseAuth.getCurrentUser().getUid())){
                                        likesRef.child(postKey).child(firebaseAuth.getCurrentUser()
                                                .getUid())
                                                .removeValue();

                                        onLikeCounter(false);
                                        processLikes = false;

                                    }else {
                                        usernameRef.child(firebaseAuth.getCurrentUser().getUid())
                                                .addValueEventListener(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                        final String uid = (String) dataSnapshot.child("uid").getValue();

                                                        final Like like = new Like();

                                                        like.setUid(uid);

                                                        likesRef.child(postKey).child(firebaseAuth.getCurrentUser().getUid())
                                                                .setValue(like);
                                                        onLikeCounter(true);
                                                        processLikes = false;

                                                    }

                                                    @Override
                                                    public void onCancelled(DatabaseError databaseError) {

                                                    }
                                                });
                                    }

                                }


                                String likesCount = dataSnapshot.child(postKey).getChildrenCount() + "";
                                Log.d(likesCount, "all the likes in one cingle");
                                final int x = Integer.parseInt(likesCount);


                                if (x > 0){

                                    final double pv = GOLDEN_RATIO/x;

                                    final double sensepoint = pv * x/1000;

                                    final double finalPoints = Math.round( sensepoint * 1000000.0)/1000000.0;

                                    databaseReference.child(postKey).child("sensepoint").setValue(finalPoints);
                                }
                                else {
                                    final double sensepoint = 0.00;

                                    databaseReference.child(postKey).child("sensepoint").setValue(sensepoint);
                                }

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                });

                likesRef.child(postKey).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                            Log.e(snapshot.getKey(), snapshot.getChildrenCount() + "likesCount");

                        }
                        viewHolder.likesCountTextView.setText(dataSnapshot.getChildrenCount() + " " + "Likes");

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


                commentsRef.child(postKey).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                            Log.e(snapshot.getKey(), snapshot.getChildrenCount() + "commentsCount");
                        }

                        viewHolder.commentsCountTextView.setText(dataSnapshot.getChildrenCount() + "");
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                viewHolder.commentsImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getActivity(), CommentsActivity.class);
                        intent.putExtra(BestCinglesFragment.EXTRA_POST_KEY, postKey);
                        startActivity(intent);
                    }
                });

                viewHolder.likesCountTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getActivity(), LikesActivity.class);
                        intent.putExtra(BestCinglesFragment.EXTRA_POST_KEY, postKey);
                        startActivity(intent);
                    }
                });


                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("d");
                String date = simpleDateFormat.format(new Date());

                if (date.endsWith("1") && !date.endsWith("11"))
                    simpleDateFormat = new SimpleDateFormat("EE, d'st' MMM yyyy");
                else if (date.endsWith("2") && !date.endsWith("12"))
                    simpleDateFormat = new SimpleDateFormat("EE, d'nd' MMM yyyy");
                else if (date.endsWith("3") && !date.endsWith("13"))
                    simpleDateFormat = new SimpleDateFormat("EE, d'rd' MMM yyyy");
                else
                    simpleDateFormat = new SimpleDateFormat("EE, d'th' MMM yyyy");
                String currentDate = simpleDateFormat.format(new Date());

                viewHolder.currentDateTextView.setText(currentDate);



            }
        };

        mBestCinglesRecyclerView.setAdapter(firebaseRecyclerAdapter);
        mBestCinglesRecyclerView.setHasFixedSize(false);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        layoutManager.onSaveInstanceState();
        layoutManager.setAutoMeasureEnabled(true);
        mBestCinglesRecyclerView.setLayoutManager(layoutManager);

    }

    private void onLikeCounter(final boolean increament){
        likesRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                if(mutableData.getValue() != null){
                    int value = mutableData.getValue(Integer.class);
                    if(increament){
                        value++;
                    }else{
                        value--;
                    }
                    mutableData.setValue(value);
                }
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b,
                                   DataSnapshot dataSnapshot) {
                Log.d(TAG, "likeTransaction:onComplete" + databaseError);

            }
        });
    }

}

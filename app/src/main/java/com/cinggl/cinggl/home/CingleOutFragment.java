package com.cinggl.cinggl.home;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cinggl.cinggl.Constants;
import com.cinggl.cinggl.R;
import com.cinggl.cinggl.adapters.CingleOutViewHolder;
import com.cinggl.cinggl.models.Like;
import com.cinggl.cinggl.models.TraceData;
import com.cinggl.cinggl.models.Cingle;
import com.cinggl.cinggl.utils.Trace;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
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

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Random;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 */
public class CingleOutFragment extends Fragment implements Trace.TracingListener{
    @Bind(R.id.cingleOutRecyclerView)RecyclerView cingleOutRecyclerView;

    private DatabaseReference databaseReference;
    private Query cinglesQuery;
    private FirebaseRecyclerAdapter firebaseRecyclerAdapter;
    private Trace trace;
    private Context mContext;
    private boolean processLikes = false;
    private DatabaseReference usernameRef;
    private DatabaseReference likesRef;
    private DatabaseReference recentLikesRef;
    private FirebaseAuth firebaseAuth;
    private static final double GOLDEN_RATIO = 1.618;
    private DatabaseReference sensepointRef;
    private double pv;
    private DatabaseReference commentReference;
    private static final String TAG = "CingleOutFragment";
    private static final String EXTRA_POST_KEY = "post key";
    private ArrayList<String> mDataSet = new ArrayList<>();



    public CingleOutFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_cingle_out, container, false);
        ButterKnife.bind(this, view);

        setUpFirebaseAdapter();
//        generateRandom();

        likesRef = FirebaseDatabase.getInstance().getReference(Constants.LIKES);
        sensepointRef = FirebaseDatabase.getInstance().getReference("Sense points");
        usernameRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_USERS);
        commentReference = FirebaseDatabase.getInstance()
                .getReference(Constants.COMMENTS);


        cinglesQuery = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CINGLES);


        firebaseAuth = FirebaseAuth.getInstance();
        likesRef.keepSynced(true);
        usernameRef.keepSynced(true);
        commentReference.keepSynced(true);

        return view;
    }

    private void setUpFirebaseAdapter(){
        databaseReference = FirebaseDatabase.getInstance()
                .getReference(Constants.FIREBASE_CINGLES);
        databaseReference.keepSynced(true);


        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Cingle, CingleOutViewHolder>
                (Cingle.class, R.layout.cingle_out_list, CingleOutViewHolder.class, databaseReference) {
            @Override
            protected void populateViewHolder(final CingleOutViewHolder viewHolder, Cingle model, int position) {
                viewHolder.bindCingle(model);
                final DatabaseReference cingleRef = getRef(position);
                final String postKey = cingleRef.getKey();

                commentReference.child(postKey).addValueEventListener(new ValueEventListener() {
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

                                    viewHolder.accountUsernameTextView.setText(username);


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

                viewHolder.commentsImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getActivity(), CommentsActivity.class);
                        intent.putExtra(CingleOutFragment.EXTRA_POST_KEY, postKey);
                        startActivity(intent);
                    }
                });

                viewHolder.likesCountTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getActivity(), LikesActivity.class);
                        intent.putExtra(CingleOutFragment.EXTRA_POST_KEY, postKey);
                        startActivity(intent);
                    }
                });

            }
        };

        cingleOutRecyclerView.setAdapter(firebaseRecyclerAdapter);
        cingleOutRecyclerView.setHasFixedSize(false);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        layoutManager.onSaveInstanceState();
        layoutManager.setAutoMeasureEnabled(true);
        cingleOutRecyclerView.setLayoutManager(layoutManager);

        trace = new Trace.Builder()
                .setRecyclerView(cingleOutRecyclerView)
                .setMinimumViewingTimeThreshold(1000)
                .setMinimumVisibleHeightThreshold(60)
                .setTracingListener(this)
                .setDataDumpInterval(1000)
                .dumpDataAfterInterval(true)
                .build();


    }

    @Override
    public void onResume() {
        super.onResume();
        try{
            trace.startTracing();
        }catch (Exception e){

        }
    }

    @Override
    public void onPause() {
        super.onPause();
        try{
            trace.getTraceData(true);

        }catch (Exception e){

        }
    }

    public void generateRandom(){
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long allCngles = dataSnapshot.getChildrenCount();
                int maxNum = (int) allCngles;
                int minNum = 1;
                int randomNum =  new Random().nextInt(maxNum - minNum + 1);

                int count = 0;

                Iterable<DataSnapshot> dataSnapshots = dataSnapshot.getChildren();
                Iterator<DataSnapshot> snapshotIterator = dataSnapshots.iterator();
                String newCingle = "";

                while (snapshotIterator.hasNext() && count < randomNum){
                    newCingle = (String) snapshotIterator.next().getValue();
                    count ++;
                }

                Toast.makeText(getContext(), newCingle, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    @Override
    public void traceDataDump(ArrayList<TraceData> data) {

        if(data != null) {
            // Do something with the data.
            for(int i = 0 ; i < data.size(); ++i)
                Log.d("Data dump", data.get(i).getViewId());

        }
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


    @Override
    public void onDestroy(){
        super.onDestroy();
        firebaseRecyclerAdapter.cleanup();
    }

}

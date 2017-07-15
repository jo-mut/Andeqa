package com.cinggl.cinggl.home;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import com.cinggl.cinggl.Constants;
import com.cinggl.cinggl.R;
import com.cinggl.cinggl.adapters.CingleOutAdapter;
import com.cinggl.cinggl.adapters.CingleOutViewHolder;
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

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import butterknife.Bind;
import butterknife.ButterKnife;


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
    private CingleOutAdapter cingleOutAdapter;
    private FirebaseAuth firebaseAuth;
    private static final double GOLDEN_RATIO = 1.618;
    private static final double MILLE = 1000.0;
    private static final int PAGE_SIZE = 30;
    private DatabaseReference sensepointRef;
    private DatabaseReference commentReference;
    private static final String TAG = "CingleOutFragment";
    private static final String EXTRA_POST_KEY = "post key";
    private ArrayList<String> mDataSet = new ArrayList<>();
    private LinearLayoutManager layoutManager;

    private static final double DEFAULT_PRICE = 1.5;
    private boolean isLoading = false;
    private boolean isLastPage = false;



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
        databaseReference = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CINGLES);
        cinglesQuery = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CINGLES);

        firebaseAuth = FirebaseAuth.getInstance();
        likesRef.keepSynced(true);
        usernameRef.keepSynced(true);
        commentReference.keepSynced(true);

        return view;
    }

//
//    @Override
//    public void onStart() {
//        super.onStart();
//
//        layoutManager = new LinearLayoutManager(getContext());
//        layoutManager.setReverseLayout(true);
//        layoutManager.setStackFromEnd(true);
//        layoutManager.onSaveInstanceState();
//        layoutManager.setAutoMeasureEnabled(true);
//        cingleOutAdapter = new CingleOutAdapter(databaseReference, getContext());
//        cingleOutRecyclerView.setAdapter(cingleOutAdapter);
//        cingleOutRecyclerView.setHasFixedSize(false);
//    }
//
//    @Override
//    public void onStop() {
//        super.onStop();
//    }

    private void setUpFirebaseAdapter(){
        databaseReference = FirebaseDatabase.getInstance()
                .getReference(Constants.FIREBASE_CINGLES);
        databaseReference.keepSynced(true);

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Cingle, CingleOutViewHolder>
                (Cingle.class, R.layout.cingle_out_list, CingleOutViewHolder.class, databaseReference) {

            @Override
            public void onAttachedToRecyclerView(RecyclerView cingleOutRecyclerView) {
                super.onAttachedToRecyclerView(cingleOutRecyclerView);
            }

            public void animate(CingleOutViewHolder viewHolder){
                final Animation animAnticipateOvershooot = AnimationUtils.loadAnimation(getContext(), R.anim.bounce_interpolator);
                viewHolder.itemView.setAnimation(animAnticipateOvershooot);
            }

            @Override
            public int getItemCount() {
                return super.getItemCount();
            }

            @Override
            public void onBindViewHolder(CingleOutViewHolder viewHolder, int position) {
                super.onBindViewHolder(viewHolder, position);
                animate(viewHolder);
            }


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
                                            if(processLikes){
                                                if (dataSnapshot.child(postKey).hasChild(firebaseAuth.getCurrentUser().getUid())){
                                                    likesRef.child(postKey)
                                                            .removeValue();
                                                    processLikes = false;
                                                    onLikeCounter(false);
                                                }else {
                                                    likesRef.child(postKey).child(firebaseAuth.getCurrentUser().getUid())
                                                            .child("uid").setValue(firebaseAuth.getCurrentUser().getUid());
                                                    processLikes = false;
                                                    onLikeCounter(false);
                                                }
                                            }
                                        }

                                    }


                                    String likesCount = dataSnapshot.child(postKey).getChildrenCount() + "";
                                    Log.d(likesCount, "all the likes in one cingle");
                                    //convert children count which is a string to integer
                                    final int x = Integer.parseInt(likesCount);

                                    if (x > 0){
                                        //mille is a thousand likes
                                        double MILLE = 1000.0;
                                        //get the number of likes per a thousand likes
                                        double likesPerMille = x/MILLE;
                                        //get the default rate of likes per unit time in seconds;
                                        double rateOfLike = 1000.0/1800.0;
                                        //get the current rate of likes per unit time in seconds;
                                        double currentRateOfLkes = x * rateOfLike/MILLE;
                                        //get the current price of cingle
                                        final double currentPrice = currentRateOfLkes * DEFAULT_PRICE/rateOfLike;
                                        //get the perfection value of cingle's interactivity online
                                        double perfectionValue = GOLDEN_RATIO/x;
                                        //get the new worth of Cingle price in Sen
                                        final double cingleWorth = perfectionValue * likesPerMille * currentPrice;
                                        //round of the worth of the cingle to 4 decimal number
//                                        double finalPoints = Math.round( cingleWorth * 10000.0)/10000.0;


                                        double finalPoints = round( cingleWorth, 10);


                                        databaseReference.child(postKey).child("sensepoint").setValue(finalPoints);
                                    }
                                    else {
                                        double sensepoint = 0.00;

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

                viewHolder.cingleSettingsImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        FragmentManager fragmenManager = getChildFragmentManager();
                        CingleSettingsDialogFragment cingleSettingsDialogFragment = CingleSettingsDialogFragment.newInstance("create your cingle");
                        cingleSettingsDialogFragment.show(fragmenManager, "new post fragment");
                    }
                });


            }
        };

        layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        layoutManager.onSaveInstanceState();
        layoutManager.setAutoMeasureEnabled(true);

        cingleOutRecyclerView.setLayoutManager(layoutManager);
        cingleOutRecyclerView.setAdapter(firebaseRecyclerAdapter);
        cingleOutRecyclerView.setHasFixedSize(false);

        trace = new Trace.Builder()
                .setRecyclerView(cingleOutRecyclerView)
                .setMinimumViewingTimeThreshold(1000)
                .setMinimumVisibleHeightThreshold(60)
                .setTracingListener(this)
                .setDataDumpInterval(1000)
                .dumpDataAfterInterval(true)
                .build();

    }



    //region listeners
    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public final class StatefulRecyclerView extends RecyclerView{
        private static final String SAVED_SUPER_STATE = "super_state";
        private static final String SAVED_LAYOUT_MANAGER = "layout_manager_state";

        private Parcelable mLayoutManagerSavedState;

        public StatefulRecyclerView(Context context){
            super(context);
        }

        public StatefulRecyclerView(Context context, @Nullable AttributeSet attrs) {
            super(context, attrs);
        }

        public StatefulRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
        }

        @Override
        protected Parcelable onSaveInstanceState() {
            Bundle bundle = new Bundle();
            bundle.putParcelable(SAVED_SUPER_STATE, super.onSaveInstanceState());
            bundle.putParcelable(SAVED_LAYOUT_MANAGER, this.getLayoutManager().onSaveInstanceState());
            return bundle;
        }

        @Override
        protected void onRestoreInstanceState(Parcelable state) {
            if (state instanceof Bundle) {
                Bundle bundle = (Bundle) state;
                mLayoutManagerSavedState = bundle.getParcelable(SAVED_LAYOUT_MANAGER);
                state = bundle.getParcelable(SAVED_SUPER_STATE);
            }
            super.onRestoreInstanceState(state);
        }

        private void restorePosition() {
            if (mLayoutManagerSavedState != null) {
                this.getLayoutManager().onRestoreInstanceState(mLayoutManagerSavedState);
                mLayoutManagerSavedState = null;
            }
        }

        @Override
        public void setAdapter(Adapter firebaseRecyclerAdapter) {
            super.setAdapter(firebaseRecyclerAdapter);
            restorePosition();
        }
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
//
//    public void generateRandom(){
//        databaseReference.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                long allCngles = dataSnapshot.getChildrenCount();
//                int maxNum = (int) allCngles;
//                int minNum = 1;
//                int randomNum =  new Random().nextInt(maxNum - minNum + 1);
//
//                int count = 0;
//
//                Iterable<DataSnapshot> dataSnapshots = dataSnapshot.getChildren();
//                Iterator<DataSnapshot> snapshotIterator = dataSnapshots.iterator();
//                String newCingle = "";
//
//                while (snapshotIterator.hasNext() && count < randomNum){
//                    newCingle = (String) snapshotIterator.next().getValue();
//                    count ++;
//                }
//
//                Toast.makeText(getContext(), newCingle, Toast.LENGTH_LONG).show();
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
//    }


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

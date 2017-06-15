package com.cinggl.cinggl.home;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.cinggl.cinggl.Constants;
import com.cinggl.cinggl.R;
import com.cinggl.cinggl.adapters.FirebaseCingleOutViewHolder;
import com.cinggl.cinggl.utils.FirebaseUtil;
import com.cinggl.cinggl.models.Cingle;
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

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class CingleOutFragment extends Fragment {
    private DatabaseReference databaseReference;
    private FirebaseRecyclerAdapter firebaseRecyclerAdapter;
    @Bind(R.id.cingleOutRecyclerView)RecyclerView cingleOutRecyclerView;
    @Bind(R.id.scrollView) ScrollView cingleOutScrollView;

    private TextView likesCountTextView;
    private ImageView likesImageView;
    private ImageView commentsImageView;
    private boolean processLikes = false;
    private DatabaseReference likesRef;
    private FirebaseAuth firebaseAuth;
    private static final String TAG = "CingleOutFragment";


    public CingleOutFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_cingle_out, container, false);
        ButterKnife.bind(this, view);

        likesRef = FirebaseDatabase.getInstance().getReference("likesByUser");

        firebaseAuth = FirebaseAuth.getInstance();

        likesRef.keepSynced(true);

        setUpFirebaseAdapter();
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_layout, menu);
        super.onCreateOptionsMenu(menu, inflater);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement

        return super.onOptionsItemSelected(item);
    }

    private void setUpFirebaseAdapter(){
        databaseReference = FirebaseDatabase.getInstance()
                .getReference(Constants.FIREBASE_PUBLIC_CINGLES);
        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Cingle, FirebaseCingleOutViewHolder>
                (Cingle.class, R.layout.cingle_out_list, FirebaseCingleOutViewHolder.class, databaseReference) {
            @Override
            protected void populateViewHolder(FirebaseCingleOutViewHolder viewHolder, Cingle model, int position) {
                viewHolder.bindCingle(model);
                DatabaseReference cingleRef = getRef(position);
                final String postKey = cingleRef.getKey();
//
//                if(model.likeByUser.containsKey(firebaseAuth.getCurrentUser().getUid())){
//                    viewHolder.likesImageView.setImageResource(R.drawable.ic_favorite_black_24dp);
//                }else{
//                    viewHolder.likesImageView.setImageResource(R.drawable.ic_favorite_border_black_24dp);
//                }
//


                viewHolder.likesImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        processLikes = true;
                            likesRef.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    if(processLikes){
                                        if(dataSnapshot.child(postKey).hasChild(firebaseAuth.getCurrentUser().getUid())){
                                            likesRef.child(postKey).child(firebaseAuth.getCurrentUser()
                                                    .getUid())
                                                    .removeValue();
                                            onLikeCounter(false);
                                            processLikes = false;

                                        }else {
                                            likesRef.child(postKey).child(firebaseAuth.getCurrentUser()
                                                    .getUid())
                                                    .setValue(firebaseAuth.getCurrentUser().getUid());
                                            onLikeCounter(true);
                                            Log.i(dataSnapshot.getKey(), dataSnapshot.getChildrenCount() + "Count");
                                            processLikes = false;
                                        }
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
                        startActivity(intent);
                    }
                });
            }
        };

        cingleOutRecyclerView.setAdapter(firebaseRecyclerAdapter);
        cingleOutRecyclerView.setHasFixedSize(false);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setAutoMeasureEnabled(true);
        cingleOutRecyclerView.setLayoutManager(layoutManager);
    }

//    public void onLikeCounter(DatabaseReference likesRef){
//        likesRef.runTransaction(new Transaction.Handler() {
//                    @Override
//                    public Transaction.Result doTransaction(MutableData mutableData) {
//                        Cingle cingle = mutableData.getValue(Cingle.class);
//                        if(cingle == null){
//                            return Transaction.success(mutableData);
//                        }
//
//                        if(cingle.likeByUser.containsKey(firebaseAuth.getCurrentUser().getUid())){
//                            Log.i(TAG, "Cingulan had already liked the cingle, now he just unliked");
//                            cingle.likesCount = cingle.likesCount - 1;
//                            cingle.likeByUser.remove(firebaseAuth.getCurrentUser().getUid());
//                        }else {
//                            Log.i(TAG, "Cingulan has liked the cingle");
//                            cingle.likesCount = cingle.likesCount + 1;
//                            cingle.likeByUser.put(firebaseAuth.getCurrentUser().getUid(), true);
//                        }
//
//                        mutableData.setValue(cingle);
//                        return  Transaction.success(mutableData);
//                    }
//
//                    @Override
//                    public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
//                        Log.d(TAG, "postTransaction:onComplete:" + databaseError);
//                    }
//                });
//
//    }

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

    @Override
    public void onStop(){
        super.onStop();
    }
}

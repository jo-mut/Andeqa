package com.cinggl.cinggl.home;


import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
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
import com.cinggl.cinggl.adapters.LikesAdapter;
import com.cinggl.cinggl.models.Cingulan;
import com.cinggl.cinggl.models.Like;
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
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 */
public class CingleOutFragment extends Fragment {
    private DatabaseReference databaseReference;
    private FirebaseRecyclerAdapter firebaseRecyclerAdapter;
    @Bind(R.id.cingleOutRecyclerView)RecyclerView cingleOutRecyclerView;

    private TextView likesCountTextView;
    private ImageView likesImageView;
    private ImageView commentsImageView;
    private TextView cingleTitleTextView;
    private TextView commentsCountTextView;
    private TextView cingleDescriptionTextView;
    private TextView accountUsernameTextView;
    private CircleImageView profileImageView;
    private LikesAdapter likesAdapter;
    private boolean processLikes = false;
    private DatabaseReference usernameRef;
    private DatabaseReference likesRef;
    private DatabaseReference recentLikesRef;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference commentReference;
    private static final String TAG = "CingleOutFragment";
    private static final String EXTRA_POST_KEY = "post key";


    public CingleOutFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_cingle_out, container, false);
        ButterKnife.bind(this, view);

        likesRef = FirebaseDatabase.getInstance().getReference(Constants.LIKES);
        usernameRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_USERS);

        commentReference = FirebaseDatabase.getInstance()
                .getReference(Constants.COMMENTS);

        firebaseAuth = FirebaseAuth.getInstance();

        likesRef.keepSynced(true);
        usernameRef.keepSynced(true);
        commentReference.keepSynced(true);

        setUpFirebaseAdapter();
        return view;
    }

    private void setUpFirebaseAdapter(){
        databaseReference = FirebaseDatabase.getInstance()
                .getReference(Constants.FIREBASE_CINGLES);
        databaseReference.keepSynced(true);
        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Cingle, FirebaseCingleOutViewHolder>
                (Cingle.class, R.layout.cingle_out_list, FirebaseCingleOutViewHolder.class, databaseReference) {
            @Override
            protected void populateViewHolder(final FirebaseCingleOutViewHolder viewHolder, Cingle model, int position) {
                viewHolder.bindCingle(model);
                DatabaseReference cingleRef = getRef(position);
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
                                                        .into(viewHolder.profileImageView);
                                            }
                                        });

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
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
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    String username = (String) dataSnapshot.child("username").getValue();
                                    String uid = (String) dataSnapshot.child("uid").getValue();
                                    String profileImage = (String) dataSnapshot.child("profileImage").getValue();

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
                                                    final String username = (String) dataSnapshot.child("username").getValue();
                                                    final String uid = (String) dataSnapshot.child("uid").getValue();
                                                    final String profileImage = (String) dataSnapshot.child("profileImage").getValue();

                                                    final Like like = new Like();

                                                    like.setUid(uid);
                                                    like.setUsername(username);
                                                    like.setProfileImage(profileImage);

                                                    likesRef.child(postKey).child(firebaseAuth.getCurrentUser()
                                                            .getUid())
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

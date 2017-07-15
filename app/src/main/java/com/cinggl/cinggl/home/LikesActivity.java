package com.cinggl.cinggl.home;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.cinggl.cinggl.Constants;
import com.cinggl.cinggl.R;
import com.cinggl.cinggl.adapters.LikesViewHolder;
import com.cinggl.cinggl.models.Like;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

import static com.cinggl.cinggl.home.CommentsActivity.EXTRA_POST_KEY;

public class LikesActivity extends AppCompatActivity {
    @Bind(R.id.recentLikesRecyclerView)RecyclerView mRecentLikesRecyclerView;

    private DatabaseReference likesRef;
    private DatabaseReference usernameRef;
    private DatabaseReference relationsRef;
    private FirebaseAuth firebaseAuth;
    private CircleImageView profileImageView;
    private String mPostKey;
    private FirebaseRecyclerAdapter firebaseRecyclerAdapter;
    private Button followButton;
    private boolean processFollow = false;
    private static final String TAG = LikesActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_likes);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        firebaseAuth = FirebaseAuth.getInstance();

        mPostKey = getIntent().getStringExtra(EXTRA_POST_KEY);
        if(mPostKey == null){
            throw new IllegalArgumentException("pass an EXTRA_POST_KEY");
        }
        likesRef = FirebaseDatabase.getInstance()
                .getReference(Constants.LIKES).child(mPostKey);
        usernameRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_USERS);
        relationsRef = FirebaseDatabase.getInstance().getReference(Constants.FOLLOWERS);
        likesRef.keepSynced(true);
        setUpFirebaseLikes();
//        fetchData();

    }


    @Override
    protected void onStart() {
        super.onStart();
//        likesAdapter = new CinglesAdapter(this, likesRef);
//        mRecentLikesRecyclerView.setAdapter(likesAdapter);
//        mRecentLikesRecyclerView.setHasFixedSize(false);
//        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
//        layoutManager.setAutoMeasureEnabled(true);
//        mRecentLikesRecyclerView.setNestedScrollingEnabled(false);
//        mRecentLikesRecyclerView.setLayoutManager(layoutManager);
    }


    @Override
    public void onStop(){
        super.onStop();
        //remove the event listner
//        likesAdapter.cleanUpListener();
        firebaseRecyclerAdapter.cleanup();
    }


    public void setUpFirebaseLikes(){
        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Like, LikesViewHolder>
                (Like.class, R.layout.likes_list_layout ,LikesViewHolder.class, likesRef) {
            @Override
            protected void populateViewHolder(final LikesViewHolder viewHolder, Like model, int position) {
                DatabaseReference userRef = getRef(position);
                final String postKey = userRef.getKey();
                viewHolder.bindLikes(model);
//

                likesRef.child(postKey).addValueEventListener(new ValueEventListener() {
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


                                    Picasso.with(LikesActivity.this)
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
                                                    Picasso.with(LikesActivity.this)
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

                relationsRef.child(postKey).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.hasChild(firebaseAuth.getCurrentUser().getUid())){
                            viewHolder.followButton.setText("Following");
                        }else {
                            viewHolder.followButton.setText("Follow");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                viewHolder.followButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        processFollow = true;
                        relationsRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (processFollow){
                                    if (dataSnapshot.child(postKey).hasChild(firebaseAuth.getCurrentUser().getUid())){
                                        relationsRef.child(postKey)
                                                .removeValue();
                                        processFollow = false;
                                        onFollow(false);
                                        //set the text on the button to follow if the user in not yet following;
//                                        followButton.setText("FOLLOW");

                                    }else {
                                        try {
                                            relationsRef.child(postKey).child(firebaseAuth.getCurrentUser().getUid())
                                                    .child("uid").setValue(firebaseAuth.getCurrentUser().getUid());
                                            processFollow = false;
                                            onFollow(false);

                                            //set text on the button to following;
                                            viewHolder.followButton.setText("Following");

                                        }catch (Exception e){

                                        }

                                    }

                                }

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                });

            }
        };


        mRecentLikesRecyclerView.setAdapter(firebaseRecyclerAdapter);
        mRecentLikesRecyclerView.setHasFixedSize(false);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        layoutManager.onSaveInstanceState();
        layoutManager.setAutoMeasureEnabled(true);
        mRecentLikesRecyclerView.setLayoutManager(layoutManager);

    }

    private void onFollow(final boolean increament){
        relationsRef.runTransaction(new Transaction.Handler() {
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
                Log.d(TAG, "followTransaction:onComplete" + databaseError);

            }
        });
    }


}

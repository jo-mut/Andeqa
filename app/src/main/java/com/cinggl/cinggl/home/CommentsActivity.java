package com.cinggl.cinggl.home;

import android.content.Intent;
import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cinggl.cinggl.Constants;
import com.cinggl.cinggl.R;
import com.cinggl.cinggl.adapters.CommentViewHolder;
import com.cinggl.cinggl.models.Cingulan;
import com.cinggl.cinggl.models.Comment;
import com.cinggl.cinggl.profile.FollowerProfileActivity;
import com.cinggl.cinggl.profile.PersonalProfileActivity;
import com.cinggl.cinggl.services.ConnectivityReceiver;
import com.cinggl.cinggl.utils.App;
import com.cinggl.cinggl.utils.ProportionalImageView;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

public class CommentsActivity extends AppCompatActivity implements View.OnClickListener
    , ConnectivityReceiver.ConnectivityReceiverListener{
    @Bind(R.id.sendCommentImageView)ImageView mSendCommentImageView;
    @Bind(R.id.commentEditText)EditText mCommentEditText;
    @Bind(R.id.commentsRecyclerView)RecyclerView mCommentsRecyclerView;
    @Bind(R.id.cingleImageView)ProportionalImageView mCingleImageView;
    @Bind(R.id.accountUsernameTextView)TextView mAccountUsernameTextView;
    @Bind(R.id.userProfileImageView)CircleImageView mUserProfileImageView;

    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private String mPostKey;
    private FirebaseRecyclerAdapter firebaseRecyclerAdapter;
    private DatabaseReference commentReference;
    private DatabaseReference relationsRef;
    private DatabaseReference cinglesReference;
    public static final String EXTRA_POST_KEY = "post key";
    private static final String EXTRA_USER_UID = "uid";
    private static final String TAG = CommentsActivity.class.getSimpleName();
    private boolean processFollow = false;
    private DatabaseReference usernameRef;
    private TextView usernameTextView;
    private CircleImageView profileImageView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        mSendCommentImageView.setOnClickListener(this);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        mPostKey = getIntent().getStringExtra(EXTRA_POST_KEY);
        if(mPostKey == null){
            throw new IllegalArgumentException("pass an EXTRA_POST_KEY");
        }

        cinglesReference = FirebaseDatabase.getInstance()
                .getReference(Constants.FIREBASE_CINGLES).child(mPostKey);

        commentReference = FirebaseDatabase.getInstance()
                .getReference(Constants.COMMENTS).child(mPostKey);
        usernameRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_USERS);
        relationsRef =  FirebaseDatabase.getInstance().getReference(Constants.FOLLOWERS);
        setUpFirebaseComments();
        cinglesReference.keepSynced(true);
        usernameRef.keepSynced(true);
        cinglesReference.keepSynced(true);

        setUserProfile();

    }

    public void setUserProfile(){
        cinglesReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final String image = (String) dataSnapshot.child(Constants.CINGLE_IMAGE).getValue();
                String uid = (String) dataSnapshot.child(Constants.UID).getValue();

                usernameRef.child(uid).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String username = (String) dataSnapshot.child("username").getValue();
                        final String profileImage = (String) dataSnapshot.child("profileImage").getValue();

                        mAccountUsernameTextView.setText(username);
                        Picasso.with(CommentsActivity.this)
                                .load(profileImage)
                                .fit()
                                .centerCrop()
                                .placeholder(R.drawable.profle_image_background)
                                .networkPolicy(NetworkPolicy.OFFLINE)
                                .into(mUserProfileImageView, new Callback() {
                                    @Override
                                    public void onSuccess() {

                                    }

                                    @Override
                                    public void onError() {
                                        Picasso.with(CommentsActivity.this)
                                                .load(profileImage)
                                                .fit()
                                                .centerCrop()
                                                .placeholder(R.drawable.profle_image_background)
                                                .into(mUserProfileImageView);
                                    }
                                });

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                Picasso.with(CommentsActivity.this)
                        .load(image)
                        .networkPolicy(NetworkPolicy.OFFLINE)
                        .into(mCingleImageView, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError() {
                                Picasso.with(CommentsActivity.this)
                                        .load(image)
                                        .into(mCingleImageView);
                            }
                        });


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void setUpFirebaseComments(){
        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Comment, CommentViewHolder>
                (Comment.class, R.layout.comments_layout_list, CommentViewHolder.class, commentReference) {
            @Override
            protected void populateViewHolder(final CommentViewHolder viewHolder, final Comment model, int position) {
                DatabaseReference userRef = getRef(position);
                final String postKey = userRef.getKey();
                viewHolder.bindComment(model);

                commentReference.child(postKey).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final String uid = (String) dataSnapshot.child("uid").getValue();

                        usernameRef.child(uid).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                String username = (String) dataSnapshot.child("username").getValue();
                                final String profileImage = (String) dataSnapshot.child("profileImage").getValue();

                                viewHolder.usernameTextView.setText(username);
                                Picasso.with(CommentsActivity.this)
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
                                                Picasso.with(CommentsActivity.this)
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

                        relationsRef.child(uid).addValueEventListener(new ValueEventListener() {
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
                                            if (dataSnapshot.child(uid).hasChild(firebaseAuth.getCurrentUser().getUid())){
                                                relationsRef.child(uid)
                                                        .removeValue();
                                                processFollow = false;
                                                onFollow(false);
                                                //set the text on the button to follow if the user in not yet following;
                                                viewHolder.followButton.setText("Follow");

                                            }else {
                                                relationsRef.child(uid).child(firebaseAuth.getCurrentUser().getUid())
                                                        .child("uid").setValue(firebaseAuth.getCurrentUser().getUid());
                                                processFollow = false;
                                                onFollow(false);

                                                //set text on the button to following;
                                                viewHolder.followButton.setText("Following");

                                            }

                                        }

                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }
                        });

                        viewHolder.profileImageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (uid.equals(firebaseAuth.getCurrentUser().getUid())){
                                    Intent intent = new Intent(CommentsActivity.this, PersonalProfileActivity.class);
                                    startActivity(intent);
                                }else {
                                    Intent intent = new Intent(CommentsActivity.this, FollowerProfileActivity.class);
                                    intent.putExtra(CommentsActivity.EXTRA_USER_UID, uid);
                                    startActivity(intent);
                                }
                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }

                });

            }

        };

        mCommentsRecyclerView.setAdapter(firebaseRecyclerAdapter);
        mCommentsRecyclerView.setHasFixedSize(false);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setAutoMeasureEnabled(true);
        mCommentsRecyclerView.setLayoutManager(layoutManager);
    }

    @Override
    protected void onStart() {
        super.onStart();
//        commentAdapter = new CommentAdapter(this, commentReference);
//        mCommentsRecyclerView.setAdapter(commentAdapter);
//        mCommentsRecyclerView.setHasFixedSize(false);
//        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
//        layoutManager.setAutoMeasureEnabled(true);
//        mCommentsRecyclerView.setNestedScrollingEnabled(false);
//        mCommentsRecyclerView.setLayoutManager(layoutManager);
    }


    @Override
    public void onStop(){
        super.onStop();
        //remove the event listner
        firebaseRecyclerAdapter.cleanup();
    }
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.menu_layout, menu);
//        return super.onCreateOptionsMenu(menu);
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        int id = item.getItemId();
//
//        return super.onOptionsItemSelected(item);
//    }


    //let a cingulan add a comment to a cingle
    @Override
    public void onClick(View v){
        final String uid = firebaseAuth.getCurrentUser().getUid();
        final String commentText = mCommentEditText.getText().toString().trim();
        if(!TextUtils.isEmpty(commentText)){
            if(v == mSendCommentImageView){
                usernameRef.child(firebaseAuth.getCurrentUser().getUid())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                String username = (String) dataSnapshot.child("username").getValue();
                                String uid = (String) dataSnapshot.child("uid").getValue();
                                String profileImage = (String) dataSnapshot.child("profileImage").getValue();

                                Comment comment = new Comment();
                                comment.setUid(uid);
                                comment.setCommentText(mCommentEditText.getText().toString());
                                commentReference.push().setValue(comment);
                                mCommentEditText.setText("");
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });



            }
        }

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
    // Method to manually check connection status
    private void checkConnection() {
        boolean isConnected = ConnectivityReceiver.isConnected();
        showConnection(isConnected);
    }

    //Showing the status in Snackbar
    private void showConnection(boolean isConnected) {
        String message;
        if (isConnected) {
            message = "Connected to the internet";
        } else {
            message = "You are disconnected from the internet";
        }

        Toast.makeText(this, message, Toast.LENGTH_LONG).show();

    }

    @Override
    protected void onResume() {
        super.onResume();

        // register connection status listener
        App.getInstance().setConnectivityListener(this);
//        checkConnection();

    }

    /**
     * Callback will be triggered when there is change in
     * network connection
     */
    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        showConnection(isConnected);
    }

}

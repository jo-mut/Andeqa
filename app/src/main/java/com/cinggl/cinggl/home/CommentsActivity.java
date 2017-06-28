package com.cinggl.cinggl.home;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.cinggl.cinggl.Constants;
import com.cinggl.cinggl.R;
import com.cinggl.cinggl.adapters.CommentAdapter;
import com.cinggl.cinggl.models.Cingulan;
import com.cinggl.cinggl.models.Comment;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;

public class CommentsActivity extends AppCompatActivity implements View.OnClickListener {
    @Bind(R.id.sendCommentImageView)ImageView mSendCommentImageView;
    @Bind(R.id.commentEditText)EditText mCommentEditText;
    @Bind(R.id.commentsRecyclerView)RecyclerView mCommentsRecyclerView;
    @Bind(R.id.cingleImageView)ImageView mCingleImageView;

    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private String mPostKey;
    private FirebaseRecyclerAdapter firebaseRecyclerAdapter;
    private DatabaseReference commentReference;
    private DatabaseReference cinglesReference;
    public static final String EXTRA_POST_KEY = "post key";
    private CommentAdapter commentAdapter;
    public static final int MAX_WIDTH = 400;
    public static final int MAX_HEIGHT = 400;
    private DatabaseReference usernameRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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

        cinglesReference.keepSynced(true);
        usernameRef.keepSynced(true);
        cinglesReference.keepSynced(true);

        cinglesReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final String image = (String) dataSnapshot.child(Constants.CINGLE_IMAGE).getValue();
                String uid = (String) dataSnapshot.child(Constants.UID).getValue();

                Picasso.with(CommentsActivity.this)
                        .load(image)
                        .resize(MAX_WIDTH, MAX_HEIGHT)
                        .onlyScaleDown()
                        .centerInside()
                        .into(mCingleImageView);

                Picasso.with(CommentsActivity.this)
                        .load(image)
                        .fit()
                        .centerCrop()
                        .networkPolicy(NetworkPolicy.OFFLINE)
                        .into(mCingleImageView, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError() {
                                Picasso.with(CommentsActivity.this)
                                        .load(image)
                                        .fit()
                                        .centerCrop()
                                        .into(mCingleImageView);
                            }
                        });


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        commentAdapter = new CommentAdapter(this, commentReference);
        mCommentsRecyclerView.setAdapter(commentAdapter);
        mCommentsRecyclerView.setHasFixedSize(false);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setAutoMeasureEnabled(true);
        mCommentsRecyclerView.setNestedScrollingEnabled(false);
        mCommentsRecyclerView.setLayoutManager(layoutManager);
    }


    @Override
    public void onStop(){
        super.onStop();
        //remove the event listner
        commentAdapter.cleanupListener();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_layout, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }


    //let a cingulan add a comment to a cingle
    @Override
    public void onClick(View v){
        final String uid = firebaseAuth.getCurrentUser().getUid();
        final String commentText = mCommentEditText.getText().toString().trim();
        if(!TextUtils.isEmpty(commentText)){
            if(v == mSendCommentImageView){
                FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_USERS).child(uid)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                //get the user info
                                final Cingulan cingulan = dataSnapshot.getValue(Cingulan.class);

                                usernameRef.child(firebaseAuth.getCurrentUser().getUid())
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        String username = (String) dataSnapshot.child("username").getValue();
                                        String uid = (String) dataSnapshot.child("uid").getValue();
                                        String profileImage = (String) dataSnapshot.child("profileImage").getValue();

                                        Comment comment = new Comment();
                                        comment.setUsername(username);
                                        comment.setUid(uid);
                                        comment.setProfileImage(profileImage);
//                                        comment.setUid(firebaseAuth.getCurrentUser().getUid());
//                                        comment.setUsername(cingulan.getUsername());
                                        comment.setCommentText(mCommentEditText.getText().toString());
                                        commentReference.push().setValue(comment);
                                        mCommentEditText.setText("");
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

            }
        }

    }



}

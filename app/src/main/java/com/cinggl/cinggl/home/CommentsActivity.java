package com.cinggl.cinggl.home;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;

import com.cinggl.cinggl.Constants;
import com.cinggl.cinggl.R;
import com.cinggl.cinggl.adapters.CommentAdapter;
import com.cinggl.cinggl.models.Cingulan;
import com.cinggl.cinggl.models.Comment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import butterknife.Bind;
import butterknife.ButterKnife;

public class CommentsActivity extends AppCompatActivity implements View.OnClickListener {
    @Bind(R.id.sendCommentImageView)ImageView mSendCommentImageView;
    @Bind(R.id.commentEditText)EditText mCommentEditText;
    @Bind(R.id.commentsRecyclerView)RecyclerView mCommentsRecyclerView;

    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private String mPostKey;
    private DatabaseReference commentReference;
    private DatabaseReference cinglesReference;
    public static final String EXTRA_POST_KEY = "post key";
    private CommentAdapter commentAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);
        ButterKnife.bind(this);

        mSendCommentImageView.setOnClickListener(this);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        mPostKey = getIntent().getStringExtra(EXTRA_POST_KEY);
        if(mPostKey == null){
            throw new IllegalArgumentException("pass an EXTRA_POST_KEY");
        }
        cinglesReference = FirebaseDatabase.getInstance()
                .getReference(Constants.FIREBASE_PUBLIC_CINGLES).child(mPostKey);

        commentReference = FirebaseDatabase.getInstance()
                .getReference("Comments").child(mPostKey);




    }

    @Override
    protected void onStart() {
        super.onStart();
        commentAdapter = new CommentAdapter(this, commentReference);
        mCommentsRecyclerView.setAdapter(commentAdapter);
        mCommentsRecyclerView.setHasFixedSize(false);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setAutoMeasureEnabled(true);
        mCommentsRecyclerView.setLayoutManager(layoutManager);
    }


    @Override
    public void onStop(){
        super.onStop();
        //remove the event listner
        commentAdapter.cleanupListener();
    }

    //let a cingulan add a comment to a cingle
    @Override
    public void onClick(View v){
        final String uid = firebaseAuth.getCurrentUser().getUid();
        if(v == mSendCommentImageView){
            FirebaseDatabase.getInstance().getReference().child("Users").child(uid)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            //get the user info
                            Cingulan cingulan = dataSnapshot.getValue(Cingulan.class);
                            Comment comment = new Comment();
                            comment.setUid(firebaseAuth.getCurrentUser().getUid());
                            comment.setUsername(cingulan.getUsername());
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

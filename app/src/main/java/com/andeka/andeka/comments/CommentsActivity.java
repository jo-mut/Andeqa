package com.andeka.andeka.comments;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.andeka.andeka.Constants;
import com.andeka.andeka.R;
import com.andeka.andeka.models.Comment;
import com.andeka.andeka.models.Post;
import com.andeka.andeka.models.Timeline;
import com.andeka.andeka.utils.EndlessLinearScrollListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class CommentsActivity extends AppCompatActivity implements View.OnClickListener{
    @Bind(R.id.commentEditText)EditText mCommentEditText;
    @Bind(R.id.commentsRecyclerView)RecyclerView mCommentsRecyclerView;
    @Bind(R.id.sendCommentsRelativeLayout)RelativeLayout mSendCommentRelativeLayout;
    @Bind(R.id.sendCommentImageView)ImageView mSendCommentImageView;
    @Bind(R.id.toolbar)Toolbar toolbar;

    private CollectionReference usersReference;
    private CollectionReference commentsCollection;
    private Query commentQuery;
    private DatabaseReference databaseReference;
    private CollectionReference postsCollections;
    private CollectionReference timelineCollection;

    private static final String EXTRA_POST_ID = "post id";
    private static final String EXTRA_USER_UID = "uid";
    private String mPostId;

    private FirebaseAuth firebaseAuth;
    private CommentsAdapter commentsAdapter;
    private static final int DEFAULT_COMMENT_LENGTH_LIMIT = 500;
    private static final int TOTAL_ITEMS = 25;
    private List<DocumentSnapshot> snapshots = new ArrayList<>();
    private static final String TAG = CommentsActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow);
        toolbar.setTitle("Comments");
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mSendCommentImageView.setOnClickListener(this);
        firebaseAuth = FirebaseAuth.getInstance();

        if (getIntent().getStringExtra(EXTRA_POST_ID) != null){
            mPostId = getIntent().getStringExtra(EXTRA_POST_ID);
        }

        usersReference = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);
        commentsCollection = FirebaseFirestore.getInstance().collection(Constants.COMMENTS);
        postsCollections = FirebaseFirestore.getInstance().collection(Constants.POSTS);
        timelineCollection = FirebaseFirestore.getInstance().collection(Constants.TIMELINE);
        databaseReference = FirebaseDatabase.getInstance().getReference(Constants.RANDOM_PUSH_ID);

    }


    @Override
    public void onClick(View v){
        if (v == mSendCommentImageView){
            sendComment();
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        // clear the recycler view before adding new data
        snapshots.clear();
        // set the recyclee view adapter
        setRecyclerView();
        // get remote snapshots
        setCollections();


        mCommentsRecyclerView.addOnScrollListener(new EndlessLinearScrollListener() {
            @Override
            public void onLoadMore() {
                setNextComments();
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    private void sendComment(){
        final long time = new Date().getTime();
        final String uid = firebaseAuth.getCurrentUser().getUid();
        final String commentText = mCommentEditText.getText().toString().trim();
        if(!TextUtils.isEmpty(commentText)){
            final String commentId = databaseReference.push().getKey();

            Comment comment = new Comment();
            comment.setUser_id(uid);
            comment.setComment_text(commentText);
            comment.setPost_id(mPostId);
            comment.setComment_id(commentId);
            comment.setTime(time);
            commentsCollection.document("post_ids").collection(mPostId)
                    .document(commentId).set(comment);


            //record the comment on the timeline
            postsCollections.document(mPostId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                    if (e != null) {
                        Log.w(TAG, "Listen error", e);
                        return;
                    }

                    if (documentSnapshot.exists()){
                        Post post = documentSnapshot.toObject(Post.class);
                        final String creatorUid = post.getUser_id();

                        final Timeline timeline = new Timeline();
                        timeline.setActivity_id(commentId);
                        timeline.setTime(time);
                        timeline.setUser_id(firebaseAuth.getCurrentUser().getUid());
                        timeline.setType("comment");
                        timeline.setPost_id(mPostId);
                        timeline.setStatus("un_read");
                        timeline.setReceiver_id(creatorUid);
                        if (creatorUid.equals(firebaseAuth.getCurrentUser().getUid())){
                            //do nothing
                        }else {
                            timelineCollection.document(creatorUid)
                                    .collection("activities").document(commentId).set(timeline);
                        }

                    }
                }
            });

            mCommentEditText.setText("");
            mSendCommentRelativeLayout.setVisibility(View.GONE);
        }
    }

    private void setRecyclerView(){
        commentsAdapter = new CommentsAdapter(this, snapshots);
        mCommentsRecyclerView.setAdapter(commentsAdapter);
        mCommentsRecyclerView.setHasFixedSize(false);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mCommentsRecyclerView.setLayoutManager(layoutManager);
    }


    private void setCollections(){
        commentsCollection.document("post_ids").collection(mPostId)
                .orderBy("time", Query.Direction.DESCENDING)
                .limit(TOTAL_ITEMS).addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                        if (e != null) {
                            Log.w(TAG, "Listen error", e);
                            return;
                        }

                        if (!documentSnapshots.isEmpty()){
                            for (DocumentSnapshot snapshot: documentSnapshots){
                                snapshots.add(snapshot);
                                commentsAdapter.notifyItemInserted(snapshots.size() - 1);
                            }
                        }

                    }
                });
    }

    private void setNextComments(){
        // Get the last visible document
        final int snapshotSize = commentsAdapter.getItemCount();

        if (snapshotSize == 0){
            //do nothing
        }else{
            DocumentSnapshot lastVisible = commentsAdapter.getSnapshot(snapshotSize - 1);
            //retrieve the first bacth of documentSnapshots
            Query nextSellingQuery = commentsCollection.document("post_ids")
                    .collection(mPostId).orderBy("time", Query.Direction.DESCENDING)
                    .startAfter(lastVisible).limit(TOTAL_ITEMS);

            nextSellingQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(final QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                    if (e != null) {
                        Log.w(TAG, "Listen error", e);
                        return;
                    }

                    if (!documentSnapshots.isEmpty()){
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                for (DocumentSnapshot snapshot: documentSnapshots){
                                    snapshots.add(snapshot);
                                    commentsAdapter.notifyItemInserted(snapshots.size() - 1);
                                }
                            }
                        },1000);
                    }
                }
            });
        }

    }

    }

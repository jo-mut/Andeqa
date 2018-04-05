package com.andeqa.andeqa.comments;

import android.content.Intent;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.R;
import com.andeqa.andeqa.models.Andeqan;
import com.andeqa.andeqa.models.CollectionPost;
import com.andeqa.andeqa.models.Single;
import com.andeqa.andeqa.models.Timeline;
import com.andeqa.andeqa.models.Comment;
import com.andeqa.andeqa.profile.ProfileActivity;
import com.andeqa.andeqa.utils.EndlessRecyclerOnScrollListener;
import com.andeqa.andeqa.utils.ProportionalImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class CommentsActivity extends AppCompatActivity implements
        View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {
    @Bind(R.id.sendCommentImageView)ImageView mSendCommentImageView;
    @Bind(R.id.commentEditText)EditText mCommentEditText;
    @Bind(R.id.commentsRecyclerView)RecyclerView mCommentsRecyclerView;
    @Bind(R.id.postImageView)ProportionalImageView mCingleImageView;
    @Bind(R.id.usernameTextView)TextView mAccountUsernameTextView;
    @Bind(R.id.profileImageView)CircleImageView mProfileImageView;
    @Bind(R.id.placeHolderRelativeLayout)RelativeLayout mPlaceHolderRelativeLayout;
    @Bind(R.id.titleTextView)TextView mCingleTitleTextView;
    @Bind(R.id.cingleTitleRelativeLayout)RelativeLayout mCingleTitleRelativeLayout;
    @Bind(R.id.swipeRefreshLayout)SwipeRefreshLayout mSwipeRefreshLayout;

    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private String mPostId;
    private String mCollectionId;
    //firebase
    private DatabaseReference databaseReference;
    //firestore
    private CollectionReference commentsCollection;
    private CollectionReference collectionsPostsCollection;
    private Query commentQuery;
    private CollectionReference usersCollection;
    private CollectionReference relationsCollection;
    private CollectionReference timelineCollection;
    private ListenerRegistration mListenerRegistration;

    //adapters
    private CommentsAdapter commentsAdapter;
    //member variables
    private static final String COLLECTION_ID = "collection id";
    private static final String EXTRA_POST_ID = "post id";
    private static final String EXTRA_USER_UID = "uid";
    private static final String TAG = CommentsActivity.class.getSimpleName();
    private boolean processFollow = false;
    private LinearLayoutManager layoutManager;
    private TextView usernameTextView;
    private CircleImageView profileImageView;
    private static final int DEFAULT_COMMENT_LENGTH_LIMIT = 500;
    private static final int TOTAL_ITEMS = 25;
    private List<String> commentsIds = new ArrayList<>();
    private List<DocumentSnapshot> comments = new ArrayList<>();

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

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        mSwipeRefreshLayout.setOnRefreshListener(this);

        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null){
            mSendCommentImageView.setOnClickListener(this);
            firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

            mPostId = getIntent().getStringExtra(EXTRA_POST_ID);
            if(mPostId == null){
                throw new IllegalArgumentException("pass a post id");
            }

            mCollectionId = getIntent().getStringExtra(COLLECTION_ID);
            if(mCollectionId == null){
                throw new IllegalArgumentException("pass a collection id");
            }

            //firestore
            collectionsPostsCollection = FirebaseFirestore.getInstance().collection(Constants.COLLECTIONS_POSTS)
                    .document("collections").collection(mCollectionId);
            commentsCollection = FirebaseFirestore.getInstance().collection(Constants.COMMENTS)
                    .document("post_ids").collection(mPostId);
            usersCollection = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);
            relationsCollection = FirebaseFirestore.getInstance().collection(Constants.RELATIONS);
            timelineCollection = FirebaseFirestore.getInstance().collection(Constants.TIMELINE);
            commentQuery = commentsCollection.orderBy("time", Query.Direction.DESCENDING);
            //firebase
            databaseReference = FirebaseDatabase.getInstance().getReference(Constants.RANDOM_PUSH_ID);

            mCommentEditText.setFilters(new InputFilter[]{new InputFilter
                    .LengthFilter(DEFAULT_COMMENT_LENGTH_LIMIT)});


            setData();
            setRecyclerView();
            setCollections();


        }
    }

    @Override
    public void onRefresh() {
        setNextCollections();
    }

    public void setData() {
        //get the cingle that user wants to comment on
        collectionsPostsCollection.document(mPostId)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (documentSnapshot.exists()){
                    final CollectionPost collectionPost = documentSnapshot.toObject(CollectionPost.class);
                    final String uid = collectionPost.getUid();
                    final String image = collectionPost.getImage();
                    final String title = collectionPost.getTitle();


                    //LAUCNH PROFILE IF ITS NOT DELETED ELSE CATCH THE EXCEPTION
                    mProfileImageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(CommentsActivity.this, ProfileActivity.class);
                            intent.putExtra(CommentsActivity.EXTRA_USER_UID, uid);
                            startActivity(intent);
                        }
                    });

                    //set the title of the single
                    if (title.equals("")) {
                        mCingleTitleRelativeLayout.setVisibility(View.GONE);
                    } else {
                        mCingleTitleTextView.setText(title);
                    }

                    //set the single image
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

                    //get the profile of the user wh just commented
                    usersCollection.document(uid).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                            if (e != null) {
                                Log.w(TAG, "Listen error", e);
                                return;
                            }

                            if (documentSnapshot.exists()){
                                final Andeqan cinggulan = documentSnapshot.toObject(Andeqan.class);
                                final String profileImage = cinggulan.getProfileImage();
                                final String username = cinggulan.getUsername();

                                mAccountUsernameTextView.setText(username);
                                Picasso.with(CommentsActivity.this)
                                        .load(profileImage)
                                        .fit()
                                        .centerCrop()
                                        .placeholder(R.drawable.profle_image_background)
                                        .networkPolicy(NetworkPolicy.OFFLINE)
                                        .into(mProfileImageView, new Callback() {
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
                                                        .into(mProfileImageView);
                                            }
                                        });

                            }
                        }
                    });

                }
            }
        });
    }

    private void setRecyclerView(){
        commentsAdapter = new CommentsAdapter(this);
        mCommentsRecyclerView.setAdapter(commentsAdapter);
        mCommentsRecyclerView.setHasFixedSize(false);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setAutoMeasureEnabled(true);
        mCommentsRecyclerView.setLayoutManager(layoutManager);
    }



    private void setCollections(){
        commentQuery.limit(TOTAL_ITEMS)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (!documentSnapshots.isEmpty()){
                    //retrieve the first bacth of documentSnapshots
                    for (final DocumentChange change : documentSnapshots.getDocumentChanges()) {
                        switch (change.getType()) {
                            case ADDED:
                                onDocumentAdded(change);
                                break;
                            case MODIFIED:
                                onDocumentModified(change);
                                break;
                            case REMOVED:
                                onDocumentRemoved(change);
                                break;
                        }
                    }
                }else {
                    mPlaceHolderRelativeLayout.setVisibility(View.VISIBLE);
                }

            }
        });
    }

    private void setNextCollections(){
        mSwipeRefreshLayout.setRefreshing(true);
        // Get the last visible document
        final int snapshotSize = commentsAdapter.getItemCount();
        DocumentSnapshot lastVisible = commentsAdapter.getSnapshot(snapshotSize - 1);

        //retrieve the first bacth of documentSnapshots
        Query nextSellingQuery = commentsCollection.orderBy("time").startAfter(lastVisible)
                .limit(TOTAL_ITEMS);

        nextSellingQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (!documentSnapshots.isEmpty()){
                    //retrieve the first bacth of documentSnapshots
                    for (final DocumentChange change : documentSnapshots.getDocumentChanges()) {
                        switch (change.getType()) {
                            case ADDED:
                                onDocumentAdded(change);
                                break;
                            case MODIFIED:
                                onDocumentModified(change);
                                break;
                            case REMOVED:
                                onDocumentRemoved(change);
                                break;
                        }
                    }
                    mSwipeRefreshLayout.setRefreshing(false);
                }else {
                    mSwipeRefreshLayout.setRefreshing(false);
                }
            }
        });
    }

    protected void onDocumentAdded(DocumentChange change) {
        commentsIds.add(change.getDocument().getId());
        comments.add(change.getDocument());
        commentsAdapter.setPostComments(comments);
        commentsAdapter.notifyItemInserted(comments.size() -1);
        commentsAdapter.getItemCount();

    }

    protected void onDocumentModified(DocumentChange change) {
        if (change.getOldIndex() == change.getNewIndex()) {
            // Item changed but remained in same position
            comments.set(change.getOldIndex(), change.getDocument());
            commentsAdapter.notifyItemChanged(change.getOldIndex());
        } else {
            // Item changed and changed position
            comments.remove(change.getOldIndex());
            comments.add(change.getNewIndex(), change.getDocument());
            commentsAdapter.notifyItemRangeChanged(0, comments.size());
        }
    }

    protected void onDocumentRemoved(DocumentChange change) {
        comments.remove(change.getOldIndex());
        commentsAdapter.notifyItemRemoved(change.getOldIndex());
        commentsAdapter.notifyItemRangeChanged(0, comments.size());
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onStop(){
        super.onStop();

    }



    @Override
    public void onResume() {
        super.onResume();
    }

    //let a cingulan add a comment to a cingle
    @Override
    public void onClick(View v){
        final long time = new Date().getTime();
        final String uid = firebaseAuth.getCurrentUser().getUid();
        final String commentText = mCommentEditText.getText().toString().trim();
        if(!TextUtils.isEmpty(commentText)){
            if(v == mSendCommentImageView){
                usersCollection.document(firebaseAuth.getCurrentUser().getUid())
                        .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen error", e);
                            return;
                        }

                        if (documentSnapshot.exists()){
                            final Andeqan cinggulan = documentSnapshot.toObject(Andeqan.class);
                            final String uid = cinggulan.getUid();

                            final String postId = databaseReference.push().getKey();

                            Comment comment = new Comment();
                            comment.setUid(uid);
                            comment.setCommentText(commentText);
                            comment.setPushId(mPostId);
                            comment.setPostId(postId);
                            comment.setTime(time);
                            commentsCollection.document(postId).set(comment);

                            final Timeline timeline = new Timeline();
                            final long time = new Date().getTime();

                            //record the comment on the timeline
                            collectionsPostsCollection.document(mPostId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                @Override
                                public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                                    if (e != null) {
                                        Log.w(TAG, "Listen error", e);
                                        return;
                                    }

                                    if (documentSnapshot.exists()){
                                        Single single = documentSnapshot.toObject(Single.class);
                                        final String creatorUid = single.getUid();

                                        timeline.setPostId(postId);
                                        timeline.setTime(time);
                                        timeline.setUid(firebaseAuth.getCurrentUser().getUid());
                                        timeline.setType("comment");
                                        timeline.setPushId(mPostId);
                                        timeline.setStatus("unRead");
                                        if (creatorUid.equals(firebaseAuth.getCurrentUser().getUid())){
                                            //do nothing
                                        }else {
                                            timelineCollection.document(creatorUid)
                                                    .collection("timeline").document(postId).set(timeline);
                                        }

                                    }
                                }
                            });

                            mCommentEditText.setText("");

                        }

                    }
                });

            }
        }

    }



}

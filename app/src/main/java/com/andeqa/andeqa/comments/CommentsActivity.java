package com.andeqa.andeqa.comments;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.R;
import com.andeqa.andeqa.models.Andeqan;
import com.andeqa.andeqa.models.Single;
import com.andeqa.andeqa.models.Relation;
import com.andeqa.andeqa.models.Timeline;
import com.andeqa.andeqa.models.Comment;
import com.andeqa.andeqa.people.FollowerProfileActivity;
import com.andeqa.andeqa.profile.PersonalProfileActivity;
import com.andeqa.andeqa.utils.ProportionalImageView;
import com.firebase.ui.common.ChangeEventType;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.firebase.ui.firestore.ObservableSnapshotArray;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.Date;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class CommentsActivity extends AppCompatActivity implements View.OnClickListener {
    @Bind(R.id.sendCommentImageView)ImageView mSendCommentImageView;
    @Bind(R.id.commentEditText)EditText mCommentEditText;
    @Bind(R.id.commentsRecyclerView)RecyclerView mCommentsRecyclerView;
    @Bind(R.id.postImageView)ProportionalImageView mCingleImageView;
    @Bind(R.id.usernameTextView)TextView mAccountUsernameTextView;
    @Bind(R.id.profileImageView)CircleImageView mUserProfileImageView;
    @Bind(R.id.saySomethingRelativeLayout)RelativeLayout mSaySomethingRelativeLayout;
    @Bind(R.id.titleTextView)TextView mCingleTitleTextView;
    @Bind(R.id.cingleTitleRelativeLayout)RelativeLayout mCingleTitleRelativeLayout;
    @Bind(R.id.commentCountTextView)TextView mCommentCountTextView;

    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private String mPostId;
    private String mCollectionId;
    //firebase
    private DatabaseReference databaseReference;
    //firestore
    private CollectionReference commentsReference;
    private CollectionReference collectionsCollection;
    private Query commentQuery;
    private CollectionReference usersReference;
    private CollectionReference relationsReference;
    private CollectionReference timelineCollection;
    //adapters
    private FirestoreRecyclerAdapter firestoreRecyclerAdapter;
    private static final String COLLECTION_ID = "collection id";
    private static final String EXTRA_POST_ID = "post id";
    private static final String EXTRA_USER_UID = "uid";
    private static final String TAG = CommentsActivity.class.getSimpleName();
    private boolean processFollow = false;
    private LinearLayoutManager layoutManager;
    private TextView usernameTextView;
    private CircleImageView profileImageView;
    private static final int DEFAULT_COMMENT_LENGTH_LIMIT = 500;

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
            collectionsCollection = FirebaseFirestore.getInstance().collection(Constants.COLLECTIONS)
                    .document("collection_posts").collection(mCollectionId);
            commentsReference = FirebaseFirestore.getInstance().collection(Constants.COMMENTS);
            usersReference = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);
            relationsReference = FirebaseFirestore.getInstance().collection(Constants.RELATIONS);
            timelineCollection = FirebaseFirestore.getInstance().collection(Constants.TIMELINE);
            //firebase
            databaseReference = FirebaseDatabase.getInstance().getReference(Constants.RANDOM_PUSH_ID);

            mCommentEditText.setFilters(new InputFilter[]{new InputFilter
                    .LengthFilter(DEFAULT_COMMENT_LENGTH_LIMIT)});
            textWatchers();

            setData();
            setUpFirebaseComments();

        }
    }

    private void textWatchers(){
        //TITLE TEXT WATCHER
        mCommentEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                int count = DEFAULT_COMMENT_LENGTH_LIMIT - editable.length();
                mCommentCountTextView.setText(Integer.toString(count));

                if (count < 0){
                }else if (count < 300){
                    mCommentCountTextView.setTextColor(Color.GRAY);
                }else {
                    mCommentCountTextView.setTextColor(Color.BLACK);
                }

            }
        });

    }

    public void setData() {
        //get the cingle that user wants to comment on
        collectionsCollection.document(mPostId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (documentSnapshot.exists()){
                    final Single single = documentSnapshot.toObject(Single.class);

                    final String uid = single.getUid();
                    final String image = single.getImage();
                    final String title = single.getTitle();


                    //LAUCNH PROFILE IF ITS NOT DELETED ELSE CATCH THE EXCEPTION
                    mUserProfileImageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (uid.equals(firebaseAuth.getCurrentUser().getUid())) {
                                Intent intent = new Intent(CommentsActivity.this, PersonalProfileActivity.class);
                                startActivity(intent);
                            } else {
                                Intent intent = new Intent(CommentsActivity.this, FollowerProfileActivity.class);
                                intent.putExtra(CommentsActivity.EXTRA_USER_UID, uid);
                                startActivity(intent);
                            }
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
                    usersReference.document(uid).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                            if (e != null) {
                                Log.w(TAG, "Listen error", e);
                                return;
                            }

                            if (documentSnapshot.exists()){
                                final Andeqan andeqan = documentSnapshot.toObject(Andeqan.class);
                                final String profileImage = andeqan.getProfileImage();
                                final String username = andeqan.getUsername();

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
                        }
                    });

                }
            }
        });
    }

    public void setUpFirebaseComments(){
        commentQuery = commentsReference.orderBy("postId").whereEqualTo("pushId", mPostId);
        FirestoreRecyclerOptions<Comment> options = new FirestoreRecyclerOptions.Builder<Comment>()
                .setQuery(commentQuery, Comment.class)
                .build();

        firestoreRecyclerAdapter = new FirestoreRecyclerAdapter<Comment, CommentViewHolder>(options) {
            @Override
            protected void onBindViewHolder(final CommentViewHolder holder, int position, Comment model) {
                holder.bindComment(model);
                final String postKey = getSnapshots().get(position).getPushId();
                final String uid = getSnapshots().get(position).getUid();

                //SET UP TEXTVIEW TO SHOW NO COMMENTS YET IF THERE ARE NO COMMENTS
                if (commentsReference.document(mPostId) != null){
                    mSaySomethingRelativeLayout.setVisibility(View.GONE);
                }else {
                    mSaySomethingRelativeLayout.setVisibility(View.VISIBLE);
                }

                holder.profileImageView.setOnClickListener(new View.OnClickListener() {
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

                //get the profile of the user wh just commented
                usersReference.document(uid).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                        if (e != null) {
                            Log.w(TAG, "Listen error", e);
                            return;
                        }

                        if (documentSnapshot.exists()){
                            final Andeqan andeqan = documentSnapshot.toObject(Andeqan.class);
                            final String profileImage = andeqan.getProfileImage();
                            final String username = andeqan.getUsername();

                            holder.usernameTextView.setText(username);
                            Picasso.with(CommentsActivity.this)
                                    .load(profileImage)
                                    .fit()
                                    .centerCrop()
                                    .placeholder(R.drawable.profle_image_background)
                                    .networkPolicy(NetworkPolicy.OFFLINE)
                                    .into(holder.profileImageView, new Callback() {
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
                                                    .into(holder.profileImageView);
                                        }
                                    });

                        }
                    }
                });


                relationsReference.document("following").collection(firebaseAuth.getCurrentUser().getUid())
                        .whereEqualTo("uid", uid).addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen error", e);
                            return;
                        }

                        if (documentSnapshots.isEmpty()){
                            holder.followButton.setText("Follow");
                        }else {
                            holder.followButton.setText("Following");
                        }
                    }
                });


                if (uid.equals(firebaseAuth.getCurrentUser().getUid())){
                    holder.followButton.setVisibility(View.GONE);
                }else {
                    holder.followButton.setVisibility(View.VISIBLE);
                    holder.followButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            processFollow = true;
                            relationsReference.document("followers")
                                    .collection(uid).whereEqualTo("uid", firebaseAuth.getCurrentUser().getUid())
                                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                        @Override
                                        public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {


                                            if (e != null) {
                                                Log.w(TAG, "Listen error", e);
                                                return;
                                            }

                                            if (processFollow){
                                                if (documentSnapshots.isEmpty()){
                                                    //set followers and following
                                                    Relation follower = new Relation();
                                                    follower.setUid(firebaseAuth.getCurrentUser().getUid());
                                                    relationsReference.document("followers").collection(uid)
                                                            .document(firebaseAuth.getCurrentUser().getUid()).set(follower)
                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            Timeline timeline = new Timeline();
                                                            final long time = new Date().getTime();

                                                            final String postId = databaseReference.push().getKey();

                                                            timeline.setPushId(postKey);
                                                            timeline.setTimeStamp(time);
                                                            timeline.setUid(firebaseAuth.getCurrentUser().getUid());
                                                            timeline.setType("followers");
                                                            timeline.setPostId(postId);
                                                            timelineCollection.document(uid).collection("timeline")
                                                                    .document(firebaseAuth.getCurrentUser().getUid())
                                                                    .set(timeline);
                                                        }
                                                    });
                                                    final Relation following = new Relation();
                                                    following.setUid(uid);
                                                    relationsReference.document("following").collection(firebaseAuth.getCurrentUser().getUid())
                                                            .document(uid).set(following);
                                                    processFollow = false;
                                                    holder.followButton.setText("Following");
                                                }else {
                                                    relationsReference.document("followers").collection(uid)
                                                            .document(firebaseAuth.getCurrentUser().getUid()).delete();
                                                    relationsReference.document("following").collection(firebaseAuth.getCurrentUser().getUid())
                                                            .document(uid).delete();
                                                    processFollow = false;
                                                    holder.followButton.setText("Follow");
                                                }
                                            }
                                        }
                                    });

                        }
                    });
                }

            }

            @Override
            public CommentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate
                        (R.layout.comments_layout_list, parent, false);
                return new CommentViewHolder(view);
            }


            @Override
            public void onError(FirebaseFirestoreException e) {
                super.onError(e);
            }

            @Override
            public void onDataChanged() {
                super.onDataChanged();
            }

            @Override
            public void onChildChanged(ChangeEventType type, DocumentSnapshot snapshot, int newIndex, int oldIndex) {
                super.onChildChanged(type, snapshot, newIndex, oldIndex);
            }

            @Override
            public int getItemCount() {
                return super.getItemCount();
            }

            @Override
            public Comment getItem(int position) {
                return super.getItem(position);
            }

            @Override
            public ObservableSnapshotArray<Comment> getSnapshots() {
                return super.getSnapshots();
            }
        };

        mCommentsRecyclerView.setAdapter(firestoreRecyclerAdapter);
        mCommentsRecyclerView.setHasFixedSize(false);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setAutoMeasureEnabled(true);
        mCommentsRecyclerView.setLayoutManager(layoutManager);
    }


    @Override
    protected void onStart() {
        super.onStart();
        firestoreRecyclerAdapter.startListening();

    }


    @Override
    public void onStop(){
        super.onStop();
        //remove the event listner
        firestoreRecyclerAdapter.stopListening();
    }

    @Override
    public void onResume() {
        super.onResume();
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
                usersReference.document(firebaseAuth.getCurrentUser().getUid())
                        .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen error", e);
                            return;
                        }

                        if (documentSnapshot.exists()){
                            final Andeqan andeqan = documentSnapshot.toObject(Andeqan.class);
                            final String uid = andeqan.getUid();

                            final String postId = databaseReference.push().getKey();

                            Comment comment = new Comment();
                            comment.setUid(uid);
                            comment.setCommentText(commentText);
                            comment.setPushId(mPostId);
                            comment.setPostId(postId);
                            commentsReference.document(postId).set(comment)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    final Timeline timeline = new Timeline();
                                    final long time = new Date().getTime();

                                    collectionsCollection.document(mPostId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
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
                                                timeline.setTimeStamp(time);
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

package com.cinggl.cinggl.home;

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

import com.cinggl.cinggl.Constants;
import com.cinggl.cinggl.R;
import com.cinggl.cinggl.models.Cingle;
import com.cinggl.cinggl.models.Cingulan;
import com.cinggl.cinggl.models.Relation;
import com.cinggl.cinggl.viewholders.CommentViewHolder;
import com.cinggl.cinggl.models.Comment;
import com.cinggl.cinggl.people.FollowerProfileActivity;
import com.cinggl.cinggl.profile.PersonalProfileActivity;
import com.cinggl.cinggl.ProportionalImageView;
import com.firebase.ui.common.ChangeEventType;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.firebase.ui.firestore.ObservableSnapshotArray;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class CommentsActivity extends AppCompatActivity implements View.OnClickListener {
    @Bind(R.id.sendCommentImageView)ImageView mSendCommentImageView;
    @Bind(R.id.commentEditText)EditText mCommentEditText;
    @Bind(R.id.commentsRecyclerView)RecyclerView mCommentsRecyclerView;
    @Bind(R.id.cingleImageView)ProportionalImageView mCingleImageView;
    @Bind(R.id.usernameTextView)TextView mAccountUsernameTextView;
    @Bind(R.id.creatorImageView)CircleImageView mUserProfileImageView;
    @Bind(R.id.saySomethingRelativeLayout)RelativeLayout mSaySomethingRelativeLayout;
    @Bind(R.id.cingleTitleTextView)TextView mCingleTitleTextView;
    @Bind(R.id.cingleTitleRelativeLayout)RelativeLayout mCingleTitleRelativeLayout;
    @Bind(R.id.commentCountTextView)TextView mCommentCountTextView;

    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private String mPostKey;
    //firebase
    //firestore
    private CollectionReference commentsReference;
    private CollectionReference cinglesReference;
    private Query commentQuery;
    private CollectionReference usersReference;
    private CollectionReference relationsReference;
    //adapters
    private FirestoreRecyclerAdapter firestoreRecyclerAdapter;
    private static final String EXTRA_POST_KEY = "post key";
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

            mPostKey = getIntent().getStringExtra(EXTRA_POST_KEY);
            if(mPostKey == null){
                throw new IllegalArgumentException("pass an EXTRA_POST_KEY");
            }

            //firestore
            cinglesReference = FirebaseFirestore.getInstance().collection(Constants.POSTS);
            commentsReference = FirebaseFirestore.getInstance().collection(Constants.COMMENTS);
            usersReference = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);
            relationsReference = FirebaseFirestore.getInstance().collection(Constants.RELATIONS);

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
        cinglesReference.document(mPostKey).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (documentSnapshot.exists()){
                    final Cingle cingle = documentSnapshot.toObject(Cingle.class);

                    final String uid = cingle.getUid();
                    final String image = cingle.getCingleImageUrl();
                    final String title = cingle.getTitle();


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

                    //set the title of the cingle
                    if (title.equals("")) {
                        mCingleTitleRelativeLayout.setVisibility(View.GONE);
                    } else {
                        mCingleTitleTextView.setText(title);
                    }

                    //set the cingle image
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
                                final Cingulan cingulan = documentSnapshot.toObject(Cingulan.class);
                                final String profileImage = cingulan.getProfileImage();
                                final String username = cingulan.getUsername();

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
        commentQuery = commentsReference.whereEqualTo("pushId", mPostKey);
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
                if (commentsReference.document(mPostKey) != null){
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
                            final Cingulan cingulan = documentSnapshot.toObject(Cingulan.class);
                            final String profileImage = cingulan.getProfileImage();
                            final String username = cingulan.getUsername();

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
                                    .collection(postKey).whereEqualTo("uid", firebaseAuth.getCurrentUser().getUid())
                                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                        @Override
                                        public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                                            if (processFollow){
                                                if (documentSnapshots.isEmpty()){
                                                    Relation follower = new Relation();
                                                    follower.setUid(firebaseAuth.getCurrentUser().getUid());
                                                    relationsReference.document("followers").collection(postKey)
                                                            .document(firebaseAuth.getCurrentUser().getUid()).set(follower)
                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {
                                                                    Relation following = new Relation();
                                                                    following.setUid(postKey);
                                                                    relationsReference.document("following").collection(firebaseAuth
                                                                            .getCurrentUser().getUid()).document(postKey).set(following);
                                                                }
                                                            });
                                                    processFollow = false;
                                                    holder.followButton.setText("Following");
                                                }else {
                                                    relationsReference.document("followers").collection(postKey)
                                                            .document(firebaseAuth.getCurrentUser().getUid()).delete()
                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {
                                                                    relationsReference.document("following").collection(firebaseAuth.getCurrentUser()
                                                                            .getUid()).document(postKey).delete();
                                                                }
                                                            });
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
                            final Cingulan cingulan = documentSnapshot.toObject(Cingulan.class);
                            final String uid = cingulan.getUid();

                            Comment comment = new Comment();
                            comment.setUid(uid);
                            comment.setCommentText(commentText);

                            DocumentReference pushRef = commentsReference.document(mPostKey);
                            final String pushId = pushRef.getId();
                            comment.setPushId(pushId);
                            pushRef.set(comment);
                            mCommentEditText.setText("");

                        }

                    }
                });

            }
        }

    }

}

package com.andeqa.andeqa.home;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
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
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.R;
import com.andeqa.andeqa.models.Andeqan;
import com.andeqa.andeqa.models.CollectionPost;
import com.andeqa.andeqa.models.Comment;
import com.andeqa.andeqa.models.Post;
import com.andeqa.andeqa.models.Timeline;
import com.andeqa.andeqa.profile.ProfileActivity;
import com.andeqa.andeqa.registration.SignInActivity;
import com.andeqa.andeqa.settings.PostSettingsFragment;
import com.andeqa.andeqa.utils.ProportionalImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Nullable;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class DeepLinkActivity extends AppCompatActivity {
    @Bind(R.id.usernameTextView)TextView mUsernameTextView;
    @Bind(R.id.postImageView)ProportionalImageView mPostImageView;
    @Bind(R.id.profileImageView)CircleImageView mProfileImageView;
    @Bind(R.id.titleTextView)TextView titleTextView;
    @Bind(R.id.viewsCountTextView)TextView mViewsCountTextView;
    @Bind(R.id.viewsLinearLayout)LinearLayout mViewsLinearLayout;
    @Bind(R.id.viewsImageView)ImageView mViewsImageView;
    @Bind(R.id.titleRelativeLayout)RelativeLayout mTitleRelativeLayout;
    @Bind(R.id.descriptionRelativeLayout)RelativeLayout mDescriptionRelativeLayout;
    @Bind(R.id.descriptionTextView)TextView mDescriptionTextView;
    @Bind(R.id.settingsRelativeLayout)RelativeLayout mSettingsRelativeLayout;
    @Bind(R.id.sendCommentImageView)ImageView mSendCommentImageView;
    @Bind(R.id.commentEditText)EditText mCommentEditText;
    @Bind(R.id.commentsImageView)ImageView mCommentImageView;
    @Bind(R.id.commentsCountTextView)TextView mCommentCountTextView;
    @Bind(R.id.commentsRecyclerView)RecyclerView mCommentsRecyclerView;
    @Bind(R.id.sendCommentsRelativeLayout)RelativeLayout mSendCommentRelativeLayout;


    //firestore reference
    private FirebaseFirestore firebaseFirestore;
    private CollectionReference postsCollections;
    private CollectionReference usersReference;
    private CollectionReference sellingCollection;
    private CollectionReference likesReference;
    private CollectionReference postWalletReference;
    private CollectionReference timelineCollection;
    private CollectionReference collectionsPosts;
    private CollectionReference marketCollections;
    private CollectionReference collectionsCollection;
    private CollectionReference commentsCollection;
    private CollectionReference postCollection;
    private Query likesQuery;
    //firebase references
    private DatabaseReference databaseReference;
    private DatabaseReference impressionReference;
    //firebase auth
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    //firestore adapter
    private FirestoreRecyclerAdapter firestoreRecyclerAdapter;
    //process likes
    private boolean processLikes = false;
    private boolean processDislikes = false;
    private static final double DEFAULT_PRICE = 1.5;
    private static final double GOLDEN_RATIO = 1.618;

    private static final String COLLECTION_ID = "collection id";
    private static final String EXTRA_POST_ID = "post id";
    private static final String EXTRA_USER_UID = "uid";
    private static final String TYPE = "type";
    private static final String EXTRA_URI = "uri";

    private static final String SOURCE = DeepLinkActivity.class.getSimpleName();
    private static final int MAX_WIDTH = 200;
    private static final int MAX_HEIGHT = 200;
    private static final String TAG = DeepLinkActivity.class.getSimpleName();
    private ProgressDialog progressDialog;
    private boolean showOnClick = false;
    private CommentsAdapter commentsAdapter;
    private static final int DEFAULT_COMMENT_LENGTH_LIMIT = 500;
    private static final int TOTAL_ITEMS = 25;
    private List<String> commentsIds = new ArrayList<>();
    private List<DocumentSnapshot> comments = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deep_link);
        ButterKnife.bind(this);

        firebaseAuth = FirebaseAuth.getInstance();

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationIcon(R.drawable.ic_arrow);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        postCollection = FirebaseFirestore.getInstance().collection(Constants.POSTS);
        usersReference = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);
        commentsCollection = FirebaseFirestore.getInstance().collection(Constants.COMMENTS);
        sellingCollection = FirebaseFirestore.getInstance().collection(Constants.SELLING);
        marketCollections = FirebaseFirestore.getInstance().collection(Constants.SELLING);
        collectionsCollection = FirebaseFirestore.getInstance().collection(Constants.COLLECTIONS);
        //firebase
        databaseReference = FirebaseDatabase.getInstance().getReference(Constants.RANDOM_PUSH_ID);
        likesReference = FirebaseFirestore.getInstance().collection(Constants.LIKES);
        postWalletReference = FirebaseFirestore.getInstance().collection(Constants.POST_WALLET);
        timelineCollection = FirebaseFirestore.getInstance().collection(Constants.TIMELINE);
      //firebase references
        impressionReference = FirebaseDatabase.getInstance().getReference(Constants.VIEWS);
        impressionReference.keepSynced(true);


    }

    @Override
    protected void onStart() {
        super.onStart();
        loadComments();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }


    private void handleLinks(){
        Uri data = null;
        if (getIntent() != null && getIntent().getData() != null) {
            data = getIntent().getData();
            Log.d("deeplink data",data.toString());

            postCollection.orderBy("post_id").whereEqualTo("deeplink", data.toString())
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot documentSnapshots, @Nullable FirebaseFirestoreException e) {

                            if (e != null) {
                                Log.w(TAG, "Listen error", e);
                                return;
                            }

                            if (!documentSnapshots.isEmpty()){

                                for (DocumentChange change : documentSnapshots.getDocumentChanges()){
                                    final Post post = change.getDocument().toObject(Post.class);
                                    final String postId = post.getPost_id();
                                    final String uid = post.getUser_id();
                                    final String collectionId = post.getCollection_id();
                                    final String type = post.getType();
                                    final String url = post.getUrl();

                                    //firestore
                                    if (type.equals("single") || type.equals("single_image_post")){
                                        collectionsPosts = FirebaseFirestore.getInstance().collection(Constants.COLLECTIONS_OF_POSTS)
                                                .document("singles").collection(collectionId);
                                    }else{
                                        collectionsPosts = FirebaseFirestore.getInstance().collection(Constants.COLLECTIONS_OF_POSTS)
                                                .document("collections").collection(collectionId);
                                    }

                                    if (post.getUrl() != null){
                                        //set the title of the single
                                        if (!TextUtils.isEmpty(post.getTitle())){
                                            mTitleRelativeLayout.setVisibility(View.GONE);
                                        }else {
                                            mTitleRelativeLayout.setVisibility(View.VISIBLE);
                                            titleTextView.setText(post.getTitle());
                                        }

                                        if (!TextUtils.isEmpty(post.getDescription())){
                                            final String [] strings = post.getDescription().split("");

                                            final int size = strings.length;

                                            if (size <= 150){
                                                mDescriptionRelativeLayout.setVisibility(View.VISIBLE);
                                                mDescriptionTextView.setText(post.getDescription());
                                            }else{

                                                mDescriptionRelativeLayout.setVisibility(View.VISIBLE);
                                                final String boldMore = "...";
                                                final String boldLess = "";
                                                String normalText = post.getDescription().substring(0, 149);
                                                mDescriptionTextView.setText(normalText + boldMore);
                                                mDescriptionRelativeLayout.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        if (showOnClick){
                                                            String normalText = post.getDescription();
                                                            mDescriptionTextView.setText(normalText + boldLess);
                                                            showOnClick = false;
                                                        }else {
                                                            String normalText = post.getDescription().substring(0, 149);
                                                            mDescriptionTextView.setText(normalText + boldMore);
                                                            showOnClick = true;
                                                        }
                                                    }
                                                });
                                            }
                                        }else {
                                            mDescriptionRelativeLayout.setVisibility(View.GONE);
                                        }

                                        Glide.with(getApplicationContext())
                                                .load(Uri.parse(url))
                                                .apply(new RequestOptions()
                                                        .placeholder(R.drawable.post_placeholder)
                                                        .diskCacheStrategy(DiskCacheStrategy.DATA))
                                                .into(mPostImageView);

                                        usersReference.document(uid).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                            @Override
                                            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                                                if (e != null) {
                                                    android.util.Log.w(TAG, "Listen error", e);
                                                    return;
                                                }

                                                if (documentSnapshot.exists()){
                                                    final Andeqan andeqan = documentSnapshot.toObject(Andeqan.class);
                                                    final String username = andeqan.getUsername();
                                                    final String profileImage = andeqan.getProfile_image();

                                                    mUsernameTextView.setText(username);
                                                    Glide.with(getApplicationContext())
                                                            .load(profileImage)
                                                            .apply(new RequestOptions()
                                                                    .placeholder(R.drawable.ic_user)
                                                                    .diskCacheStrategy(DiskCacheStrategy.DATA))
                                                            .into(mProfileImageView);
                                                }
                                            }
                                        });

                                    }else {
                                        collectionsPosts.document(postId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                            @Override
                                            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                                                if (e != null) {
                                                    android.util.Log.w(TAG, "Listen error", e);
                                                    return;
                                                }

                                                if (documentSnapshot.exists()){
                                                    final CollectionPost collectionPost = documentSnapshot.toObject(CollectionPost.class);
                                                    final String image = collectionPost.getImage();
                                                    final String uid = collectionPost.getUser_id();
                                                    final String title = collectionPost.getTitle();

                                                    //set the title of the single
                                                    if (title.equals("")){
                                                        mTitleRelativeLayout.setVisibility(View.GONE);
                                                    }else {
                                                        mTitleRelativeLayout.setVisibility(View.VISIBLE);
                                                        titleTextView.setText(title);
                                                    }

                                                    if (!TextUtils.isEmpty(collectionPost.getDescription())){
                                                        final String [] strings = collectionPost.getDescription().split("");

                                                        final int size = strings.length;

                                                        if (size <= 150){
                                                            mDescriptionRelativeLayout.setVisibility(View.VISIBLE);
                                                            mDescriptionTextView.setText(collectionPost.getDescription());
                                                        }else{

                                                            mDescriptionRelativeLayout.setVisibility(View.VISIBLE);
                                                            final String boldMore = "...";
                                                            final String boldLess = "";
                                                            String normalText = collectionPost.getDescription().substring(0, 149);
                                                            mDescriptionTextView.setText(normalText + boldMore);
                                                            mDescriptionRelativeLayout.setOnClickListener(new View.OnClickListener() {
                                                                @Override
                                                                public void onClick(View v) {
                                                                    if (showOnClick){
                                                                        String normalText = collectionPost.getDescription();
                                                                        mDescriptionTextView.setText(normalText + boldLess);
                                                                        showOnClick = false;
                                                                    }else {
                                                                        String normalText = collectionPost.getDescription().substring(0, 149);
                                                                        mDescriptionTextView.setText(normalText + boldMore);
                                                                        showOnClick = true;
                                                                    }
                                                                }
                                                            });
                                                        }
                                                    }else {
                                                        mDescriptionRelativeLayout.setVisibility(View.GONE);
                                                    }

                                                    Glide.with(getApplicationContext())
                                                            .load(Uri.parse(image))
                                                            .apply(new RequestOptions()
                                                                    .placeholder(R.drawable.post_placeholder)
                                                                    .diskCacheStrategy(DiskCacheStrategy.DATA))
                                                            .into(mPostImageView);

                                                    usersReference.document(uid).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                                        @Override
                                                        public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                                                            if (e != null) {
                                                                android.util.Log.w(TAG, "Listen error", e);
                                                                return;
                                                            }

                                                            if (documentSnapshot.exists()){
                                                                final Andeqan andeqan = documentSnapshot.toObject(Andeqan.class);
                                                                final String username = andeqan.getUsername();
                                                                final String profileImage = andeqan.getProfile_image();

                                                                mUsernameTextView.setText(username);
                                                                Glide.with(getApplicationContext())
                                                                        .load(profileImage)
                                                                        .apply(new RequestOptions()
                                                                                .placeholder(R.drawable.ic_user)
                                                                                .diskCacheStrategy(DiskCacheStrategy.DATA))
                                                                        .into(mProfileImageView);
                                                            }
                                                        }
                                                    });

                                                }
                                            }
                                        });
                                    }

                                    mCommentImageView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            mSendCommentImageView.setVisibility(View.VISIBLE);
                                        }
                                    });

                                    commentsCollection.document("post_ids").collection(postId)
                                            .orderBy("comment_id").whereEqualTo("post_id", postId)
                                            .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                                @Override
                                                public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                                                    if (e != null) {
                                                        android.util.Log.w(TAG, "Listen error", e);
                                                        return;
                                                    }

                                                    if (!documentSnapshots.isEmpty()){
                                                        final int commentsCount = documentSnapshots.size();
                                                        mCommentCountTextView.setText(commentsCount + "");
                                                    }else {
                                                        mCommentCountTextView.setText("0");

                                                    }

                                                }
                                            });

                                    commentsCollection.document("post_ids").collection(postId)
                                            .orderBy("time", Query.Direction.DESCENDING)
                                            .limit(TOTAL_ITEMS)
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
                                                    }

                                                }
                                            });

                                    mProfileImageView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            Intent intent = new Intent(DeepLinkActivity.this, ProfileActivity.class);
                                            intent.putExtra(DeepLinkActivity.EXTRA_USER_UID, uid);
                                            startActivity(intent);
                                        }
                                    });


                                    mPostImageView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Intent intent = new Intent(DeepLinkActivity.this, ImageViewActivity.class);
                                            intent.putExtra(DeepLinkActivity.EXTRA_POST_ID, postId);
                                            intent.putExtra(DeepLinkActivity.COLLECTION_ID, collectionId);
                                            intent.putExtra(DeepLinkActivity.TYPE, type);
                                            startActivity(intent);
                                        }
                                    });
//
                                    mSettingsRelativeLayout.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            final Uri bmpUri = getLocalBitmapUri(mPostImageView);
                                            if (bmpUri != null){
                                                Bundle bundle = new Bundle();
                                                bundle.putString(DeepLinkActivity.EXTRA_POST_ID, postId);
                                                bundle.putString(DeepLinkActivity.COLLECTION_ID, collectionId);
                                                bundle.putString(DeepLinkActivity.TYPE, type);
                                                bundle.putString(DeepLinkActivity.EXTRA_URI, bmpUri.toString());
                                                PostSettingsFragment postSettingsFragment = PostSettingsFragment.newInstance();
                                                postSettingsFragment.setArguments(bundle);
                                                postSettingsFragment.show(getSupportFragmentManager(), "share bottom fragment");
                                            }
                                        }
                                    });

                                    mSendCommentImageView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            final long time = new Date().getTime();
                                            final String uid = firebaseAuth.getCurrentUser().getUid();
                                            final String commentText = mCommentEditText.getText().toString().trim();
                                            if(!TextUtils.isEmpty(commentText)){
                                                final String commentId = databaseReference.push().getKey();

                                                Comment comment = new Comment();
                                                comment.setUser_id(uid);
                                                comment.setComment_text(commentText);
                                                comment.setPost_id(postId);
                                                comment.setComment_id(commentId);
                                                comment.setTime(time);
                                                commentsCollection.document("post_ids").collection(postId)
                                                        .document(commentId).set(comment);


                                                //record the comment on the timeline
                                                postsCollections.document(postId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
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
                                                            timeline.setPost_id(postId);
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
                                    });

                                    impressionReference.child("post_views").child(postId)
                                            .addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                    if (dataSnapshot.exists()){
                                                        final long size = dataSnapshot.getChildrenCount();
                                                        int childrenCount = (int) size;
                                                        mViewsCountTextView.setText(childrenCount + "");
                                                    }else {
                                                        mViewsCountTextView.setText("0");
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                                }
                                            });

                                }
                            }else {
                                Toast.makeText(DeepLinkActivity.this, "This post has been deleted",
                                        Toast.LENGTH_SHORT).show();
                                finish();
                            }

                        }
                    });


        }


    }



    private void setRecyclerView(){
        commentsAdapter = new CommentsAdapter(this);
        mCommentsRecyclerView.setAdapter(commentsAdapter);
        mCommentsRecyclerView.setHasFixedSize(false);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mCommentsRecyclerView.setLayoutManager(layoutManager);
    }

    private void loadComments(){
        comments.clear();
        setRecyclerView();
    }


    protected void onDocumentAdded(DocumentChange change) {
        commentsIds.add(change.getDocument().getId());
        comments.add(change.getDocument());
        commentsAdapter.setPostComments(comments);
        commentsAdapter.notifyItemInserted(comments.size() -1);
        commentsAdapter.getItemCount();

    }

    protected void onDocumentModified(DocumentChange change) {
        try {
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
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    protected void onDocumentRemoved(DocumentChange change) {
        try {
            comments.remove(change.getOldIndex());
            commentsAdapter.notifyItemRemoved(change.getOldIndex());
            commentsAdapter.notifyItemRangeChanged(0, comments.size());
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //region listeners
    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    /**show delete dialog*/
    public void deletePostDialog(){
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Deleting ...");
        progressDialog.setCancelable(false);
    }


    public Uri getLocalBitmapUri(ImageView imageView) {
        // Extract Bitmap from ImageView drawable
        Drawable drawable = imageView.getDrawable();
        Bitmap bmp = null;
        if (drawable instanceof BitmapDrawable){
            bmp = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        } else {
            return null;
        }
        // Store image to default external storage directory
        Uri bmpUri = null;
        try {
            // Use methods on Context to access package-specific directories on external storage.
            // This way, you don't need to request external read/write permission.
            // See https://youtu.be/5xVh-7ywKpE?t=25m25s
            File file =  new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "share_image_" + System.currentTimeMillis() + ".png");
            FileOutputStream out = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.close();
            // **Warning:** This will fail for API >= 24, use a FileProvider as shown below instead.
            bmpUri = Uri.fromFile(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bmpUri;
    }
}

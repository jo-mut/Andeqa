package com.andeqa.andeqa.profile;

import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.R;
import com.andeqa.andeqa.models.Andeqan;
import com.andeqa.andeqa.models.Post;
import com.andeqa.andeqa.utils.EndlesssStaggeredRecyclerOnScrollListener;
import com.andeqa.andeqa.utils.ItemOffsetDecoration;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ProfilePostsActivity extends AppCompatActivity {
    @Bind(R.id.postsRecyclerView)RecyclerView mPostssRecyclerView;
    @Bind(R.id.toolbar)Toolbar toolbar;
    private static final String TAG = ProfilePostsActivity.class.getSimpleName();

    //firestore reference
    private CollectionReference postsCollection;
    private CollectionReference collectionsPosts;
    private Query profilePostsQuery;
    private CollectionReference usersReference;
    //firebase auth
    private FirebaseAuth firebaseAuth;
    //firestore adapters
    private ProfilePostsAdapter profilePostsAdapter;
    private static final String KEY_LAYOUT_POSITION = "layout pooition";
    private Parcelable recyclerViewState;
    private  static final int MAX_WIDTH = 400;
    private static final int MAX_HEIGHT = 400;
    private int TOTAL_ITEMS = 10;
    private StaggeredGridLayoutManager layoutManager;
    private static final String EXTRA_USER_UID = "uid";

    private String collectionId;
    private String mUid;
    private String mSource;
    private static final String COLLECTION_ID = "collection id";
    private List<String> mSnapshotsIds = new ArrayList<>();
    private List<DocumentSnapshot> documentSnapshots = new ArrayList<>();
    int spanCount = 2; // 3 columns
    int spacing = 10; // 50px
    boolean includeEdge = false;
    private ItemOffsetDecoration itemOffsetDecoration;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_posts);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        toolbar.setNavigationIcon(R.drawable.ic_arrow);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        //FIREBASE AUTH
        firebaseAuth = FirebaseAuth.getInstance();
        mUid = getIntent().getStringExtra(EXTRA_USER_UID);

        postsCollection = FirebaseFirestore.getInstance().collection(Constants.POSTS);
        profilePostsQuery = postsCollection.orderBy("time", Query.Direction.DESCENDING)
                .whereEqualTo("user_id", mUid).limit(TOTAL_ITEMS);
        collectionsPosts = FirebaseFirestore.getInstance().collection(Constants.COLLECTIONS_OF_POSTS);
        usersReference = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);
        mPostssRecyclerView.addOnScrollListener(new EndlesssStaggeredRecyclerOnScrollListener() {
            @Override
            public void onLoadMore() {
                setNextPosts();
            }
        });

        setToolbarTitle();

    }


    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStart() {
        super.onStart();
        loadData();
        mPostssRecyclerView.addItemDecoration(itemOffsetDecoration);
    }

    @Override
    public void onStop() {
        super.onStop();
        mPostssRecyclerView.removeItemDecoration(itemOffsetDecoration);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void setToolbarTitle(){
        usersReference.document(mUid).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {

                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (documentSnapshot.exists()){
                    Andeqan andeqan = documentSnapshot.toObject(Andeqan.class);
                    final String username = andeqan.getUsername();
                    toolbar.setTitle(username + "'s" + " posts");

                }
            }
        });
    }

    private void loadData(){
        documentSnapshots.clear();
        setRecyclerView();
        setPosts();
    }

    private void setRecyclerView(){
        // RecyclerView
        profilePostsAdapter = new ProfilePostsAdapter(this);
        mPostssRecyclerView.setAdapter(profilePostsAdapter);
        mPostssRecyclerView.setHasFixedSize(false);
        layoutManager = new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL);
        itemOffsetDecoration = new ItemOffsetDecoration(this, R.dimen.item_off_set);
        mPostssRecyclerView.setLayoutManager(layoutManager);

    }

    private void setPosts(){
        profilePostsQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (!documentSnapshots.isEmpty()) {
                    for (final DocumentChange change : documentSnapshots.getDocumentChanges()) {
                        switch (change.getType()) {
                            case ADDED:
                                Post post = change.getDocument().toObject(Post.class);
                                String type = post.getType();
                                if (!type.equals("single_video_post") || !type.equals("collection_video_post")){
                                    onDocumentAdded(change);
                                }
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

    }

    private void setNextPosts(){
        // Get the last visible document
        final int snapshotSize = profilePostsAdapter.getItemCount();

        if (snapshotSize != 0){
            DocumentSnapshot lastVisible = profilePostsAdapter.getSnapshot(snapshotSize - 1);
            //retrieve the first bacth of posts
            Query nextSinglesQuery = postsCollection.orderBy("time", Query.Direction.DESCENDING)
                    .whereEqualTo("user_id", mUid).startAfter(lastVisible)
                    .limit(TOTAL_ITEMS);
            nextSinglesQuery.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot documentSnapshots) {
                    if (!documentSnapshots.isEmpty()){
                        //retrieve the first bacth of posts
                        for (final DocumentChange change : documentSnapshots.getDocumentChanges()) {
                            switch (change.getType()) {
                                case ADDED:
                                    Post post = change.getDocument().toObject(Post.class);
                                    String type = post.getType();
                                    if (!type.equals("single_video_post") || !type.equals("collection_video_post")){
                                        onDocumentAdded(change);
                                    }
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
        }

    }

    protected void onDocumentAdded(DocumentChange change) {
        mSnapshotsIds.add(change.getDocument().getId());
        documentSnapshots.add(change.getDocument());
        profilePostsAdapter.setCollectionPosts(documentSnapshots);
        profilePostsAdapter.notifyItemInserted(documentSnapshots.size() -1);
        profilePostsAdapter.getItemCount();

    }

    protected void onDocumentModified(DocumentChange change) {
        if (change.getOldIndex() == change.getNewIndex()) {
            // Item changed but remained in same position
            documentSnapshots.set(change.getOldIndex(), change.getDocument());
            profilePostsAdapter.notifyItemChanged(change.getOldIndex());
        } else {
            // Item changed and changed position
            documentSnapshots.remove(change.getOldIndex());
            documentSnapshots.add(change.getNewIndex(), change.getDocument());
            profilePostsAdapter.notifyItemRangeChanged(0, documentSnapshots.size());
        }
    }

    protected void onDocumentRemoved(DocumentChange change) {
        try{
            documentSnapshots.remove(change.getOldIndex());
            profilePostsAdapter.notifyItemRemoved(change.getOldIndex());
            profilePostsAdapter.notifyItemRangeChanged(0, documentSnapshots.size());
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    @Override
    public void onResume() {
        super.onResume();

    }

}

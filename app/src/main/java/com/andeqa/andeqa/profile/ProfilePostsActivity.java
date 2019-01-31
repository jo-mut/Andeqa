package com.andeqa.andeqa.profile;

import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintSet;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.R;
import com.andeqa.andeqa.home.HomeFragment;
import com.andeqa.andeqa.home.PostsAdapter;
import com.andeqa.andeqa.models.Andeqan;
import com.andeqa.andeqa.utils.BottomReachedListener;
import com.andeqa.andeqa.utils.EndlessStaggeredScrollListener;
import com.andeqa.andeqa.utils.ItemOffsetDecoration;
import com.google.android.gms.tasks.OnSuccessListener;
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
import java.util.List;

import javax.annotation.Nullable;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ProfilePostsActivity extends AppCompatActivity {
    @Bind(R.id.postsRecyclerView)RecyclerView mPostsRecyclerView;
    @Bind(R.id.toolbar)Toolbar toolbar;
    @Bind(R.id.progressBar)ProgressBar mProgressBar;
    @Bind(R.id.progressRelativeLayout)RelativeLayout mProgressRelativeLayout;
    private static final String TAG = HomeFragment.class.getSimpleName();
    //firestore reference
    private CollectionReference postsCollection;
    private CollectionReference usersReference;
    private DatabaseReference impressionReference;
    //lists
    private List<DocumentSnapshot> snapshots = new ArrayList<>();
    //layouts
    private ConstraintSet constraintSet;
    private ItemOffsetDecoration itemOffsetDecoration;
    private StaggeredGridLayoutManager layoutManager;
    private PostsAdapter postsAdapter;
    //strings
    private static final String EXTRA_POST_ID = "post id";
    private static final String COLLECTION_ID = "collection id";
    private static final String EXTRA_USER_UID =  "uid";
    private String mUid;
    private int INITIAL_ITEMS = 20;
    private int TOTAL_ITEMS = 10;
    //firebase auth
    private FirebaseAuth firebaseAuth;


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

        initReferences();

        setToolbarTitle();

    }


    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStart() {
        super.onStart();
        snapshots.clear();
        setRecyclerView();
        getProfilePosts();
        mPostsRecyclerView.addItemDecoration(itemOffsetDecoration);

        mPostsRecyclerView.addOnScrollListener(new EndlessStaggeredScrollListener() {
            @Override
            public void onLoadMore() {
                getNextProfilePosts();
            }
        });

    }

    @Override
    public void onStop() {
        super.onStop();
        mPostsRecyclerView.removeItemDecoration(itemOffsetDecoration);
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

    private void initReferences(){
        firebaseAuth = FirebaseAuth.getInstance();
        mUid = getIntent().getStringExtra(EXTRA_USER_UID);
        postsCollection = FirebaseFirestore.getInstance().collection(Constants.POSTS);
        usersReference = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);
        usersReference = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);
        usersReference = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);
        //firebase database references
        impressionReference = FirebaseDatabase.getInstance().getReference(Constants.VIEWS);
        impressionReference.keepSynced(true);
    }


    private void getProfilePosts(){
        Query query =  postsCollection.orderBy("time", Query.Direction.DESCENDING)
                .whereEqualTo("user_id", mUid)
                .limit(INITIAL_ITEMS);

        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(final @javax.annotation.Nullable QuerySnapshot documentSnapshots,
                                @javax.annotation.Nullable FirebaseFirestoreException e) {

                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (!documentSnapshots.isEmpty()){
                    postsAdapter.setBottomReachedListener(new BottomReachedListener() {
                        @Override
                        public void onBottomReached(int position) {
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    for (DocumentSnapshot snapshot: documentSnapshots){
                                        snapshots.add(snapshot);
                                        postsAdapter.notifyItemInserted(snapshots.size() - 1);
                                    }
                                }
                            }, 4000);
                        }
                    });

                }
            }
        });

    }

    private void setRecyclerView() {
        postsAdapter = new PostsAdapter(this, snapshots);
        postsAdapter.setHasStableIds(true);
        mPostsRecyclerView.setHasFixedSize(false);
        mPostsRecyclerView.setAdapter(postsAdapter);
        layoutManager = new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL);
        itemOffsetDecoration = new ItemOffsetDecoration(this, R.dimen.item_off_set);
        mPostsRecyclerView.setLayoutManager(layoutManager);
    }

    private void getNextProfilePosts(){
        DocumentSnapshot last = snapshots.get(snapshots.size() - 1);
        Query query =  postsCollection.orderBy("time", Query.Direction.DESCENDING)
                .whereEqualTo("user_id", mUid)
                .startAfter(last).limit(TOTAL_ITEMS);
        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot documentSnapshots) {

                if (!documentSnapshots.isEmpty()){
                    for (DocumentSnapshot snapshot: documentSnapshots){
                        snapshots.add(snapshot);
                        postsAdapter.notifyItemInserted(snapshots.size() - 1);
                    }

                    mProgressRelativeLayout.setVisibility(View.GONE);
                }
            }
        });

    }

    private void showToast(@NonNull String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

}

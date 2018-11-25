package com.andeqa.andeqa.profile;

import android.arch.paging.PagedList;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.R;
import com.andeqa.andeqa.collections.CollectionPostsActivity;
import com.andeqa.andeqa.collections.ExploreCollectionViewHolder;
import com.andeqa.andeqa.collections.ExploreCollectionsActivity;
import com.andeqa.andeqa.models.Andeqan;
import com.andeqa.andeqa.models.Collection;
import com.andeqa.andeqa.utils.ItemOffsetDecoration;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.firebase.ui.firestore.paging.LoadingState;
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

import javax.annotation.Nullable;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ProfileCollectionsActivity extends AppCompatActivity{
    @Bind(R.id.collectionsRecyclerView)RecyclerView mCollectionsRecyclerView;
    @Bind(R.id.toolbar)Toolbar mToolbar;
    private static final String TAG = ProfileCollectionsActivity.class.getSimpleName();

    //firestore reference
    private CollectionReference collectionsCollection;
    private CollectionReference usersCollection;
    private Query collectionsQuery;
    private CollectionReference followingCollection;
    private Query postCountQuery;
    //firebase auth
    private FirebaseAuth firebaseAuth;
    //layout
    private StaggeredGridLayoutManager layoutManager;
    private ItemOffsetDecoration itemOffsetDecoration;
    // strings
    private static final String COLLECTION_ID = "collection id";
    private static final String EXTRA_USER_UID = "uid";
    private String mUid;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_collections);
        ButterKnife.bind(this);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationIcon(R.drawable.ic_arrow);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        //get intents extras
        getIntents();
        // init firebase references and auth
        initFirebase();
        // set up the adapters
        setUpAdapter();


    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStart() {
        super.onStart();
        mCollectionsRecyclerView.addItemDecoration(itemOffsetDecoration);

    }

    @Override
    public void onStop() {
        super.onStop();
        mCollectionsRecyclerView.removeItemDecoration(itemOffsetDecoration);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void initFirebase() {
        firebaseAuth = FirebaseAuth.getInstance();
        collectionsCollection = FirebaseFirestore.getInstance().collection(Constants.COLLECTIONS);
        collectionsQuery = collectionsCollection.orderBy("time", Query.Direction.DESCENDING);
        usersCollection = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);
        usersCollection.document(mUid).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {

                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (documentSnapshot.exists()){
                    Andeqan andeqan = documentSnapshot.toObject(Andeqan.class);
                    final String username = andeqan.getUsername();
                    mToolbar.setTitle(username + "'s" + " collections");

                }
            }
        });
    }

    private void getIntents() {
        mUid = getIntent().getStringExtra(EXTRA_USER_UID);
    }


    @Override
    public void onResume() {
        super.onResume();
    }


    private void setUpAdapter() {
        collectionsQuery.whereEqualTo("user_id", mUid);
        PagedList.Config config = new PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPrefetchDistance(10)
                .setPageSize(20)
                .build();

        FirestorePagingOptions<Collection> options = new FirestorePagingOptions.Builder<Collection>()
                .setLifecycleOwner(this)
                .setQuery(collectionsQuery, config, Collection.class)
                .build();

        FirestorePagingAdapter<Collection, ExploreCollectionViewHolder> pagingAdapter
                = new FirestorePagingAdapter<Collection, ExploreCollectionViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ExploreCollectionViewHolder holder, int position, @NonNull Collection model) {
                final Collection collection = model;
                final String collectionId = collection.getCollection_id();
                final String userId = collection.getUser_id();

                firebaseAuth = FirebaseAuth.getInstance();
                collectionsCollection = FirebaseFirestore.getInstance().collection(Constants.COLLECTIONS_OF_POSTS);
                postCountQuery = collectionsCollection.document("collections").collection(collectionId)
                        .orderBy("collection_id");
                followingCollection = FirebaseFirestore.getInstance().collection(Constants.COLLECTION_RELATIONS);
                usersCollection = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);

                Glide.with(getApplicationContext())
                        .asBitmap()
                        .load(collection.getImage())
                        .apply(new RequestOptions()
                                .placeholder(R.drawable.post_placeholder)
                                .diskCacheStrategy(DiskCacheStrategy.DATA))
                        .into(holder.mCollectionCoverImageView);

                if (!TextUtils.isEmpty(collection.getName())){
                    holder.mCollectionNameTextView.setText(collection.getName());
                }else {
                    holder.mCollectionNameTextView.setVisibility(View.GONE);
                }

                if (!TextUtils.isEmpty(collection.getNote())){
                    holder.mCollectionsNoteTextView.setVisibility(View.VISIBLE);
                    //prevent collection note from overlapping other layouts
                    final String [] strings = collection.getNote().split("");

                    final int size = strings.length;

                    if (size <= 45){
                        //setence will not have read more
                        holder.mCollectionsNoteTextView.setText(collection.getNote());
                    }else {
                        holder.mCollectionsNoteTextView.setText(collection.getNote().substring(0, 44) + "...");
                    }
                }else {
                    holder.mCollectionsNoteTextView.setVisibility(View.GONE);
                }


                holder.mCollectionsLinearLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getApplicationContext(), CollectionPostsActivity.class);
                        intent.putExtra(ProfileCollectionsActivity.COLLECTION_ID, collectionId);
                        intent.putExtra(ProfileCollectionsActivity.EXTRA_USER_UID, userId);
                        startActivity(intent);
                    }
                });



                /**follow or un follow collection*/
                if (!userId.equals(firebaseAuth.getCurrentUser().getUid())){
//                    holder.followButton.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            processFollow = true;
//                            followingCollection.document("following")
//                                    .collection(collectionId)
//                                    .whereEqualTo("following_id", firebaseAuth.getCurrentUser().getUid())
//                                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
//                                        @Override
//                                        public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
//
//                                            if (e != null) {
//                                                Log.w(TAG, "Listen error", e);
//                                                return;
//                                            }
//
//                                            if (processFollow){
//                                                if (documentSnapshots.isEmpty()){
//                                                    final Relation following = new Relation();
//                                                    following.setFollowing_id(firebaseAuth.getCurrentUser().getUid());
//                                                    following.setFollowed_id(collectionId);
//                                                    following.setType("followed_collection");
//                                                    following.setTime(System.currentTimeMillis());
//                                                    followingCollection.document("following")
//                                                            .collection(collectionId)
//                                                            .document(firebaseAuth.getCurrentUser().getUid()).set(following);
//                                                    processFollow = false;
//                                                }else {
//                                                    followingCollection.document("following")
//                                                            .collection(collectionId)
//                                                            .document(firebaseAuth.getCurrentUser().getUid()).delete();
//
//                                                    processFollow = false;
//                                                }
//                                            }
//                                        }
//                                    });
//                        }
//                    });
                }
            }

            @NonNull
            @Override
            public ExploreCollectionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.layout_explore_posts, parent, false);
                return new ExploreCollectionViewHolder(view);
            }


            @Override
            public int getItemViewType(int position) {
                return super.getItemViewType(position);
            }

            @Override
            public void setHasStableIds(boolean hasStableIds) {
                super.setHasStableIds(hasStableIds);
            }


            @Override
            public long getItemId(int position) {
                return super.getItemId(position);
            }

            @Override
            protected void onLoadingStateChanged(@NonNull LoadingState state) {
                switch (state) {
                    case LOADING_INITIAL:
                    case LOADING_MORE:
                        break;
                    case LOADED:
                        break;
                    case FINISHED:
                        showToast("Reached end of data set.");
                        break;
                    case ERROR:
                        showToast("An error occurred.");
                        retry();
                        break;
                }
            }
        };

        pagingAdapter.setHasStableIds(true);
        mCollectionsRecyclerView.setAdapter(pagingAdapter);
        mCollectionsRecyclerView.setHasFixedSize(false);
        layoutManager = new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL);
        itemOffsetDecoration = new ItemOffsetDecoration(this, R.dimen.item_off_set);
        mCollectionsRecyclerView.setLayoutManager(layoutManager);
        ViewCompat.setNestedScrollingEnabled(mCollectionsRecyclerView,false);

    }

    private void showToast(@NonNull String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

}

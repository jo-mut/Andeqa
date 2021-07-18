package com.andeka.andeka.search;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintSet;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.andeka.andeka.Constants;
import com.andeka.andeka.R;
import com.andeka.andeka.collections.CollectionPostsActivity;
import com.andeka.andeka.more.MoreFragment;
import com.andeka.andeka.home.PhotoPostViewHolder;
import com.andeka.andeka.post_detail.PostDetailActivity;
import com.andeka.andeka.impressions.ImpressionTracker;
import com.andeka.andeka.models.Andeqan;
import com.andeka.andeka.models.Collection;
import com.andeka.andeka.models.Post;
import com.andeka.andeka.models.QueryOptions;
import com.andeka.andeka.profile.ProfileActivity;
import com.andeka.andeka.utils.ItemOffsetDecoration;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;

import butterknife.Bind;
import butterknife.ButterKnife;

public class SearchPostsActivity extends AppCompatActivity {
    @Bind(R.id.searchPostsRecyclerView)RecyclerView mSearchPeopleRecyclerView;
    @Bind(R.id.toolbar)Toolbar toolbar;
    private static final String TAG = MoreFragment.class.getSimpleName();
    //firestore reference
    private CollectionReference queryOptionsReference;
    //adapters
    private StaggeredGridLayoutManager layoutManager;
    private int TOTAL_ITEMS = 10;
    private List<String> postsIds = new ArrayList<>();
    private List<DocumentSnapshot> documentSnapshots = new ArrayList<>();
    private ItemOffsetDecoration itemOffsetDecoration;
    private SearchView searchView;
    private FirebaseAuth firebaseAuth;
    private  SearchPostsAdapter searchPostsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_posts);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!searchView.isIconified()) {
                    searchView.setIconified(true);
                    return;
                }
                finish();
            }
        });
        firebaseAuth = FirebaseAuth.getInstance();
        //firestore
        queryOptionsReference = FirebaseFirestore.getInstance().collection(Constants.QUERY_OPTIONS);

    }


    @Override
    public void onStart() {
        super.onStart();
        documentSnapshots.clear();
        setRecyclerView();
        mSearchPeopleRecyclerView.addItemDecoration(itemOffsetDecoration);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_search, menu);
        searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchPostsAdapter.cleanUp();
                documentSnapshots.clear();
                searchPosts(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchPostsAdapter.cleanUp();
                documentSnapshots.clear();
                searchPosts(newText);
                return false;
            }
        });

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }


    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }


    @Override
    public void onStop() {
        super.onStop();
        mSearchPeopleRecyclerView.removeItemDecoration(itemOffsetDecoration);
    }


    private void setRecyclerView(){
        searchPostsAdapter = new SearchPostsAdapter(SearchPostsActivity.this);
        searchPostsAdapter.setHasStableIds(true);
        mSearchPeopleRecyclerView.setHasFixedSize(false);
        searchPostsAdapter.setSearchedPosts(documentSnapshots);
        mSearchPeopleRecyclerView.setAdapter(searchPostsAdapter);
        layoutManager = new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL);
        itemOffsetDecoration = new ItemOffsetDecoration(this, R.dimen.item_off_set);
        mSearchPeopleRecyclerView.setLayoutManager(layoutManager);

    }

    private void searchPosts(final String word){
        if (word.isEmpty()){
            searchPostsAdapter.cleanUp();
        }else {
            queryOptionsReference.whereEqualTo("type", "post")
                    .whereArrayContains("one", word)
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@javax.annotation.Nullable QuerySnapshot documentSnapshots,
                                            @javax.annotation.Nullable FirebaseFirestoreException e) {
                            if (e != null) {
                                Log.w(TAG, "Listen error", e);
                                return;
                            }

                            if (!documentSnapshots.isEmpty()){
                                for (DocumentChange documentChange : documentSnapshots.getDocumentChanges()){
                                    switch (documentChange.getType()) {
                                        case ADDED:
                                            onDocumentAdded(documentChange);
                                            break;
                                        case MODIFIED:
                                            onDocumentModified(documentChange);
                                            break;
                                        case REMOVED:
                                            onDocumentRemoved(documentChange);
                                            break;

                                    }
                                }

                            }
                        }
                    });

            queryOptionsReference.whereEqualTo("type", "post")
                    .whereArrayContains("two", word)
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@javax.annotation.Nullable QuerySnapshot documentSnapshots,
                                            @javax.annotation.Nullable FirebaseFirestoreException e) {
                            if (e != null) {
                                Log.w(TAG, "Listen error", e);
                                return;
                            }

                            if (!documentSnapshots.isEmpty()){
                                for (DocumentChange documentChange : documentSnapshots.getDocumentChanges()){
                                    switch (documentChange.getType()) {
                                        case ADDED:
                                            onDocumentAdded(documentChange);
                                            break;
                                        case MODIFIED:
                                            onDocumentModified(documentChange);
                                            break;
                                        case REMOVED:
                                            onDocumentRemoved(documentChange);
                                            break;

                                    }
                                }

                                Log.d("two is pressent", documentSnapshots.size() + "");

                            }
                        }
                    });

        }
    }

    protected void onDocumentAdded(DocumentChange change) {
        postsIds.add(change.getDocument().getId());
        documentSnapshots.add(change.getDocument());
        searchPostsAdapter.notifyItemInserted(documentSnapshots.size() - 1);
        searchPostsAdapter.getItemCount();
    }

    protected void onDocumentModified(DocumentChange change) {
        try {
            if (change.getOldIndex() == change.getNewIndex()) {
                // Item changed but remained in same position
                documentSnapshots.set(change.getOldIndex(), change.getDocument());
                searchPostsAdapter.notifyItemChanged(change.getOldIndex());
            } else {
                // Item changed and changed position
                documentSnapshots.remove(change.getOldIndex());
                documentSnapshots.add(change.getNewIndex(), change.getDocument());
                searchPostsAdapter.notifyItemRangeChanged(0, documentSnapshots.size());
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    protected void onDocumentRemoved(DocumentChange change) {
        try {
            documentSnapshots.remove(change.getOldIndex());
            searchPostsAdapter.notifyItemRemoved(change.getOldIndex());
            searchPostsAdapter.notifyItemRangeChanged(0, documentSnapshots.size());
        }catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static class SearchPostsAdapter extends RecyclerView.Adapter<PhotoPostViewHolder>{
        private List<DocumentSnapshot> documentSnapshots = new ArrayList<>();
        private Context mContext;
        private CollectionReference followingCollection;
        private CollectionReference usersCollection;
        private CollectionReference roomsCollection;
        private DatabaseReference databaseReference;
        private FirebaseAuth firebaseAuth;
        //firestore reference
        private CollectionReference queryParamsCollection;
        private CollectionReference collectionsPosts;
        private com.google.firebase.firestore.Query commentsCountQuery;
        private CollectionReference usersReference;
        private CollectionReference commentsReference;
        private CollectionReference postsCollectionsReference;
        private CollectionReference likesReference;
        private DatabaseReference impressionReference;
        private CollectionReference timelineCollection;
        private static final String EXTRA_USER_UID = "uid";
        private static final String EXTRA_ROOM_UID = "roomId";
        private static final int PEOPLE=1;
        private static final int POST =2;
        private static final int COLLECTION =3;
        private static final int EMPTY =3;
        private String roomId;
        private static final String SEARCH_KEY_WORD = "search word";
        private static final String POST_HEIGHT = "height";
        private static final String POST_WIDTH = "width";
        private static final String EXTRA_POST_ID = "post id";
        private static final String COLLECTION_ID = "collection id";
        private static final String VIDEO = "video";
        private static final String TYPE = "type";
        private ConstraintSet constraintSet;
        private ImpressionTracker impressionTracker;
        private final WeakHashMap<View, Integer> mViewPositionMap = new WeakHashMap<>();


        public SearchPostsAdapter(Context mContext) {
            this.mContext = mContext;
        }

        public void setSearchedPosts(List<DocumentSnapshot> posts){
            this.documentSnapshots = posts;
            notifyDataSetChanged();
            initReferences();
        }

        public DocumentSnapshot getSnapshot(int index) {
            return documentSnapshots.get(index);
        }

        private void initReferences(){
            firebaseAuth = FirebaseAuth.getInstance();
            if (firebaseAuth.getCurrentUser() != null){
                postsCollectionsReference = FirebaseFirestore.getInstance().collection(Constants.POSTS);
                usersReference = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);
                timelineCollection = FirebaseFirestore.getInstance().collection(Constants.TIMELINE);
                collectionsPosts = FirebaseFirestore.getInstance().collection(Constants.COLLECTIONS);
                //firebase
                databaseReference = FirebaseDatabase.getInstance().getReference(Constants.RANDOM_PUSH_ID);
                //document reference
                commentsReference  = FirebaseFirestore.getInstance().collection(Constants.COMMENTS);
                commentsCountQuery= commentsReference;
                likesReference = FirebaseFirestore.getInstance().collection(Constants.LIKES);
                //firebase database references
                impressionReference = FirebaseDatabase.getInstance().getReference(Constants.VIEWS);
                impressionReference.keepSynced(true);
            }

        }

        @NonNull
        @Override
        public PhotoPostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.layout_explore_posts, parent, false);
            return new PhotoPostViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final PhotoPostViewHolder holder, int position) {
            QueryOptions queryOptions = getSnapshot(holder.getAdapterPosition()).toObject(QueryOptions.class);
            final String optionId = queryOptions.getOption_id();

            Log.d("option id", optionId);

            postsCollectionsReference.document(optionId)
                    .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(@javax.annotation.Nullable DocumentSnapshot documentSnapshot,
                                            @javax.annotation.Nullable FirebaseFirestoreException e) {

                            if (e != null) {
                                Log.w(TAG, "Listen error", e);
                                return;
                            }

                            if (documentSnapshot.exists()){
                                final Post post = documentSnapshot.toObject(Post.class);
                                final String postId = post.getPost_id();
                                final String uid = post.getUser_id();
                                final String collectionId = post.getCollection_id();
                                final String type = post.getType();

                                Log.d("post id", postId);

                                if (post.getHeight() != null && post.getWidth() != null){
                                    final float width = (float) Integer.parseInt(post.getWidth());
                                    final float height = (float) Integer.parseInt(post.getHeight());
                                    float ratio = height/width;

                                    constraintSet = new ConstraintSet();
                                    constraintSet.clone(holder.postConstraintLayout);
                                    constraintSet.setDimensionRatio(holder.postImageView.getId(), "H," + ratio);
                                    holder.postImageView.setImageResource(R.drawable.post_placeholder);
                                    constraintSet.applyTo(holder.postConstraintLayout);

                                }else {
                                    constraintSet = new ConstraintSet();
                                    constraintSet.clone(holder.postConstraintLayout);
                                    constraintSet.setDimensionRatio(holder.postImageView.getId(), "H," + 1);
                                    holder.postImageView.setImageResource(R.drawable.post_placeholder);
                                    constraintSet.applyTo(holder.postConstraintLayout);

                                }

//                        //calculate view visibility and add visible views to impression tracker
//                        mViewPositionMap.put(holder.itemView, position);
//                        impressionTracker.addView(holder.itemView, 100, postId);

                                Glide.with(mContext.getApplicationContext())
                                        .load(post.getUrl())
                                        .apply(new RequestOptions()
                                                .placeholder(R.drawable.post_placeholder)
                                                .diskCacheStrategy(DiskCacheStrategy.DATA))
                                        .into(holder.postImageView);

                                if (!TextUtils.isEmpty(post.getTitle())){
                                    holder.captionLinearLayout.setVisibility(View.VISIBLE);
                                    holder.titleTextView.setText(post.getTitle());
                                    holder.titleRelativeLayout.setVisibility(View.VISIBLE);
                                }else {
                                    holder.titleRelativeLayout.setVisibility(View.GONE);
                                }

                                if (!TextUtils.isEmpty(post.getDescription())){
                                    //prevent collection note from overlapping other layouts
                                    final String [] strings = post.getDescription().split("");
                                    final int size = strings.length;
                                    if (size <= 50){
                                        holder.captionLinearLayout.setVisibility(View.VISIBLE);
                                        holder.descriptionRelativeLayout.setVisibility(View.VISIBLE);
                                        holder.descriptionTextView.setText(post.getDescription());
                                    }else{
                                        holder.captionLinearLayout.setVisibility(View.VISIBLE);
                                        holder.descriptionRelativeLayout.setVisibility(View.VISIBLE);
                                        final String boldMore = "...";
                                        String normalText = post.getDescription().substring(0, 49);
                                        holder.descriptionTextView.setText(normalText + boldMore);
                                    }
                                }else {
                                    holder.captionLinearLayout.setVisibility(View.GONE);
                                }

                                if (post.getWidth() != null && post.getHeight() != null){
                                    holder.postImageView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            Intent intent =  new Intent(mContext, PostDetailActivity.class);
                                            intent.putExtra(SearchPostsAdapter.EXTRA_POST_ID, postId);
                                            intent.putExtra(SearchPostsAdapter.COLLECTION_ID, collectionId);
                                            intent.putExtra(SearchPostsAdapter.EXTRA_USER_UID, uid);
                                            intent.putExtra(SearchPostsAdapter.TYPE, type);
                                            intent.putExtra(SearchPostsAdapter.POST_HEIGHT, post.getHeight());
                                            intent.putExtra(SearchPostsAdapter.POST_WIDTH, post.getWidth());
                                            mContext.startActivity(intent);
                                        }
                                    });
                                }else {
                                    holder.postImageView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            Intent intent =  new Intent(mContext, PostDetailActivity.class);
                                            intent.putExtra(SearchPostsAdapter.EXTRA_POST_ID, postId);
                                            intent.putExtra(SearchPostsAdapter.COLLECTION_ID, collectionId);
                                            intent.putExtra(SearchPostsAdapter.EXTRA_USER_UID, uid);
                                            intent.putExtra(SearchPostsAdapter.TYPE, type);
                                            mContext.startActivity(intent);
                                        }
                                    });

                                }

                                holder.profileImageView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Intent intent = new Intent(mContext, ProfileActivity.class);
                                        intent.putExtra(SearchPostsAdapter.EXTRA_USER_UID, uid);
                                        mContext.startActivity(intent);
                                    }
                                });

//                        //calculate the generated points from the compiled time
//                        impressionReference.child("compiled_views").child(postId)
//                                .addValueEventListener(new ValueEventListener() {
//                                    @Override
//                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                                        if (dataSnapshot.exists()){
//                                            holder.mCreditsLinearLayout.setVisibility(View.VISIBLE);
//                                            ViewDuration impression = dataSnapshot.getValue(ViewDuration.class);
//                                            final long compiledDuration = impression.getCompiled_duration();
//                                            Log.d("compiled duration", compiledDuration + "");
//                                            //get seconds in milliseconds
//                                            final long durationInSeconds = compiledDuration / 1000;
//                                            //get the points generate
//                                            final double points = durationInSeconds * 0.000001;
//                                            DecimalFormat formatter = new DecimalFormat("0.0000");
//                                            final String pts = formatter.format(points);
//                                            holder.senseCreditsTextView.setText(pts + " points");
//
//                                        }else {
//                                            holder.mCreditsLinearLayout.setVisibility(View.VISIBLE);
//                                            final double points = 0.00;
//                                            DecimalFormat formatter = new DecimalFormat("0.00");
//                                            final String pts = formatter.format(points);
//                                            holder.senseCreditsTextView.setText(pts + " points");
//                                        }
//                                    }
//
//                                    @Override
//                                    public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                                    }
//                                });
//
//                        impressionReference.child("post_views").child(postId)
//                                .addValueEventListener(new ValueEventListener() {
//                                    @Override
//                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                                        if (dataSnapshot.exists()){
//                                            final long size = dataSnapshot.getChildrenCount();
//                                            int childrenCount = (int) size;
//                                            holder.viewsCountTextView.setText(childrenCount + "");
//                                        }else {
//                                            holder.viewsCountTextView.setText("0");
//                                        }
//                                    }
//
//                                    @Override
//                                    public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                                    }
//                                });


                                collectionsPosts.document(collectionId)
                                        .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                            @Override
                                            public void onEvent(@javax.annotation.Nullable DocumentSnapshot documentSnapshot,
                                                                @javax.annotation.Nullable FirebaseFirestoreException e) {
                                                if (e != null) {
                                                    Log.w(TAG, "Listen error", e);
                                                    return;
                                                }

                                                if (documentSnapshot.exists()){
                                                    Collection collection = documentSnapshot.toObject(Collection.class);
                                                    final String name = collection.getName();
                                                    final String creatorUid = collection.getUser_id();
                                                    holder.collectionNameTextView.setText("@" + name);
                                                    holder.collectionNameTextView.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View v) {
                                                            Intent intent = new Intent(mContext, CollectionPostsActivity.class);
                                                            intent.putExtra(SearchPostsAdapter.COLLECTION_ID, collectionId);
                                                            intent.putExtra(SearchPostsAdapter.EXTRA_USER_UID, creatorUid);
                                                            mContext.startActivity(intent);
                                                        }
                                                    });
                                                }
                                            }
                                        });

                                usersReference.document(uid).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                    @Override
                                    public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                                        if (e != null) {
                                            Log.w(TAG, "Listen error", e);
                                            return;
                                        }

                                        if (documentSnapshot.exists()){
                                            final Andeqan andeqan = documentSnapshot.toObject(Andeqan.class);
                                            holder.usernameTextView.setText(andeqan.getUsername());
                                            Glide.with(mContext.getApplicationContext())
                                                    .load(andeqan.getProfile_image())
                                                    .apply(new RequestOptions()
                                                            .placeholder(R.drawable.ic_user)
                                                            .diskCacheStrategy(DiskCacheStrategy.DATA))
                                                    .into(holder.profileImageView);
                                        }
                                    }
                                });

                                //get the number of commments in a single
                                commentsReference.document("post_ids").collection(postId)
                                        .orderBy("comment_id").whereEqualTo("post_id", postId)
                                        .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                            @Override
                                            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                                                if (e != null) {
                                                    Log.w(TAG, "Listen error", e);
                                                    return;
                                                }

                                                if (!documentSnapshots.isEmpty()){
                                                    final int commentsCount = documentSnapshots.size();
                                                    holder.commentsCountTextView.setText(commentsCount + "");
                                                }else {
                                                    holder.commentsCountTextView.setText("0");
                                                }
                                            }
                                        });

                            }

                        }
                    });

        }

        @Override
        public int getItemCount() {
            return documentSnapshots.size();
        }

        public void cleanUp(){
            documentSnapshots.clear();
            notifyDataSetChanged();
        }


    }


}

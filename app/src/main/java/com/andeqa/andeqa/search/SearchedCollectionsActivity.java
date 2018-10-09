package com.andeqa.andeqa.search;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
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

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.R;
import com.andeqa.andeqa.collections.CollectionPostsActivity;
import com.andeqa.andeqa.collections.CollectionViewHolder;
import com.andeqa.andeqa.collections.FeaturedCollectionsAdapter;
import com.andeqa.andeqa.models.Collection;
import com.andeqa.andeqa.models.QueryOptions;
import com.andeqa.andeqa.models.Relation;
import com.andeqa.andeqa.utils.ItemOffsetDecoration;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
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

public class SearchedCollectionsActivity extends AppCompatActivity {
    @Bind(R.id.searchedCollectionsRecyclerView)RecyclerView searchedCollectionsRecyclerView;
    @Bind(R.id.toolbar)Toolbar toolbar;
    private CollectionReference usersCollections;
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
    private SearchCollectionsAdapter searchCollectionsAdapter;
    private static final String TAG = SearchedCollectionsActivity.class.getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_collections);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

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

        queryOptionsReference = FirebaseFirestore.getInstance().collection(Constants.QUERY_OPTIONS);

    }

    @Override
    public void onStart() {
        super.onStart();
        documentSnapshots.clear();
        setRecyclerView();
        searchedCollectionsRecyclerView.addItemDecoration(itemOffsetDecoration);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_search, menu);
        searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchCollectionsAdapter.cleanUp();
                documentSnapshots.clear();
                searchPosts(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchCollectionsAdapter.cleanUp();
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
        searchedCollectionsRecyclerView.removeItemDecoration(itemOffsetDecoration);
    }


    private void setRecyclerView(){
        searchCollectionsAdapter = new SearchCollectionsAdapter(SearchedCollectionsActivity.this);
        searchCollectionsAdapter.setHasStableIds(true);
        searchedCollectionsRecyclerView.setHasFixedSize(false);
        searchCollectionsAdapter.setSearchedCollections(documentSnapshots);
        searchedCollectionsRecyclerView.setAdapter(searchCollectionsAdapter);
        layoutManager = new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL);
        itemOffsetDecoration = new ItemOffsetDecoration(this, R.dimen.item_off_set);
        searchedCollectionsRecyclerView.setLayoutManager(layoutManager);

    }

    private void searchPosts(final String word){
        if (word.isEmpty()){
            searchCollectionsAdapter.cleanUp();
        }else {
            queryOptionsReference.whereEqualTo("type", "collection")
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

            queryOptionsReference.whereEqualTo("type", "collection")
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

                            }
                        }
                    });

        }
    }

    protected void onDocumentAdded(DocumentChange change) {
        postsIds.add(change.getDocument().getId());
        documentSnapshots.add(change.getDocument());
        searchCollectionsAdapter.notifyItemInserted(documentSnapshots.size() - 1);
        searchCollectionsAdapter.getItemCount();
    }

    protected void onDocumentModified(DocumentChange change) {
        try {
            if (change.getOldIndex() == change.getNewIndex()) {
                // Item changed but remained in same position
                documentSnapshots.set(change.getOldIndex(), change.getDocument());
                searchCollectionsAdapter.notifyItemChanged(change.getOldIndex());
            } else {
                // Item changed and changed position
                documentSnapshots.remove(change.getOldIndex());
                documentSnapshots.add(change.getNewIndex(), change.getDocument());
                searchCollectionsAdapter.notifyItemRangeChanged(0, documentSnapshots.size());
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    protected void onDocumentRemoved(DocumentChange change) {
        try {
            documentSnapshots.remove(change.getOldIndex());
            searchCollectionsAdapter.notifyItemRemoved(change.getOldIndex());
            searchCollectionsAdapter.notifyItemRangeChanged(0, documentSnapshots.size());
        }catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static class SearchCollectionsAdapter extends RecyclerView.Adapter<CollectionViewHolder>{
        private static final String TAG = FeaturedCollectionsAdapter.class.getSimpleName();

        private Context mContext;
        //firestore
        private CollectionReference collectionsPostsReference;
        private CollectionReference followingCollection;
        private CollectionReference collectionsReference;
        private Query postCountQuery;
        //firebase auth
        private FirebaseAuth firebaseAuth;
        private static final String COLLECTION_ID = "collection id";
        private static final String EXTRA_USER_UID = "uid";
        private  static final int MAX_WIDTH = 200;
        private static final int MAX_HEIGHT = 200;
        private boolean processFollow = false;
        private List<DocumentSnapshot> featuredCollections = new ArrayList<>();

        public SearchCollectionsAdapter(Context mContext) {
            this.mContext = mContext;
        }

        protected void setSearchedCollections(List<DocumentSnapshot> mSnapshots){
            this.featuredCollections = mSnapshots;
        }

        @Override
        public int getItemCount() {
            return featuredCollections.size();
        }

        protected DocumentSnapshot getSnapshot(int index) {
            return featuredCollections.get(index);
        }


        public void cleanUp(){
            featuredCollections.clear();
            notifyDataSetChanged();
        }


        @NonNull
        @Override
        public CollectionViewHolder onCreateViewHolder(final @NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_collections, parent, false);
            return new CollectionViewHolder(view );
        }

        @Override
        public void onBindViewHolder(final @NonNull CollectionViewHolder holder, int position) {
            final QueryOptions queryOptions = getSnapshot(position).toObject(QueryOptions.class);
            final String collectionId = queryOptions.getOption_id();
            final String userId = queryOptions.getUser_id();

            firebaseAuth = FirebaseAuth.getInstance();
            collectionsReference = FirebaseFirestore.getInstance().collection(Constants.COLLECTIONS);
            collectionsPostsReference = FirebaseFirestore.getInstance().collection(Constants.COLLECTIONS_OF_POSTS);
            postCountQuery = collectionsPostsReference.document("collections").collection(collectionId)
                    .orderBy("collection_id");
            followingCollection = FirebaseFirestore.getInstance().collection(Constants.COLLECTION_RELATIONS);

            collectionsReference.document(collectionId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {


                    if (e != null) {
                        Log.w(TAG, "Listen error", e);
                        return;
                    }

                    if (documentSnapshot.exists()){
                        Collection collection = documentSnapshot.toObject(Collection.class);
                        Glide.with(mContext.getApplicationContext())
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

                    }

                    holder.mCollectionsLinearLayout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(mContext, CollectionPostsActivity.class);
                            intent.putExtra(SearchCollectionsAdapter.COLLECTION_ID, collectionId);
                            intent.putExtra(SearchCollectionsAdapter.EXTRA_USER_UID, userId);
                            mContext.startActivity(intent);
                        }
                    });

//                    postCountQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
//                        @Override
//                        public void onEvent(@Nullable QuerySnapshot documentSnapshots,
//                                            @Nullable FirebaseFirestoreException e) {
//                            if (e != null) {
//                                Log.w(TAG, "Listen error", e);
//                                return;
//                            }
//
//                            if (!documentSnapshots.isEmpty()){
//                                holder.postsCountTextView.setVisibility(View.VISIBLE);
//                                holder.postsCountTextView.setText( "following " + documentSnapshots.size());
//                            }else {
//                                holder.postsCountTextView.setText("Posts 0");
//                                holder.postsCountTextView.setVisibility(View.VISIBLE);
//                            }
//                        }
//                    });

                    /**show if the user is following collection or not**/
                    followingCollection.document("following")
                            .collection(collectionId)
                            .whereEqualTo("following_id", firebaseAuth.getCurrentUser().getUid())
                            .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                @Override
                                public void onEvent(@Nullable QuerySnapshot documentSnapshots,
                                                    @Nullable FirebaseFirestoreException e) {

                                    if (e != null) {
                                        Log.w(TAG, "Listen error", e);
                                        return;
                                    }

                                    if (!documentSnapshots.isEmpty()){
                                        holder.followButton.setText("FOLLOWING");
                                    }else {
                                        if (userId.equals(firebaseAuth.getCurrentUser().getUid())){
                                            holder.followButton.setVisibility(View.GONE);
                                        }else {
                                            holder.followButton.setText("FOLLOW");
                                        }
                                    }

                                }
                            });

//                    /**show the number of peopl following collection**/
//                    followingCollection.document("following")
//                            .collection(collectionId)
//                            .addSnapshotListener(new EventListener<QuerySnapshot>() {
//                                @Override
//                                public void onEvent(@Nullable QuerySnapshot documentSnapshots,
//                                                    @Nullable FirebaseFirestoreException e) {
//
//                                    if (e != null) {
//                                        Log.w(TAG, "Listen error", e);
//                                        return;
//                                    }
//
//                                    if (!documentSnapshots.isEmpty()){
//                                        holder.followingCountTextView.setVisibility(View.VISIBLE);
//                                        int following = documentSnapshots.size();
//                                        holder.followingCountTextView.setText("following " + following);
//                                    }else {
//                                        holder.followingCountTextView.setVisibility(View.VISIBLE);
//                                        holder.followingCountTextView.setText("following 0");
//                                    }
//
//                                }
//                            });


                    /**follow or un follow collection*/
                    if (!userId.equals(firebaseAuth.getCurrentUser().getUid())){
                        holder.followButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                processFollow = true;
                                followingCollection.document("following")
                                        .collection(collectionId)
                                        .whereEqualTo("following_id", firebaseAuth.getCurrentUser().getUid())
                                        .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                            @Override
                                            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                                                if (e != null) {
                                                    Log.w(TAG, "Listen error", e);
                                                    return;
                                                }

                                                if (processFollow){
                                                    if (documentSnapshots.isEmpty()){
                                                        final Relation following = new Relation();
                                                        following.setFollowing_id(firebaseAuth.getCurrentUser().getUid());
                                                        following.setFollowed_id(collectionId);
                                                        following.setType("followed_collection");
                                                        following.setTime(System.currentTimeMillis());
                                                        followingCollection.document("following")
                                                                .collection(collectionId)
                                                                .document(firebaseAuth.getCurrentUser().getUid()).set(following);
                                                        holder.followButton.setText("FOLLOWING");
                                                        processFollow = false;
                                                    }else {
                                                        followingCollection.document("following")
                                                                .collection(collectionId)
                                                                .document(firebaseAuth.getCurrentUser().getUid()).delete();

                                                        holder.followButton.setText("FOLLOW");
                                                        processFollow = false;
                                                    }
                                                }
                                            }
                                        });
                            }
                        });
                    }

                }
            });


        }

    }
}

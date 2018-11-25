package com.andeqa.andeqa.more;


import android.arch.paging.PagedList;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintSet;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.R;
import com.andeqa.andeqa.collections.CollectionPostsActivity;
import com.andeqa.andeqa.collections.CollectionsFragment;
import com.andeqa.andeqa.comments.CommentsActivity;
import com.andeqa.andeqa.home.HomeFragment;
import com.andeqa.andeqa.home.PhotoPostViewHolder;
import com.andeqa.andeqa.models.Andeqan;
import com.andeqa.andeqa.models.Collection;
import com.andeqa.andeqa.models.CollectionPost;
import com.andeqa.andeqa.models.Post;
import com.andeqa.andeqa.post_detail.PostDetailActivity;
import com.andeqa.andeqa.profile.ProfileActivity;
import com.andeqa.andeqa.search.SearchPostsActivity;
import com.andeqa.andeqa.utils.ItemOffsetDecoration;
import com.andeqa.andeqa.utils.BottomReachedListener;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.firebase.ui.firestore.paging.LoadingState;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class MoreFragment extends Fragment{
    // bind views
    @Bind(R.id.morePostsRecyclerView)RecyclerView mMorePostsRecyclerView;
    @Bind(R.id.progressBar)ProgressBar mProgressBar;
    @Bind(R.id.progressRelativeLayout)RelativeLayout mProgressRelativeLayout;
    private static final String TAG = HomeFragment.class.getSimpleName();
    //firestore reference
    private CollectionReference postsCollectionReference;
    private CollectionReference usersReference;
    private CollectionReference collectionsPosts;
    private CollectionReference commentsReference;
    private DatabaseReference impressionReference;
    private CollectionReference collectionsPostReference;
    //firebase auth
    private FirebaseAuth firebaseAuth;
    //layouts
    private ConstraintSet constraintSet;
    private ItemOffsetDecoration itemOffsetDecoration;
    private StaggeredGridLayoutManager layoutManager;
    //strings
    private static final String EXTRA_POST_ID = "post id";
    private static final String COLLECTION_ID = "collection id";
    private static final String EXTRA_USER_UID =  "uid";
    private static final String TYPE = "type";
    private static final String POST_HEIGHT = "height";
    private static final String POST_WIDTH = "width";

    public static MoreFragment newInstance(Collection collection) {
        final MoreFragment fragment = new MoreFragment();
        final Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public MoreFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_more, container, false);
        // Inflate the layout for this fragment
        ButterKnife.bind(this, view);
        // initialize firestore references
        initReferences();
        //set up the adapter
        setUpAdapter();

        return view;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FragmentManager manager = getChildFragmentManager();
        CollectionsFragment collectionsFragment = new CollectionsFragment();
        FragmentTransaction ft = manager.beginTransaction();
        ft.replace(R.id.collectionsContainerFrameLayout, collectionsFragment);
        ft.commit();
    }

    @Override
    public void onStart() {
        super.onStart();
        mMorePostsRecyclerView.addItemDecoration(itemOffsetDecoration);

    }

    @Override
    public void onCreate(@android.support.annotation.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.explore_menu, menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_search){
            Intent intent =  new Intent(getActivity(), SearchPostsActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
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
    public void onDestroyView() {
        super.onDestroyView();

    }

    @Override
    public void onStop() {
        super.onStop();
        mMorePostsRecyclerView.removeItemDecoration(itemOffsetDecoration);
    }

    private void initReferences(){
        firebaseAuth = FirebaseAuth.getInstance();
        //firestore
        postsCollectionReference = FirebaseFirestore.getInstance().collection(Constants.POSTS);
        usersReference = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);
        usersReference = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);
        collectionsPostReference = FirebaseFirestore.getInstance().collection(Constants.COLLECTIONS);
        //firebase
        //document reference
        commentsReference  = FirebaseFirestore.getInstance().collection(Constants.COMMENTS);
        //firebase database references
        impressionReference = FirebaseDatabase.getInstance().getReference(Constants.VIEWS);
        impressionReference.keepSynced(true);
    }


    private void setUpAdapter() {
        Query query = postsCollectionReference.orderBy("random_number", Query.Direction.DESCENDING);
        PagedList.Config config = new PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPrefetchDistance(10)
                .setPageSize(20)
                .build();

        FirestorePagingOptions<Post> options = new FirestorePagingOptions.Builder<Post>()
                .setLifecycleOwner(this)
                .setQuery(query, config, Post.class)
                .build();

        FirestorePagingAdapter<Post, PhotoPostViewHolder> pagingAdapter
                = new FirestorePagingAdapter<Post, PhotoPostViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final PhotoPostViewHolder holder, int position, @NonNull Post model) {
                final Post post = model;
                final String postId = post.getPost_id();
                final String uid = post.getUser_id();
                final String collectionId = post.getCollection_id();
                final String type = post.getType();

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


                if (post.getUrl() == null){
                    //firebase firestore references
                    if (type.equals("single")|| type.equals("single_image_post")){
                        collectionsPosts = FirebaseFirestore.getInstance().collection(Constants.COLLECTIONS_OF_POSTS)
                                .document("singles").collection(collectionId);
                    }else{
                        collectionsPosts = FirebaseFirestore.getInstance().collection(Constants.COLLECTIONS_OF_POSTS)
                                .document("collections").collection(collectionId);
                    }
                    collectionsPosts.document(postId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                            if (e != null) {
                                Log.w(TAG, "Listen error", e);
                                return;
                            }

                            if (documentSnapshot.exists()){
                                final CollectionPost collectionPost = documentSnapshot.toObject(CollectionPost.class);
                                //set the image on the image view
                                Glide.with(getActivity().getApplicationContext())
                                        .load(collectionPost.getImage())
                                        .apply(new RequestOptions()
                                                .placeholder(R.drawable.post_placeholder)
                                                .diskCacheStrategy(DiskCacheStrategy.DATA))
                                        .into(holder.postImageView);

                                if (!TextUtils.isEmpty(collectionPost.getTitle())){
                                    holder.captionLinearLayout.setVisibility(View.VISIBLE);
                                    holder.titleTextView.setText(collectionPost.getTitle());
                                    holder.titleRelativeLayout.setVisibility(View.VISIBLE);
                                }else {
                                    holder.titleRelativeLayout.setVisibility(View.GONE);
                                }

                                if (!TextUtils.isEmpty(collectionPost.getDescription())){
                                    //prevent collection note from overlapping other layouts
                                    final String [] strings = collectionPost.getDescription().split("");
                                    final int size = strings.length;
                                    if (size <= 50){
                                        holder.captionLinearLayout.setVisibility(View.VISIBLE);
                                        holder.descriptionRelativeLayout.setVisibility(View.VISIBLE);
                                        holder.descriptionTextView.setText(collectionPost.getDescription());
                                    }else{
                                        holder.captionLinearLayout.setVisibility(View.VISIBLE);
                                        holder.descriptionRelativeLayout.setVisibility(View.VISIBLE);
                                        final String boldMore = "...";
                                        String normalText = collectionPost.getDescription().substring(0, 49);
                                        holder.descriptionTextView.setText(normalText + boldMore);
                                    }
                                }else {
                                    holder.captionLinearLayout.setVisibility(View.GONE);
                                }
                            }else {
                                //post does not exist
                            }
                        }
                    });
                }else {
                    Glide.with(getActivity().getApplicationContext())
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
                            holder. captionLinearLayout.setVisibility(View.VISIBLE);
                            holder.descriptionRelativeLayout.setVisibility(View.VISIBLE);
                            final String boldMore = "...";
                            String normalText = post.getDescription().substring(0, 49);
                            holder.descriptionTextView.setText(normalText + boldMore);
                        }
                    }else {
                        holder.captionLinearLayout.setVisibility(View.GONE);
                    }
                }


                if (post.getWidth() != null && post.getHeight() != null){
                    holder.postImageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent =  new Intent(getActivity(), PostDetailActivity.class);
                            intent.putExtra(MoreFragment.EXTRA_POST_ID, postId);
                            intent.putExtra(MoreFragment.COLLECTION_ID, collectionId);
                            intent.putExtra(MoreFragment.EXTRA_USER_UID, uid);
                            intent.putExtra(MoreFragment.TYPE, type);
                            intent.putExtra(MoreFragment.POST_HEIGHT, post.getHeight());
                            intent.putExtra(MoreFragment.POST_WIDTH, post.getWidth());
                            startActivity(intent);
                        }
                    });
                }else {
                    holder.postImageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent =  new Intent(getActivity(), PostDetailActivity.class);
                            intent.putExtra(MoreFragment.EXTRA_POST_ID, postId);
                            intent.putExtra(MoreFragment.COLLECTION_ID, collectionId);
                            intent.putExtra(MoreFragment.EXTRA_USER_UID, uid);
                            intent.putExtra(MoreFragment.TYPE, type);
                            startActivity(intent);
                        }
                    });

                }

                holder.profileImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getActivity(), ProfileActivity.class);
                        intent.putExtra(MoreFragment.EXTRA_USER_UID, uid);
                        startActivity(intent);
                    }
                });

                holder.commentsImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getActivity(), CommentsActivity.class);
                        intent.putExtra(MoreFragment.EXTRA_POST_ID, postId);
                        startActivity(intent);
                    }
                });

                collectionsPostReference.document(collectionId)
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
                                            Intent intent = new Intent(getActivity(), CollectionPostsActivity.class);
                                            intent.putExtra(MoreFragment.COLLECTION_ID, collectionId);
                                            intent.putExtra(MoreFragment.EXTRA_USER_UID, creatorUid);
                                            startActivity(intent);
                                        }
                                    });
                                }else {
                                    holder.collectionNameTextView.setText("");
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

                            Glide.with(getActivity().getApplicationContext())
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

            @NonNull
            @Override
            public PhotoPostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.layout_explore_posts, parent, false);
                return new PhotoPostViewHolder(view);
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
                        mProgressBar.setVisibility(View.VISIBLE);
                        mProgressRelativeLayout.setVisibility(View.VISIBLE);
                        break;
                    case LOADED:
                        mProgressBar.setVisibility(View.GONE);
                        mProgressRelativeLayout.setVisibility(View.GONE);
                        break;
                    case FINISHED:
                        mProgressBar.setVisibility(View.GONE);
                        showToast("Reached end of data set.");
                        break;
                    case ERROR:
                        showToast("An error occurred.");
                        retry();
                        break;
                }            }
        };

        pagingAdapter.setHasStableIds(true);
        mMorePostsRecyclerView.setHasFixedSize(false);
        mMorePostsRecyclerView.setAdapter(pagingAdapter);
        layoutManager = new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL);
        itemOffsetDecoration = new ItemOffsetDecoration(getContext(), R.dimen.item_off_set);
        mMorePostsRecyclerView.setLayoutManager(layoutManager);

    }

    private void showToast(@NonNull String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

}

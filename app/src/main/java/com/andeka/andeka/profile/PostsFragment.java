package com.andeka.andeka.profile;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.andeka.andeka.Constants;
import com.andeka.andeka.R;
import com.andeka.andeka.models.CollectionPost;
import com.andeka.andeka.models.Post;
import com.andeka.andeka.post_detail.PostDetailActivity;
import com.andeka.andeka.post_detail.VideoDetailActivity;
import com.andeka.andeka.utils.ItemOffsetDecoration;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class PostsFragment extends Fragment{
    @Bind(R.id.postsRecyclerView)RecyclerView mPostssRecyclerView;
    private static final String TAG = PostsFragment.class.getSimpleName();

    //firestore reference
    private CollectionReference postsCollection;
    private Query postsQuery;
    private CollectionReference collectionsPosts;
    private CollectionReference commentsReference;
    //firebase auth
    private FirebaseAuth firebaseAuth;
    //firestore adapters
//    private ProfilePostAdapter profilePostAdapter;
    private FirestoreRecyclerAdapter firestoreRecyclerAdapter;
    private int TOTAL_ITEMS = 10;
    private StaggeredGridLayoutManager layoutManager;
    private static final String EXTRA_USER_UID = "uid";
    private String mUid;
    private String mPostId;
    private List<String> mSnapshotsIds = new ArrayList<>();
    private List<DocumentSnapshot> mSnapshots = new ArrayList<>();
    private ItemOffsetDecoration itemOffsetDecoration;

    private static final String COLLECTION_ID = "collection id";
    private static final String EXTRA_POST_ID = "post id";
    private static final String TYPE = "type";
    private static final String POST_HEIGHT = "height";
    private static final String POST_WIDTH = "width";

    public static PostsFragment newInstance(String title) {
        PostsFragment fragment = new PostsFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        fragment.setArguments(args);
        return fragment;
    }


    public PostsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_posts, container, false);
        ButterKnife.bind(this, view);
        //initialize click listener
        //FIREBASE AUTH
        firebaseAuth = FirebaseAuth.getInstance();
        mUid = getActivity().getIntent().getStringExtra(EXTRA_USER_UID);

        postsCollection = FirebaseFirestore.getInstance().collection(Constants.POSTS);
        postsQuery = postsCollection.orderBy("time", Query.Direction.DESCENDING)
                .whereEqualTo("user_id", mUid).limit(TOTAL_ITEMS);
        firebaseAuth = FirebaseAuth.getInstance();

        return view;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        loadData();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStart() {
        super.onStart();
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
        firestoreRecyclerAdapter.stopListening();
    }


    private void loadData(){
        mSnapshots.clear();
        profilePosts();
        setRecyclerView();
    }

    private void setRecyclerView(){
        // RecyclerView
        mPostssRecyclerView.setAdapter(firestoreRecyclerAdapter);
        firestoreRecyclerAdapter.startListening();
        mPostssRecyclerView.setHasFixedSize(false);
        layoutManager = new StaggeredGridLayoutManager(1,StaggeredGridLayoutManager.HORIZONTAL);
        itemOffsetDecoration = new ItemOffsetDecoration(getContext(), R.dimen.item_off_set);
        mPostssRecyclerView.setLayoutManager(layoutManager);

    }

    private void profilePosts() {
        FirestoreRecyclerOptions<Post> options = new FirestoreRecyclerOptions.Builder<Post>()
                .setQuery(postsQuery, Post.class)
                .build();

        firestoreRecyclerAdapter = new FirestoreRecyclerAdapter<Post, ProfilePostViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ProfilePostViewHolder holder, int position, @NonNull Post mode) {
                final Post post = getSnapshots().get(position);
                final String postId = post.getPost_id();
                final String uid = post.getUser_id();
                final String collectionId = post.getCollection_id();
                final String type = post.getType();

                if (type.equals("single") || type.equals("single_image_post")){
                    collectionsPosts = FirebaseFirestore.getInstance().collection(Constants.POSTS_OF_COLLECTION)
                            .document("singles").collection(collectionId);
                }else {
                    collectionsPosts = FirebaseFirestore.getInstance().collection(Constants.POSTS_OF_COLLECTION)
                            .document("collections").collection(collectionId);
                }

                if (post.getUrl() == null){
                    collectionsPosts.document(postId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                            if (e != null) {
                                Log.w(TAG, "Listen error", e);
                                return;
                            }

                            if (documentSnapshot.exists()){
                                final CollectionPost collectionPost = documentSnapshot.toObject(CollectionPost.class);

                                Glide.with(PostsFragment.this)
                                        .load(collectionPost.getImage())
                                        .apply(new RequestOptions()
                                                .placeholder(R.drawable.post_placeholder)
                                                .diskCacheStrategy(DiskCacheStrategy.DATA))
                                        .into(holder.postImageView);

                            }
                        }
                    });

                }else {
                    Glide.with(PostsFragment.this)
                            .load(post.getUrl())
                            .apply(new RequestOptions()
                                    .placeholder(R.drawable.post_placeholder)
                                    .diskCacheStrategy(DiskCacheStrategy.DATA))
                            .into(holder.postImageView);

                }



                if (type.equals("video")){
                    holder.postImageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent =  new Intent(getActivity(), VideoDetailActivity.class);
                            intent.putExtra(PostsFragment.EXTRA_POST_ID, postId);
                            intent.putExtra(PostsFragment.COLLECTION_ID, collectionId);
                            intent.putExtra(PostsFragment.EXTRA_USER_UID, uid);
                            intent.putExtra(PostsFragment.TYPE, type);
                            startActivity(intent);
                        }
                    });
                }else {
                    if (post.getWidth() != null && post.getHeight() != null){
                        holder.postImageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent =  new Intent(getActivity(), PostDetailActivity.class);
                                intent.putExtra(PostsFragment.EXTRA_POST_ID, postId);
                                intent.putExtra(PostsFragment.COLLECTION_ID, collectionId);
                                intent.putExtra(PostsFragment.EXTRA_USER_UID, uid);
                                intent.putExtra(PostsFragment.TYPE, type);
                                intent.putExtra(PostsFragment.POST_HEIGHT, post.getHeight());
                                intent.putExtra(PostsFragment.POST_WIDTH, post.getWidth());
                                startActivity(intent);
                            }
                        });
                    }else {
                        holder.postImageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent =  new Intent(getActivity(), PostDetailActivity.class);
                                intent.putExtra(PostsFragment.EXTRA_POST_ID, postId);
                                intent.putExtra(PostsFragment.COLLECTION_ID, collectionId);
                                intent.putExtra(PostsFragment.EXTRA_USER_UID, uid);
                                intent.putExtra(PostsFragment.TYPE, type);
                                startActivity(intent);
                            }
                        });

                    }
                }

            }

            @NonNull
            @Override
            public ProfilePostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.layout_profile_posts, parent, false);
                return new ProfilePostViewHolder(view);
            }

            @Override
            public int getItemViewType(int position) {
                return super.getItemViewType(position);
            }
        };

    }

    public static class ProfilePostViewHolder extends RecyclerView.ViewHolder{
        View mView;
        Context mContext;
        public ImageView postImageView;


        public ProfilePostViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            mContext = itemView.getContext();
            postImageView = (ImageView) mView.findViewById(R.id.postImageView);

        }
    }

    @Override
    public void onResume() {
        super.onResume();

    }

}

package com.andeqa.andeqa.collections;


import android.arch.paging.PagedList;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.R;
import com.andeqa.andeqa.models.Collection;
import com.andeqa.andeqa.utils.ItemOffsetDecoration;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class CollectionsFragment extends Fragment implements View.OnClickListener{
    @Bind(R.id.collectionsRecyclerView)RecyclerView mCollectionsRecyclerView;
    @Bind(R.id.viewCollectionsTextView)TextView mViewCollectionsTextView;

    private static final String TAG = CollectionsFragment.class.getSimpleName();
    //firestore reference
    private CollectionReference collectionsCollection;
    private CollectionReference usersReference;
    private Query collectionsQuery;
    //firebase auth
    private FirebaseAuth firebaseAuth;
    //strings
    private static final String EXTRA_USER_UID = "uid";
    private static final String COLLECTION_ID = "collection id";
    //layouts
    private ItemOffsetDecoration itemOffsetDecoration;
    private StaggeredGridLayoutManager layoutManager;
    private SearchView searchView;


    public CollectionsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_collections, container, false);
        ButterKnife.bind(this, view);
        //initialize click listener
        mViewCollectionsTextView.setOnClickListener(this);
        // initialise firebase
        initFirebase();
        //set up adapter
        setUpAdapter();

        return view;
    }


    @Override
    public void onActivityCreated(@android.support.annotation.Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
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
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onStop() {
        super.onStop();
        mCollectionsRecyclerView.removeItemDecoration(itemOffsetDecoration);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View v){
        if (v == mViewCollectionsTextView){
            Intent intent = new Intent(getActivity(), ExploreCollectionsActivity.class);
            startActivity(intent);
        }

    }


    private void initFirebase(){
        firebaseAuth = FirebaseAuth.getInstance();
        usersReference = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);
        collectionsCollection = FirebaseFirestore.getInstance().collection(Constants.COLLECTIONS);
        collectionsQuery = collectionsCollection.orderBy("name", Query.Direction.ASCENDING);
    }

    private void setUpAdapter(){
        PagedList.Config config = new PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPrefetchDistance(10)
                .setPageSize(20)
                .build();

        FirestorePagingOptions<Collection> options = new FirestorePagingOptions.Builder<Collection>()
                .setLifecycleOwner(this)
                .setQuery(collectionsQuery, config, Collection.class)
                .build();


        FirestorePagingAdapter<Collection, CollectionViewHolder>
                pagingAdapter = new FirestorePagingAdapter<Collection, CollectionViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull CollectionViewHolder holder, int position, @NonNull Collection model) {
                final String collectionId = model.getCollection_id();
                final String userId = model.getUser_id();
                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getActivity(), CollectionPostsActivity.class);
                        intent.putExtra(CollectionsFragment.COLLECTION_ID, collectionId);
                        intent.putExtra(CollectionsFragment.EXTRA_USER_UID, userId);
                        startActivity(intent);
                    }
                });

                Glide.with(CollectionsFragment.this)
                        .asBitmap()
                        .load(model.getImage())
                        .apply(new RequestOptions()
                                .placeholder(R.drawable.post_placeholder)
                                .diskCacheStrategy(DiskCacheStrategy.DATA))
                        .into(holder.mCollectionCoverImageView);

                if (!TextUtils.isEmpty(model.getName())){
                    holder.mCollectionNameTextView.setText(model.getName());
                }else {
                    holder.mCollectionNameTextView.setVisibility(View.GONE);
                }
            }

            @NonNull
            @Override
            public CollectionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_collections, parent, false);
                return new CollectionViewHolder(view );
            }
        };

        mCollectionsRecyclerView.setAdapter(pagingAdapter);
        mCollectionsRecyclerView.setHasFixedSize(false);
        layoutManager = new StaggeredGridLayoutManager(1,StaggeredGridLayoutManager.HORIZONTAL);
        itemOffsetDecoration = new ItemOffsetDecoration(getContext(), R.dimen.item_off_set);
        mCollectionsRecyclerView.setLayoutManager(layoutManager);


    }

    public class CollectionViewHolder extends RecyclerView.ViewHolder {

        View mView;
        Context mContext;
        public ImageView mCollectionCoverImageView;
        public TextView mCollectionNameTextView;


        public CollectionViewHolder(View itemView) {
            super(itemView);
            mContext = itemView.getContext();
            mView = itemView;
            mCollectionCoverImageView = (ImageView) mView.findViewById(R.id.collectionCoverImageView);
            mCollectionNameTextView = (TextView) mView.findViewById(R.id.collectionNameTextView);

        }
    }
}



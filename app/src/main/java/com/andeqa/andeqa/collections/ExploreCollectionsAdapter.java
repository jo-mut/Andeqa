package com.andeqa.andeqa.collections;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.R;
import com.andeqa.andeqa.models.Collection;
import com.andeqa.andeqa.utils.BottomReachedListener;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class ExploreCollectionsAdapter extends RecyclerView.Adapter<ExploreCollectionViewHolder> {
    private static final String TAG = ExploreCollectionsAdapter.class.getSimpleName();

    private Context mContext;
    //firestore
    private CollectionReference collectionsCollection;
    private CollectionReference followingCollection;
    private CollectionReference usersCollection;
    private Query postCountQuery;
    //firebase auth
    private FirebaseAuth firebaseAuth;
    private static final String COLLECTION_ID = "collection id";
    private static final String EXTRA_USER_UID = "uid";
    private  static final int MAX_WIDTH = 200;
    private static final int MAX_HEIGHT = 200;
    private boolean processFollow = false;
    private List<DocumentSnapshot> documentSnapshots = new ArrayList<>();
    private BottomReachedListener mBottomReachedListener;

    public ExploreCollectionsAdapter(Context mContext) {
        this.mContext = mContext;
    }

    public void setCollections(List<DocumentSnapshot> mSnapshots){
        this.documentSnapshots = mSnapshots;
    }

    @Override
    public int getItemCount() {
        return documentSnapshots.size();
    }

    public DocumentSnapshot getSnapshot(int index) {
        return documentSnapshots.get(index);
    }

    public void setBottomReachedListener(BottomReachedListener bottomReachedListener){
        this.mBottomReachedListener = bottomReachedListener;
    }

    @NonNull
    @Override
    public ExploreCollectionViewHolder onCreateViewHolder(final @NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_explore_collections, parent, false);
        return new ExploreCollectionViewHolder(view );
    }

    @Override
    public void onBindViewHolder(final @NonNull ExploreCollectionViewHolder holder, int position) {
        final Collection collection = getSnapshot(position).toObject(Collection.class);
        final String collectionId = collection.getCollection_id();
        final String userId = collection.getUser_id();

        try {
            if (position == documentSnapshots.size() - 1){
                mBottomReachedListener.onBottomReached(position);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        firebaseAuth = FirebaseAuth.getInstance();
        collectionsCollection = FirebaseFirestore.getInstance().collection(Constants.COLLECTIONS_OF_POSTS);
        postCountQuery = collectionsCollection.document("collections").collection(collectionId)
                .orderBy("collection_id");
        followingCollection = FirebaseFirestore.getInstance().collection(Constants.COLLECTION_RELATIONS);
        usersCollection = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);

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


        holder.mCollectionsLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, CollectionPostsActivity.class);
                intent.putExtra(ExploreCollectionsAdapter.COLLECTION_ID, collectionId);
                intent.putExtra(ExploreCollectionsAdapter.EXTRA_USER_UID, userId);
                mContext.startActivity(intent);
            }
        });



        /**follow or un follow collection*/
        if (!userId.equals(firebaseAuth.getCurrentUser().getUid())){
//            holder.followButton.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    processFollow = true;
//                    followingCollection.document("following")
//                            .collection(collectionId)
//                            .whereEqualTo("following_id", firebaseAuth.getCurrentUser().getUid())
//                            .addSnapshotListener(new EventListener<QuerySnapshot>() {
//                                @Override
//                                public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
//
//                                    if (e != null) {
//                                        Log.w(TAG, "Listen error", e);
//                                        return;
//                                    }
//
//                                    if (processFollow){
//                                        if (documentSnapshots.isEmpty()){
//                                            final Relation following = new Relation();
//                                            following.setFollowing_id(firebaseAuth.getCurrentUser().getUid());
//                                            following.setFollowed_id(collectionId);
//                                            following.setType("followed_collection");
//                                            following.setTime(System.currentTimeMillis());
//                                            followingCollection.document("following")
//                                                    .collection(collectionId)
//                                                    .document(firebaseAuth.getCurrentUser().getUid()).set(following);
//                                            processFollow = false;
//                                        }else {
//                                            followingCollection.document("following")
//                                                    .collection(collectionId)
//                                                    .document(firebaseAuth.getCurrentUser().getUid()).delete();
//
//                                            processFollow = false;
//                                        }
//                                    }
//                                }
//                            });
//                }
//            });
        }

    }

}


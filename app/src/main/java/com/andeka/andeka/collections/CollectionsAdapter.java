package com.andeka.andeka.collections;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.andeka.andeka.Constants;
import com.andeka.andeka.R;
import com.andeka.andeka.models.Andeqan;
import com.andeka.andeka.models.Collection;
import com.andeka.andeka.models.Relation;
import com.andeka.andeka.utils.BottomReachedListener;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
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

public class CollectionsAdapter extends RecyclerView.Adapter<CollectionViewHolder> {
    private static final String TAG = CollectionsAdapter.class.getSimpleName();
    private Context mContext;
    // firebase
    private CollectionReference followingCollections;
    private CollectionReference postCollections;
    private Query postCountQuery;
    private CollectionReference usersCollection;
    //firebase auth
    private FirebaseAuth firebaseAuth;
    private static final String COLLECTION_ID = "collection id";
    private static final String EXTRA_USER_UID = "uid";
    private BottomReachedListener mBottomReachedListener;
    private List<DocumentSnapshot> documentSnapshots;
    private boolean processFollow = false;

    public CollectionsAdapter(Context mContext, List<DocumentSnapshot> documents) {
        this.mContext = mContext;
        this.documentSnapshots = new ArrayList<>();
        this.documentSnapshots = documents;
        initFirebaseReferences();

    }

    public void setBottomReachedListener(BottomReachedListener bottomReachedListener){
        this.mBottomReachedListener = bottomReachedListener;
    }

    @Override
    public int getItemCount() {
        return documentSnapshots.size();
    }

    public DocumentSnapshot getSnapshot(int index){
        return documentSnapshots.get(index);
    }

  @Override
    public long getItemId(int position) {
      final Collection collection = getSnapshot(position).toObject(Collection.class);
      return collection.getNumber();
    }


    @Override
    public void setHasStableIds(boolean hasStableIds) {
        super.setHasStableIds(hasStableIds);
    }

    private void initFirebaseReferences(){
        firebaseAuth = FirebaseAuth.getInstance();
        followingCollections = FirebaseFirestore.getInstance().collection(Constants.COLLECTION_RELATIONS);
        postCollections = FirebaseFirestore.getInstance().collection(Constants.POSTS);
        postCountQuery = postCollections;
        usersCollection = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);

    }

    @NonNull
    @Override
    public CollectionViewHolder onCreateViewHolder(final @NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_explore_collections, parent, false);
        return new CollectionViewHolder(view );
    }

    @Override
    public void onBindViewHolder(@NonNull final CollectionViewHolder holder, int position) {
        final Collection collection = getSnapshot(holder.getAdapterPosition()).toObject(Collection.class);
        final String collectionId = collection.getCollection_id();
        final String userId = collection.getUser_id();

        try {
            if (position == getItemCount() - 1){
                mBottomReachedListener.onBottomReached(position);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

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
            holder.mCollectionsNoteTextView.setText(collection.getNote());
        }else {
            holder.mCollectionsNoteTextView.setVisibility(View.GONE);
        }


        holder.mCollectionCoverImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, CollectionPostsActivity.class);
                intent.putExtra(CollectionsAdapter.COLLECTION_ID, collectionId);
                intent.putExtra(CollectionsAdapter.EXTRA_USER_UID, userId);
                mContext.startActivity(intent);
            }
        });

        usersCollection.document(firebaseAuth.getCurrentUser().getUid())
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                        if (e != null) {
                            android.util.Log.w(TAG, "Listen error", e);
                            return;
                        }

                        if (documentSnapshot.exists()){
                            final Andeqan andeqan = documentSnapshot.toObject(Andeqan.class);
                            final String profileImage = andeqan.getProfile_image();
                            Glide.with(mContext)
                                    .load(profileImage)
                                    .apply(new RequestOptions()
                                            .placeholder(R.drawable.ic_user_white)
                                            .diskCacheStrategy(DiskCacheStrategy.DATA))
                                    .into(holder.mProfileImageView);
                            holder.mUsernameTextView.setText(andeqan.getUsername());
                        }
                    }
                });

        postCountQuery.whereEqualTo("collection_id", collectionId)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot documentSnapshots,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (!documentSnapshots.isEmpty()){
                    int count = documentSnapshots.size();
                    holder.mPostCountTextView.setText("Post " + count);
                }else {
                    holder.mPostCountTextView.setText("Posts 0");
                }
            }
        });


        /**show if the user is following collection or not**/
        followingCollections.document("following")
                .collection(collectionId).whereEqualTo("following_id", firebaseAuth.getCurrentUser().getUid())
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot documentSnapshots,
                                        @Nullable FirebaseFirestoreException e) {

                        if (e != null) {
                            Log.w(TAG, "Listen error", e);
                            return;
                        }

                        if (!documentSnapshots.isEmpty()){
                            holder.mlikesImageView.setBackgroundResource(R.drawable.ic_heart_fill);
                        }else {
                            holder.mlikesImageView.setBackgroundResource(R.drawable.ic_heart_grey);
                        }

                    }
                });


        /**follow or un follow collection*/
        if (!userId.equals(firebaseAuth.getCurrentUser().getUid())){
            holder.mlikesImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    processFollow = true;
                    followingCollections.document("following")
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
                                            followingCollections.document("following")
                                                    .collection(collectionId).document(firebaseAuth.getCurrentUser().getUid())
                                                    .set(following);
                                            holder.mlikesImageView.setBackgroundResource(R.drawable.ic_heart_fill);

                                            processFollow = false;
                                        }else {
                                            followingCollections.document("following")
                                                    .collection(collectionId).document(firebaseAuth.getCurrentUser()
                                                    .getUid()).delete();
                                            holder.mlikesImageView.setBackgroundResource(R.drawable.ic_heart_grey);

                                            processFollow = false;
                                        }
                                    }
                                }
                            });
                }
            });
        }
    }
}


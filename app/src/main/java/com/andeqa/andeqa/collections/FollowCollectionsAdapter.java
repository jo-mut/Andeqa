package com.andeqa.andeqa.collections;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.R;
import com.andeqa.andeqa.models.Andeqan;
import com.andeqa.andeqa.models.Collection;
import com.andeqa.andeqa.models.QueryOptions;
import com.andeqa.andeqa.models.Relation;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
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

public class FollowCollectionsAdapter extends RecyclerView.Adapter<CollectionViewHolder> {
    private static final String TAG = FeaturedCollectionsAdapter.class.getSimpleName();

    private Context mContext;
    //firestore
    private CollectionReference collectionsCollection;
    private CollectionReference queryParamsCollection;
    private CollectionReference usersCollection;
    private CollectionReference followingCollection;
    private Query postCountQuery;
    //firebase auth
    private FirebaseAuth firebaseAuth;
    private static final String COLLECTION_ID = "collection id";
    private static final String EXTRA_USER_UID = "uid";
    private  static final int MAX_WIDTH = 200;
    private static final int MAX_HEIGHT = 200;
    private boolean processFollow = false;
    private List<DocumentSnapshot> featuredCollections = new ArrayList<>();

    public FollowCollectionsAdapter(Context mContext) {
        this.mContext = mContext;
    }

    protected void setFeaturedCollections(List<DocumentSnapshot> mSnapshots){
        this.featuredCollections = mSnapshots;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return featuredCollections.size();
    }

    protected DocumentSnapshot getSnapshot(int index) {
        return featuredCollections.get(index);
    }



    @NonNull
    @Override
    public CollectionViewHolder onCreateViewHolder(final @NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_collections, parent, false);
        return new CollectionViewHolder(view );
    }

    @Override
    public void onBindViewHolder(final @NonNull CollectionViewHolder holder, int position) {
        final Collection collection = getSnapshot(position).toObject(Collection.class);
        final String collectionId = collection.getCollection_id();
        final String userId = collection.getUser_id();

        firebaseAuth = FirebaseAuth.getInstance();
        collectionsCollection = FirebaseFirestore.getInstance().collection(Constants.COLLECTIONS_POSTS);
        postCountQuery = collectionsCollection.document("collections").collection(collectionId)
                .orderBy("collection_id");
        followingCollection = FirebaseFirestore.getInstance().collection(Constants.COLLECTION_RELATIONS);
        queryParamsCollection = FirebaseFirestore.getInstance().collection(Constants.QUERY_OPTIONS);
        usersCollection = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);

        Glide.with(mContext.getApplicationContext())
                .asBitmap()
                .load(collection.getImage())
                .apply(new RequestOptions()
                        .placeholder(R.drawable.post_placeholder)
                        .diskCacheStrategy(DiskCacheStrategy.DATA))
                .listener(new RequestListener<Bitmap>() {
                    @Override
                    public boolean onLoadFailed(@android.support.annotation.Nullable GlideException e,
                                                Object model, Target<Bitmap> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, Object model,
                                                   Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                        if (resource != null){
                            int colorPalette;
                            Palette.from(resource).generate(new Palette.PaletteAsyncListener() {
                                @Override
                                public void onGenerated(@NonNull Palette palette) {
                                    try {
                                        Palette.Swatch swatch = palette.getVibrantSwatch();
                                        holder.collectionDetailsLinearLayout.setBackgroundColor(swatch.getRgb());
                                    }catch (Exception e){

                                    }
                                }
                            });
                        }
                        return false;
                    }
                })
                .into(holder.mCollectionCoverImageView);

        if (!TextUtils.isEmpty(collection.getName())){
            holder.mCollectionNameTextView.setText(collection.getName());
        }else {
            holder.mCollectionNameTextView.setText("");
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
            holder.mCollectionsNoteTextView.setText("");
        }


        holder.mCollectionsLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, CollectionPostsActivity.class);
                intent.putExtra(FollowCollectionsAdapter.COLLECTION_ID, collectionId);
                intent.putExtra(FollowCollectionsAdapter.EXTRA_USER_UID, userId);
                mContext.startActivity(intent);
            }
        });

        usersCollection.document(userId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (documentSnapshot.exists()){
                    Andeqan andeqan = documentSnapshot.toObject(Andeqan.class);
                    holder.usernameTextView.setText(andeqan.getUsername());
                    Glide.with(mContext.getApplicationContext())
                            .load(andeqan.getProfile_image())
                            .apply(new RequestOptions()
                                    .placeholder(R.drawable.ic_user_white)
                                    .diskCacheStrategy(DiskCacheStrategy.DATA))
                            .into(holder.profileImageView);
                }
            }
        });

        /**show if the user is following collection or not**/
        followingCollection.document("following")
                .collection(firebaseAuth.getCurrentUser().getUid())
                .whereEqualTo("followed_id", collectionId)
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
                            holder.followButton.setText("FOLLOW");
                        }

                    }
                });

        /**follow or un follow collection*/
        if (userId.equals(firebaseAuth.getCurrentUser().getUid())){
            holder.followButton.setVisibility(View.GONE);
        }else {
            holder.followButton.setVisibility(View.VISIBLE);
            holder.followButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    processFollow = true;
                    followingCollection.document("following")
                            .collection(firebaseAuth.getCurrentUser().getUid())
                            .whereEqualTo("followed_id", collectionId)
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
                                            followingCollection.document("following").collection(firebaseAuth
                                                    .getCurrentUser().getUid()).document(collectionId).set(following);

                                            final String id = queryParamsCollection.document().getId();
                                            QueryOptions queryOptions = new QueryOptions();
                                            queryOptions.setUser_id(userId);
                                            queryOptions.setQuery_option(collectionId);
                                            queryOptions.setOption_id(id);
                                            queryParamsCollection.document("options")
                                                    .collection(firebaseAuth.getCurrentUser().getUid()).document(collectionId)
                                                    .set(queryOptions);

                                            holder.followButton.setText("FOLLOWING");
                                            processFollow = false;
                                        }else {
                                            followingCollection.document("following").collection(firebaseAuth.getCurrentUser().getUid())
                                                    .document(collectionId).delete();
                                            queryParamsCollection.document("options").collection(firebaseAuth.getCurrentUser().getUid())
                                                    .document(collectionId).delete();
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
}
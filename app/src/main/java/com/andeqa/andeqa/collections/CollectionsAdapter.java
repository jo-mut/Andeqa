package com.andeqa.andeqa.collections;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.R;
import com.andeqa.andeqa.models.Collection;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

public class CollectionsAdapter extends RecyclerView.Adapter<CollectionViewHolder> {
    private static final String TAG = CollectionsAdapter.class.getSimpleName();

    private Context mContext;
    //firestore
    private CollectionReference collectionsCollection;
    private Query postCountQuery;
    //firebase auth
    private FirebaseAuth firebaseAuth;
    private static final String COLLECTION_ID = "collection id";
    private static final String EXTRA_USER_UID = "uid";
    private  static final int MAX_WIDTH = 200;
    private static final int MAX_HEIGHT = 200;
    private List<DocumentSnapshot> featuredCollections = new ArrayList<>();

    public CollectionsAdapter(Context mContext) {
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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.collections_layout, parent, false);
        return new CollectionViewHolder(view );
    }

    @Override
    public void onBindViewHolder(final @NonNull CollectionViewHolder holder, int position) {
        final Collection collection = getSnapshot(position).toObject(Collection.class);
        final String collectionId = collection.getCollection_id();
        final String uid = collection.getUser_id();

        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null){
            collectionsCollection = FirebaseFirestore.getInstance().collection(Constants.COLLECTIONS_POSTS);
            postCountQuery = collectionsCollection.document("collections").collection(collectionId)
                    .orderBy("collection_id");
        }

        holder.mCollectionNameTextView.setText(collection.getName());

        //prevent collection note from overlapping other layouts
        final String [] strings = collection.getNote().split("");

        final int size = strings.length;

        if (size <= 50){
            //setence will not have read more
            holder.mCollectionsNoteTextView.setText(collection.getNote());
        }else {
            holder.mCollectionsNoteTextView.setText(collection.getNote().substring(0, 49) + "...");
        }


        if (collection.getImage() != null){
            Picasso.with(mContext)
                    .load(collection.getImage())
                    .resize(MAX_WIDTH, MAX_HEIGHT)
                    .onlyScaleDown()
                    .centerCrop()
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .into(holder.mCollectionCoverImageView, new Callback() {
                        @Override
                        public void onSuccess() {
                            Picasso.with(mContext)
                                    .load(collection.getImage())
                                    .resize(MAX_WIDTH, MAX_HEIGHT)
                                    .onlyScaleDown()
                                    .centerCrop()
                                    .into(holder.mCollectionCoverImageView);
                        }

                        @Override
                        public void onError() {

                        }
                    });

        }else {
            holder.mCollectionCoverImageView.setImageBitmap(null);
        }

        postCountQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots,
                                @Nullable FirebaseFirestoreException e) {

                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (!queryDocumentSnapshots.isEmpty()){
                    holder.postCountTextVew.setText(queryDocumentSnapshots.size() + " posts" );
                }else {
                    holder.postCountTextVew.setText("0 posts");
                }
            }
        });

        holder.mCollectionsLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, CollectionPostsActivity.class);
                intent.putExtra(CollectionsAdapter.COLLECTION_ID, collectionId);
                intent.putExtra(CollectionsAdapter.EXTRA_USER_UID, uid);
                mContext.startActivity(intent);
            }
        });



    }

}


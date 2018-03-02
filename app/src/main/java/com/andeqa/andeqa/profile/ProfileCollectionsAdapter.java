package com.andeqa.andeqa.profile;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.andeqa.andeqa.R;
import com.andeqa.andeqa.creation.CreatePostActivity;
import com.andeqa.andeqa.firestore.FirestoreAdapter;
import com.andeqa.andeqa.models.Collection;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.Query;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

/**
 * Created by J.EL on 2/28/2018.
 */

public class ProfileCollectionsAdapter extends FirestoreAdapter<CollectionViewHolder> {
    private static final String TAG = ProfileCollectionsAdapter.class.getSimpleName();
    private Context mContext;
    //firestore
    private CollectionReference collectionsCollection;
    //firebase auth
    private FirebaseAuth firebaseAuth;
    private static final String COLLECTION_ID = "collection id";
    private  static final int MAX_WIDTH = 200;
    private static final int MAX_HEIGHT = 200;

    public ProfileCollectionsAdapter(Query query, Context mContext) {
        super(query);
        this.mContext = mContext;
    }

    @Override
    public void onBindViewHolder(final CollectionViewHolder holder, int position) {
        final Collection collection = getSnapshot(position).toObject(Collection.class);
        final String collectionId = collection.getPushId();
        Log.d("collection id", collectionId);

        holder.mCollectionNameTextView.setText(collection.getName());

        if (collection.getImage() != null){
            Picasso.with(mContext)
                    .load(collection.getImage())
                    .resize(MAX_WIDTH, MAX_HEIGHT)
                    .onlyScaleDown()
                    .centerCrop()
                    .placeholder(R.drawable.profle_image_background)
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .into(holder.mCollectionCoverImageView, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {
                            Picasso.with(mContext)
                                    .load(collection.getImage())
                                    .resize(MAX_WIDTH, MAX_HEIGHT)
                                    .onlyScaleDown()
                                    .centerCrop()
                                    .placeholder(R.drawable.profle_image_background)
                                    .into(holder.mCollectionCoverImageView);

                        }
                    });
        }

        holder.mCollectionsLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, CreatePostActivity.class);
                intent.putExtra(ProfileCollectionsAdapter.COLLECTION_ID, collectionId);
                mContext.startActivity(intent);
            }
        });


    }

    @Override
    public CollectionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.collections_layout, parent, false);
        return new CollectionViewHolder(view );
    }
}

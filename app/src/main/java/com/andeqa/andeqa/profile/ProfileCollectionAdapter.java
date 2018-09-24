package com.andeqa.andeqa.profile;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.R;
import com.andeqa.andeqa.collections.MineCollectionsAdapter;
import com.andeqa.andeqa.collections.MinePostsActivity;
import com.andeqa.andeqa.models.Collection;
import com.andeqa.andeqa.models.Relation;
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
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

public class ProfileCollectionAdapter extends RecyclerView.Adapter
        <ProfileCollectionAdapter.ProfileCollectionsViewHolder> {
    private static final String TAG = MineCollectionsAdapter.class.getSimpleName();
    private Context mContext;
    //firestore
    private CollectionReference collectionsCollection;
    private CollectionReference followingCollection;
    private Query postCountQuery;
    //firebase auth
    private FirebaseAuth firebaseAuth;
    private static final String COLLECTION_ID = "collection id";
    private static final String EXTRA_USER_UID = "uid";
    private  static final int MAX_WIDTH = 200;
    private static final int MAX_HEIGHT = 200;
    private boolean processFollow = false;
    private List<DocumentSnapshot> profileCollections = new ArrayList<>();

    public ProfileCollectionAdapter(Context mContext) {
        this.mContext = mContext;
    }

    @Override
    public int getItemCount() {
        return profileCollections.size();
    }

    protected void setProfileCollections(List<DocumentSnapshot> mSnapshots){
        this.profileCollections = mSnapshots;
        notifyDataSetChanged();
    }

    protected DocumentSnapshot getSnapshot(int index) {
        return profileCollections.get(index);
    }


    @Override
    public void onBindViewHolder(final ProfileCollectionAdapter.ProfileCollectionsViewHolder holder, int position) {
        final Collection collection = getSnapshot(position).toObject(Collection.class);
        final String collectionId = collection.getCollection_id();
        final String userId = collection.getUser_id();

        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null){
            followingCollection = FirebaseFirestore.getInstance().collection(Constants.COLLECTION_RELATIONS);
            collectionsCollection = FirebaseFirestore.getInstance().collection(Constants.COLLECTIONS_POSTS);
            postCountQuery = collectionsCollection.document("collections").collection(collectionId)
                    .orderBy("collection_id");
        }

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


        Glide.with(mContext.getApplicationContext())
                .load(collection.getImage())
                .apply(new RequestOptions()
                        .diskCacheStrategy(DiskCacheStrategy.DATA))
                .into(holder.mCollectionCoverImageView);


        holder.mCollectionsLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, MinePostsActivity.class);
                intent.putExtra(ProfileCollectionAdapter.COLLECTION_ID, collectionId);
                intent.putExtra(ProfileCollectionAdapter.EXTRA_USER_UID, userId);
                mContext.startActivity(intent);
            }
        });

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

                        if (!documentSnapshots.isEmpty()) {
                            holder.followButton.setText("FOLLOWING");
                        } else {
                            holder.followButton.setText("FOLLOW");
                        }

                    }
                });

        /**show the number of peopl following collection**/
        followingCollection.document("following")
                .collection(collectionId)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot documentSnapshots,
                                        @Nullable FirebaseFirestoreException e) {

                        if (e != null) {
                            Log.w(TAG, "Listen error", e);
                            return;
                        }

                        if (!documentSnapshots.isEmpty()) {
                            holder.followingCountTextView.setVisibility(View.VISIBLE);
                            int following = documentSnapshots.size();
                            holder.followingCountTextView.setText(following + " following");
                        } else {
                            holder.followingCountTextView.setVisibility(View.GONE);
                        }

                    }
                });


        /**follow or un follow collection*/
        if (userId.equals(firebaseAuth.getCurrentUser().getUid())) {
            holder.followRelativeLayout.setVisibility(View.GONE);
        } else {
            holder.followRelativeLayout.setVisibility(View.VISIBLE);
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

                                    if (processFollow) {
                                        if (documentSnapshots.isEmpty()) {
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
                                        } else {
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

    @Override
    public ProfileCollectionAdapter.ProfileCollectionsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_profile_collection, parent, false);
        return new ProfileCollectionAdapter.ProfileCollectionsViewHolder(view );
    }

    public static class ProfileCollectionsViewHolder extends RecyclerView.ViewHolder {

        View mView;
        Context mContext;
        public ImageView mCollectionCoverImageView;
        public Button followButton;
        public LinearLayout followRelativeLayout;
        public TextView mCollectionNameTextView;
        public TextView mCollectionsNoteTextView;
        public LinearLayout mCollectionsLinearLayout;
        public TextView followingCountTextView;


        public ProfileCollectionsViewHolder(View itemView) {
            super(itemView);
            mContext = itemView.getContext();
            mView = itemView;
            mCollectionCoverImageView = (ImageView) mView.findViewById(R.id.collectionCoverImageView);
            mCollectionNameTextView = (TextView) mView.findViewById(R.id.collectionNameTextView);
            mCollectionsNoteTextView = (TextView) mView.findViewById(R.id.collectionsNoteTextView);
            mCollectionsLinearLayout = (LinearLayout) mView.findViewById(R.id.collectionLinearLayout);
            followButton = (Button) mView.findViewById(R.id.followButton);
            followingCountTextView  = (TextView) mView.findViewById(R.id.followingCountTextView);
            followRelativeLayout = (LinearLayout) mView.findViewById(R.id.followRelativeLayout);


        }

    }
}

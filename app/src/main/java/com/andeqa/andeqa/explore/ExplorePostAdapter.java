package com.andeqa.andeqa.explore;

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
import com.andeqa.andeqa.home.PostDetailActivity;
import com.andeqa.andeqa.models.CollectionPost;
import com.andeqa.andeqa.models.Credit;
import com.andeqa.andeqa.models.Market;
import com.andeqa.andeqa.models.Post;
import com.andeqa.andeqa.models.Wallet;
import com.andeqa.andeqa.utils.RoundedTransform;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class ExplorePostAdapter extends RecyclerView.Adapter<ExploreViewHolder> {
    private static final String TAG = ExplorePostAdapter.class.getSimpleName();
    private Context mContext;
    private List<Market> markets = new ArrayList<>();
    //firestore
    private CollectionReference collectionsPosts;
    private CollectionReference postsCollection;
    private CollectionReference usersReference;
    private CollectionReference creditsReference;
    private Query creditQuery;

    //adapters
    private FirebaseAuth firebaseAuth;
    private static final String KEY_LAYOUT_POSITION = "layout pooition";
    private static final String EXTRA_POST_ID = "post id";
    private static final String EXTRA_USER_UID = "uid";
    private static final String COLLECTION_ID = "collection id";
    private static final String TYPE = "type";
    private static final int MAX_WIDTH = 200;
    private static final int MAX_HEIGHT = 200;
    private List<DocumentSnapshot> documentSnapshots = new ArrayList<>();

    public ExplorePostAdapter(Context mContext) {
        this.mContext = mContext;
    }

    protected void setBestPosts(List<DocumentSnapshot> mSnapshots){
        this.documentSnapshots = mSnapshots;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return documentSnapshots.size();
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    public DocumentSnapshot getSnapshot(int index) {
        return documentSnapshots.get(index);
    }



    @NonNull
    @Override
    public ExploreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.explore_layout, parent, false);
        return new ExploreViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final @NonNull ExploreViewHolder holder, int position) {
        Credit credit = getSnapshot(position).toObject(Credit.class);
        final String postId = credit.getPost_id();
        final String uid = credit.getUser_id();

        /**initialize firebase auth*/
        firebaseAuth = FirebaseAuth.getInstance();

        /**collections references*/
        if (firebaseAuth.getCurrentUser() != null){
            postsCollection = FirebaseFirestore.getInstance().collection(Constants.POSTS);
            collectionsPosts = FirebaseFirestore.getInstance().collection(Constants.COLLECTIONS_POSTS);
        }

        postsCollection.document(postId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (documentSnapshot.exists()){
                    final Post post = documentSnapshot.toObject(Post.class);
                    final String collectionId = post.getCollection_id();
                    final String type = post.getType();

                    holder.postImageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(mContext, PostDetailActivity.class);
                            intent.putExtra(ExplorePostAdapter.EXTRA_POST_ID, postId);
                            intent.putExtra(ExplorePostAdapter.COLLECTION_ID, collectionId);
                            intent.putExtra(ExplorePostAdapter.EXTRA_USER_UID, uid);
                            intent.putExtra(ExplorePostAdapter.TYPE, type);
                            mContext.startActivity(intent);
                        }
                    });


                    collectionsPosts.document("collections").collection(collectionId)
                            .document(postId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                            if (e != null) {
                                Log.w(TAG, "Listen error", e);
                                return;
                            }

                            if (documentSnapshot.exists()){
                                final CollectionPost collectionPost = documentSnapshot.toObject(CollectionPost.class);

                                Picasso.with(mContext)
                                        .load(collectionPost.getImage())
                                        .networkPolicy(NetworkPolicy.OFFLINE)
                                        .placeholder(R.drawable.image_place_holder)
                                        .into(holder.postImageView, new Callback() {
                                            @Override
                                            public void onSuccess() {

                                            }

                                            @Override
                                            public void onError() {
                                                Picasso.with(mContext)
                                                        .load(collectionPost.getImage())
                                                        .placeholder(R.drawable.image_place_holder)
                                                        .into(holder.postImageView, new Callback() {
                                                            @Override
                                                            public void onSuccess() {

                                                            }

                                                            @Override
                                                            public void onError() {
                                                                Log.v("Picasso", "Could not fetch image");
                                                            }
                                                        });


                                            }
                                        });


                            }
                        }
                    });


                }
            }
        });

    }

}

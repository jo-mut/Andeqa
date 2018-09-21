package com.andeqa.andeqa.search;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintSet;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.R;
import com.andeqa.andeqa.chatting.MessagingActivity;
import com.andeqa.andeqa.collections.CollectionPostsActivity;
import com.andeqa.andeqa.collections.CollectionViewHolder;
import com.andeqa.andeqa.comments.CommentsActivity;
import com.andeqa.andeqa.home.PhotoPostViewHolder;
import com.andeqa.andeqa.home.PostDetailActivity;
import com.andeqa.andeqa.impressions.ImpressionTracker;
import com.andeqa.andeqa.models.Andeqan;
import com.andeqa.andeqa.models.Collection;
import com.andeqa.andeqa.models.Post;
import com.andeqa.andeqa.models.QueryOptions;
import com.andeqa.andeqa.models.Relation;
import com.andeqa.andeqa.models.Room;
import com.andeqa.andeqa.models.Search;
import com.andeqa.andeqa.profile.ProfileActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;

import javax.annotation.Nullable;

public class SearchOverallAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = SearchOverallAdapter.class.getSimpleName();
    private FirebaseAuth firebaseAuth;
    private Context mContext;
    private CollectionReference followingCollection;
    private CollectionReference postsCollection;
    private CollectionReference usersCollection;
    private CollectionReference roomsCollection;
    private DatabaseReference databaseReference;
    //firestore reference
    private CollectionReference queryParamsCollection;
    private CollectionReference collectionsPosts;
    private com.google.firebase.firestore.Query commentsCountQuery;
    private CollectionReference usersReference;
    private CollectionReference commentsReference;
    private CollectionReference postsCollections;
    private CollectionReference likesReference;
    private DatabaseReference impressionReference;
    private CollectionReference timelineCollection;
    private CollectionReference collectionsCollection;
    private boolean processFollow = false;
    private boolean processRoom = false;
    private static final String EXTRA_USER_UID = "uid";
    private static final String EXTRA_ROOM_UID = "roomId";
    private static final int PEOPLE=1;
    private static final int POST =2;
    private static final int COLLECTION =3;
    private static final int EMPTY =3;
    private String roomId;
    private static final String POST_HEIGHT = "height";
    private static final String POST_WIDTH = "width";
    private static final String EXTRA_POST_ID = "post id";
    private static final String COLLECTION_ID = "collection id";
    private static final String VIDEO = "video";
    private static final String TYPE = "type";
    private ConstraintSet constraintSet;
    private List<Search> documentSnapshots = new ArrayList<>();
    private ImpressionTracker impressionTracker;
    private final WeakHashMap<View, Integer> mViewPositionMap = new WeakHashMap<>();




    public SearchOverallAdapter(Context mContext) {
        this.mContext = mContext;
        iniReferences();
    }

    protected void setResults(List<Search> mSnapshots){
        this.documentSnapshots = mSnapshots;
        notifyDataSetChanged();

        Log.d("search results", mSnapshots.size() + "");
    }

    private void iniReferences(){
        firebaseAuth = FirebaseAuth.getInstance();
        usersCollection = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);
        roomsCollection = FirebaseFirestore.getInstance().collection(Constants.MESSAGES);
        databaseReference = FirebaseDatabase.getInstance().getReference(Constants.RANDOM_PUSH_ID);
        postsCollection = FirebaseFirestore.getInstance().collection(Constants.POSTS);
        collectionsCollection = FirebaseFirestore.getInstance().collection(Constants.USER_COLLECTIONS);
        queryParamsCollection = FirebaseFirestore.getInstance().collection(Constants.QUERY_OPTIONS);
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    @Override
    public int getItemCount() {
        return documentSnapshots.size();
    }

    @Override
    public int getItemViewType(int position) {
        Search search = documentSnapshots.get(position);
        final String type = search.getType();

        if (type != null &&  type.equals("person")){
            return PEOPLE;
        }else if ( type != null &&  type.equals("post")){
            return POST;
        }else if (type != null && type.equals("collection")){
            return COLLECTION;
        }else {
            return EMPTY;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;

        switch (viewType){
            case PEOPLE:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.layout_people, parent, false);
                return  new SearchPeopleViewHolder(view);
            case POST:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.layout_image_post, parent, false);
                return new PhotoPostViewHolder(view);
            case COLLECTION:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.layout_collections, parent, false);
                return new CollectionViewHolder(view);
        }

        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        switch (holder.getItemViewType()){
            case PEOPLE:
                populatePeople((SearchPeopleViewHolder)holder, position);
                break;
            case POST:
                populatePosts((PhotoPostViewHolder)holder, position);
                break;
            case COLLECTION:
                populateCollections((CollectionViewHolder)holder, position);
                break;
        }

    }

    private void populatePeople(final SearchPeopleViewHolder holder, int position){
        Search search = documentSnapshots.get(position);
        final String userId = search.getId();

        usersCollection.document(userId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (documentSnapshot.exists()){
                    Andeqan andeqan = documentSnapshot.toObject(Andeqan.class);
                    final String userId = andeqan.getUser_id();
                    final String username = andeqan.getUsername();
                    final String profileImage = andeqan.getProfile_image();
                    final String firstName = andeqan.getFirst_name();
                    final String secondName = andeqan.getSecond_name();


                    holder.usernameTextView.setText(username);
                    holder.fullNameTextView.setText(firstName + " " +  secondName);
                    Glide.with(mContext.getApplicationContext())
                            .load(profileImage)
                            .apply(new RequestOptions()
                                    .placeholder(R.drawable.ic_user)
                                    .diskCacheStrategy(DiskCacheStrategy.DATA))
                            .into(holder.profileImageView);

                    holder.profileImageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //look to see if current user has a chat history with mUid
                            processRoom = true;
                            roomsCollection.document(userId).collection("last message")
                                    .document(firebaseAuth.getCurrentUser().getUid())
                                    .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                        @Override
                                        public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                                            if (e != null) {
                                                Log.w(TAG, "Listen error", e);
                                                return;
                                            }

                                            if (processRoom){
                                                if (documentSnapshot.exists()){
                                                    Room room = documentSnapshot.toObject(Room.class);
                                                    roomId = room.getRoom_id();
                                                    Intent intent = new Intent(mContext, MessagingActivity.class);
                                                    intent.putExtra(SearchOverallAdapter.EXTRA_ROOM_UID, roomId);
                                                    intent.putExtra(SearchOverallAdapter.EXTRA_USER_UID, userId);
                                                    mContext.startActivity(intent);
                                                    processRoom = false;
                                                }else {
                                                    roomsCollection.document(firebaseAuth.getCurrentUser().getUid())
                                                            .collection("last message")
                                                            .document(userId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                                        @Override
                                                        public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                                                            if (e != null) {
                                                                Log.w(TAG, "Listen error", e);
                                                                return;
                                                            }

                                                            if (processRoom){
                                                                if (documentSnapshot.exists()){
                                                                    Room room = documentSnapshot.toObject(Room.class);
                                                                    roomId = room.getRoom_id();
                                                                    Intent intent = new Intent(mContext, MessagingActivity.class);
                                                                    intent.putExtra(SearchOverallAdapter.EXTRA_ROOM_UID, roomId);
                                                                    intent.putExtra(SearchOverallAdapter.EXTRA_USER_UID, userId);
                                                                    mContext.startActivity(intent);

                                                                    processRoom = false;

                                                                }else {
                                                                    //start a chat with mUid since they have no chatting history
                                                                    roomId = databaseReference.push().getKey();
                                                                    Intent intent = new Intent(mContext, MessagingActivity.class);
                                                                    intent.putExtra(SearchOverallAdapter.EXTRA_ROOM_UID, roomId);
                                                                    intent.putExtra(SearchOverallAdapter.EXTRA_USER_UID, userId);
                                                                    mContext.startActivity(intent);
                                                                    processRoom = false;
                                                                }
                                                            }
                                                        }
                                                    });
                                                }
                                            }

                                        }
                                    });

                        }
                    });

                }
            }
        });

    }


    private void populatePosts(final PhotoPostViewHolder holder, final int position){
//        Search search = documentSnapshots.get(position);
//        final int count = search.getCount();
//        final String word = search.getWord();
//        holder.searchWordTextView.setText(word);
//        holder.searchCountTextVieww.setText( "posts " +count);


        Search search = documentSnapshots.get(position);
        final String postId = search.getId();

        postsCollections.document(postId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {

                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (documentSnapshot.exists()){
                    final Post post = documentSnapshot.toObject(Post.class);
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

                    //calculate view visibility and add visible views to impression tracker
                    mViewPositionMap.put(holder.itemView, position);
                    impressionTracker.addView(holder.itemView, 100, postId);

                    Glide.with(mContext.getApplicationContext())
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
                            holder.captionLinearLayout.setVisibility(View.VISIBLE);
                            holder.descriptionRelativeLayout.setVisibility(View.VISIBLE);
                            final String boldMore = "...";
                            String normalText = post.getDescription().substring(0, 49);
                            holder.descriptionTextView.setText(normalText + boldMore);
                        }
                    }else {
                        holder.captionLinearLayout.setVisibility(View.GONE);
                    }

                    holder.mCommentsLinearLayout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent =  new Intent(mContext, CommentsActivity.class);
                            intent.putExtra(SearchOverallAdapter.EXTRA_POST_ID, postId);
                            intent.putExtra(SearchOverallAdapter.COLLECTION_ID, collectionId);
                            intent.putExtra(SearchOverallAdapter.TYPE, type);
                            mContext.startActivity(intent);
                        }
                    });

                    if (post.getWidth() != null && post.getHeight() != null){
                        holder.postImageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent =  new Intent(mContext, PostDetailActivity.class);
                                intent.putExtra(SearchOverallAdapter.EXTRA_POST_ID, postId);
                                intent.putExtra(SearchOverallAdapter.COLLECTION_ID, collectionId);
                                intent.putExtra(SearchOverallAdapter.EXTRA_USER_UID, uid);
                                intent.putExtra(SearchOverallAdapter.TYPE, type);
                                intent.putExtra(SearchOverallAdapter.POST_HEIGHT, post.getHeight());
                                intent.putExtra(SearchOverallAdapter.POST_WIDTH, post.getWidth());
                                mContext.startActivity(intent);
                            }
                        });
                    }else {
                        holder.postImageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent =  new Intent(mContext, PostDetailActivity.class);
                                intent.putExtra(SearchOverallAdapter.EXTRA_POST_ID, postId);
                                intent.putExtra(SearchOverallAdapter.COLLECTION_ID, collectionId);
                                intent.putExtra(SearchOverallAdapter.EXTRA_USER_UID, uid);
                                intent.putExtra(SearchOverallAdapter.TYPE, type);
                                mContext.startActivity(intent);
                            }
                        });

                    }

                    holder.profileImageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(mContext, ProfileActivity.class);
                            intent.putExtra(SearchOverallAdapter.EXTRA_USER_UID, uid);
                            mContext.startActivity(intent);
                        }
                    });


//        //calculate the generated points from the compiled time
//        impressionReference.child("compiled_views").child(postId)
//                .addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                if (dataSnapshot.exists()){
//                    holder.mCreditsLinearLayout.setVisibility(View.VISIBLE);
//                    ViewDuration impression = dataSnapshot.getValue(ViewDuration.class);
//                    final long compiledDuration = impression.getCompiled_duration();
//                    Log.d("compiled duration", compiledDuration + "");
//                    //get seconds in milliseconds
//                    final long durationInSeconds = compiledDuration / 1000;
//                    //get the points generate
//                    final double points = durationInSeconds * 0.000001;
//                    DecimalFormat formatter = new DecimalFormat("0.0000");
//                    final String pts = formatter.format(points);
//                    holder.senseCreditsTextView.setText(pts + " points");
//
//                }else {
//                    holder.mCreditsLinearLayout.setVisibility(View.VISIBLE);
//                    final double points = 0.00;
//                    DecimalFormat formatter = new DecimalFormat("0.00");
//                    final String pts = formatter.format(points);
//                    holder.senseCreditsTextView.setText(pts + " points");
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });

                    impressionReference.child("post_views").child(postId)
                            .addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()){
                                        final long size = dataSnapshot.getChildrenCount();
                                        int childrenCount = (int) size;
                                        holder.viewsCountTextView.setText(childrenCount + "");
                                    }else {
                                        holder.viewsCountTextView.setText("0");
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

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
                                Glide.with(mContext.getApplicationContext())
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

            }
        });


    }

    private void populateCollections(final CollectionViewHolder holder, final int position){
//        Search search = documentSnapshots.get(position);
//        final int count = search.getCount();
//        final String word = search.getWord();
//        holder.searchWordTextView.setText(word);
//        holder.searchCountTextVieww.setText( "collections " + count);

        Search search = documentSnapshots.get(position);
        final String collectionId = search.getId();

        collectionsCollection.document(collectionId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {

                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (documentSnapshot.exists()){
                    final Collection collection = documentSnapshot.toObject(Collection.class);
                    final String userId = collection.getUser_id();
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
                            intent.putExtra(SearchOverallAdapter.COLLECTION_ID, collectionId);
                            intent.putExtra(SearchOverallAdapter.EXTRA_USER_UID, userId);
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
                                                .placeholder(R.drawable.ic_user)
                                                .diskCacheStrategy(DiskCacheStrategy.DATA))
                                        .into(holder.profileImageView);
                            }
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

                                    if (!documentSnapshots.isEmpty()){
                                        holder.followButton.setText("FOLLOWING");
                                    }else {
                                        holder.followButton.setText("FOLLOW");
                                    }

                                }
                            });

                    /**show the number of peopl following collection**/
                    followingCollection.document("following")
                            .collection(collectionId).whereEqualTo("followed_id",collectionId)
                            .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                @Override
                                public void onEvent(@Nullable QuerySnapshot documentSnapshots,
                                                    @Nullable FirebaseFirestoreException e) {

                                    if (e != null) {
                                        Log.w(TAG, "Listen error", e);
                                        return;
                                    }

                                    if (!documentSnapshots.isEmpty()){
                                        holder.followingCountTextView.setVisibility(View.VISIBLE);
                                        int following = documentSnapshots.size();
                                        holder.followingCountTextView.setText(following + " following");
                                    }else {
                                        holder.followingCountTextView.setVisibility(View.GONE);
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
                                                        followingCollection.document("following")
                                                                .collection(firebaseAuth.getCurrentUser().getUid())
                                                                .document(collectionId).set(following);

                                                        final String id = queryParamsCollection.document().getId();
                                                        QueryOptions queryOptions = new QueryOptions();
                                                        queryOptions.setUser_id(userId);
                                                        queryOptions.setFollowed_id(collectionId);
                                                        queryOptions.setType("collection");
                                                        queryParamsCollection.document("options")
                                                                .collection(firebaseAuth.getCurrentUser().getUid()).document(collectionId)
                                                                .set(queryOptions);

                                                        holder.followButton.setText("FOLLOWING");
                                                        processFollow = false;
                                                    }else {
                                                        followingCollection.document("following")
                                                                .collection(firebaseAuth.getCurrentUser().getUid())
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
        });

    }

    public void cleanUp(){
        documentSnapshots.clear();
        notifyDataSetChanged();
    }
}

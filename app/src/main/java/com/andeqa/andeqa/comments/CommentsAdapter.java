package com.andeqa.andeqa.comments;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.R;
import com.andeqa.andeqa.chatting.MessagingActivity;
import com.andeqa.andeqa.models.Andeqan;
import com.andeqa.andeqa.models.Comment;
import com.andeqa.andeqa.models.QueryOptions;
import com.andeqa.andeqa.models.Relation;
import com.andeqa.andeqa.models.Room;
import com.andeqa.andeqa.models.Timeline;
import com.andeqa.andeqa.profile.ProfileActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Nullable;

/**
 * Created by J.EL on 3/23/2018.
 */

public class CommentsAdapter extends RecyclerView.Adapter<CommentViewHolder> {
    private Context mContext;
    private FirebaseAuth firebaseAuth;
    private String mPostId;
    //firebase
    private DatabaseReference databaseReference;
    //firestore
    private Query mQuery;
    private ListenerRegistration mRegistration;
    private CollectionReference usersCollection;
    private CollectionReference followersCollection;
    private CollectionReference timelineCollection;
    private CollectionReference queryParamsCollection;
    private CollectionReference roomsCollection;
    private static final String COLLECTION_ID = "collection id";
    private static final String EXTRA_POST_ID = "post id";
    private static final String EXTRA_USER_UID = "uid";
    private static final String EXTRA_ROOM_UID = "roomId";
    private static final String TAG = CommentsAdapter.class.getSimpleName();
    private boolean processRoom = false;
    private boolean processFollow = false;
    private boolean showOnClick = false;
    private List<DocumentSnapshot> documentSnapshots = new ArrayList<>();
    private String roomId;


    public CommentsAdapter(Context mContext) {
        this.mContext = mContext;
    }

    protected void setPostComments(List<DocumentSnapshot> mSnapshots){
        this.documentSnapshots = mSnapshots;
        notifyDataSetChanged();
        initReferences();
    }

    private void initReferences(){
        queryParamsCollection = FirebaseFirestore.getInstance().collection(Constants.QUERY_OPTIONS);

    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    protected DocumentSnapshot getSnapshot(int index) {
        return documentSnapshots.get(index);
    }


    @Override
    public int getItemCount() {
        return documentSnapshots.size();
    }

    @Override
    public CommentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_comments, parent, false);
        return new CommentViewHolder(view);

    }

    @Override
    public void onBindViewHolder(final CommentViewHolder holder, int position) {
        final Comment comment = getSnapshot(holder.getAdapterPosition()).toObject(Comment.class);
        final String userId = comment.getUser_id();

        firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() != null){
            //firestore
            usersCollection = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);
            followersCollection = FirebaseFirestore.getInstance().collection(Constants.PEOPLE);
            timelineCollection = FirebaseFirestore.getInstance().collection(Constants.TIMELINE);
            roomsCollection = FirebaseFirestore.getInstance().collection(Constants.MESSAGES);
            //firebase
            databaseReference = FirebaseDatabase.getInstance().getReference(Constants.RANDOM_PUSH_ID);

        }

        if (!TextUtils.isEmpty(comment.getComment_text())){
            final String [] strings = comment.getComment_text().split("");

            final int size = strings.length;

            if (size <= 120){
                holder.mCommentTextView.setText(comment.getComment_text());
            }else{

                holder.mCommentTextView.setVisibility(View.VISIBLE);
                final String boldMore = "...";
                final String boldLess = "...";
                String normalText = comment.getComment_text().substring(0, 119);
                holder.mCommentTextView.setText(normalText + boldMore);
                holder.mCommentTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (showOnClick){
                            String normalText = comment.getComment_text();
                            holder.mCommentTextView.setText(normalText + boldLess);
                            showOnClick = false;
                        }else {
                            String normalText = comment.getComment_text().substring(0, 119);
                            holder.mCommentTextView.setText(normalText + boldMore);
                            showOnClick = true;
                        }
                    }
                });
            }
        }

        holder.profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, ProfileActivity.class);
                intent.putExtra(CommentsAdapter.EXTRA_USER_UID, userId);
                mContext.startActivity(intent);
            }
        });

        //get the profile of the user wh just commented
        usersCollection.document(userId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (documentSnapshot.exists()){
                    final Andeqan andeqan = documentSnapshot.toObject(Andeqan.class);
                    final String profileImage = andeqan.getProfile_image();
                    final String username = andeqan.getUsername();

                    holder.usernameTextView.setText(username);
                    Glide.with(mContext.getApplicationContext())
                            .load(profileImage)
                            .apply(new RequestOptions()
                                    .placeholder(R.drawable.ic_user)
                                    .diskCacheStrategy(DiskCacheStrategy.DATA))
                            .into(holder.profileImageView);

                }
            }
        });

        if (userId.equals(firebaseAuth.getCurrentUser().getUid())){
            holder.sendMessageImageView.setVisibility(View.GONE);
        }else {
            holder.mSendMessageRelativeLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
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
                                            intent.putExtra(CommentsAdapter.EXTRA_ROOM_UID, roomId);
                                            intent.putExtra(CommentsAdapter.EXTRA_USER_UID, userId);
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
                                                            intent.putExtra(CommentsAdapter.EXTRA_ROOM_UID, roomId);
                                                            intent.putExtra(CommentsAdapter.EXTRA_USER_UID, userId);
                                                            mContext.startActivity(intent);

                                                            processRoom = false;

                                                        }else {
                                                            //start a chat with mUid since they have no chatting history
                                                            roomId = databaseReference.push().getKey();
                                                            Intent intent = new Intent(mContext, MessagingActivity.class);
                                                            intent.putExtra(CommentsAdapter.EXTRA_ROOM_UID, roomId);
                                                            intent.putExtra(CommentsAdapter.EXTRA_USER_UID, userId);
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


        //show if following or not
        followersCollection.document("followers").collection(userId)
                .whereEqualTo("following_id", firebaseAuth.getCurrentUser().getUid())
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                        if (e != null) {
                            Log.w(TAG, "Listen error", e);
                            return;
                        }

                        if (documentSnapshots.isEmpty()){
                            if (!userId.equals(firebaseAuth.getCurrentUser().getUid())){
                                holder.followButton.setVisibility(View.VISIBLE);
                                holder.followButton.setText("Follow");
                            }
                        }else {
                            if (!userId.equals(firebaseAuth.getCurrentUser().getUid())){
                                holder.followButton.setVisibility(View.VISIBLE);
                                holder.followButton.setText("Following");
                            }
                        }
                    }
                });


        //follow or unfollow
        if (userId.equals(firebaseAuth.getCurrentUser().getUid())){
            holder.followButton.setVisibility(View.GONE);
        }else {
            holder.followButton.setVisibility(View.VISIBLE);
            holder.followButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    processFollow = true;
                    followersCollection.document("followers")
                            .collection(userId)
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
                                            //set followers and following
                                            Relation follower = new Relation();
                                            follower.setFollowing_id(firebaseAuth.getCurrentUser().getUid());
                                            follower.setFollowed_id(userId);
                                            follower.setType("followed_user");
                                            follower.setTime(System.currentTimeMillis());
                                            followersCollection.document("followers").collection(userId)
                                                    .document(firebaseAuth.getCurrentUser().getUid()).set(follower)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            Timeline timeline = new Timeline();
                                                            final long time = new Date().getTime();
                                                            final String postid =  databaseReference.push().getKey();
                                                            timeline.setPost_id(userId);
                                                            timeline.setTime(time);
                                                            timeline.setUser_id(firebaseAuth.getCurrentUser().getUid());
                                                            timeline.setType("followers");
                                                            timeline.setActivity_id(postid);
                                                            timeline.setStatus("un_read");

                                                            timelineCollection.document(userId).collection("activities")
                                                                    .document(firebaseAuth.getCurrentUser().getUid())
                                                                    .set(timeline);
                                                        }
                                                    });


                                            final Relation following = new Relation();
                                            following.setFollowing_id(firebaseAuth.getCurrentUser().getUid());
                                            following.setFollowed_id(userId);
                                            following.setType("following_user");
                                            following.setTime(System.currentTimeMillis());
                                            followersCollection.document("following").collection(firebaseAuth.getCurrentUser().getUid())
                                                    .document(userId).set(following);

                                            if (!userId.equals(firebaseAuth.getCurrentUser().getUid())){
                                                final String id = queryParamsCollection.document().getId();
                                                QueryOptions queryOptions = new QueryOptions();
                                                queryOptions.setUser_id(firebaseAuth.getCurrentUser().getUid());
                                                queryOptions.setFollowed_id(userId);
                                                queryOptions.setType("people");
                                                queryParamsCollection.document("options")
                                                        .collection(firebaseAuth.getCurrentUser().getUid()).document(userId)
                                                        .set(queryOptions);
                                            }

                                            processFollow = false;
                                        }else {
                                            followersCollection.document("followers").collection(userId)
                                                    .document(firebaseAuth.getCurrentUser().getUid()).delete();
                                            followersCollection.document("following").collection(firebaseAuth.getCurrentUser().getUid())
                                                    .document(userId).delete();
                                            queryParamsCollection.document("options")
                                                    .collection(firebaseAuth.getCurrentUser().getUid()).document(userId)
                                                    .delete();
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
    public void onViewRecycled(CommentViewHolder holder) {
        super.onViewRecycled(holder);
    }
}

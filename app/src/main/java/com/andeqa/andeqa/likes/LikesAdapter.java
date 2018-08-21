package com.andeqa.andeqa.likes;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.R;
import com.andeqa.andeqa.message.MessagesAccountActivity;
import com.andeqa.andeqa.models.Andeqan;
import com.andeqa.andeqa.models.Like;
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
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Nullable;


/**
 * Created by J.EL on 4/5/2018.
 */

public class LikesAdapter extends RecyclerView.Adapter<LikesViewHolder> {
    //context
    private Context mContext;
    //firestore
    private CollectionReference followersCollection;
    private CollectionReference usersReference;
    private CollectionReference timelineCollection;
    private CollectionReference roomsCollection;
    //firestore
    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;
    private boolean processFollow = false;
    private boolean processRoom = false;
    private String roomId;
    private static final String TAG = LikesActivity.class.getSimpleName();
    private static final String EXTRA_USER_UID = "uid";
    private static final String EXTRA_ROOM_UID = "roomId";
    private List<DocumentSnapshot> documentSnapshots = new ArrayList<>();


    public LikesAdapter(Context mContext) {
        this.mContext = mContext;
    }

    public void setPostLikes(List<DocumentSnapshot> likes){
        this.documentSnapshots = likes;
        notifyDataSetChanged();
    }

    protected DocumentSnapshot getSnapshot(int index) {
        return documentSnapshots.get(index);
    }



    @Override
    public int getItemCount() {
        return documentSnapshots.size();
    }

    @Override
    public LikesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate
                (R.layout.layout_likes, parent, false);
        return new LikesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final LikesViewHolder holder, int position) {
        Like like = getSnapshot(position).toObject(Like.class);
        final String userId = like.getUser_id();

        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null){

            //firestore
            followersCollection = FirebaseFirestore.getInstance().collection(Constants.PEOPLE);
            usersReference = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);
            timelineCollection = FirebaseFirestore.getInstance().collection(Constants.TIMELINE);
            databaseReference = FirebaseDatabase.getInstance().getReference(Constants.RANDOM_PUSH_ID);
            roomsCollection = FirebaseFirestore.getInstance().collection(Constants.MESSAGES);

        }

        //get the profile of the user who just liked
        usersReference.document(userId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
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
                    final String firstName = andeqan.getFirst_name();
                    final String secondName = andeqan.getSecond_name();


                    holder.usernameTextView.setText(username);
                    holder.fullNameTextView.setText(firstName + " " + secondName);

                    Glide.with(mContext.getApplicationContext())
                            .load(andeqan.getProfile_image())
                            .apply(new RequestOptions()
                                    .placeholder(R.drawable.ic_user)
                                    .diskCacheStrategy(DiskCacheStrategy.DATA))
                            .into(holder.profileImageView);

                }
            }
        });



        holder.profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, ProfileActivity.class);
                intent.putExtra(LikesAdapter.EXTRA_USER_UID, userId);
                mContext.startActivity(intent);
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
                                            Intent intent = new Intent(mContext, MessagesAccountActivity.class);
                                            intent.putExtra(LikesAdapter.EXTRA_ROOM_UID, roomId);
                                            intent.putExtra(LikesAdapter.EXTRA_USER_UID, userId);
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
                                                            Intent intent = new Intent(mContext, MessagesAccountActivity.class);
                                                            intent.putExtra(LikesAdapter.EXTRA_ROOM_UID, roomId);
                                                            intent.putExtra(LikesAdapter.EXTRA_USER_UID, userId);
                                                            mContext.startActivity(intent);

                                                            processRoom = false;

                                                        }else {
                                                            //start a chat with mUid since they have no chatting history
                                                            roomId = databaseReference.push().getKey();
                                                            Intent intent = new Intent(mContext, MessagesAccountActivity.class);
                                                            intent.putExtra(LikesAdapter.EXTRA_ROOM_UID, roomId);
                                                            intent.putExtra(LikesAdapter.EXTRA_USER_UID, userId);
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
                .whereEqualTo("user_id", firebaseAuth.getCurrentUser().getUid())
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
                            .whereEqualTo("user_id", firebaseAuth.getCurrentUser().getUid())
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
                                            follower.setUser_id(firebaseAuth.getCurrentUser().getUid());
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
                                            following.setUser_id(userId);
                                            followersCollection.document("following").collection(firebaseAuth.getCurrentUser().getUid())
                                                    .document(userId).set(following);
                                            holder.followButton.setText("Following");
                                            processFollow = false;
                                        }else {
                                            followersCollection.document("followers").collection(userId)
                                                    .document(firebaseAuth.getCurrentUser().getUid()).delete();
                                            followersCollection.document("following").collection(firebaseAuth.getCurrentUser().getUid())
                                                    .document(userId).delete();
                                            holder.followButton.setText("Follow");
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

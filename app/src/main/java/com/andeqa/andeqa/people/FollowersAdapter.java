package com.andeqa.andeqa.people;

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

public class FollowersAdapter extends RecyclerView.Adapter<PeopleViewHolder> {
    private static final String TAG = FollowersAdapter.class.getSimpleName();
    private FirebaseAuth firebaseAuth;
    private Context mContext;
    private DatabaseReference databaseReference;
    private CollectionReference followersCollection;
    private CollectionReference timelineCollection;
    private CollectionReference usersCollection;
    private CollectionReference roomsCollection;
    private static final String EXTRA_USER_UID = "uid";
    private static final String EXTRA_ROOM_UID = "roomId";
    private boolean processFollow = false;
    private boolean processRoom = false;
    private String roomId;

    private List<DocumentSnapshot> documentSnapshots = new ArrayList<>();

    public FollowersAdapter(Context mContext) {
        this.mContext = mContext;
    }

    protected void setPeople(List<DocumentSnapshot> mSnapshots){
        this.documentSnapshots = mSnapshots;
        notifyDataSetChanged();
    }

    @Override
    public PeopleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_people, parent, false);
        return new PeopleViewHolder(view);
    }

    protected DocumentSnapshot getSnapshot(int index) {
        return documentSnapshots.get(index);
    }




    @Override
    public void onBindViewHolder(final PeopleViewHolder holder, int position) {
        Relation relation = getSnapshot(position).toObject(Relation.class);
        final String userId = relation.getUser_id();

        firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser()!= null){
            usersCollection = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);
            followersCollection = FirebaseFirestore.getInstance().collection(Constants.PEOPLE);
            timelineCollection = FirebaseFirestore.getInstance().collection(Constants.TIMELINE);
            databaseReference = FirebaseDatabase.getInstance().getReference(Constants.RANDOM_PUSH_ID);
            roomsCollection = FirebaseFirestore.getInstance().collection(Constants.MESSAGES);
        }

        usersCollection.document(userId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }


                if (documentSnapshot.exists()){
                    Andeqan andeqan =  documentSnapshot.toObject(Andeqan.class);
                    final String profileImage = andeqan.getProfile_image();
                    final String firstName = andeqan.getFirst_name();
                    final String secondName = andeqan.getSecond_name();
                    final String username = andeqan.getUsername();
                    final String uid = andeqan.getUser_id();


                    holder.usernameTextView.setText(username);
                    holder.fullNameTextView.setText(firstName + " " + secondName);
                    Glide.with(mContext.getApplicationContext())
                            .load(profileImage)
                            .apply(new RequestOptions()
                                    .placeholder(R.drawable.ic_user)
                                    .diskCacheStrategy(DiskCacheStrategy.DATA))
                            .into(holder.profileImageView);

                    //lauch user profile
                    holder.profileImageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(mContext, ProfileActivity.class);
                            intent.putExtra(FollowersAdapter.EXTRA_USER_UID, userId);
                            mContext.startActivity(intent);
                        }
                    });

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
                                        if (!uid.equals(firebaseAuth.getCurrentUser().getUid())){
                                            holder.followButton.setVisibility(View.VISIBLE);
                                            holder.followButton.setText("Follow");
                                        }
                                    }else {
                                        if (!uid.equals(firebaseAuth.getCurrentUser().getUid())){
                                            holder.followButton.setVisibility(View.VISIBLE);
                                            holder.followButton.setText("Following");
                                        }
                                    }
                                }
                            });

                    if (uid.equals(firebaseAuth.getCurrentUser().getUid())){
                        holder.sendMessageImageView.setVisibility(View.GONE);
                    }else {
                        holder.sendMessageImageView.setVisibility(View.VISIBLE);
                        holder.sendMessageImageView.setOnClickListener(new View.OnClickListener() {
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
                                                        intent.putExtra(FollowersAdapter.EXTRA_ROOM_UID, roomId);
                                                        intent.putExtra(FollowersAdapter.EXTRA_USER_UID, userId);
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
                                                                        intent.putExtra(FollowersAdapter.EXTRA_ROOM_UID, roomId);
                                                                        intent.putExtra(FollowersAdapter.EXTRA_USER_UID, userId);
                                                                        mContext.startActivity(intent);

                                                                        processRoom = false;

                                                                    }else {
                                                                        //start a chat with mUid since they have no chatting history
                                                                        roomId = databaseReference.push().getKey();
                                                                        Intent intent = new Intent(mContext, MessagesAccountActivity.class);
                                                                        intent.putExtra(FollowersAdapter.EXTRA_ROOM_UID, roomId);
                                                                        intent.putExtra(FollowersAdapter.EXTRA_USER_UID, userId);
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

                    //follow or unfollow
                    if (uid.equals(firebaseAuth.getCurrentUser().getUid())){
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
        });

    }

    @Override
    public int getItemCount() {
        return documentSnapshots.size();
    }


}

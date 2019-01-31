package com.andeqa.andeqa.people;

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
import com.andeqa.andeqa.chatting.ChatActivity;
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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Nullable;

public class PeopleAdapter extends RecyclerView.Adapter<PeopleRelationsViewHolder> {
    //firestore
    private CollectionReference usersCollection;
    private CollectionReference peopleCollection;
    private CollectionReference timelineCollection;
    private CollectionReference roomsCollection;
    //    private Query usersQuery;
    private DatabaseReference databaseReference;
    //firebase auth
    private FirebaseAuth firebaseAuth;
    //boolean
    private boolean processFollow = false;
    private boolean processRoom = false;
    //strings
    private static final String TAG = PeopleAdapter.class.getSimpleName();
    private static final String EXTRA_USER_UID = "uid";
    private static final String EXTRA_ROOM_ID = "roomId";
    private String roomId;
    private String mUid;
    //context
    private Context mContext;
    // lists
    private List<DocumentSnapshot> documentSnapshots;

    public PeopleAdapter(Context mContext, List<DocumentSnapshot> documentSnapshots) {
        this.mContext = mContext;
        this.documentSnapshots = new ArrayList<>();
        this.documentSnapshots = documentSnapshots;
        initFirebase();
    }

    private void initFirebase() {
        firebaseAuth = FirebaseAuth.getInstance();
        usersCollection = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);
        peopleCollection = FirebaseFirestore.getInstance().collection(Constants.PEOPLE_RELATIONS);
        roomsCollection = FirebaseFirestore.getInstance().collection(Constants.MESSAGES);
        databaseReference = FirebaseDatabase.getInstance().getReference(Constants.RANDOM_PUSH_ID);
        timelineCollection = FirebaseFirestore.getInstance().collection(Constants.TIMELINE);
    }

    @NonNull
    @Override
    public PeopleRelationsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_people, parent, false);
        return new PeopleRelationsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final PeopleRelationsViewHolder holder, int position) {
        Relation relation = documentSnapshots.get(position).toObject(Relation.class);
        final String userId = relation.getFollowing_id();

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

                    holder.mUsernameTextView.setText(username);
                    holder.mFullNameTextView.setText(firstName + " " + secondName);
                    Glide.with(mContext)
                            .load(profileImage)
                            .apply(new RequestOptions()
                                    .placeholder(R.drawable.ic_user)
                                    .diskCacheStrategy(DiskCacheStrategy.DATA))
                            .into(holder.mProfileImageView);

                    //lauch user profile
                    holder.mProfileImageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(mContext, ProfileActivity.class);
                            intent.putExtra(PeopleAdapter.EXTRA_USER_UID, userId);
                            mContext.startActivity(intent);
                        }
                    });


                    //show if following or not
                    peopleCollection.document("followers").collection(userId)
                            .document(firebaseAuth.getCurrentUser().getUid())
                            .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                @Override
                                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                                    if (e != null) {
                                        Log.w(TAG, "Listen error", e);
                                        return;
                                    }

                                    if (documentSnapshot.exists()){
                                        if (!uid.equals(firebaseAuth.getCurrentUser().getUid())){
                                            holder.mFollowButton.setVisibility(View.VISIBLE);
                                            holder.mFollowButton.setText("Follow");
                                        }
                                    }else {
                                        if (!uid.equals(firebaseAuth.getCurrentUser().getUid())){
                                            holder.mFollowButton.setVisibility(View.VISIBLE);
                                            holder.mFollowButton.setText("Following");
                                        }
                                    }
                                }
                            });


                    if (uid.equals(firebaseAuth.getCurrentUser().getUid())){
                        holder.mSendMessageImageView.setVisibility(View.GONE);
                    }else {
                        holder.mSendMessageImageView.setVisibility(View.VISIBLE);
                        holder.mSendMessageImageView.setOnClickListener(new View.OnClickListener() {
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
                                                        Intent intent = new Intent(mContext, ChatActivity.class);
                                                        intent.putExtra(PeopleAdapter.EXTRA_ROOM_ID, roomId);
                                                        intent.putExtra(PeopleAdapter.EXTRA_USER_UID, userId);
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
                                                                        Intent intent = new Intent(mContext, ChatActivity.class);
                                                                        intent.putExtra(PeopleAdapter.EXTRA_ROOM_ID, roomId);
                                                                        intent.putExtra(PeopleAdapter.EXTRA_USER_UID, userId);
                                                                        mContext.startActivity(intent);

                                                                        processRoom = false;

                                                                    }else {
                                                                        //start a chat with mUid since they have no chatting history
                                                                        roomId = databaseReference.push().getKey();
                                                                        Intent intent = new Intent(mContext, ChatActivity.class);
                                                                        intent.putExtra(PeopleAdapter.EXTRA_ROOM_ID, roomId);
                                                                        intent.putExtra(PeopleAdapter.EXTRA_USER_UID, userId);
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
                        holder.mFollowButton.setVisibility(View.GONE);
                    }else {
                        holder.mFollowButton.setVisibility(View.VISIBLE);
                        holder.mFollowButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                processFollow = true;
                                peopleCollection.document("followers")
                                        .collection(userId).whereEqualTo("following_id",
                                        firebaseAuth.getCurrentUser().getUid())
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
                                                        peopleCollection.document("followers").collection(userId)
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
                                                        follower.setFollowing_id(firebaseAuth.getCurrentUser().getUid());
                                                        following.setFollowed_id(userId);
                                                        following.setType("following_user");;
                                                        following.setTime(System.currentTimeMillis());
                                                        peopleCollection.document("following").collection(firebaseAuth.getCurrentUser().getUid())
                                                                .document(userId).set(following);
                                                        holder.mFollowButton.setText("Following");

                                                        processFollow = false;
                                                    }else {
                                                        peopleCollection.document("followers").collection(userId)
                                                                .document(firebaseAuth.getCurrentUser().getUid()).delete();
                                                        peopleCollection.document("following").collection(firebaseAuth.getCurrentUser().getUid())
                                                                .document(userId).delete();
                                                        holder.mFollowButton.setText("Follow");
                                                        processFollow = false;
                                                    }
                                                }
                                            }
                                        });

                            }
                        });
                    }
                }else {
                    peopleCollection.document("followers").collection(userId)
                            .document(firebaseAuth.getCurrentUser().getUid()).delete();
                }


            }
        });

    }

    @Override
    public int getItemCount() {
        return documentSnapshots.size();
    }
}

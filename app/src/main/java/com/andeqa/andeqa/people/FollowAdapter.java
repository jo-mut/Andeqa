package com.andeqa.andeqa.people;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.R;
import com.andeqa.andeqa.models.Andeqan;
import com.andeqa.andeqa.models.Relation;
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

public class FollowAdapter extends RecyclerView.Adapter<FollowViewHolder> {
    private static final String TAG = FollowAdapter.class.getSimpleName();
    //context
    private Context context;
    //firebase auth
    private FirebaseAuth firebaseAuth;
    //firebase firestore references
    private CollectionReference usersCollection;
    private CollectionReference peopleCollection;
    private DatabaseReference databaseReference;
    private CollectionReference timelineCollection;
    private boolean processFollow = false;
    private static final String EXTRA_USER_UID = "uid";
    private static final String EXTRA_ROOM_UID = "roomId";
    private List<DocumentSnapshot> documentSnapshots = new ArrayList<>();


    public FollowAdapter(Context context) {
        this.context = context;
    }

    protected void setPeople(List<DocumentSnapshot> mSnapshots){
        this.documentSnapshots = mSnapshots;
        notifyDataSetChanged();
        initReferences();
    }

    @Override
    public FollowViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_follow_user, parent, false);
        return new FollowViewHolder(view);
    }

    protected DocumentSnapshot getSnapshot(int index) {
        return documentSnapshots.get(index);
    }

    private void initReferences(){
        firebaseAuth = FirebaseAuth.getInstance();
        peopleCollection = FirebaseFirestore.getInstance().collection(Constants.PEOPLE);
        usersCollection = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);
        timelineCollection = FirebaseFirestore.getInstance().collection(Constants.TIMELINE);
        databaseReference = FirebaseDatabase.getInstance().getReference(Constants.RANDOM_PUSH_ID);

    }


    @Override
    public void onBindViewHolder(final FollowViewHolder holder, int position) {
        Andeqan andeqan = getSnapshot(holder.getAdapterPosition()).toObject(Andeqan.class);
        final String userId = andeqan.getUser_id();
        final String username = andeqan.getUsername();
        final String profileCover = andeqan.getProfile_cover();
        final String profileImage = andeqan.getProfile_image();


        holder.usernameTextView.setText(username);
        Glide.with(context.getApplicationContext())
                .load(profileImage)
                .apply(new RequestOptions()
                        .placeholder(R.drawable.ic_user)
                        .diskCacheStrategy(DiskCacheStrategy.DATA))
                .into(holder.profileImageView);


        if (profileImage != null && profileCover == null){
            holder.usernameTextView.setTextColor(Color.WHITE);
            holder.followLinearLayout.setBackgroundResource(R.drawable.default_gradient_color);
            Glide.with(context.getApplicationContext())
                    .load(profileImage)
                    .apply(new RequestOptions()
                            .placeholder(R.drawable.post_placeholder)
                            .diskCacheStrategy(DiskCacheStrategy.DATA))
                    .into(holder.profileCoverImageView);
        }else if (profileCover != null){
            holder.usernameTextView.setTextColor(Color.WHITE);
            holder.followLinearLayout.setBackgroundResource(R.drawable.default_gradient_color);
            Glide.with(context.getApplicationContext())
                    .load(profileCover)
                    .apply(new RequestOptions()
                            .placeholder(R.drawable.post_placeholder)
                            .diskCacheStrategy(DiskCacheStrategy.DATA))
                    .into(holder.profileCoverImageView);
        }else {
            holder.usernameTextView.setTextColor(Color.BLACK);
            holder.followLinearLayout.setBackgroundResource(R.drawable.transparent_background);
            Glide.with(context.getApplicationContext())
                    .load(profileCover)
                    .apply(new RequestOptions()
                            .placeholder(R.drawable.post_placeholder)
                            .diskCacheStrategy(DiskCacheStrategy.DATA))
                    .into(holder.profileCoverImageView);
        }

        //lauch user profile
        holder.profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ProfileActivity.class);
                intent.putExtra(FollowAdapter.EXTRA_USER_UID, userId);
                context.startActivity(intent);
            }
        });

        //show if following or not
        peopleCollection.document("followers").collection(userId)
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
                    peopleCollection.document("followers")
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
                                            following.setFollowing_id(firebaseAuth.getCurrentUser().getUid());
                                            following.setFollowed_id(userId);
                                            following.setType("following_user");
                                            following.setTime(System.currentTimeMillis());
                                            peopleCollection.document("following").collection(firebaseAuth.getCurrentUser().getUid())
                                                    .document(userId).set(following);
                                            holder.followButton.setText("Following");
                                            processFollow = false;
                                        }else {
                                            peopleCollection.document("followers").collection(userId)
                                                    .document(firebaseAuth.getCurrentUser().getUid()).delete();
                                            peopleCollection.document("following").collection(firebaseAuth.getCurrentUser().getUid())
                                                    .document(userId).delete();
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
    public int getItemCount() {
        return documentSnapshots.size();
    }

}

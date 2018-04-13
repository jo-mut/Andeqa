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
import com.andeqa.andeqa.models.Andeqan;
import com.andeqa.andeqa.models.Relation;
import com.andeqa.andeqa.models.Timeline;
import com.andeqa.andeqa.profile.ProfileActivity;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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
import java.util.Date;
import java.util.List;

/**
 * Created by J.EL on 3/30/2018.
 */

public class FollowingAdapter extends RecyclerView.Adapter<PeopleViewHolder> {
    //context
    private Context mContext;
    //firestore references
    private CollectionReference relationsReference;
    private CollectionReference usersReference;
    private CollectionReference timelineCollection;
    private Query followingQuery;

    //firebase
    private DatabaseReference databaseReference;
    //adapters
    private FirestoreRecyclerAdapter firestoreRecyclerAdapter;
    private FirebaseAuth firebaseAuth;
    private boolean processFollow = false;
    private static final String TAG = FollowersActivity.class.getSimpleName();
    private static final String EXTRA_USER_UID = "uid";

    private List<DocumentSnapshot> documentSnapshots = new ArrayList<>();


    public FollowingAdapter(Context mContext) {
        this.mContext = mContext;
    }

    protected void setProfileRelations(List<DocumentSnapshot> mSnapshots){
        this.documentSnapshots = mSnapshots;
        notifyDataSetChanged();
    }


    @Override
    public PeopleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.poeple_list, parent, false);
        return new PeopleViewHolder(view);
    }

    protected DocumentSnapshot getSnapshot(int index) {
        return documentSnapshots.get(index);
    }


    @Override
    public void onBindViewHolder(final PeopleViewHolder holder, int position) {
        Relation relation = getSnapshot(position).toObject(Relation.class);
        final String userId = relation.getUserId();
        Log.d("following postKey", userId);

        firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser()!= null){

            relationsReference = FirebaseFirestore.getInstance().collection(Constants.RELATIONS);
            usersReference = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);
            followingQuery = relationsReference.document("following").collection(userId);
            timelineCollection = FirebaseFirestore.getInstance().collection(Constants.TIMELINE);
            //firebase
            databaseReference = FirebaseDatabase.getInstance().getReference(Constants.RANDOM_PUSH_ID);

            firestoreRecyclerAdapter.startListening();
        }

        //post key and uid are same
        usersReference.document(userId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (documentSnapshot.exists()){
                    Andeqan cinggulan =  documentSnapshot.toObject(Andeqan.class);
                    final String profileImage = cinggulan.getProfileImage();
                    final String firstName = cinggulan.getFirstName();
                    final String secondName = cinggulan.getSecondName();
                    final String username = cinggulan.getUsername();
                    final String uid = cinggulan.getUserId();
                    Log.d("following uid fa", uid);

                    holder.usernameTextView.setText(username);
                    holder.fullNameTextView.setText(firstName + " " + secondName);
                    Picasso.with(mContext)
                            .load(profileImage)
                            .fit()
                            .centerCrop()
                            .placeholder(R.drawable.profle_image_background)
                            .networkPolicy(NetworkPolicy.OFFLINE)
                            .into(holder.profileImageView, new Callback() {
                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onError() {
                                    Picasso.with(mContext)
                                            .load(profileImage)
                                            .fit()
                                            .centerCrop()
                                            .placeholder(R.drawable.profle_image_background)
                                            .into(holder.profileImageView);


                                }
                            });

                    //lauch user profile
                    holder.profileImageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(mContext, ProfileActivity.class);
                            intent.putExtra(FollowingAdapter.EXTRA_USER_UID, uid);
                            mContext.startActivity(intent);
                        }
                    });

                    //show if following or not
                    relationsReference.document("following").collection(userId)
                            .whereEqualTo("userId", firebaseAuth.getCurrentUser().getUid())
                            .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                @Override
                                public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                                    if (documentSnapshots.isEmpty()){
                                        holder.followButton.setText("Unfollow");
                                    }else {
                                        holder.followButton.setText("Following");
                                    }
                                }
                            });

                    //follow or unfollow
                    if (uid.equals(firebaseAuth.getCurrentUser().getUid())){
                        holder.followButton.setVisibility(View.GONE);
                    }else {
                        holder.followButton.setVisibility(View.VISIBLE);
                        holder.followButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                processFollow = true;
                                relationsReference.document("followers")
                                        .collection(userId)
                                        .whereEqualTo("userId", firebaseAuth.getCurrentUser().getUid())
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
                                                        follower.setUserId(firebaseAuth.getCurrentUser().getUid());
                                                        relationsReference.document("followers").collection(userId)
                                                                .document(firebaseAuth.getCurrentUser().getUid()).set(follower)
                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void aVoid) {
                                                                        Timeline timeline = new Timeline();
                                                                        final long time = new Date().getTime();
                                                                        final String postid =  databaseReference.push().getKey();
                                                                        timeline.setPostId(uid);
                                                                        timeline.setTime(time);
                                                                        timeline.setUserId(firebaseAuth.getCurrentUser().getUid());
                                                                        timeline.setType("followers");
                                                                        timeline.setActivityId(postid);
                                                                        timeline.setStatus("unRead");

                                                                        timelineCollection.document(userId).collection("activities")
                                                                                .document(firebaseAuth.getCurrentUser().getUid())
                                                                                .set(timeline);
                                                                    }
                                                                });
                                                        final Relation following = new Relation();
                                                        following.setUserId(userId);
                                                        relationsReference.document("following").collection(firebaseAuth.getCurrentUser().getUid())
                                                                .document(userId).set(following);
                                                        processFollow = false;
                                                        holder.followButton.setText("Following");
                                                    }else {
                                                        relationsReference.document("followers").collection(userId)
                                                                .document(firebaseAuth.getCurrentUser().getUid()).delete();
                                                        relationsReference.document("following").collection(firebaseAuth.getCurrentUser().getUid())
                                                                .document(userId).delete();
                                                        processFollow = false;
                                                        holder.followButton.setText("Unfollow");
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

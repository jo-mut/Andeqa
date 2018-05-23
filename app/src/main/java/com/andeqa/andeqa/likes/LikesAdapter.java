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
import com.andeqa.andeqa.models.Andeqan;
import com.andeqa.andeqa.models.Like;
import com.andeqa.andeqa.models.Relation;
import com.andeqa.andeqa.models.Timeline;
import com.andeqa.andeqa.profile.ProfileActivity;
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


/**
 * Created by J.EL on 4/5/2018.
 */

public class LikesAdapter extends RecyclerView.Adapter<LikesViewHolder> {
    //context
    private Context mContext;
    //firestore
    private CollectionReference relationsReference;
    private CollectionReference usersReference;
    private CollectionReference timelineCollection;
    //firestore
    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;
    private boolean processFollow = false;
    private static final String TAG = LikesActivity.class.getSimpleName();
    private static final String EXTRA_USER_UID = "uid";

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
                (R.layout.likes_list_layout, parent, false);
        return new LikesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final LikesViewHolder holder, int position) {
        Like like = getSnapshot(position).toObject(Like.class);
        final String uid = like.getUser_id();

        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null){

            //firestore
            relationsReference = FirebaseFirestore.getInstance().collection(Constants.RELATIONS);
            usersReference = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);
            timelineCollection = FirebaseFirestore.getInstance().collection(Constants.TIMELINE);
            databaseReference = FirebaseDatabase.getInstance().getReference(Constants.RANDOM_PUSH_ID);

        }

        //get the profile of the user who just liked
        usersReference.document(uid).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (documentSnapshot.exists()){
                    final Andeqan cinggulan = documentSnapshot.toObject(Andeqan.class);
                    final String profileImage = cinggulan.getProfile_image();
                    final String username = cinggulan.getUsername();
                    final String firstName = cinggulan.getFirst_name();
                    final String secondName = cinggulan.getSecond_name();


                    holder.usernameTextView.setText(username);
                    holder.fullNameTextView.setText(firstName + " " + secondName);

                    Picasso.with(mContext)
                            .load(profileImage)
                            .fit()
                            .centerCrop()
                            .placeholder(R.drawable.ic_user)
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
                                            .placeholder(R.drawable.ic_user)
                                            .into(holder.profileImageView);


                                }
                            });

                }
            }
        });

        holder.profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, ProfileActivity.class);
                intent.putExtra(LikesAdapter.EXTRA_USER_UID, uid);
                mContext.startActivity(intent);
            }
        });

    }


}

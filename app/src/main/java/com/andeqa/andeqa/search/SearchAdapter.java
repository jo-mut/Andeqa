package com.andeqa.andeqa.search;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.R;
import com.andeqa.andeqa.message.MessagingActivity;
import com.andeqa.andeqa.models.Andeqan;
import com.andeqa.andeqa.models.Room;
import com.andeqa.andeqa.people.FollowersAdapter;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

public class SearchAdapter extends RecyclerView.Adapter<SearchPeopleViewHolder> {
    private static final String TAG = FollowersAdapter.class.getSimpleName();
    private FirebaseAuth firebaseAuth;
    private Context mContext;
    private CollectionReference followingCollection;
    private CollectionReference timelineCollection;
    private CollectionReference usersCollection;
    private CollectionReference roomsCollection;
    private DatabaseReference databaseReference;
    private boolean processFollow = false;
    private boolean processRoom = false;
    private static final String EXTRA_USER_UID = "uid";
    private static final String EXTRA_ROOM_UID = "roomId";
    private String roomId;

    private List<DocumentSnapshot> documentSnapshots = new ArrayList<>();

    public SearchAdapter(Context mContext) {
        this.mContext = mContext;
        iniReferences();
    }

    protected void setPeople(List<DocumentSnapshot> mSnapshots){
        this.documentSnapshots = mSnapshots;
        notifyDataSetChanged();
    }

    @Override
    public SearchPeopleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_people, parent, false);
        return new SearchPeopleViewHolder(view);
    }

    protected DocumentSnapshot getSnapshot(int index) {
        return documentSnapshots.get(index);
    }


    private void iniReferences(){
        roomsCollection = FirebaseFirestore.getInstance().collection(Constants.MESSAGES);
        databaseReference = FirebaseDatabase.getInstance().getReference(Constants.RANDOM_PUSH_ID);
    }


    @Override
    public void onBindViewHolder(final SearchPeopleViewHolder holder, int position) {
        Andeqan andeqan = getSnapshot(position).toObject(Andeqan.class);
        final String userId = andeqan.getUser_id();
        final String username = andeqan.getUsername();
        final String profileImage = andeqan.getProfile_image();
        final String firstName = andeqan.getFirst_name();
        final String secondName = andeqan.getSecond_name();

        firebaseAuth = FirebaseAuth.getInstance();

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
                                        intent.putExtra(SearchAdapter.EXTRA_ROOM_UID, roomId);
                                        intent.putExtra(SearchAdapter.EXTRA_USER_UID, userId);
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
                                                        intent.putExtra(SearchAdapter.EXTRA_ROOM_UID, roomId);
                                                        intent.putExtra(SearchAdapter.EXTRA_USER_UID, userId);
                                                        mContext.startActivity(intent);

                                                        processRoom = false;

                                                    }else {
                                                        //start a chat with mUid since they have no chatting history
                                                        roomId = databaseReference.push().getKey();
                                                        Intent intent = new Intent(mContext, MessagingActivity.class);
                                                        intent.putExtra(SearchAdapter.EXTRA_ROOM_UID, roomId);
                                                        intent.putExtra(SearchAdapter.EXTRA_USER_UID, userId);
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

    @Override
    public int getItemCount() {
        return documentSnapshots.size();
    }


    public void cleanUp(){
        documentSnapshots.clear();
        notifyDataSetChanged();
    }

}

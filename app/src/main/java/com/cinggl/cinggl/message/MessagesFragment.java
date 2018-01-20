package com.cinggl.cinggl.message;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cinggl.cinggl.Constants;
import com.cinggl.cinggl.R;
import com.cinggl.cinggl.models.Cinggulan;
import com.cinggl.cinggl.models.Room;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
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

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class MessagesFragment extends Fragment {

    @Bind(R.id.massagingUsersRecyclerView)RecyclerView mMessagingUsersRecyclerView;
    private static final String TAG = MessagesFragment.class.getSimpleName();
    private static final String EXTRA_ROOM_ID = "roomId";
    private static final String EXTRA_USER_UID = "uid";
    private MessagingAdapter messagingAdapter;
    private FirestoreRecyclerAdapter firestoreRecyclerAdapter;
    private CollectionReference messagingUsersCollection;
    private CollectionReference usersCollection;
    private Query messagingUsersQuery;
    private FirebaseAuth firebaseAuth;

    public MessagesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_messages, container, false);
        ButterKnife.bind(this, view);

        firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() != null){
            messagingUsersCollection = FirebaseFirestore.getInstance().collection(Constants.MESSAGES);
            messagingUsersQuery = messagingUsersCollection.document("messaging users")
                    .collection(firebaseAuth.getCurrentUser().getUid());
            usersCollection = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);


            getMessagingUsers();
        }

        return view;
    }

    /**retrieve all the messages*/
    private void getMessagingUsers(){
        messagingUsersQuery.orderBy("time");
        FirestoreRecyclerOptions<Room> options = new FirestoreRecyclerOptions.Builder<Room>()
                .setQuery(messagingUsersQuery, Room.class)
                .build();
        firestoreRecyclerAdapter = new FirestoreRecyclerAdapter<Room, MessageViewHolder>(options) {
            @Override
            protected void onBindViewHolder(final MessageViewHolder holder, int position, Room model) {
                final String uid = getSnapshots().get(position).getUid();
                final String postKey = getSnapshots().get(position).getPushId();
                final String lastMessage = getSnapshots().get(position).getMessage();
                final String roomId = getSnapshots().get(position).getRoomId();
                final String status = getSnapshots().get(position).getStatus();

                holder.bindMessagingUser(model);

                if (status.equals("unRead")){
                    holder.statusView.setVisibility(View.VISIBLE);
                    holder.roomRelativeLayout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            messagingUsersCollection.document("messaging users")
                                    .collection(firebaseAuth.getCurrentUser().getUid())
                                    .document(postKey).update("status", "read");
                        }
                    });

                }else {
                    holder.roomRelativeLayout.setVisibility(View.GONE);
                }

                holder.roomRelativeLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getContext(), MessagesAccountActivity.class);
                        intent.putExtra(MessagesFragment.EXTRA_ROOM_ID, roomId);
                        intent.putExtra(MessagesFragment.EXTRA_USER_UID, uid);
                        startActivity(intent);
                    }
                });


                holder.lastMessageTextView.setText(lastMessage);

                //postkey is same as uid
                usersCollection.document(postKey).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen error", e);
                            return;
                        }


                        if (documentSnapshot.exists()){
                            Cinggulan cinggulan =  documentSnapshot.toObject(Cinggulan.class);
                            final String profileImage = cinggulan.getProfileImage();
                            final String username = cinggulan.getUsername();

                            holder.usernameTextView.setText(username);
                            Picasso.with(getContext())
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
                                            Picasso.with(getContext())
                                                    .load(profileImage)
                                                    .fit()
                                                    .centerCrop()
                                                    .placeholder(R.drawable.profle_image_background)
                                                    .into(holder.profileImageView);


                                        }
                                    });



                        }

                    }
                });
            }

            @Override
            public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_list_layout, parent, false);
                return new MessageViewHolder(view);
            }
        };

        mMessagingUsersRecyclerView.setAdapter(firestoreRecyclerAdapter);
        firestoreRecyclerAdapter.startListening();
        mMessagingUsersRecyclerView.setHasFixedSize(false);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        mMessagingUsersRecyclerView.setLayoutManager(layoutManager);
    }


}

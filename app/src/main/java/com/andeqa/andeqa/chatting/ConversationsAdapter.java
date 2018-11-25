package com.andeqa.andeqa.chatting;

import android.arch.paging.PagedList;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.R;
import com.andeqa.andeqa.models.Andeqan;
import com.andeqa.andeqa.models.Room;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by J.EL on 3/30/2018.
 */

public class ConversationsAdapter extends FirestorePagingAdapter<Room, RecyclerView.ViewHolder> {
    private static final String TAG = ConversationsAdapter.class.getSimpleName();
    //firebase
    private CollectionReference roomCollection;
    private CollectionReference usersCollection;
    private FirebaseAuth firebaseAuth;
    private static final String EXTRA_ROOM_ID = "roomId";
    private static final String EXTRA_USER_UID = "uid";
    private static final int TEXT = 1;
    private static final int PHOTO = 2;
    private Context mContext;


    public ConversationsAdapter(@NonNull FirestorePagingOptions options, Context mContext) {
        super(options);
        this.mContext = mContext;
    }


    @Nullable
    @Override
    public PagedList<DocumentSnapshot> getCurrentList() {
        return super.getCurrentList();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case TEXT:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.room_text_layout, parent, false);
                return new ConversationViewHolder(view);
            case PHOTO:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.room_photo_layout, parent, false);
                return new ConversationViewHolder(view);
        }
        return null;
    }

    protected DocumentSnapshot getSnapshot(int index) {
        return getCurrentList().get(index);
    }


    @Override
    public int getItemViewType(int position) {
        Room room = getSnapshot(position).toObject(Room.class);
        final String type = room.getType();

        if (type != null){
            if (type.equals("photo")){
                return PHOTO;
            }else if (type.equals("text")){
                return TEXT;
            }else {
                return TEXT;
            }
        }else {
            return TEXT;
        }
    }

    @Override
    protected void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull Room model) {
        final String type = model.getType();
        firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() != null) {
            roomCollection = FirebaseFirestore.getInstance().collection(Constants.MESSAGES);
            usersCollection = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);

        }

        if (type!=null){
            if (type.equals("photo")) {
                populatePhoto((ConversationViewHolder) holder, position);
            } else if (type.equals("text")) {
                populateText((ConversationViewHolder) holder, position);
            } else {
                populateText((ConversationViewHolder) holder, position);
            }
        }else {
            populateText((ConversationViewHolder) holder, position);
        }

    }

    @Override
    public int getItemCount() {
        return super.getItemCount();
    }


    private void populateText(final ConversationViewHolder holder, int position) {
        firebaseAuth = FirebaseAuth.getInstance();
        Room room = getSnapshot(position).toObject(Room.class);
        final String receiverUid = room.getReceiver_id();
        final String senderUid = room.getSender_id();
        final String lastMessage = room.getMessage();
        final String roomId = room.getRoom_id();
        final String status = room.getStatus();
        roomCollection = FirebaseFirestore.getInstance().collection(Constants.MESSAGES);
        usersCollection = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);


        if (firebaseAuth.getCurrentUser().getUid().equals(senderUid)) {
            holder.lastMessageTextView.setTypeface(holder.lastMessageTextView.getTypeface(), Typeface.NORMAL);
            holder.roomRelativeLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(mContext, ChatActivity.class);
                    intent.putExtra(ConversationsAdapter.EXTRA_ROOM_ID, roomId);
                    intent.putExtra(ConversationsAdapter.EXTRA_USER_UID, receiverUid);
                    mContext.startActivity(intent);
                }
            });

            holder.lastMessageTextView.setText(lastMessage);

            long due = room.getTime();
            long now = System.currentTimeMillis();


            if (now > due && (now - due) < 86400000){
                holder.timeTextView.setText(DateUtils.getRelativeTimeSpanString(due, now,
                        DateUtils.FORMAT_ABBREV_ALL));
            }else if(now > due && (now - due) > 86400000){
                holder.timeTextView.setText(DateUtils.getRelativeTimeSpanString(due, now,
                        DateUtils.DAY_IN_MILLIS));
            }else {
                holder.timeTextView.setText(DateFormat.format("dd:MM:yy", room.getTime()));
            }


            usersCollection.document(receiverUid).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                    if (e != null) {
                        Log.w(TAG, "Listen error", e);
                        return;
                    }

                    if (documentSnapshot.exists()) {
                        Andeqan cinggulan = documentSnapshot.toObject(Andeqan.class);
                        final String profileImage = cinggulan.getProfile_image();
                        final String username = cinggulan.getUsername();

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

        }else {
            if (status.equals("un_read")) {
                holder.lastMessageTextView.setTypeface(holder.lastMessageTextView.getTypeface(), Typeface.BOLD);
                holder.roomRelativeLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        roomCollection.document("last messages")
                                .collection(firebaseAuth.getCurrentUser().getUid())
                                .document(roomId).update("status", "read");
                        Intent intent = new Intent(mContext, ChatActivity.class);
                        intent.putExtra(ConversationsAdapter.EXTRA_ROOM_ID, roomId);
                        intent.putExtra(ConversationsAdapter.EXTRA_USER_UID, senderUid);
                        mContext.startActivity(intent);
                    }
                });
            } else {
                holder.lastMessageTextView.setTypeface(holder.lastMessageTextView.getTypeface(), Typeface.NORMAL);
                holder.roomRelativeLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(mContext, ChatActivity.class);
                        intent.putExtra(ConversationsAdapter.EXTRA_ROOM_ID, roomId);
                        intent.putExtra(ConversationsAdapter.EXTRA_USER_UID, senderUid);
                        mContext.startActivity(intent);
                    }
                });
            }

            holder.lastMessageTextView.setText(lastMessage);

            long due = room.getTime();
            long now = System.currentTimeMillis();


            if (now > due && (now - due) < 86400000){
                holder.timeTextView.setText(DateUtils.getRelativeTimeSpanString(due, now,
                        DateUtils.FORMAT_ABBREV_ALL));
            }else if(now > due && (now - due) > 86400000){
                holder.timeTextView.setText(DateUtils.getRelativeTimeSpanString(due, now,
                        DateUtils.DAY_IN_MILLIS));
            }else {
                holder.timeTextView.setText(DateFormat.format("dd:MM:yy", room.getTime()));
            }


            usersCollection.document(senderUid).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                    if (e != null) {
                        Log.w(TAG, "Listen error", e);
                        return;
                    }

                    if (documentSnapshot.exists()) {
                        Andeqan cinggulan = documentSnapshot.toObject(Andeqan.class);
                        final String profileImage = cinggulan.getProfile_image();
                        final String username = cinggulan.getUsername();

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
        }

    }

    private void populatePhoto(final ConversationViewHolder holder, int position){
        firebaseAuth = FirebaseAuth.getInstance();
        Room room = getSnapshot(position).toObject(Room.class);
        final String receiverUid = room.getReceiver_id();
        final String senderUid = room.getSender_id();
        final String roomId = room.getRoom_id();
        final String status = room.getStatus();
        roomCollection = FirebaseFirestore.getInstance().collection(Constants.MESSAGES);
        usersCollection = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);

        if (firebaseAuth.getCurrentUser().getUid().equals(senderUid)) {
            holder.lastMessageTextView.setTypeface(holder.lastMessageTextView.getTypeface(), Typeface.NORMAL);
            holder.roomRelativeLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(mContext, ChatActivity.class);
                    intent.putExtra(ConversationsAdapter.EXTRA_ROOM_ID, roomId);
                    intent.putExtra(ConversationsAdapter.EXTRA_USER_UID, receiverUid);
                    mContext.startActivity(intent);
                }
            });

            long due = room.getTime();
            long now = System.currentTimeMillis();

            if (now > due && (now - due) < 86400000){
                holder.timeTextView.setText(DateUtils.getRelativeTimeSpanString(due, now,
                        DateUtils.FORMAT_ABBREV_ALL));
            }else if(now > due && (now - due) > 86400000){
                holder.timeTextView.setText(DateUtils.getRelativeTimeSpanString(due, now,
                        DateUtils.DAY_IN_MILLIS));
            }else {
                holder.timeTextView.setText(DateFormat.format("dd:MM:yy", room.getTime()));
            }


            usersCollection.document(receiverUid).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                    if (e != null) {
                        Log.w(TAG, "Listen error", e);
                        return;
                    }

                    if (documentSnapshot.exists()) {
                        Andeqan cinggulan = documentSnapshot.toObject(Andeqan.class);
                        final String profileImage = cinggulan.getProfile_image();
                        final String username = cinggulan.getUsername();

                        holder.usernameTextView.setText(username);
                        holder.lastMessageTextView.setText("You sent a photo");
                        Glide.with(mContext.getApplicationContext())
                                .load(profileImage)
                                .apply(new RequestOptions()
                                        .placeholder(R.drawable.ic_user)
                                        .diskCacheStrategy(DiskCacheStrategy.DATA))
                                .into(holder.profileImageView);

                    }
                }
            });

        }else {
            if (status.equals("un_read")) {
                holder.lastMessageTextView.setTypeface(holder.lastMessageTextView.getTypeface(), Typeface.BOLD);
                holder.roomRelativeLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        roomCollection.document("last messages")
                                .collection(firebaseAuth.getCurrentUser().getUid())
                                .document(roomId).update("status", "read");
                        Intent intent = new Intent(mContext, ChatActivity.class);
                        intent.putExtra(ConversationsAdapter.EXTRA_ROOM_ID, roomId);
                        intent.putExtra(ConversationsAdapter.EXTRA_USER_UID, senderUid);
                        mContext.startActivity(intent);
                    }
                });
            } else {
                holder.lastMessageTextView.setTypeface(holder.lastMessageTextView.getTypeface(), Typeface.NORMAL);
                holder.roomRelativeLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(mContext, ChatActivity.class);
                        intent.putExtra(ConversationsAdapter.EXTRA_ROOM_ID, roomId);
                        intent.putExtra(ConversationsAdapter.EXTRA_USER_UID, senderUid);
                        mContext.startActivity(intent);
                    }
                });
            }

            long due = room.getTime();
            long now = System.currentTimeMillis();

            if (now > due && (now - due) < 86400000){
                holder.timeTextView.setText(DateUtils.getRelativeTimeSpanString(due, now,
                        DateUtils.FORMAT_ABBREV_ALL));
            }else if(now > due && (now - due) > 86400000){
                holder.timeTextView.setText(DateUtils.getRelativeTimeSpanString(due, now,
                        DateUtils.DAY_IN_MILLIS));
            }else {
                holder.timeTextView.setText(DateFormat.format("dd:MM:yy", room.getTime()));
            }


            usersCollection.document(senderUid).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                    if (e != null) {
                        Log.w(TAG, "Listen error", e);
                        return;
                    }

                    if (documentSnapshot.exists()) {
                        Andeqan cinggulan = documentSnapshot.toObject(Andeqan.class);
                        final String profileImage = cinggulan.getProfile_image();
                        final String username = cinggulan.getUsername();

                        holder.usernameTextView.setText(username);
                        holder.lastMessageTextView.setText("Sent you a photo");
                        Glide.with(mContext.getApplicationContext())
                                .load(profileImage)
                                .apply(new RequestOptions()
                                        .placeholder(R.drawable.ic_user)
                                        .diskCacheStrategy(DiskCacheStrategy.DATA))
                                .into(holder.profileImageView);


                    }

                }
            });

        }

    }
}
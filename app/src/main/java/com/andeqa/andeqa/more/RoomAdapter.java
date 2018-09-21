package com.andeqa.andeqa.more;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.R;
import com.andeqa.andeqa.chatting.MessagingActivity;
import com.andeqa.andeqa.models.Andeqan;
import com.andeqa.andeqa.models.Room;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
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

public class RoomAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = RoomAdapter.class.getSimpleName();
    private static final String EXTRA_ROOM_ID = "roomId";
    private static final String EXTRA_USER_UID = "uid";
    private static final int TEXT = 1;
    private static final int PHOTO = 2;
    private CollectionReference roomCollection;
    private CollectionReference usersCollection;
    private FirebaseAuth firebaseAuth;
    private Context mContext;
    private List<DocumentSnapshot> documentSnapshots = new ArrayList<>();


    public RoomAdapter(Context mContext) {
        this.mContext = mContext;
    }

    protected void setChatRooms(List<DocumentSnapshot> mSnapshots) {
        this.documentSnapshots = mSnapshots;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case TEXT:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.room_text_layout, parent, false);
                return new RoomTextViewHolder(view);
            case PHOTO:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.room_photo_layout, parent, false);
                return new RoomPhotoViewHolder(view);
        }
        return null;
    }

    protected DocumentSnapshot getSnapshot(int index) {
        return documentSnapshots.get(index);
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
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        Room room = getSnapshot(position).toObject(Room.class);
        final String type = room.getType();

        firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() != null) {
            roomCollection = FirebaseFirestore.getInstance().collection(Constants.MESSAGES);
            usersCollection = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);

        }

        if (type!=null){
            if (type.equals("photo")) {
                populatePhoto((RoomPhotoViewHolder) holder, position);
            } else if (type.equals("text")) {
                populateText((RoomTextViewHolder) holder, position);
            } else {
                populateText((RoomTextViewHolder) holder, position);
            }
        }else {
            populateText((RoomTextViewHolder) holder, position);
        }

    }


    @Override
    public int getItemCount() {
        return documentSnapshots.size();
    }

    private void populateText(final RoomTextViewHolder holder, int position) {
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
                    Intent intent = new Intent(mContext, MessagingActivity.class);
                    intent.putExtra(RoomAdapter.EXTRA_ROOM_ID, roomId);
                    intent.putExtra(RoomAdapter.EXTRA_USER_UID, receiverUid);
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
                        Intent intent = new Intent(mContext, MessagingActivity.class);
                        intent.putExtra(RoomAdapter.EXTRA_ROOM_ID, roomId);
                        intent.putExtra(RoomAdapter.EXTRA_USER_UID, senderUid);
                        mContext.startActivity(intent);
                    }
                });
            } else {
                holder.lastMessageTextView.setTypeface(holder.lastMessageTextView.getTypeface(), Typeface.NORMAL);
                holder.roomRelativeLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(mContext, MessagingActivity.class);
                        intent.putExtra(RoomAdapter.EXTRA_ROOM_ID, roomId);
                        intent.putExtra(RoomAdapter.EXTRA_USER_UID, senderUid);
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

    private void populatePhoto(final RoomPhotoViewHolder holder, int position){
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
                    Intent intent = new Intent(mContext, MessagingActivity.class);
                    intent.putExtra(RoomAdapter.EXTRA_ROOM_ID, roomId);
                    intent.putExtra(RoomAdapter.EXTRA_USER_UID, receiverUid);
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
                        Intent intent = new Intent(mContext, MessagingActivity.class);
                        intent.putExtra(RoomAdapter.EXTRA_ROOM_ID, roomId);
                        intent.putExtra(RoomAdapter.EXTRA_USER_UID, senderUid);
                        mContext.startActivity(intent);
                    }
                });
            } else {
                holder.lastMessageTextView.setTypeface(holder.lastMessageTextView.getTypeface(), Typeface.NORMAL);
                holder.roomRelativeLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(mContext, MessagingActivity.class);
                        intent.putExtra(RoomAdapter.EXTRA_ROOM_ID, roomId);
                        intent.putExtra(RoomAdapter.EXTRA_USER_UID, senderUid);
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
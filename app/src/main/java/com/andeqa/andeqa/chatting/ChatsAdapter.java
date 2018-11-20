package com.andeqa.andeqa.chatting;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.R;
import com.andeqa.andeqa.impressions.ImpressionTracker;
import com.andeqa.andeqa.impressions.MessagingVisibilityTracker;
import com.andeqa.andeqa.models.Message;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;

import javax.annotation.Nullable;

public class ChatsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
        implements ImpressionTracker.VisibilityTrackerListener{
    private final String TAG = ChatsAdapter.class.getSimpleName();
    private Context mContext;
    private static final String EXTRA_MESSAGE_ID = "message id";
    private static final String EXTRA_ROOM_ID = "roomId";
    private static final int SEND_DEFAULT =0;
    private static final int SEND_PHOTO =1;
    private static final int RECEIVE_DEFAULT =2;
    private static final int RECEIVE_PHOTO =3;
    private static final int EMPTY =4;
    private FirebaseAuth firebaseAuth;
    private CollectionReference messagesCollection;
    private DatabaseReference seenMessagesReference;
    private boolean showOnClick = true;
    private List<DocumentSnapshot> snapshots = new ArrayList<>();
    private MessagingVisibilityTracker visibilityTracker;
    private final WeakHashMap<View, Integer> mViewPositionMap = new WeakHashMap<>();

    public ChatsAdapter(Activity activity) {
        this.mContext = activity;
        visibilityTracker = new MessagingVisibilityTracker(activity);
        initReferences();

    }

    public void setProfileMessages(List<DocumentSnapshot> messages){
        this.snapshots = messages;
        notifyDataSetChanged();
    }

    private void initReferences(){
        firebaseAuth = FirebaseAuth.getInstance();
        messagesCollection = FirebaseFirestore.getInstance().collection(Constants.MESSAGES);
        seenMessagesReference = FirebaseDatabase.getInstance().getReference(Constants.MESSAGES);
        seenMessagesReference.keepSynced(true);

    }

    protected DocumentSnapshot getSnapshot(int index) {

        return snapshots.get(index);
    }

    @Override
    public void onVisibilityChanged(List<View> visibleViews, List<View> invisibleViews) {
        handleVisibleViews(visibleViews);
    }

    private void handleVisibleViews(List<View> visibleViews) {
        for (View v : visibleViews) {
            Integer viewPosition = mViewPositionMap.get(v);
        }

    }

    @Override
    public int getItemViewType(int position) {
        Message message = getSnapshot(position).toObject(Message.class);
        final String senderUid = message.getSender_id();
        if (message.getType() != null && senderUid.equals(firebaseAuth.getCurrentUser().getUid())){
            if (message.getType().equals("sent_text") || message.getType().equals("text")){
                return SEND_DEFAULT;
            }else if( message.getType().equals("sent_photo") ||  message.getType().equals("photo")){
                return SEND_PHOTO;
            }else {
                return SEND_DEFAULT;
            }
        }else if (message.getType() != null && message.getReceiver_id().equals(firebaseAuth.getCurrentUser().getUid())){
            if (message.getType() != null && message.getType().equals("received_text") ||  message.getType().equals("text")){
                return RECEIVE_DEFAULT;
            }else if (message.getType() != null && message.getType().equals("received_photo") ||  message.getType().equals("photo")){
                return RECEIVE_PHOTO;
            }else {
                return RECEIVE_DEFAULT;
            }
        }else {
            return super.getItemViewType(position);
        }

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        switch (viewType){
            case SEND_DEFAULT:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.layout_message_sent_default, parent, false);
                return new MessageSendViewHolder(view);
            case SEND_PHOTO:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.layout_message_sent_photo, parent, false);
                return new MessageSentPhotoViewHolder(view);
            case RECEIVE_DEFAULT:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.layout_message_receive_default, parent, false);
                return  new MessageReceiveViewHolder(view);
            case RECEIVE_PHOTO:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.layout_message_receive_photo, parent, false);
                return new MessageReceivePhotoViewHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        switch (holder.getItemViewType()){
            case 0:
                populateSend((MessageSendViewHolder)holder, position);
                break;
            case 1:
                populateSendPhoto((MessageSentPhotoViewHolder)holder, position);
                break;
            case 2:
                populateReceive((MessageReceiveViewHolder)holder, position);
                break;
            case 3:
                populateReceivePhoto((MessageReceivePhotoViewHolder) holder, position);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return snapshots.size();
    }

    private void populateSendPhoto(final MessageSentPhotoViewHolder holder, final int position) {
        final Message message = getSnapshot(position).toObject(Message.class);

        if (message.getPhoto() != null){
            final String photo = message.getPhoto();
            Glide.with(mContext.getApplicationContext())
                    .load(photo)
                    .apply(new RequestOptions()
                            .diskCacheStrategy(DiskCacheStrategy.DATA))
                    .into(holder.photoImageView);
            if (TextUtils.isEmpty(message.getPhoto())){
                holder.photoImageView.setVisibility(View.GONE);
            }
        }

        holder.dateTextView.setText(DateFormat.format("dd-MM-yy", message.getTime()));

        if (message.getMessage() != null){
            if (!message.getMessage().equals("")) {
                holder.messageTextView.setVisibility(View.VISIBLE);
                holder.messageTextView.setText(message.getMessage());
            }
        }

        if (message.getPhoto() != null && !message.getPhoto().equals("")){
            holder.photoImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, PhotoMessageActivity.class);
                    intent.putExtra(ChatsAdapter.EXTRA_MESSAGE_ID, message.getMessage_id());
                    intent.putExtra(ChatsAdapter.EXTRA_ROOM_ID, message.getRoom_id());
                    mContext.startActivity(intent);
                }
            });
        }

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (showOnClick) {
                    holder.dateTextView.setVisibility(View.GONE);
                    holder.statusRelativeLayout.setVisibility(View.GONE);
                    showOnClick = false;
                } else {
                    showOnClick = true;
                    holder.dateTextView.setVisibility(View.VISIBLE);
                    holder.statusRelativeLayout.setVisibility(View.VISIBLE);
                }
            }
        });


        messagesCollection.document(firebaseAuth.getCurrentUser().getUid())
                .collection(message.getRoom_id()).orderBy("time", Query.Direction.DESCENDING).limit(1)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot documentSnapshots, @Nullable FirebaseFirestoreException e) {

                        if (e != null) {
                            Log.w(TAG, "Listen error", e);
                            return;
                        }

                        if (!documentSnapshots.isEmpty()){
                            seenMessagesReference.child("seen_messages")
                                    .child(message.getReceiver_id())
                                    .child(firebaseAuth.getCurrentUser().getUid())
                                    .child(message.getMessage_id()).child("seen")
                                    .addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.exists()){
                                                holder.timeTextView.setText("seen " + DateFormat.format("HH:mm", message.getTime()));
                                                if (position == snapshots.size() - 1){
                                                    holder.seenImageView.setVisibility(View.VISIBLE);
                                                    holder.seenImageView.setImageResource(R.drawable.ic_seen_accent);
                                                }

                                            }else {
                                                if (position == snapshots.size() - 1){
                                                    holder.statusRelativeLayout.setVisibility(View.VISIBLE);
                                                    holder.seenImageView.setVisibility(View.VISIBLE);
                                                    holder.seenImageView.setImageResource(R.drawable.ic_done_black);
                                                }
                                                holder.timeTextView.setText("sent " + DateFormat.format("HH:mm", message.getTime()));

                                            }

                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                        }
                    }
                });


    }

    private void populateSend(final MessageSendViewHolder holder, final int position){
        final Message message = getSnapshot(position).toObject(Message.class);

        //calculate view visibility and add visible views to impression tracker
        holder.dateTextView.setText(DateFormat.format("dd-MM-yy", message.getTime()));
        if (message.getMessage()!= null &&!message.getMessage().equals("")){
            holder.messageTextView.setText(message.getMessage());
        }

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (showOnClick) {
                    holder.dateTextView.setVisibility(View.GONE);
                    holder.timeRelativeLayout.setVisibility(View.GONE);
                    showOnClick = false;
                } else {
                    showOnClick = true;
                    holder.dateTextView.setVisibility(View.VISIBLE);
                    holder.timeRelativeLayout.setVisibility(View.VISIBLE);
                }
            }
        });

        messagesCollection.document(firebaseAuth.getCurrentUser().getUid())
                .collection(message.getRoom_id()).orderBy("time", Query.Direction.DESCENDING).limit(1)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot documentSnapshots, @Nullable FirebaseFirestoreException e) {

                        if (e != null) {
                            Log.w(TAG, "Listen error", e);
                            return;
                        }

                        if (!documentSnapshots.isEmpty()){
                            seenMessagesReference.child("seen_messages")
                                    .child(message.getReceiver_id())
                                    .child(firebaseAuth.getCurrentUser().getUid())
                                    .child(message.getMessage_id()).child("seen")
                                    .addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.exists()){
                                                holder.timeTextView.setText("seen " + DateFormat.format("HH:mm", message.getTime()));
                                                if (position == snapshots.size() - 1){
                                                    holder.seenImageView.setVisibility(View.VISIBLE);
                                                    holder.seenImageView.setImageResource(R.drawable.ic_seen_accent);
                                                }

                                            }else {
                                                if (position == snapshots.size() - 1){
                                                    holder.timeRelativeLayout.setVisibility(View.VISIBLE);
                                                    holder.seenImageView.setVisibility(View.VISIBLE);
                                                    holder.seenImageView.setImageResource(R.drawable.ic_done_black);
                                                }
                                                holder.timeTextView.setText("sent " + DateFormat.format("HH:mm", message.getTime()));

                                            }

                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                        }
                    }
                });

    }

    private void populateReceivePhoto(final MessageReceivePhotoViewHolder holder, final int position){
        final Message message = getSnapshot(position).toObject(Message.class);

//        //calculate view visibility and add visible views to impression tracker
//        mViewPositionMap.put(holder.itemView, position);
//        visibilityTracker.addView(holder.itemView, 50, message.getMessage_id(),
//                "message", message.getReceiver_id(), message.getSender_id());

        holder.dateTextView.setText(DateFormat.format("dd-MM-yy", message.getTime()));
        if (message.getMessage() != null){
            if (!message.getMessage().equals("")) {
                holder.messageTextView.setVisibility(View.VISIBLE);
                holder.messageTextView.setText(message.getMessage());
            }
        }


        if (message.getPhoto() != null){
            final String photo = message.getPhoto();
            Glide.with(mContext.getApplicationContext())
                    .load(photo)
                    .apply(new RequestOptions()
                            .diskCacheStrategy(DiskCacheStrategy.DATA))
                    .into(holder.photoImageView);
            if (TextUtils.isEmpty(message.getPhoto())){
                holder.photoImageView.setVisibility(View.GONE);
            }
        }

        if (message.getMessage() != null && TextUtils.isEmpty(message.getMessage())){
            holder.photoImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, PhotoMessageActivity.class);
                    intent.putExtra(ChatsAdapter.EXTRA_ROOM_ID, message.getRoom_id());
                    mContext.startActivity(intent);
                }
            });
        }else {
            holder.photoImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, PhotoMessageActivity.class);
                    intent.putExtra(ChatsAdapter.EXTRA_MESSAGE_ID, message.getMessage());
                    intent.putExtra(ChatsAdapter.EXTRA_ROOM_ID, message.getRoom_id());
                    mContext.startActivity(intent);
                }
            });
        }

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (showOnClick) {
                    holder.dateTextView.setVisibility(View.GONE);
                    holder.timeRelativeLayout.setVisibility(View.GONE);
                    showOnClick = false;
                } else {
                    showOnClick = true;
                    holder.dateTextView.setVisibility(View.VISIBLE);
                    holder.timeRelativeLayout.setVisibility(View.VISIBLE);
                }
            }
        });

        holder.timeTextView.setText("seen " + DateFormat.format("HH:mm", message.getTime()));

    }

    private void populateReceive(final MessageReceiveViewHolder holder, final int position){
        final Message message = getSnapshot(position).toObject(Message.class);

        //calculate view visibility and add visible views to impression tracker
//        mViewPositionMap.put(holder.itemView, position);
//        visibilityTracker.addView(holder.itemView, 50, message.getMessage_id(),
//                "message", message.getReceiver_id(), message.getSender_id());

        holder.dateTextView.setText(DateFormat.format("dd-MM-yy", message.getTime()));

        if (message.getMessage()!= null &&!message.getMessage().equals("")){
            holder.messageTextView.setVisibility(View.VISIBLE);
            holder.messageTextView.setText(message.getMessage());
        }


        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (showOnClick) {
                    holder.dateTextView.setVisibility(View.GONE);
                    holder.timeRelativeLayout.setVisibility(View.GONE);
                    showOnClick = false;
                } else {
                    showOnClick = true;
                    holder.dateTextView.setVisibility(View.VISIBLE);
                    holder.timeRelativeLayout.setVisibility(View.VISIBLE);
                }
            }
        });

        holder.timeTextView.setText("seen " + DateFormat.format("HH:mm", message.getTime()));
    }
}

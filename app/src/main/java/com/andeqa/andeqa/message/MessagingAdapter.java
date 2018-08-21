package com.andeqa.andeqa.message;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.R;
import com.andeqa.andeqa.models.Message;
import com.andeqa.andeqa.models.Room;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MessagingAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private final String TAG = MessagingAdapter.class.getSimpleName();
    private Context mContext;
    private static final String KEY_LAYOUT_POSITION = "layout pooition";
    private static final String EXTRA_USER_UID = "uid";
    private static final String EXTRA_MESSAGE_ID = "message id";
    private static final String EXTRA_ROOM_ID = "roomId";
    private static final int MAX_WIDTH = 200;
    private static final int MAX_HEIGHT = 200;
    private static final int SEND_TYPE=0;
    private static final int RECEIVE_TYPE=1;
    private static final int SEND_PHOTO=2;
    private static final int RECEIVE_PHOTO=3;
    private FirebaseAuth firebaseAuth;
    private CollectionReference messagesCollection;
    private CollectionReference roomCollection;
    private DatabaseReference databaseReference;
    private Query roomQuery;
    private Query messagingUsersQuery;
    private boolean showOnClick = true;
    private List<DocumentSnapshot> documentSnapshots = new ArrayList<>();

    public MessagingAdapter(Context mContext) {
        this.mContext = mContext;
    }

    public void setProfileMessages(List<DocumentSnapshot> messages){
        this.documentSnapshots = messages;
        notifyDataSetChanged();
    }


    protected DocumentSnapshot getSnapshot(int index) {
        return documentSnapshots.get(index);
    }

    @Override
    public int getItemViewType(int position) {
        Message message = getSnapshot(position).toObject(Message.class);
        final String senderUid = message.getSender_id();
        firebaseAuth = FirebaseAuth.getInstance();

        if (senderUid.equals(firebaseAuth.getCurrentUser().getUid())){
            if (message.getType() != null && message.getType().equals("text")){
                return SEND_TYPE;
            }else if (message.getType() != null && message.getType().equals("photo")){
                return SEND_PHOTO;
            }else {
                return SEND_TYPE;
            }
        }else {
            if (message.getType() != null && message.getType().equals("text")){
                return RECEIVE_TYPE;
            }else if (message.getType() != null && message.getType().equals("photo")){
                return RECEIVE_PHOTO;
            }else {
                return RECEIVE_TYPE;
            }
        }

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        switch (viewType){
            case SEND_TYPE:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.layout_sent_text_message, parent, false);
                return new MessageSendViewHolder(view);
            case SEND_PHOTO:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.layout_sent_photo_message, parent, false);
                return  new MessageSentPhotoViewHolder(view);
            case RECEIVE_TYPE:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.layout_message_receive, parent, false);
                return  new MessageReceiveViewHolder(view);
            case RECEIVE_PHOTO:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.layout_receive_photo_message, parent, false);
                return new MessageReceivePhotoViewHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() != null){

            Message message = getSnapshot(position).toObject(Message.class);
            final String uid = message.getSender_id();
            final String type = message.getType();

            if (uid.equals(firebaseAuth.getCurrentUser().getUid())){
                if (type != null && type.equals("text")){
                    populateSend((MessageSendViewHolder)holder, position);
                }else if (type != null && type.equals("photo")){
                    populateSendPhoto((MessageSentPhotoViewHolder)holder, position);
                }else {
                    populateSend((MessageSendViewHolder)holder, position);
                }
            }else {

                if (type != null && type.equals("text")){
                    populateReceive((MessageReceiveViewHolder)holder, position);
                }else if (type != null && type.equals("photo")){
                    populateReceivePhoto((MessageReceivePhotoViewHolder) holder, position);
                }else {
                    populateReceive((MessageReceiveViewHolder)holder, position);
                }

            }

        }
    }

    @Override
    public int getItemCount() {
        return documentSnapshots.size();
    }

    private void populateSendPhoto(final MessageSentPhotoViewHolder holder, int position) {
        messagesCollection = FirebaseFirestore.getInstance().collection(Constants.MESSAGES);
        messagesCollection.document("room").collection(firebaseAuth.getCurrentUser().getUid());
        roomCollection = FirebaseFirestore.getInstance().collection(Constants.MESSAGES);
        databaseReference = FirebaseDatabase.getInstance().getReference(Constants.MESSAGES);

        final Message message = getSnapshot(position).toObject(Message.class);
        final String messageId = message.getMessage_id();
        final String photo = message.getPhoto();
        final String text = message.getMessage();
        final String roomId = message.getRoom_id();

        holder.timeTextView.setText(DateFormat.format("HH:mm", message.getTime()));

        if (!text.equals("")) {
            holder.photoTextView.setVisibility(View.VISIBLE);
            holder.photoTextView.setText(message.getMessage());
        }

        Glide.with(mContext.getApplicationContext())
                .load(photo)
                .apply(new RequestOptions()
                        .diskCacheStrategy(DiskCacheStrategy.DATA))
                .into(holder.photoImageView);


        holder.photoImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, ViewMessagePhoto.class);
                intent.putExtra(MessagingAdapter.EXTRA_MESSAGE_ID, messageId);
                intent.putExtra(MessagingAdapter.EXTRA_ROOM_ID, roomId);
                mContext.startActivity(intent);
            }
        });

        holder.photoLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (showOnClick) {
                    holder.statusLinearLayout.setVisibility(View.GONE);
                    showOnClick = false;
                } else {
                    showOnClick = true;
                    holder.statusLinearLayout.setVisibility(View.VISIBLE);
                }
            }
        });

    }

    private void populateReceivePhoto(final MessageReceivePhotoViewHolder holder, int position){
        roomCollection = FirebaseFirestore.getInstance().collection(Constants.MESSAGES);

        final Message message = getSnapshot(position).toObject(Message.class);
        final String messageId = message.getMessage_id();
        final String photo = message.getPhoto();
        final String text = message.getMessage();
        final String roomId = message.getRoom_id();

        holder.timeTextView.setText(DateFormat.format("HH:mm", message.getTime()));

        if (!text.equals("")){
            holder.messageTextView.setVisibility(View.VISIBLE);
            holder.messageTextView.setText(message.getMessage());
        }
        Glide.with(mContext.getApplicationContext())
                .load(photo)
                .apply(new RequestOptions()
                        .diskCacheStrategy(DiskCacheStrategy.DATA))
                .into(holder.photoImageView);

        holder.photoImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, ViewMessagePhoto.class);
                intent.putExtra(MessagingAdapter.EXTRA_MESSAGE_ID, messageId);
                intent.putExtra(MessagingAdapter.EXTRA_ROOM_ID, roomId);
                mContext.startActivity(intent);
            }
        });

        holder.photoLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (showOnClick){
                    holder.statusRelativeLayout.setVisibility(View.GONE);
                    showOnClick = false;
                }else {
                    showOnClick = true;
                    holder.statusRelativeLayout.setVisibility(View.VISIBLE);
                }
            }
        });

    }

    private void populateSend(final MessageSendViewHolder holder, int position){
        messagesCollection = FirebaseFirestore.getInstance().collection(Constants.MESSAGES);
        messagesCollection.document("room").collection(firebaseAuth.getCurrentUser().getUid());
        roomCollection = FirebaseFirestore.getInstance().collection(Constants.MESSAGES);


        final Message message = getSnapshot(position).toObject(Message.class);
        final String messageId = message.getMessage_id();
        final String text = message.getMessage();
        holder.timeTextView.setText(DateFormat.format("HH:mm", message.getTime()));

        if (!text.equals("")){
            holder.messageTextView.setText(message.getMessage());
        }

        holder.sendLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (showOnClick){
                    holder.statusLinearLayout.setVisibility(View.GONE);
                    showOnClick = false;
                }else {
                    showOnClick = true;
                    holder.statusLinearLayout.setVisibility(View.VISIBLE);
                }
            }
        });

    }

    private void populateReceive(final MessageReceiveViewHolder holder, int position){
        roomCollection = FirebaseFirestore.getInstance().collection(Constants.MESSAGES);

        final Message message = getSnapshot(position).toObject(Message.class);
        final String messageId = message.getMessage_id();
        final String text = message.getMessage();

        holder.timeTextView.setText(DateFormat.format("HH:mm", message.getTime()));

        if (!text.equals("")){
            holder.messageTextView.setVisibility(View.VISIBLE);
            holder.messageTextView.setText(message.getMessage());
        }


        holder.receiveLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (showOnClick){
                    holder.statusRelativeLayout.setVisibility(View.GONE);
                    showOnClick = false;
                }else {
                    showOnClick = true;
                    holder.statusRelativeLayout.setVisibility(View.VISIBLE);
                }
            }
        });

    }
}

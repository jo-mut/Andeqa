package com.andeqa.andeqa.message;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.R;
import com.andeqa.andeqa.models.Message;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

import static android.media.CamcorderProfile.get;

/**
 * Created by J.EL on 1/4/2018.
 */

public class MessagingAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = MessagingAdapter.class.getSimpleName();
    private Context mContext;
    private static final String KEY_LAYOUT_POSITION = "layout pooition";
    private static final String EXTRA_USER_UID = "uid";
    private static final int MAX_WIDTH = 200;
    private static final int MAX_HEIGHT = 200;
    private static final int SEND_TYPE=0;
    private static final int RECEIVE_TYPE=1;
    private FirebaseAuth firebaseAuth;
    private CollectionReference messagingUsersCollection;
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
    public long getItemId(int position) {
        return super.getItemId(position);

    }

    @Override
    public int getItemCount() {
        return documentSnapshots.size();
    }

    @Override
    public int getItemViewType(int position) {
        Message message = getSnapshot(position).toObject(Message.class);
        final String senderUid = message.getSenderUid();
        firebaseAuth = FirebaseAuth.getInstance();

        if (senderUid.equals(firebaseAuth.getCurrentUser().getUid())){
            return SEND_TYPE;
        }else {
            return RECEIVE_TYPE;
        }

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        switch (viewType){
            case SEND_TYPE:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.message_send_layout, parent, false);
                return new MessageSendViewHolder(view);
            case RECEIVE_TYPE:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.message_receive_layout, parent, false);
                return  new MessageReceiveViewHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        firebaseAuth = FirebaseAuth.getInstance();
        Message message = getSnapshot(position).toObject(Message.class);
        final String uid = message.getSenderUid();

        if (uid.equals(firebaseAuth.getCurrentUser().getUid())){
            populateSend((MessageSendViewHolder)holder,position);
        }else {
            populateReceive((MessageReceiveViewHolder)holder, position);

        }
    }

    private void populateSend(final MessageSendViewHolder holder, int position){
        messagingUsersCollection = FirebaseFirestore.getInstance().collection(Constants.MESSAGES);
        messagingUsersCollection.document("room")
                .collection(firebaseAuth.getCurrentUser().getUid());
        Message message = getSnapshot(position).toObject(Message.class);
        final String pushId = message.getMessageId();

        holder.messageTextView.setText(message.getMessage());
        holder.timeTextView.setText(DateFormat.format("HH:mm", message.getTime()));
        holder.dateTextView.setText(DateFormat.format("dd-MMM-yy", message.getTime()));



        holder.sendRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (showOnClick){
                    holder.statusRelativeLayout.setVisibility(View.GONE);
                    holder.dateTextView.setVisibility(View.GONE);
                    showOnClick = false;
                }else {
                    showOnClick = true;
                    holder.statusRelativeLayout.setVisibility(View.VISIBLE);
                    holder.dateTextView.setVisibility(View.VISIBLE);
                }
            }
        });


    }

    private void populateReceive(final MessageReceiveViewHolder holder, int position){

        Message message = getSnapshot(position).toObject(Message.class);
        holder.messageTextView.setText(message.getMessage());
        holder.timeTextView.setText(DateFormat.format("HH:mm", message.getTime()));
        holder.dateTextView.setText(DateFormat.format("dd-MMM-yy", message.getTime()));

        holder.receiveRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (showOnClick){
                    holder.statusRelativeLayout.setVisibility(View.GONE);
                    holder.dateTextView.setVisibility(View.GONE);
                    showOnClick = false;
                }else {
                    showOnClick = true;
                    holder.statusRelativeLayout.setVisibility(View.VISIBLE);
                    holder.dateTextView.setVisibility(View.VISIBLE);
                }
            }
        });

    }


}

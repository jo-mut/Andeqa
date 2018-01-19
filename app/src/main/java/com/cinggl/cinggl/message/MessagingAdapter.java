package com.cinggl.cinggl.message;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cinggl.cinggl.R;
import com.cinggl.cinggl.firestore.FirestoreAdapter;
import com.cinggl.cinggl.models.Message;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

import static android.media.CamcorderProfile.get;

/**
 * Created by J.EL on 1/4/2018.
 */

public class MessagingAdapter extends FirestoreAdapter<RecyclerView.ViewHolder> {
    private static final String TAG = MessagingAdapter.class.getSimpleName();
    private Context mContext;
    private static final String KEY_LAYOUT_POSITION = "layout pooition";
    private static final String EXTRA_USER_UID = "uid";
    private static final int MAX_WIDTH = 200;
    private static final int MAX_HEIGHT = 200;
    public static final int SEND_TYPE=0;
    public static final int RECEIVE_TYPE=1;
    private FirebaseAuth firebaseAuth;
    public boolean showOnClick = true;



    public MessagingAdapter(Query query, Context mContext) {
        super(query);
        this.mContext = mContext;
    }

    @Override
    protected DocumentSnapshot getSnapshot(int index) {
        return super.getSnapshot(index);
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);

    }

    @Override
    public int getItemCount() {
        return super.getItemCount();
    }

    @Override
    public int getItemViewType(int position) {
        Message message = getSnapshot(position).toObject(Message.class);
        final String senderUid = message.getSenderUid();
        final String recepientUid = message.getRecepientUid();
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
        Message message = getSnapshot(position).toObject(Message.class);

        holder.messageTextView.setText(message.getMessage());
        holder.timeTextView.setText(DateFormat.format("(HH:mm:ss)", message.getTimeStamp()));
        holder.dateTextView.setText(DateFormat.format("dd-MMM-yy", message.getTimeStamp()));

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
        holder.timeTextView.setText(DateFormat.format("(HH:mm:ss)", message.getTimeStamp()));
        holder.dateTextView.setText(DateFormat.format("dd-MMM-yy", message.getTimeStamp()));

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

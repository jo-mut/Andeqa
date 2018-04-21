package com.andeqa.andeqa.message;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.format.DateFormat;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.R;
import com.andeqa.andeqa.models.Andeqan;
import com.andeqa.andeqa.models.Room;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
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

import java.util.ArrayList;
import java.util.List;

/**
 * Created by J.EL on 3/30/2018.
 */

public class RoomAdapter extends RecyclerView.Adapter<RoomViewHolder> {
    private static final String TAG = RoomAdapter.class.getSimpleName();
    private static final String EXTRA_ROOM_ID = "roomId";
    private static final String EXTRA_USER_UID = "uid";
    private MessagingAdapter messagingAdapter;
    private FirestoreRecyclerAdapter firestoreRecyclerAdapter;
    private CollectionReference roomCollection;
    private CollectionReference usersCollection;
    private Query roomQuery;
    private FirebaseAuth firebaseAuth;
    private Context mContext;
    private List<DocumentSnapshot> documentSnapshots = new ArrayList<>();


    public RoomAdapter(Context mContext) {
        this.mContext = mContext;
    }

    protected void setChatRooms(List<DocumentSnapshot> mSnapshots){
        this.documentSnapshots = mSnapshots;
        notifyDataSetChanged();
    }

    @Override
    public RoomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_list_layout, parent, false);
        return new RoomViewHolder(view);
    }

    protected DocumentSnapshot getSnapshot(int index) {
        return documentSnapshots.get(index);
    }




    @Override
    public void onBindViewHolder(final RoomViewHolder holder, int position) {
        Room room = getSnapshot(position).toObject(Room.class);
        final String uid = room.getUserId();
        final String lastMessage = room.getMessage();
        final String roomId = room.getRoomId();
        final String status = room.getStatus();

        Log.d("room id", roomId);

        firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() != null){
            roomCollection = FirebaseFirestore.getInstance().collection(Constants.MESSAGES);
            roomQuery = roomCollection.document("rooms")
                    .collection(firebaseAuth.getCurrentUser().getUid());
            usersCollection = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);

        }

        if (status.equals("unRead")){
            holder.statusView.setVisibility(View.VISIBLE);
            holder.roomRelativeLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    roomCollection.document("rooms")
                            .collection(firebaseAuth.getCurrentUser().getUid())
                            .document(uid).update("status", "read");
                    Intent intent = new Intent(mContext, MessagesAccountActivity.class);
                    intent.putExtra(RoomAdapter.EXTRA_ROOM_ID, roomId);
                    intent.putExtra(RoomAdapter.EXTRA_USER_UID, uid);
                    mContext.startActivity(intent);
                }
            });

        }else {
            holder.statusView.setVisibility(View.GONE);
            holder.roomRelativeLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(mContext, MessagesAccountActivity.class);
                    intent.putExtra(RoomAdapter.EXTRA_ROOM_ID, roomId);
                    intent.putExtra(RoomAdapter.EXTRA_USER_UID, uid);
                    mContext.startActivity(intent);
                }
            });
        }

        final String [] strings = lastMessage.split("");

        final int size = strings.length;

        if (size <= 75){
            //setence will not have read more
            holder.lastMessageTextView.setText(lastMessage);
        }else {
            holder.lastMessageTextView.setText(lastMessage.substring(0, 74) + "..." );
        }

        holder.timeTextView.setText(DateFormat.format("HH:mm", room.getTime()));


        //postkey is same as uid
        usersCollection.document(uid).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }


                if (documentSnapshot.exists()){
                    Andeqan cinggulan =  documentSnapshot.toObject(Andeqan.class);
                    final String profileImage = cinggulan.getProfileImage();
                    final String username = cinggulan.getUsername();

                    holder.usernameTextView.setText(username);
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



                }

            }
        });

    }

    @Override
    public int getItemCount() {
        return documentSnapshots.size();
    }


}

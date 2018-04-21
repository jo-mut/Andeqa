package com.andeqa.andeqa.message;

import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.R;
import com.andeqa.andeqa.models.Andeqan;
import com.andeqa.andeqa.models.Message;
import com.andeqa.andeqa.models.Room;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class MessagesAccountActivity extends AppCompatActivity
        implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {
    private static final String TAG = MessagesAccountActivity.class.getSimpleName();
    //bind views
    @Bind(R.id.massagesRecyclerView)RecyclerView mMessagesRecyclerView;
    @Bind(R.id.sendMessageEditText)EditText mSendMessageEditText;
    @Bind(R.id.toolbar)Toolbar mToolBar;
    @Bind(R.id.sendMessageImageView)ImageView mSendMessageImageView;
    @Bind(R.id.senderImageView)CircleImageView mSenderImageView;
    @Bind(R.id.swipeRefreshLayout)SwipeRefreshLayout mSwipeRefreshLayout;

    //firestore references
    private CollectionReference messagesCollection;
    private CollectionReference roomCollection;
    private CollectionReference usersCollection;
    private CollectionReference usersReference;

    private DatabaseReference databaseReference;

    private Query messagesQuery;
    private FirestoreRecyclerAdapter firestoreRecyclerAdapter;
    //firebase auth
    private FirebaseAuth firebaseAuth;
    private String mUid;
    private static final String EXTRA_ROOM_ID = "roomId";
    private static final String EXTRA_USER_UID = "uid";
    private  static final int MAX_WIDTH = 200;
    private static final int MAX_HEIGHT = 200;
    private static final int SEND_TYPE=0;
    private static final int RECEIVE_TYPE=1;
    private MessagingAdapter messagingAdapter;
    private LinearLayoutManager layoutManager;
    private String roomId;
    private boolean processMessage = false;

    private List<String> messagesIds = new ArrayList<>();
    private List<DocumentSnapshot> documentSnapshots = new ArrayList<>();
    private static final int TOTAL_ITEMS = 50;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages_account);
        ButterKnife.bind(this);

        //toolbar
        setSupportActionBar(mToolBar);
        mToolBar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        mToolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        //initilize click listeners
        mSendMessageImageView.setOnClickListener(this);

        //initialize firebase auth
        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null){

            //get the passed uid
            roomId = getIntent().getStringExtra(EXTRA_ROOM_ID);

            mSwipeRefreshLayout.setOnRefreshListener(this);

            if(roomId == null){
                throw new IllegalArgumentException("pass an ROOM ID");
            }

            mUid = getIntent().getStringExtra(EXTRA_USER_UID);
            if(mUid == null){
                throw new IllegalArgumentException("pass an EXTRA_UID");
            }

            //firebase
            databaseReference = FirebaseDatabase.getInstance().getReference(Constants.MESSAGES);
            //initialize references
            messagesCollection = FirebaseFirestore.getInstance().collection(Constants.MESSAGES);
            usersCollection = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);
            roomCollection = FirebaseFirestore.getInstance().collection(Constants.MESSAGES);
            usersReference = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);
            messagesQuery = messagesCollection.document("chat_rooms").collection(roomId);

            setRecyclerView();
            getProfile();
            getSenderProfile();
            setMessages();
            setStatus();

        }

    }

    /**get passed uid user profile*/
    private void getProfile(){
        usersCollection.document(mUid).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (documentSnapshot.exists()){
                    Andeqan cinggulan = documentSnapshot.toObject(Andeqan.class);
                    final String username = cinggulan.getUsername();
                    mToolBar.setTitle(username);
                }
            }
        });
    }


    /**get current user profile*/
    private void getSenderProfile(){
        usersCollection.document(firebaseAuth.getCurrentUser().getUid())
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                        if (e != null) {
                            Log.w(TAG, "Listen error", e);
                            return;
                        }

                        if (documentSnapshot.exists()){
                            Andeqan cinggulan = documentSnapshot.toObject(Andeqan.class);
                            final String profileImage = cinggulan.getProfileImage();

                            Picasso.with(MessagesAccountActivity.this)
                                    .load(profileImage)
                                    .resize(MAX_WIDTH, MAX_HEIGHT)
                                    .onlyScaleDown()
                                    .centerCrop()
                                    .placeholder(R.drawable.profle_image_background)
                                    .networkPolicy(NetworkPolicy.OFFLINE)
                                    .into(mSenderImageView, new Callback() {
                                        @Override
                                        public void onSuccess() {

                                        }

                                        @Override
                                        public void onError() {
                                            Picasso.with(MessagesAccountActivity.this)
                                                    .load(profileImage)
                                                    .resize(MAX_WIDTH, MAX_HEIGHT)
                                                    .onlyScaleDown()
                                                    .centerCrop()
                                                    .placeholder(R.drawable.profle_image_background)
                                                    .into(mSenderImageView);

                                        }
                                    });

                        }
                    }
                });
    }

    private void setRecyclerView(){
        messagingAdapter = new MessagingAdapter(this);
        mMessagesRecyclerView.setAdapter(messagingAdapter);
        mMessagesRecyclerView.setHasFixedSize(false);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setAutoMeasureEnabled(true);
        mMessagesRecyclerView.setLayoutManager(layoutManager);
    }


    private void setStatus(){
        final int snapshotSize = messagingAdapter.getItemCount();

        if (snapshotSize > 0){
            DocumentSnapshot lastVisible = messagingAdapter.getSnapshot(snapshotSize - 1);

            int lastVisibleItem = layoutManager.findFirstCompletelyVisibleItemPosition();
            if (snapshotSize - 1 == lastVisibleItem){
                final Message lastMessage = lastVisible.toObject(Message.class);
                final String messageId = lastMessage.getMessageId();

                roomCollection.document("rooms").collection(messageId).document(messageId)
                        .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                            @Override
                            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                                if (e != null) {
                                    Log.w(TAG, "Listen error", e);
                                    return;
                                }

                                if (documentSnapshot.exists()){
                                    Room room = documentSnapshot.toObject(Room.class);
                                    final String status = room.getStatus();

                                    if (status.equals("read")){
                                        //this message has already been seen
                                    }else {
                                        roomCollection.document("rooms")
                                                .collection(messageId)
                                                .document(messageId).update("status", "read");
                                    }

                                }
                            }
                        });
            }
        }

    }

    private void setMessages(){
        messagesQuery.orderBy("time").limit(TOTAL_ITEMS)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                        if (e != null) {
                            Log.w(TAG, "Listen error", e);
                            return;
                        }

                        if (!documentSnapshots.isEmpty()){
                            //retrieve the first bacth of documentSnapshots
                            for (final DocumentChange change : documentSnapshots.getDocumentChanges()) {
                                switch (change.getType()) {
                                    case ADDED:
                                        onDocumentAdded(change);
                                        mMessagesRecyclerView.scrollToPosition(messagingAdapter.getItemCount() - 1);
                                        break;
                                    case MODIFIED:
                                        onDocumentModified(change);
                                        break;
                                    case REMOVED:
                                        onDocumentRemoved(change);
                                        break;
                                }
                            }

                        }

                    }
                });
    }

    @Override
    public void onRefresh() {
        setNextCollections();
    }

    private void setNextCollections(){
        mSwipeRefreshLayout.setRefreshing(true);
        // Get the last visible document
        final int snapshotSize = messagingAdapter.getItemCount();

        if (snapshotSize == 0){
            mSwipeRefreshLayout.setRefreshing(false);
        }else {
            DocumentSnapshot lastVisible = messagingAdapter.getSnapshot(snapshotSize - 1);

            //retrieve the first bacth of documentSnapshots
            Query nextSellingQuery =messagesCollection.document("chat_rooms").collection(roomId)
                    .orderBy("time")
                    .startAfter(lastVisible)
                    .limit(TOTAL_ITEMS);

            nextSellingQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                    if (e != null) {
                        Log.w(TAG, "Listen error", e);
                        return;
                    }

                    if (!documentSnapshots.isEmpty()){
                        //retrieve the first bacth of documentSnapshots
                        for (final DocumentChange change : documentSnapshots.getDocumentChanges()) {
                            switch (change.getType()) {
                                case ADDED:
                                    onDocumentAdded(change);
                                    break;
                                case MODIFIED:
                                    onDocumentModified(change);
                                    break;
                                case REMOVED:
                                    onDocumentRemoved(change);
                                    break;
                            }
                        }
                        mSwipeRefreshLayout.setRefreshing(false);
                    }else {
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                }
            });
        }
    }

    protected void onDocumentAdded(DocumentChange change) {
        messagesIds.add(change.getDocument().getId());
        documentSnapshots.add(change.getDocument());
        messagingAdapter.setProfileMessages(documentSnapshots);
        messagingAdapter.notifyItemInserted(documentSnapshots.size() -1);
        messagingAdapter.getItemCount();

    }

    protected void onDocumentModified(DocumentChange change) {
        if (change.getOldIndex() == change.getNewIndex()) {
            // Item changed but remained in same position
            documentSnapshots.set(change.getOldIndex(), change.getDocument());
            messagingAdapter.notifyItemChanged(change.getOldIndex());
        } else {
            // Item changed and changed position
            documentSnapshots.remove(change.getOldIndex());
            documentSnapshots.add(change.getNewIndex(), change.getDocument());
            messagingAdapter.notifyItemRangeChanged(0, documentSnapshots.size());
        }
    }

    protected void onDocumentRemoved(DocumentChange change) {
        documentSnapshots.remove(change.getOldIndex());
        messagingAdapter.notifyItemRemoved(change.getOldIndex());
        messagingAdapter.notifyItemRangeChanged(0, documentSnapshots.size());
    }


    /**send messages to a specific user*/
    private void sendMessage(){
        if (!TextUtils.isEmpty(mSendMessageEditText.getText())){

            final String text_message = mSendMessageEditText.getText().toString().trim();
            final long time = new Date().getTime();

            final String documentId = databaseReference.push().getKey();
            final DocumentReference documentReference = messagesCollection.document("chat_rooms")
                    .collection(roomId).document(documentId);

            processMessage = true;
            messagesCollection.document("chat_rooms").collection(roomId)
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                    if (e != null) {
                        Log.w(TAG, "Listen error", e);
                        return;
                    }

                    if (processMessage){
                       if (!documentSnapshots.isEmpty()){

                           final Message message = new Message();
                           message.setMessage(text_message);
                           message.setSenderUid(firebaseAuth.getCurrentUser().getUid());
                           message.setRecepientUid(mUid);
                           message.setTime(time);
                           message.setMessageId(documentId);
                           message.setType("Text");
                           message.setRoomId(roomId);
                           documentReference.set(message);
                           processMessage = false;

                       }else {
                           final Message message = new Message();
                           message.setMessage(text_message);
                           message.setSenderUid(firebaseAuth.getCurrentUser().getUid());
                           message.setRecepientUid(mUid);
                           message.setTime(time);
                           message.setMessageId(documentId);
                           message.setType("Text");
                           message.setRoomId(roomId);
                           documentReference.set(message);
                           processMessage = false;

                       }
                   }
                }
            });

            DocumentReference receipientReference = roomCollection.document("rooms")
                    .collection(mUid).document(firebaseAuth.getCurrentUser().getUid());
            DocumentReference senderReference = roomCollection.document("rooms")
                    .collection(firebaseAuth.getCurrentUser().getUid())
                    .document(mUid);
            DocumentReference statusReference = roomCollection.document("rooms")
                    .collection(documentId).document(documentId);
            Room room = new Room();
            room.setUserId(mUid);
            room.setMessage(text_message);
            room.setTime(time);
            room.setRoomId(roomId);
            room.setStatus("unRead");
            receipientReference.set(room);
            senderReference.set(room);
            statusReference.set(room);
            mSendMessageEditText.setText("");

        }else {
            mSendMessageEditText.setError("");
        }
    }



    @Override
    public void onClick(View v){
        if (v == mSendMessageImageView){
            sendMessage();
        }
    }


    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}

package com.andeqa.andeqa.message;

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

import java.util.Date;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class MessagesAccountActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = MessagesAccountActivity.class.getSimpleName();
    //bind views
    @Bind(R.id.massagesRecyclerView)RecyclerView mMessagesRecyclerView;
    @Bind(R.id.sendMessageEditText)EditText mSendMessageEditText;
    @Bind(R.id.toolbar)Toolbar mToolBar;
    @Bind(R.id.sendMessageImageView)ImageView mSendMessageImageView;
    @Bind(R.id.senderImageView)CircleImageView mSenderImageView;
    //firestore references
    private CollectionReference messagesCollection;
    private CollectionReference messagingUsersCollection;
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
            messagingUsersCollection = FirebaseFirestore.getInstance().collection(Constants.MESSAGES);
            usersReference = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);
            messagesQuery = messagesCollection.document("rooms").collection(roomId);


            getProfile();
            getSenderProfile();
            getMessages();
//            scrollToBottom();

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
                    Andeqan andeqan = documentSnapshot.toObject(Andeqan.class);
                    final String username = andeqan.getUsername();
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
                            Andeqan andeqan = documentSnapshot.toObject(Andeqan.class);
                            final String profileImage = andeqan.getProfileImage();

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

    /**get all the messages between two users chatting*/
    private void getMessages(){
        messagesQuery.orderBy("timeStamp")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (!documentSnapshots.isEmpty()){
                    Log.d("messages count", documentSnapshots.size() + "");
                    messagingAdapter = new MessagingAdapter(messagesQuery, MessagesAccountActivity.this);
                    messagingAdapter.startListening();
                    layoutManager = new LinearLayoutManager(MessagesAccountActivity.this);
                    mMessagesRecyclerView.setHasFixedSize(false);
                    mMessagesRecyclerView.setLayoutManager(layoutManager);
                    messagingAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
                        @Override
                        public void onItemRangeChanged(int positionStart, int itemCount) {
                            mMessagesRecyclerView.smoothScrollToPosition(messagingAdapter.getItemCount());
                        }
                    });

                    mMessagesRecyclerView.setAdapter(messagingAdapter);

                }
            }
        });
    }


    /**send messages to a specific user*/
    private void sendMessage(){
        if (!TextUtils.isEmpty(mSendMessageEditText.getText())){

            final String text_message = mSendMessageEditText.getText().toString().trim();
            final long time = new Date().getTime();

            final String documentId = databaseReference.push().getKey();

            processMessage = true;
            messagesCollection.document("rooms").collection(roomId)
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                    if (e != null) {
                        Log.w(TAG, "Listen error", e);
                        return;
                    }

                    if (processMessage){
                       if (!documentSnapshots.isEmpty()){
                           DocumentReference documentReference = messagesCollection.document("rooms")
                                   .collection(roomId).document(documentId);
                           final Message message = new Message();
                           message.setMessage(text_message);
                           message.setSenderUid(firebaseAuth.getCurrentUser().getUid());
                           message.setRecepientUid(mUid);
                           message.setTimeStamp(time);
                           message.setPushId(documentId);
                           message.setType("Message");
                           message.setRoomId(roomId);
                           documentReference.set(message);
                           processMessage = false;

                       }else {
                           DocumentReference documentReference = messagesCollection.document("rooms")
                                   .collection(roomId).document(documentId);
                           final Message message = new Message();
                           message.setMessage(text_message);
                           message.setSenderUid(firebaseAuth.getCurrentUser().getUid());
                           message.setRecepientUid(mUid);
                           message.setTimeStamp(time);
                           message.setPushId(documentId);
                           message.setType("Message");
                           message.setRoomId(roomId);
                           documentReference.set(message);
                           processMessage = false;

                       }
                   }
                }
            });

            DocumentReference messagingReference = messagingUsersCollection.document("room")
                    .collection(mUid).document(firebaseAuth.getCurrentUser().getUid());
            Room room = new Room();
            room.setUid(firebaseAuth.getCurrentUser().getUid());
            room.setMessage(text_message);
            room.setTime(time);
            room.setPushId(firebaseAuth.getCurrentUser().getUid());
            room.setRoomId(roomId);
            room.setStatus("unRead");
            messagingReference.set(room);
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
}

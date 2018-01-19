package com.cinggl.cinggl.message;

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

import com.cinggl.cinggl.Constants;
import com.cinggl.cinggl.R;
import com.cinggl.cinggl.models.Cinggulan;
import com.cinggl.cinggl.models.Message;
import com.cinggl.cinggl.models.MessagingUser;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
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
    private String roomId;

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

            //initialize references
            messagesCollection = FirebaseFirestore.getInstance().collection(Constants.MESSAGES);
            usersCollection = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);
            messagingUsersCollection = FirebaseFirestore.getInstance().collection(Constants.MESSAGES);
            usersReference = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);
            messagesQuery = messagesCollection.document("rooms").collection(roomId);

            getProfile();
            getSenderProfile();
            getMessages();

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
                    Cinggulan cinggulan = documentSnapshot.toObject(Cinggulan.class);
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
                            Cinggulan cinggulan = documentSnapshot.toObject(Cinggulan.class);
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
                    mMessagesRecyclerView.setAdapter(messagingAdapter);
                    LinearLayoutManager layoutManager = new LinearLayoutManager(MessagesAccountActivity.this);
                    mMessagesRecyclerView.setLayoutManager(layoutManager);
                }
            }
        });
    }

    /**send messages to a specific user*/
    private void sendMessage(){
        if (!TextUtils.isEmpty(mSendMessageEditText.getText())){

            final String documentId = messagesCollection.getId();
            final String text_message = mSendMessageEditText.getText().toString().trim();
            final long time = new Date().getTime();
            final Message message = new Message();
            message.setMessage(text_message);
            message.setSenderUid(firebaseAuth.getCurrentUser().getUid());
            message.setRecepientUid(mUid);
            message.setTimeStamp(time);

            DocumentReference documentReference = messagesCollection.document("rooms")
                    .collection(roomId).document();
            message.setPushId(documentId);
            documentReference.set(message);

            DocumentReference messagingReference = messagingUsersCollection.document("messaging users")
                    .collection(mUid).document(firebaseAuth.getCurrentUser().getUid());
            final String docId = messagingReference.getId();
            MessagingUser messagingUser = new MessagingUser();
            messagingUser.setUid(firebaseAuth.getCurrentUser().getUid());
            messagingUser.setMessage(text_message);
            messagingUser.setTime(time);
            messagingUser.setPushId(docId);
            messagingUser.setRoomId(roomId);
            messagingReference.set(messagingUser);
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
package com.cinggl.cinggl.message;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import com.cinggl.cinggl.Constants;
import com.cinggl.cinggl.R;
import com.cinggl.cinggl.models.Cinggulan;
import com.cinggl.cinggl.models.Message;
import com.cinggl.cinggl.models.MessagingUser;
import com.cinggl.cinggl.models.Relation;
import com.cinggl.cinggl.people.FollowerProfileActivity;
import com.cinggl.cinggl.people.FollowersActivity;
import com.cinggl.cinggl.profile.PersonalProfileActivity;
import com.cinggl.cinggl.viewholders.MessageViewHolder;
import com.cinggl.cinggl.viewholders.MessagingUserViewHolder;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
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
import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

import static android.os.Build.VERSION_CODES.M;
import static java.lang.System.load;

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
    private static final String EXTRA_USER_UID = "uid";
    private  static final int MAX_WIDTH = 200;
    private static final int MAX_HEIGHT = 200;

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
            mUid = getIntent().getStringExtra(EXTRA_USER_UID);
            if(mUid == null){
                throw new IllegalArgumentException("pass an EXTRA_UID");
            }
            //initialize references
            messagesCollection = FirebaseFirestore.getInstance().collection(Constants.MESSAGES);
            usersCollection = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);
            messagingUsersCollection = FirebaseFirestore.getInstance().collection(Constants.MESSAGES);
            messagesQuery = messagesCollection.document(mUid).collection("messages");
            usersReference = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);


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
        FirestoreRecyclerOptions<Message> options = new FirestoreRecyclerOptions.Builder<Message>()
                .setQuery(messagesQuery, Message.class)
                .build();
        firestoreRecyclerAdapter = new FirestoreRecyclerAdapter<Message, MessageViewHolder>(options) {
            @Override
            protected void onBindViewHolder(final MessageViewHolder holder, int position, Message model) {
                holder.bindMessage(model);
                final String uid = getSnapshots().get(position).getUid();
                final String postKey = getSnapshots().get(position).getPushId();
                final String lastMessage = getSnapshots().get(position).getMessage();

            }

            @Override
            public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

                return null;
            };

            @Override
            public int getItemCount() {
                return super.getItemCount();
            }


            @Override
            public Message getItem(int position) {
                return super.getItem(position);
            }

            @Override
            public int getItemViewType(int position) {
                return super.getItemViewType(position);


            }

            @Override
            public long getItemId(int position) {
                return super.getItemId(position);
            }
        };


    }

    /**send messages to a specific user*/
    private void sendMessage(){
        if (!TextUtils.isEmpty(mSendMessageEditText.getText())){
            DocumentReference documentReference = messagesCollection.document("messages")
                    .collection(mUid).document();
            final String documentId = messagesCollection.getId();
            final String text_message = mSendMessageEditText.getText().toString().trim();
            final long time = new Date().getTime();
            final Message message = new Message();
            message.setMessage(text_message);
            message.setUid(firebaseAuth.getCurrentUser().getUid());
            message.setTimeStamp(time);
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

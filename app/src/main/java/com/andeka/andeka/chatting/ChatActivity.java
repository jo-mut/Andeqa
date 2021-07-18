package com.andeka.andeka.chatting;

import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.andeka.andeka.Constants;
import com.andeka.andeka.R;
import com.andeka.andeka.gallery.GalleryDialogFragment;
import com.andeka.andeka.models.Andeqan;
import com.andeka.andeka.models.Message;
import com.andeka.andeka.models.Room;
import com.andeka.andeka.profile.ProfileActivity;
import com.andeka.andeka.utils.BottomReachedListener;
import com.andeka.andeka.utils.EndlessLinearScrollListener;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ChatActivity extends AppCompatActivity
        implements View.OnClickListener {
    private static final String TAG = ChatActivity.class.getSimpleName();
    //bind views
    @Bind(R.id.massagesRecyclerView)RecyclerView mMessagesRecyclerView;
    @Bind(R.id.sendMessageEditText)EditText mSendMessageEditText;
    @Bind(R.id.toolbar)Toolbar mToolBar;
    @Bind(R.id.sendMessageImageView)ImageView mSendMessageImageView;
    @Bind(R.id.attachFilesImageView)ImageView mGalleryImageView;

    //firebase references
    private CollectionReference messagesCollection;
    private CollectionReference roomCollection;
    private CollectionReference usersCollection;
    private DatabaseReference databaseReference;
    private DatabaseReference seenMessagesReference;
    private Query messagesQuery;
    //firebase auth
    private FirebaseAuth firebaseAuth;
    //memeber strings
    private static final String EXTRA_ROOM_ID = "roomId";
    private static final String EXTRA_USER_UID = "uid";
    private static final String GALLERY_PATH ="gallery image";
    private String mUid;
    private String roomId;
    private String image;
    //booleans
    private ChatsAdapter chatsAdapter;
    // lists
    private int TOTAL_ITEMS = 50;
    private List<DocumentSnapshot> snapshots = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        ButterKnife.bind(this);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationIcon(R.drawable.ic_arrow);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //initilize click listeners
        mSendMessageImageView.setOnClickListener(this);
        mGalleryImageView.setOnClickListener(this);
        //initialize firebase auth
        firebaseAuth = FirebaseAuth.getInstance();
        //get intent extras
        getIntents();
        // init firebase
        initFirebase();
        //update the toolbar
        getProfile();
        // set the recycler view
        setRecyclerView();
        getChatMessages();
        mMessagesRecyclerView.addOnScrollListener(new EndlessLinearScrollListener() {
            @Override
            public void onLoadMore() {
                chatsAdapter.setBottomReachedListener(new BottomReachedListener() {
                    @Override
                    public void onBottomReached(int position) {
                        getNextMessages();
                    }
                });
            }
        });



    }

    @Override
    protected void onStart() {
        super.onStart();
        //set messages
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.messaging_menu, menu);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_view_profile){
            Intent intent = new Intent(ChatActivity.this, ProfileActivity.class);
            intent.putExtra(ChatActivity.EXTRA_USER_UID, mUid);
            startActivity(intent);
        }

//        if (id == R.id.action_block){
//
//        }

//        if (id == R.id.action_clear_messages){
//            Bundle bundle = new Bundle();
//            bundle.putString(ChatActivity.EXTRA_USER_UID, mUid);
//            bundle.putString(ChatActivity.CLEAR_MESSAGES, mUid);
//            ConfirmDeleteFragment confirmDeleteFragment = ConfirmDeleteFragment.newInstance("confirm");
//            confirmDeleteFragment.setArguments(bundle);
//            confirmDeleteFragment.show(getSupportFragmentManager(), "confirm delete fragment");
//
//        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void getIntents(){
        roomId = getIntent().getStringExtra(EXTRA_ROOM_ID);
        mUid = getIntent().getStringExtra(EXTRA_USER_UID);
        image =  getIntent().getStringExtra(GALLERY_PATH);


        if(image != null){
            sendPhoto();
        }

    }

    private void initFirebase(){
        //firebase
        databaseReference = FirebaseDatabase.getInstance().getReference(Constants.RANDOM_PUSH_ID);
        seenMessagesReference = FirebaseDatabase.getInstance().getReference(Constants.MESSAGES);
        //initialize references
        messagesCollection = FirebaseFirestore.getInstance().collection(Constants.MESSAGES);
        usersCollection = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);
        roomCollection = FirebaseFirestore.getInstance().collection(Constants.MESSAGES);
        messagesQuery = messagesCollection.document(firebaseAuth.getCurrentUser().getUid())
                .collection(roomId);
        seenMessagesReference.keepSynced(true);
        databaseReference.keepSynced(true);
    }

    private void setRecyclerView() {
        chatsAdapter = new ChatsAdapter(ChatActivity.this, snapshots);
        mMessagesRecyclerView.setAdapter(chatsAdapter);
        mMessagesRecyclerView.setHasFixedSize(false);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mMessagesRecyclerView.setLayoutManager(layoutManager);
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
                    mToolBar.setTitle(andeqan.getUsername());
                }
            }
        });
    }

    private void getChatMessages(){
        messagesQuery.orderBy("time", Query.Direction.ASCENDING)
                .limit(TOTAL_ITEMS);

        messagesQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable QuerySnapshot documentSnapshots,
                                @javax.annotation.Nullable FirebaseFirestoreException e) {

                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (!documentSnapshots.isEmpty()){
                    for (final DocumentChange documentChange : documentSnapshots.getDocumentChanges()) {
                        switch (documentChange.getType()) {
                            case ADDED:
                                onDocumentAdded(documentChange);
                                mMessagesRecyclerView.scrollToPosition(chatsAdapter.getItemCount() - 1);
                                seenMessages(documentChange);
                                break;
                            case MODIFIED:
                                onDocumentModified(documentChange);
                                break;
                            case REMOVED:
                                onDocumentRemoved(documentChange);
                                break;
                        }
                    }

                }
            }
        });

    }

    private void getNextMessages(){
        DocumentSnapshot last = snapshots.get(snapshots.size() - 1);
        Query nextQuery = messagesQuery.orderBy("time", Query.Direction.ASCENDING)
                .startAfter(last).limit(TOTAL_ITEMS);
        nextQuery.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(final QuerySnapshot documentSnapshots) {

                if (!documentSnapshots.isEmpty()){
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            for (final DocumentChange documentChange : documentSnapshots.getDocumentChanges()) {
                                switch (documentChange.getType()) {
                                    case ADDED:
                                        onDocumentAdded(documentChange);
                                        mMessagesRecyclerView.scrollToPosition(chatsAdapter.getItemCount() - 1);
                                        seenMessages(documentChange);
                                        break;
                                    case MODIFIED:
                                        onDocumentModified(documentChange);
                                        break;
                                    case REMOVED:
                                        onDocumentRemoved(documentChange);
                                        break;
                                }
                            }
                        }
                    }, 4000);

                }
            }
        });

    }

    private void seenMessages(DocumentChange change) {
        Message message = change.getDocument().toObject(Message.class);
        final String receiverUid = message.getReceiver_id();
        final String senderUid = message.getSender_id();
        final String messageId = message.getMessage_id();

        if (receiverUid.equals(firebaseAuth.getCurrentUser().getUid())) {
            seenMessagesReference.child("seen_messages")
                    .child(receiverUid).child(senderUid)
                    .child(senderUid).child("seen").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.exists()){
                        seenMessagesReference.child("seen_messages")
                                .child(receiverUid).child(senderUid)
                                .child(messageId).child("seen").setValue("seen");
                        Log.d("view not seen", messageId);
                    }else {
                        Log.d("view is seen", messageId);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    protected void onDocumentAdded(DocumentChange change) {
        snapshots.add(change.getDocument());
        chatsAdapter.notifyItemInserted(snapshots.size() - 1);
        chatsAdapter.getItemCount();

    }

    protected void onDocumentModified(DocumentChange change) {
        try {
            if (change.getOldIndex() == change.getNewIndex()) {
                // Item changed but remained in same position
                snapshots.set(change.getOldIndex(), change.getDocument());
                chatsAdapter.notifyItemChanged(change.getOldIndex());
            } else {
                // Item changed and changed position
                snapshots.remove(change.getOldIndex());
                snapshots.add(change.getNewIndex(), change.getDocument());
                chatsAdapter.notifyItemRangeChanged(0, snapshots.size());
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    protected void onDocumentRemoved(DocumentChange change) {
        try {
            snapshots.remove(change.getOldIndex());
            chatsAdapter.notifyItemRemoved(change.getOldIndex());
            chatsAdapter.notifyItemRangeChanged(0, snapshots.size());
        }catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onClick(View v){

        if (v==mGalleryImageView){
            Bundle bundle = new Bundle();
            bundle.putString(ChatActivity.EXTRA_USER_UID, mUid);
            bundle.putString(ChatActivity.EXTRA_ROOM_ID, roomId);
            GalleryDialogFragment dialogFragment = GalleryDialogFragment.newInstance();
            dialogFragment.setArguments(bundle);
            dialogFragment.show(getSupportFragmentManager(), "gallery");
        }

        if (v == mSendMessageImageView){
            if (!TextUtils.isEmpty(mSendMessageEditText.getText())){
                final String text_message = mSendMessageEditText.getText().toString().trim();
                final long time = new Date().getTime();

                final String documentId = databaseReference.push().getKey();
                final DocumentReference senderRef = messagesCollection.document(firebaseAuth.getCurrentUser().getUid())
                        .collection(roomId).document(documentId);
                final DocumentReference receiverRef = messagesCollection.document(mUid)
                        .collection(roomId).document(documentId);

                final Message message = new Message();
                message.setMessage(text_message);
                message.setSender_id(firebaseAuth.getCurrentUser().getUid());
                message.setReceiver_id(mUid);
                message.setPhoto("");
                message.setType("text");
                message.setTime(time);
                message.setMessage_id(documentId);
                message.setRoom_id(roomId);
                senderRef.set(message);
                receiverRef.set(message);

                //get the rooms that have been created
                DocumentReference sendReference = roomCollection.document(mUid)
                        .collection("last message")
                        .document(firebaseAuth.getCurrentUser().getUid());

                DocumentReference receiveReference = roomCollection.document(firebaseAuth.getCurrentUser().getUid())
                        .collection("last message")
                        .document(mUid);

                DocumentReference receiverReference = roomCollection.document("last messages")
                        .collection(firebaseAuth.getCurrentUser().getUid())
                        .document(roomId);

                DocumentReference senderReference = roomCollection.document("last messages")
                        .collection(mUid)
                        .document(roomId);

                Room room = new Room();
                room.setReceiver_id(mUid);
                room.setSender_id(firebaseAuth.getCurrentUser().getUid());
                room.setMessage(text_message);
                room.setType("text");
                room.setTime(time);
                room.setRoom_id(roomId);
                room.setStatus("un_read");
                sendReference.set(room);
                receiveReference.set(room);
                receiverReference.set(room);
                senderReference.set(room);
                mSendMessageEditText.setText("");
            }else {
                mSendMessageEditText.setError("");
            }
        }
    }

    /**send messages to a specific user*/
    private void sendPhoto(){
        //get the data from the imageview as bytes
        final File file = new File(image);
        Uri imageUri = Uri.fromFile(file);
        //push id to organise the posts according to time
        final DatabaseReference reference = databaseReference.push();
        final String pushId = reference.getKey();
        final StorageReference storageReference = FirebaseStorage
                .getInstance().getReference()
                .child(Constants.MESSAGES)
                .child("photos")
                .child(pushId);

        if (imageUri != null) {
            final long time = new Date().getTime();
            final String documentId = databaseReference.push().getKey();
            final DocumentReference senderRef = messagesCollection.document(firebaseAuth.getCurrentUser().getUid())
                    .collection(roomId).document(documentId);
            final DocumentReference receiverRef = messagesCollection.document(mUid)
                    .collection(roomId).document(documentId);
            final Message message = new Message();
            message.setSender_id(firebaseAuth.getCurrentUser().getUid());
            message.setReceiver_id(mUid);
            message.setTime(time);
            message.setMessage_id(documentId);
            message.setType("photo");
            message.setRoom_id(roomId);
            if (!TextUtils.isEmpty(mSendMessageEditText.getText())){
                message.setMessage(mSendMessageEditText.getText().toString());
            }
            senderRef.set(message);
            receiverRef.set(message);

            //get the rooms that have been created
            final DocumentReference sendReference = roomCollection.document(mUid)
                    .collection("last message")
                    .document(firebaseAuth.getCurrentUser().getUid());

            final DocumentReference receiveReference = roomCollection.document(firebaseAuth.getCurrentUser().getUid())
                    .collection("last message")
                    .document(mUid);

            final DocumentReference receiverReference = roomCollection.document("last messages")
                    .collection(firebaseAuth.getCurrentUser().getUid())
                    .document(roomId);

            final DocumentReference senderReference = roomCollection.document("last messages")
                    .collection(mUid)
                    .document(roomId);

            UploadTask uploadTask = storageReference.putFile(imageUri);
            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    // Continue with the task to get the download URL
                    return storageReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        final Uri downloadUri = task.getResult();

                        senderRef.update("photo", downloadUri.toString());
                        receiverRef.update("photo", downloadUri.toString());

                        Room room = new Room();
                        room.setReceiver_id(mUid);
                        room.setSender_id(firebaseAuth.getCurrentUser().getUid());
                        room.setMessage(mSendMessageEditText.getText().toString());
                        room.setTime(time);
                        room.setType("photo");
                        room.setRoom_id(roomId);
                        room.setStatus("un_read");
                        sendReference.set(room);
                        receiveReference.set(room);
                        receiverReference.set(room);
                        senderReference.set(room);

                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(ChatActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

            uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                    if (progress == 100.0) {

                    }
                }
            });

        }

    }
    
}

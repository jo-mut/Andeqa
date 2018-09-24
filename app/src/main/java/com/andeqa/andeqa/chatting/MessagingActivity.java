package com.andeqa.andeqa.chatting;

import android.content.Intent;
import android.net.Uri;
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

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.R;
import com.andeqa.andeqa.camera.PicturesActivity;
import com.andeqa.andeqa.models.Andeqan;
import com.andeqa.andeqa.models.Message;
import com.andeqa.andeqa.models.Room;
import com.andeqa.andeqa.profile.ProfileActivity;
import com.andeqa.andeqa.utils.EndlessLinearRecyclerViewOnScrollListener;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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

public class MessagingActivity extends AppCompatActivity
        implements View.OnClickListener {
    private static final String TAG = MessagingActivity.class.getSimpleName();
    //bind views
    @Bind(R.id.massagesRecyclerView)RecyclerView mMessagesRecyclerView;
    @Bind(R.id.sendMessageEditText)EditText mSendMessageEditText;
    @Bind(R.id.toolbar)Toolbar mToolBar;
    @Bind(R.id.sendMessageImageView)ImageView mSendMessageImageView;
    @Bind(R.id.attachFilesImageView)ImageView mGalleryImageView;

    //firestore references
    private CollectionReference messagesCollection;
    private CollectionReference roomCollection;
    private CollectionReference usersCollection;
    private CollectionReference usersReference;

    private DatabaseReference databaseReference;

    private Query messagesQuery;
    //firebase auth
    private FirebaseAuth firebaseAuth;
    private static final String EXTRA_ROOM_ID = "roomId";
    private static final String EXTRA_USER_UID = "uid";
    private static final String GALLERY_PATH ="gallery image";
    private static final String CLEAR_MESSAGES = "clear messages";

    private String mUid;
    private String roomId;
    private String image;

    private static final String MESSAGE_PHOTO_PATH ="gallery image";
    private  static final int MAX_WIDTH = 200;
    private static final int MAX_HEIGHT = 200;
    private static final int SEND_TYPE=0;
    private static final int RECEIVE_TYPE=1;
    private MessagingAdapter messagingAdapter;
    private LinearLayoutManager layoutManager;
    private boolean processMessage = false;

    private List<String> messagesIds = new ArrayList<>();
    private List<DocumentSnapshot> documentSnapshots = new ArrayList<>();
    private static final int TOTAL_ITEMS = 50;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messaging);
        ButterKnife.bind(this);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationIcon(R.drawable.ic_arrow);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        //initilize click listeners
        mSendMessageImageView.setOnClickListener(this);
        mGalleryImageView.setOnClickListener(this);


        //initialize firebase auth
        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null){
            //get the passed uid
            roomId = getIntent().getStringExtra(EXTRA_ROOM_ID);
            mUid = getIntent().getStringExtra(EXTRA_USER_UID);
            image =  getIntent().getStringExtra(GALLERY_PATH);
            //firebase
            databaseReference = FirebaseDatabase.getInstance().getReference(Constants.MESSAGES);
            //initialize references
            messagesCollection = FirebaseFirestore.getInstance().collection(Constants.MESSAGES);
            usersCollection = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);
            roomCollection = FirebaseFirestore.getInstance().collection(Constants.MESSAGES);
            usersReference = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);
            messagesQuery = messagesCollection.document(firebaseAuth.getCurrentUser().getUid())
                    .collection(roomId);
            mMessagesRecyclerView.addOnScrollListener(new EndlessLinearRecyclerViewOnScrollListener() {
                @Override
                public void onLoadMore() {
                    setNextMessages();
                }
            });

            if(image != null){
                sendPhoto();
            }

        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        documentSnapshots.clear();
        getProfile();
        setMessages();
        setRecyclerView();
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
            Intent intent = new Intent(MessagingActivity.this, ProfileActivity.class);
            intent.putExtra(MessagingActivity.EXTRA_USER_UID, mUid);
            startActivity(intent);
        }

//        if (id == R.id.action_block){
//
//        }

//        if (id == R.id.action_clear_messages){
//            Bundle bundle = new Bundle();
//            bundle.putString(MessagingActivity.EXTRA_USER_UID, mUid);
//            bundle.putString(MessagingActivity.CLEAR_MESSAGES, mUid);
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


    private void setRecyclerView(){
        messagingAdapter = new MessagingAdapter(this);
        mMessagesRecyclerView.setAdapter(messagingAdapter);
        mMessagesRecyclerView.setHasFixedSize(false);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mMessagesRecyclerView.setLayoutManager(layoutManager);
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


    private void setNextMessages(){
        // Get the last visible document
        final int snapshotSize = messagingAdapter.getItemCount();

        if (snapshotSize == 0){
        }else {
            DocumentSnapshot lastVisible = messagingAdapter.getSnapshot(snapshotSize - 1);

            //retrieve the first bacth of documentSnapshots
            Query nextSellingQuery =messagesCollection.document(firebaseAuth.getCurrentUser().getUid())
                    .collection(roomId).orderBy("time").startAfter(lastVisible)
                    .limit(TOTAL_ITEMS);

            nextSellingQuery.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot documentSnapshots) {

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
       try {
           documentSnapshots.remove(change.getOldIndex());
           messagingAdapter.notifyItemRemoved(change.getOldIndex());
           messagingAdapter.notifyItemRangeChanged(0, documentSnapshots.size());
       }catch (Exception e){
           e.printStackTrace();
       }
    }

    @Override
    public void onClick(View v){
        if (v==mGalleryImageView){
            Intent intent = new Intent(MessagingActivity.this, PicturesActivity.class);
            intent.putExtra(MessagingActivity.EXTRA_USER_UID, mUid);
            intent.putExtra(MessagingActivity.EXTRA_ROOM_ID, roomId);
            startActivity(intent);
            finish();
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
                message.setTime(time);
                message.setMessage_id(documentId);
                message.setType("text");
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
                    Toast.makeText(MessagingActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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

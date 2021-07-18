package com.andeka.andeka.chatting;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.andeka.andeka.Constants;
import com.andeka.andeka.R;
import com.andeka.andeka.utils.ProportionalImageView;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import butterknife.Bind;
import butterknife.ButterKnife;

public class SendPhotoActivity extends AppCompatActivity implements View.OnClickListener {
    @Bind(R.id.photoImageView)ProportionalImageView mPhotoImageView;
    @Bind(R.id.sendMessageImageView)ImageView sendMessageImageView;
    @Bind(R.id.sendMessageEditText)EditText mSendMessageEditText;


    //firestore references
    private CollectionReference messagesCollection;
    private CollectionReference roomCollection;
    private DatabaseReference databaseReference;
    private DatabaseReference randomReference;

    private Query messagesQuery;
    //firebase auth
    private FirebaseAuth firebaseAuth;
    private String mUid;
    private String image;
    private static final String EXTRA_ROOM_ID = "roomId";
    private static final String EXTRA_USER_UID = "uid";
    private static final String GALLERY_PATH ="gallery image";

    private String roomId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_photo);
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

        //initialize firebase auth
        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null){

            sendMessageImageView.setOnClickListener(this);
            //get the passed uid
            roomId = getIntent().getStringExtra(EXTRA_ROOM_ID);
            mUid = getIntent().getStringExtra(EXTRA_USER_UID);
            image = getIntent().getStringExtra(GALLERY_PATH);
            if(image != null){
                Glide.with(this)
                        .asBitmap()
                        .load(image)
                        .into(mPhotoImageView);
            }
            //firebase
            databaseReference = FirebaseDatabase.getInstance().getReference(Constants.MESSAGES);
            //initialize references
            messagesCollection = FirebaseFirestore.getInstance().collection(Constants.MESSAGES);
            roomCollection = FirebaseFirestore.getInstance().collection(Constants.MESSAGES);
            randomReference = FirebaseDatabase.getInstance().getReference(Constants.RANDOM_PUSH_ID);
            messagesQuery = messagesCollection.document(firebaseAuth.getCurrentUser().getUid())
                    .collection(roomId);

        }

    }


    @Override
    public void onClick(View v){
        if (v == sendMessageImageView){

        }

    }
}

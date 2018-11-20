package com.andeqa.andeqa.chatting;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.R;
import com.andeqa.andeqa.post_detail.ImageViewActivity;
import com.andeqa.andeqa.models.Message;
import com.andeqa.andeqa.utils.ProportionalImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;

public class PhotoMessageActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = ImageViewActivity.class.getSimpleName();
    @Bind(R.id.photoImageView)ProportionalImageView mPhotoImageView;
    @Bind(R.id.toolbar)Toolbar mToolbar;

    private FirebaseAuth firebaseAuth;
    //firestore
    private CollectionReference messageCollection;
    private static final String EXTRA_MESSAGE_ID = "message id";
    private static final String EXTRA_ROOM_ID = "roomId";
    private String mType;
    private String mRoomId;


    private String mMessageId;
    public boolean showOnClick = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_message);
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
        firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser()!=null){
            mPhotoImageView.setOnClickListener(this);


            messageCollection = FirebaseFirestore.getInstance().collection(Constants.MESSAGES);
            setUpCingleDetails();
        }
    }

    private void setUpCingleDetails(){
        if (getIntent().getExtras() != null){
            mMessageId = getIntent().getStringExtra(EXTRA_MESSAGE_ID);
            mRoomId = getIntent().getStringExtra(EXTRA_ROOM_ID);

            messageCollection.document(firebaseAuth.getCurrentUser().getUid())
                    .collection(mRoomId).document(mMessageId)
                    .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                            if (e != null) {
                                Log.w(TAG, "Listen error", e);
                                return;
                            }

                            if (documentSnapshot.exists()){
                                final Message message = documentSnapshot.toObject(Message.class);
                                final String image = message.getPhoto();

                                //set the single image
                                Picasso.with(PhotoMessageActivity.this)
                                        .load(image)
                                        .networkPolicy(NetworkPolicy.OFFLINE)
                                        .into(mPhotoImageView, new Callback() {
                                            @Override
                                            public void onSuccess() {

                                            }

                                            @Override
                                            public void onError() {
                                                Picasso.with(PhotoMessageActivity.this)
                                                        .load(image)
                                                        .into(mPhotoImageView);
                                            }
                                        });

                            }
                        }
                    });

        }

    }

    @Override
    public void onClick(View v){
        if (v == mPhotoImageView){
            if (showOnClick){
                mToolbar.setVisibility(View.GONE);
                showOnClick = false;
            }else {
                showOnClick = true;
                mToolbar.setVisibility(View.VISIBLE);
            }
        }

    }
}

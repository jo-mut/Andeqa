package com.cinggl.cinggl.home;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.cinggl.cinggl.App;
import com.cinggl.cinggl.Constants;
import com.cinggl.cinggl.utils.ProportionalImageView;
import com.cinggl.cinggl.R;
import com.cinggl.cinggl.models.Post;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;

import butterknife.Bind;
import butterknife.ButterKnife;

public class FullImageViewActivity extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG = FullImageViewActivity.class.getSimpleName();
    @Bind(R.id.postImageView)ProportionalImageView mCingleImageView;

    private FirebaseAuth firebaseAuth;
    private String mPostKey;
    //firebase
    private DatabaseReference cinglesRef;
    private DatabaseReference likesRef;
    //firestore
    private CollectionReference cinglesReference;
    private CollectionReference likesReference;
    private CollectionReference commentsReference;
    private Query cingleQuery;
    private Query likesQuery;
    private Query commentsCountQuery;
    //adapters
    private FirestoreRecyclerAdapter firestoreRecyclerAdapter;
    private static final String EXTRA_POST_KEY = "post key";
    public boolean showOnClick = true;
    public Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.full_image_view_activity);
        ButterKnife.bind(this);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseFirestore.setLoggingEnabled(true);


        if (firebaseAuth.getCurrentUser()!=null){
            mCingleImageView.setOnClickListener(this);

            mPostKey = getIntent().getStringExtra(EXTRA_POST_KEY);
            if(mPostKey == null){
                throw new IllegalArgumentException("pass an EXTRA_POST_KEY");
            }

            likesReference = FirebaseFirestore.getInstance().collection(Constants.LIKES);
            commentsReference = FirebaseFirestore.getInstance().collection(Constants.COMMENTS);
            cinglesReference = FirebaseFirestore.getInstance().collection(Constants.POSTS);
            commentsCountQuery = commentsReference;
            //firebase
            cinglesRef = FirebaseDatabase.getInstance().getReference(Constants.POSTS);
            likesRef = FirebaseDatabase.getInstance().getReference(Constants.LIKES);

            cinglesRef.keepSynced(true);
            likesRef.keepSynced(true);

            setUpCingleDetails();
        }
    }

    private void setUpCingleDetails(){

        cinglesReference.document(mPostKey).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (documentSnapshot.exists()){
                    final Post post = documentSnapshot.toObject(Post.class);
                    final String image = post.getCingleImageUrl();
                    Log.d("detailed image", image);

                    //set the post image
                    App.picasso.with(FullImageViewActivity.this)
                            .load(image)
                            .networkPolicy(NetworkPolicy.OFFLINE)
                            .into(mCingleImageView, new Callback() {
                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onError() {
                                    App.picasso.with(FullImageViewActivity.this)
                                            .load(image)
                                            .into(mCingleImageView);
                                }
                            });

                }
            }
        });

    }

    @Override
    public void onClick(View v){
        if (v == mCingleImageView){
            if (showOnClick){
                toolbar.setVisibility(View.GONE);
                showOnClick = false;
            }else {
                showOnClick = true;
                toolbar.setVisibility(View.VISIBLE);
            }
        }

    }
}

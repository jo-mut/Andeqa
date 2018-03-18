package com.andeqa.andeqa.home;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.models.Single;
import com.andeqa.andeqa.utils.ProportionalImageView;
import com.andeqa.andeqa.R;
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
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;

public class FullImageViewActivity extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG = FullImageViewActivity.class.getSimpleName();
    @Bind(R.id.postImageView)ProportionalImageView mCingleImageView;
    @Bind(R.id.toolbar)Toolbar mToolbar;

    private FirebaseAuth firebaseAuth;
    //firebase
    private DatabaseReference cinglesRef;
    private DatabaseReference likesRef;
    //firestore
    private CollectionReference collectionsCollection;
    private CollectionReference likesReference;
    private CollectionReference commentsReference;
    private Query cingleQuery;
    private Query likesQuery;
    private Query commentsCountQuery;
    //adapters
    private FirestoreRecyclerAdapter firestoreRecyclerAdapter;
    private static final String COLLECTION_ID = "collection id";
    private static final String EXTRA_POST_ID = "post id";
    private String mPostId;
    private String mCollectionId;
    public boolean showOnClick = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.full_image_view_activity);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);

        mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseFirestore.setLoggingEnabled(true);


        if (firebaseAuth.getCurrentUser()!=null){
            mCingleImageView.setOnClickListener(this);

            mPostId = getIntent().getStringExtra(EXTRA_POST_ID);
            if(mPostId == null){
                throw new IllegalArgumentException("pass an post id");
            }

            mCollectionId = getIntent().getStringExtra(COLLECTION_ID);
            if(mCollectionId == null){
                throw new IllegalArgumentException("pass a collection id");
            }

            likesReference = FirebaseFirestore.getInstance().collection(Constants.LIKES);
            commentsReference = FirebaseFirestore.getInstance().collection(Constants.COMMENTS);
            collectionsCollection = FirebaseFirestore.getInstance().collection(Constants.COLLECTIONS)
                    .document("collection_posts").collection(mCollectionId);
            commentsCountQuery = commentsReference;
            //firebase
            cinglesRef = FirebaseDatabase.getInstance().getReference(Constants.COLLECTIONS);
            likesRef = FirebaseDatabase.getInstance().getReference(Constants.LIKES);

            cinglesRef.keepSynced(true);
            likesRef.keepSynced(true);

            setUpCingleDetails();
        }
    }

    private void setUpCingleDetails(){

        collectionsCollection.document(mPostId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (documentSnapshot.exists()){
                    final Single single = documentSnapshot.toObject(Single.class);
                    final String image = single.getImage();
                    Log.d("detailed image", image);

                    //set the single image
                    Picasso.with(FullImageViewActivity.this)
                            .load(image)
                            .networkPolicy(NetworkPolicy.OFFLINE)
                            .into(mCingleImageView, new Callback() {
                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onError() {
                                    Picasso.with(FullImageViewActivity.this)
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
                mToolbar.setVisibility(View.GONE);
                showOnClick = false;
            }else {
                showOnClick = true;
                mToolbar.setVisibility(View.VISIBLE);
            }
        }

    }
}

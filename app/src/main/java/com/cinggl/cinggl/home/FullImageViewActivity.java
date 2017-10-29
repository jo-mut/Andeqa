package com.cinggl.cinggl.home;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cinggl.cinggl.Constants;
import com.cinggl.cinggl.ProportionalImageView;
import com.cinggl.cinggl.R;
import com.cinggl.cinggl.models.Cingle;
import com.cinggl.cinggl.models.Cingulan;
import com.cinggl.cinggl.people.FollowerProfileActivity;
import com.cinggl.cinggl.profile.PersonalProfileActivity;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;

import static com.cinggl.cinggl.R.id.commentsCountTextView;
import static java.lang.System.load;

public class FullImageViewActivity extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG = FullImageViewActivity.class.getSimpleName();
    @Bind(R.id.cingleImageView)ProportionalImageView mCingleImageView;
    @Bind(R.id.commentsImageView)ImageView mCommentsImageView;
    @Bind(commentsCountTextView)TextView mCommentsCountTextView;
    @Bind(R.id.likesImageView)ImageView mLikesImageView;
    @Bind(R.id.likesCountTextView)TextView mLikesCountTextView;
    @Bind(R.id.cingleToolLinearLayout)LinearLayout mCingleToolsLinearLayout;
    @Bind(R.id.activity_cingle_detail)LinearLayout mActivityCingleDetail;

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

        if (firebaseAuth.getCurrentUser()!=null){
            mCingleImageView.setOnClickListener(this);
            mLikesImageView.setOnClickListener(this);
            mCommentsImageView.setOnClickListener(this);
            mLikesCountTextView.setOnClickListener(this);

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

            setUpCingleDetails();
            setCingleData();
        }
    }

    public void setCingleData(){
        //get the number of commments in a cingle
        commentsCountQuery.whereEqualTo("pushId", mPostKey)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (!documentSnapshots.isEmpty()){
                    final int commentsCount = documentSnapshots.size();
                    mCommentsCountTextView.setText(commentsCount + "");

                }
            }
        });

        likesRef.child(mPostKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mLikesCountTextView.setText(dataSnapshot.getChildrenCount() +" " + "Likes");

                if (dataSnapshot.child(mPostKey).hasChild(firebaseAuth.getCurrentUser().getUid())){
                    mLikesImageView.setColorFilter(Color.RED);
                }else {
                    mLikesImageView.setColorFilter(Color.BLACK);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


    private void setUpCingleDetails(){

        cinglesRef.child(mPostKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    final Cingle cingle = dataSnapshot.getValue(Cingle.class);
                    final String image = cingle.getCingleImageUrl();

                    //set the cingle image
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

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        cinglesReference.document(mPostKey).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }


                if (documentSnapshot.exists()){
                    final Cingle cingle = documentSnapshot.toObject(Cingle.class);
                    final String image = cingle.getCingleImageUrl();
                    final String uid = cingle.getUid();

                    //set the cingle image
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
                mCingleToolsLinearLayout.setVisibility(View.INVISIBLE);
                toolbar.setVisibility(View.GONE);
                showOnClick = false;
            }else {
                showOnClick = true;
                mCingleToolsLinearLayout.setVisibility(View.VISIBLE);
                toolbar.setVisibility(View.VISIBLE);
            }
        }

        if (v == mCommentsImageView){
            Intent intent =  new Intent(FullImageViewActivity.this, CommentsActivity.class);
            intent.putExtra(FullImageViewActivity.EXTRA_POST_KEY, mPostKey);
            startActivity(intent);
        }

        if (v == mLikesImageView){
            Intent intent =  new Intent(FullImageViewActivity.this, LikesActivity.class);
            intent.putExtra(FullImageViewActivity.EXTRA_POST_KEY, mPostKey);
            startActivity(intent);
        }

    }
}

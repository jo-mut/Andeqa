package com.andeqa.andeqa.home;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.models.CollectionPost;
import com.andeqa.andeqa.models.Impression;
import com.andeqa.andeqa.utils.ProportionalImageView;
import com.andeqa.andeqa.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
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

import butterknife.Bind;
import butterknife.ButterKnife;

public class ImageViewActivity extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG = ImageViewActivity.class.getSimpleName();
    @Bind(R.id.postImageView)ProportionalImageView mPostImageView;
    @Bind(R.id.toolbar)Toolbar mToolbar;
    private FirebaseAuth firebaseAuth;
    //firestore
    private CollectionReference collectionPost;
    private CollectionReference likesReference;
    private DatabaseReference impressionReference;
    private DatabaseReference databaseReference;
    //adapters
    private FirestoreRecyclerAdapter firestoreRecyclerAdapter;
    private static final String COLLECTION_ID = "collection id";
    private static final String EXTRA_POST_ID = "post id";
    private static final String TYPE = "type";
    private String mType;
    private String mPostId;
    private String mCollectionId;
    public boolean showOnClick = true;
    private boolean processCompiledImpression = false;
    private boolean processOverallImpressions = false;
    private boolean processImpression = false;
    private long startTime;
    private long stopTime;
    private long duration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);
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

        mPostImageView.setOnClickListener(this);
        mPostId = getIntent().getStringExtra(EXTRA_POST_ID);
        mCollectionId = getIntent().getStringExtra(COLLECTION_ID);
        mType = getIntent().getStringExtra(TYPE);
        //firestore
        if (mType.equals("single") || mType.equals("single_image_post")){
            collectionPost = FirebaseFirestore.getInstance().collection(Constants.COLLECTIONS_POSTS)
                    .document("singles").collection(mCollectionId);
        }else {
            collectionPost = FirebaseFirestore.getInstance().collection(Constants.COLLECTIONS_POSTS)
                    .document("collections").collection(mCollectionId);
        }

        likesReference = FirebaseFirestore.getInstance().collection(Constants.LIKES);
        setUpCingleDetails();
        //firebase references
        impressionReference = FirebaseDatabase.getInstance().getReference(Constants.VIEWS);
        databaseReference = FirebaseDatabase.getInstance().getReference(Constants.RANDOM_PUSH_ID);
        impressionReference.keepSynced(true);
    }

    private void setUpCingleDetails(){

        collectionPost.document(mPostId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (documentSnapshot.exists()){
                    final CollectionPost collectionPost = documentSnapshot.toObject(CollectionPost.class);
                    final String image = collectionPost.getImage();
                    //set the single image
                    Glide.with(getApplicationContext())
                            .load(image)
                            .apply(new RequestOptions()
                                    .diskCacheStrategy(DiskCacheStrategy.DATA))
                            .into(mPostImageView);

                }
            }
        });

    }

    @Override
    public void onClick(View v){
        if (v == mPostImageView){
            if (showOnClick){
                mToolbar.setVisibility(View.GONE);
                showOnClick = false;
            }else {
                showOnClick = true;
                mToolbar.setVisibility(View.VISIBLE);
            }
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopTime = System.currentTimeMillis();
        duration = stopTime - startTime;
        final long time = System.currentTimeMillis();
        final String impressionId = databaseReference.child("generateId").getKey();
        processImpression = true;
        processCompiledImpression = true;
        processOverallImpressions = true;
        processImpression = true;
        if (duration >= 5000){
            if (processImpression){
                if (duration >= 5000){
                    impressionReference.child("user_views").child(firebaseAuth.getCurrentUser().getUid())
                            .child(mPostId).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (processOverallImpressions){
                                if (dataSnapshot.exists()){
                                    Impression impression = dataSnapshot.getValue(Impression.class);
                                    final String type = impression.getType();
                                    final long recentDuration = impression.getRecent_duration();
                                    final long total_duration = impression.getCompiled_duration();
                                    final long newTotalDuration = total_duration + duration;
                                    final long newRecentDuration = recentDuration + duration;
                                    impressionReference.child("user_views").child(firebaseAuth.getCurrentUser().getUid())
                                            .child(mPostId).child("compiled_duration").setValue(newTotalDuration);
                                    impressionReference.child("user_views").child(firebaseAuth.getCurrentUser().getUid())
                                            .child(mPostId).child("recent_duration").setValue(newRecentDuration)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    impressionReference.child("compiled_views").child(mPostId)
                                                            .addValueEventListener(new ValueEventListener() {
                                                                @Override
                                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                    if (processCompiledImpression){
                                                                        if (dataSnapshot.exists()){
                                                                            Impression impress = dataSnapshot.getValue(Impression.class);
                                                                            if (type.equals("liked")){
                                                                                final long newDuration = impress.getCompiled_duration() + newRecentDuration;
                                                                                impressionReference.child("compiled_views").child(mPostId)
                                                                                        .child("compiled_duration").setValue(newDuration);
                                                                                impressionReference.child("compiled_views").child(mPostId)
                                                                                        .child("recent_duration").setValue(newRecentDuration);
                                                                                impressionReference.child("compiled_views").child(mPostId)
                                                                                        .child("un_compiled_duration").setValue(0);
                                                                                impressionReference.child("user_views").child(firebaseAuth.getCurrentUser().getUid())
                                                                                        .child(mPostId).child("recent_duration").setValue(0);
                                                                            }else if (type.equals("disliked")){
                                                                                final long newDuration = impress.getCompiled_duration() - newRecentDuration;
                                                                                impressionReference.child("compiled_views").child(mPostId)
                                                                                        .child("compiled_duration").setValue(newDuration);
                                                                                impressionReference.child("compiled_views").child(mPostId)
                                                                                        .child("recent_duration").setValue(newRecentDuration);
                                                                                impressionReference.child("compiled_views").child(mPostId)
                                                                                        .child("un_compiled_duration").setValue(0);
                                                                                impressionReference.child("user_views").child(firebaseAuth.getCurrentUser().getUid())
                                                                                        .child(mPostId).child("recent_duration").setValue(0);
                                                                            }else {
                                                                                final long newDuration = impress.getUn_compiled_duration() + newRecentDuration;
                                                                                impressionReference.child("compiled_views").child(mPostId)
                                                                                        .child("un_compiled_duration").setValue(newDuration);
                                                                                impressionReference.child("user_views").child(firebaseAuth.getCurrentUser().getUid())
                                                                                        .child(mPostId).child("recent_duration").setValue(newRecentDuration);
                                                                            }

                                                                            processCompiledImpression = false;
                                                                        }else {
                                                                            Impression impression = new Impression();
                                                                            impression.setCompiled_duration(newTotalDuration);
                                                                            impression.setRecent_duration(newRecentDuration);
                                                                            impression.setUn_compiled_duration(newTotalDuration);
                                                                            impression.setPost_id(mPostId);
                                                                            impression.setUser_id(firebaseAuth.getCurrentUser().getUid());
                                                                            impression.setImpression_id(impressionId);
                                                                            impression.setTime(time);
                                                                            impression.setType("compiled");
                                                                            impressionReference.child("compiled_views").child(mPostId)
                                                                                    .setValue(impression);
                                                                            processCompiledImpression = false;
                                                                        }
                                                                    }
                                                                }

                                                                @Override
                                                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                                                }
                                                            });
                                                }
                                            });
                                    processOverallImpressions = false;

                                }else {
                                    Impression impression = new Impression();
                                    impression.setCompiled_duration(duration);
                                    impression.setRecent_duration(duration);
                                    impression.setUn_compiled_duration(duration);
                                    impression.setPost_id(mPostId);
                                    impression.setUser_id(firebaseAuth.getCurrentUser().getUid());
                                    impression.setImpression_id(impressionId);
                                    impression.setTime(time);
                                    impression.setType("un_compiled");
                                    impressionReference.child("user_views").child(firebaseAuth.getCurrentUser().getUid())
                                            .child(mPostId).setValue(impression);
                                    processOverallImpressions = false;
                                }
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                }

                processImpression = false;
            }
        }
    }
}

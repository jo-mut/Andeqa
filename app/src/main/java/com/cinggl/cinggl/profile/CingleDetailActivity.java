package com.cinggl.cinggl.profile;

import android.content.Intent;
import android.provider.ContactsContract;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.cinggl.cinggl.Constants;
import com.cinggl.cinggl.R;
import com.cinggl.cinggl.camera.CreateCingleActivity;
import com.cinggl.cinggl.home.CingleOutFragment;
import com.cinggl.cinggl.home.CommentsActivity;
import com.cinggl.cinggl.home.LikesActivity;
import com.cinggl.cinggl.models.Cingle;
import com.cinggl.cinggl.models.Like;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.parceler.Parcels;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

public class CingleDetailActivity extends AppCompatActivity implements View.OnClickListener{
    @Bind(R.id.profileImageView)ImageView mProfileImageView;
    @Bind(R.id.cingleImageView)ImageView mCingleImageView;
    @Bind(R.id.accountUsernameTextView)TextView mAccountUsernameTextView;
    @Bind(R.id.cingleTitleTextView)TextView mCingleTitleTextView;
    @Bind(R.id.cingleDescriptionTextView)TextView mCingleDescriptionTextView;
    @Bind(R.id.likesCountTextView) TextView mLikesCountTextView;
    @Bind(R.id.commentsCountTextView)TextView mCommentsCountTextView;
    @Bind(R.id.commentsImageView)ImageView mCommentsImageView;
    @Bind(R.id.likesImageView)ImageView mLikesImageView;
//    @Bind(R.id.senseCreditsTextView)TextView mSenseCreditsTextView;

    private Cingle cingle;
    private DatabaseReference likesRef;
    private DatabaseReference usernameRef;
    private DatabaseReference commentReference;
    private DatabaseReference cinglesReference;
    private static final int MAX_WIDTH = 400;
    private static final int MAX_HEIGHT = 400;
    private String mPostKey;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private static final String TAG = "CingleOutFragment";
    private boolean processLikes = false;
    public static final String EXTRA_POST_KEY = "post key";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cingle_detail);
        ButterKnife.bind(this);

        mCommentsImageView.setOnClickListener(this);
        mLikesImageView.setOnClickListener(this);
        mLikesCountTextView.setOnClickListener(this);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        mPostKey = getIntent().getExtras().getString("cingle_id");
        if(mPostKey == null){
            throw  new IllegalArgumentException("pass the cingle's post key");
        }

        cinglesReference = FirebaseDatabase.getInstance()
                .getReference(Constants.FIREBASE_CINGLES).child(mPostKey);

        commentReference = FirebaseDatabase.getInstance()
                .getReference(Constants.COMMENTS).child(mPostKey);
        usernameRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_USERS)
                .child(firebaseAuth.getCurrentUser().getUid());
        likesRef = FirebaseDatabase.getInstance()
                .getReference(Constants.LIKES);

        cinglesReference.keepSynced(true);
        usernameRef.keepSynced(true);
        cinglesReference.keepSynced(true);

        cinglesReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final Cingle cingle = dataSnapshot.getValue(Cingle.class);

                Picasso.with(CingleDetailActivity.this).load(cingle.getCingleImageUrl())
                        .fit().centerCrop().networkPolicy(NetworkPolicy.OFFLINE)
                        .into(mCingleImageView, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError() {
                                Picasso.with(CingleDetailActivity.this).load(cingle.getCingleImageUrl())
                                        .fit().centerCrop().into(mCingleImageView);

                            }
                        });

                Picasso.with(CingleDetailActivity.this).load(cingle.getProfileImageUrl())
                        .fit().centerCrop()
                        .placeholder(R.drawable.profle_image_background)
                        .networkPolicy(NetworkPolicy.OFFLINE)
                        .into(mProfileImageView, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError() {
                                Picasso.with(CingleDetailActivity.this).load(cingle.getProfileImageUrl())
                                        .fit().centerCrop()
                                        .placeholder(R.drawable.profle_image_background)
                                        .into(mProfileImageView);

                            }
                        });
                mAccountUsernameTextView.setText(cingle.getAccountUserName());
                mCingleTitleTextView.setText(cingle.getTitle());
                mCingleDescriptionTextView.setText(cingle.getDescription());

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        commentReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Log.e(snapshot.getKey(), snapshot.getChildrenCount() + "commentsCount");
                }

                mCommentsCountTextView.setText(dataSnapshot.getChildrenCount() + "");

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        likesRef.child(mPostKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Log.e(snapshot.getKey(), snapshot.getChildrenCount() + "likesCount");
                }

                mLikesCountTextView.setText(dataSnapshot.getChildrenCount() + "" + "Likes");

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void onClick(View v){
        if (v == mLikesImageView){
            processLikes = true;
            likesRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(processLikes){
                        if (dataSnapshot.child(mPostKey).hasChild(firebaseAuth.getCurrentUser().getUid())){
                            likesRef.child(mPostKey)
                                    .removeValue();
                            processLikes = false;
                            onLikeCounter(false);
                        }else {
                            likesRef.child(mPostKey).child(firebaseAuth.getCurrentUser().getUid())
                                    .child("uid").setValue(firebaseAuth.getCurrentUser().getUid());
                            processLikes = false;
                            onLikeCounter(false);
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }

        if(v == mLikesCountTextView){
            Intent intent = new Intent(CingleDetailActivity.this, LikesActivity.class);
            intent.putExtra(CingleDetailActivity.EXTRA_POST_KEY, mPostKey);
            startActivity(intent);
        }

        if(v == mCommentsImageView){
            Intent intent = new Intent(CingleDetailActivity.this, CommentsActivity.class);
            intent.putExtra(CingleDetailActivity.EXTRA_POST_KEY, mPostKey);
            startActivity(intent);
        }

    }

    private void onLikeCounter(final boolean increament){
        likesRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                if(mutableData.getValue() != null){
                    int value = mutableData.getValue(Integer.class);
                    if(increament){
                        value++;
                    }else{
                        value--;
                    }
                    mutableData.setValue(value);
                }
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b,
                                   DataSnapshot dataSnapshot) {
                Log.d(TAG, "likeTransaction:onComplete" + databaseError);

            }
        });
    }

}

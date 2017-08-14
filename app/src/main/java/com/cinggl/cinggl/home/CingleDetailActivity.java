package com.cinggl.cinggl.home;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cinggl.cinggl.Constants;
import com.cinggl.cinggl.ProportionalImageView;
import com.cinggl.cinggl.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;

public class CingleDetailActivity extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG = CingleDetailActivity.class.getSimpleName();
    @Bind(R.id.cingleImageView)ProportionalImageView mCingleImageView;
    @Bind(R.id.commentsImageView)ImageView mCommentsImageView;
    @Bind(R.id.commentsCountTextView)TextView mCommentsCountTextView;
    @Bind(R.id.likesImageView)ImageView mLikesImageView;
    @Bind(R.id.likesCountTextView)TextView mLikesCountTextView;
    @Bind(R.id.cingleToolLinearLayout)LinearLayout mCingleToolsLinearLayout;
    @Bind(R.id.activity_cingle_detail)LinearLayout mActivityCingleDetail;

    private FirebaseAuth firebaseAuth;
    private String mPostKey;
    private DatabaseReference commentReference;
    private DatabaseReference cinglesReference;
    public static final String EXTRA_POST_KEY = "post key";
    public boolean showOnClick = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cingle_detail);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        firebaseAuth = FirebaseAuth.getInstance();

        mCingleImageView.setOnClickListener(this);
        mLikesImageView.setOnClickListener(this);
        mCommentsImageView.setOnClickListener(this);
        mLikesCountTextView.setOnClickListener(this);

        mPostKey = getIntent().getStringExtra(EXTRA_POST_KEY);
        if(mPostKey == null){
            throw new IllegalArgumentException("pass an EXTRA_POST_KEY");
        }

        cinglesReference = FirebaseDatabase.getInstance()
                .getReference(Constants.FIREBASE_CINGLES).child(mPostKey);

        commentReference = FirebaseDatabase.getInstance()
                .getReference(Constants.COMMENTS).child(mPostKey);

        cinglesReference.keepSynced(true);
        commentReference.keepSynced(true);

        setUpCingleDetails();
    }

    public void setUpCingleDetails(){
        cinglesReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    final String image = (String) dataSnapshot.child(Constants.CINGLE_IMAGE).getValue();
                    final String uid = (String) dataSnapshot.child("uid").getValue();

                    //set the cingle image
                    Picasso.with(CingleDetailActivity.this)
                            .load(image)
                            .networkPolicy(NetworkPolicy.OFFLINE)
                            .into(mCingleImageView, new Callback() {
                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onError() {
                                    Picasso.with(CingleDetailActivity.this)
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

    }

    @Override
    public void onClick(View v){
        if (v == mCingleImageView){
            if (showOnClick){
                mCingleToolsLinearLayout.setVisibility(View.INVISIBLE);
                showOnClick = false;
            }else {
                showOnClick = true;
                mCingleToolsLinearLayout.setVisibility(View.VISIBLE);
            }
        }

        if (v == mCommentsImageView){
            Intent intent =  new Intent(CingleDetailActivity.this, CommentsActivity.class);
            intent.putExtra(CingleDetailActivity.EXTRA_POST_KEY, mPostKey);
            startActivity(intent);
        }

        if (v == mLikesImageView){
            Intent intent =  new Intent(CingleDetailActivity.this, LikesActivity.class);
            intent.putExtra(CingleDetailActivity.EXTRA_POST_KEY, mPostKey);
            startActivity(intent);
        }

    }
}

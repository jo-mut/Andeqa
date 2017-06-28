package com.cinggl.cinggl.home;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.cinggl.cinggl.Constants;
import com.cinggl.cinggl.R;
import com.cinggl.cinggl.adapters.CommentAdapter;
import com.cinggl.cinggl.adapters.LikesAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import butterknife.Bind;
import butterknife.ButterKnife;

import static com.cinggl.cinggl.home.CommentsActivity.EXTRA_POST_KEY;

public class LikesActivity extends AppCompatActivity {
    @Bind(R.id.recentLikesRecyclerView)RecyclerView mRecentLikesRecyclerView;

    private LikesAdapter likesAdapter;
    private DatabaseReference likesRef;
    private String mPostKey;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_likes);
        ButterKnife.bind(this);

        mPostKey = getIntent().getStringExtra(EXTRA_POST_KEY);
        if(mPostKey == null){
            throw new IllegalArgumentException("pass an EXTRA_POST_KEY");
        }
        likesRef = FirebaseDatabase.getInstance()
                .getReference(Constants.LIKES).child(mPostKey);
        likesRef.keepSynced(true);

    }


    @Override
    protected void onStart() {
        super.onStart();
        likesAdapter = new LikesAdapter(this, likesRef);
        mRecentLikesRecyclerView.setAdapter(likesAdapter);
        mRecentLikesRecyclerView.setHasFixedSize(false);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setAutoMeasureEnabled(true);
        mRecentLikesRecyclerView.setNestedScrollingEnabled(false);
        mRecentLikesRecyclerView.setLayoutManager(layoutManager);
    }


    @Override
    public void onStop(){
        super.onStop();
        //remove the event listner
        likesAdapter.cleanUpListener();
    }
}

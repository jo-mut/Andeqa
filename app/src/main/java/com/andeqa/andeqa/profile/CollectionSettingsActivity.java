package com.andeqa.andeqa.profile;

import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.andeqa.andeqa.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class CollectionSettingsActivity extends AppCompatActivity {
    //bind views
    @Bind(R.id.chnageNoteRelativeLayout)RelativeLayout mChangeNoteRelativeLayout;
    @Bind(R.id.changeNameRelativeLayout)RelativeLayout mChangeNameRelativeLayout;
    @Bind(R.id.changeCoverRelativeLayout)RelativeLayout mChangeCoverRelativeLayout;
    @Bind(R.id.collectionCoverImageView)ImageView mCollectionCoverImageView;
    @Bind(R.id.collectionNoteTextView)TextView mCollectionNoteTextView;
    @Bind(R.id.collectionNameTextView)TextView mCollectionNameTextView;
    @Bind(R.id.toolbar)Toolbar toolbar;

    private static final String TAG = CollectionSettingsActivity.class.getSimpleName();
    //firestore reference
    private CollectionReference collectionCollection;
    private Query collectionsQuery;
    //firebase auth
    private FirebaseAuth firebaseAuth;
    private String collectionId;
    private String mUid;
    private static final String COLLECTION_ID = "collection id";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection_settings);
        ButterKnife.bind(this);

        firebaseAuth = FirebaseAuth.getInstance();

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        if (firebaseAuth.getCurrentUser() != null){
            collectionId = getIntent().getStringExtra(COLLECTION_ID);
            if(collectionId == null){
                throw new IllegalArgumentException("pass an collection id");
            }

        }

    }
}

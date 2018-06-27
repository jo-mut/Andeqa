package com.andeqa.andeqa.likes;

import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.R;
import com.andeqa.andeqa.utils.EndlessLinearRecyclerViewOnScrollListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class LikesActivity extends AppCompatActivity{
    @Bind(R.id.recentLikesRecyclerView)RecyclerView mRecentLikesRecyclerView;
    @Bind(R.id.emptyLikesRelativeLayout)RelativeLayout mEmptyRelativelayout;

    //firestore
    private CollectionReference likesCollection;
    private Query likesQuery;
    private FirebaseAuth firebaseAuth;
    private CircleImageView profileImageView;
    private String mPostKey;

    private LikesAdapter likesAdapter;
    private static final String TAG = LikesActivity.class.getSimpleName();
    private static final String EXTRA_POST_KEY = "post id";
    private static  final int TOTAL_ITEMS = 30;


    private List<String> likesIds = new ArrayList<>();
    private List<DocumentSnapshot> documentSnapshots = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_likes);
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
        if (firebaseAuth.getCurrentUser() != null){
            mPostKey = getIntent().getStringExtra(EXTRA_POST_KEY);
            //firestore
            likesCollection = FirebaseFirestore.getInstance().collection(Constants.LIKES);
            likesQuery = likesCollection.document(mPostKey).collection("likes")
                    .orderBy("user_id", Query.Direction.DESCENDING).limit(TOTAL_ITEMS);

          mRecentLikesRecyclerView.addOnScrollListener(new EndlessLinearRecyclerViewOnScrollListener() {
              @Override
              public void onLoadMore() {
                  setNextCollections();
              }
          });

        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        loadData();

    }


    @Override
    public void onStop(){
        super.onStop();
    }


    private void loadData(){
        documentSnapshots.clear();
        setRecyclerView();
        setCollections();
    }

    private void setRecyclerView(){
        likesAdapter = new LikesAdapter(this);
        mRecentLikesRecyclerView.setAdapter(likesAdapter);
        mRecentLikesRecyclerView.setHasFixedSize(false);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setAutoMeasureEnabled(true);
        mRecentLikesRecyclerView.setLayoutManager(layoutManager);
    }


    private void setCollections(){
        likesQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                        if (e != null) {
                            Log.w(TAG, "Listen error", e);
                            return;
                        }

                        if (!documentSnapshots.isEmpty()){
                            //retrieve the first bacth of documentSnapshots
                            for (final DocumentChange change : documentSnapshots.getDocumentChanges()) {
                                switch (change.getType()) {
                                    case ADDED:
                                        onDocumentAdded(change);
                                        break;
                                    case MODIFIED:
                                        onDocumentModified(change);
                                        break;
                                    case REMOVED:
                                        onDocumentRemoved(change);
                                        break;
                                }
                            }
                        }

                    }
                });
    }

    private void setNextCollections(){
        // Get the last visible document
        final int snapshotSize = likesAdapter.getItemCount();

        if (snapshotSize == 0){
        }else {
            DocumentSnapshot lastVisible = likesAdapter.getSnapshot(snapshotSize - 1);

            //retrieve the first bacth of documentSnapshots
            Query nextSellingQuery = likesCollection.document(mPostKey)
                    .collection("likes").orderBy("user_id", Query.Direction.DESCENDING)
                    .startAfter(lastVisible).limit(TOTAL_ITEMS);

            nextSellingQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                    if (e != null) {
                        Log.w(TAG, "Listen error", e);
                        return;
                    }

                    if (!documentSnapshots.isEmpty()){
                        //retrieve the first bacth of documentSnapshots
                        for (final DocumentChange change : documentSnapshots.getDocumentChanges()) {
                            switch (change.getType()) {
                                case ADDED:
                                    onDocumentAdded(change);
                                    break;
                                case MODIFIED:
                                    onDocumentModified(change);
                                    break;
                                case REMOVED:
                                    onDocumentRemoved(change);
                                    break;
                            }
                        }
                    }
                }
            });
        }
    }

    protected void onDocumentAdded(DocumentChange change) {
        likesIds.add(change.getDocument().getId());
        documentSnapshots.add(change.getDocument());
        likesAdapter.setPostLikes(documentSnapshots);
        likesAdapter.notifyItemInserted(documentSnapshots.size() -1);
        likesAdapter.getItemCount();

    }

    protected void onDocumentModified(DocumentChange change) {
      try {
          if (change.getOldIndex() == change.getNewIndex()) {
              // Item changed but remained in same position
              documentSnapshots.set(change.getOldIndex(), change.getDocument());
              likesAdapter.notifyItemChanged(change.getOldIndex());
          } else {
              // Item changed and changed position
              documentSnapshots.remove(change.getOldIndex());
              documentSnapshots.add(change.getNewIndex(), change.getDocument());
              likesAdapter.notifyItemRangeChanged(0, documentSnapshots.size());
          }
      }catch (Exception e){
          e.printStackTrace();
      }
    }

    protected void onDocumentRemoved(DocumentChange change) {
     try {
         documentSnapshots.remove(change.getOldIndex());
         likesAdapter.notifyItemRemoved(change.getOldIndex());
         likesAdapter.notifyItemRangeChanged(0, documentSnapshots.size());
     }catch (Exception e){
         e.printStackTrace();
     }
    }
}

package com.andeqa.andeqa.post_detail;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.R;
import com.andeqa.andeqa.home.PostsAdapter;
import com.andeqa.andeqa.models.Post;
import com.andeqa.andeqa.utils.ItemOffsetDecoration;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
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

/**
 * A simple {@link Fragment} subclass.
 */
public class RelatedPostsFragment extends Fragment {
    @Bind(R.id.postsRecyclerView)RecyclerView mPostssRecyclerView;
    private static final String TAG = RelatedPostsFragment.class.getSimpleName();

    //firestore reference
    private CollectionReference postsCollection;
    private Query postsQuery;
    //firebase auth
    private FirebaseAuth firebaseAuth;
    //firestore adapters
    private PostsAdapter postsAdapter;
    private int TOTAL_ITEMS = 10;
    private StaggeredGridLayoutManager layoutManager;
    private static final String EXTRA_POST_ID = "post id";
    private String mPostId;
    private List<String> mSnapshotsIds = new ArrayList<>();
    private List<DocumentSnapshot> mSnapshots = new ArrayList<>();
    private ItemOffsetDecoration itemOffsetDecoration;

    public static RelatedPostsFragment newInstance(String title) {
        RelatedPostsFragment fragment = new RelatedPostsFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        fragment.setArguments(args);
        return fragment;
    }


    public RelatedPostsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_related_posts, container, false);
        ButterKnife.bind(this, view);

        Bundle bundle = getArguments();
        mPostId = bundle.getString(EXTRA_POST_ID);
        //initialize click listener
        mPostId = getActivity().getIntent().getStringExtra(EXTRA_POST_ID);
        postsCollection = FirebaseFirestore.getInstance().collection(Constants.POSTS);
        postsQuery = postsCollection.orderBy("time", Query.Direction.DESCENDING)
                .whereEqualTo("post_id", mPostId).limit(TOTAL_ITEMS);

        return view;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        loadData();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStart() {
        super.onStart();
        mPostssRecyclerView.addItemDecoration(itemOffsetDecoration);
    }

    @Override
    public void onStop() {
        super.onStop();
        mPostssRecyclerView.removeItemDecoration(itemOffsetDecoration);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    private void loadData(){
        mSnapshots.clear();
        setRecyclerView();
        setPosts();
    }

    private void setRecyclerView(){
        // RecyclerView
        postsAdapter = new PostsAdapter(getActivity());
        mPostssRecyclerView.setAdapter(postsAdapter);
        mPostssRecyclerView.setHasFixedSize(false);
        layoutManager = new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL);
        itemOffsetDecoration = new ItemOffsetDecoration(getContext(), R.dimen.item_off_set);
        mPostssRecyclerView.setLayoutManager(layoutManager);

    }

    private void setPosts(){
        postsQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (!documentSnapshots.isEmpty()) {
                    for (final DocumentChange change : documentSnapshots.getDocumentChanges()) {
                        switch (change.getType()) {
                            case ADDED:
                                Post post = change.getDocument().toObject(Post.class);
                                String type = post.getType();
                                if (!post.getPost_id().equals(mPostId)){
                                    onDocumentAdded(change);
                                }
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

    private void setNextPosts(){
        // Get the last visible document
        final int snapshotSize = postsAdapter.getItemCount();

        if (snapshotSize != 0){
            DocumentSnapshot lastVisible = postsAdapter.getSnapshot(snapshotSize - 1);

            //retrieve the first bacth of posts
            Query nextSinglesQuery = postsCollection.orderBy("time", Query.Direction.DESCENDING)
                    .whereEqualTo("post_id", mPostId).startAfter(lastVisible)
                    .limit(TOTAL_ITEMS);
            nextSinglesQuery.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot documentSnapshots) {
                    if (!documentSnapshots.isEmpty()){
                        //retrieve the first bacth of posts
                        for (final DocumentChange change : documentSnapshots.getDocumentChanges()) {
                            switch (change.getType()) {
                                case ADDED:
                                    Post post = change.getDocument().toObject(Post.class);
                                    String type = post.getType();
                                    if (!post.getPost_id().equals(mPostId)){
                                        onDocumentAdded(change);
                                    }
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
        mSnapshotsIds.add(change.getDocument().getId());
        mSnapshots.add(change.getDocument());
        postsAdapter.setDefaultsPosts(mSnapshots);
        postsAdapter.notifyItemInserted(mSnapshots.size() -1);
        postsAdapter.getItemCount();

    }

    protected void onDocumentModified(DocumentChange change) {
        if (change.getOldIndex() == change.getNewIndex()) {
            // Item changed but remained in same position
            mSnapshots.set(change.getOldIndex(), change.getDocument());
            postsAdapter.notifyItemChanged(change.getOldIndex());
        } else {
            // Item changed and changed position
            mSnapshots.remove(change.getOldIndex());
            mSnapshots.add(change.getNewIndex(), change.getDocument());
            postsAdapter.notifyItemRangeChanged(0, mSnapshots.size());
        }
    }

    protected void onDocumentRemoved(DocumentChange change) {
        try{
            mSnapshots.remove(change.getOldIndex());
            postsAdapter.notifyItemRemoved(change.getOldIndex());
            postsAdapter.notifyItemRangeChanged(0, mSnapshots.size());
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    @Override
    public void onResume() {
        super.onResume();

    }

}
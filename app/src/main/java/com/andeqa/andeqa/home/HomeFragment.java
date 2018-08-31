package com.andeqa.andeqa.home;


import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.R;
import com.andeqa.andeqa.models.Post;
import com.andeqa.andeqa.models.QueryOptions;
import com.andeqa.andeqa.models.Relation;
import com.andeqa.andeqa.people.FollowFragment;
import com.andeqa.andeqa.utils.EndlessRecyclerOnScrollListener;
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

import static android.media.CamcorderProfile.get;

/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment{
    @Bind(R.id.postsRecyclerView)RecyclerView postsRecyclerView;
    @Bind(R.id.placeHolderRelativeLayout)RelativeLayout mPlaceHolderRelativeLayout;
    @Bind(R.id.progressBarRelativeLayout)RelativeLayout mProgressBarRelativeLayout;
    @Bind(R.id.peopleRelativeLayout)RelativeLayout peopleRelativeView;
    @Bind(R.id.postRelativeLayout)RelativeLayout postRelativeLayout;

    private static final String TAG = HomeFragment.class.getSimpleName();
    //firestore reference
    private CollectionReference postsCollection;
    private CollectionReference followingCollection;
    private CollectionReference queryOptionsCollections;
    private Query randomPostsQuery;
    private Query nextRandomQuery;
    private CollectionReference usersReference;

    //firebase auth
    private FirebaseAuth firebaseAuth;
    //adapters
    private PostsAdapter postsAdapter;
    private StaggeredGridLayoutManager layoutManager;
    private int TOTAL_ITEMS = 10;
    private List<String> postsIds = new ArrayList<>();
    private List<DocumentSnapshot> posts = new ArrayList<>();
    private ArrayList<Parcelable> snapshots = new ArrayList<>();
    int spanCount = 2; // 3 columns
    int spacing = 5; // 50px
    boolean includeEdge = true;
    private ItemOffsetDecoration itemOffsetDecoration;
    private FragmentManager fragmentManager;
    private boolean processPosts =  false;



    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        ButterKnife.bind(this, view);
        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null){
            //firestore
            postsCollection = FirebaseFirestore.getInstance().collection(Constants.POSTS);
            usersReference = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);
            randomPostsQuery = postsCollection;
            followingCollection = FirebaseFirestore.getInstance().collection(Constants.PEOPLE);
            queryOptionsCollections = FirebaseFirestore.getInstance().collection(Constants.QUERY_OPTIONS);
            postsRecyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener() {
                @Override
                public void onLoadMore() {
                    setNextCollections();
                }
            });

        }

        return view;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadData();
    }


    @Override
    public void onStart() {
        super.onStart();
        postsRecyclerView.addItemDecoration(itemOffsetDecoration);

    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onStop() {
        super.onStop();
        postsRecyclerView.removeItemDecoration(itemOffsetDecoration);
    }

    public void loadData(){
        posts.clear();
        setRecyclerView();
        setCollections();
    }

    private void setRecyclerView(){
        postsAdapter = new PostsAdapter(getActivity());
        postsRecyclerView.setAdapter(postsAdapter);
        postsRecyclerView.setHasFixedSize(false);
        layoutManager = new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL);
        itemOffsetDecoration = new ItemOffsetDecoration(getContext(), R.dimen.item_off_set);
        postsRecyclerView.setLayoutManager(layoutManager);
        ViewCompat.setNestedScrollingEnabled(postsRecyclerView,false);

    }

    private void showUsersToFollow(){
        try {
            FragmentManager fragmentManager = getChildFragmentManager();
            FollowFragment followFragment = new FollowFragment();
            FragmentTransaction ft = fragmentManager.beginTransaction();
            ft.replace(R.id.follow_container, followFragment);
            ft.commit();
        }catch (Exception e1){

        }
    }

    private void setCollections(){
        processPosts = true;
        queryOptionsCollections.document("options")
                .collection(firebaseAuth.getCurrentUser().getUid())
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@javax.annotation.Nullable QuerySnapshot documentSnapshots,
                                        @javax.annotation.Nullable FirebaseFirestoreException e) {

                        if (e != null) {
                            Log.w(TAG, "Listen error", e);
                            return;
                        }


                        if (processPosts){
                            if (!documentSnapshots.isEmpty()){
                                for (final DocumentChange change : documentSnapshots.getDocumentChanges()) {
                                    QueryOptions relation = change.getDocument().toObject(QueryOptions.class);
                                    final String collectionId = relation.getQuery_option();
//                                    final String queryOptions = collectionId + " " + firebaseAuth.getCurrentUser().getUid();
//                                    final String [] words = queryOptions.split(" ");
//                                    for (String word : words){
//
//                                    }

                                    randomPostsQuery.orderBy("time", Query.Direction.DESCENDING)
                                            .whereEqualTo("collection_id", collectionId).limit(TOTAL_ITEMS)
                                            .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                                @Override
                                                public void onEvent(@Nullable QuerySnapshot documentSnapshots,
                                                                    @Nullable FirebaseFirestoreException e) {

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
                                                                    if (type.equals("single_video_post") || type.equals("collection_video_post")){
                                                                        //do not add to the adapter
                                                                    }else {
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

                                    processPosts = false;
                                }
                            }
                        }

                    }
                });

    }

    private void setNextCollections(){
        processPosts = false;
        // Get the last visible document
        final int snapshotSize = postsAdapter.getItemCount();
        if (snapshotSize != 0){
            final DocumentSnapshot lastVisible = postsAdapter.getSnapshot(snapshotSize - 1);
            queryOptionsCollections.document("options")
                    .collection(firebaseAuth.getCurrentUser().getUid())
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@javax.annotation.Nullable QuerySnapshot documentSnapshots,
                                            @javax.annotation.Nullable FirebaseFirestoreException e) {

                            if (e != null) {
                                Log.w(TAG, "Listen error", e);
                                return;
                            }

                            if (processPosts){
                                if (!documentSnapshots.isEmpty()){
                                    for (final DocumentChange change : documentSnapshots.getDocumentChanges()) {
                                        QueryOptions relation = change.getDocument().toObject(QueryOptions.class);
                                        final String collectionId = relation.getQuery_option();

//                                        final String queryOptions = userId + " " + firebaseAuth.getCurrentUser().getUid();
//                                        final String [] words = queryOptions.split("");
//                                        for (String word : words){
//
//                                        }

                                        Query nextSellingQuery = postsCollection.orderBy("time", Query.Direction.DESCENDING)
                                                .whereEqualTo("collection_id", collectionId).startAfter(lastVisible).limit(TOTAL_ITEMS);

                                        nextSellingQuery.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                            @Override
                                            public void onSuccess(QuerySnapshot documentSnapshots) {
                                                if (!documentSnapshots.isEmpty()){
                                                    //retrieve the first bacth of documentSnapshots
                                                    for (final DocumentChange change : documentSnapshots.getDocumentChanges()) {
                                                        switch (change.getType()) {
                                                            case ADDED:
                                                                Post post = change.getDocument().toObject(Post.class);
                                                                String type = post.getType();
                                                                if (type.equals("single_video_post") || type.equals("collection_video_post")){
                                                                    //do not add to the adapter
                                                                }else {
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

                                processPosts = false;

                            }
                        }
                    });

        }
    }


    protected void onDocumentAdded(DocumentChange change) {
        postsIds.add(change.getDocument().getId());
        posts.add(change.getDocument());
        postsAdapter.setRandomPosts(posts);
        postsAdapter.notifyItemInserted(posts.size() -1);
        postsAdapter.getItemCount();

    }

    protected void onDocumentModified(DocumentChange change) {
        try {
            if (change.getOldIndex() == change.getNewIndex()) {
                // Item changed but remained in same position
                posts.set(change.getOldIndex(), change.getDocument());
                postsAdapter.notifyItemChanged(change.getOldIndex());
            } else {
                // Item changed and changed position
                posts.remove(change.getOldIndex());
                posts.add(change.getNewIndex(), change.getDocument());
                postsAdapter.notifyItemRangeChanged(0, posts.size());
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    protected void onDocumentRemoved(DocumentChange change) {
       try {
           posts.remove(change.getOldIndex());
           postsAdapter.notifyItemRemoved(change.getOldIndex());
           postsAdapter.notifyItemRangeChanged(0, posts.size());
       }catch (Exception e) {
           e.printStackTrace();
       }
    }

}

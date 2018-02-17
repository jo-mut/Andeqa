package com.andeqa.andeqa.home;


import android.os.Bundle;
import android.os.Parcelable;
import android.os.Trace;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.R;
import com.andeqa.andeqa.models.Post;
import com.andeqa.andeqa.models.TraceData;
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
    @Bind(R.id.singleOutRecyclerView)RecyclerView singleOutRecyclerView;
    @Bind(R.id.placeHolderRelativeLayout)RelativeLayout mPlaceHolderRelativeLayout;
    @Bind(R.id.progressBar)ProgressBar progressBar;


    private static final String TAG = HomeFragment.class.getSimpleName();
    private static final String KEY_LAYOUT_POSITION = "layout position";
    private Parcelable recyclerViewState;
    //firestore reference
    private CollectionReference cinglesReference;
    private Query randomPostsQuery;
    private DocumentSnapshot lastVisible;

    //firebase auth
    private FirebaseAuth firebaseAuth;
    //adapters
    private MainPostsAdapter mainPostsAdapter;
    private MainPostsViewHolder mainPostsViewHolder;
    private PostsAdapter postsAdapter;
    private PostViewHolder postViewHolder;
    private LinearLayoutManager layoutManager;
    private int TOTAL_ITEMS = 50;
    private List<String> postsIds = new ArrayList<>();
    private List<Post> posts = new ArrayList<>();

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
            cinglesReference = FirebaseFirestore.getInstance().collection(Constants.POSTS);
            randomPostsQuery = cinglesReference.orderBy("timeStamp", Query.Direction.DESCENDING)
                    .limit(TOTAL_ITEMS);

            setAllPosts();

        }


        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setRecyclerView();

        if (singleOutRecyclerView != null){
            progressBar.setVisibility(View.GONE);
        }else {
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    private void setRecyclerView(){
        mainPostsAdapter = new MainPostsAdapter(randomPostsQuery, getContext());
        mainPostsAdapter.startListening();
        singleOutRecyclerView.setAdapter(mainPostsAdapter);
        singleOutRecyclerView.setHasFixedSize(false);
        layoutManager = new LinearLayoutManager(getContext());
        singleOutRecyclerView.setLayoutManager(layoutManager);

    }

    private void setAllPosts(){
        randomPostsQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (documentSnapshots.isEmpty()){
                    mPlaceHolderRelativeLayout.setVisibility(View.VISIBLE);
                }

            }
        });
    }

}

package com.cinggl.cinggl.home;


import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cinggl.cinggl.Constants;
import com.cinggl.cinggl.R;
import com.cinggl.cinggl.adapters.MainPostsAdapter;
import com.cinggl.cinggl.models.Post;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
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

import static android.os.Build.VERSION_CODES.M;
import static com.cinggl.cinggl.R.id.singleOutRecyclerView;

/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {
    @Bind(R.id.singleOutRecyclerView)RecyclerView singleOutRecyclerView;

    private static final String TAG = HomeFragment.class.getSimpleName();
    private static final String KEY_LAYOUT_POSITION = "layout position";
    private Parcelable recyclerViewState;
    //firestore reference
    private CollectionReference cinglesReference;
    private Query randomQuery;
    private Query randomPostsQuery;
    //firebase auth
    private FirebaseAuth firebaseAuth;
    //adapters
    private MainPostsAdapter mainPostsAdapter;
    private DocumentSnapshot lastVisible;
    private List<Post> posts = new ArrayList<>();
    private List<String> cinglesIds = new ArrayList<>();
    private LinearLayoutManager layoutManager;
    private int TOTAL_ITEMS = 4;



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
            randomPostsQuery = cinglesReference;

            latestPosts();

        }



        return view;
    }

    private void latestPosts(){
        randomPostsQuery.orderBy("timeStamp", Query.Direction.DESCENDING)
                .limit(50).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }


                if (!documentSnapshots.isEmpty()){
                    mainPostsAdapter = new MainPostsAdapter(randomPostsQuery, getContext());
                    mainPostsAdapter.startListening();
                    singleOutRecyclerView.setAdapter(mainPostsAdapter);
                    singleOutRecyclerView.setHasFixedSize(false);
                    layoutManager = new LinearLayoutManager(getContext());
                    singleOutRecyclerView.setLayoutManager(layoutManager);
                }
            }
        });
    }


}

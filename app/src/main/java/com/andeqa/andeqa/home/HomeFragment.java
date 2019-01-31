package com.andeqa.andeqa.home;


import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.R;
import com.andeqa.andeqa.camera.CameraActivity;
import com.andeqa.andeqa.main.HomeActivity;
import com.andeqa.andeqa.models.Post;
import com.andeqa.andeqa.settings.PostSettingsFragment;
import com.andeqa.andeqa.utils.BottomReachedListener;
import com.andeqa.andeqa.utils.EndlessLinearScrollListener;
import com.andeqa.andeqa.utils.EndlessStaggeredScrollListener;
import com.andeqa.andeqa.utils.ItemOffsetDecoration;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import org.parceler.Parcel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment{
    // bind views
    @Bind(R.id.homeRecyclerView)RecyclerView homeRecyclerView;
    @Bind(R.id.progressBar) ProgressBar mProgressBar;
    @Bind(R.id.progressRelativeLayout)RelativeLayout mProgressRelativeLayout;
    private static final String TAG = HomeFragment.class.getSimpleName();
    //firestore reference
    private CollectionReference postsCollection;
    private ItemOffsetDecoration itemOffsetDecoration;
    private StaggeredGridLayoutManager layoutManager;
    private PostsAdapter postsAdapter;
    //lists
    private int TOTAL_ITEMS = 20;
    private List<DocumentSnapshot> mSnapshots = new ArrayList<>();
    private HomeActivity mHomeActivity;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

    }

    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        ButterKnife.bind(this, view);
        initFirebase();

        setRecyclerView();
        getHomePosts();

        homeRecyclerView.addOnScrollListener(new EndlessStaggeredScrollListener() {
            @Override
            public void onLoadMore() {
                postsAdapter.setBottomReachedListener(new BottomReachedListener() {
                    @Override
                    public void onBottomReached(int position) {
                        mProgressRelativeLayout.setVisibility(View.VISIBLE);
                        getNextHomePosts();
                    }
                });
            }
        });
        return view;
    }


    @Override
    public void onActivityCreated(@android.support.annotation.Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.home_menu, menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_create){
            Intent intent =  new Intent(getActivity(),CameraActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onStart() {
        super.onStart();
        homeRecyclerView.addItemDecoration(itemOffsetDecoration);
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
    public void onStop() {
        super.onStop();
        homeRecyclerView.removeItemDecoration(itemOffsetDecoration);
    }

    private void initFirebase() {
        postsCollection = FirebaseFirestore.getInstance().collection(Constants.POSTS);
    }

    private void setRecyclerView(){
        postsAdapter = new PostsAdapter(getContext(), mSnapshots);
        postsAdapter.setHasStableIds(true);
        homeRecyclerView.setHasFixedSize(false);
        homeRecyclerView.setAdapter(postsAdapter);
        layoutManager = new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL);
        itemOffsetDecoration = new ItemOffsetDecoration(getContext(), R.dimen.item_off_set);
        homeRecyclerView.setLayoutManager(layoutManager);
    }


    public void getHomePosts(){
        Query query = postsCollection
                .orderBy("time", Query.Direction.DESCENDING).limit(TOTAL_ITEMS);

        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable QuerySnapshot documentSnapshots,
                                @javax.annotation.Nullable FirebaseFirestoreException e) {

                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (!documentSnapshots.isEmpty()){
                    for (final DocumentChange documentChange : documentSnapshots.getDocumentChanges()) {
                        switch (documentChange.getType()) {
                            case ADDED:
                                onDocumentAdded(documentChange);
                                break;
                            case MODIFIED:
                                onDocumentModified(documentChange);
                                break;
                            case REMOVED:
                                onDocumentRemoved(documentChange);
                                break;
                        }
                    }
                    Log.d("home snapshots", mSnapshots.size() + "");

                }
            }
        });

    }


    public void getNextHomePosts(){

        DocumentSnapshot last = mSnapshots.get(mSnapshots.size() - 1);

        Query query = postsCollection.orderBy("time", Query.Direction.DESCENDING)
                .startAfter(last).limit(TOTAL_ITEMS);

        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(final QuerySnapshot documentSnapshots) {

                if (!documentSnapshots.isEmpty()){
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            for (final DocumentChange documentChange : documentSnapshots.getDocumentChanges()) {
                                switch (documentChange.getType()) {
                                    case ADDED:
                                        onDocumentAdded(documentChange);
                                        break;
                                    case MODIFIED:
                                        onDocumentModified(documentChange);
                                        break;
                                    case REMOVED:
                                        onDocumentRemoved(documentChange);
                                        break;
                                }
                            }

                        }
                    }, 4000);
                    mProgressRelativeLayout.setVisibility(View.GONE);
                }else {
                    mProgressRelativeLayout.setVisibility(View.GONE);
                }
            }
        });

    }

    private void showToast(@NonNull String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }


    protected void onDocumentAdded(DocumentChange change) {
        mSnapshots.add(change.getDocument());
        postsAdapter.notifyItemInserted(mSnapshots.size() - 1);
        postsAdapter.getItemCount();

    }

    protected void onDocumentModified(DocumentChange change) {
        try {
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
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    protected void onDocumentRemoved(DocumentChange change) {
        try {
            mSnapshots.remove(change.getOldIndex());
            postsAdapter.notifyItemRemoved(change.getOldIndex());
            postsAdapter.notifyItemRangeChanged(0, mSnapshots.size());
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

}

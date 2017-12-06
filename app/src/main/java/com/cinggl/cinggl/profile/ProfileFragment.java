package com.cinggl.cinggl.profile;


import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cinggl.cinggl.Constants;
import com.cinggl.cinggl.R;
import com.cinggl.cinggl.adapters.ProfilePostsAdapter;
import com.cinggl.cinggl.market.WalletActivity;
import com.cinggl.cinggl.models.Post;
import com.cinggl.cinggl.models.Cinggulan;
import com.cinggl.cinggl.people.PeopleActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment implements View.OnClickListener{
    //BIND VIEWS
    @Bind(R.id.profileCinglesRecyclerView)RecyclerView mProfileCinglesRecyclerView;
    @Bind(R.id.creatorImageView)CircleImageView mProifleImageView;
    @Bind(R.id.fullNameTextView)TextView mFullNameTextView;
    @Bind(R.id.bioTextView)TextView mBioTextView;
    @Bind(R.id.followersCountTextView) TextView mFollowersCountTextView;
    @Bind(R.id.followingCountTextView)TextView mFollowingCountTextView;
    @Bind(R.id.postsCountTextView)TextView mCinglesCountTextView;
    @Bind(R.id.header_cover_image)ImageView mProfileCover;
    @Bind(R.id.editProfileImageView)ImageView mEditProfileImageView;

    private static final String TAG = ProfileFragment.class.getSimpleName();
    //firestore reference
    private CollectionReference cinglesReference;
    private CollectionReference relationsReference;
    private CollectionReference usersReference;
    private com.google.firebase.firestore.Query profileCinglesQuery;
    private Query profileCinglesCountQuery;
    //firebase auth
    private FirebaseAuth firebaseAuth;
    //firestore adapters
    private ProfilePostsAdapter profilePostsAdapter;
    private static final String KEY_LAYOUT_POSITION = "layout pooition";
    private Parcelable recyclerViewState;
    private  static final int MAX_WIDTH = 200;
    private static final int MAX_HEIGHT = 200;
    //posts meber variables
    private List<Post> posts = new ArrayList<>();
    private List<String> cinglesIds = new ArrayList<>();
    private int TOTAL_ITEMS = 4;
    private DocumentSnapshot lastVisible;
    private LinearLayoutManager layoutManager;

    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        ButterKnife.bind(this, view);
        //FIREBASE AUTH
        firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser()!= null){
            usersReference = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);
            relationsReference = FirebaseFirestore.getInstance().collection(Constants.RELATIONS);
            cinglesReference = FirebaseFirestore.getInstance().collection(Constants.POSTS);
            profileCinglesQuery = cinglesReference.orderBy("timeStamp", Query.Direction.DESCENDING)
                    .whereEqualTo("uid", firebaseAuth.getCurrentUser().getUid()).limit(TOTAL_ITEMS);
            profileCinglesCountQuery = cinglesReference.whereEqualTo("uid",
                    firebaseAuth.getCurrentUser().getUid());


            fetchData();
            recyclerViewScrolling();
            //INITIALIZE CLICK LISTENERS
            mEditProfileImageView.setOnClickListener(this);
            mFollowersCountTextView.setOnClickListener(this);
            mFollowingCountTextView.setOnClickListener(this);

        }

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setTheFirstBacthProfileCingles();
        if (savedInstanceState != null){
            recyclerViewState = savedInstanceState.getParcelable(KEY_LAYOUT_POSITION);
            Log.d("Profile saved Instance", "Instance is not null");
        }else {
            Log.d("Saved Instance", "Instance is completely null");
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.profile_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action b item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_wallet){
            Intent intent = new Intent(getActivity(), WalletActivity.class);
            startActivity(intent);
        }

        if (id == R.id.action_signout){
            firebaseAuth.signOut();
            startActivity(new Intent(getActivity(), SignInActivity.class));

        }
        return super.onOptionsItemSelected(item);
    }


    private void fetchData(){
        profileCinglesCountQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (!documentSnapshots.isEmpty()){
                    final int cingleCount = documentSnapshots.size();
                    mCinglesCountTextView.setText(cingleCount + "");
                }else {
                    mCinglesCountTextView.setText("0");
                }
            }
        });

        //get followers count
        relationsReference.document("followers")
                .collection(firebaseAuth.getCurrentUser().getUid())
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                if (documentSnapshots.isEmpty()){
                    mFollowersCountTextView.setText("0");
                }else {
                    mFollowersCountTextView.setText(documentSnapshots.size() + "");
                }
            }
        });

        //get following count
        relationsReference.document("following")
                .collection(firebaseAuth.getCurrentUser().getUid())
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                if (documentSnapshots.isEmpty()){
                    mFollowingCountTextView.setText("0");
                }else {
                    mFollowingCountTextView.setText(documentSnapshots.size() + "");
                }
            }
        });


        usersReference.document(firebaseAuth.getCurrentUser().getUid())
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (documentSnapshot.exists()){
                    final Cinggulan cinggulan = documentSnapshot.toObject(Cinggulan.class);

                    String firstName = cinggulan.getFirstName();
                    String secondName = cinggulan.getSecondName();
                    final String profileImage = cinggulan.getProfileImage();
                    String bio = cinggulan.getBio();
                    final String profileCover = cinggulan.getProfileCover();

                    mFullNameTextView.setText(firstName + " " + secondName);
                    mBioTextView.setText(bio);

                    Picasso.with(getContext())
                            .load(profileImage)
                            .resize(MAX_WIDTH, MAX_HEIGHT)
                            .onlyScaleDown()
                            .centerCrop()
                            .placeholder(R.drawable.profle_image_background)
                            .networkPolicy(NetworkPolicy.OFFLINE)
                            .into(mProifleImageView, new Callback() {
                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onError() {
                                    Picasso.with(getContext())
                                            .load(profileImage)
                                            .resize(MAX_WIDTH, MAX_HEIGHT)
                                            .onlyScaleDown()
                                            .centerCrop()
                                            .placeholder(R.drawable.profle_image_background)
                                            .into(mProifleImageView);

                                }
                            });

                    Picasso.with(getContext())
                            .load(profileCover)
                            .fit()
                            .centerCrop()
                            .networkPolicy(NetworkPolicy.OFFLINE)
                            .into(mProfileCover, new Callback() {
                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onError() {
                                    Picasso.with(getContext())
                                            .load(profileCover)
                                            .fit()
                                            .centerCrop()
                                            .into(mProfileCover);


                                }
                            });
                }
            }
        });
    }


    private void recyclerViewScrolling(){
        mProfileCinglesRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (!recyclerView.canScrollVertically(-1)) {
                    onScrolledToTop();
                } else if (!recyclerView.canScrollVertically(1)) {
                    onScrolledToBottom();
                } else if (dy < 0) {
                    onScrolledUp();
                } else if (dy > 0) {
                    onScrolledDown();
                }
            }
        });
    }

    public void onScrolledUp() {}

    public void onScrolledDown() {}

    public void onScrolledToTop() {

    }

    public void onScrolledToBottom() {
        setNextProfileCingles();
    }

    private void setTheFirstBacthProfileCingles(){
        profileCinglesQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                for (DocumentChange change : documentSnapshots.getDocumentChanges()) {
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
                    onDataChanged();
                }

            }
        });

        // RecyclerView
        profilePostsAdapter = new ProfilePostsAdapter(getContext());
        mProfileCinglesRecyclerView.setAdapter(profilePostsAdapter);
        mProfileCinglesRecyclerView.setHasFixedSize(false);
        layoutManager = new LinearLayoutManager(getContext());
        mProfileCinglesRecyclerView.setLayoutManager(layoutManager);

    }



    private void setNextProfileCingles(){
        profileCinglesQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(final QuerySnapshot profileSnapshots, FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (profileSnapshots.isEmpty()){
                }else {
                    profileCinglesQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(final QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                            if (e != null) {
                                Log.w(TAG, "Listen error", e);
                                return;
                            }

                            if (!documentSnapshots.isEmpty()){
                                //get the last visible document(cingle)
                                lastVisible = documentSnapshots.getDocuments()
                                        .get(documentSnapshots.size() - 1);

                                //query starting from last retrived cingle
                                final Query nextBestCinglesQuery = cinglesReference.orderBy("timeStamp", Query.Direction.DESCENDING)
                                        .whereEqualTo("uid", firebaseAuth.getCurrentUser().getUid()).startAfter(lastVisible);
                                //retrive more cingles if present
                                nextBestCinglesQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
                                    @Override
                                    public void onEvent(final QuerySnapshot snapshots, FirebaseFirestoreException e) {
                                        if (e != null) {
                                            Log.w(TAG, "Listen error", e);
                                            return;
                                        }

                                        Log.d("remaining posts", snapshots.size() + "");

                                        //retrieve cingles depending on the remaining size of the list
                                        if (!snapshots.isEmpty()){
                                            final long lastSize = snapshots.size();
                                            if (lastSize < TOTAL_ITEMS){
                                                nextBestCinglesQuery.limit(lastSize);
                                            }else {
                                                nextBestCinglesQuery.limit(TOTAL_ITEMS);
                                            }

                                            //make sure that the size of snapshot equals item count
                                            if (profilePostsAdapter.getItemCount() < profileSnapshots.size()){
                                                for (DocumentChange change : snapshots.getDocumentChanges()) {
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
                                                    onDataChanged();
                                                }
                                            }

                                        }


                                    }
                                });
                            }

                        }
                    });
                }

            }
        });

    }

    public void onDocumentAdded(DocumentChange change) {
        Post post = change.getDocument().toObject(Post.class);
        cinglesIds.add(change.getDocument().getId());
        posts.add(post);
        profilePostsAdapter.setPosts(posts);
        profilePostsAdapter.getItemCount();
        profilePostsAdapter.notifyItemInserted(posts.size());

    }

    private void onDocumentModified(DocumentChange change) {
        Post post = change.getDocument().toObject(Post.class);
        if (change.getOldIndex() == change.getNewIndex()) {
            // Item changed but remained in same position
            cinglesIds.add(change.getDocument().getId());
            posts.set(change.getNewIndex(), post);
            profilePostsAdapter.notifyItemChanged(change.getOldIndex());

        } else {
            // Item changed and changed position
            posts.remove(change.getOldIndex());
            posts.add(change.getNewIndex(), post);
            profilePostsAdapter.notifyItemMoved(change.getOldIndex(), change.getNewIndex());
        }
    }

    private void onDocumentRemoved(DocumentChange change) {
        String cingle_key = change.getDocument().getId();
        int cingle_index = cinglesIds.indexOf(cingle_key);
        if (cingle_index > -1){
            //remove data from the list
            cinglesIds.remove(change.getDocument().getId());
            profilePostsAdapter.removeAt(change.getOldIndex());
            profilePostsAdapter.notifyItemRemoved(change.getOldIndex());
            profilePostsAdapter.getItemCount();
        }else {
            Log.v(TAG, "onDocumentRemoved:" + cingle_key);
        }

    }

    private void onError(FirebaseFirestoreException e) {};

    private void onDataChanged() {}



    @Override
    public void onSaveInstanceState(Bundle outState) {

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (recyclerViewState != null){
            layoutManager.onRestoreInstanceState(recyclerViewState);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }


    @Override
    public void onClick(View v){
        if (v == mFollowingCountTextView) {
            Intent intent = new Intent(getActivity(), PeopleActivity.class);
            startActivity(intent);
        }

        if (v == mFollowersCountTextView){
            Intent intent = new Intent(getActivity(), PeopleActivity.class);
            startActivity(intent);
        }

        if (v == mEditProfileImageView){
            Intent intent = new Intent(getActivity(), UpdateProfileActivity.class);
            startActivity(intent);
        }

    }

}

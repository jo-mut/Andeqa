package com.andeqa.andeqa.search;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.R;
import com.andeqa.andeqa.chatting.MessagingActivity;
import com.andeqa.andeqa.models.Andeqan;
import com.andeqa.andeqa.models.Room;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
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

public class SearchPeopleActivity extends AppCompatActivity {
    @Bind(R.id.searchPeopleRecyclerView)RecyclerView mUsersRecyclerView;
    @Bind(R.id.toolbar)Toolbar toolbar;
    private CollectionReference usersCollections;
    private CollectionReference postsCollection;
    private CollectionReference collectionsCollection;
    private Query usersQuery;
    private SearchPeopleAdapter searchPeopleAdapter;
    private FirebaseAuth firebaseAuth;
    private List<DocumentSnapshot> documentSnapshots = new ArrayList<>();
    private List<String> searchIds = new ArrayList<>();
    private static final String TAG = SearchPeopleActivity.class.getSimpleName();
    private SearchView searchView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_people);
        //bind views
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!searchView.isIconified()) {
                    searchView.setIconified(true);
                    return;
                }
                finish();
            }
        });
        //initialize firebase auth
        firebaseAuth = FirebaseAuth.getInstance();
        postsCollection = FirebaseFirestore.getInstance().collection(Constants.POSTS);
        usersCollections = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);
        collectionsCollection = FirebaseFirestore.getInstance().collection(Constants.COLLECTIONS);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_search, menu);
        searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchPeopleAdapter.cleanUp();
                documentSnapshots.clear();
                getUsers(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchPeopleAdapter.cleanUp();
                documentSnapshots.clear();
                getUsers(newText);
                return false;
            }
        });

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        setSearchRecyclerView();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        // close search view on back button pressed
        if (!searchView.isIconified()) {
            searchView.setIconified(true);
            return;
        }
        super.onBackPressed();
    }

    private void getUsers(final String name) {
        if (name.isEmpty()){
            searchPeopleAdapter.cleanUp();
        }else {
            usersQuery = usersCollections.orderBy("username").startAt(name).endAt(name + "\uf8ff");
            usersQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot documentSnapshots, @Nullable FirebaseFirestoreException e) {
                    if (e != null) {
                        Log.w(TAG, "Listen error", e);
                        return;
                    }

                    if (!documentSnapshots.isEmpty()){
                        //retrieve the first bacth of documentSnapshots
                        for (DocumentChange documentChange : documentSnapshots.getDocumentChanges()){
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
                }
            });
        }
    }

    private void setSearchRecyclerView(){
        searchPeopleAdapter = new SearchPeopleAdapter(this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mUsersRecyclerView.setAdapter(searchPeopleAdapter);
        mUsersRecyclerView.setHasFixedSize(false);
        mUsersRecyclerView.setLayoutManager(layoutManager);
    }

    protected void onDocumentAdded(DocumentChange change) {
        searchIds.add(change.getDocument().getId());
        documentSnapshots.add(change.getDocument());
        searchPeopleAdapter.notifyItemInserted(documentSnapshots.size() - 1);
        searchPeopleAdapter.getItemCount();
        searchPeopleAdapter.setResults(documentSnapshots);
        Log.d("search result present", documentSnapshots.size() + "");

    }

    protected void onDocumentModified(DocumentChange change) {
        try {
            if (change.getOldIndex() == change.getNewIndex()) {
                // Item changed but remained in same position
                documentSnapshots.set(change.getOldIndex(), change.getDocument());
                searchPeopleAdapter.notifyItemChanged(change.getOldIndex());
            } else {
                // Item changed and changed position
                documentSnapshots.remove(change.getOldIndex());
                documentSnapshots.add(change.getNewIndex(), change.getDocument());
                searchPeopleAdapter.notifyItemRangeChanged(0, documentSnapshots.size());
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    protected void onDocumentRemoved(DocumentChange change) {
        try {
            documentSnapshots.remove(change.getOldIndex());
            searchPeopleAdapter.notifyItemRemoved(change.getOldIndex());
            searchPeopleAdapter.notifyItemRangeChanged(0, documentSnapshots.size());
        }catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static class SearchPeopleAdapter extends RecyclerView.Adapter<SearchPeopleViewHolder>{
        private Context mContext;
        private List<DocumentSnapshot> documentSnapshots = new ArrayList<>();

        private CollectionReference usersCollection;
        private CollectionReference roomsCollection;
        private String roomId;
        private FirebaseAuth firebaseAuth;
        private boolean processRoom = false;
        private static final String EXTRA_USER_UID = "uid";
        private static final String EXTRA_ROOM_UID = "roomId";
        private DatabaseReference databaseReference;

        public SearchPeopleAdapter(Context mContext) {
            this.mContext = mContext;
            initReferences();
        }

        public void setResults(List<DocumentSnapshot> mSnapshots){
            this.documentSnapshots = mSnapshots;
        }

        private void initReferences(){
            firebaseAuth = FirebaseAuth.getInstance();
            usersCollection = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);
            roomsCollection = FirebaseFirestore.getInstance().collection(Constants.MESSAGES);
            databaseReference = FirebaseDatabase.getInstance().getReference(Constants.RANDOM_PUSH_ID);

        }

        public DocumentSnapshot getSnapshot(int index) {
            return documentSnapshots.get(index);
        }


        @Override
        public long getItemId(int position) {
            return super.getItemId(position);
        }

        @Override
        public int getItemCount() {
            Log.d("search result adapter", documentSnapshots.size() + "");
            return documentSnapshots.size();
        }

        @Override
        public int getItemViewType(int position) {
            return super.getItemViewType(position);
        }

        @Override
        public SearchPeopleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.layout_people, parent, false);
            return  new SearchPeopleViewHolder(view);

        }

        @Override
        public void onBindViewHolder(final SearchPeopleViewHolder holder, int position) {
            Andeqan search = documentSnapshots.get(position).toObject(Andeqan.class);
            final String userId = search.getUser_id();
            usersCollection.document(userId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@javax.annotation.Nullable DocumentSnapshot documentSnapshot,
                                    @javax.annotation.Nullable FirebaseFirestoreException e) {
                    if (e != null) {
                        Log.w(TAG, "Listen error", e);
                        return;
                    }

                    if (documentSnapshot.exists()){
                        Andeqan andeqan = documentSnapshot.toObject(Andeqan.class);
                        final String userId = andeqan.getUser_id();
                        final String username = andeqan.getUsername();
                        final String profileImage = andeqan.getProfile_image();
                        final String firstName = andeqan.getFirst_name();
                        final String secondName = andeqan.getSecond_name();
                        Log.d("search post id", userId);

                        holder.usernameTextView.setText(username);
                        holder.fullNameTextView.setText(firstName + " " +  secondName);
                        Glide.with(mContext.getApplicationContext())
                                .load(profileImage)
                                .apply(new RequestOptions()
                                        .placeholder(R.drawable.ic_user)
                                        .diskCacheStrategy(DiskCacheStrategy.DATA))
                                .into(holder.profileImageView);

                        holder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //look to see if current user has a chat history with mUid
                                processRoom = true;
                                roomsCollection.document(userId).collection("last message")
                                        .document(firebaseAuth.getCurrentUser().getUid())
                                        .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                            @Override
                                            public void onEvent(@javax.annotation.Nullable DocumentSnapshot documentSnapshot,
                                                                @javax.annotation.Nullable FirebaseFirestoreException e) {
                                                if (e != null) {
                                                    Log.w(TAG, "Listen error", e);
                                                    return;
                                                }

                                                if (processRoom){
                                                    if (documentSnapshot.exists()){
                                                        Room room = documentSnapshot.toObject(Room.class);
                                                        roomId = room.getRoom_id();
                                                        Intent intent = new Intent(mContext, MessagingActivity.class);
                                                        intent.putExtra(SearchPeopleAdapter.EXTRA_ROOM_UID, roomId);
                                                        intent.putExtra(SearchPeopleAdapter.EXTRA_USER_UID, userId);
                                                        mContext.startActivity(intent);
                                                        processRoom = false;
                                                    }else {
                                                        roomsCollection.document(firebaseAuth.getCurrentUser().getUid())
                                                                .collection("last message")
                                                                .document(userId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                                            @Override
                                                            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                                                                if (e != null) {
                                                                    Log.w(TAG, "Listen error", e);
                                                                    return;
                                                                }

                                                                if (processRoom){
                                                                    if (documentSnapshot.exists()){
                                                                        Room room = documentSnapshot.toObject(Room.class);
                                                                        roomId = room.getRoom_id();
                                                                        Intent intent = new Intent(mContext, MessagingActivity.class);
                                                                        intent.putExtra(SearchPeopleAdapter.EXTRA_ROOM_UID, roomId);
                                                                        intent.putExtra(SearchPeopleAdapter.EXTRA_USER_UID, userId);
                                                                        mContext.startActivity(intent);

                                                                        processRoom = false;

                                                                    }else {
                                                                        //start a chat with mUid since they have no chatting history
                                                                        roomId = databaseReference.push().getKey();
                                                                        Intent intent = new Intent(mContext, MessagingActivity.class);
                                                                        intent.putExtra(SearchPeopleAdapter.EXTRA_ROOM_UID, roomId);
                                                                        intent.putExtra(SearchPeopleAdapter.EXTRA_USER_UID, userId);
                                                                        mContext.startActivity(intent);
                                                                        processRoom = false;
                                                                    }
                                                                }
                                                            }
                                                        });
                                                    }
                                                }

                                            }
                                        });

                            }
                        });

                    }
                }
            });
        }



        public void cleanUp(){
            documentSnapshots.clear();
            notifyDataSetChanged();
        }

    }

    public static class SearchPeopleViewHolder extends RecyclerView.ViewHolder{
        View mView;
        Context mContext;
        public TextView fullNameTextView;
        public CircleImageView profileImageView;
        public TextView usernameTextView;

        public ImageView sendMessageImageView;

        public SearchPeopleViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            mContext = itemView.getContext();
            fullNameTextView = (TextView) itemView.findViewById(R.id.fullNameTextView);
            profileImageView = (CircleImageView) itemView.findViewById(R.id.profileImageView);
            usernameTextView = (TextView) itemView.findViewById(R.id.usernameTextView);
        }
    }

}


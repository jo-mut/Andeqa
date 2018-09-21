package com.andeqa.andeqa.search;

import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.R;
import com.andeqa.andeqa.models.Andeqan;
import com.andeqa.andeqa.models.Collection;
import com.andeqa.andeqa.models.Post;
import com.andeqa.andeqa.models.Search;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class SearchActivity extends AppCompatActivity {
    @Bind(R.id.usersRecyclerView)RecyclerView mUsersRecyclerView;
    @Bind(R.id.toolbar)Toolbar toolbar;
    private CollectionReference usersCollections;
    private CollectionReference postsCollection;
    private CollectionReference collectionsCollection;
    private Query usersQuery;
    private Query postQuery;
    private Query collectionQuery;
    private SearchOverallAdapter searchAdapter;
    private FirebaseAuth firebaseAuth;
    private List<String> documentIds = new ArrayList<>();
    private List<Collection> collectionSnapshots = new ArrayList<>();
    private List<Search> snapshots = new ArrayList<>();
    private List<String> searchIds = new ArrayList<>();
    private static final String TAG = SearchActivity.class.getSimpleName();
    private SearchView searchView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
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
        collectionsCollection = FirebaseFirestore.getInstance().collection(Constants.USER_COLLECTIONS);
        postQuery = postsCollection;
        collectionQuery = collectionsCollection;

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_search, menu);
        searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchAdapter.cleanUp();
                snapshots.clear();
                getUsers(query);
//                getPostAndCollections(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchAdapter.cleanUp();
                snapshots.clear();
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
            searchAdapter.cleanUp();
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
                        for (final DocumentChange change : documentSnapshots.getDocumentChanges()) {
                            Andeqan andeqan = change.getDocument().toObject(Andeqan.class);
                            final String userId = andeqan.getUser_id();
                            final String username = andeqan.getUsername();
                            if (!TextUtils.isEmpty(username)){
                                final String usernameLowercase = username.toLowerCase();
                                if (usernameLowercase.contains(name.toLowerCase()) &&
                                        !userId.equals(firebaseAuth.getCurrentUser().getUid())){
                                    Search search = new Search();
                                    search.setWord(name.toLowerCase());
                                    search.setType("person");
                                    search.setCount(documentSnapshots.size());
                                    search.setId(userId);
                                    snapshots.add(search);
                                    searchIds.add(change.getDocument().getId());
                                    searchAdapter.notifyItemInserted(snapshots.size() - 1);

                                }
                            }

                        }

                    }
                }
            });
        }
    }


    private void getPostAndCollections(final String name) {
        if (name.isEmpty()){
            searchAdapter.cleanUp();
        }else {
            postQuery.orderBy("title")
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
                                    Post post = change.getDocument().toObject(Post.class);
                                    final String postId = post.getPost_id();
                                    final String title = post.getTitle();
                                    if (!TextUtils.isEmpty(title)){
                                        final String postTitle = title.toLowerCase();
                                        if (postTitle.contains(name.toLowerCase())){
                                            List<Post> titleSnapshots = new ArrayList<>();
                                            titleSnapshots.add(post);

                                            for (Post searchedPost : titleSnapshots){
                                                final String searchedTitle = searchedPost.getTitle();
                                                final String splittedTitle [] = searchedTitle.split(" ");

                                                for (int i = 0; i <= splittedTitle.length; i++){

                                                    try {
                                                        final String searchedWord = splittedTitle[i];
                                                        Log.d("searched word", searchedWord + "");
                                                        if (searchedWord.toLowerCase().contains(name.toLowerCase())){
                                                            Search search = new Search();
                                                            search.setWord(searchedWord.toLowerCase());
                                                            search.setType("person");
                                                            search.setCount(titleSnapshots.size());
                                                            search.setId(postId);
                                                            snapshots.add(search);
                                                            searchIds.add(change.getDocument().getId());
                                                            searchAdapter.notifyItemInserted(snapshots.size() - 1);
                                                        }
                                                    }catch (Exception ex){
                                                        ex.printStackTrace();
                                                    }

                                                }

                                            }
                                        }
                                    }

                                }

                            }
                        }
                    });

            postQuery.orderBy("description")
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
                                    Post post = change.getDocument().toObject(Post.class);
                                    final String postId = post.getPost_id();
                                    final String description = post.getDescription();
                                    if (!TextUtils.isEmpty(description)){
                                        final String postDescription = description.toLowerCase();
                                        if (postDescription.contains(name.toLowerCase())){
                                            List<Post> descriptionSnapshots = new ArrayList<>();
                                            descriptionSnapshots.add(post);
                                            for (Post searchedPost : descriptionSnapshots){
                                                final String searchedPostDescription = searchedPost.getDescription();
                                                final String splittedDescription [] = searchedPostDescription.split(" ");
                                                for (int i = 0; i <= splittedDescription.length; i++){
                                                   try {
                                                       final String searchedWord = splittedDescription[i];
                                                       Log.d("searched word", searchedWord + "");

                                                       if (searchedWord.toLowerCase().contains(name.toLowerCase())){
                                                           Search search = new Search();
                                                           search.setWord(searchedWord.toLowerCase());
                                                           search.setType("person");
                                                           search.setCount(descriptionSnapshots.size());
                                                           search.setId(postId);
                                                           snapshots.add(search);
                                                           searchIds.add(change.getDocument().getId());
                                                           searchAdapter.notifyItemInserted(snapshots.size() - 1);
                                                       }
                                                   }catch (Exception ex){
                                                       ex.printStackTrace();
                                                   }
                                                }

                                            }
                                        }
                                    }

                                }



                            }
                        }
                    });

            collectionQuery.orderBy("name")
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
                                    Collection collection = change.getDocument().toObject(Collection.class);
                                    final String collectionId = collection.getCollection_id();
                                    final String collectionName = collection.getName();
                                    if (!TextUtils.isEmpty(collectionName)){
                                        final String postDescription = collectionName.toLowerCase();
                                        if (postDescription.contains(name.toLowerCase())){
                                            List<Collection> nameSnapshots = new ArrayList<>();
                                            nameSnapshots.add(collection);

                                            for (Collection searchedCollection : nameSnapshots){
                                                final String searchedCollectionName = searchedCollection.getName();
                                                final String splittedName [] = searchedCollectionName.split(" ");
                                                for (int i = 0; i <= splittedName.length; i++){

                                                    try {
                                                        final String searchedWord = splittedName[i];
                                                        if (searchedWord.toLowerCase().contains(name.toLowerCase())){
                                                            Search search = new Search();
                                                            search.setWord(searchedWord.toLowerCase());
                                                            search.setType("collection");
                                                            search.setId("searched_collection");
                                                            search.setCount(nameSnapshots.size());
                                                            snapshots.add(search);
                                                            searchIds.add("searched_collection");
                                                            searchAdapter.notifyItemInserted(snapshots.size() - 1);

                                                        }
                                                    }catch (Exception ex){
                                                        ex.printStackTrace();
                                                    }

                                                }

                                            }
                                        }
                                    }
                                }

                            }

                        }
                    });

            collectionQuery.orderBy("note")
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
                                    Collection collection = change.getDocument().toObject(Collection.class);
                                    final String collectionId = collection.getCollection_id();
                                    final String collectionNote = collection.getNote();
                                    if (!TextUtils.isEmpty(collectionNote)){
                                        final String note = collectionNote.toLowerCase();
                                        if (note.contains(name.toLowerCase())){
                                            List<Collection> notenapshots = new ArrayList<>();
                                            notenapshots.add(collection);
                                            for (Collection searchedCollection : notenapshots){
                                                final String searchedNote = searchedCollection.getNote();
                                                final String splittedNote [] = searchedNote.split(" ");
                                                for (int i = 0; i <= splittedNote.length; i++){

                                                    try {
                                                        final String searchedWord = splittedNote[i];
                                                        if (searchedWord.toLowerCase().contains(name.toLowerCase())){
                                                            Search search = new Search();
                                                            search.setWord(searchedWord.toLowerCase());
                                                            search.setType("collection");
                                                            search.setId("searched_collection");
                                                            search.setCount(notenapshots.size());
                                                            snapshots.add(search);
                                                            searchIds.add("searched_collection");
                                                            searchAdapter.notifyItemInserted(snapshots.size() - 1);

                                                        }
                                                    }catch (Exception ex){
                                                        ex.printStackTrace();
                                                    }
                                                }

                                            }
                                        }
                                    }
                                }
                            }

                        }
                    });

        }
    }


    private void setSearchRecyclerView(){
        searchAdapter = new SearchOverallAdapter(this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        searchAdapter.setResults(snapshots);
        mUsersRecyclerView.setAdapter(searchAdapter);
        mUsersRecyclerView.setHasFixedSize(false);
        mUsersRecyclerView.setLayoutManager(layoutManager);
    }


}


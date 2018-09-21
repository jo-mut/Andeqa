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

import com.andeqa.andeqa.R;
import com.andeqa.andeqa.models.Collection;
import com.andeqa.andeqa.models.Post;
import com.andeqa.andeqa.models.Search;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;

public class SearchedPostsActivity extends AppCompatActivity {
    @Bind(R.id.searchedPostRecyclerView)RecyclerView searchedPostRecyclerView;
    @Bind(R.id.toolbar)Toolbar toolbar;
    private CollectionReference usersCollections;
    private CollectionReference postsCollection;
    private CollectionReference collectionsCollection;
    private Query usersQuery;
    private Query postQuery;
    private Query collectionQuery;
    private SearchedPostsAdapter searchedPostsAdapter;
    private FirebaseAuth firebaseAuth;
    private List<String> documentIds = new ArrayList<>();
    private List<Collection> collectionSnapshots = new ArrayList<>();
    private List<Search> snapshots = new ArrayList<>();
    private List<String> searchIds = new ArrayList<>();
    private static final String TAG = SearchActivity.class.getSimpleName();
    private SearchView searchView;
    private static final String SEARCH_KEY_WORD = "search key word";
    private String searchWord;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searched_posts);

        searchedPosts(searchWord);
    }

    public SearchedPostsActivity() {
        super();
        setSearchRecyclerView();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void setSearchRecyclerView(){
        searchedPostsAdapter = new SearchedPostsAdapter(this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        searchedPostsAdapter.setRandomPosts(snapshots);
        searchedPostRecyclerView.setAdapter(searchedPostsAdapter);
        searchedPostRecyclerView.setHasFixedSize(false);
        searchedPostRecyclerView.setLayoutManager(layoutManager);
    }



    private void searchedPosts(final String name){
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
                                                        searchedPostsAdapter.notifyItemInserted(snapshots.size() - 1);
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
                                                        searchedPostsAdapter.notifyItemInserted(snapshots.size() - 1);
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

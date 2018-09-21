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
import butterknife.ButterKnife;

public class SearchedCollectionsActivity extends AppCompatActivity {
    @Bind(R.id.searchedCollectionsRecyclerView)RecyclerView searchedCollectionsRecyclerView;
    @Bind(R.id.toolbar)Toolbar toolbar;
    private CollectionReference usersCollections;
    private CollectionReference postsCollection;
    private CollectionReference collectionsCollection;
    private Query usersQuery;
    private Query postQuery;
    private Query collectionQuery;
    private SearchedCollectionsAdapter searchedCollectionsAdapter;
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
        setContentView(R.layout.activity_searched_collections);
        ButterKnife.bind(this);

        if (getIntent().getExtras() != null){
            searchWord = getIntent().getStringExtra(SEARCH_KEY_WORD);
            searchedCollections(searchWord);
        }

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

    private void setSearchRecyclerView(){
        searchedCollectionsAdapter = new SearchedCollectionsAdapter(this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        searchedCollectionsAdapter.setSearches(snapshots);
        searchedCollectionsRecyclerView.setAdapter(searchedCollectionsAdapter);
        searchedCollectionsRecyclerView.setHasFixedSize(false);
        searchedCollectionsRecyclerView.setLayoutManager(layoutManager);
    }


    private void searchedCollections(final String name){
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
                                                        searchedCollectionsAdapter.notifyItemInserted(snapshots.size() - 1);

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

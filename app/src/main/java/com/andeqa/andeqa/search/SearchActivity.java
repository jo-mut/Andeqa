package com.andeqa.andeqa.search;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.R;
import com.andeqa.andeqa.main.HomeActivity;
import com.andeqa.andeqa.message.MessagesActivity;
import com.andeqa.andeqa.models.Andeqan;
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

public class SearchActivity extends AppCompatActivity {
    @Bind(R.id.usersRecyclerView)RecyclerView mUsersRecyclerView;
    @Bind(R.id.toolbar)Toolbar toolbar;
    private CollectionReference usersCollections;
    private Query usersQuery;
    private SearchAdapter searchAdapter;
    private FirebaseAuth firebaseAuth;
    private List<String> roomIds = new ArrayList<>();
    private List<DocumentSnapshot> documentSnapshots = new ArrayList<>();
    private static final String TAG = SearchActivity.class.getSimpleName();


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
                finish();
            }
        });
        //initialize firebase auth
        firebaseAuth = FirebaseAuth.getInstance();

        usersCollections = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_search, menu);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
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

    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    protected void onDocumentAdded(DocumentChange change) {
        roomIds.add(change.getDocument().getId());
        documentSnapshots.add(change.getDocument());
        searchAdapter.setPeople(documentSnapshots);
        searchAdapter.notifyItemInserted(documentSnapshots.size() -1);
        searchAdapter.getItemCount();

    }

    protected void onDocumentModified(DocumentChange change) {
        if (change.getOldIndex() == change.getNewIndex()) {
            // Item changed but remained in same position
            documentSnapshots.set(change.getOldIndex(), change.getDocument());
            searchAdapter.notifyItemChanged(change.getOldIndex());
        } else {
            // Item changed and changed position
            documentSnapshots.remove(change.getOldIndex());
            documentSnapshots.add(change.getNewIndex(), change.getDocument());
            searchAdapter.notifyItemRangeChanged(0, documentSnapshots.size());
        }
    }

    protected void onDocumentRemoved(DocumentChange change) {
        try {
            documentSnapshots.remove(change.getOldIndex());
            searchAdapter.notifyItemRemoved(change.getOldIndex());
            searchAdapter.notifyItemRangeChanged(0, documentSnapshots.size());
        }catch (Exception e){
            e.printStackTrace();
        }
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
                            switch (change.getType()) {
                                case ADDED:
                                    Andeqan andeqan = change.getDocument().toObject(Andeqan.class);
                                    final String userId = andeqan.getUser_id();
                                    final String username = andeqan.getUsername();
                                    if (username.contains(name) &&
                                            !userId.equals(firebaseAuth.getCurrentUser().getUid())){
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

            searchAdapter = new SearchAdapter(this);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
            mUsersRecyclerView.setAdapter(searchAdapter);
            mUsersRecyclerView.setHasFixedSize(false);
            mUsersRecyclerView.setLayoutManager(linearLayoutManager);
        }
    }
    
}


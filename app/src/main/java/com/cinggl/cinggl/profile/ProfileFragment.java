package com.cinggl.cinggl.profile;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.cinggl.cinggl.Constants;
import com.cinggl.cinggl.R;
import com.cinggl.cinggl.adapters.FirebaseCingleOutViewHolder;
import com.cinggl.cinggl.adapters.FirebaseProfileCinglesViewHolder;
import com.cinggl.cinggl.home.HomeActivity;
import com.cinggl.cinggl.models.Cingle;
import com.cinggl.cinggl.models.Cingulan;
import com.cinggl.cinggl.ui.FirebaseUtil;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {
    @Bind(R.id.accountUsernameTextView)TextView mAccountUsernameTextView;
    @Bind(R.id.profileImageView)ImageView mProfileImageView;
    @Bind(R.id.bioTextView)TextView mBioTextView;
    @Bind(R.id.profileCinglesRecyclerView)RecyclerView mProfileCinglesRecyclerView;

    private DatabaseReference databaseReference;
    private FirebaseRecyclerAdapter firebaseRecyclerAdapter;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;


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

        firebaseAuth = FirebaseAuth.getInstance();

        setUpFirebaseAdapter();

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_layout, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    private void setUpFirebaseAdapter(){
        databaseReference = FirebaseDatabase.getInstance()
                .getReference(Constants.FIREBASE_PUBLIC_CINGLES);
        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Cingle, FirebaseProfileCinglesViewHolder>
                (Cingle.class, R.layout.profile_cingles_item_list, FirebaseProfileCinglesViewHolder.class, databaseReference) {
            @Override
            protected void populateViewHolder(FirebaseProfileCinglesViewHolder viewHolder, Cingle model, int position) {
                viewHolder.bindProfileCingle(model);


            }
        };
        mProfileCinglesRecyclerView.setAdapter(firebaseRecyclerAdapter);
        mProfileCinglesRecyclerView.setHasFixedSize(false);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getContext(), 3);
        layoutManager.setAutoMeasureEnabled(true);
        mProfileCinglesRecyclerView.setLayoutManager(layoutManager);
    }

//    private void setUpFirebaseAdapter(){
////        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
////        String uid = user.getUid();
//
//        Query query = FirebaseDatabase.getInstance()
//                .getReference(Constants.FIREBASE_PUBLIC_CINGLES).addValueEventListener(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(DataSnapshot dataSnapshot) {
//
//                    }
//
//                    @Override
//                    public void onCancelled(DatabaseError databaseError) {
//
//                    }
//                })
//    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        firebaseRecyclerAdapter.cleanup();
    }

    @Override
    public void onStop(){
        super.onStop();
    }

}

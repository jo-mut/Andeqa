package com.cinggl.cinggl.profile;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.cinggl.cinggl.Constants;
import com.cinggl.cinggl.R;
import com.cinggl.cinggl.adapters.FirebaseProfileCinglesViewHolder;
import com.cinggl.cinggl.adapters.ProfileInfoViewHolder;
import com.cinggl.cinggl.models.Cingle;
import com.cinggl.cinggl.models.Cingulan;
import com.cinggl.cinggl.ui.SettingsActivity;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment{
    @Bind(R.id.profileCinglesRecyclerView)RecyclerView mProfileCinglesRecyclerView;
    @Bind(R.id.profileInfoRecyclerView)RecyclerView mProfileInfoRecyclerView;

    private DatabaseReference databaseReference;
    private DatabaseReference usersRef;
    private FirebaseRecyclerAdapter firebaseRecyclerAdapter;
    private Query profileCinglesQuery;
    private Query profileInfoQuery;
    public String userKey;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;


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
        firebaseUser = firebaseAuth.getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CINGLES);
        profileCinglesQuery = databaseReference.orderByChild("uid").equalTo(firebaseAuth.getCurrentUser().getUid());
        databaseReference = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_USERS);
        profileInfoQuery = databaseReference.orderByChild("uid").equalTo(firebaseAuth.getCurrentUser().getUid());

        setUpProfile();
        setUpFirebaseAdapter();

        databaseReference.keepSynced(true);

        return view;
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            launchSettings();
            return true;
        }

        if(id == R.id.action_search){
            return true;
        }

        if(id == R.id.action_notifications){
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void launchSettings(){
        Intent intentSettings = new Intent(getActivity(), SettingsActivity.class);
        startActivity(intentSettings);
    }


    private void setUpFirebaseAdapter(){

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Cingle, FirebaseProfileCinglesViewHolder>
                (Cingle.class, R.layout.profile_cingles_item_list, FirebaseProfileCinglesViewHolder.class, profileCinglesQuery) {
            @Override
            protected void populateViewHolder(FirebaseProfileCinglesViewHolder viewHolder, Cingle model, int position) {
                viewHolder.bindProfileCingle(model);
                final String postKey = getRef(position).getKey();

                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getActivity(), CingleDetailActivity.class);
                        intent.putExtra("cingle_id", postKey);
                        startActivity(intent);
                    }
                });


            }
        };

        mProfileCinglesRecyclerView.setAdapter(firebaseRecyclerAdapter);
        mProfileCinglesRecyclerView.setHasFixedSize(false);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getContext(), 3);
        layoutManager.setAutoMeasureEnabled(true);
        mProfileCinglesRecyclerView.setNestedScrollingEnabled(false);
        mProfileCinglesRecyclerView.setLayoutManager(layoutManager);

    }

    public void setUpProfile(){
        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Cingulan, ProfileInfoViewHolder>
                (Cingulan.class, R.layout.profile_info_viewholder, ProfileInfoViewHolder.class, profileInfoQuery) {
            @Override
            protected void populateViewHolder(ProfileInfoViewHolder viewHolder, Cingulan model, int position) {
                viewHolder.bindProfileInfo(model);
                DatabaseReference cingleRef = getRef(position);
                userKey = cingleRef.getKey();


            }
        };

        mProfileInfoRecyclerView.setAdapter(firebaseRecyclerAdapter);
        mProfileInfoRecyclerView.setHasFixedSize(false);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setAutoMeasureEnabled(true);
        mProfileInfoRecyclerView.setLayoutManager(layoutManager);

    }


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

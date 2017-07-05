package com.cinggl.cinggl.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cinggl.cinggl.Constants;
import com.cinggl.cinggl.R;
import com.cinggl.cinggl.models.Cingulan;
import com.cinggl.cinggl.models.Like;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by J.EL on 7/3/2017.
 */

public class PeopleAdapter extends RecyclerView.Adapter<PeopleViewHolder> {
    private Context mContext;
    private DatabaseReference mDatabaseReference;
    private ChildEventListener mChildEventListener;
    private DatabaseReference cingulansRef;
    private DatabaseReference followersRef;
    private DatabaseReference usernameRef;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;

    private static final String TAG = PeopleAdapter.class.getSimpleName();

    private List<String> mCingulanIds = new ArrayList<>();
    private List<Cingulan> cingulans  = new ArrayList<>();

    public PeopleAdapter(final Context context, DatabaseReference reference) {
        mContext = context;
        mDatabaseReference = reference;
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();



        usernameRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_USERS);
        followersRef = FirebaseDatabase.getInstance().getReference(Constants.FOLLOWERS);
        cingulansRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_USERS);

        DatabaseReference ref = usernameRef;
        final String refKey = ref.getKey();
        Log.d(refKey, "refKey");

        followersRef.child(refKey).addValueEventListener(new ValueEventListener() {
            DatabaseReference followerRef = followersRef.child(refKey);
            final String followerKey = followerRef.getKey();
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String uid = (String) dataSnapshot.child(followerKey).child("uid").getValue();

                cingulansRef.child(followerKey).addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        Cingulan cingulan = dataSnapshot.getValue(Cingulan.class);
                        mCingulanIds.add(dataSnapshot.getKey());
                        cingulans.add(cingulan);
                        notifyItemInserted(cingulans.size() - 1);
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void cleanUpListener(){
        if(mChildEventListener != null){
            mDatabaseReference.removeEventListener(mChildEventListener);
        }
    }

    @Override
    public void onBindViewHolder(PeopleViewHolder holder, int position) {
        Cingulan cingulan = cingulans.get(position);
        holder.bindPeople(cingulan);
    }

    @Override
    public PeopleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view =  inflater.inflate(R.layout.followers_list, parent, false);
        return  new PeopleViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return cingulans.size();
    }
}

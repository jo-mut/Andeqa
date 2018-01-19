package com.cinggl.cinggl.registration;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cinggl.cinggl.Constants;
import com.cinggl.cinggl.R;
import com.cinggl.cinggl.firestore.FirestoreAdapter;
import com.cinggl.cinggl.models.Cinggulan;
import com.cinggl.cinggl.models.Relation;
import com.cinggl.cinggl.people.FollowCinggulansViewHolder;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;


/**
 * Created by J.EL on 12/15/2017.
 */

public class CinggulansAdapter extends FirestoreAdapter<FollowCinggulansViewHolder> {
    private static final String TAG = CinggulansAdapter.class.getSimpleName();
    private CollectionReference relationsReference;
    private FirebaseAuth firebaseAuth;
    private Context mContext;
    private boolean processFollow = false;

    public CinggulansAdapter(Query query, Context mContext) {
        super(query);
        this.mContext = mContext;
    }

    @Override
    public FollowCinggulansViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.intro_people_layout, parent, false);
        return new FollowCinggulansViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final FollowCinggulansViewHolder holder, int position) {
        holder.bindCinggulans(getSnapshot(position));
        final Cinggulan cinggulan = getSnapshot(position).toObject(Cinggulan.class);
        final String uid = cinggulan.getUid();
        Log.d("follow cinggulan uid", uid);

        firebaseAuth = FirebaseAuth.getInstance();
        relationsReference = FirebaseFirestore.getInstance().collection(Constants.RELATIONS);


        relationsReference.document("following").collection(uid)
                .whereEqualTo("uid", firebaseAuth.getCurrentUser().getUid())
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen error", e);
                            return;
                        }

                        Log.d("relations size", documentSnapshots.size() + "");

                        if (documentSnapshots.isEmpty()){
                            holder.mFollowButton.setText("FOLLOW");
                        }else {
                            holder.mFollowButton.setText("FOLLOWING");
                        }
                    }
                });

        if (uid.equals(firebaseAuth.getCurrentUser().getUid())){
            holder.mFollowButtonRelativeLayout.setVisibility(View.GONE);
        }else {
            holder.mFollowButton.setVisibility(View.VISIBLE);
            holder.mFollowButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    processFollow = true;
                    relationsReference.document("followers")
                            .collection(uid).whereEqualTo("uid", firebaseAuth.getCurrentUser().getUid())
                            .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                @Override
                                public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {


                                    if (e != null) {
                                        Log.w(TAG, "Listen error", e);
                                        return;
                                    }

                                    if (processFollow){
                                        if (documentSnapshots.isEmpty()){
                                            Relation follower = new Relation();
                                            follower.setUid(firebaseAuth.getCurrentUser().getUid());
                                            relationsReference.document("followers").collection(uid)
                                                    .document(firebaseAuth.getCurrentUser().getUid()).set(follower)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            final Relation following = new Relation();
                                                            following.setUid(uid);
                                                            relationsReference.document("following").collection(firebaseAuth
                                                                    .getCurrentUser().getUid()).document(uid).set(following);
                                                            relationsReference.document("following").collection(firebaseAuth.getCurrentUser().getUid())
                                                                    .whereEqualTo("uid", firebaseAuth.getCurrentUser().getUid())
                                                                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                                                        @Override
                                                                        public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                                                                            if (e != null) {
                                                                                Log.w(TAG, "Listen error", e);
                                                                                return;
                                                                            }

                                                                            //set the
                                                                            if (documentSnapshots.isEmpty()){
                                                                                final Relation myUid = new Relation();
                                                                                myUid.setUid(firebaseAuth.getCurrentUser().getUid());
                                                                                relationsReference.document("following").collection(firebaseAuth.getCurrentUser().getUid())
                                                                                        .document().set(myUid);
                                                                            }
                                                                        }
                                                                    });

                                                        }
                                                    });
                                            processFollow = false;
                                            holder.mFollowButton.setText("Following");
                                        }else {
                                            relationsReference.document("followers").collection(uid)
                                                    .document(firebaseAuth.getCurrentUser().getUid()).delete()
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            relationsReference.document("following").collection(firebaseAuth.getCurrentUser()
                                                                    .getUid()).document(uid).delete();
                                                        }
                                                    });
                                            processFollow = false;
                                            holder.mFollowButton.setText("Follow");
                                        }
                                    }
                                }
                            });

                }
            });
        }

    }


    @Override
    protected void onDocumentAdded(DocumentChange change) {
        super.onDocumentAdded(change);

    }

    @Override
    protected void onDocumentModified(DocumentChange change) {
        super.onDocumentModified(change);
    }

    @Override
    protected void onDocumentRemoved(DocumentChange change) {
        super.onDocumentRemoved(change);

    }

    @Override
    protected void onError(FirebaseFirestoreException e) {
        super.onError(e);
    }

    @Override
    protected void onDataChanged() {
        super.onDataChanged();
    }

    @Override
    protected DocumentSnapshot getSnapshot(int index) {
        return super.getSnapshot(index);
    }

    @Override
    public int getItemCount() {
        return super.getItemCount();
    }

}

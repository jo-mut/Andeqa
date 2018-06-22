package com.andeqa.andeqa.settings;


import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class ConfirmDeleteFragment extends DialogFragment implements View.OnClickListener{
    @Bind(R.id.noRelativeLayout)RelativeLayout mNoRelativeLayout;
    @Bind(R.id.YesRelativeLayout)RelativeLayout mYesRelativeLayout;

    private CollectionReference sellingCollection;
    private CollectionReference postsCollection;
    private CollectionReference collectionsPosts;
    private CollectionReference senseCreditReference;
    private CollectionReference marketCollections;
    private CollectionReference postOnwersCollection;
    private FirebaseAuth firebaseAuth;

    private String mPostId;
    private String mCollectionId;
    private String mType;
    private static final String COLLECTION_ID = "collection id";
    private static final String EXTRA_POST_ID = "post id";
    private static final String TYPE = "type";
    private static final String TAG = ConfirmDeleteFragment.class.getSimpleName();
    private ProgressDialog progressDialog;

    public static ConfirmDeleteFragment newInstance(String title){
        ConfirmDeleteFragment confirmDeleteFragment = new ConfirmDeleteFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        confirmDeleteFragment.setArguments(args);
        return confirmDeleteFragment;

    }


    public ConfirmDeleteFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_confirm_delete, container, false);
        ButterKnife.bind(this, view);

        mNoRelativeLayout.setOnClickListener(this);
        mYesRelativeLayout.setOnClickListener(this);

        firebaseAuth = FirebaseAuth.getInstance();

        Bundle bundle = getArguments();
        if (bundle != null){
            mCollectionId = bundle.getString(ConfirmDeleteFragment.COLLECTION_ID);
            mPostId = bundle.getString(ConfirmDeleteFragment.EXTRA_POST_ID);
            mType = bundle.getString(ConfirmDeleteFragment.TYPE);

        }

        if (firebaseAuth.getCurrentUser() != null){
            sellingCollection = FirebaseFirestore.getInstance().collection(Constants.SELLING);
            //firestore
            postsCollection = FirebaseFirestore.getInstance().collection(Constants.POSTS);
            //firestore
            if (mType.equals("single")){
                collectionsPosts = FirebaseFirestore.getInstance().collection(Constants.COLLECTIONS_POSTS)
                        .document("singles").collection(mCollectionId);
            }else{
                collectionsPosts = FirebaseFirestore.getInstance().collection(Constants.COLLECTIONS_POSTS)
                        .document("collections").collection(mCollectionId);
            }

            senseCreditReference = FirebaseFirestore.getInstance().collection(Constants.CREDITS);
            marketCollections = FirebaseFirestore.getInstance().collection(Constants.SELLING);
            postOnwersCollection = FirebaseFirestore.getInstance().collection(Constants.POST_OWNERS);

            updatingSalePrice();
        }

        return view;
    }


    public void updatingSalePrice(){
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Deleting from the market...");
        progressDialog.setCancelable(true);
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Dialog dialog = getDialog();

        if (dialog != null){
            String title = getArguments().getString("title", "post market settings");
            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }


    @Override
    public void onClick(View v){
        if (v == mYesRelativeLayout){
            progressDialog.show();

            try {
                progressDialog.show();
            }catch (Exception e){
                e.printStackTrace();
            }

            collectionsPosts.document(mPostId)
                    .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                            if (e != null) {
                                Log.w(TAG, "Listen error", e);
                                return;
                            }

                            if (documentSnapshot.exists()){
                                postsCollection.document(mPostId)
                                        .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                            @Override
                                            public void onEvent(DocumentSnapshot documentSnapshot,
                                                                FirebaseFirestoreException e) {
                                                if (e != null) {
                                                    Log.w(TAG, "Listen error", e);
                                                    return;
                                                }

                                                if (documentSnapshot.exists()){
                                                    postsCollection.document(mPostId).delete();
                                                }
                                            }
                                        });

                                marketCollections.document(mPostId)
                                        .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                            @Override
                                            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                                                if (e != null) {
                                                    Log.w(TAG, "Listen error", e);
                                                    return;
                                                }


                                                if (documentSnapshot.exists()) {
                                                    marketCollections.document(mPostId).delete();

                                                }
                                            }
                                        });

                                senseCreditReference.document(mPostId)
                                        .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                            @Override
                                            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                                                if (e != null) {
                                                    Log.w(TAG, "Listen error", e);
                                                    return;
                                                }

                                                if (documentSnapshot.exists()){
                                                    senseCreditReference.document(mPostId).delete();


                                                }
                                            }
                                        });

                                postOnwersCollection.document(mPostId)
                                        .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                            @Override
                                            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                                                if (e != null) {
                                                    Log.w(TAG, "Listen error", e);
                                                    return;
                                                }

                                                if (documentSnapshot.exists()){
                                                    postOnwersCollection.document(mPostId).delete();

                                                }
                                            }
                                        });

                                collectionsPosts.document(mPostId).delete()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                try {
                                                    progressDialog.dismiss();
                                                }catch (Exception e){
                                                    e.printStackTrace();
                                                }

                                            }
                                        });

                            }
                        }
                    });

        }

        if (v == mNoRelativeLayout){
            dismiss();
        }
    }

}

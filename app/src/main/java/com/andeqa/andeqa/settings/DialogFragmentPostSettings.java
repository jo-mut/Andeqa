package com.andeqa.andeqa.settings;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.RelativeLayout;

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.R;
import com.andeqa.andeqa.market.ListOnMarketActivity;
import com.andeqa.andeqa.market.RedeemCreditsActivity;
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
 * Created by J.EL on 2/6/2018.
 */

public class DialogFragmentPostSettings extends DialogFragment implements View.OnClickListener {

    @Bind(R.id.deletePostRelativeLayout)RelativeLayout mDeleteCingleRelativeLayout;
    @Bind(R.id.tradePostRelativeLayout)RelativeLayout mTradeCingleRelativeLayout;
    @Bind(R.id.redeemCreditsRelativeLayout)RelativeLayout mRedeemCreditsRelativeLayout;

    private String mPostId;
    //firebase auth
    private FirebaseAuth firebaseAuth;
    //firestore
    private CollectionReference postsCollection;
    private CollectionReference collectionsPosts;
    private CollectionReference senseCreditReference;
    private CollectionReference marketCollections;
    private CollectionReference postOnwersCollection;
    private static final String TAG = DialogFragmentPostSettings.class.getSimpleName();

    private static final String COLLECTION_ID = "collection id";
    private static final String EXTRA_POST_ID = "post id";
    private String mCollectionId;
    private String mUid;
    private static final String EXTRA_USER_UID = "uid";
    private ProgressDialog progressDialog;


    public static DialogFragmentPostSettings newInstance(String title){
        DialogFragmentPostSettings dialogFragmentPostSettings = new DialogFragmentPostSettings();
        Bundle args = new Bundle();
        args.putString("title", title);
        dialogFragmentPostSettings.setArguments(args);
        return dialogFragmentPostSettings;

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_fragment_post_settings, container, false);

        ButterKnife.bind(this, view);

        firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() != null){
            mDeleteCingleRelativeLayout.setOnClickListener(this);
            mTradeCingleRelativeLayout.setOnClickListener(this);
            mRedeemCreditsRelativeLayout.setOnClickListener(this);

            Bundle bundle = getArguments();
            if (bundle != null){
                mCollectionId = bundle.getString(DialogFragmentPostSettings.COLLECTION_ID);
                mPostId = bundle.getString(DialogFragmentPostSettings.EXTRA_POST_ID);

                Log.d("the passed poskey", mPostId);

            }else {
                throw new IllegalArgumentException("pass an EXTRA_POST_KEY");
            }

            //firestore
            postsCollection = FirebaseFirestore.getInstance().collection(Constants.POSTS);
            collectionsPosts = FirebaseFirestore.getInstance().collection(Constants.COLLECTIONS_POSTS)
                    .document("collections").collection(mCollectionId);
            senseCreditReference = FirebaseFirestore.getInstance().collection(Constants.CREDITS);
            marketCollections = FirebaseFirestore.getInstance().collection(Constants.SELLING);
            postOnwersCollection = FirebaseFirestore.getInstance().collection(Constants.POST_OWNERS);

            showSaleLayout();
            deletePostDialog();

        }

        return view;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Dialog dialog = getDialog();

        if (dialog != null){
            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }
    }

    @Override
    public void onClick(View v){
        if (v == mDeleteCingleRelativeLayout){
            deleteCingle();
        }

        if (v == mTradeCingleRelativeLayout){
            Intent intent = new Intent(getActivity(), ListOnMarketActivity.class);
            intent.putExtra(DialogFragmentPostSettings.EXTRA_POST_ID, mPostId);
            intent.putExtra(DialogFragmentPostSettings.COLLECTION_ID, mCollectionId);
            startActivity(intent);
        }

        if (v == mRedeemCreditsRelativeLayout){
            //LAUCH THE DIALOG TO REDEEM CREDITS
            Intent intent = new Intent(getActivity(), RedeemCreditsActivity.class);
            intent.putExtra(DialogFragmentPostSettings.EXTRA_POST_ID, mPostId);
            intent.putExtra(DialogFragmentPostSettings.COLLECTION_ID, mCollectionId);
            startActivity(intent);
        }

    }

    public void showSaleLayout(){
        marketCollections.document(mPostId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (documentSnapshot.exists()){
                    mTradeCingleRelativeLayout.setVisibility(View.GONE);
                }else {
                    mTradeCingleRelativeLayout.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    public void deletePostDialog(){
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Deleting your post...");
        progressDialog.setCancelable(false);
    }


    public void deleteCingle(){
        // delete post in collection and delete post in overall document

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

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}
